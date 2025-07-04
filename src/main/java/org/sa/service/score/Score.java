package org.sa.service.score;

import org.sa.dto.SegmentDTO;

public class Score {

  // one would get score 100, if ran flat 300m at 3min/km pace.
  private static final double EXCELLENCE_PACE_MIN_PER_KM_FOR_300_METER_FLAT_SEGMENT = 2.99;
  private static final int EXCELLENT_SCORE = 100;
  private static final double SCORE_FACTOR = EXCELLENT_SCORE * EXCELLENCE_PACE_MIN_PER_KM_FOR_300_METER_FLAT_SEGMENT;


//  public static int getPerformanceScore(SegmentDTO s) {
//    if (s.userPersonalRecordDTO == null) return 0;
//    return getPerformanceScore(s, s.userPersonalRecordDTO.elapsedTime);
//  }
//
//  public static int getPerformanceScore(SegmentDTO s, int time) { // non s time in case of another athlete
//    if (time == 0) return 0;
//    if (s.deltaAltitude > s.nonFlatDistanceMeters) throw new IllegalArgumentException("IMPOSSIBLE DELTA ALTITUDE");
//    double flatDistance = Math.sqrt(s.nonFlatDistanceMeters * s.nonFlatDistanceMeters - s.deltaAltitude * s.deltaAltitude);
//    return (int) (100 * (s.deltaAltitude + 0.1 * flatDistance) / time);
//  }

  public static int getScore(SegmentDTO s) {
    if (s.userPersonalRecordSeconds == null) return 0;
    return getScore(s, s.userPersonalRecordSeconds);
  }

  public static int getScore(SegmentDTO s, int timeSeconds) {
    if (s.userPersonalRecordSeconds == null) return 0;
    int normalizedFlatDistanceMeters = GradeAdjustmentModel.calculateGradeAdjustedFlatLength((int) s.nonFlatDistanceMeters, s.averageGradePercent);
    double normalizedPaceMinPerKm = DistancePaceNormalizer.normalizePaceFor300Meters(timeSeconds, normalizedFlatDistanceMeters);
    return (int) (SCORE_FACTOR / normalizedPaceMinPerKm);
  }
}
