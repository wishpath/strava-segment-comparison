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

public class AppSegmentsComparator {

  public static final String RESET = "\u001B[0m";
  public static final String RED = "\u001B[31m";
  private static final StravaService stravaService = new StravaService();

  public static void main(String[] args) throws IOException {
    List<List<Double>> neverTriedPoints = new ArrayList<>();
    List<String> neverTriedLabels = new ArrayList<>();
    List<SegmentDTO> neverTriedSegments = new ArrayList<>();

    stravaService
      .getStarredSegments()
      .stream()
      .filter(s -> CoordinateService.isCloseToHome(s))
      .filter(s -> s.activityType.equals("Run"))
      .filter(s -> s.averageGradePercent > 1)
      .sorted(Comparator.comparingInt(CoordinateService::getDistanceFromHomeInMeters))
      .sorted(Comparator.comparingInt(ScoringService::getPerformanceScore))
      .forEach(s -> {

        String myBestTimeSeconds = "-";
        if (s.userPersonalRecordDTO == null) {
          neverTriedPoints.add(s.startLatitudeLongitude);
          neverTriedLabels.add(s.name);
          neverTriedSegments.add(s);
        }
        else myBestTimeSeconds = "" + s.userPersonalRecordDTO.elapsedTime;

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

    exportToLeafletJS(neverTriedPoints, neverTriedLabels);
    exportToLeafletMapWithNiceLabels(neverTriedPoints, neverTriedLabels);
    exportPolylineToLeafletJS(stravaService.getSegmentPolyline(neverTriedSegments.get(0).id));

    try {
      Desktop.getDesktop().browse(new File("map.html").getAbsoluteFile().toURI());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String getGoogleMapsPoint(List<Double> latLng) {
    return latLng.get(0) + "," + latLng.get(1);
  }

  private static void exportToLeafletJS(List<List<Double>> points, List<String> labels) {
    try (PrintWriter writer = new PrintWriter(new File("map.html"))) {
      writer.println("<!DOCTYPE html><html><head><meta charset='utf-8'>");
      writer.println("<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css' />");
      writer.println("<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script></head><body>");
      writer.println("<div id='map' style='height: 100vh'></div>");
      writer.println("<script>var map = L.map('map').setView([" + getGoogleMapsPoint(points.get(0)) + "], 13);");
      writer.println("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);");

      for (int i = 0; i < points.size(); i++) {
        List<Double> point = points.get(i);
        String label = labels.get(i).replace("\"", "\\\"");
        writer.println("L.marker([" + point.get(0) + "," + point.get(1) + "]).addTo(map).bindPopup(\"" + label + "\");");
      }

      writer.println("</script></body></html>");
      System.out.println("\nMap file generated: map.html. Open it in your browser.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void exportToLeafletMapWithNiceLabels(List<List<Double>> points, List<String> labels) {
    try (PrintWriter writer = new PrintWriter(new File("map_nice_labels.html"))) {
      writer.println("<!DOCTYPE html><html><head><meta charset='utf-8'>");
      writer.println("<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css' />");
      writer.println("<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>");
      writer.println("<style>");
      writer.println(".leaflet-label {");
      writer.println("  background: rgba(255,255,255,0.8);");
      writer.println("  padding: 2px 6px;");
      writer.println("  border-radius: 4px;");
      writer.println("  font-size: 13px;");
      writer.println("  border: 1px solid #666;");
      writer.println("  white-space: nowrap;");
      writer.println("  transform: translate(6px, -24px);");
      writer.println("  position: absolute;");
      writer.println("  z-index: 1000;");
      writer.println("}");
      writer.println("</style></head><body>");
      writer.println("<div id='map' style='height: 100vh; width: 100vw;'></div>");
      writer.println("<script>");
      writer.println("var map = L.map('map').setView([" + getGoogleMapsPoint(points.get(0)) + "], 13);");
      writer.println("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);");

      writer.println("function createLabel(text) {");
      writer.println("  var div = document.createElement('div');");
      writer.println("  div.className = 'leaflet-label';");
      writer.println("  div.innerHTML = text;");
      writer.println("  return div;");
      writer.println("}");

      for (int i = 0; i < points.size(); i++) {
        List<Double> point = points.get(i);
        String label = labels.get(i).replace("\"", "\\\"");

        writer.println("var marker = L.marker([" + point.get(0) + "," + point.get(1) + "]).addTo(map);");
        writer.println("var label = createLabel(\"" + label + "\");");
        writer.println("map.getPanes().overlayPane.appendChild(label);");
        writer.println("var pos = map.latLngToLayerPoint(marker.getLatLng());");
        writer.println("L.DomUtil.setPosition(label, pos);");
        writer.println("marker.on('move', function() {");
        writer.println("  var p = map.latLngToLayerPoint(marker.getLatLng());");
        writer.println("  L.DomUtil.setPosition(label, p);");
        writer.println("});");
      }

      writer.println("map.on('zoomend moveend', function() {");
      writer.println("  document.querySelectorAll('.leaflet-label').forEach(function(label, i) {");
      writer.println("    var marker = map._layers[Object.keys(map._layers)[i + 1]];");
      writer.println("    var p = map.latLngToLayerPoint(marker.getLatLng());");
      writer.println("    L.DomUtil.setPosition(label, p);");
      writer.println("  });");
      writer.println("});");

      writer.println("</script></body></html>");
      System.out.println("Map with nice labels saved as: map_nice_labels.html");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void exportPolylineToLeafletJS(String googlePolylineFormat) {
    try (PrintWriter writer = new PrintWriter(new File("map_polyline.html"))) {
      writer.println("<!DOCTYPE html><html><head><meta charset='utf-8'>");
      writer.println("<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css' />");
      writer.println("<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>");
      writer.println("<script src='https://unpkg.com/@mapbox/polyline'></script>"); // for decoding polyline
      writer.println("</head><body>");
      writer.println("<div id='map' style='height: 100vh; width: 100vw;'></div>");
      writer.println("<script>");
      writer.println("var map = L.map('map').setView([0, 0], 13);");
      writer.println("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);");

      writer.println("var decoded = polyline.decode(\"" + googlePolylineFormat + "\");");
      writer.println("var latlngs = decoded.map(function(pair) { return [pair[0], pair[1]]; });");
      writer.println("L.polyline(latlngs, {color: 'blue', weight: 4}).addTo(map);");
      writer.println("map.fitBounds(latlngs);");

      writer.println("</script></body></html>");
      System.out.println("\nMap file generated: map_polyline.html. Open it in your browser.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
