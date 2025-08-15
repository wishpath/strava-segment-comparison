package org.sa.app;

import org.sa.dto.SegmentDTO;
import org.sa.facade.AllPeopleBestTimeSecondsFacade;
import org.sa.facade.PolylineFacade;
import org.sa.service.*;
import org.sa.service.score.Score;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class App {
  private static final String STRAVA_SEGMENT_URI =  "https://www.strava.com/segments/";
  private static final StravaService stravaService = new StravaService();
  private static SegmentsProcessor segmentsProcessor = new SegmentsProcessor();
  private static LocalLegendService localLegendService = new LocalLegendService();
  private static AllPeopleBestTimeSecondsFacade allPeopleBestTimeSecondsFacade = new AllPeopleBestTimeSecondsFacade();

  public static void main(String[] args) throws IOException {
    System.out.println();
    List<SegmentDTO> segments = new ArrayList<>();

    //first block
    long start = System.currentTimeMillis();
    stravaService
      .getStarredSegments()
      .stream()
      .filter(s -> CoordinateService.isCloseToHome(s))
      .filter(s -> s.activityType.equals("Run"))
      .filter(s -> s.averageGradePercent > 1)
      .peek(s -> s.deltaAltitude = s.elevationHighMeters - s.elevationLowMeters)
      .sorted(Comparator.comparingInt(CoordinateService::getDistanceFromHomeInMeters))
      .peek(s -> s.myScore = Score.getScore(s))
      .sorted(Comparator.comparingInt(s -> s.myScore))
      .peek(s -> s.amKingOfMountain = segmentsProcessor.amKingOfMountain(s))
      .peek(s -> s.link = STRAVA_SEGMENT_URI + s.id)
      .peek(s -> s.myPaceString = segmentsProcessor.calculatePace(s))
      .peek(s -> s.myBestTimeString = segmentsProcessor.formatBestTimeStringExplicit(s))
      .peek(s -> s.startCoordinatePair = s.startLatitudeLongitude.get(0) + "," + s.startLatitudeLongitude.get(1))
      .forEach(s -> segments.add(s));
    System.out.println("first block, ms: " + (System.currentTimeMillis() - start));

    //fetch polylines
    start = System.currentTimeMillis();
    new PolylineFacade(stravaService).fetchPolylines(segments);
    System.out.println("fetched polylines, ms: " + (System.currentTimeMillis() - start));

    //all people best time stats
    start = System.currentTimeMillis();
    segmentsProcessor.setAllPeopleBestTimesAndScores_andFixAmKOM(segments, allPeopleBestTimeSecondsFacade);
    segmentsProcessor.formatAllPeopleBestTimeStrings(segments);
    segmentsProcessor.formatAllPeoplePaceStrings(segments);
    System.out.println("all people best time stats, ms: " + (System.currentTimeMillis() - start));

    //block B
    start = System.currentTimeMillis();
    segmentsProcessor.setSegmentColors(segments);
    segmentsProcessor.setIsMyWorstScore(segments);
    segmentsProcessor.setIsMyBestScore(segments);
    System.out.println("block B, ms: " + (System.currentTimeMillis() - start));

    //local legend stats
    start = System.currentTimeMillis();
    localLegendService.setLocalLegendStats(stravaService, segments);
    System.out.println("local legend stats, ms: " + (System.currentTimeMillis() - start));

    //easiest to get KOM
    start = System.currentTimeMillis();
    segmentsProcessor.setIsEasiestToGetKingOfMountain(segments);
    System.out.println("easiest to get KOM stats, ms: " + (System.currentTimeMillis() - start));

    //PrintFacade.printSegments(segments, segmentsProcessor);
    //map
    MapService.exportSegmentsWithPolylinesToLeafletJS(segments);
    MapService.openMap("map_with_polylines.html");

    //store course records
    allPeopleBestTimeSecondsFacade.overwriteCourseRecordsBeforeAppTerminates();
  }
}
