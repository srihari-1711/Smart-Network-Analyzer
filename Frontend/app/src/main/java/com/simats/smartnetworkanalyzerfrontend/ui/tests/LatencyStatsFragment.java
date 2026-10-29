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
import com.simats.smartnetworkanalyzerfrontend.data.model.LatencyTestDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentLatencyStatsBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LatencyStatsFragment extends Fragment implements LatencyTestAdapter.OnLatencyActionListener {

    private FragmentLatencyStatsBinding binding;
    private MainViewModel viewModel;
    private LatencyTestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLatencyStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new LatencyTestAdapter(this);
        binding.rvLatencyHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLatencyHistory.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.swipeRefresh.setOnRefreshListener(this::loadData);

        setupChart();
        loadData();
    }

    private void setupChart() {
        binding.chartLatency.getDescription().setEnabled(false);
        binding.chartLatency.setDrawGridBackground(false);
        binding.chartLatency.getAxisRight().setEnabled(false);
        int textColor = requireContext().getColor(R.color.text_tertiary);
        int gridColor = requireContext().getColor(R.color.divider);

        XAxis xAxis = binding.chartLatency.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textColor);
        xAxis.setAxisLineColor(gridColor);

        com.github.mikephil.charting.components.YAxis yAxis = binding.chartLatency.getAxisLeft();
        yAxis.setTextColor(textColor);
        yAxis.setGridColor(gridColor);
        yAxis.setAxisLineColor(gridColor);
        binding.chartLatency.getLegend().setTextColor(textColor);
    }

    private void loadData() {
        binding.swipeRefresh.setRefreshing(true);

        // 1. Load Aggregated Stats
        viewModel.getLatencyStats().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null) {
                binding.tvMeanLat.setText(String.format(Locale.getDefault(), "Avg: %.0f ms", resource.data.meanLatencyMs));
                binding.tvMeanJitter.setText(String.format(Locale.getDefault(), "Jitter: %.1f ms", resource.data.meanJitterMs));
                binding.tvMeanLoss.setText(String.format(Locale.getDefault(), "Loss: %.0f%%", resource.data.meanPacketLossPercent));
            }
        });

        // 2. Load History & Chart
        viewModel.listLatencyTests(1, 50).observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            if (resource != null && resource.data != null && resource.data.items != null) {
                List<LatencyTestDto> items = resource.data.items;
                adapter.setItems(items);
                binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                updateChart(items);
            }
        });
    }

    private void updateChart(List<LatencyTestDto> items) {
        if (items.isEmpty()) {
            binding.chartLatency.clear();
            return;
        }

        List<LatencyTestDto> reversed = new ArrayList<>(items);
        Collections.reverse(reversed);

        List<Entry> latencyEntries = new ArrayList<>();
        for (int i = 0; i < reversed.size(); i++) {
            latencyEntries.add(new Entry(i, reversed.get(i).latencyMs));
        }

        LineDataSet set = new LineDataSet(latencyEntries, "Latency (ms)");
        set.setColor(requireContext().getColor(R.color.primary));
        set.setCircleColor(requireContext().getColor(R.color.primary));
        set.setLineWidth(2.5f);
        set.setCircleRadius(3.5f);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawFilled(true);
        set.setFillColor(requireContext().getColor(R.color.primary));
        set.setFillAlpha(35);

        LineData data = new LineData(set);
        binding.chartLatency.setData(data);
        binding.chartLatency.invalidate();
    }

    @Override
    public void onDelete(LatencyTestDto test) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Latency Test")
                .setMessage("Delete test record for " + test.targetHost + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteLatencyTest(test.id).observe(getViewLifecycleOwner(), resource -> {
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
