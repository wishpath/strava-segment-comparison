package org.sa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPersonalRecordDTO {
  @JsonProperty("id") public long id;
  @JsonProperty("activity_id") public long activityId;
  @JsonProperty("elapsed_time") public int elapsedTime;
  @JsonProperty("distance") public double distance;
  @JsonProperty("start_date") public String startDate;
  @JsonProperty("start_date_local") public String startDateLocal;
  @JsonProperty("is_kom") public boolean isKingOfMountain; //does not mean necessarily, is absolute best. Means best for gender (men or women). Someone who does not pick gender, does not steal KOM from men or women.

  @Override
  public String toString() {
    return "UserPersonalRecordDTO{" +
        "id=" + id +
        ", activityId=" + activityId +
        ", elapsedTime=" + elapsedTime +
        ", distance=" + distance +
        ", startDate='" + startDate + '\'' +
        ", startDateLocal='" + startDateLocal + '\'' +
        ", isKingOfMountain=" + isKingOfMountain +
        '}';
  }
}
