package com.simats.smartnetworkanalyzerfrontend.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.simats.smartnetworkanalyzerfrontend.data.model.AuthResponse;
import com.simats.smartnetworkanalyzerfrontend.data.model.ForgotPasswordResponse;
import com.simats.smartnetworkanalyzerfrontend.data.model.MessageResponse;
import com.simats.smartnetworkanalyzerfrontend.data.model.User;
import com.simats.smartnetworkanalyzerfrontend.data.repository.AuthRepository;
import com.simats.smartnetworkanalyzerfrontend.data.repository.Resource;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    public LiveData<Resource<AuthResponse>> signup(String name, String email, String password) {
        return authRepository.signup(name, email, password);
    }

    public LiveData<Resource<AuthResponse>> login(String email, String password) {
        return authRepository.login(email, password);
    }

    public LiveData<Resource<ForgotPasswordResponse>> forgotPassword(String email) {
        return authRepository.forgotPassword(email);
    }

    public LiveData<Resource<MessageResponse>> resetPassword(String token, String newPassword) {
        return authRepository.resetPassword(token, newPassword);
    }

    public LiveData<Resource<User>> getProfile() {
        return authRepository.getProfile();
    }

    public LiveData<Resource<User>> updateProfile(String name, String email) {
        return authRepository.updateProfile(name, email);
    }

    public LiveData<Resource<MessageResponse>> changePassword(String currentPassword, String newPassword) {
        return authRepository.changePassword(currentPassword, newPassword);
    }

    public void logout() {
        authRepository.logout();
    }

    public LiveData<Resource<Boolean>> deleteAccount() {
        return authRepository.deleteAccount();
    }
}
