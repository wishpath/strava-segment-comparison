package org.sa.facade;

import org.sa.dto.SegmentDTO;
import org.sa.service.HtmlFetcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AllPeopleBestTimeSecondsFacade {
  private static final Path COURSE_ALL_POPLE_BEST_TIME_SECONDS_CSV_FILEPATH = Path.of("src/main/java/org/sa/storage/course_record_times.csv");
  private final Map<Long, CourseRecord> segmentId_courseAllPeopleBestTimeRecord = new HashMap<>();

  public AllPeopleBestTimeSecondsFacade() { loadCourseRecords(); }

  private void loadCourseRecords() {
    if (!Files.exists(COURSE_ALL_POPLE_BEST_TIME_SECONDS_CSV_FILEPATH)) return;
    LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
    try {
      Files.lines(COURSE_ALL_POPLE_BEST_TIME_SECONDS_CSV_FILEPATH).forEach(line -> {
        String[] lineParts = line.split(",");
        if (lineParts.length < 3) return;
        long id = Long.parseLong(lineParts[0]);
        int bestTimeSeconds = Integer.parseInt(lineParts[1]);
        LocalDateTime recordDate = LocalDateTime.parse(lineParts[2], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (recordDate.isAfter(oneWeekAgo))
          segmentId_courseAllPeopleBestTimeRecord.put(id, new CourseRecord(bestTimeSeconds, recordDate));
      });
    } catch (IOException ignored) {}
  }

  public int getAllPeopleBestTimeSeconds(SegmentDTO s) {
    //check if we already have the record
    if (s.amKingOfMountain) return s.userPersonalRecordSeconds; //
    if (segmentId_courseAllPeopleBestTimeRecord.containsKey(s.id)) // course record is stored in the file
      return segmentId_courseAllPeopleBestTimeRecord.get(s.id).bestTimeSeconds;

    //fetch and memorize
    System.out.print(s.name + ": fetching course record: ");
    int fetchedTime = HtmlFetcher.fetchAllPeopleBestTimeSeconds(s);
    segmentId_courseAllPeopleBestTimeRecord.put(s.id, new CourseRecord(fetchedTime, LocalDateTime.now()));

    //doubles the record in the file, but that is ok since first one is too old, and will be overwritten before app terminates
    appendAllPeopleBestTimeRecordToCSV(s.id, fetchedTime, LocalDateTime.now());

    return fetchedTime;
  }

  private void appendAllPeopleBestTimeRecordToCSV(long segmentId, int bestTimeSeconds, LocalDateTime dateTime) {
    try {
      String line = segmentId + "," + bestTimeSeconds + "," + dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n";
      Files.writeString(COURSE_ALL_POPLE_BEST_TIME_SECONDS_CSV_FILEPATH, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException ignored) {}
  }

  public void overwriteCourseRecordsBeforeAppTerminates() {
    //build content
    StringBuilder content = new StringBuilder();
    for (Map.Entry<Long, CourseRecord> id_record : segmentId_courseAllPeopleBestTimeRecord.entrySet()) {
      CourseRecord record = id_record.getValue();
      content.append(id_record.getKey() + "," + record.bestTimeSeconds + "," + record.dateOfFetching.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +"\n");
    }

    //overwrite
    try {
      Files.writeString(COURSE_ALL_POPLE_BEST_TIME_SECONDS_CSV_FILEPATH, content.toString());
    } catch (IOException e) {}
  }

  private record CourseRecord(int bestTimeSeconds, LocalDateTime dateOfFetching) { }
}
