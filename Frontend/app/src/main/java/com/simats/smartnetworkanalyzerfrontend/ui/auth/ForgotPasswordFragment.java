package com.simats.smartnetworkanalyzerfrontend.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.simats.smartnetworkanalyzerfrontend.data.repository.Resource;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentForgotPasswordBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.AuthViewModel;

public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.btnRequestToken.setOnClickListener(v -> requestResetToken());
        binding.btnSubmitReset.setOnClickListener(v -> submitPasswordReset());
    }

    private void requestResetToken() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        binding.cardError.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRequestToken.setEnabled(false);

        viewModel.forgotPassword(email).observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRequestToken.setEnabled(true);
                Toast.makeText(requireContext(), "Instructions sent!", Toast.LENGTH_SHORT).show();

                if (resource.getData() != null && resource.getData().getResetToken() != null) {
                    String token = resource.getData().getResetToken();
                    binding.tvDevToken.setText(token);
                    binding.cardDevToken.setVisibility(View.VISIBLE);
                    binding.btnAutofillToken.setOnClickListener(v -> {
                        binding.etToken.setText(token);
                    });
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRequestToken.setEnabled(true);
                binding.tvErrorMessage.setText(resource.getMessage());
                binding.cardError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void submitPasswordReset() {
        String token = binding.etToken.getText() != null ? binding.etToken.getText().toString().trim() : "";
        String newPassword = binding.etNewPassword.getText() != null ? binding.etNewPassword.getText().toString().trim() : "";

        if (token.isEmpty()) {
            binding.tilToken.setError("Reset token is required");
            return;
        } else {
            binding.tilToken.setError(null);
        }

        if (newPassword.length() < 8) {
            binding.tilNewPassword.setError("New password must be at least 8 characters");
            return;
        } else {
            binding.tilNewPassword.setError(null);
        }

        binding.cardError.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitReset.setEnabled(false);

        viewModel.resetPassword(token, newPassword).observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Password reset successfully! Please sign in.", Toast.LENGTH_LONG).show();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmitReset.setEnabled(true);
                binding.tvErrorMessage.setText(resource.getMessage());
                binding.cardError.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
