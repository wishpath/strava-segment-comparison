package org.sa.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class StorageUtil {
  public static void saveSegmentsToPropertiesFile(Map<Long, String> id_polyline, String filePath) throws IOException {
    var props = new Properties();
    id_polyline.forEach((id, polyline) -> props.setProperty(id.toString(), polyline));
    try (var out = new FileOutputStream(filePath)) {
      props.store(out, "SegmentID to Polyline mappings");
    }
  }

  public static Map<Long, String> loadPolylines(String filePath) throws IOException {
    var props = new Properties();
    try (var in = new java.io.FileInputStream(filePath)) {
      props.load(in);
    }
    return props.entrySet().stream()
        .collect(java.util.stream.Collectors.toMap(
            e -> Long.parseLong(e.getKey().toString()),
            e -> e.getValue().toString()
        ));
  }
}
