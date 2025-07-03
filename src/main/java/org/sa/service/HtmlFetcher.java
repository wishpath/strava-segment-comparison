package org.sa.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class HtmlFetcher {
  public static String fetchSegmentFastestTimeString(Long segmentId) throws Exception {
    URL url = new URL("https://www.strava.com/segments/" + segmentId);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("User-Agent", "Mozilla/5.0");

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
      String dom =  reader.lines().collect(Collectors.joining("\n"));
      return dom.split("<td>1</td>")[1].split("</tr>")[0].split("\">")[1].split("<")[0];
    }
  }

  public static int fetchSegmentFastestTimeSeconds(Long segmentId) throws Exception {
    String[] time = fetchSegmentFastestTimeString(segmentId).split(":");
    return Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
  }
  public static void main(String[] args) throws Exception {
    System.out.println(fetchSegmentFastestTimeSeconds(39505949L));
  }
}
