package org.sa.helper;

import org.sa.config.Props;
import org.sa.console.Colors;
import org.sa.console.WebColorGradientCalculator;
import org.sa.dto.SegmentDTO;
import org.sa.helper.score.ScoreCalculatorHelper;
import org.sa.util.HexColorUtil;
import org.sa.util.TimeUtil;

import java.util.List;

public class SegmentsProcessorHelper {
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

  public String calculatePace(SegmentDTO s) {
    return TimeUtil.calculatePaceString(s.userPersonalRecordSeconds, s.nonFlatDistanceMeters);
  }

  public void formatAllPeoplePaceStrings(List<SegmentDTO> segments) {
    for (SegmentDTO s : segments) {
      s.allPeoplePaceString = TimeUtil.calculatePaceString(s.allPeopleBestTimeSeconds, s.nonFlatDistanceMeters);
    }
  }

  public String formatBestTimeString(SegmentDTO s) {
    if (s.userPersonalRecordSeconds == null || s.userPersonalRecordSeconds <= 0) return "-1";
    return TimeUtil.formatMinutesAndSecondsNoLetters(s.userPersonalRecordSeconds);
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
      if (s.allPeopleBestTimeSeconds.equals(s.userPersonalRecordSeconds) && !s.amKingOfMountain) {
        System.out.println(Props.TAB.repeat(2) + Colors.BLUE + "FIXING, I'm The KingOfMountain: " + s.name + Colors.RESET);
        s.amKingOfMountain = true;
      }
    }
  }
  public void setAllPeopleBestScores(List<SegmentDTO> segments) {
    for (SegmentDTO s : segments) {
      s.allPeopleBestScore = s.amKingOfMountain ? s.myScore : ScoreCalculatorHelper.getScore(s, s.allPeopleBestTimeSeconds);
    }
  }

  public void formatAllPeopleBestTimeStrings(List<SegmentDTO> segments) {
    for (SegmentDTO s : segments) {
      s.allPeopleBestTimeString = TimeUtil.formatMinutesAndSecondsNoLetters(s.allPeopleBestTimeSeconds);
    }
  }
}
