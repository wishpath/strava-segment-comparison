package org.sa.facade;

import org.sa.dto.SegmentDTO;
import org.sa.service.StravaService;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PolylineFacade {

  private final StravaService stravaService;
  private Map<Long, String> id_polyline = loadPolylines();
  private static final String POLYLINE_STORAGE_FILEPATH =  "src/main/java/org/sa/storage/polylines.properties";


  public PolylineFacade(StravaService stravaService) throws IOException {
    this.stravaService = stravaService;
  }

  public void fetchPolylines(List<SegmentDTO> segments) throws IOException {
    for (SegmentDTO s : segments) {
      if (id_polyline.containsKey(s.id)) s.polyline = id_polyline.get(s.id);
      else {
        s.polyline = stravaService.getSegmentPolyline(s.id);
        id_polyline.put(s.id, s.polyline);
      }
    }
    savePolylinesToFile(id_polyline);
  }

  public void savePolylinesToFile(Map<Long, String> idPolyline) throws IOException {
    try (var writer = new BufferedWriter(new FileWriter(POLYLINE_STORAGE_FILEPATH))) {
      for (var entry : idPolyline.entrySet())
        writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
    }
  }

  public Map<Long, String> loadPolylines() throws IOException {
    try (var reader = new BufferedReader(new FileReader(POLYLINE_STORAGE_FILEPATH))) {
      return reader.lines()
          .filter(line -> line.contains("="))
          .collect(Collectors.toMap(
              line -> Long.parseLong(line.substring(0, line.indexOf('='))),
              line -> line.substring(line.indexOf('=') + 1)
          ));
    }
  }
}
