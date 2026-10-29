package com.simats.smartnetworkanalyzerfrontend.ui.tests;

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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.SpeedTestDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentSpeedStatsBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SpeedStatsFragment extends Fragment implements SpeedTestAdapter.OnSpeedActionListener {

    private FragmentSpeedStatsBinding binding;
    private MainViewModel viewModel;
    private SpeedTestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSpeedStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new SpeedTestAdapter(this);
        binding.rvSpeedHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSpeedHistory.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.swipeRefresh.setOnRefreshListener(this::loadData);

        setupChart();
        loadData();
    }

    private void setupChart() {
        binding.chartSpeed.getDescription().setEnabled(false);
        binding.chartSpeed.setDrawGridBackground(false);
        binding.chartSpeed.getAxisRight().setEnabled(false);
        int textColor = requireContext().getColor(R.color.text_tertiary);
        int gridColor = requireContext().getColor(R.color.divider);

        XAxis xAxis = binding.chartSpeed.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textColor);
        xAxis.setAxisLineColor(gridColor);

        com.github.mikephil.charting.components.YAxis yAxis = binding.chartSpeed.getAxisLeft();
        yAxis.setTextColor(textColor);
        yAxis.setGridColor(gridColor);
        yAxis.setAxisLineColor(gridColor);
        binding.chartSpeed.getLegend().setTextColor(textColor);
    }

    private void loadData() {
        binding.swipeRefresh.setRefreshing(true);

        // 1. Load Aggregated Stats
        viewModel.getSpeedStats().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null) {
                binding.tvMeanDown.setText(String.format(Locale.getDefault(), "Avg Down: %.1f", resource.data.meanDownloadMbps));
                binding.tvMaxDown.setText(String.format(Locale.getDefault(), "Max Down: %.1f", resource.data.maxDownloadMbps));
                binding.tvP95Down.setText(String.format(Locale.getDefault(), "P95: %.1f", resource.data.p95DownloadMbps));
            }
        });

        // 2. Load History & Chart
        viewModel.listSpeedTests(1, 50).observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            if (resource != null && resource.data != null && resource.data.items != null) {
                List<SpeedTestDto> items = resource.data.items;
                adapter.setItems(items);
                binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                updateChart(items);
            }
        });
    }

    private void updateChart(List<SpeedTestDto> items) {
        if (items.isEmpty()) {
            binding.chartSpeed.clear();
            return;
        }

        // Show chronological order (oldest to newest)
        List<SpeedTestDto> reversed = new ArrayList<>(items);
        Collections.reverse(reversed);

        List<Entry> downEntries = new ArrayList<>();
        List<Entry> upEntries = new ArrayList<>();

        for (int i = 0; i < reversed.size(); i++) {
            downEntries.add(new Entry(i, (float) reversed.get(i).downloadMbps));
            upEntries.add(new Entry(i, (float) reversed.get(i).uploadMbps));
        }

        LineDataSet downSet = new LineDataSet(downEntries, "Download (Mbps)");
        downSet.setColor(requireContext().getColor(R.color.primary));
        downSet.setCircleColor(requireContext().getColor(R.color.primary));
        downSet.setLineWidth(2.5f);
        downSet.setCircleRadius(3.5f);
        downSet.setDrawValues(false);
        downSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        downSet.setDrawFilled(true);
        downSet.setFillColor(requireContext().getColor(R.color.primary));
        downSet.setFillAlpha(35);

        LineDataSet upSet = new LineDataSet(upEntries, "Upload (Mbps)");
        upSet.setColor(requireContext().getColor(R.color.accent));
        upSet.setCircleColor(requireContext().getColor(R.color.accent));
        upSet.setLineWidth(2.5f);
        upSet.setCircleRadius(3.5f);
        upSet.setDrawValues(false);
        upSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(downSet, upSet);
        binding.chartSpeed.setData(data);
        binding.chartSpeed.invalidate();
    }

    @Override
    public void onDelete(SpeedTestDto test) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Speed Test")
                .setMessage("Delete test record from " + test.testedAt + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteSpeedTest(test.id).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                            Toast.makeText(requireContext(), "Test deleted", Toast.LENGTH_SHORT).show();
                            loadData();
                        }
                    });
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
