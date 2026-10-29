package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;

public class LatencyStatsDto {
    @SerializedName("count")
    public int count;

    @SerializedName(value = "mean_latency_ms", alternate = {"avg_ms"})
    public float meanLatencyMs;

    @SerializedName(value = "min_latency_ms", alternate = {"min_ms"})
    public float minLatencyMs;

    @SerializedName(value = "max_latency_ms", alternate = {"max_ms"})
    public float maxLatencyMs;

    @SerializedName(value = "mean_jitter_ms", alternate = {"avg_jitter_ms"})
    public float meanJitterMs;

    @SerializedName(value = "mean_packet_loss_percent", alternate = {"avg_packet_loss_percent"})
    public float meanPacketLossPercent;

    @SerializedName("rating")
    public String rating;
}
