package org.sa.service;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public class StorageUtil {

  // literal to txt
  public static void savePolylinesToFile(Map<Long, String> idPolyline, String filePath) throws IOException {
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
