package org.sa.service;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public class StorageUtil {

  //literal properties
//  public static void saveSegmentsToPropertiesFile(Map<Long, String> id_polyline, String filePath) throws IOException {
//    var props = new Properties();
//    id_polyline.forEach((id, polyline) -> props.setProperty(id.toString(), polyline));
//    try (var out = new FileOutputStream(filePath)) {
//      props.store(out, "SegmentID to Polyline mappings");
//    }
//  }
//
//  public static Map<Long, String> loadPolylines(String filePath) throws IOException {
//    var props = new Properties();
//    try (var in = new java.io.FileInputStream(filePath)) {
//      props.load(in);
//    }
//    return props.entrySet().stream()
//        .collect(java.util.stream.Collectors.toMap(
//            e -> Long.parseLong(e.getKey().toString()),
//            e -> e.getValue().toString()
//        ));
//  }


//  //base 64 to properties
//  public static void saveSegmentsToPropertiesFile(Map<Long, String> id_polyline, String filePath) throws IOException {
//    var props = new Properties();
//    id_polyline.forEach((id, polyline) -> {
//      var encoded = Base64.getEncoder().encodeToString(polyline.getBytes());
//      props.setProperty(id.toString(), encoded);
//    });
//    try (var out = new FileOutputStream(filePath)) {
//      props.store(out, "SegmentID to Polyline mappings");
//    }
//  }
//
//  public static Map<Long, String> loadPolylines(String filePath) throws IOException {
//    var props = new Properties();
//    try (var in = new java.io.FileInputStream(filePath)) {props.load(in);}
//    return props.entrySet().stream()
//        .collect(java.util.stream.Collectors.toMap(
//            e -> Long.parseLong(e.getKey().toString()),
//            e -> new String(Base64.getDecoder().decode(e.getValue().toString()))
//        ));
//  }

  // literal to txt
  public static void saveSegmentsToFile(Map<Long, String> idPolyline, String filePath) throws IOException {
    try (var writer = new BufferedWriter(new FileWriter(filePath))) {
      for (var entry : idPolyline.entrySet())
        writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
    }
  }

  public static Map<Long, String> loadPolylines(String filePath) throws IOException {
    try (var reader = new BufferedReader(new FileReader(filePath))) {
      return reader.lines()
          .filter(line -> line.contains("="))
          .collect(Collectors.toMap(
              line -> Long.parseLong(line.substring(0, line.indexOf('='))),
              line -> line.substring(line.indexOf('=') + 1)
          ));
    }
  }

}
