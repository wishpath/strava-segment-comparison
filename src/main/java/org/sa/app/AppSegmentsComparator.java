package org.sa.app;

import org.sa.service.CoordinateService;
import org.sa.service.ScoringService;
import org.sa.service.StravaService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AppSegmentsComparator {

  public static final String RESET = "\u001B[0m";
  public static final String RED = "\u001B[31m";

  public static void main(String[] args) throws IOException {
    List<List<Double>> neverTriedPoints = new ArrayList<>();
    List<String> neverTriedLabels = new ArrayList<>();

    new StravaService()
      .getStarredSegments()
      .stream()
      .filter(s -> CoordinateService.isCloseToHome(s))
      .filter(s -> s.activityType.equals("Run"))
      .filter(s -> s.averageGrade > 1)
      .sorted(Comparator.comparingInt(CoordinateService::getDistanceFromHomeInMeters))
      .sorted(Comparator.comparingInt(ScoringService::getPerformanceScore))
      .forEach(s -> {

        String myBestTimeSeconds = "-";
        if (s.athletePrEffort == null) {
          neverTriedPoints.add(s.startLatLng);
          neverTriedLabels.add(s.name);
        }
        else myBestTimeSeconds = "" + s.athletePrEffort.elapsedTime;

        System.out.println(s.name);
        System.out.println("     distance (m): " + (int)s.distance);
        System.out.println("     avg grade (%): " + s.averageGrade);
        System.out.println("     delta altitude (m): " + (int)(s.elevationHigh - s.elevationLow));
        System.out.println("     my best time (s): " + myBestTimeSeconds);
        System.out.println("     home proximity (m): " + CoordinateService.getDistanceFromHomeInMeters(s));
        System.out.println("     https://www.strava.com/segments/" + s.id);
        System.out.println("     score: " + RED + ScoringService.getPerformanceScore(s) + RESET);
        System.out.println();
      });

    placePointsInGoogleMaps(neverTriedPoints);
    generateCSVForMyMaps(neverTriedPoints, neverTriedLabels);
    exportToLeafletJS(neverTriedPoints, neverTriedLabels);
    exportToLeafletMapWithNiceLabels(neverTriedPoints, neverTriedLabels);
  }

  private static void placePointsInGoogleMaps(List<List<Double>> neverTriedPoints) {
    StringBuilder googleMaps = new StringBuilder("https://www.google.com/maps/dir/?api=1&travelmode=walking");

    if (!neverTriedPoints.isEmpty()) {
      List<Double> origin = neverTriedPoints.get(0);
      googleMaps.append("&origin=").append(getGoogleMapsPoint(origin));

      List<Double> destination = neverTriedPoints.get(neverTriedPoints.size() - 1);
      googleMaps.append("&destination=").append(getGoogleMapsPoint(destination));

      if (neverTriedPoints.size() > 2) {
        googleMaps.append("&waypoints=");
        for (int i = 1; i < neverTriedPoints.size() - 1; i++) {
          googleMaps.append(getGoogleMapsPoint(neverTriedPoints.get(i)));
          if (i < neverTriedPoints.size() - 2) googleMaps.append("|");
        }
      }
    }
    System.out.println("\n Never tried Segments on the map: " + googleMaps.toString());
  }

  private static String getGoogleMapsPoint(List<Double> latLng) {
    return latLng.get(0) + "," + latLng.get(1);
  }

  private static void generateCSVForMyMaps(List<List<Double>> points, List<String> labels) {
    try (PrintWriter writer = new PrintWriter(new File("points.csv"))) {
      writer.println("Name,Latitude,Longitude");
      for (int i = 0; i < points.size(); i++) {
        List<Double> latLng = points.get(i);
        String label = labels.get(i);
        writer.println(label + "," + latLng.get(0) + "," + latLng.get(1));
      }
      System.out.println("\nCSV generated: points.csv. How to import this into Google My Maps:\n" +
          "     • go to https://www.google.com/maps/d/\n" +
          "     • create a new map\n" +
          "     • import generated csv");
    } catch (IOException e) {
      e.printStackTrace();
    }
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

}
