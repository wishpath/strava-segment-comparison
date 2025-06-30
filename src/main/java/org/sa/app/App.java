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
      .peek(segment -> PolylineFacade.fetchPolyline(segment, id_polyline, segments, stravaService, segmentsProcessor))
      .peek(segment -> segment.score = segmentsProcessor.getPerformanceScore(segment))
      .peek(segment -> segment.isKing = segmentsProcessor.isKing(segment))
      .peek(segment -> segment.link = STRAVA_SEGMENT_URI + segment.id)
      .forEach(segment -> segments.add(segment));

    PrintFacade.printSegments(segments,segmentsProcessor);
    segmentsProcessor.setSegmentColors(segments);
    segmentsProcessor.pickWorstSegments(segments);

    //map
    MapService.exportSegmentsWithPolylinesToLeafletJS(segments);
    MapService.openMap("map_with_polylines.html");

    //store polyline
    StorageUtil.saveSegmentsToFile(id_polyline, "polylines.properties");
  }


}
