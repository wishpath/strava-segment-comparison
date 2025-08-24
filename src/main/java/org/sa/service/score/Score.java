package org.sa.service.score;

import org.sa.dto.SegmentDTO;

import java.util.Map;

public class Score {

  // one would get score 100, if ran flat 300m at 3min/km pace.
  private static final double EXCELLENCE_PACE_MIN_PER_KM_FOR_300_METER_FLAT_SEGMENT = 2.99;
  private static final int EXCELLENT_SCORE = 100;
  private static final double SCORE_FACTOR = EXCELLENT_SCORE * EXCELLENCE_PACE_MIN_PER_KM_FOR_300_METER_FLAT_SEGMENT;
  private static final Map<Long, Double> id_coefficient = Map.of(
      35709164L, 80.0 / 69.0); // "Veršvų piliakalnis+" segment should have higher score (getting 69 points seems like should be at least 80)


  public static int getScore(SegmentDTO s) {
    if (s.userPersonalRecordSeconds == null) return 0;
    return getScore(s, s.userPersonalRecordSeconds);
  }

  public static int getScore(SegmentDTO s, Integer timeSeconds) {
    if (timeSeconds == null) return 0;
    int normalizedFlatDistanceMeters = GradeAdjustmentModel.calculateGradeAdjustedFlatLength((int) s.nonFlatDistanceMeters, s.averageGradePercent);
    double normalizedPaceMinPerKm = DistancePaceNormalizer.normalizePaceFor300Meters(timeSeconds, normalizedFlatDistanceMeters);
    int score = (int) (SCORE_FACTOR / normalizedPaceMinPerKm);
    if (id_coefficient.get(s.id) != null) score *= id_coefficient.get(s.id);
    return score;
  }
}
