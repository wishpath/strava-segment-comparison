package org.sa.service;

import org.sa.dto.SegmentDTO;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
      writer.println("var map = L.map('map').setView([" + segments.get(0).startLatitudeLongitude.get(0) + "," + segments.get(0).startLatitudeLongitude.get(1) + "], 13);");
      writer.println("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);");

      for (SegmentDTO segment : segments) writeSegment(segment, writer);

      writer.println("</script></body></html>");
      System.out.println("\nMap file generated: map_with_polylines.html. Open it in your browser.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void writeSegment(SegmentDTO s, PrintWriter writer) {

    List<Double> p = s.startLatitudeLongitude;

    String label = buildSegmentLabel(s);


    if (s.isKing)
      writer.println("L.marker([" + p.get(0) + "," + p.get(1) + "], {icon: L.divIcon({className: 'crown-icon', html: '" + CROWN_EMOJI + "', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else if (s.isWeakest)
      writer.println("L.marker([" + p.get(0) + "," + p.get(1) + "], {icon: L.divIcon({className: 'crown-icon', html: '<div style=\"font-size: 20px;\">" + SKULL_EMOJI + "</div>', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else if (s.isStrongest)
      writer.println("L.marker([" + p.get(0) + "," + p.get(1) + "], {icon: L.divIcon({className: 'crown-icon', html: '<div style=\"font-size: 11px;\">" + STRONG_EMOJI + "</div>', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else
      writer.println("L.circleMarker([" + p.get(0) + "," + p.get(1) + "], {color: '" + s.webColor + "', fillColor: '" + s.webColor + "', opacity: 0.65, fillOpacity: 0.65, radius: 4.5}).addTo(map).bindPopup(\"" + label + "\");");

    if (s.polyline != null && !s.polyline.isEmpty()) {
      String escapedPolyline = s.polyline.replace("\\", "\\\\");
      writer.println("var decoded = polyline.decode(\"" + escapedPolyline + "\");");
      writer.println("var latlngs = decoded.map(function(pair) { return [pair[0], pair[1]]; });");
      writer.println("L.polyline(latlngs, {color: '" + s.webColor + "', weight: 4, opacity: 0.65}).addTo(map);");
    }
  }

//  private static String buildSegmentLabel(SegmentDTO s) {
//    return  "<a href=\\\"" + s.link + "\\\" target=\\\"_blank\\\">" + s.name.replace("\"", "\\\\\"") + "</a>";
//  }

//  private static String buildSegmentLabel(SegmentDTO s) {
//    String score = "<span style='color:red'>" + s.score + "</span>";
//    return "<a href=\\\"" + s.link + "\\\" target=\\\"_blank\\\">" + s.name.replace("\"", "\\\\\"") + "</a>" +
//        "<br/>Score: " + score;
//  }

  private static String buildSegmentLabel(SegmentDTO s) {
    String score = "<span style='color:red'>" + s.score + "</span>";
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


//  private static String buildSegmentLabel(SegmentDTO s) {
//    String score = "<span style='color:red'>" + s.score + "</span>";
//    String pace = (s.pace != null && !s.pace.isEmpty()) ? s.pace : "N/A";
//    String bestTime = (s.bestTimeString != null && !s.bestTimeString.isEmpty()) ? s.bestTimeString : "N/A";
//    String distanceM = String.format("%.0f m", s.distanceMeters);
//    String grade = String.format("%.1f%%", s.averageGradePercent);
//    String deltaAlt = String.format("▲ %.0f m", s.deltaAltitude);
//
//    return "<a href=\"" + s.link + "\" target=\"_blank\">" + s.name.replace("\"", "\\\"") + "</a>" +
//        "<br/>Score: " + score +
//        "<br/>Pace: " + pace +
//        "<br/>Best time: " + bestTime +
//        "<br/>Length: " + distanceM +
//        "<br/>Gradient: " + grade +
//        "<br/>Delta altitude: " + deltaAlt;
//  }

//  private static String escapeHtml(String s) {
//    if (s == null) return "";
//    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
//  }
//
//  private static String buildSegmentLabel(SegmentDTO s) {
//    String score = "<span style='color:red'>" + s.score + "</span>";
//    String pace = (s.pace != null && !s.pace.isEmpty()) ? s.pace : "N/A";
//    String bestTime = (s.bestTimeString != null && !s.bestTimeString.isEmpty()) ? s.bestTimeString : "N/A";
//    String distanceM = String.format("%.0f m", s.distanceMeters);
//    String grade = String.format("%.1f%%", s.averageGradePercent);
//    String deltaAlt = String.format("▲ %.0f m", s.deltaAltitude);
//
//    return "<a href=\"" + s.link + "\" target=\"_blank\">" + escapeHtml(s.name) + "</a>" +
//        "<br/>Score: " + score +
//        "<br/>Pace: " + pace +
//        "<br/>Best time: " + bestTime +
//        "<br/>Length: " + distanceM +
//        "<br/>Gradient: " + grade +
//        "<br/>Delta altitude: " + deltaAlt;
//  }

}
