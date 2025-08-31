package org.sa.app;

import org.sa.config.Props;
import org.sa.console.SimpleColorPrint;
import org.sa.dto.SegmentDTO;
import org.sa.facade.AllPeopleBestTimeSecondsFacade;
import org.sa.facade.PolylineFacade;
import org.sa.service.LocalLegendService;
import org.sa.service.MapService;
import org.sa.service.SegmentsProcessor;
import org.sa.service.StravaService;
import org.sa.service.score.Score;

import java.io.IOException;
import java.util.List;

public class App {
  private static final String STRAVA_SEGMENT_URI =  "https://www.strava.com/segments/";
  private static final StravaService stravaService = new StravaService();
  private static SegmentsProcessor segmentsProcessor = new SegmentsProcessor();
  private static LocalLegendService localLegendService = new LocalLegendService();
  private static AllPeopleBestTimeSecondsFacade allPeopleBestTimeSecondsFacade = new AllPeopleBestTimeSecondsFacade();

  public static void main(String[] args) throws IOException {
    SimpleColorPrint.red("Tracking how efficient each code block is:");

    //BLOCK A: download segments and enhance them with calculated values
    long start = System.currentTimeMillis();
    List<SegmentDTO> segments = stravaService.getStarredSegmentsFilterAndSort();
    for (SegmentDTO s : segments) {
      s.deltaAltitude = s.elevationHighMeters - s.elevationLowMeters;
      s.myScore = Score.getScore(s);
      s.amKingOfMountain = segmentsProcessor.amKingOfMountain(s);
      s.link = STRAVA_SEGMENT_URI + s.id;
      s.myPaceString = segmentsProcessor.calculatePace(s);
      s.myBestTimeString = segmentsProcessor.formatBestTimeString(s);
      s.startCoordinatePair = s.startLatitudeLongitude.get(0) + "," + s.startLatitudeLongitude.get(1);
    }
    System.out.println(Props.TAB + (System.currentTimeMillis() - start) + "ms, BLOCK A: download segments and enhance them with calculated values");

    //BLOCK B: download polylines
    start = System.currentTimeMillis();
    new PolylineFacade(stravaService).fetchPolylines(segments);
    System.out.println(Props.TAB + (System.currentTimeMillis() - start) + "ms, BLOCK B: download polylines" );

    //BLOCK C: download (html fetch) or remember all people best times
    start = System.currentTimeMillis();
    allPeopleBestTimeSecondsFacade.setAllPeopleBestTimes(segments);
    segmentsProcessor.fixAmKOM(segments);
    segmentsProcessor.setAllPeopleBestScores(segments);
    segmentsProcessor.formatAllPeopleBestTimeStrings(segments);
    segmentsProcessor.formatAllPeoplePaceStrings(segments);
    allPeopleBestTimeSecondsFacade.overwriteAllPeopleBestTimesBeforeAppTerminates(); //storage
    System.out.println(Props.TAB + (System.currentTimeMillis() - start) + "ms, BLOCK C: download (html fetch) or remember all people best times" );

    //BLOCK D: local calculations: color and score flags:
    start = System.currentTimeMillis();
    segmentsProcessor.setSegmentColors(segments);
    segmentsProcessor.setIsMyWorstScore(segments);
    segmentsProcessor.setIsMyBestScore(segments);
    System.out.println(Props.TAB + (System.currentTimeMillis() - start) + "ms, BLOCK D: local calculation: color and score flags" );

    //BLOCK E: download or remember local legend stats
    start = System.currentTimeMillis();
    localLegendService.setLocalLegendStats(stravaService, segments);
    System.out.println(Props.TAB + (System.currentTimeMillis() - start) + "ms, BLOCK E: download or remember local legend stats" );

    //BLOCK F: local calculation: easiest to get KOM
    start = System.currentTimeMillis();
    segmentsProcessor.setIsEasiestToGetKingOfMountain(segments);
    System.out.println(Props.TAB + (System.currentTimeMillis() - start) +  "ms, BLOCK F: local calculation: is easiest to get KOM");

    //PrintFacade.printSegments(segments, segmentsProcessor);
    //map
    MapService.exportSegmentsWithPolylinesToLeafletJS(segments);
    MapService.openMap("src/main/java/org/sa/storage/map_with_polylines.html");
  }
}
