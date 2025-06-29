package org.sa.service;

import org.sa.dto.SegmentDTO;

public class ScoringService {
  public static int getPerformanceScore(SegmentDTO segment) {
    if (segment.userPersonalRecordDTO == null) return 0;

    int time = segment.userPersonalRecordDTO.elapsedTime;
    if (time == 0) return 0;

    double elevationGain = segment.elevationHighMeters - segment.elevationLowMeters;
    double flatDistance = Math.max(0, segment.distanceMeters - elevationGain);
    return (int) (100 * (elevationGain + 0.1 * flatDistance) / time);
  }
}
