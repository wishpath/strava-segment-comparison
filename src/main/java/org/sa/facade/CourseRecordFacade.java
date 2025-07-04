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

public class CourseRecordFacade {
  private static final Path COURSE_RECORD_PATH = Path.of("course_record_times.csv");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private final Map<Long, CourseRecord> courseRecords = new HashMap<>();

  public CourseRecordFacade() { loadCourseRecords(); }

  private void loadCourseRecords() {
    if (!Files.exists(COURSE_RECORD_PATH)) return;
    LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
    try {
      Files.lines(COURSE_RECORD_PATH).forEach(line -> {
        String[] parts = line.split(",");
        if (parts.length < 3) return;
        long id = Long.parseLong(parts[0]);
        int bestTimeSeconds = Integer.parseInt(parts[1]);
        LocalDateTime recordDate = LocalDateTime.parse(parts[2], DATE_FORMATTER);
        if (recordDate.isAfter(oneWeekAgo))
          courseRecords.put(id, new CourseRecord(bestTimeSeconds, recordDate));
      });
    } catch (IOException ignored) {}
  }

  public int getAllPeopleBestTimeSeconds(SegmentDTO s) {
    //check if we already have the record
    if (s.isKing) return s.userPersonalRecordSeconds;
    if (courseRecords.containsKey(s.id))
      return courseRecords.get(s.id).bestTimeSeconds;

    //fetch and memorize
    System.out.print(s.name + ": fetching course record: ");
    int fetchedTime = HtmlFetcher.fetchSegmentFastestTimeSeconds(s.id);
    courseRecords.put(s.id, new CourseRecord(fetchedTime, LocalDateTime.now()));
    //doubles the record in the file, but ok since one is too old, and will be overwritten before app terminates
    appendLineToCSV(s.id, fetchedTime, LocalDateTime.now());

    return fetchedTime;
  }

  private void appendLineToCSV(long segmentId, int bestTimeSeconds, LocalDateTime dateTime) {
    try {
      String line = segmentId + "," + bestTimeSeconds + "," + dateTime.format(DATE_FORMATTER) + "\n";
      Files.writeString(COURSE_RECORD_PATH, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException ignored) {}
  }

  public void overwriteCourseRecordsBeforeAppTerminates() {
    //build content
    StringBuilder content = new StringBuilder();
    for (Map.Entry<Long, CourseRecord> id_record : courseRecords.entrySet()) {
      CourseRecord record = id_record.getValue();
      content.append(id_record.getKey() + "," + record.bestTimeSeconds + "," + record.date.format(DATE_FORMATTER) +"\n");
    }

    //overwrite
    try {
      Files.writeString(COURSE_RECORD_PATH, content.toString());
    } catch (IOException e) {}
  }

  private record CourseRecord(int bestTimeSeconds, LocalDateTime date) { }
}
