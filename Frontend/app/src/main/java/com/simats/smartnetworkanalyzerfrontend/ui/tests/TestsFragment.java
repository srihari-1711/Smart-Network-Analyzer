package com.simats.smartnetworkanalyzerfrontend.ui.tests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.tabs.TabLayout;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.LatencyTestDto;
import com.simats.smartnetworkanalyzerfrontend.data.model.SpeedTestDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentTestsBinding;
import com.simats.smartnetworkanalyzerfrontend.ui.motion.MotionUtils;
import com.simats.smartnetworkanalyzerfrontend.utils.LatencyTester;
import com.simats.smartnetworkanalyzerfrontend.utils.SpeedTester;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestsFragment extends Fragment {

    private FragmentTestsBinding binding;
    private MainViewModel viewModel;

    private SpeedTester speedTester;
    private LatencyTester latencyTester;
    private boolean isSpeedTesting = false;
    private boolean isPingTesting = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupTabs();
        setupSpeedControls();
        setupPingControls();
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    MotionUtils.crossfade(binding.layoutLatencyTest, binding.layoutSpeedTest, 220);
                } else {
                    MotionUtils.crossfade(binding.layoutSpeedTest, binding.layoutLatencyTest, 220);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSpeedControls() {
        MotionUtils.applyPressSpring(binding.btnStartSpeedTest, () -> {
            if (isSpeedTesting) return;
            startSpeedTest();
        });

        MotionUtils.applyPressSpring(binding.btnSpeedStats, () ->
                Navigation.findNavController(requireView()).navigate(R.id.nav_speed_stats));
    }

    private void startSpeedTest() {
        isSpeedTesting = true;
        binding.btnStartSpeedTest.setEnabled(false);
        binding.speedGauge.reset();
        binding.tvSpeedStage.setText("Testing Download Speed...");

        speedTester = new SpeedTester();
        speedTester.startTest(new SpeedTester.SpeedTestListener() {
            @Override
            public void onProgress(boolean isUpload, float currentMbps, int percent) {
                if (binding == null) return;
                binding.speedGauge.setSpeed(currentMbps);
                if (isUpload) {
                    binding.tvSpeedStage.setText(String.format(Locale.getDefault(), "Testing Upload Speed (%d%%)...", percent));
                } else {
                    binding.tvSpeedStage.setText(String.format(Locale.getDefault(), "Testing Download Speed (%d%%)...", percent));
                }
            }

            @Override
            public void onFinished(float downloadMbps, float uploadMbps) {
                if (binding == null) return;
                isSpeedTesting = false;
                binding.btnStartSpeedTest.setEnabled(true);
                binding.tvSpeedStage.setText("Speed Test Completed!");
                binding.speedGauge.setSpeed(downloadMbps);

                MotionUtils.performSuccessHaptic(requireContext());
                MotionUtils.animateCountUp(binding.tvResultDownload, 0f, downloadMbps, "%.1f", " Mbps", 500);
                MotionUtils.animateCountUp(binding.tvResultUpload, 0f, uploadMbps, "%.1f", " Mbps", 500);

                // Save result to backend
                SpeedTestDto dto = new SpeedTestDto();
                dto.downloadMbps = downloadMbps;
                dto.uploadMbps = uploadMbps;
                dto.networkType = "wifi";
                viewModel.createSpeedTest(dto).observe(getViewLifecycleOwner(), resource -> {
                    if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                        Toast.makeText(requireContext(), "Speed test saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (binding == null) return;
                isSpeedTesting = false;
                binding.btnStartSpeedTest.setEnabled(true);
                binding.tvSpeedStage.setText("Speed Test Failed");
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupPingControls() {
        MotionUtils.applyPressSpring(binding.btnStartPingTest, () -> {
            if (isPingTesting) return;
            startPingTest();
        });

        MotionUtils.applyPressSpring(binding.btnLatencyStats, () ->
                Navigation.findNavController(requireView()).navigate(R.id.nav_latency_stats));
    }

    private void startPingTest() {
        String host = binding.etTargetHost.getText() != null ? binding.etTargetHost.getText().toString().trim() : "8.8.8.8";
        if (host.isEmpty()) host = "8.8.8.8";

        isPingTesting = true;
        binding.btnStartPingTest.setEnabled(false);
        binding.pbPingProgress.setVisibility(View.VISIBLE);
        binding.pbPingProgress.setProgress(0);
        binding.tvPingStatus.setText("Pinging " + host + "...");
        binding.tvPingLogs.setText("");

        final List<Float> latencies = new ArrayList<>();
        final String finalHost = host;

        latencyTester = new LatencyTester();
        latencyTester.startTest(host, 10, new LatencyTester.LatencyListener() {
            @Override
            public void onProbe(int probeIndex, int totalProbes, Float rttMs) {
                if (binding == null) return;
                binding.pbPingProgress.setProgress(probeIndex);
                if (rttMs != null) {
                    latencies.add(rttMs);
                    binding.tvPingLogs.append(String.format(Locale.getDefault(), "#%d: 64 bytes from %s: rtt=%.1f ms\n", probeIndex, finalHost, rttMs));
                } else {
                    binding.tvPingLogs.append(String.format(Locale.getDefault(), "#%d: Request timeout from %s\n", probeIndex, finalHost));
                }
            }

            @Override
            public void onComplete(LatencyTestDto dto) {
                if (binding == null) return;
                isPingTesting = false;
                binding.btnStartPingTest.setEnabled(true);
                binding.pbPingProgress.setVisibility(View.INVISIBLE);
                binding.tvPingStatus.setText("Ping completed!");

                MotionUtils.performSuccessHaptic(requireContext());
                MotionUtils.animateCountUp(binding.tvStatAvg, 0f, dto.latencyMs, "%.1f", " ms", 400);
                MotionUtils.animateCountUp(binding.tvStatJitter, 0f, dto.jitterMs, "%.1f", " ms", 400);
                binding.tvStatLoss.setText(String.format(Locale.getDefault(), "%.0f%%", dto.packetLossPercent));

                // Save to backend
                viewModel.createLatencyTest(dto).observe(getViewLifecycleOwner(), resource -> {
                    if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                        Toast.makeText(requireContext(), "Ping result saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (binding == null) return;
                isPingTesting = false;
                binding.btnStartPingTest.setEnabled(true);
                binding.pbPingProgress.setVisibility(View.INVISIBLE);
                binding.tvPingStatus.setText("Ping error: " + message);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (speedTester != null) {
            speedTester.cancel();
        }
        if (latencyTester != null) {
            latencyTester.cancel();
        }
        binding = null;
    }
}
