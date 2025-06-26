package org.sa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AthletePrEffort {
  @JsonProperty("id") public long id;
  @JsonProperty("activity_id") public long activityId;
  @JsonProperty("elapsed_time") public int elapsedTime;
  @JsonProperty("distance") public double distance;
  @JsonProperty("start_date") public String startDate;
  @JsonProperty("start_date_local") public String startDateLocal;
  @JsonProperty("is_kom") public boolean isKom;
}
