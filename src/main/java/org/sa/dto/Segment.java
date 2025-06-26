package org.sa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Segment {
  @JsonProperty("id") public long id;
  @JsonProperty("resource_state") public int resourceState;
  @JsonProperty("name") public String name;
  @JsonProperty("activity_type") public String activityType;
  @JsonProperty("distance") public double distance;
  @JsonProperty("average_grade") public double averageGrade;
  @JsonProperty("maximum_grade") public double maximumGrade;
  @JsonProperty("elevation_high") public double elevationHigh;
  @JsonProperty("elevation_low") public double elevationLow;
  @JsonProperty("start_latlng") public List<Double> startLatLng;
  @JsonProperty("end_latlng") public List<Double> endLatLng;
  @JsonProperty("climb_category") public int climbCategory;
  @JsonProperty("city") public String city;
  @JsonProperty("state") public String state;
  @JsonProperty("country") public String country;
  @JsonProperty("private") public boolean isPrivate;
  @JsonProperty("hazardous") public boolean hazardous;
  @JsonProperty("starred") public boolean starred;
  @JsonProperty("pr_time") public Integer prTime;
  @JsonProperty("athlete_pr_effort") public AthletePrEffort athletePrEffort;
  @JsonProperty("starred_date") public String starredDate;
}
