package org.sa.app;

import org.sa.dto.SegmentDTO;
import org.sa.service.AllPeopleBestTimeHtmlFetcher;

import java.io.IOException;

public class TestStuff {
  public static void main(String[] args) throws IOException {
    //LocalLegendInfoDTO l = new StravaService().getLocalLegendInfo(39489017);
    //https://www.strava.com/segments/39422974
    String dom = AllPeopleBestTimeHtmlFetcher.fetchSegmentPageDom(new SegmentDTO());
    //System.out.println(l.toString());
    System.out.println(dom);
  }
}
