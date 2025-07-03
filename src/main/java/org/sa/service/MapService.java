package org.sa.service;

import org.sa.dto.SegmentDTO;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class MapService {
  private static final String DARKER_GRAY = "#505050";
  private static final String CROWN_EMOJI = "&#x1F451;"; //👑
  private static final String SKULL_EMOJI = "&#x2620;"; //☠️
  private static final String STRONG_EMOJI = "&#x1F4AA;"; //💪

  public static void openMap(String filename) {
    try {
      Desktop.getDesktop().browse(new File(filename).getAbsoluteFile().toURI());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportSegmentsWithPolylinesToLeafletJS(java.util.List<SegmentDTO> segments) {
    try (PrintWriter writer = new PrintWriter(new File("map_with_polylines.html"))) {
      writer.println("<!DOCTYPE html><html><head><meta charset='utf-8'>");
      writer.println("<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css' />");
      writer.println("<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>");
      writer.println("<script src='https://unpkg.com/@mapbox/polyline'></script>");
      writer.println("</head><body>");
      writer.println("<div id='map' style='height: 100vh; width: 100vw;'></div>");
      writer.println("<script>");
      writer.println("var map = L.map('map').setView([" + segments.get(0).coordinate + "], 13);");
      writer.println("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);");

      for (SegmentDTO s : segments) {
        drawPin(s, writer, buildLabel(s));
        drawPolyline(s, writer);
      }

      writer.println("</script></body></html>");
      System.out.println("\nMap file generated: map_with_polylines.html. Open it in your browser.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void drawPolyline(SegmentDTO s, PrintWriter writer) {
    if (s.polyline != null && !s.polyline.isEmpty()) {
      String escapedPolyline = s.polyline.replace("\\", "\\\\");
      String link = s.link.replace("\"", "\\\"");

      writer.println("var decoded = polyline.decode(\"" + escapedPolyline + "\");");
      writer.println("var latlngs = decoded.map(function(pair) { return [pair[0], pair[1]]; });");

      // White edge polyline (bottom layer)
      writer.println("var edgeLine = L.polyline(latlngs, {color: 'black', weight: 4.5, opacity: 0.85}).addTo(map);");

      // Colored polyline (top layer)
      writer.println("var colorLine = L.polyline(latlngs, {color: '" + s.webColor + "', weight: 3, opacity: 1}).addTo(map);");

      // Hover effect: darker color & higher opacity
      writer.println("colorLine.on('mouseover', function() { this.setStyle({color: '" + s.webColorDarker + "', opacity: 1}); });");
      writer.println("colorLine.on('mouseout', function() { this.setStyle({color: '" + s.webColor + "', opacity: 1}); });");

      // Click event to navigate to link
      writer.println("colorLine.on('click', function() { window.open(\"" + link + "\", '_blank'); });");
    }
  }

  private static void drawPin(SegmentDTO s, PrintWriter writer, String label) {
    if (s.isKing)
      writer.println("L.marker([" + s.coordinate + "], {icon: L.divIcon({className: 'crown-icon', html: '" + CROWN_EMOJI + "', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else if (s.isWeakest)
      writer.println("L.marker([" + s.coordinate + "], {icon: L.divIcon({className: 'scull-icon', html: '<div style=\"font-size: 20px;\">" + SKULL_EMOJI + "</div>', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else if (s.isStrongest)
      writer.println("L.marker([" + s.coordinate + "], {icon: L.divIcon({className: 'strong-icon', html: '<div style=\"font-size: 11px;\">" + STRONG_EMOJI + "</div>', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else {
      writer.println("L.marker([" + s.coordinate + "], {icon: L.divIcon({className: 'white-icon', html: '<div style=\"color: " + "black" + "; font-size: 20px; line-height: 18px;\">&#9679;</div>', iconSize: [14, 14], iconAnchor: [7, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
      writer.println("L.marker([" + s.coordinate + "], {icon: L.divIcon({className: 'color-dot', html: '<div style=\"color: " + s.webColor + "; font-size: 14px; line-height: 18px;\">&#9679;</div>', iconSize: [14, 14], iconAnchor: [5, 7]})}).addTo(map).bindPopup(\"" + label + "\");");
    }
  }

  private static String buildLabel(SegmentDTO s) {
    String score = "<span style='color:red'>" + s.score + "</span> / " + s.allPeopleBestScore;
    String pace = (s.paceString != null && !s.paceString.isEmpty()) ? s.paceString : "N/A";
    String bestTime = (s.bestTimeString != null && !s.bestTimeString.isEmpty()) ? s.bestTimeString : "N/A";
    String distanceM = String.format("%.0f m", s.distanceMeters);
    String grade = String.format("%.1f%%", s.averageGradePercent);
    String deltaAlt = String.format("▲ %.0f m", s.deltaAltitude);

    return "<a href=\\\"" + s.link + "\\\" target=\\\"_blank\\\">" + s.name.replace("\"", "\\\\\"") + "</a>" +
        "<br/>Score: " + score +
        "<br/>Pace: " + pace +
        "<br/>Best time: " + bestTime +
        "<br/>Length: " + distanceM +
        "<br/>Gradient: " + grade +
        "<br/>Delta altitude: " + deltaAlt;
  }
}
