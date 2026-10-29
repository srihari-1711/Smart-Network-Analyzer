package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;

public class LatencyTestDto {
    @SerializedName("id")
    public int id;

    @SerializedName("target_host")
    public String targetHost;

    @SerializedName(value = "latency_ms", alternate = {"avg_ms"})
    public float latencyMs;

    @SerializedName(value = "min_latency_ms", alternate = {"min_ms"})
    public float minLatencyMs;

    @SerializedName(value = "max_latency_ms", alternate = {"max_ms"})
    public float maxLatencyMs;

    @SerializedName("jitter_ms")
    public float jitterMs;

    @SerializedName("packet_loss_percent")
    public float packetLossPercent;

    @SerializedName("target_ip")
    public String targetIp;

    @SerializedName(value = "tested_at", alternate = {"created_at"})
    public String testedAt;
}
