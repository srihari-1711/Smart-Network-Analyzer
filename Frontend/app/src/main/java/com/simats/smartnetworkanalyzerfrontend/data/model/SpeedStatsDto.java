package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;

public class SpeedStatsDto {
    @SerializedName("count")
    public int count;

    @SerializedName(value = "mean_download_mbps", alternate = {"avg_download_mbps"})
    public double meanDownloadMbps;

    @SerializedName(value = "mean_upload_mbps", alternate = {"avg_upload_mbps"})
    public double meanUploadMbps;

    @SerializedName("max_download_mbps")
    public double maxDownloadMbps;

    @SerializedName("min_download_mbps")
    public double minDownloadMbps;

    @SerializedName("max_upload_mbps")
    public double maxUploadMbps;

    @SerializedName("min_upload_mbps")
    public double minUploadMbps;

    @SerializedName("p95_download_mbps")
    public double p95DownloadMbps;

    @SerializedName("p95_upload_mbps")
    public double p95UploadMbps;
}
