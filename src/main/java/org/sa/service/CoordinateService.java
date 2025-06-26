package org.sa.service;

import org.sa.config.Props;
import org.sa.dto.Segment;

import java.util.List;

public class CoordinateService {
  public static int getDistanceInMeters(List<Double> from, List<Double> to) {
    double R = 6371000; // Earth's radius in meters
    double lat1 = Math.toRadians(from.get(0));
    double lon1 = Math.toRadians(from.get(1));
    double lat2 = Math.toRadians(to.get(0));
    double lon2 = Math.toRadians(to.get(1));

    double dLat = lat2 - lat1;
    double dLon = lon2 - lon1;
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return (int) (R * c);
  }

  public static int getDistanceFromHomeInMeters(Segment segment) {
    List<Double> to = segment.startLatLng;
    return getDistanceInMeters(Props.HOME_COORDINATE, to);
  }

  public static boolean isCloseToHome(Segment segment) {
    return CoordinateService.getDistanceFromHomeInMeters(segment) < Props.DEFINITION_OF_CLOSENESS_METERS;
  }
}
