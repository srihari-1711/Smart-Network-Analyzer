package com.simats.smartnetworkanalyzerfrontend.ui.wifi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.smartnetworkanalyzerfrontend.data.model.WifiNetworkDto;
import com.simats.smartnetworkanalyzerfrontend.data.model.WifiScanDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentWifiHistoryBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

public class WifiHistoryFragment extends Fragment implements WifiScanHistoryAdapter.OnScanActionListener {

    private FragmentWifiHistoryBinding binding;
    private MainViewModel viewModel;
    private WifiScanHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWifiHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new WifiScanHistoryAdapter(this);
        binding.rvScans.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvScans.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.swipeRefresh.setOnRefreshListener(this::loadScans);

        loadScans();
    }

    private void loadScans() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.listWifiScans(1, 50).observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null && resource.data.items != null) {
                        adapter.setItems(resource.data.items);
                        binding.tvEmpty.setVisibility(resource.data.items.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), "Failed to load scans: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    break;
            }
        });
    }

    @Override
    public void onDelete(WifiScanDto scan) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Scan")
                .setMessage("Are you sure you want to delete scan #" + scan.id + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteWifiScan(scan.id).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                            Toast.makeText(requireContext(), "Scan deleted", Toast.LENGTH_SHORT).show();
                            loadScans();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSelect(WifiScanDto scan) {
        if (scan.networks == null || scan.networks.isEmpty()) {
            Toast.makeText(requireContext(), "No network details recorded for this scan", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (WifiNetworkDto net : scan.networks) {
            sb.append("• ").append(net.ssid != null && !net.ssid.isEmpty() ? net.ssid : "(Hidden)")
                    .append(" (").append(net.signalDbm).append(" dBm, ")
                    .append(net.securityType).append(")\n");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(scan.locationTag != null ? scan.locationTag : "Scan Details")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
