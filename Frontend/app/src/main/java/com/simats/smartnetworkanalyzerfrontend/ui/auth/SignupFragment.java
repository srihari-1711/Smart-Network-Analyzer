package com.simats.smartnetworkanalyzerfrontend.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.simats.smartnetworkanalyzerfrontend.MainActivity;
import com.simats.smartnetworkanalyzerfrontend.data.repository.Resource;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentSignupBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.AuthViewModel;

public class SignupFragment extends Fragment {
    private FragmentSignupBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnSignup.setOnClickListener(v -> performSignup());

        binding.btnGoToLogin.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void performSignup() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.tilName.setError("Full name is required");
            return;
        } else {
            binding.tilName.setError(null);
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email address is required");
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (password.length() < 8) {
            binding.tilPassword.setError("Password must be at least 8 characters");
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        binding.cardError.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignup.setEnabled(false);

        viewModel.signup(name, email, password).observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSignup.setEnabled(true);
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
