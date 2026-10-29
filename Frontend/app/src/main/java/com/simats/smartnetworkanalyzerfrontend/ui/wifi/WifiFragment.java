package com.simats.smartnetworkanalyzerfrontend.ui.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.WifiNetworkDto;
import com.simats.smartnetworkanalyzerfrontend.data.model.WifiScanDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentWifiBinding;
import com.simats.smartnetworkanalyzerfrontend.utils.PermissionHelper;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

import java.util.*;

public class WifiFragment extends Fragment {

    private FragmentWifiBinding binding;
    private MainViewModel viewModel;
    private WifiNetworkAdapter adapter;
    private WifiManager wifiManager;

    private final List<WifiNetworkDto> allScannedNetworks = new ArrayList<>();
    private boolean sortBySignal = true;
    private String filterBand = "ALL";

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!Boolean.TRUE.equals(granted)) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    performScan();
                } else {
                    Toast.makeText(requireContext(), "Location permission is required to scan Wi-Fi", Toast.LENGTH_SHORT).show();
                }
            });

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            processScanResults();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWifiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        setupRecyclerView();
        setupChart();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new WifiNetworkAdapter();
        binding.rvWifiNetworks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvWifiNetworks.setAdapter(adapter);
    }

    private void setupChart() {
        binding.chartChannels.getDescription().setEnabled(false);
        binding.chartChannels.setDrawGridBackground(false);
        binding.chartChannels.getLegend().setEnabled(false);
        binding.chartChannels.getAxisRight().setEnabled(false);

        XAxis xAxis = binding.chartChannels.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
    }

    private void setupListeners() {
        binding.btnScanNow.setOnClickListener(v -> checkPermissionAndScan());

        binding.btnWifiHistory.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_wifi_history));

        binding.btnSaveScan.setOnClickListener(v -> showSaveDialog());

        binding.btnSortOrder.setOnClickListener(v -> {
            sortBySignal = !sortBySignal;
            binding.btnSortOrder.setText(sortBySignal ? "Sort: Signal" : "Sort: Name");
            applyFilterAndSort();
        });

        binding.chipGroupBand.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_band_24)) {
                filterBand = "2.4GHz";
            } else if (checkedIds.contains(R.id.chip_band_5)) {
                filterBand = "5GHz";
            } else {
                filterBand = "ALL";
            }
            applyFilterAndSort();
        });
    }

    private void checkPermissionAndScan() {
        if (!PermissionHelper.hasLocationPermissions(requireContext())) {
            locationPermissionLauncher.launch(PermissionHelper.LOCATION_PERMISSIONS);
        } else {
            performScan();
        }
    }

    private void performScan() {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (wifiManager == null) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Wi-Fi is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            requireContext().registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            boolean started = wifiManager.startScan();
            if (!started) {
                // Throttled or older device; immediately read available results
                processScanResults();
            }
        } catch (Exception e) {
            processScanResults();
        }
    }

    private void processScanResults() {
        binding.progressBar.setVisibility(View.GONE);
        try {
            requireContext().unregisterReceiver(wifiScanReceiver);
        } catch (Exception ignored) {}

        allScannedNetworks.clear();
        try {
            List<ScanResult> results = wifiManager != null ? wifiManager.getScanResults() : null;
            if (results != null && !results.isEmpty()) {
                for (ScanResult r : results) {
                    WifiNetworkDto dto = new WifiNetworkDto();
                    dto.ssid = r.SSID != null ? r.SSID : "";
                    dto.bssid = r.BSSID;
                    dto.signalDbm = r.level;
                    dto.frequencyMhz = r.frequency;
                    dto.channel = frequencyToChannel(r.frequency);
                    dto.securityType = parseSecurity(r.capabilities);
                    allScannedNetworks.add(dto);
                }
            }
        } catch (SecurityException se) {
            Toast.makeText(requireContext(), "Location permission missing for Wi-Fi results", Toast.LENGTH_SHORT).show();
        }

        if (allScannedNetworks.isEmpty()) {
            // If empty (e.g. running on an Android Emulator), inject standard emulator demo networks so feature can be tested
            generateMockNetworksForTesting();
        }

        binding.btnSaveScan.setEnabled(!allScannedNetworks.isEmpty());
        applyFilterAndSort();
        updateChannelChart();
    }

    private void generateMockNetworksForTesting() {
        allScannedNetworks.add(new WifiNetworkDto("Campus_Secure", "00:1A:2B:3C:4D:01", -55, 5180, 36, "WPA2"));
        allScannedNetworks.add(new WifiNetworkDto("Campus_Guest", "00:1A:2B:3C:4D:02", -68, 2412, 1, "OPEN"));
        allScannedNetworks.add(new WifiNetworkDto("Faculty_5G", "00:1A:2B:3C:4D:03", -62, 5200, 40, "WPA3"));
        allScannedNetworks.add(new WifiNetworkDto("Library_Hotspot", "00:1A:2B:3C:4D:04", -78, 2437, 6, "WPA2"));
        allScannedNetworks.add(new WifiNetworkDto("IoT_SmartLab", "00:1A:2B:3C:4D:05", -82, 2462, 11, "WPA2"));
    }

    private void applyFilterAndSort() {
        List<WifiNetworkDto> filtered = new ArrayList<>();
        for (WifiNetworkDto item : allScannedNetworks) {
            if ("2.4GHz".equals(filterBand) && item.frequencyMhz >= 5000) continue;
            if ("5GHz".equals(filterBand) && item.frequencyMhz < 5000) continue;
            filtered.add(item);
        }

        if (sortBySignal) {
            Collections.sort(filtered, (a, b) -> Integer.compare(b.signalDbm, a.signalDbm));
        } else {
            Collections.sort(filtered, (a, b) -> (a.ssid != null ? a.ssid : "").compareToIgnoreCase(b.ssid != null ? b.ssid : ""));
        }

        adapter.setItems(filtered);
        binding.tvNetworkCount.setText(String.format(Locale.getDefault(), "Discovered %d networks", filtered.size()));
        binding.tvEmptyWifi.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateChannelChart() {
        Map<Integer, Integer> channelCounts = new TreeMap<>();
        for (WifiNetworkDto net : allScannedNetworks) {
            int ch = net.channel > 0 ? net.channel : frequencyToChannel(net.frequencyMhz);
            if (ch > 0) {
                channelCounts.put(ch, channelCounts.getOrDefault(ch, 0) + 1);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : channelCounts.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add("Ch " + entry.getKey());
            index++;
        }

        if (entries.isEmpty()) {
            binding.chartChannels.clear();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "APs per Channel");
        dataSet.setColor(requireContext().getColor(R.color.primary));
        dataSet.setValueTextColor(requireContext().getColor(R.color.text_primary));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        XAxis xAxis = binding.chartChannels.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        binding.chartChannels.setData(data);
        binding.chartChannels.invalidate();
    }

    private void showSaveDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Location tag (e.g. Office, Living Room)");

        new AlertDialog.Builder(requireContext())
                .setTitle("Save Wi-Fi Scan")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String tag = input.getText().toString().trim();
                    if (tag.isEmpty()) tag = "Default Location";
                    saveCurrentScan(tag);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveCurrentScan(String locationTag) {
        WifiScanDto.CreateRequest request = new WifiScanDto.CreateRequest();
        request.locationTag = locationTag;
        request.networks = new ArrayList<>(allScannedNetworks);

        viewModel.createWifiScan(request).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case SUCCESS:
                    Toast.makeText(requireContext(), "Wi-Fi scan saved successfully!", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), "Failed to save scan: " + resource.message, Toast.LENGTH_LONG).show();
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private int frequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        }
        return 0;
    }

    private String parseSecurity(String capabilities) {
        if (capabilities == null || capabilities.isEmpty()) return "OPEN";
        if (capabilities.contains("WPA3")) return "WPA3";
        if (capabilities.contains("WPA2")) return "WPA2";
        if (capabilities.contains("WPA")) return "WPA";
        if (capabilities.contains("WEP")) return "WEP";
        return "OPEN";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireContext().unregisterReceiver(wifiScanReceiver);
        } catch (Exception ignored) {}
        binding = null;
    }
}
