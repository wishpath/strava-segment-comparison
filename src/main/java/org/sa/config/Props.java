package org.sa.config;

import java.util.List;

public class Props {
  public static final double HOME_LATITUDE = Double.parseDouble(System.getenv("HOME_LATITUDE"));
  //public static final double HOME_LATITUDE = 49.3607058;
  //public static final double HOME_LONGITUDE = 19.8180418;
  public static final double HOME_LONGITUDE = Double.parseDouble(System.getenv("HOME_LONGITUDE"));



  public static final List<Double> HOME_COORDINATE = List.of(HOME_LATITUDE, HOME_LONGITUDE);
  public static final int DEFINITION_OF_CLOSENESS_METERS = 1700;
  public static String STRAVA_CLIENT_ID = System.getenv("STRAVA_CLIENT_ID");
  public static String STRAVA_CLIENT_SECRET = System.getenv("STRAVA_CLIENT_SECRET");
  public static String STRAVA_REFRESH_TOKEN = System.getenv("STRAVA_REFRESH_TOKEN");
  public static final long MY_ATHLETE_ID = 7280094;
}
