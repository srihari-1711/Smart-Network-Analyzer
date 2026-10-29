package com.simats.smartnetworkanalyzerfrontend.network;

import com.simats.smartnetworkanalyzerfrontend.data.model.*;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;

public interface ApiService {

    // Health
    @GET("health")
    Call<Map<String, Object>> getHealth();

    // Auth
    @POST("auth/signup")
    Call<AuthResponse> signup(@Body Map<String, String> body);

    @POST("auth/login")
    Call<AuthResponse> login(@Body Map<String, String> body);

    @POST("auth/refresh")
    Call<TokenResponse> refreshToken(@Body Map<String, String> body);

    @POST("auth/logout")
    Call<Response<Void>> logout(@Body Map<String, String> body);

    @POST("auth/forgot-password")
    Call<ForgotPasswordResponse> forgotPassword(@Body Map<String, String> body);

    @POST("auth/reset-password")
    Call<MessageResponse> resetPassword(@Body Map<String, String> body);

    // Users
    @GET("users/me")
    Call<User> getProfile();

    @PUT("users/me")
    Call<User> updateProfile(@Body Map<String, String> body);

    @PUT("users/me/password")
    Call<MessageResponse> changePassword(@Body Map<String, String> body);

    @DELETE("users/me")
    Call<Response<Void>> deleteAccount();

    // Dashboard
    @GET("dashboard")
    Call<DashboardDto> getDashboard();

    // Wi-Fi Scans
    @POST("wifi/scans")
    Call<WifiScanDto> createWifiScan(@Body WifiScanDto.CreateRequest body);

    @GET("wifi/scans")
    Call<PaginatedResponse<WifiScanDto>> listWifiScans(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("wifi/scans/{id}")
    Call<WifiScanDto> getWifiScan(@Path("id") int id);

    @DELETE("wifi/scans/{id}")
    Call<Response<Void>> deleteWifiScan(@Path("id") int id);

    // Speed Tests
    @POST("speed-tests")
    Call<SpeedTestDto> createSpeedTest(@Body SpeedTestDto body);

    @GET("speed-tests/stats")
    Call<SpeedStatsDto> getSpeedStats();

    @GET("speed-tests")
    Call<PaginatedResponse<SpeedTestDto>> listSpeedTests(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("speed-tests/{id}")
    Call<SpeedTestDto> getSpeedTest(@Path("id") int id);

    @DELETE("speed-tests/{id}")
    Call<Response<Void>> deleteSpeedTest(@Path("id") int id);

    // Latency Tests
    @POST("latency-tests")
    Call<LatencyTestDto> createLatencyTest(@Body LatencyTestDto body);

    @GET("latency-tests/stats")
    Call<LatencyStatsDto> getLatencyStats();

    @GET("latency-tests")
    Call<PaginatedResponse<LatencyTestDto>> listLatencyTests(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("latency-tests/{id}")
    Call<LatencyTestDto> getLatencyTest(@Path("id") int id);

    @DELETE("latency-tests/{id}")
    Call<Response<Void>> deleteLatencyTest(@Path("id") int id);

    // Signal Maps
    @POST("signal-maps")
    Call<SignalMapDto> createSignalMap(@Body SignalMapDto body);

    @GET("signal-maps")
    Call<PaginatedResponse<SignalMapDto>> listSignalMaps(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("signal-maps/{id}")
    Call<SignalMapDto> getSignalMap(@Path("id") int id);

    @DELETE("signal-maps/{id}")
    Call<Response<Void>> deleteSignalMap(@Path("id") int id);

    @POST("signal-maps/{id}/points")
    Call<SignalPointDto> addSignalPoint(@Path("id") int id, @Body SignalPointDto body);

    @DELETE("signal-maps/{id}/points/{point_id}")
    Call<Response<Void>> deleteSignalPoint(@Path("id") int id, @Path("point_id") int pointId);

    @GET("signal-maps/{id}/summary")
    Call<SignalMapSummaryDto> getSignalMapSummary(@Path("id") int id);

    // Data Usage
    @POST("data-usage")
    Call<DataUsageDto> upsertDataUsage(@Body DataUsageDto body);

    @GET("data-usage")
    Call<PaginatedResponse<DataUsageDto>> listDataUsage(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("data-usage/summary")
    Call<DataUsageSummaryDto> getDataUsageSummary(@Query("period") String period);

    // Security Scans
    @POST("security-scans")
    Call<SecurityScanDto> createSecurityScan(@Body SecurityScanDto.CreateRequest body);

    @GET("security-scans")
    Call<PaginatedResponse<SecurityScanDto>> listSecurityScans(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("security-scans/{id}")
    Call<SecurityScanDto> getSecurityScan(@Path("id") int id);

    // AI Analysis
    @POST("analysis")
    Call<AnalysisDto> runAnalysis(@Body AnalysisDto.CreateRequest body);

    @GET("analysis")
    Call<PaginatedResponse<AnalysisDto>> listAnalyses(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("analysis/{id}")
    Call<AnalysisDto> getAnalysis(@Path("id") int id);

    // Reports
    @POST("reports")
    Call<ReportDto> createReport(@Body ReportDto.CreateRequest body);

    @GET("reports")
    Call<PaginatedResponse<ReportDto>> listReports(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("reports/{id}")
    Call<ReportDto> getReport(@Path("id") int id);

    @DELETE("reports/{id}")
    Call<Response<Void>> deleteReport(@Path("id") int id);

    @Streaming
    @GET("reports/{id}/export")
    Call<ResponseBody> exportReportPdf(@Path("id") int id);
}
