package org.sa.service;

import org.sa.console.WebColorGradientCalculator;
import org.sa.dto.SegmentDTO;

import java.util.List;

public class SegmentsProcessor {
  HexColorUtil hexColorUtil = new HexColorUtil(new WebColorGradientCalculator());

  public int getPerformanceScore(SegmentDTO segment) {
    if (segment.userPersonalRecordDTO == null) return 0;

    int time = segment.userPersonalRecordDTO.elapsedTime;
    if (time == 0) return 0;

    double elevationGain = segment.elevationHighMeters - segment.elevationLowMeters;
    double flatDistance = Math.max(0, segment.distanceMeters - elevationGain);
    return (int) (100 * (elevationGain + 0.1 * flatDistance) / time);
  }

  public void setSegmentColors(List<SegmentDTO> segments) {
    int maxScore = getMaxScore(segments);
    int minScore = getMinScore(segments);

    int range = maxScore - minScore;
    if (range == 0) range = 1;
    double times = 100 / (double) range;

    for (SegmentDTO s : segments) {
      if (s.score == 0) s.webColor = "black";
      else if (s.isKing) s.webColor = "blue";
      else {
        int colorValue = (int) ((double)(s.score - minScore) * times);
        s.webColor = hexColorUtil.hexColorFromRedThroughYellowToGreen(colorValue);
      }
    }
  }

  private int getMinScore(List<SegmentDTO> segments) {
    int minScore = 100;

    for (SegmentDTO s : segments)
      if (s.userPersonalRecordDTO != null)
        if (!s.isKing)
          minScore = Math.min(minScore, s.score);

    return minScore;
  }

  private int getMaxScore(List<SegmentDTO> segments) {
    int maxScore = 0;

    for (SegmentDTO s : segments)
      if (s.userPersonalRecordDTO != null)
        if (!s.isKing)
          maxScore = Math.max(maxScore, s.score);

    return maxScore;
  }

  public void pickWorstSegments(List<SegmentDTO> segments) {
    int minScore = getMinScore(segments);

    for (SegmentDTO s : segments) {
      if (s.userPersonalRecordDTO != null)
        if (!s.isKing)
          if (s.score == minScore)
            s.isWeakest = true;
    }
  }

  public boolean isKing(SegmentDTO segment) {
    if (segment.userPersonalRecordDTO == null) return false;
    return segment.userPersonalRecordDTO.isKingOfMountain;
  }
}
