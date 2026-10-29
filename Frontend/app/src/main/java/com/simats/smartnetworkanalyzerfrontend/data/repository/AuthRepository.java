package com.simats.smartnetworkanalyzerfrontend.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.simats.smartnetworkanalyzerfrontend.data.local.TokenManager;
import com.simats.smartnetworkanalyzerfrontend.data.model.AuthResponse;
import com.simats.smartnetworkanalyzerfrontend.data.model.ForgotPasswordResponse;
import com.simats.smartnetworkanalyzerfrontend.data.model.MessageResponse;
import com.simats.smartnetworkanalyzerfrontend.data.model.User;
import com.simats.smartnetworkanalyzerfrontend.network.ApiClient;
import com.simats.smartnetworkanalyzerfrontend.network.ApiService;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private ApiService apiService;
    private ApiClient apiClient;
    private final Context context;
    private final TokenManager tokenManager;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.tokenManager = TokenManager.getInstance(context);
        refreshClient();
    }

    public void refreshClient() {
        this.apiClient = ApiClient.getInstance(context);
        this.apiService = apiClient.getApiService();
    }

    public LiveData<Resource<AuthResponse>> signup(String name, String email, String password) {
        refreshClient();
        MutableLiveData<Resource<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);

        apiService.signup(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    tokenManager.saveSession(auth.getAccessToken(), auth.getRefreshToken(), auth.getUser());
                    result.setValue(Resource.success(auth));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                result.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<AuthResponse>> login(String email, String password) {
        refreshClient();
        MutableLiveData<Resource<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    tokenManager.saveSession(auth.getAccessToken(), auth.getRefreshToken(), auth.getUser());
                    result.setValue(Resource.success(auth));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                result.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<ForgotPasswordResponse>> forgotPassword(String email) {
        MutableLiveData<Resource<ForgotPasswordResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        apiService.forgotPassword(body).enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                result.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<MessageResponse>> resetPassword(String token, String newPassword) {
        MutableLiveData<Resource<MessageResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("new_password", newPassword);

        apiService.resetPassword(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                result.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<User>> getProfile() {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.getProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveUser(response.body());
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                result.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<User>> updateProfile(String name, String email) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, String> body = new HashMap<>();
        if (name != null) body.put("name", name);
        if (email != null) body.put("email", email);

        apiService.updateProfile(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveUser(response.body());
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                result.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<MessageResponse>> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<Resource<MessageResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        Map<String, String> body = new HashMap<>();
        body.put("current_password", currentPassword);
        body.put("new_password", newPassword);

        apiService.changePassword(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                result.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public void logout() {
        String refresh = tokenManager.getRefreshToken();
        if (refresh != null) {
            Map<String, String> body = new HashMap<>();
            body.put("refresh_token", refresh);
            apiService.logout(body).enqueue(new Callback<Response<Void>>() {
                @Override
                public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {}
                @Override
                public void onFailure(Call<Response<Void>> call, Throwable t) {}
            });
        }
        tokenManager.clearSession();
    }

    public LiveData<Resource<Boolean>> deleteAccount() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        apiService.deleteAccount().enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    tokenManager.clearSession();
                    result.setValue(Resource.success(true));
                } else {
                    result.setValue(Resource.error(apiClient.parseError(response)));
                }
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                result.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return result;
    }
}
