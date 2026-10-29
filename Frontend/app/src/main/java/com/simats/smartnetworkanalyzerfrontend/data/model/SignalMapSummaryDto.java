package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SignalMapSummaryDto {
    @SerializedName("total_points")
    public int totalPoints;

    @SerializedName("avg_rssi_dbm")
    public Double avgRssiDbm;

    @SerializedName("strongest_point")
    public PointZoneInfo strongestPoint;

    @SerializedName("weakest_point")
    public PointZoneInfo weakestPoint;

    @SerializedName("zone_counts")
    public ZoneCounts zoneCounts;

    @SerializedName("points")
    public List<PointZoneInfo> points;

    @SerializedName(value = "verdict", alternate = {"verdict_summary"})
    public String verdictSummary;

    public static class PointZoneInfo {
        @SerializedName("id")
        public int id;

        @SerializedName("label")
        public String label;

        @SerializedName("rssi_dbm")
        public int rssiDbm;

        @SerializedName("zone")
        public String zone; // strong | fair | weak
    }

    public static class ZoneCounts {
        @SerializedName("strong")
        public int strong;

        @SerializedName("fair")
        public int fair;

        @SerializedName("weak")
        public int weak;
    }
}
