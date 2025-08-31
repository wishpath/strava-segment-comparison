package org.sa.service;

import org.sa.config.Props;
import org.sa.console.Colors;
import org.sa.console.WebColorGradientCalculator;
import org.sa.dto.SegmentDTO;
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

    for (SegmentDTO s : segments)
      if (s.userPersonalRecordDTO != null) //
        if (!s.amKingOfMountain)
          if (s.myScore == maxScore)
            s.isMyStrongestSegmentAttempted = true;
  }

  public boolean amKingOfMountain(SegmentDTO s) {
    if (s.userPersonalRecordDTO == null) return false;
    return s.userPersonalRecordDTO.isKingOfMountain;
  }

  /**
   * Calculates pace as minutes and seconds per kilometer, e.g. "4m:30s /km".
   */
  public String calculatePace(SegmentDTO s) {
    return calculatePaceFromSeconds(s.userPersonalRecordSeconds, s.nonFlatDistanceMeters);
  }

  public void formatAllPeoplePaceStrings(List<SegmentDTO> segments) {
    for (SegmentDTO s : segments) {
      s.allPeoplePaceString = calculatePaceFromSeconds(s.allPeopleBestTimeSeconds, s.nonFlatDistanceMeters);
    }
  }

  private String calculatePaceFromSeconds(Integer totalTimeSeconds, double distanceMeters) {
    if (totalTimeSeconds == null || distanceMeters <= 0) return "-1";
    int totalSeconds = (int) Math.round(totalTimeSeconds / (distanceMeters / 1000.0));
    int min = totalSeconds / 60, sec = totalSeconds % 60;
    return min + ":" + (sec < 10 ? "0" : "") + sec;
  }

  /**
   * Formats best time as "Xm:YYs", e.g. "5m:00s" or "5m:07s".
   */
  public String formatBestTimeString(SegmentDTO s) {
    if (s.userPersonalRecordSeconds == null || s.userPersonalRecordSeconds <= 0) return "-1";
    return formatMinutesAndSecondsNoLetters(s.userPersonalRecordSeconds);
  }

  public void setIsEasiestToGetKingOfMountain(List<SegmentDTO> segments) {
    int minAllPeopleScore = Integer.MAX_VALUE;

    for (SegmentDTO s : segments)
      if (!s.amKingOfMountain)
        minAllPeopleScore = Math.min(minAllPeopleScore, s.allPeopleBestScore);

    for (SegmentDTO s : segments)
      if (s.allPeopleBestScore == minAllPeopleScore)
        if (!s.amKingOfMountain)
          s.isEasiestToGetKingOfMountain = true;
  }

  public void fixAmKOM(List<SegmentDTO> segments) {
    for (SegmentDTO s : segments) {
      if (s.allPeopleBestTimeSeconds == s.userPersonalRecordSeconds && !s.amKingOfMountain) {
        System.out.println(Props.TAB.repeat(2) + Colors.BLUE + "FIXING, I'm The KingOfMountain: " + s.name + Colors.RESET);
        s.amKingOfMountain = true;
      }
    }
  }
  public void setAllPeopleBestScores(List<SegmentDTO> segments) {
    for (SegmentDTO s : segments) {
      s.allPeopleBestScore = s.amKingOfMountain ? s.myScore : Score.getScore(s, s.allPeopleBestTimeSeconds);
    }
  }

  public void formatAllPeopleBestTimeStrings(List<SegmentDTO> segments) {
    for (SegmentDTO s : segments) {
      s.allPeopleBestTimeString = formatMinutesAndSecondsNoLetters(s.allPeopleBestTimeSeconds);
    }
  }

  private String formatMinutesAndSecondsNoLetters(int seconds) {
    if (seconds <= 0) return "-1";
    if (seconds < 60) return "0:" + (seconds < 10 ? "0" : "") + seconds;
    int minutes = seconds / 60, remainingSeconds = seconds % 60;
    return minutes + ":" + (remainingSeconds < 10 ? "0" : "") + remainingSeconds;
  }
}
