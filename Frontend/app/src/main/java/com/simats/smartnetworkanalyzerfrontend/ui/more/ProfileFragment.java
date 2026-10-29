package com.simats.smartnetworkanalyzerfrontend.ui.more;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.simats.smartnetworkanalyzerfrontend.data.model.User;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentProfileBinding;
import com.simats.smartnetworkanalyzerfrontend.data.local.TokenManager;
import com.simats.smartnetworkanalyzerfrontend.ui.auth.AuthActivity;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.AuthViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private TokenManager tokenManager;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        tokenManager = TokenManager.getInstance(requireContext());

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.etServerUrl.setText(tokenManager.getBaseUrl());

        setupListeners();
        loadProfile();
    }

    private void setupListeners() {
        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        binding.btnSaveServerUrl.setOnClickListener(v -> {
            String url = binding.etServerUrl.getText() != null ? binding.etServerUrl.getText().toString().trim() : "";
            if (!url.endsWith("/")) url += "/";
            tokenManager.setBaseUrl(url);
            Toast.makeText(requireContext(), "Server URL updated to " + url, Toast.LENGTH_SHORT).show();
        });

        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        authViewModel.logout();
                        redirectToAuth();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("This will permanently delete your account and all associated network scans, speed tests, maps, and reports. This action cannot be undone.")
                    .setPositiveButton("Delete Permanently", (dialog, which) -> {
                        authViewModel.deleteAccount().observe(getViewLifecycleOwner(), resource -> {
                            if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                                Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                redirectToAuth();
                            } else if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.ERROR) {
                                Toast.makeText(requireContext(), "Failed to delete account: " + resource.message, Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadProfile() {
        authViewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null) {
                currentUser = resource.data;
                binding.tvProfileName.setText(currentUser.name);
                binding.tvProfileEmail.setText(currentUser.email);
                if (currentUser.createdAt != null) {
                    binding.tvProfileCreated.setText("Member since: " + currentUser.createdAt);
                }
            }
        });
    }

    private void showEditProfileDialog() {
        if (currentUser == null) return;

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        EditText etName = new EditText(requireContext());
        etName.setText(currentUser.name);
        etName.setHint("Full Name");
        layout.addView(etName);

        EditText etEmail = new EditText(requireContext());
        etEmail.setText(currentUser.email);
        etEmail.setHint("Email Address");
        layout.addView(etEmail);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    if (name.isEmpty() || email.isEmpty()) {
                        Toast.makeText(requireContext(), "Name and email cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    authViewModel.updateProfile(name, email).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null && resource.data != null) {
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                            loadProfile();
                        } else if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.ERROR) {
                            Toast.makeText(requireContext(), "Update failed: " + resource.message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        EditText etCurrent = new EditText(requireContext());
        etCurrent.setHint("Current Password");
        etCurrent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etCurrent);

        EditText etNew = new EditText(requireContext());
        etNew.setHint("New Password (min 8 characters)");
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNew);

        new AlertDialog.Builder(requireContext())
                .setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("Update", (dialog, which) -> {
                    String currentPass = etCurrent.getText().toString();
                    String newPass = etNew.getText().toString();

                    if (currentPass.isEmpty() || newPass.length() < 8) {
                        Toast.makeText(requireContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    authViewModel.changePassword(currentPass, newPass).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                            Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        } else if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.ERROR) {
                            Toast.makeText(requireContext(), "Error: " + resource.message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void redirectToAuth() {
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
