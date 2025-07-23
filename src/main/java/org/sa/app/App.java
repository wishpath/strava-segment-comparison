package org.sa.app;

import org.sa.dto.SegmentDTO;
import org.sa.facade.CourseRecordFacade;
import org.sa.facade.PolylineFacade;
import org.sa.facade.PrintFacade;
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
  private static CourseRecordFacade courseRecordFacade = new CourseRecordFacade();

  public static void main(String[] args) throws IOException {
    List<SegmentDTO> segments = new ArrayList<>();
    Map<Long, String> id_polyline = StorageUtil.loadPolylines("polylines.properties");

    stravaService
      .getStarredSegments()
      .stream()
      .filter(s -> CoordinateService.isCloseToHome(s))
      .filter(s -> s.activityType.equals("Run"))
      .filter(s -> s.averageGradePercent > 1)
      .peek(s -> s.deltaAltitude = s.elevationHighMeters - s.elevationLowMeters)
      .sorted(Comparator.comparingInt(CoordinateService::getDistanceFromHomeInMeters))
      //.peek(s -> s.score = Score.getPerformanceScore(s))
      .peek(s -> s.myScore = Score.getScore(s))
      .sorted(Comparator.comparingInt(s -> s.myScore))
      .peek(s -> PolylineFacade.fetchPolyline(s, id_polyline, stravaService))
      .peek(s -> s.amKingOfMountain = segmentsProcessor.isKing(s))
      //.peek(s -> s.allPeopleBestTimeSeconds = courseRecordFacade.getAllPeopleBestTimeSeconds(s))
      //.peek(s -> s.allPeopleBestScore = s.isKing? s.score : Score.getPerformanceScore(s, s.allPeopleBestTimeSeconds))
      .peek(s -> s.allPeopleBestScore = s.amKingOfMountain ? s.myScore : Score.getScore(s, s.allPeopleBestTimeSeconds))
      .peek(s -> s.link = STRAVA_SEGMENT_URI + s.id)
      .peek(s -> s.paceString = segmentsProcessor.calculatePace(s))
      .peek(s -> s.bestTimeString = segmentsProcessor.calculateBestTime(s))
      .peek(s -> s.startCoordinatePair = s.startLatitudeLongitude.get(0) + "," + s.startLatitudeLongitude.get(1))
      .forEach(s -> segments.add(s));

    PrintFacade.printSegments(segments, segmentsProcessor);
    segmentsProcessor.setSegmentColors(segments);
    segmentsProcessor.setIsMyWorstScore(segments);
    segmentsProcessor.setIsMyBestScore(segments);
    segmentsProcessor.setIsEasiestToGetKingOfMountain(segments);
    segmentsProcessor.setLocalLegendStats(stravaService, segments); //slow

    //map
    MapService.exportSegmentsWithPolylinesToLeafletJS(segments);
    MapService.openMap("map_with_polylines.html");

    //store polylines and course records
    StorageUtil.savePolylinesToFile(id_polyline, "polylines.properties");
    courseRecordFacade.overwriteCourseRecordsBeforeAppTerminates();
  }
}
