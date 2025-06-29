package org.sa.service;

public class ColorUtil {
  public static String colorFromRedToBlue(int value) {
    value = Math.max(0, Math.min(100, value));
    if (value <= 50) {
      int red = 255;
      int blue = (int) (value * 255 / 50);
      return String.format("#%02X00%02X", red, blue);
    } else {
      int blue = 255;
      int red = 255 - (int) ((value - 50) * 255 / 50);
      return String.format("#%02X00%02X", red, blue);
    }
  }
}
