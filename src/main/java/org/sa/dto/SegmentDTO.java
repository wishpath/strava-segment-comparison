package org.sa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SegmentDTO {
  @JsonProperty("id") public long id;
  @JsonProperty("resource_state") public int resourceState; // indicates how much detail is available: 1 = only ID (meta), 2 = basic info (summary), 3 = full detail; useful to know whether other fields are reliably populated
  @JsonProperty("name") public String name;
  @JsonProperty("activity_type") public String activityType; // (e.g., Ride, Run)
  @JsonProperty("distance") public double nonFlatDistanceMeters;
  @JsonProperty("average_grade") public double averageGradePercent;
  @JsonProperty("maximum_grade") public double maximumGradePercent; // maybe for about 25-50 meter subsegment
  @JsonProperty("elevation_high") public double elevationHighMeters;
  @JsonProperty("elevation_low") public double elevationLowMeters;
  @JsonProperty("start_latlng") public List<Double> startLatitudeLongitude;
  @JsonProperty("end_latlng") public List<Double> endLatitudeLongitude;
  @JsonProperty("climb_category") public int climbCategory; // 1 - hardest, 5 - easiest, 0 - not a climb
  @JsonProperty("city") public String city;
  @JsonProperty("state") public String state;
  @JsonProperty("country") public String country;
  @JsonProperty("private") public boolean isPrivate;
  @JsonProperty("hazardous") public boolean hazardous;
  @JsonProperty("starred") public boolean starred;
  @JsonProperty("pr_time") public Integer userPersonalRecordSeconds;
  @JsonProperty("athlete_pr_effort") public UserPersonalRecordDTO userPersonalRecordDTO; //if i have not tried this segmet this should be null... probably
  @JsonProperty("starred_date") public String starredDate; // date when segment was starred

  // about segment itself
  public String polyline; //Google Polyline is a lossy-compressed format for encoding a series of lat/lng coordinates.
  public String link; // to Strava segment page
  public String startCoordinatePair; //e.g, San Francisco: "37.7749,-122.4194"
  public double deltaAltitude;


  //my attempt stats
  public String paceString;
  public String bestTimeString;


  //my score and comparisons
  public int myScore;
  public boolean isMyLowestScore = false; //lowest score comparing my other segments (not including "King Of the Mountain" ones)
  public boolean amKingOfMountain = false; // better than all other athletes
  public boolean isMyStrongestSegmentAttempted = false; //best score comparing my other segments (not including "King Of the Mountain" ones)


  //other people score
  public int allPeopleBestScore;
  public int allPeopleBestTimeSeconds;
  public boolean isEasiestToGetKingOfMountain = false; // has lowest allPeopleBestScore comparing other segments (not including "King Of the Mountain" ones)

  //local legend
  public boolean amLocalLegend = false;
  public int myRecentAttemptCount = 0;
  public int localLegendRecentAttemptCount = 0;

  //color
  public String webColor; // color for pin and polyline on the map
  public String webColorDarker; // color for hovered polyline
}
