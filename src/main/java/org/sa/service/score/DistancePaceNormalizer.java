package org.sa.service.score;

import java.util.List;

record DistancePace(double distanceMeters, double referencePacePerKm) {}

public class DistancePaceNormalizer {

  /**
   * Reference table of typical elite-level pace (min/km) for a range of distances.
   * These serve as a basis for normalization — the idea is that each listed pace
   * represents the same level of athletic performance at its respective distance.
   */
  static final List<DistancePace> REFERENCE_PACES = List.of(
      new DistancePace(50,    1.70), // m    // t / m
      new DistancePace(100,   2.00),
      new DistancePace(300,   2.40),
      new DistancePace(400,   2.50),
      new DistancePace(750,   2.80),
      new DistancePace(1000,  3.00),
      new DistancePace(5000,  3.50),
      new DistancePace(10000, 4.00),
      new DistancePace(21097, 4.30),
      new DistancePace(42195, 4.50)
  );

  /**
   * Estimates reference pace (min/km) for a given distance by linear interpolation
   * between the closest known reference distances.
   */
  public static double referencePaceFor(double meters) {
    if (meters <= REFERENCE_PACES.get(0).distanceMeters())
      return REFERENCE_PACES.get(0).referencePacePerKm();

    for (int i = 1; i < REFERENCE_PACES.size(); i++) {

      DistancePace lower = REFERENCE_PACES.get(i - 1);
      DistancePace upper = REFERENCE_PACES.get(i);

      if (meters <= upper.distanceMeters()) {
        double ratio = (meters - lower.distanceMeters()) / (upper.distanceMeters() - lower.distanceMeters());
        return lower.referencePacePerKm() * (1 - ratio) + upper.referencePacePerKm() * ratio;
      }
    }

    return REFERENCE_PACES.getLast().referencePacePerKm();
  }


  /**
   * Takes a performance (time and distance) and normalizes it to 300 meters.
   * Returns the normalized pace in minutes per km.
   */
  public static double normalizePaceFor300Meters(int timeSeconds, int distanceMeters) {
    double actualPacePerKm = timeSeconds / 60.0 / (distanceMeters / 1000.0);
    return normalizePace(distanceMeters, actualPacePerKm, 300);
  }

  public static void main(String[] args) {
    double targetMeters = 300;

    for (DistancePace dp : REFERENCE_PACES) {
      double normalized = normalizePace(dp.distanceMeters(), dp.referencePacePerKm(), targetMeters);
      System.out.printf("From %5.0fm @ %.2f → Normalized to %.2f min/km at %.0fm%n",
          dp.distanceMeters(), dp.referencePacePerKm(), normalized, targetMeters);
    }

    System.out.println();

    // Example: normalize someone who ran 1000m in 3 minutes (180 seconds) to 300m equivalent pace
    int testTimeSec = 180, testDistance = 1000;
    double normalizedTo300 = normalizePaceFor300Meters(testTimeSec, testDistance);
    System.out.printf("Performance: %dm in %ds → normalized to %.2f min/km at 300m%n",
        testDistance, testTimeSec, normalizedTo300);
  }


  /**
   * Normalizes a given pace (min/km) over `distanceMeters` to its equivalent at `targetMeters`.
   * Keeps the relative effort the same, assuming the runner performs equally well across distances.
   */
  public static double normalizePace(double distanceMeters, double actualPacePerKm, double targetMeters) {
    double relativeEffort = actualPacePerKm / referencePaceFor(distanceMeters);
    return relativeEffort * referencePaceFor(targetMeters);
  }
}
