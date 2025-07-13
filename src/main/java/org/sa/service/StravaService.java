package org.sa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.sa.config.Props;
import org.sa.dto.LocalLegendInfoDTO;
import org.sa.dto.SegmentDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class StravaService {

  public String getAccessToken(String refreshToken) {
    try {
      URL url = new URL("https://www.strava.com/oauth/token");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setDoOutput(true);

      String requestBody = "client_id=" + Props.STRAVA_CLIENT_ID +
          "&client_secret=" + Props.STRAVA_CLIENT_SECRET +
          "&grant_type=refresh_token" +
          "&refresh_token=" + refreshToken;


      try (OutputStream os = conn.getOutputStream()) {
        os.write(requestBody.getBytes(StandardCharsets.UTF_8));
      }

      if (conn.getResponseCode() != 200)
        throw new RuntimeException("Token refresh failed: HTTP " + conn.getResponseCode());

      JsonNode json = new ObjectMapper().readTree(conn.getInputStream());
      return json.get("access_token").asText();
    } catch (Exception e) {
      throw new RuntimeException("Failed to get access token: " + e.getMessage(), e);
    }
  }

  public String getStarredSegmentsJson() {
    try {
      URL url = new URL("https://www.strava.com/api/v3/segments/starred?page=1&per_page=200");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Bearer " + getAccessToken(Props.STRAVA_REFRESH_TOKEN));

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        return reader.lines().reduce("", (a, b) -> a + b);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch starred segments: " + e.getMessage(), e);
    }
  }

  public List<SegmentDTO> parseSegments(String json) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper.readValue(json, new TypeReference<>() {});
  }

  public List<SegmentDTO> getStarredSegments() throws IOException {
    return parseSegments(getStarredSegmentsJson());
  }

  public List<List<Double>> getSegmentPoints(long segmentId) {
    try {
      URL url = new URL("https://www.strava.com/api/v3/segments/" + segmentId);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Bearer " + getAccessToken(Props.STRAVA_REFRESH_TOKEN));

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        String json = reader.lines().collect(Collectors.joining());
        JSONObject obj = new JSONObject(json);
        String googlePolylineFormat = obj.getJSONObject("map").getString("polyline"); // compressed segment track
        return PolylineUtil.decodePolyline(googlePolylineFormat);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch segment: " + e.getMessage(), e);
    }
  }

  public String getSegmentPolyline(long segmentId) {
    System.out.print("extracting polyline; segment id: " + segmentId);
    try {
      URL url = new URL("https://www.strava.com/api/v3/segments/" + segmentId);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Bearer " + getAccessToken(Props.STRAVA_REFRESH_TOKEN));

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        String json = reader.lines().collect(Collectors.joining());
        JSONObject obj = new JSONObject(json);
        String googlePolylineFormat = obj.getJSONObject("map").getString("polyline"); // compressed segment track
        System.out.println(" polyline: " + googlePolylineFormat);
        return googlePolylineFormat;
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch segment: " + e.getMessage(), e);
    }
  }

  public LocalLegendInfoDTO getLocalLegendInfo(long segmentId) {
    try {
      URL url = new URL("https://www.strava.com/api/v3/segments/" + segmentId);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Bearer " + getAccessToken(Props.STRAVA_REFRESH_TOKEN));

      if (conn.getResponseCode() != 200)
        throw new RuntimeException("Failed to fetch segment: HTTP " + conn.getResponseCode());

      JsonNode root = new ObjectMapper().readTree(conn.getInputStream());
      JsonNode legendNode = root.path("local_legend");
      if (legendNode.isMissingNode()) return null;

      return new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .treeToValue(legendNode, LocalLegendInfoDTO.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get Local Legend info: " + e.getMessage(), e);
    }
  }


  /** How to get authorizationCode:
   * 1. Open this link in browser:
   *    https://www.strava.com/oauth/authorize?client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=http://localhost/exchange_token&approval_prompt=force&scope=activity:read_all
   * 2. Authorize the app.
   * 3. Copy the 'code' param from the URL you're redirected to.
   * 4. Use that code as 'authorizationCode' in this method.
   *
   * Code is valid once and expires in a few minutes.
   * Returned refresh token is long-lived.
   */
  public String exchangeCodeForRefreshToken(String authorizationCode) {
    try {
      URL url = new URL("https://www.strava.com/oauth/token");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setDoOutput(true);

      String body = "client_id=" + Props.STRAVA_CLIENT_ID +
          "&client_secret=" + Props.STRAVA_CLIENT_SECRET +
          "&code=" + authorizationCode +
          "&grant_type=authorization_code";

      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes(StandardCharsets.UTF_8));
      }

      if (conn.getResponseCode() != 200)
        throw new RuntimeException("Failed to exchange code: HTTP " + conn.getResponseCode());

      JsonNode json = new ObjectMapper().readTree(conn.getInputStream());
      String refreshToken = json.get("refresh_token").asText();
      String accessToken = json.get("access_token").asText();
      String athleteUsername = json.get("athlete").get("username").asText();

      System.out.println("Access token: " + accessToken);
      System.out.println("Refresh token: " + refreshToken);
      System.out.println("Athlete: " + athleteUsername);
      return refreshToken;
    } catch (Exception e) {
      throw new RuntimeException("Failed to exchange authorization code: " + e.getMessage(), e);
    }
  }

  public long getMyRecentEffortCount(long segmentId) {
    try {
      String urlStr = "https://www.strava.com/api/v3/segment_efforts?segment_id=" + segmentId + "&per_page=200&athlete_id=" + Props.MY_ATHLETE_ID;
      URL url = new URL(urlStr);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Bearer " + getAccessToken(Props.STRAVA_REFRESH_TOKEN));

      if (conn.getResponseCode() != 200)
        throw new RuntimeException("Failed to fetch efforts: HTTP " + conn.getResponseCode());

      JsonNode root = new ObjectMapper().readTree(conn.getInputStream());
      LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
      return StreamSupport.stream(root.spliterator(), false)
          .filter(n -> {
            JsonNode dateNode = n.get("start_date_local");
            if (dateNode == null) return false;
            LocalDateTime date = LocalDateTime.parse(dateNode.asText().replace("Z", ""));
            return date.isAfter(ninetyDaysAgo);
          })
          .count();
    } catch (Exception e) {
      throw new RuntimeException("Failed to count recent efforts: " + e.getMessage(), e);
    }
  }



}
