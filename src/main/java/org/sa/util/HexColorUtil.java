package org.sa.util;

import org.sa.console.WebColorGradientCalculator;

import java.awt.*;
import java.util.List;

public class HexColorUtil {

  private final List<String> colors;
  private final List<String> colorsDarker;

  public HexColorUtil(WebColorGradientCalculator webColorGradientCalculator) {
    this.colors = webColorGradientCalculator.generateGradient(Color.RED, Color.YELLOW, Color.GREEN, 101);
    this.colorsDarker = webColorGradientCalculator.generateGradient(Color.RED.darker(), Color.YELLOW.darker(), Color.GREEN.darker(), 101);
  }

  public String hexColorFromRedThroughYellowToGreen(int value) {
    value = Math.max(0, Math.min(100, value));
    return colors.get(value);
  }

  public String hexColorFromRedThroughYellowToGreenDarker(int value) {
    value = Math.max(0, Math.min(100, value));
    return colorsDarker.get(value);
  }
}
