package org.sa.facade;

import org.sa.dto.SegmentDTO;
import org.sa.service.CoordinateService;
import org.sa.service.SegmentsProcessor;

import java.util.List;

public class PrintFacade {
  public static final String RESET = "\u001B[0m";
  public static final String RED = "\u001B[31m";

  public static void printSegments(List<SegmentDTO> segments, SegmentsProcessor segmentsProcessor) {
    segments.forEach(s -> {
      System.out.println(s.name);
      System.out.println("     distance (m): " + (int)s.nonFlatDistanceMeters);
      System.out.println("     avg grade (%): " + s.averageGradePercent);
      System.out.println("     delta altitude (m): " + s.deltaAltitude);
      System.out.println("     my best time (s): " + s.userPersonalRecordSeconds);
      System.out.println("     home proximity (m): " + CoordinateService.getDistanceFromHomeInMeters(s));
      System.out.println("     https://www.strava.com/segments/" + s.id);
      //System.out.println("     score: " + RED + segmentsProcessor.getPerformanceScore(s) + RESET + (s.isKing ? "\uD83D\uDC51\uD83D\uDC51\uD83D\uDC51" : ""));
      System.out.println("     score: " + RED + s.myScore + RESET + (s.amKingOfMountain ? "\uD83D\uDC51\uD83D\uDC51\uD83D\uDC51" : ""));
      System.out.print(s.amKingOfMountain ? "" : "     \uD83D\uDC51: " + s.allPeopleBestScore + "\n");
      System.out.println();
    });
  }
}
