package org.sa.app;

import org.sa.dto.SegmentDTO;
import org.sa.service.CoordinateService;
import org.sa.service.ScoringService;
import org.sa.service.StravaService;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class App {

  public static final String RESET = "\u001B[0m";
  public static final String RED = "\u001B[31m";
  private static final StravaService stravaService = new StravaService();

  public static void main(String[] args) throws IOException {
    List<SegmentDTO> segments = new ArrayList<>();

    stravaService
      .getStarredSegments()
      .stream()
      .filter(s -> CoordinateService.isCloseToHome(s))
      .filter(s -> s.activityType.equals("Run"))
      .filter(s -> s.averageGradePercent > 1)
      .sorted(Comparator.comparingInt(CoordinateService::getDistanceFromHomeInMeters))
      .sorted(Comparator.comparingInt(ScoringService::getPerformanceScore))
      .forEach(s -> {

        String myBestTimeSeconds = s.userPersonalRecordDTO == null ? "-" : "" + s.userPersonalRecordDTO.elapsedTime;
        s.polyline = stravaService.getSegmentPolyline(s.id);
        segments.add(s);

        System.out.println(s.name);
        System.out.println("     distance (m): " + (int)s.distanceMeters);
        System.out.println("     avg grade (%): " + s.averageGradePercent);
        System.out.println("     delta altitude (m): " + (int)(s.elevationHighMeters - s.elevationLowMeters));
        System.out.println("     my best time (s): " + myBestTimeSeconds);
        System.out.println("     home proximity (m): " + CoordinateService.getDistanceFromHomeInMeters(s));
        System.out.println("     https://www.strava.com/segments/" + s.id);
        System.out.println("     score: " + RED + ScoringService.getPerformanceScore(s) + RESET);
        System.out.println();
      });

    exportSegmentsWithPolylinesToLeafletJS(segments);

    try {
      Desktop.getDesktop().browse(new File("map_with_polylines.html").getAbsoluteFile().toURI());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String getGoogleMapsPoint(List<Double> latLng) {
    return latLng.get(0) + "," + latLng.get(1);
  }

  private static void exportSegmentsWithPolylinesToLeafletJS(List<SegmentDTO> segments) {
    try (PrintWriter writer = new PrintWriter(new File("map_with_polylines.html"))) {
      writer.println("<!DOCTYPE html><html><head><meta charset='utf-8'>");
      writer.println("<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css' />");
      writer.println("<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>");
      writer.println("<script src='https://unpkg.com/@mapbox/polyline'></script>");
      writer.println("</head><body>");
      writer.println("<div id='map' style='height: 100vh; width: 100vw;'></div>");
      writer.println("<script>");
      writer.println("var map = L.map('map').setView([" + segments.get(0).startLatitudeLongitude.get(0) + "," + segments.get(0).startLatitudeLongitude.get(1) + "], 13);");
      writer.println("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);");

      for (SegmentDTO segment : segments) {
        List<Double> p = segment.startLatitudeLongitude;
        String label = segment.name.replace("\"", "\\\"");
        writer.println("L.marker([" + p.get(0) + "," + p.get(1) + "]).addTo(map).bindPopup(\"" + label + "\");");
        if (segment.polyline != null && !segment.polyline.isEmpty()) {
          writer.println("var decoded = polyline.decode(\"" + segment.polyline + "\");");
          writer.println("var latlngs = decoded.map(function(pair) { return [pair[0], pair[1]]; });");
          writer.println("L.polyline(latlngs, {color: 'blue', weight: 4}).addTo(map);");
        }
      }

      writer.println("</script></body></html>");
      System.out.println("\nMap file generated: map_with_polylines.html. Open it in your browser.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
