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

    printLinkOfMapOfPoints(neverTriedPoints);
    generateCSVForMyMaps(neverTriedPoints, neverTriedLabels);
  }

  private static void printLinkOfMapOfPoints(List<List<Double>> neverTriedPoints) {
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
      System.out.println("\nCSV generated: points.csv. Import this into Google My Maps.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
