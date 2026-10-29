package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SignalMapDto {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("ssid")
    public String ssid;

    @SerializedName("point_count")
    public int pointCount;

    @SerializedName("avg_rssi_dbm")
    public Double avgRssiDbm;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("points")
    public List<SignalPointDto> points;

    public SignalMapDto() {}

    public SignalMapDto(String name, String description, String ssid) {
        this.name = name;
        this.description = description;
        this.ssid = ssid;
    }
}
