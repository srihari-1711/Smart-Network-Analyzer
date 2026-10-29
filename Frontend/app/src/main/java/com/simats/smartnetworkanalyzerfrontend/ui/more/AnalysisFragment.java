package com.simats.smartnetworkanalyzerfrontend.ui.more;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.AnalysisDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentAnalysisBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

import java.util.List;
import java.util.Locale;

public class AnalysisFragment extends Fragment implements AnalysisAdapter.OnAnalysisClickListener {

    private FragmentAnalysisBinding binding;
    private MainViewModel viewModel;
    private AnalysisAdapter adapter;
    private int selectedDays = 14;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new AnalysisAdapter(this);
        binding.rvAnalyses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAnalyses.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.swipeRefresh.setOnRefreshListener(this::loadAnalyses);

        setupDaysSelector();
        binding.btnRunAnalysis.setOnClickListener(v -> runAnalysis());

        loadAnalyses();
    }

    private void setupDaysSelector() {
        binding.chipGroupDays.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_7_days)) {
                selectedDays = 7;
            } else if (checkedIds.contains(R.id.chip_30_days)) {
                selectedDays = 30;
            } else {
                selectedDays = 14;
            }
        });
    }

    private void runAnalysis() {
        binding.btnRunAnalysis.setEnabled(false);
        binding.swipeRefresh.setRefreshing(true);

        viewModel.runAnalysis(selectedDays).observe(getViewLifecycleOwner(), resource -> {
            binding.btnRunAnalysis.setEnabled(true);
            binding.swipeRefresh.setRefreshing(false);
            if (resource != null && resource.data != null) {
                Toast.makeText(requireContext(), "Diagnostic completed!", Toast.LENGTH_SHORT).show();
                displayAnalysis(resource.data);
                loadAnalyses();
            } else if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.ERROR) {
                String msg = resource.message != null ? resource.message : "Diagnostic failed";
                if (msg.contains("no_data") || msg.contains("No measurement")) {
                    Toast.makeText(requireContext(), "No network tests recorded in the past " + selectedDays + " days. Run a speed test or Wi-Fi scan first!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void displayAnalysis(@NonNull AnalysisDto analysis) {
        binding.cardAnalysisResult.setVisibility(View.VISIBLE);
        binding.tvHealthScore.setText(String.format(Locale.getDefault(), "%d / 100", analysis.healthScore));
        binding.tvEngineBadge.setText(analysis.engine != null ? analysis.engine.toUpperCase() : "RULES");
        binding.tvSummaryText.setText(analysis.summary != null ? analysis.summary : "No summary available.");

        // Issues
        if (analysis.issues != null && !analysis.issues.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AnalysisDto.IssueDto issue : analysis.issues) {
                sb.append("• [").append(issue.severity != null ? issue.severity.toUpperCase() : "INFO")
                        .append("] ").append(issue.title != null ? issue.title : "")
                        .append(": ").append(issue.description != null ? issue.description : "").append("\n");
            }
            binding.tvIssuesText.setText(sb.toString().trim());
        } else {
            binding.tvIssuesText.setText("No performance or stability issues identified.");
        }

        // Recommendations
        if (analysis.recommendations != null && !analysis.recommendations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (AnalysisDto.RecommendationDto rec : analysis.recommendations) {
                sb.append("• ").append(rec.toString()).append("\n");
            }
            binding.tvRecommendationsText.setText(sb.toString().trim());
        } else {
            binding.tvRecommendationsText.setText("• Continue current network configuration.");
        }
    }

    private void loadAnalyses() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.listAnalyses(1, 20).observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            if (resource != null && resource.data != null && resource.data.items != null) {
                List<AnalysisDto> items = resource.data.items;
                adapter.setItems(items);
                binding.tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                if (!items.isEmpty() && binding.cardAnalysisResult.getVisibility() == View.GONE) {
                    displayAnalysis(items.get(0));
                }
            }
        });
    }

    @Override
    public void onAnalysisClick(AnalysisDto analysis) {
        displayAnalysis(analysis);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
