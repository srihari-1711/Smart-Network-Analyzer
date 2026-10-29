package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DataUsageSummaryDto {
    @SerializedName("period")
    public String period;

    @SerializedName("buckets")
    public List<UsageBucket> buckets;

    @SerializedName("totals")
    public UsageTotals totals;

    @SerializedName("avg_daily_bytes")
    public long avgDailyBytes;

    @SerializedName("peak_bucket_label")
    public String peakBucketLabel;

    public static class UsageBucket {
        @SerializedName("label")
        public String label;

        @SerializedName("start")
        public String start;

        @SerializedName("end")
        public String end;

        @SerializedName("wifi_bytes")
        public long wifiBytes;

        @SerializedName("mobile_bytes")
        public long mobileBytes;

        @SerializedName("total_bytes")
        public long totalBytes;
    }

    public static class UsageTotals {
        @SerializedName("wifi_bytes")
        public long wifiBytes;

        @SerializedName("mobile_bytes")
        public long mobileBytes;

        @SerializedName("total_bytes")
        public long totalBytes;
    }
}
