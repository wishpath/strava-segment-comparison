package org.sa.service;

import org.sa.console.WebColorGradientCalculator;

import java.awt.*;
import java.util.List;

public class HexColorUtil {

  private final List<String> colors;

  public HexColorUtil(WebColorGradientCalculator webColorGradientCalculator) {
    this.colors = webColorGradientCalculator.generateGradient(Color.RED, Color.YELLOW, Color.GREEN, 101);
  }

  public String hexColorFromRedToBlue(int value) {
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

  public String hexColorFromRedThroughYellowToGreen(int value) {
    value = Math.max(0, Math.min(100, value));
    return colors.get(value);
  }
}
