package org.sa.service;

import org.sa.config.Console;
import org.sa.console.Colors;
import org.sa.console.WebColorGradientCalculator;
import org.sa.dto.SegmentDTO;
import org.sa.facade.AllPeopleBestTimeSecondsFacade;
import org.sa.service.score.Score;

import java.util.List;

public class SegmentsProcessor {
  HexColorUtil hexColorUtil = new HexColorUtil(new WebColorGradientCalculator());

  public void setSegmentColors(List<SegmentDTO> segments) {
    int maxScore = getMaxScore(segments);
    int minScore = getMinScore(segments);

    int range = maxScore - minScore;
    if (range == 0) range = 1;
    double times = 100 / (double) range;

    for (SegmentDTO s : segments) {
      if (s.myScore == 0) {
        s.webColor = "dimgray";
        s.webColorDarker = "black";
      }
      else if (s.amKingOfMountain) {
        s.webColor = "blue";
        s.webColorDarker = "darkblue";
      }
      else {
        int colorValue = (int) ((double)(s.myScore - minScore) * times);
        s.webColor = hexColorUtil.hexColorFromRedThroughYellowToGreen(colorValue);
        s.webColorDarker = hexColorUtil.hexColorFromRedThroughYellowToGreenDarker(colorValue);
      }
    }
  }

  private int getMinScore(List<SegmentDTO> segments) {
    int minScore = 100;

    for (SegmentDTO s : segments)
      if (s.userPersonalRecordDTO != null)
        if (!s.amKingOfMountain)
          minScore = Math.min(minScore, s.myScore);

    return minScore;
  }

  private int getMaxScore(List<SegmentDTO> segments) {
    int maxScore = 0;

    for (SegmentDTO s : segments)
      if (s.userPersonalRecordDTO != null)
        if (!s.amKingOfMountain)
          maxScore = Math.max(maxScore, s.myScore);

    return maxScore;
  }

  public void setIsMyWorstScore(List<SegmentDTO> segments) {
    int minScore = getMinScore(segments);

    for (SegmentDTO s : segments) {
      if (s.userPersonalRecordDTO != null)
        if (!s.amKingOfMountain)
          if (s.myScore == minScore)
            s.isMyLowestScore = true;
    }
  }

  public void setIsMyBestScore(List<SegmentDTO> segments) {
    int maxScore = getMaxScore(segments);

    for (SegmentDTO s : segments) {
      if (s.userPersonalRecordDTO != null) //
        if (!s.amKingOfMountain)
          if (s.myScore == maxScore) {
            System.out.println(Console.TAB.repeat(3) + "my best score: " + s.name + " ; am king of mountain: " + s.amKingOfMountain);
            s.isMyStrongestSegmentAttempted = true;
          }

    }
  }

  public boolean amKingOfMountain(SegmentDTO s) {
    System.out.print(Console.TAB + Colors.MAGENTA + s.name + ": am king of mountain:");
    if (s.userPersonalRecordDTO == null) {
      System.out.println( "userPersonalRecordDTO is null" + Colors.RESET);
      return false;
    }
    System.out.println( s.userPersonalRecordDTO.isKingOfMountain + Colors.RESET);
    return s.userPersonalRecordDTO.isKingOfMountain;
  }

  /**
   * Calculates pace as minutes and seconds per kilometer, e.g. "4m:30s /km".
   */
  public String calculatePace(SegmentDTO s) {
    if (s.userPersonalRecordSeconds == null || s.nonFlatDistanceMeters <= 0) return "-1";
    int totalSeconds = (int) Math.round(s.userPersonalRecordSeconds / (s.nonFlatDistanceMeters / 1000.0));
    int min = totalSeconds / 60, sec = totalSeconds % 60;
    return min + "m:" + (sec < 10 ? "0" : "") + sec + "s /km";
  }

  /**
   * Formats best time as "Xm:YYs", e.g. "5m:00s" or "5m:07s".
   */
  public String calculateBestTime(SegmentDTO s) {
    if (s.userPersonalRecordSeconds == null || s.userPersonalRecordSeconds <= 0) return "-1";
    int secs = s.userPersonalRecordSeconds;
    if (secs < 60) return "0m:" + (secs < 10 ? "0" : "") + secs + "s";
    int min = secs / 60, sec = secs % 60;
    return min + "m:" + (sec < 10 ? "0" : "") + sec + "s";
  }

  public void setIsEasiestToGetKingOfMountain(List<SegmentDTO> segments) {
    int minAllPeopleScore = Integer.MAX_VALUE;

    for (SegmentDTO s : segments)
      if (!s.amKingOfMountain)
        minAllPeopleScore = Math.min(minAllPeopleScore, s.allPeopleBestScore);

    for (SegmentDTO s : segments)
      if (s.allPeopleBestScore == minAllPeopleScore) {
        if (s.amKingOfMountain) continue;
        s.isEasiestToGetKingOfMountain = true;
        System.out.println(Console.TAB + Colors.LIGHT_GRAY + "easiestKOM: " + s.name + Colors.RESET);
      }
  }

  public void setAllPeopleBestTimesAndScores_andFixAmKOM(List<SegmentDTO> segments, AllPeopleBestTimeSecondsFacade allPeopleBestTimeSecondsFacade) {
    for (SegmentDTO s : segments) {
      s.allPeopleBestTimeSeconds = allPeopleBestTimeSecondsFacade.getAllPeopleBestTimeSeconds(s);
      if (s.allPeopleBestTimeSeconds == s.userPersonalRecordSeconds && !s.amKingOfMountain) {
        System.out.println("FIXING 'am king of mountain:" + s.name);
        s.amKingOfMountain = true;
      }
      s.allPeopleBestScore = s.amKingOfMountain ? s.myScore : Score.getScore(s, s.allPeopleBestTimeSeconds);
    }
  }
}
