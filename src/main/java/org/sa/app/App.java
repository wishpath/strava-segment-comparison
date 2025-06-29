package org.sa.app;

import org.sa.dto.SegmentDTO;
import org.sa.service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class App {
  private static final StravaService stravaService = new StravaService();
  private static SegmentsProcessor segmentsProcessor = new SegmentsProcessor();

  public static void main(String[] args) throws IOException {
    List<SegmentDTO> segments = new ArrayList<>();

    stravaService
      .getStarredSegments()
      .stream()
      .filter(s -> CoordinateService.isCloseToHome(s))
      .filter(s -> s.activityType.equals("Run"))
      .filter(s -> s.averageGradePercent > 1)
      .sorted(Comparator.comparingInt(CoordinateService::getDistanceFromHomeInMeters))
      .sorted(Comparator.comparingInt(segmentsProcessor::getPerformanceScore))
      .forEach(s -> {
        s.polyline = stravaService.getSegmentPolyline(s.id);
        s.score = segmentsProcessor.getPerformanceScore(s);
        segments.add(s);
      });

    PrintUtil.printSegments(segments,segmentsProcessor);
    segmentsProcessor.setSegmentColors(segments);

    //map
    MapService.exportSegmentsWithPolylinesToLeafletJS(segments);
    MapService.openMap("map_with_polylines.html");
  }




}
