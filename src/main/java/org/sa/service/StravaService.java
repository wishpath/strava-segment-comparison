package org.sa.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.sa.config.Props;
import org.sa.dto.SegmentDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class StravaService {

  public String getAccessToken() {
    try {
      URL url = new URL("https://www.strava.com/oauth/token");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setDoOutput(true);

      String requestBody = "client_id=" + Props.STRAVA_CLIENT_ID +
          "&client_secret=" + Props.STRAVA_CLIENT_SECRET +
          "&grant_type=refresh_token" +
          "&refresh_token=" + Props.STRAVA_REFRESH_TOKEN;

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
      conn.setRequestProperty("Authorization", "Bearer " + getAccessToken());

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
      conn.setRequestProperty("Authorization", "Bearer " + getAccessToken());

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        String response = reader.lines().collect(Collectors.joining());
        String googlePolylineFormat = extractPolyline(response); // compressed segment track
        return PolylineUtil.decodePolyline(googlePolylineFormat);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch segment: " + e.getMessage(), e);
    }
  }

  public String getSegmentPolyline(long segmentId) {
    try {
      URL url = new URL("https://www.strava.com/api/v3/segments/" + segmentId);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Authorization", "Bearer " + getAccessToken());

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        String response = reader.lines().collect(Collectors.joining());
        String googlePolylineFormat = extractPolyline(response); // compressed segment track
        return googlePolylineFormat;
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch segment: " + e.getMessage(), e);
    }
  }

  private static String extractPolyline(String json) {
    JSONObject obj = new JSONObject(json);
    return obj.getJSONObject("map").getString("polyline");
  }
}
