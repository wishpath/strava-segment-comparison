package org.sa.helper.score;

import java.util.List;

public class GradeAdjustmentModelUtil {

  private static final List<GradientFactor> ADJUSTMENT_TABLE = List.of(
      //more significance on uphill effort
      new GradientFactor(-25, 1.00),
      new GradientFactor(-18, 1.00),
      new GradientFactor(-15, 0.96),
      new GradientFactor(-12, 0.94),
      new GradientFactor(-9,  0.92),
      new GradientFactor(-6,  0.95),
      new GradientFactor(-3,  0.98),
      new GradientFactor(0,   1.00),
      new GradientFactor(3,   0.95),
      new GradientFactor(6,   0.88),
      new GradientFactor(9,   0.80),
      new GradientFactor(12,  0.72),
      new GradientFactor(15,  0.66),
      new GradientFactor(18,  0.60),
      new GradientFactor(21,  0.55),
      new GradientFactor(25,  0.50)
  );

  public static int calculateGradeAdjustedFlatLength(int meters, double gradientPercent) {
    return (int) Math.round(meters / getGradeAdjustedFactor(gradientPercent));
  }

  public static double getGradeAdjustedFactor(double gradientPercent) {
    if (gradientPercent <= ADJUSTMENT_TABLE.get(0).gradient)
      return ADJUSTMENT_TABLE.get(0).factor;
    for (int i = 1; i < ADJUSTMENT_TABLE.size(); i++) {
      GradientFactor lower = ADJUSTMENT_TABLE.get(i - 1);
      GradientFactor upper = ADJUSTMENT_TABLE.get(i);
      if (gradientPercent <= upper.gradient)
        return interpolate(lower, upper, gradientPercent);
    }
    return ADJUSTMENT_TABLE.getLast().factor;
  }

  private static double interpolate(GradientFactor lower, GradientFactor upper, double targetGradient) {
    double range = upper.gradient - lower.gradient;
    double delta = targetGradient - lower.gradient;
    double ratio = delta / range;
    return lower.factor + ratio * (upper.factor - lower.factor);
  }

  private record GradientFactor(double gradient, double factor) {}
}

