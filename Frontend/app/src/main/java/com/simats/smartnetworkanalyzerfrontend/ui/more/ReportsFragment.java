package com.simats.smartnetworkanalyzerfrontend.ui.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.smartnetworkanalyzerfrontend.data.model.ReportDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentReportsBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReportsFragment extends Fragment implements ReportAdapter.OnReportActionListener {

    private FragmentReportsBinding binding;
    private MainViewModel viewModel;
    private ReportAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new ReportAdapter(this);
        binding.rvReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReports.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.swipeRefresh.setOnRefreshListener(this::loadReports);
        binding.btnCreateReport.setOnClickListener(v -> showCreateReportDialog());

        loadReports();
    }

    private void loadReports() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.listReports(1, 50).observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            if (resource != null && resource.data != null && resource.data.items != null) {
                adapter.setItems(resource.data.items);
                binding.tvEmpty.setVisibility(resource.data.items.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showCreateReportDialog() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        String endStr = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -30);
        String startStr = sdf.format(cal.getTime());

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        EditText etTitle = new EditText(requireContext());
        etTitle.setText("Monthly Network Diagnostics");
        etTitle.setHint("Report Title");
        layout.addView(etTitle);

        EditText etStart = new EditText(requireContext());
        etStart.setText(startStr);
        etStart.setHint("Start Date (YYYY-MM-DD)");
        layout.addView(etStart);

        EditText etEnd = new EditText(requireContext());
        etEnd.setText(endStr);
        etEnd.setHint("End Date (YYYY-MM-DD)");
        layout.addView(etEnd);

        new AlertDialog.Builder(requireContext())
                .setTitle("Generate Diagnostics Report")
                .setView(layout)
                .setPositiveButton("Generate", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String startDate = etStart.getText().toString().trim();
                    String endDate = etEnd.getText().toString().trim();

                    if (title.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                        Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    binding.swipeRefresh.setRefreshing(true);
                    viewModel.createReport(title, startDate, endDate).observe(getViewLifecycleOwner(), resource -> {
                        binding.swipeRefresh.setRefreshing(false);
                        if (resource != null && resource.data != null) {
                            Toast.makeText(requireContext(), "Report created successfully!", Toast.LENGTH_SHORT).show();
                            loadReports();
                        } else if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.ERROR) {
                            Toast.makeText(requireContext(), "Failed: " + resource.message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(ReportDto report) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Report")
                .setMessage("Delete '" + report.title + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteReport(report.id).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                            Toast.makeText(requireContext(), "Report deleted", Toast.LENGTH_SHORT).show();
                            loadReports();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onExportPdf(ReportDto report) {
        Toast.makeText(requireContext(), "Downloading PDF report...", Toast.LENGTH_SHORT).show();
        viewModel.exportReportPdf(report.id).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null) {
                saveAndOpenPdf(report.id, resource.data);
            } else if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.ERROR) {
                Toast.makeText(requireContext(), "Failed to download PDF: " + resource.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveAndOpenPdf(int reportId, ResponseBody body) {
        try {
            File pdfDir = new File(requireContext().getCacheDir(), "reports");
            if (!pdfDir.exists()) pdfDir.mkdirs();

            File pdfFile = new File(pdfDir, "network_report_" + reportId + ".pdf");
            try (InputStream is = body.byteStream(); FileOutputStream fos = new FileOutputStream(pdfFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
            }

            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent chooser = Intent.createChooser(intent, "Open Network Report PDF");
            startActivity(chooser);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
