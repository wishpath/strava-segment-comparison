package org.sa.service;

import org.sa.config.Props;
import org.sa.console.Colors;
import org.sa.dto.LocalLegendInfoDTO;
import org.sa.dto.SegmentDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalLegendService {
  private static final Path LOCAL_LEGEND_RECORDS_CSV_FILEPATH = Path.of("src/main/java/org/sa/storage/localLegend.csv");
  private final Map<Long, LocalLegendRecord> segmentId_localLegendRecord = new HashMap<>();

  public LocalLegendService(){
    //load from file storage
    if (!Files.exists(LOCAL_LEGEND_RECORDS_CSV_FILEPATH)) return;
    LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
    List<String> lines = null;
    try {
      lines = Files.lines(LOCAL_LEGEND_RECORDS_CSV_FILEPATH).toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    lines.forEach(line -> {
      String[] lineParts = line.split(",");
      //if (lineParts.length != 5) throw new RuntimeException("WRONG RECORD FORMAT IN LOCAL LEGEND CSV");

      //naming the data
      long id = Long.parseLong(lineParts[0]);
      int localLegendRecentAttemptCount = Integer.parseInt(lineParts[1]);
      boolean amLocalLegend = lineParts[2].equals("I am LocalLegend");
      int myRecentAttemptCount = Integer.parseInt(lineParts[3]);
      LocalDateTime recordDate = LocalDateTime.parse(lineParts[4], DateTimeFormatter.ISO_LOCAL_DATE_TIME);

      if (recordDate.isAfter(oneWeekAgo))
        segmentId_localLegendRecord.put(id, new LocalLegendRecord(localLegendRecentAttemptCount, amLocalLegend, myRecentAttemptCount, recordDate));
    });
  }

  public void setLocalLegendStats(StravaService stravaService, List<SegmentDTO> segments) {
    for (SegmentDTO s : segments) {
      if (s.amKingOfMountain) continue;
      if (s.isEasiestToGetKingOfMountain) continue;

      LocalLegendRecord localLegendRecord = segmentId_localLegendRecord.get(s.id);
      if (localLegendRecord == null) {
        System.out.println(Props.TAB + "Parsing local legend stats: " + Colors.LIGHT_GRAY + s.name + Colors.RESET);
        LocalLegendInfoDTO ll = stravaService.getLocalLegendInfo(s.id);
        if (ll == null) continue; // no tries in the last 90 days
        int myRecentAttemptCount = ll.amLocalLegend ? ll.legendEffortCount : (int) stravaService.getMyRecentEffortCount(s.id);
        localLegendRecord = new LocalLegendRecord(ll.legendEffortCount, ll.amLocalLegend, myRecentAttemptCount, LocalDateTime.now());
        segmentId_localLegendRecord.put(s.id, localLegendRecord);
      }
      s.localLegendRecentAttemptCount = localLegendRecord.localLegendRecentAttemptCount;
      s.amLocalLegend = localLegendRecord.amLocalLegend;
      s.myRecentAttemptCount = localLegendRecord.myRecentAttemptCount;
    }
    overwriteLocalLegendStorageBeforeAppTerminates();
  }

  public void overwriteLocalLegendStorageBeforeAppTerminates() {
    //build content
    StringBuilder content = new StringBuilder();
    for (Map.Entry<Long, LocalLegendRecord> id_record : segmentId_localLegendRecord.entrySet()) {
      LocalLegendRecord record = id_record.getValue();
      content.append(
          id_record.getKey() + "," +
          record.localLegendRecentAttemptCount + "," +
          (record.amLocalLegend ? "I am LocalLegend" : "other athlete is LocalLegend") + "," +
          record.myRecentAttemptCount + "," +
          record.dateOfFetching.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
    }

    //overwrite
    try {
      Files.writeString(LOCAL_LEGEND_RECORDS_CSV_FILEPATH, content.toString());
    } catch (IOException e) {}
  }

  private record LocalLegendRecord(int localLegendRecentAttemptCount, boolean amLocalLegend, int myRecentAttemptCount, LocalDateTime dateOfFetching) { }
}

