package com.simats.smartnetworkanalyzerfrontend.ui.tests;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.smartnetworkanalyzerfrontend.data.model.SpeedTestDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.ItemSpeedTestBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeedTestAdapter extends RecyclerView.Adapter<SpeedTestAdapter.ViewHolder> {

    public interface OnSpeedActionListener {
        void onDelete(SpeedTestDto test);
    }

    private final List<SpeedTestDto> items = new ArrayList<>();
    private final OnSpeedActionListener listener;

    public SpeedTestAdapter(OnSpeedActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SpeedTestDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSpeedTestBinding binding = ItemSpeedTestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpeedTestDto item = items.get(position);
        holder.binding.tvTimestamp.setText(item.testedAt != null ? item.testedAt : "--");
        holder.binding.tvDownloadMbps.setText(String.format(Locale.getDefault(), "%.1f Mbps", item.downloadMbps));
        holder.binding.tvUploadMbps.setText(String.format(Locale.getDefault(), "%.1f Mbps", item.uploadMbps));

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSpeedTestBinding binding;

        ViewHolder(ItemSpeedTestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
