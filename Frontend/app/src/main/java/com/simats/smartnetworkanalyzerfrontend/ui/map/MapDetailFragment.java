package com.simats.smartnetworkanalyzerfrontend.ui.map;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.simats.smartnetworkanalyzerfrontend.data.model.SignalPointDto;
import com.simats.smartnetworkanalyzerfrontend.data.repository.Resource;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentMapDetailBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapDetailFragment extends Fragment implements SignalPointAdapter.OnPointActionListener {

    private static final String TAG = "MapDetailFragment";

    private FragmentMapDetailBinding binding;
    private MainViewModel viewModel;
    private SignalPointAdapter adapter;
    private WifiManager wifiManager;

    private int mapId;
    private String mapName;
    private String mapDesc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (getArguments() != null) {
            mapId = getArguments().getInt("map_id", 0);
            mapName = getArguments().getString("map_name", "Map Details");
            mapDesc = getArguments().getString("map_desc", "");
        }

        binding.tvTitle.setText(mapName);
        binding.tvDescription.setText(mapDesc != null && !mapDesc.isEmpty() ? mapDesc : "Floor coverage map");

        adapter = new SignalPointAdapter(this);
        binding.rvPoints.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPoints.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.heatMapView.setOnMapTapListener(this::promptRecordPoint);
        binding.btnRecordPoint.setOnClickListener(v -> promptRecordPoint(0.5f, 0.5f));

        loadMapDetails();
    }

    private void loadMapDetails() {
        // 1. Load Map & Points
        viewModel.getSignalMap(mapId).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    List<SignalPointDto> pts = resource.data.points != null ? resource.data.points : new ArrayList<>();
                    adapter.setItems(pts);
                    binding.heatMapView.setPoints(pts);

                    boolean isEmpty = pts.isEmpty();
                    binding.tvEmptyPoints.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    binding.rvPoints.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                } else if (resource.status == Resource.Status.ERROR) {
                    Log.e(TAG, "getSignalMap error: " + resource.message);
                }
            }
        });

        // 2. Load Summary
        viewModel.getSignalMapSummary(mapId).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    StringBuilder sb = new StringBuilder();
                    if (resource.data.verdictSummary != null && !resource.data.verdictSummary.isEmpty()) {
                        sb.append(resource.data.verdictSummary);
                    } else {
                        sb.append("Summary verdict available after recording points.");
                    }

                    if (resource.data.avgRssiDbm != null) {
                        sb.append(String.format(Locale.getDefault(), " (Avg: %.1f dBm)", resource.data.avgRssiDbm));
                    }
                    if (resource.data.strongestPoint != null) {
                        sb.append(String.format(Locale.getDefault(), "\nBest: %s (%d dBm)",
                                resource.data.strongestPoint.label, resource.data.strongestPoint.rssiDbm));
                    }
                    if (resource.data.weakestPoint != null) {
                        sb.append(String.format(Locale.getDefault(), "  •  Weakest: %s (%d dBm)",
                                resource.data.weakestPoint.label, resource.data.weakestPoint.rssiDbm));
                    }
                    binding.tvSummaryVerdict.setText(sb.toString());

                    if (resource.data.zoneCounts != null) {
                        binding.tvZoneStrong.setText(String.format(Locale.getDefault(), "Strong: %d", resource.data.zoneCounts.strong));
                        binding.tvZoneFair.setText(String.format(Locale.getDefault(), "Fair: %d", resource.data.zoneCounts.fair));
                        binding.tvZoneWeak.setText(String.format(Locale.getDefault(), "Weak: %d", resource.data.zoneCounts.weak));
                    }
                } else if (resource.status == Resource.Status.ERROR) {
                    Log.e(TAG, "getSignalMapSummary error: " + resource.message);
                }
            }
        });
    }

    private void promptRecordPoint(float xRatio, float yRatio) {
        int currentRssi = -65;
        if (wifiManager != null) {
            try {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null && info.getRssi() < 0 && info.getRssi() >= -120) {
                    currentRssi = info.getRssi();
                }
            } catch (Exception ignored) {}
        }

        final int sampledBm = currentRssi;
        final int nextSpotNum = (adapter != null ? adapter.getItemCount() : 0) + 1;
        final String defaultName = "Spot " + nextSpotNum;

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        EditText etName = new EditText(requireContext());
        etName.setText(defaultName);
        etName.setHint("Location Name (e.g. Desk, Kitchen)");
        etName.setSelectAllOnFocus(true);
        layout.addView(etName);

        EditText etDbm = new EditText(requireContext());
        etDbm.setText(String.valueOf(sampledBm));
        etDbm.setHint("Signal (dBm, -120 to 0)");
        etDbm.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        layout.addView(etDbm);

        new AlertDialog.Builder(requireContext())
                .setTitle("Record Signal Spot")
                .setMessage(String.format(Locale.getDefault(), "Coordinate: (%.2f, %.2f)", xRatio, yRatio))
                .setView(layout)
                .setPositiveButton("Record", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        name = defaultName;
                    }

                    int dbm = sampledBm;
                    try {
                        String txt = etDbm.getText().toString().trim();
                        if (!txt.isEmpty()) {
                            int parsed = Integer.parseInt(txt);
                            if (parsed >= -120 && parsed <= 0) {
                                dbm = parsed;
                            } else if (parsed > 0 && parsed <= 120) {
                                dbm = -parsed;
                            }
                        }
                    } catch (NumberFormatException ignored) {}

                    SignalPointDto pt = new SignalPointDto();
                    pt.xCoord = xRatio;
                    pt.yCoord = yRatio;
                    pt.signalDbm = dbm;
                    pt.locationName = name;

                    final String recordedName = name;
                    final int recordedDbm = dbm;

                    viewModel.addSignalPoint(mapId, pt).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null) {
                            if (resource.status == Resource.Status.SUCCESS) {
                                Snackbar.make(binding.getRoot(),
                                        String.format(Locale.getDefault(), "Recorded %s (%d dBm)", recordedName, recordedDbm),
                                        Snackbar.LENGTH_SHORT).show();
                                loadMapDetails();
                            } else if (resource.status == Resource.Status.ERROR) {
                                String errorMsg = resource.message != null ? resource.message : "Failed to record point";
                                Log.e(TAG, "addSignalPoint error: " + errorMsg);
                                Snackbar.make(binding.getRoot(), "Error: " + errorMsg, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(SignalPointDto point) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Point")
                .setMessage("Delete recorded spot '" + point.locationName + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteSignalPoint(mapId, point.id).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null) {
                            if (resource.status == Resource.Status.SUCCESS) {
                                Snackbar.make(binding.getRoot(), "Point deleted", Snackbar.LENGTH_SHORT).show();
                                loadMapDetails();
                            } else if (resource.status == Resource.Status.ERROR) {
                                String errorMsg = resource.message != null ? resource.message : "Failed to delete point";
                                Log.e(TAG, "deleteSignalPoint error: " + errorMsg);
                                Snackbar.make(binding.getRoot(), "Error: " + errorMsg, Snackbar.LENGTH_LONG).show();
                            }
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
