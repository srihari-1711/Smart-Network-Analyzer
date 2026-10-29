package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WifiScanDto {
    @SerializedName("id")
    public int id;

    @SerializedName("scanned_at")
    public String scannedAt;

    @SerializedName(value = "location_label", alternate = {"location_tag"})
    public String locationTag;

    @SerializedName("network_count")
    public int networkCount;

    @SerializedName("strongest_signal_dbm")
    public Integer strongestSignalDbm;

    @SerializedName("connected_ssid")
    public String connectedSsid;

    @SerializedName("networks")
    public List<WifiNetworkDto> networks;

    public static class CreateRequest {
        @SerializedName("scanned_at")
        public String scannedAt;

        @SerializedName(value = "location_label", alternate = {"location_tag"})
        public String locationTag;

        @SerializedName("networks")
        public List<WifiNetworkDto> networks;

        public CreateRequest() {}

        public CreateRequest(String locationTag, List<WifiNetworkDto> networks) {
            this.locationTag = locationTag;
            this.networks = networks;
        }
    }
}
