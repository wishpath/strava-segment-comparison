package org.sa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.sa.config.Props;
import org.sa.console.Colors;

public class LocalLegendInfoDTO {
  @JsonProperty("athlete_id") public long legendId; //id of whoever legend is
  @JsonProperty("title") public String athleteName;
  @JsonProperty("effort_count") public int legendEffortCount;
  @JsonProperty("rank") public int legendRank; // 0
  @JsonProperty("effort_counts") public EffortCounts effortCounts; //probably legend effort counts ass well...

  public boolean amLocalLegend = false;

  @JsonProperty("athlete_id")
  private void unpackLegendId(long id) {
    this.legendId = id;
    if (id == Props.MY_ATHLETE_ID) {
      this.amLocalLegend = true;
    }
  }

  public static class EffortCounts {
    @JsonProperty("overall") public String yourOverallEfforts; // probably legend effort counts ass well...
  }

  @Override
  public String toString() {
    String color = amLocalLegend ? Colors.GREEN : Colors.ORANGE;
    return color + "LocalLegendInfoDTO{\n" +
        Props.TAB + "legendId=" + legendId + ",\n" +
        Props.TAB + "athleteName='" + athleteName + "\',\n" +
        Props.TAB + "legendEffortCount=" + legendEffortCount +  ",\n" +
        Props.TAB + "amLocalLegend=" + amLocalLegend + ",\n" +
        '}' + Colors.RESET;
  }
}
