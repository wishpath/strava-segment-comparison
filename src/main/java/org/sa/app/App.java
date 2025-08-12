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
import java.util.Map;

public class App {
  private static final String STRAVA_SEGMENT_URI =  "https://www.strava.com/segments/";
  private static final StravaService stravaService = new StravaService();
  private static SegmentsProcessor segmentsProcessor = new SegmentsProcessor();
  private static LocalLegendService localLegendService = new LocalLegendService();
  private static AllPeopleBestTimeSecondsFacade allPeopleBestTimeSecondsFacade = new AllPeopleBestTimeSecondsFacade();

  public static void main(String[] args) throws IOException {
    System.out.println();
    List<SegmentDTO> segments = new ArrayList<>();
    Map<Long, String> id_polyline = StorageUtil.loadPolylines("polylines.properties");

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
      .peek(s -> PolylineFacade.fetchPolyline(s, id_polyline, stravaService))
      .peek(s -> s.amKingOfMountain = segmentsProcessor.isKing(s))
      .peek(s -> s.link = STRAVA_SEGMENT_URI + s.id)
      .peek(s -> s.paceString = segmentsProcessor.calculatePace(s))
      .peek(s -> s.bestTimeString = segmentsProcessor.calculateBestTime(s))
      .peek(s -> s.startCoordinatePair = s.startLatitudeLongitude.get(0) + "," + s.startLatitudeLongitude.get(1))
      .forEach(s -> segments.add(s));
    System.out.println("first block, ms: " + (System.currentTimeMillis() - start));

    //second block
    start = System.currentTimeMillis();
    segmentsProcessor.setSegmentColors(segments);
    segmentsProcessor.setIsMyWorstScore(segments);
    segmentsProcessor.setIsMyBestScore(segments);
    System.out.println("second block, ms: " + (System.currentTimeMillis() - start));

    //local legend stats
    System.out.println("local legend: ");
    start = System.currentTimeMillis();
    localLegendService.setLocalLegendStats(stravaService, segments);
    System.out.println("local legend stats, ms: " + (System.currentTimeMillis() - start));

    //all people best time stats
    start = System.currentTimeMillis();
    segmentsProcessor.setAllPeopleBestTimesAndScores(segments, allPeopleBestTimeSecondsFacade);
    System.out.println("all people best time stats, ms: " + (System.currentTimeMillis() - start));

    //easiest to get KOM
    start = System.currentTimeMillis();
    segmentsProcessor.setIsEasiestToGetKingOfMountain(segments);
    System.out.println("easiest to get KOM stats, ms: " + (System.currentTimeMillis() - start));

    //PrintFacade.printSegments(segments, segmentsProcessor);
    //map
    MapService.exportSegmentsWithPolylinesToLeafletJS(segments);
    MapService.openMap("map_with_polylines.html");

    //store polylines and course records
    StorageUtil.savePolylinesToFile(id_polyline, "polylines.properties");
    allPeopleBestTimeSecondsFacade.overwriteCourseRecordsBeforeAppTerminates();
  }
}
