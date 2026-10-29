package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;

public class SpeedTestDto {
    @SerializedName("id")
    public int id;

    @SerializedName("download_mbps")
    public double downloadMbps;

    @SerializedName("upload_mbps")
    public double uploadMbps;

    @SerializedName("ping_ms")
    public Double pingMs;

    @SerializedName("server")
    public String server;

    @SerializedName("network_type")
    public String networkType;

    @SerializedName("ssid")
    public String ssid;

    @SerializedName(value = "tested_at", alternate = {"created_at"})
    public String testedAt;

    public SpeedTestDto() {}

    public SpeedTestDto(double downloadMbps, double uploadMbps, Double pingMs, String server, String networkType, String ssid) {
        this.downloadMbps = downloadMbps;
        this.uploadMbps = uploadMbps;
        this.pingMs = pingMs;
        this.server = server;
        this.networkType = networkType;
        this.ssid = ssid;
    }
}
