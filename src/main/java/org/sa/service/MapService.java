package org.sa.service;

import org.sa.dto.SegmentDTO;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class MapService {

  private static final String CROWN_EMOJI = "&#x1F451;"; //👑
  private static final String SKULL_EMOJI = "\uD83D\uDC80"; //💀
  private static final String STRONG_EMOJI = "&#x1F4AA;"; //💪
  private static final String TARGET_EMOJI = "&#x1F3AF;"; //🎯
  private static final String HUNDRED_EMOJI = "&#x1F4AF;"; // 💯


  public static void openMap(String filename) {
    try {
      Desktop.getDesktop().browse(new File(filename).getAbsoluteFile().toURI());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportSegmentsWithPolylinesToLeafletJS(java.util.List<SegmentDTO> segments) {
    try (PrintWriter writer = new PrintWriter(new File("src/main/java/org/sa/storage/map_with_polylines.html"))) {
      writer.println("<!DOCTYPE html><html><head><meta charset='utf-8'>");
      writer.println("<link rel='icon' type='image/png' href='crown.png'>"); // favicon
      writer.println("<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css' />");
      writer.println("<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>");
      writer.println("<script src='https://unpkg.com/@mapbox/polyline'></script>");
      writer.println("</head><body>");
      writer.println("<div id='map' style='height: 100vh; width: 100vw;'></div>");
      writer.println("<script>");
      writer.println("var map = L.map('map').setView([" + segments.get(0).startCoordinatePair + "], 13);");
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
    if (s.amKingOfMountain)
      writer.println("L.marker([" + s.startCoordinatePair + "], {icon: L.divIcon({className: 'crown-icon', html: '" + CROWN_EMOJI + "', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else if (s.isMyLowestScore)
      writer.println("L.marker([" + s.startCoordinatePair + "], {icon: L.divIcon({className: 'scull-icon', html: '<div style=\"font-size: 12px;\">" + SKULL_EMOJI + "</div>', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else if (s.isMyStrongestSegmentAttempted)
      writer.println("L.marker([" + s.startCoordinatePair + "], {icon: L.divIcon({className: 'strong-icon', html: '<div style=\"font-size: 11px;\">" + STRONG_EMOJI + "</div>', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else if (s.isEasiestToGetKingOfMountain)
      writer.println("L.marker([" + s.startCoordinatePair + "], {icon: L.divIcon({className: 'strong-icon', html: '<div style=\"font-size: 11px;\">" + TARGET_EMOJI + "</div>', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else if (s.amLocalLegend)
      writer.println("L.marker([" + s.startCoordinatePair + "], {icon: L.divIcon({className: 'strong-icon', html: '<div style=\"font-size: 11px;\">" + HUNDRED_EMOJI + "</div>', iconSize: [16, 16], iconAnchor: [8, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
    else {
      writer.println("L.marker([" + s.startCoordinatePair + "], {icon: L.divIcon({className: 'white-icon', html: '<div style=\"color: " + "black" + "; font-size: 20px; line-height: 18px;\">&#9679;</div>', iconSize: [14, 14], iconAnchor: [7, 8]})}).addTo(map).bindPopup(\"" + label + "\");");
      writer.println("L.marker([" + s.startCoordinatePair + "], {icon: L.divIcon({className: 'color-dot', html: '<div style=\"color: " + s.webColor + "; font-size: 14px; line-height: 18px;\">&#9679;</div>', iconSize: [14, 14], iconAnchor: [5, 7]})}).addTo(map).bindPopup(\"" + label + "\");");
    }
  }

  private static String buildLabel(SegmentDTO s) {


    //unchangeable parameters of a segment
    String distanceM = String.format("%.0f m", s.nonFlatDistanceMeters);
    String deltaAlt = String.format("▲ %.0f m", s.deltaAltitude);
    String grade = String.format("%.1f%%", s.averageGradePercent);

    //people results
    String scoreMineAndOverall = "<span style='color:red'>" + s.myScore + "</span> / " + s.allPeopleBestScore;
    String myPace = (s.myPaceString != null && !s.myPaceString.isEmpty()) ? s.myPaceString : "N/A";
    String overallPace = (s.allPeoplePaceString != null && !s.allPeoplePaceString.isEmpty()) ? s.allPeoplePaceString : "N/A";
    String paceMineAndOverall = "<span style='color:red'>" + myPace + "</span> / " + overallPace;

    String myBestTimeString = (s.myBestTimeString != null && !s.myBestTimeString.isEmpty()) ? s.myBestTimeString : "N/A";
    String overallBestTimeString = (s.allPeopleBestTimeString != null && !s.allPeopleBestTimeString.isEmpty()) ? s.allPeopleBestTimeString : "N/A";
    String bestTimeMineAndOverall = "<span style='color:red'>" + myBestTimeString + "</span> / " + overallBestTimeString;
    String recentTriesMineAndOverall = "<span style='color:red'>" + s.myRecentAttemptCount + "</span> / " + s.localLegendRecentAttemptCount;

    return

        //unchangeable parameters of a segment
        "<a href=\\\"" + s.link + "\\\" target=\\\"_blank\\\">" + s.name.replace("\"", "\\\\\"") + "</a>" +
        "<br/>" +
        "<br/>Length: " + distanceM +
        "<br/>Delta altitude: " + deltaAlt +
        "<br/>Gradient: " + grade +
        "<br/>" +

        //people results
        "<br/>" + "<span style='color:red'>Mine</span>" + " / overall best:" +
        "<br/>&nbsp;&nbsp;Score: " + scoreMineAndOverall +
        "<br/>&nbsp;&nbsp;Pace, m:ss/km: " + paceMineAndOverall +
        "<br/>&nbsp;&nbsp;Time, m:ss: " + bestTimeMineAndOverall +
        (s.amKingOfMountain ?
            "" :
            "<br/>&nbsp;&nbsp;Tries: " + recentTriesMineAndOverall) +
        "<br/>" +

        //developer stuff
        "<br/>Id: " + s.id;


  }
}
