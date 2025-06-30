package org.sa.facade;

import org.sa.dto.SegmentDTO;
import org.sa.service.SegmentsProcessor;
import org.sa.service.StravaService;

import java.util.List;
import java.util.Map;

public class PolylineFacade {
  public static void fetchPolyline(SegmentDTO segment, Map<Long, String> id_polyline, List<SegmentDTO> segments, StravaService stravaService, SegmentsProcessor segmentsProcessor) {
    if (id_polyline.containsKey(segment.id)) segment.polyline = id_polyline.get(segment.id);
    else {
      segment.polyline = stravaService.getSegmentPolyline(segment.id);
      id_polyline.put(segment.id, segment.polyline);
    }
  }
}
