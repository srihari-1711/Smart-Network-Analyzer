package com.simats.smartnetworkanalyzerfrontend.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.simats.smartnetworkanalyzerfrontend.MainActivity;
import com.simats.smartnetworkanalyzerfrontend.data.local.TokenManager;
import com.simats.smartnetworkanalyzerfrontend.data.repository.Resource;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentLoginBinding;
import com.simats.smartnetworkanalyzerfrontend.network.ApiClient;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.AuthViewModel;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnLogin.setOnClickListener(v -> performLogin());

        binding.btnGoToSignup.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).navigateTo(new SignupFragment(), true);
            }
        });

        binding.btnForgotPassword.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).navigateTo(new ForgotPasswordFragment(), true);
            }
        });

        binding.btnServerConfig.setOnClickListener(v -> showServerConfigDialog());
    }

    private void performLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        binding.cardError.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        viewModel.login(email, password).observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                binding.tvErrorMessage.setText(resource.getMessage());
                binding.cardError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showServerConfigDialog() {
        TokenManager tokenManager = TokenManager.getInstance(requireContext());
        EditText input = new EditText(requireContext());
        input.setText(tokenManager.getBaseUrl());
        input.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(requireContext())
                .setTitle("Server Base URL")
                .setMessage("Enter the backend API base URL (e.g. http://10.0.2.2:8000/api/v1/ or your PC LAN IP http://192.168.1.X:8000/api/v1/):")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        tokenManager.setBaseUrl(url);
                        ApiClient.resetClient();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
