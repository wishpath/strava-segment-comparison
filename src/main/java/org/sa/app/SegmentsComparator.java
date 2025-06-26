package org.sa.app;

import org.sa.service.CoordinateService;
import org.sa.service.ScoringService;
import org.sa.service.StravaService;

import java.io.IOException;
import java.util.Comparator;

public class SegmentsComparator {

  public static final String RESET = "\u001B[0m";
  public static final String RED = "\u001B[31m";

  public static void main(String[] args) throws IOException {
    new StravaService()
      .getStarredSegments()
      .stream()
      .filter(s -> CoordinateService.isCloseToHome(s))
      .filter(s -> s.activityType.equals("Run"))
      .filter(s -> s.averageGrade > 1)
      .sorted(Comparator.comparingInt(CoordinateService::getDistanceFromHomeInMeters))
      .sorted(Comparator.comparingInt(ScoringService::getPerformanceScore))
      .forEach(s -> {
        String myBestTimeSeconds = s.athletePrEffort == null ? "-" : "" + s.athletePrEffort.elapsedTime;
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
  }
}
