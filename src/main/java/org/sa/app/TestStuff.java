package org.sa.app;

import org.sa.dto.LocalLegendInfoDTO;
import org.sa.service.StravaService;

public class TestStuff {
  public static void main(String[] args) {
    LocalLegendInfoDTO l = new StravaService().getLocalLegendInfo(39489017);
    System.out.println(l.toString());
  }
}
