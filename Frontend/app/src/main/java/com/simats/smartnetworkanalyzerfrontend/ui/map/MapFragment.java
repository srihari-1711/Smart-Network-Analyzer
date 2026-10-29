package com.simats.smartnetworkanalyzerfrontend.ui.map;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.SignalMapDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentMapBinding;
import com.simats.smartnetworkanalyzerfrontend.viewmodel.MainViewModel;

public class MapFragment extends Fragment implements SignalMapAdapter.OnMapActionListener {

    private FragmentMapBinding binding;
    private MainViewModel viewModel;
    private SignalMapAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new SignalMapAdapter(this);
        binding.rvMaps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMaps.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadMaps);
        binding.fabAddMap.setOnClickListener(v -> showCreateMapDialog());

        loadMaps();
    }

    private void loadMaps() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.listSignalMaps(1, 50).observe(getViewLifecycleOwner(), resource -> {
            binding.swipeRefresh.setRefreshing(false);
            if (resource != null && resource.data != null && resource.data.items != null) {
                adapter.setItems(resource.data.items);
                binding.tvEmpty.setVisibility(resource.data.items.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showCreateMapDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        EditText etName = new EditText(requireContext());
        etName.setHint("Map Name (e.g. 1st Floor)");
        layout.addView(etName);

        EditText etDesc = new EditText(requireContext());
        etDesc.setHint("Description (optional)");
        layout.addView(etDesc);

        new AlertDialog.Builder(requireContext())
                .setTitle("Create Signal Map")
                .setView(layout)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Map name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SignalMapDto dto = new SignalMapDto();
                    dto.name = name;
                    dto.description = desc;
                    viewModel.createSignalMap(dto).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                            Toast.makeText(requireContext(), "Map created", Toast.LENGTH_SHORT).show();
                            loadMaps();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSelect(SignalMapDto map) {
        Bundle bundle = new Bundle();
        bundle.putInt("map_id", map.id);
        bundle.putString("map_name", map.name);
        bundle.putString("map_desc", map.description);
        Navigation.findNavController(requireView()).navigate(R.id.nav_map_detail, bundle);
    }

    @Override
    public void onDelete(SignalMapDto map) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Map")
                .setMessage("Delete '" + map.name + "' and all its recorded points?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteSignalMap(map.id).observe(getViewLifecycleOwner(), resource -> {
                        if (resource != null && resource.status == com.simats.smartnetworkanalyzerfrontend.data.repository.Resource.Status.SUCCESS) {
                            Toast.makeText(requireContext(), "Map deleted", Toast.LENGTH_SHORT).show();
                            loadMaps();
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
