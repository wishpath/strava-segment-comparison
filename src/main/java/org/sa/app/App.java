package org.sa.app;

import org.sa.dto.SegmentDTO;
import org.sa.facade.PolylineFacade;
import org.sa.facade.PrintFacade;
import org.sa.service.*;

import java.io.IOException;
import java.util.*;

public class App {
  private static final String STRAVA_SEGMENT_URI =  "https://www.strava.com/segments/";
  private static final StravaService stravaService = new StravaService();
  private static SegmentsProcessor segmentsProcessor = new SegmentsProcessor();

  public static void main(String[] args) throws IOException {
    List<SegmentDTO> segments = new ArrayList<>();
    Map<Long, String> id_polyline = StorageUtil.loadPolylines("polylines.properties");

    stravaService
      .getStarredSegments()
      .stream()
      .filter(s -> CoordinateService.isCloseToHome(s))
      .filter(s -> s.activityType.equals("Run"))
      .filter(s -> s.averageGradePercent > 1)
      .sorted(Comparator.comparingInt(CoordinateService::getDistanceFromHomeInMeters))
      .sorted(Comparator.comparingInt(segmentsProcessor::getPerformanceScore))
      .peek(s -> PolylineFacade.fetchPolyline(s, id_polyline, segments, stravaService, segmentsProcessor))
      .peek(s -> s.score = segmentsProcessor.getPerformanceScore(s))
      .peek(s -> s.isKing = segmentsProcessor.isKing(s))
      //.peek(s -> s.allPeopleBestTimeSeconds = s.isKing? s.userPersonalRecordSeconds : stravaService.getFastestSegmentEffort(s.id))
      .peek(s -> s.allPeopleBestTimeSeconds = s.isKing? s.userPersonalRecordSeconds : HtmlFetcher.fetchSegmentFastestTimeSeconds(s.id))
      .peek(s -> s.allPeopleBestScore = s.isKing? s.score : segmentsProcessor.getPerformanceScore(s, s.allPeopleBestTimeSeconds))
      .peek(s -> s.link = STRAVA_SEGMENT_URI + s.id)
      .peek(s -> s.paceString = segmentsProcessor.calculatePace(s))
      .peek(s -> s.bestTimeString = segmentsProcessor.calculateBestTime(s))
      .peek(s -> s.deltaAltitude = s.elevationHighMeters - s.elevationLowMeters)
      .peek(s -> s.coordinate = s.startLatitudeLongitude.get(0) + "," + s.startLatitudeLongitude.get(1))
      .forEach(s -> segments.add(s));

    PrintFacade.printSegments(segments, segmentsProcessor);
    segmentsProcessor.setSegmentColors(segments);
    segmentsProcessor.pickWorstSegments(segments);
    segmentsProcessor.pickBestSegments(segments);

    //map
    MapService.exportSegmentsWithPolylinesToLeafletJS(segments);
    MapService.openMap("map_with_polylines.html");

    //store polyline
    StorageUtil.saveSegmentsToFile(id_polyline, "polylines.properties");
  }


}
