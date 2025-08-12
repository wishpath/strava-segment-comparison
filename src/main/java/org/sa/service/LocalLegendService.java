package org.sa.service;

import org.sa.config.Console;
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
      if (!segmentId_localLegendRecord.containsKey(s.id)) {
        System.out.println(Console.TAB + Colors.LIGHT_GRAY + s.name + Colors.RESET);
        LocalLegendInfoDTO ll = stravaService.getLocalLegendInfo(s.id);
        if (ll == null) continue; // no tries in the last 90 days
        s.localLegendRecentAttemptCount = ll.legendEffortCount;
        if (ll.amLocalLegend) {
          s.amLocalLegend = true;
          s.myRecentAttemptCount = ll.legendEffortCount;
        }
        else s.myRecentAttemptCount = (int) stravaService.getMyRecentEffortCount(s.id);
        segmentId_localLegendRecord.put(s.id, new LocalLegendRecord(s.localLegendRecentAttemptCount, s.amLocalLegend, s.myRecentAttemptCount, LocalDateTime.now()));
        //appendLocalLegendRecordToCsv(s);
      }
    }
    overwriteLocalLegendStorageBeforeAppTerminates();
  }

//  private static void appendLocalLegendRecordToCsv(SegmentDTO s) {
//    String id = s.id + "";
//    String localLegendRecentAttemptCount = s.localLegendRecentAttemptCount + "";
//    String amLocalLegend = s.amLocalLegend ? "I am LocalLegend" : "other athlete is LocalLegend";
//    String myRecentAttemptCount = s.myRecentAttemptCount + "";
//    String recordDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//
//    String csvLine = id + "," + localLegendRecentAttemptCount + "," + amLocalLegend + "," + myRecentAttemptCount + "," + recordDate + "\n";
//
//    try {
//      Files.writeString(LOCAL_LEGEND_RECORDS_CSV_FILEPATH, csvLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//    } catch (IOException ignored) {}
//  }

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

