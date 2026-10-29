package com.simats.smartnetworkanalyzerfrontend.ui.more;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.SecurityScanDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentSecurityBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SecurityFragment extends Fragment implements SecurityScanAdapter.OnScanClickListener {

    private FragmentSecurityBinding binding;
    private MainViewModel viewModel;
    private SecurityScanAdapter adapter;
    private WifiManager wifiManager;

    private String currentSsid = "Direct Gateway";
    private String currentBssid = "00:00:00:00:00:00";
    private String currentSecurity = "WPA2";
    private String currentGateway = "192.168.1.1";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecurityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        adapter = new SecurityScanAdapter(this);
        binding.rvSecurityScans.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSecurityScans.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.swipeRefresh.setOnRefreshListener(this::loadScans);

        binding.btnRunAudit.setOnClickListener(v -> runAudit());

        detectCurrentNetwork();
        loadScans();
    }

    private void detectCurrentNetwork() {
        if (wifiManager != null) {
            try {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null && info.getSSID() != null && !info.getSSID().contains("unknown")) {
                    currentSsid = info.getSSID().replace("\"", "");
                    currentBssid = info.getBSSID() != null ? info.getBSSID() : "00:11:22:33:44:55";
                }

                DhcpInfo dhcp = wifiManager.getDhcpInfo();
                if (dhcp != null && dhcp.gateway != 0) {
                    currentGateway = Formatter.formatIpAddress(dhcp.gateway);
                    String localIp = Formatter.formatIpAddress(dhcp.ipAddress);
                    binding.tvConnectedDetails.setText(String.format(Locale.getDefault(), "IP: %s  •  Gateway: %s  •  Security: WPA2", localIp, currentGateway));
                }
            } catch (Exception ignored) {}
        }
        binding.tvConnectedSsid.setText(String.format(Locale.getDefault(), "Connected: %s", currentSsid));
    }

    private void runAudit() {
        binding.btnRunAudit.setEnabled(false);
        binding.swipeRefresh.setRefreshing(true);

        SecurityScanDto.CreateRequest req = new SecurityScanDto.CreateRequest();
        req.networkSsid = currentSsid;
        req.bssid = currentBssid;
        req.securityType = currentSecurity;
        req.gatewayIp = currentGateway;
        req.openPorts = new ArrayList<>();
        req.devices = new ArrayList<>();

        // Add detected gateway & self device
        SecurityScanDto.DeviceDto dev1 = new SecurityScanDto.DeviceDto();
        dev1.ipAddress = currentGateway;
        dev1.macAddress = currentBssid;
        dev1.hostname = "Router-Gateway";
        dev1.deviceType = "ROUTER";
        req.devices.add(dev1);

        SecurityScanDto.DeviceDto dev2 = new SecurityScanDto.DeviceDto();
        dev2.ipAddress = "192.168.1.100";
        dev2.macAddress = "AA:BB:CC:DD:EE:01";
        dev2.hostname = "Android-Device";
        dev2.deviceType = "MOBILE";
        req.devices.add(dev2);

        viewModel.createSecurityScan(req).observe(getViewLifecycleOwner(), resource -> {
            binding.btnRunAudit.setEnabled(true);
            binding.swipeRefresh.setRefreshing(false);
            if (resource != null && resource.data != null) {
                Toast.makeText(requireContext(), "Security Audit Completed!", Toast.LENGTH_SHORT).show();
                displayScanResults(resource.data);
                loadScans();
            } else if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Audit failed: " + resource.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayScanResults(@NonNull SecurityScanDto scan) {
        binding.cardLatestAudit.setVisibility(View.VISIBLE);
        binding.tvAuditScore.setText(String.format(Locale.getDefault(), "%d / 100", scan.securityScore));

        String risk = scan.riskLevel != null ? scan.riskLevel.toUpperCase() : "LOW";
        binding.tvAuditRisk.setText(risk);

        int riskColor;
        if ("CRITICAL".equals(risk) || "HIGH".equals(risk)) {
            riskColor = ContextCompat.getColor(requireContext(), R.color.danger);
            binding.tvAuditRisk.setBackgroundResource(R.drawable.bg_badge_weak);
        } else if ("MEDIUM".equals(risk)) {
            riskColor = ContextCompat.getColor(requireContext(), R.color.warning);
            binding.tvAuditRisk.setBackgroundResource(R.drawable.bg_badge_fair);
        } else {
            riskColor = ContextCompat.getColor(requireContext(), R.color.success);
            binding.tvAuditRisk.setBackgroundResource(R.drawable.bg_badge_strong);
        }
        binding.tvAuditRisk.setTextColor(riskColor);

        // Issues
        if (scan.issues != null && !scan.issues.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (SecurityScanDto.IssueDto issue : scan.issues) {
                sb.append("• [").append(issue.severity != null ? issue.severity.toUpperCase() : "INFO").append("] ")
                        .append(issue.title != null ? issue.title : "")
                        .append(": ").append(issue.description != null ? issue.description : "").append("\n");
            }
            binding.tvAuditIssues.setText(sb.toString().trim());
        } else {
            binding.tvAuditIssues.setText("No vulnerabilities or insecure protocols identified.");
        }

        // Recommendations
        if (scan.recommendations != null && !scan.recommendations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (SecurityScanDto.RecommendationDto rec : scan.recommendations) {
                sb.append("• ").append(rec.toString()).append("\n");
            }
            binding.tvAuditRecommendations.setText(sb.toString().trim());
        } else {
            binding.tvAuditRecommendations.setText("• Network configuration meets standard security baselines.");
        }
    }

    private void loadScans() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.listSecurityScans(1, 20).observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            if (resource != null && resource.data != null && resource.data.items != null) {
                List<SecurityScanDto> items = resource.data.items;
                adapter.setItems(items);
                binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                if (!items.isEmpty() && binding.cardLatestAudit.getVisibility() == View.GONE) {
                    displayScanResults(items.get(0));
                }
            }
        });
    }

    @Override
    public void onScanClick(SecurityScanDto scan) {
        displayScanResults(scan);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
