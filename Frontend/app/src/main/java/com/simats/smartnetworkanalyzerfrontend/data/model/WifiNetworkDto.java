package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;

public class WifiNetworkDto {
    @SerializedName("id")
    public int id;

    @SerializedName("ssid")
    public String ssid;

    @SerializedName("bssid")
    public String bssid;

    @SerializedName("signal_dbm")
    public int signalDbm;

    @SerializedName("frequency_mhz")
    public int frequencyMhz;

    @SerializedName("channel")
    public int channel;

    @SerializedName(value = "security", alternate = {"security_type"})
    public String securityType;

    @SerializedName("is_connected")
    public boolean isConnected;

    public WifiNetworkDto() {}

    public WifiNetworkDto(String ssid, String bssid, int signalDbm, int frequencyMhz, int channel, String securityType) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.signalDbm = signalDbm;
        this.frequencyMhz = frequencyMhz;
        this.channel = channel;
        this.securityType = securityType;
        this.isConnected = false;
    }

    public WifiNetworkDto(String ssid, String bssid, int signalDbm, int frequencyMhz, int channel, String securityType, boolean isConnected) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.signalDbm = signalDbm;
        this.frequencyMhz = frequencyMhz;
        this.channel = channel;
        this.securityType = securityType;
        this.isConnected = isConnected;
    }
}
