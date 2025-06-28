package org.sa.service;

import java.util.ArrayList;
import java.util.List;

public class PolylineUtil {
  /**
   * Decodes a Google Encoded Polyline string into a list of [latitude, longitude] points.
   *
   * Google Polyline is a lossy compressed format for encoding a series of lat/lng coordinates.
   * Each coordinate is encoded using a series of characters, each representing 5 bits of data.
   * This function reverses that encoding back into actual coordinates.
   *
   * @param encoded The encoded polyline string (e.g., from Strava or Google Maps)
   * @return A list of [latitude, longitude] pairs (in decimal degrees)
   */
  public static List<List<Double>> decodePolyline(String encoded) {
    List<List<Double>> path = new ArrayList<>();
    int index = 0, len = encoded.length();
    int lat = 0, lng = 0;

    while (index < len) {
      int b, shift = 0, result = 0;
      do {
        b = encoded.charAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      int dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
      lat += dlat;

      shift = 0;
      result = 0;
      do {
        b = encoded.charAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      int dlng = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
      lng += dlng;

      path.add(List.of(lat / 1e5, lng / 1e5));
    }

    return path;
  }
}
