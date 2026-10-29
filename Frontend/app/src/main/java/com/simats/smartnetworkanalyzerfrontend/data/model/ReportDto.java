package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ReportDto {
    @SerializedName("id")
    public int id;

    @SerializedName("title")
    public String title;

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("end_date")
    public String endDate;

    @SerializedName("sections")
    public Map<String, Object> sections;

    @SerializedName("health_score")
    public int healthScore;

    @SerializedName("source")
    public String source;

    @SerializedName("created_at")
    public String createdAt;

    public static class CreateRequest {
        @SerializedName("title")
        public String title;

        @SerializedName("start_date")
        public String startDate;

        @SerializedName("end_date")
        public String endDate;

        public CreateRequest(String title, String startDate, String endDate) {
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
