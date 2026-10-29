package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SecurityScanDto {
    @SerializedName("id")
    public int id;

    @SerializedName(value = "ssid", alternate = {"network_ssid"})
    public String networkSsid;

    @SerializedName("bssid")
    public String bssid;

    @SerializedName("security_type")
    public String securityType;

    @SerializedName("signal_dbm")
    public Integer signalDbm;

    @SerializedName("gateway_ip")
    public String gatewayIp;

    @SerializedName("local_ip")
    public String localIp;

    @SerializedName("risk_level")
    public String riskLevel;

    @SerializedName("security_score")
    public int securityScore;

    @SerializedName("issues")
    public List<IssueDto> issues;

    @SerializedName("recommendations")
    public List<RecommendationDto> recommendations;

    @SerializedName("devices")
    public List<DeviceDto> devices;

    @SerializedName("scanned_at")
    public String scannedAt;

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

    public static class IssueDto {
        @SerializedName("title")
        public String title;

        @SerializedName("severity")
        public String severity;

        @SerializedName("description")
        public String description;
    }

    public static class DeviceDto {
        @SerializedName(value = "ip", alternate = {"ip_address"})
        public String ipAddress;

        @SerializedName(value = "mac", alternate = {"mac_address"})
        public String macAddress;

        @SerializedName("hostname")
        public String hostname;

        @SerializedName("device_type")
        public String deviceType;
    }

    public static class CreateRequest {
        @SerializedName(value = "ssid", alternate = {"network_ssid"})
        public String networkSsid;

        @SerializedName("bssid")
        public String bssid;

        @SerializedName("security_type")
        public String securityType;

        @SerializedName("gateway_ip")
        public String gatewayIp;

        @SerializedName("open_ports")
        public List<Integer> openPorts;

        @SerializedName("devices")
        public List<DeviceDto> devices;
    }
}
