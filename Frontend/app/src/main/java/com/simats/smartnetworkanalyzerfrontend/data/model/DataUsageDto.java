package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;

public class DataUsageDto {
    @SerializedName("id")
    public int id;

    @SerializedName("period_start")
    public String periodStart;

    @SerializedName("period_end")
    public String periodEnd;

    @SerializedName(value = "wifi_rx_bytes", alternate = {"rx_bytes_wifi"})
    public long rxBytesWifi;

    @SerializedName(value = "wifi_tx_bytes", alternate = {"tx_bytes_wifi"})
    public long txBytesWifi;

    @SerializedName(value = "mobile_rx_bytes", alternate = {"rx_bytes_mobile"})
    public long rxBytesMobile;

    @SerializedName(value = "mobile_tx_bytes", alternate = {"tx_bytes_mobile"})
    public long txBytesMobile;

    @SerializedName("created_at")
    public String createdAt;

    public DataUsageDto() {}

    public DataUsageDto(String periodStart, String periodEnd, long rxBytesWifi, long txBytesWifi, long rxBytesMobile, long txBytesMobile) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.rxBytesWifi = rxBytesWifi;
        this.txBytesWifi = txBytesWifi;
        this.rxBytesMobile = rxBytesMobile;
        this.txBytesMobile = txBytesMobile;
    }
}
