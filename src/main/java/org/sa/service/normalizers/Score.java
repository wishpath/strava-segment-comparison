package org.sa.service.normalizers;

import org.sa.dto.SegmentDTO;

public class Score {
  public static int getScore(SegmentDTO s) {
    if (s.userPersonalRecordSeconds == null) return 0;
    return getScore((int)s.nonFlatDistanceMeters, s.averageGradePercent, s.userPersonalRecordSeconds);
  }

  public static int getScore(int distanceMeters, double averageGradient, int durationSeconds) {
    double flat300MetersPaceMinPerKm = normalizePaceFor300Meters(distanceMeters, averageGradient, durationSeconds);
    int score = flat300MetersPaceToScore(flat300MetersPaceMinPerKm);
    return score;
  }

  public static double normalizePaceFor300Meters(int distanceMeters, double averageGradient, int durationSeconds) {
    int normalizedDistanceMeters = GradeAdjustedPaceModel.calculateGradeAdjustedLength(distanceMeters, averageGradient);
    double normalizedPace = DistanceEffortNormalizer.normalizePaceFor300Meters(durationSeconds, normalizedDistanceMeters);
    return normalizedPace;
  }

  private static int flat300MetersPaceToScore(double normalizedPacePerKm) {
    double baselinePace = 4.0; // 4:00 min/km is considered average-good effort for 300m
    double scalingFactor = 25.0; // higher = less sensitive, lower = more steep

    // Score increases if pace is faster than baseline, decreases if slower
    double score = 100 + (baselinePace - normalizedPacePerKm) * scalingFactor;
    return Math.max(1, (int) Math.round(score)); // prevent negative scores
  }
}
