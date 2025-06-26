package org.sa.service;

import org.sa.dto.Segment;

public class ScoringService {
  public static int getPerformanceScore(Segment segment) {
    if (segment.athletePrEffort == null) return 0;

    int time = segment.athletePrEffort.elapsedTime;
    if (time == 0) return 0;

    double elevationGain = segment.elevationHigh - segment.elevationLow;
    double flatDistance = Math.max(0, segment.distance - elevationGain);
    return (int) (100 * (elevationGain + 0.1 * flatDistance) / time);
  }
}
