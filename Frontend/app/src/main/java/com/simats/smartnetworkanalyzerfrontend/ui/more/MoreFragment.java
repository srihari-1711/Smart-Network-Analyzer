package com.simats.smartnetworkanalyzerfrontend.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.databinding.FragmentMoreBinding;

public class MoreFragment extends Fragment {

    private FragmentMoreBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cardMoreDataUsage.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_data_usage));

        binding.cardMoreSecurity.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_security));

        binding.cardMoreAnalysis.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_analysis));

        binding.cardMoreReports.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_reports));

        binding.cardMoreProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_profile));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
