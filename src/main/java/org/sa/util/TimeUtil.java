package org.sa.util;

public class TimeUtil {
  public static String formatMinutesAndSecondsNoLetters(int seconds) {
    if (seconds <= 0) return "-1";
    if (seconds < 60) return "0:" + (seconds < 10 ? "0" : "") + seconds;
    int minutes = seconds / 60, remainingSeconds = seconds % 60;
    return minutes + ":" + (remainingSeconds < 10 ? "0" : "") + remainingSeconds;
  }


  public static String calculatePaceString(Integer totalTimeSeconds, double distanceMeters) {
    if (totalTimeSeconds == null || distanceMeters <= 0) return "-1";
    int totalSeconds = (int) Math.round(totalTimeSeconds / (distanceMeters / 1000.0));
    int min = totalSeconds / 60, sec = totalSeconds % 60;
    return min + ":" + (sec < 10 ? "0" : "") + sec;
  }
}
