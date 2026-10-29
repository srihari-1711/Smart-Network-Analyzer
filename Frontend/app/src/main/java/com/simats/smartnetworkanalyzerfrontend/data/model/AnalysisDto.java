package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class AnalysisDto {
    @SerializedName("id")
    public int id;

    @SerializedName(value = "engine", alternate = {"source"})
    public String engine;

    @SerializedName("health_score")
    public int healthScore;

    @SerializedName("summary")
    public String summary;

    @SerializedName(value = "window_days", alternate = {"days"})
    public int windowDays;

    @SerializedName("issues")
    public List<IssueDto> issues;

    @SerializedName("recommendations")
    public List<RecommendationDto> recommendations;

    public static class RecommendationDto {
        @SerializedName("priority")
        public String priority;

        @SerializedName("title")
        public String title;

        @SerializedName("description")
        public String description;

        @Override
        public String toString() {
            if (title != null && !title.isEmpty()) {
                if (description != null && !description.isEmpty()) {
                    return title + ": " + description;
                }
                return title;
            }
            return description != null ? description : "";
        }
    }

    @SerializedName("metrics_snapshot")
    public Map<String, Object> metricsSnapshot;

    @SerializedName("created_at")
    public String createdAt;

    public static class IssueDto {
        @SerializedName("severity")
        public String severity;

        @SerializedName("category")
        public String category;

        @SerializedName("title")
        public String title;

        @SerializedName("description")
        public String description;
    }

    public static class CreateRequest {
        @SerializedName("days")
        public int days;

        public CreateRequest(int days) {
            this.days = days;
        }
    }
}
