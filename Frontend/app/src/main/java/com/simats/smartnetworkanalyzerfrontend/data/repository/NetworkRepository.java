package com.simats.smartnetworkanalyzerfrontend.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.simats.smartnetworkanalyzerfrontend.data.model.*;
import com.simats.smartnetworkanalyzerfrontend.network.ApiClient;
import com.simats.smartnetworkanalyzerfrontend.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkRepository {
    private final ApiService apiService;
    private final ApiClient apiClient;

    public NetworkRepository(Context context) {
        this.apiClient = ApiClient.getInstance(context);
        this.apiService = apiClient.getApiService();
    }

    // --- Dashboard ---
    public LiveData<Resource<DashboardDto>> getDashboard() {
        MutableLiveData<Resource<DashboardDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getDashboard().enqueue(new Callback<DashboardDto>() {
            @Override
            public void onResponse(Call<DashboardDto> call, Response<DashboardDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<DashboardDto> call, Throwable t) {
                result.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });
        return result;
    }

    // --- Wi-Fi Scans ---
    public LiveData<Resource<WifiScanDto>> createWifiScan(WifiScanDto.CreateRequest request) {
        MutableLiveData<Resource<WifiScanDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.createWifiScan(request).enqueue(new Callback<WifiScanDto>() {
            @Override
            public void onResponse(Call<WifiScanDto> call, Response<WifiScanDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<WifiScanDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to save scan: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<PaginatedResponse<WifiScanDto>>> listWifiScans(int page, int pageSize) {
        MutableLiveData<Resource<PaginatedResponse<WifiScanDto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.listWifiScans(page, pageSize).enqueue(new Callback<PaginatedResponse<WifiScanDto>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<WifiScanDto>> call, Response<PaginatedResponse<WifiScanDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<WifiScanDto>> call, Throwable t) {
                result.setValue(Resource.error("Failed to load scans: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<WifiScanDto>> getWifiScan(int id) {
        MutableLiveData<Resource<WifiScanDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getWifiScan(id).enqueue(new Callback<WifiScanDto>() {
            @Override
            public void onResponse(Call<WifiScanDto> call, Response<WifiScanDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<WifiScanDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to load scan: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Boolean>> deleteWifiScan(int id) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.deleteWifiScan(id).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                result.setValue(Resource.error("Failed to delete scan: " + t.getMessage()));
            }
        });
        return result;
    }

    // --- Speed Tests ---
    public LiveData<Resource<SpeedTestDto>> createSpeedTest(SpeedTestDto dto) {
        MutableLiveData<Resource<SpeedTestDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.createSpeedTest(dto).enqueue(new Callback<SpeedTestDto>() {
            @Override
            public void onResponse(Call<SpeedTestDto> call, Response<SpeedTestDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<SpeedTestDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to record speed test: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<SpeedStatsDto>> getSpeedStats() {
        MutableLiveData<Resource<SpeedStatsDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getSpeedStats().enqueue(new Callback<SpeedStatsDto>() {
            @Override
            public void onResponse(Call<SpeedStatsDto> call, Response<SpeedStatsDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<SpeedStatsDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to load speed stats: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<PaginatedResponse<SpeedTestDto>>> listSpeedTests(int page, int pageSize) {
        MutableLiveData<Resource<PaginatedResponse<SpeedTestDto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.listSpeedTests(page, pageSize).enqueue(new Callback<PaginatedResponse<SpeedTestDto>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<SpeedTestDto>> call, Response<PaginatedResponse<SpeedTestDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<SpeedTestDto>> call, Throwable t) {
                result.setValue(Resource.error("Failed to load speed history: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Boolean>> deleteSpeedTest(int id) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.deleteSpeedTest(id).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                result.setValue(Resource.error("Failed to delete record: " + t.getMessage()));
            }
        });
        return result;
    }

    // --- Latency Tests ---
    public LiveData<Resource<LatencyTestDto>> createLatencyTest(LatencyTestDto dto) {
        MutableLiveData<Resource<LatencyTestDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.createLatencyTest(dto).enqueue(new Callback<LatencyTestDto>() {
            @Override
            public void onResponse(Call<LatencyTestDto> call, Response<LatencyTestDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<LatencyTestDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to record latency: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<LatencyStatsDto>> getLatencyStats() {
        MutableLiveData<Resource<LatencyStatsDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getLatencyStats().enqueue(new Callback<LatencyStatsDto>() {
            @Override
            public void onResponse(Call<LatencyStatsDto> call, Response<LatencyStatsDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<LatencyStatsDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to load latency stats: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<PaginatedResponse<LatencyTestDto>>> listLatencyTests(int page, int pageSize) {
        MutableLiveData<Resource<PaginatedResponse<LatencyTestDto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.listLatencyTests(page, pageSize).enqueue(new Callback<PaginatedResponse<LatencyTestDto>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<LatencyTestDto>> call, Response<PaginatedResponse<LatencyTestDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<LatencyTestDto>> call, Throwable t) {
                result.setValue(Resource.error("Failed to load latency history: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Boolean>> deleteLatencyTest(int id) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.deleteLatencyTest(id).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                result.setValue(Resource.error("Failed to delete latency test: " + t.getMessage()));
            }
        });
        return result;
    }

    // --- Signal Maps ---
    public LiveData<Resource<SignalMapDto>> createSignalMap(SignalMapDto dto) {
        MutableLiveData<Resource<SignalMapDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.createSignalMap(dto).enqueue(new Callback<SignalMapDto>() {
            @Override
            public void onResponse(Call<SignalMapDto> call, Response<SignalMapDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<SignalMapDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to create map: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<PaginatedResponse<SignalMapDto>>> listSignalMaps(int page, int pageSize) {
        MutableLiveData<Resource<PaginatedResponse<SignalMapDto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.listSignalMaps(page, pageSize).enqueue(new Callback<PaginatedResponse<SignalMapDto>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<SignalMapDto>> call, Response<PaginatedResponse<SignalMapDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<SignalMapDto>> call, Throwable t) {
                result.setValue(Resource.error("Failed to load maps: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<SignalMapDto>> getSignalMap(int id) {
        MutableLiveData<Resource<SignalMapDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getSignalMap(id).enqueue(new Callback<SignalMapDto>() {
            @Override
            public void onResponse(Call<SignalMapDto> call, Response<SignalMapDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<SignalMapDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to load map: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Boolean>> deleteSignalMap(int id) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.deleteSignalMap(id).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                result.setValue(Resource.error("Failed to delete map: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<SignalPointDto>> addSignalPoint(int mapId, SignalPointDto point) {
        MutableLiveData<Resource<SignalPointDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.addSignalPoint(mapId, point).enqueue(new Callback<SignalPointDto>() {
            @Override
            public void onResponse(Call<SignalPointDto> call, Response<SignalPointDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<SignalPointDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to record point: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Boolean>> deleteSignalPoint(int mapId, int pointId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.deleteSignalPoint(mapId, pointId).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                result.setValue(Resource.error("Failed to delete point: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<SignalMapSummaryDto>> getSignalMapSummary(int mapId) {
        MutableLiveData<Resource<SignalMapSummaryDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getSignalMapSummary(mapId).enqueue(new Callback<SignalMapSummaryDto>() {
            @Override
            public void onResponse(Call<SignalMapSummaryDto> call, Response<SignalMapSummaryDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<SignalMapSummaryDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to load map summary: " + t.getMessage()));
            }
        });
        return result;
    }

    // --- Data Usage ---
    public LiveData<Resource<DataUsageDto>> upsertDataUsage(DataUsageDto dto) {
        MutableLiveData<Resource<DataUsageDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.upsertDataUsage(dto).enqueue(new Callback<DataUsageDto>() {
            @Override
            public void onResponse(Call<DataUsageDto> call, Response<DataUsageDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<DataUsageDto> call, Throwable t) {
                result.setValue(Resource.error("Sync failed: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<DataUsageSummaryDto>> getDataUsageSummary(String period) {
        MutableLiveData<Resource<DataUsageSummaryDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getDataUsageSummary(period).enqueue(new Callback<DataUsageSummaryDto>() {
            @Override
            public void onResponse(Call<DataUsageSummaryDto> call, Response<DataUsageSummaryDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<DataUsageSummaryDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to load usage summary: " + t.getMessage()));
            }
        });
        return result;
    }

    // --- Security Scans ---
    public LiveData<Resource<SecurityScanDto>> createSecurityScan(SecurityScanDto.CreateRequest request) {
        MutableLiveData<Resource<SecurityScanDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.createSecurityScan(request).enqueue(new Callback<SecurityScanDto>() {
            @Override
            public void onResponse(Call<SecurityScanDto> call, Response<SecurityScanDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<SecurityScanDto> call, Throwable t) {
                result.setValue(Resource.error("Security scan failed: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<PaginatedResponse<SecurityScanDto>>> listSecurityScans(int page, int pageSize) {
        MutableLiveData<Resource<PaginatedResponse<SecurityScanDto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.listSecurityScans(page, pageSize).enqueue(new Callback<PaginatedResponse<SecurityScanDto>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<SecurityScanDto>> call, Response<PaginatedResponse<SecurityScanDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<SecurityScanDto>> call, Throwable t) {
                result.setValue(Resource.error("Failed to load security scans: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<SecurityScanDto>> getSecurityScan(int id) {
        MutableLiveData<Resource<SecurityScanDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getSecurityScan(id).enqueue(new Callback<SecurityScanDto>() {
            @Override
            public void onResponse(Call<SecurityScanDto> call, Response<SecurityScanDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<SecurityScanDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to load scan details: " + t.getMessage()));
            }
        });
        return result;
    }

    // --- AI Analysis ---
    public LiveData<Resource<AnalysisDto>> runAnalysis(int days) {
        MutableLiveData<Resource<AnalysisDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.runAnalysis(new AnalysisDto.CreateRequest(days)).enqueue(new Callback<AnalysisDto>() {
            @Override
            public void onResponse(Call<AnalysisDto> call, Response<AnalysisDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<AnalysisDto> call, Throwable t) {
                result.setValue(Resource.error("Analysis failed: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<PaginatedResponse<AnalysisDto>>> listAnalyses(int page, int pageSize) {
        MutableLiveData<Resource<PaginatedResponse<AnalysisDto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.listAnalyses(page, pageSize).enqueue(new Callback<PaginatedResponse<AnalysisDto>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<AnalysisDto>> call, Response<PaginatedResponse<AnalysisDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<AnalysisDto>> call, Throwable t) {
                result.setValue(Resource.error("Failed to load past analyses: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<AnalysisDto>> getAnalysis(int id) {
        MutableLiveData<Resource<AnalysisDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getAnalysis(id).enqueue(new Callback<AnalysisDto>() {
            @Override
            public void onResponse(Call<AnalysisDto> call, Response<AnalysisDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<AnalysisDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to load analysis details: " + t.getMessage()));
            }
        });
        return result;
    }

    // --- Reports ---
    public LiveData<Resource<ReportDto>> createReport(String title, String startDate, String endDate) {
        MutableLiveData<Resource<ReportDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.createReport(new ReportDto.CreateRequest(title, startDate, endDate)).enqueue(new Callback<ReportDto>() {
            @Override
            public void onResponse(Call<ReportDto> call, Response<ReportDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<ReportDto> call, Throwable t) {
                result.setValue(Resource.error("Report generation failed: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<PaginatedResponse<ReportDto>>> listReports(int page, int pageSize) {
        MutableLiveData<Resource<PaginatedResponse<ReportDto>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.listReports(page, pageSize).enqueue(new Callback<PaginatedResponse<ReportDto>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<ReportDto>> call, Response<PaginatedResponse<ReportDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<PaginatedResponse<ReportDto>> call, Throwable t) {
                result.setValue(Resource.error("Failed to load reports: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<ReportDto>> getReport(int id) {
        MutableLiveData<Resource<ReportDto>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.getReport(id).enqueue(new Callback<ReportDto>() {
            @Override
            public void onResponse(Call<ReportDto> call, Response<ReportDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<ReportDto> call, Throwable t) {
                result.setValue(Resource.error("Failed to load report: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Boolean>> deleteReport(int id) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.deleteReport(id).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }
            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                result.setValue(Resource.error("Failed to delete report: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<okhttp3.ResponseBody>> exportReportPdf(int id) {
        MutableLiveData<Resource<okhttp3.ResponseBody>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        apiService.exportReportPdf(id).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Failed to download PDF"));
                }
            }
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getMessage()));
            }
        });
        return result;
    }
}
