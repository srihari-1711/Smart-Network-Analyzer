package com.simats.smartnetworkanalyzerfrontend.ui.more;

import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.DataUsageDto;
import com.simats.smartnetworkanalyzerfrontend.data.model.DataUsageSummaryDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentDataUsageBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.*;

public class DataUsageFragment extends Fragment {

    private FragmentDataUsageBinding binding;
    private MainViewModel viewModel;
    private String selectedPeriod = "daily";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDataUsageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupChart();
        setupListeners();
        checkUsagePermission();
        loadSummary();
    }

    private void setupChart() {
        binding.chartDataUsage.getDescription().setEnabled(false);
        binding.chartDataUsage.setDrawGridBackground(false);
        binding.chartDataUsage.getAxisRight().setEnabled(false);
        int textColor = requireContext().getColor(R.color.text_tertiary);
        int gridColor = requireContext().getColor(R.color.divider);

        XAxis xAxis = binding.chartDataUsage.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(textColor);
        xAxis.setAxisLineColor(gridColor);

        com.github.mikephil.charting.components.YAxis yAxis = binding.chartDataUsage.getAxisLeft();
        yAxis.setTextColor(textColor);
        yAxis.setGridColor(gridColor);
        yAxis.setAxisLineColor(gridColor);
        binding.chartDataUsage.getLegend().setTextColor(textColor);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.swipeRefresh.setOnRefreshListener(this::loadSummary);

        binding.btnGrantUsagePermission.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Could not open Usage Access settings", Toast.LENGTH_SHORT).show();
            }
        });

        binding.chipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_period_weekly)) {
                selectedPeriod = "weekly";
            } else if (checkedIds.contains(R.id.chip_period_monthly)) {
                selectedPeriod = "monthly";
            } else {
                selectedPeriod = "daily";
            }
            loadSummary();
        });

        binding.btnSyncUsage.setOnClickListener(v -> syncCurrentUsage());
    }

    private void checkUsagePermission() {
        boolean hasPermission = false;
        try {
            AppOpsManager appOps = (AppOpsManager) requireContext().getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), requireContext().getPackageName());
            hasPermission = (mode == AppOpsManager.MODE_ALLOWED);
        } catch (Exception ignored) {}

        binding.cardPermissionWarning.setVisibility(hasPermission ? View.GONE : View.VISIBLE);
    }

    private void syncCurrentUsage() {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        long rxWifi = 0;
        long txWifi = 0;
        long rxMobile = 0;
        long txMobile = 0;

        try {
            NetworkStatsManager nsm = (NetworkStatsManager) requireContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            if (nsm != null) {
                NetworkStats.Bucket bucketWifi = nsm.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "", startOfDay, now);
                rxWifi = bucketWifi.getRxBytes();
                txWifi = bucketWifi.getTxBytes();

                NetworkStats.Bucket bucketMobile = nsm.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, "", startOfDay, now);
                rxMobile = bucketMobile.getRxBytes();
                txMobile = bucketMobile.getTxBytes();
            }
        } catch (Exception e) {
            // Permission not granted or simulator fallback values
            rxWifi = 120L * 1024L * 1024L;
            txWifi = 35L * 1024L * 1024L;
            rxMobile = 15L * 1024L * 1024L;
            txMobile = 4L * 1024L * 1024L;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        DataUsageDto dto = new DataUsageDto();
        dto.periodStart = sdf.format(new Date(startOfDay));
        dto.periodEnd = sdf.format(new Date(now));
        dto.rxBytesWifi = rxWifi;
        dto.txBytesWifi = txWifi;
        dto.rxBytesMobile = rxMobile;
        dto.txBytesMobile = txMobile;

        viewModel.upsertDataUsage(dto).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Data usage synced!", Toast.LENGTH_SHORT).show();
                loadSummary();
            }
        });
    }

    private void loadSummary() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.getDataUsageSummary(selectedPeriod).observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            if (resource != null && resource.data != null) {
                DataUsageSummaryDto summary = resource.data;
                updateSummaryView(summary);
                updateChart(summary);
            }
        });
    }

    private void updateSummaryView(DataUsageSummaryDto summary) {
        if (summary.totals == null) return;
        double wifiMb = summary.totals.wifiBytes / (1024.0 * 1024.0);
        double mobileMb = summary.totals.mobileBytes / (1024.0 * 1024.0);
        double totalMb = summary.totals.totalBytes / (1024.0 * 1024.0);

        binding.tvTotalRx.setText(formatMbOrGb(wifiMb));
        binding.tvTotalTx.setText(formatMbOrGb(mobileMb));
        binding.tvTotalCombined.setText(formatMbOrGb(totalMb));
    }

    private String formatMbOrGb(double mb) {
        if (mb >= 1024.0) {
            return String.format(Locale.getDefault(), "%.2f GB", mb / 1024.0);
        }
        return String.format(Locale.getDefault(), "%.1f MB", mb);
    }

    private void updateChart(DataUsageSummaryDto summary) {
        if (summary.buckets == null || summary.buckets.isEmpty()) {
            binding.chartDataUsage.clear();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < summary.buckets.size(); i++) {
            DataUsageSummaryDto.UsageBucket b = summary.buckets.get(i);
            float wifi = (float) (b.wifiBytes / (1024.0 * 1024.0));
            float mobile = (float) (b.mobileBytes / (1024.0 * 1024.0));
            entries.add(new BarEntry(i, new float[]{wifi, mobile}));

            String label = b.label;
            if (label != null && label.length() >= 10) {
                label = label.substring(5, 10); // MM-dd
            }
            labels.add(label != null ? label : String.valueOf(i));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Wi-Fi (MB) / Mobile (MB)");
        dataSet.setColors(
                requireContext().getColor(R.color.primary),
                requireContext().getColor(R.color.accent)
        );
        dataSet.setStackLabels(new String[]{"Wi-Fi (MB)", "Mobile (MB)"});
        dataSet.setValueTextColor(requireContext().getColor(R.color.text_secondary));
        dataSet.setValueTextSize(9f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        XAxis xAxis = binding.chartDataUsage.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        binding.chartDataUsage.setData(data);
        binding.chartDataUsage.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
