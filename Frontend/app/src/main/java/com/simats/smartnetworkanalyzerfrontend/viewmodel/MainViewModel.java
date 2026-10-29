package com.simats.smartnetworkanalyzerfrontend.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.simats.smartnetworkanalyzerfrontend.data.model.*;
import com.simats.smartnetworkanalyzerfrontend.data.repository.NetworkRepository;
import com.simats.smartnetworkanalyzerfrontend.data.repository.Resource;

public class MainViewModel extends AndroidViewModel {
    private final NetworkRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.repository = new NetworkRepository(application);
    }

    // Dashboard
    public LiveData<Resource<DashboardDto>> getDashboard() {
        return repository.getDashboard();
    }

    // Wi-Fi Scans
    public LiveData<Resource<WifiScanDto>> createWifiScan(WifiScanDto.CreateRequest request) {
        return repository.createWifiScan(request);
    }

    public LiveData<Resource<PaginatedResponse<WifiScanDto>>> listWifiScans(int page, int pageSize) {
        return repository.listWifiScans(page, pageSize);
    }

    public LiveData<Resource<WifiScanDto>> getWifiScan(int id) {
        return repository.getWifiScan(id);
    }

    public LiveData<Resource<Boolean>> deleteWifiScan(int id) {
        return repository.deleteWifiScan(id);
    }

    // Speed Tests
    public LiveData<Resource<SpeedTestDto>> createSpeedTest(SpeedTestDto dto) {
        return repository.createSpeedTest(dto);
    }

    public LiveData<Resource<SpeedStatsDto>> getSpeedStats() {
        return repository.getSpeedStats();
    }

    public LiveData<Resource<PaginatedResponse<SpeedTestDto>>> listSpeedTests(int page, int pageSize) {
        return repository.listSpeedTests(page, pageSize);
    }

    public LiveData<Resource<Boolean>> deleteSpeedTest(int id) {
        return repository.deleteSpeedTest(id);
    }

    // Latency Tests
    public LiveData<Resource<LatencyTestDto>> createLatencyTest(LatencyTestDto dto) {
        return repository.createLatencyTest(dto);
    }

    public LiveData<Resource<LatencyStatsDto>> getLatencyStats() {
        return repository.getLatencyStats();
    }

    public LiveData<Resource<PaginatedResponse<LatencyTestDto>>> listLatencyTests(int page, int pageSize) {
        return repository.listLatencyTests(page, pageSize);
    }

    public LiveData<Resource<Boolean>> deleteLatencyTest(int id) {
        return repository.deleteLatencyTest(id);
    }

    // Signal Maps
    public LiveData<Resource<SignalMapDto>> createSignalMap(SignalMapDto dto) {
        return repository.createSignalMap(dto);
    }

    public LiveData<Resource<PaginatedResponse<SignalMapDto>>> listSignalMaps(int page, int pageSize) {
        return repository.listSignalMaps(page, pageSize);
    }

    public LiveData<Resource<SignalMapDto>> getSignalMap(int id) {
        return repository.getSignalMap(id);
    }

    public LiveData<Resource<Boolean>> deleteSignalMap(int id) {
        return repository.deleteSignalMap(id);
    }

    public LiveData<Resource<SignalPointDto>> addSignalPoint(int mapId, SignalPointDto point) {
        return repository.addSignalPoint(mapId, point);
    }

    public LiveData<Resource<Boolean>> deleteSignalPoint(int mapId, int pointId) {
        return repository.deleteSignalPoint(mapId, pointId);
    }

    public LiveData<Resource<SignalMapSummaryDto>> getSignalMapSummary(int mapId) {
        return repository.getSignalMapSummary(mapId);
    }

    // Data Usage
    public LiveData<Resource<DataUsageDto>> upsertDataUsage(DataUsageDto dto) {
        return repository.upsertDataUsage(dto);
    }

    public LiveData<Resource<DataUsageSummaryDto>> getDataUsageSummary(String period) {
        return repository.getDataUsageSummary(period);
    }

    // Security Scans
    public LiveData<Resource<SecurityScanDto>> createSecurityScan(SecurityScanDto.CreateRequest request) {
        return repository.createSecurityScan(request);
    }

    public LiveData<Resource<PaginatedResponse<SecurityScanDto>>> listSecurityScans(int page, int pageSize) {
        return repository.listSecurityScans(page, pageSize);
    }

    public LiveData<Resource<SecurityScanDto>> getSecurityScan(int id) {
        return repository.getSecurityScan(id);
    }

    // AI Analysis
    public LiveData<Resource<AnalysisDto>> runAnalysis(int days) {
        return repository.runAnalysis(days);
    }

    public LiveData<Resource<PaginatedResponse<AnalysisDto>>> listAnalyses(int page, int pageSize) {
        return repository.listAnalyses(page, pageSize);
    }

    public LiveData<Resource<AnalysisDto>> getAnalysis(int id) {
        return repository.getAnalysis(id);
    }

    // Reports
    public LiveData<Resource<ReportDto>> createReport(String title, String startDate, String endDate) {
        return repository.createReport(title, startDate, endDate);
    }

    public LiveData<Resource<PaginatedResponse<ReportDto>>> listReports(int page, int pageSize) {
        return repository.listReports(page, pageSize);
    }

    public LiveData<Resource<ReportDto>> getReport(int id) {
        return repository.getReport(id);
    }

    public LiveData<Resource<Boolean>> deleteReport(int id) {
        return repository.deleteReport(id);
    }

    public LiveData<Resource<okhttp3.ResponseBody>> exportReportPdf(int id) {
        return repository.exportReportPdf(id);
    }
}
