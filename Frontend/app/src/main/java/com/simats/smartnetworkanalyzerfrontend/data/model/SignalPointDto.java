package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;

public class SignalPointDto {
    @SerializedName("id")
    public int id;

    @SerializedName(value = "label", alternate = {"location_label", "location_name"})
    public String locationName;

    @SerializedName(value = "x", alternate = {"x_coord"})
    public float xCoord;

    @SerializedName(value = "y", alternate = {"y_coord"})
    public float yCoord;

    @SerializedName(value = "rssi_dbm", alternate = {"signal_dbm"})
    public int signalDbm;

    @SerializedName("ssid")
    public String ssid;

    @SerializedName("recorded_at")
    public String recordedAt;

    public SignalPointDto() {}

    public SignalPointDto(String locationName, float xCoord, float yCoord, int signalDbm, String ssid) {
        this.locationName = locationName;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.signalDbm = signalDbm;
        this.ssid = ssid;
    }
}
