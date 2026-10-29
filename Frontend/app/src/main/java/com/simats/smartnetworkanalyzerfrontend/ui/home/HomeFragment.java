package com.simats.smartnetworkanalyzerfrontend.ui.home;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.DashboardDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentHomeBinding;
import com.simats.smartnetworkanalyzerfrontend.ui.motion.MotionUtils;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupListeners();
        loadDashboard();

        // Staggered card entrance
        MotionUtils.applyStaggeredEntrance(binding.homeContentContainer);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadDashboard);

        MotionUtils.applyPressSpring(binding.btnActionScan, () ->
                Navigation.findNavController(requireView()).navigate(R.id.nav_wifi));

        MotionUtils.applyPressSpring(binding.btnActionSpeed, () ->
                Navigation.findNavController(requireView()).navigate(R.id.nav_tests));

        MotionUtils.applyPressSpring(binding.btnActionAnalyze, () ->
                Navigation.findNavController(requireView()).navigate(R.id.nav_analysis));

        MotionUtils.applyPressSpring(binding.cardHealthScore, () ->
                Navigation.findNavController(requireView()).navigate(R.id.nav_analysis));
    }

    private void loadDashboard() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.getDashboard().observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            if (resource == null) return;

            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) {
                        populateData(resource.data);
                    }
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), "Error: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    break;
            }
        });
    }

    private void populateData(@NonNull DashboardDto data) {
        if (data.latestAnalysis != null) {
            int score = Math.max(0, Math.min(100, data.latestAnalysis.healthScore));
            animateHealthScoreRing(score);

            boolean isAi = "ai".equalsIgnoreCase(data.latestAnalysis.engine);
            binding.tvEngineBadge.setText(isAi ? "AI ENGINE" : "RULES");
            binding.tvEngineBadge.setBackgroundResource(isAi ? R.drawable.bg_badge_ai : R.drawable.bg_badge_strong);
            binding.tvEngineBadge.setTextColor(ContextCompat.getColor(requireContext(), isAi ? R.color.ai_accent : R.color.text_secondary));

            binding.tvAnalysisSummary.setText(data.latestAnalysis.summary != null ? data.latestAnalysis.summary : "No summary available.");
        } else {
            binding.tvHealthScore.setText("-- / 100");
            binding.progressHealthRing.setProgress(0);
            binding.tvEngineBadge.setText("PENDING");
            binding.tvAnalysisSummary.setText("Run your first AI diagnostic to inspect overall health.");
        }

        if (data.latestSpeedTest != null) {
            MotionUtils.animateCountUp(binding.tvLatestDownload, 0f, (float) data.latestSpeedTest.downloadMbps, "%.1f", " Mbps", 600);
            MotionUtils.animateCountUp(binding.tvLatestUpload, 0f, (float) data.latestSpeedTest.uploadMbps, "%.1f", " Mbps", 600);
        } else {
            binding.tvLatestDownload.setText("-- Mbps");
            binding.tvLatestUpload.setText("-- Mbps");
        }

        if (data.latestLatencyTest != null) {
            MotionUtils.animateCountUp(binding.tvLatestLatency, 0f, (float) data.latestLatencyTest.latencyMs, "%.0f", " ms", 500);
        } else {
            binding.tvLatestLatency.setText("-- ms");
        }

        if (data.latestWifiScan != null && data.latestWifiScan.networks != null && !data.latestWifiScan.networks.isEmpty()) {
            binding.tvCurrentSignal.setText(String.format(Locale.getDefault(), "%d dBm", data.latestWifiScan.networks.get(0).signalDbm));
        } else {
            binding.tvCurrentSignal.setText("-- dBm");
        }

        if (data.activityCounts != null) {
            binding.tvActivityCounts.setText(String.format(Locale.getDefault(),
                    "Speed Tests: %d  |  Pings: %d  |  Wi-Fi Scans: %d  |  Maps: %d",
                    data.activityCounts.speedTests,
                    data.activityCounts.latencyTests,
                    data.activityCounts.wifiScans,
                    data.activityCounts.signalMaps));
        }
    }

    private void animateHealthScoreRing(int targetScore) {
        int colorRes = targetScore >= 80 ? R.color.status_strong : (targetScore >= 60 ? R.color.status_fair : R.color.status_weak);
        int color = ContextCompat.getColor(requireContext(), colorRes);
        binding.progressHealthRing.setIndicatorColor(color);

        ValueAnimator animator = ValueAnimator.ofInt(0, targetScore);
        animator.setDuration(800);
        animator.setInterpolator(new DecelerateInterpolator(1.8f));
        animator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            if (binding != null) {
                binding.progressHealthRing.setProgress(val);
                binding.tvHealthScore.setText(String.format(Locale.getDefault(), "%d / 100", val));
            }
        });
        animator.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
