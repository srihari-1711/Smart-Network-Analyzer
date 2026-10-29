package com.simats.smartnetworkanalyzerfrontend.network;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.simats.smartnetworkanalyzerfrontend.BuildConfig;
import com.simats.smartnetworkanalyzerfrontend.data.local.TokenManager;
import com.simats.smartnetworkanalyzerfrontend.data.model.ErrorResponse;
import com.simats.smartnetworkanalyzerfrontend.data.model.TokenResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static ApiClient instance;
    private final ApiService apiService;
    private final TokenManager tokenManager;
    private final Gson gson = new Gson();

    private ApiClient(Context context) {
        this.tokenManager = TokenManager.getInstance(context);

        // Logging interceptor (Debug only)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(
                BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE
        );

        // Header interceptor for Bearer token
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                String token = tokenManager.getAccessToken();
                if (token != null && !token.isEmpty() && !original.url().encodedPath().contains("/auth/")) {
                    Request.Builder builder = original.newBuilder()
                            .header("Authorization", "Bearer " + token);
                    return chain.proceed(builder.build());
                }
                return chain.proceed(original);
            }
        };

        // 401 Authenticator with Token Rotation
        Authenticator authenticator = new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                if (responseCount(response) >= 2) {
                    return null; // Don't loop infinitely
                }

                String refreshToken = tokenManager.getRefreshToken();
                if (refreshToken == null) {
                    return null;
                }

                synchronized (this) {
                    // Check if token was already updated by another thread
                    String currentAccess = tokenManager.getAccessToken();
                    String reqHeader = response.request().header("Authorization");
                    if (reqHeader != null && !reqHeader.equals("Bearer " + currentAccess)) {
                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + currentAccess)
                                .build();
                    }

                    // Call refresh endpoint synchronously
                    OkHttpClient refreshClient = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .build();

                    Retrofit refreshRetrofit = new Retrofit.Builder()
                            .baseUrl(tokenManager.getBaseUrl())
                            .client(refreshClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ApiService refreshService = refreshRetrofit.create(ApiService.class);
                    Map<String, String> body = new HashMap<>();
                    body.put("refresh_token", refreshToken);

                    try {
                        retrofit2.Response<TokenResponse> refreshResp = refreshService.refreshToken(body).execute();
                        if (refreshResp.isSuccessful() && refreshResp.body() != null) {
                            TokenResponse tokens = refreshResp.body();
                            tokenManager.saveTokens(tokens.getAccessToken(), tokens.getRefreshToken());

                            return response.request().newBuilder()
                                    .header("Authorization", "Bearer " + tokens.getAccessToken())
                                    .build();
                        } else {
                            Log.w(TAG, "Token refresh failed with code: " + refreshResp.code());
                            tokenManager.clearSession();
                            return null;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception during token refresh", e);
                        tokenManager.clearSession();
                        return null;
                    }
                }
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .authenticator(authenticator)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(tokenManager.getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.apiService = retrofit.create(ApiService.class);
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public static synchronized void resetClient() {
        instance = null;
    }

    public ApiService getApiService() {
        return apiService;
    }

    public String parseError(retrofit2.Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ErrorResponse err = gson.fromJson(errorJson, ErrorResponse.class);
                if (err != null && err.getError() != null && err.getError().getMessage() != null) {
                    return err.getError().getMessage();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error body", e);
        }
        return "An error occurred (" + response.code() + ")";
    }
}
