package com.simats.smartnetworkanalyzerfrontend.ui.tests;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.smartnetworkanalyzerfrontend.data.model.LatencyTestDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.ItemLatencyTestBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LatencyTestAdapter extends RecyclerView.Adapter<LatencyTestAdapter.ViewHolder> {

    public interface OnLatencyActionListener {
        void onDelete(LatencyTestDto test);
    }

    private final List<LatencyTestDto> items = new ArrayList<>();
    private final OnLatencyActionListener listener;

    public LatencyTestAdapter(OnLatencyActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<LatencyTestDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLatencyTestBinding binding = ItemLatencyTestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LatencyTestDto item = items.get(position);
        holder.binding.tvTargetHost.setText(item.targetHost != null ? item.targetHost : "8.8.8.8");
        holder.binding.tvTimestamp.setText(item.testedAt != null ? item.testedAt : "--");
        holder.binding.tvAvgLatency.setText(String.format(Locale.getDefault(), "Avg: %.0f ms", item.latencyMs));
        holder.binding.tvJitter.setText(String.format(Locale.getDefault(), "Jitter: %.1f ms", item.jitterMs));
        holder.binding.tvPacketLoss.setText(String.format(Locale.getDefault(), "Loss: %.0f%%", item.packetLossPercent));

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemLatencyTestBinding binding;

        ViewHolder(ItemLatencyTestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
