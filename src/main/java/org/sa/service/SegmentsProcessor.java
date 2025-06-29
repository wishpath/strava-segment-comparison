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
    int maxScore = 0;
    int minScore = 100;

    for (SegmentDTO s : segments) {
      if (s.score == 0) continue;
      maxScore = Math.max(maxScore, s.score);
      minScore = Math.min(minScore, s.score);
    }

    int range = maxScore - minScore;
    if (range == 0) range = 1;
    double times = 100 / (double) range;

    for (SegmentDTO s : segments) {
      if (s.score == 0) continue;
      int colorValue = (int) ((double)(s.score - minScore) * times);
      s.colorHex = hexColorUtil.hexColorFromRedThroughYellowToGreen(colorValue);
    }
  }
}
