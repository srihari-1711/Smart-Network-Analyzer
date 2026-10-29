package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardDto {
    @SerializedName("latest_speed_test")
    public SpeedTestDto latestSpeedTest;

    @SerializedName("latest_latency_test")
    public LatencyTestDto latestLatencyTest;

    @SerializedName(value = "latest_wifi_scan", alternate = {"latest_wifi_scan_summary"})
    public WifiScanDto latestWifiScan;

    @SerializedName("current_signal_dbm")
    public Integer currentSignalDbm;

    @SerializedName("latest_analysis")
    public LatestAnalysis latestAnalysis;

    @SerializedName(value = "counts", alternate = {"activity_counts"})
    public DashboardCounts activityCounts;

    public static class LatestAnalysis {
        @SerializedName("id")
        public int id;

        @SerializedName("health_score")
        public int healthScore;

        @SerializedName("summary")
        public String summary;

        @SerializedName(value = "engine", alternate = {"source"})
        public String engine;

        @SerializedName("created_at")
        public String createdAt;
    }

    public static class DashboardCounts {
        @SerializedName("speed_tests")
        public int speedTests;

        @SerializedName("latency_tests")
        public int latencyTests;

        @SerializedName("wifi_scans")
        public int wifiScans;

        @SerializedName("signal_maps")
        public int signalMaps;

        @SerializedName("reports")
        public int reports;
    }
}
