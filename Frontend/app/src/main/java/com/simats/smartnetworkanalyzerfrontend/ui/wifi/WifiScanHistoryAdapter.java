package com.simats.smartnetworkanalyzerfrontend.ui.wifi;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.smartnetworkanalyzerfrontend.data.model.WifiScanDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.ItemWifiScanBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WifiScanHistoryAdapter extends RecyclerView.Adapter<WifiScanHistoryAdapter.ViewHolder> {

    public interface OnScanActionListener {
        void onDelete(WifiScanDto scan);
        void onSelect(WifiScanDto scan);
    }

    private final List<WifiScanDto> items = new ArrayList<>();
    private final OnScanActionListener listener;

    public WifiScanHistoryAdapter(OnScanActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<WifiScanDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWifiScanBinding binding = ItemWifiScanBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WifiScanDto item = items.get(position);
        holder.binding.tvScanLocation.setText(item.locationTag != null && !item.locationTag.isEmpty() ? item.locationTag : "Scan #" + item.id);
        holder.binding.tvScanTimestamp.setText(item.scannedAt != null ? item.scannedAt : "--");

        int networkCount = item.networks != null ? item.networks.size() : 0;
        int bestSignal = -100;
        if (item.networks != null) {
            for (int i = 0; i < item.networks.size(); i++) {
                if (item.networks.get(i).signalDbm > bestSignal) {
                    bestSignal = item.networks.get(i).signalDbm;
                }
            }
        }
        holder.binding.tvScanDetails.setText(String.format(Locale.getDefault(),
                "Found %d networks  •  Best: %d dBm", networkCount, bestSignal == -100 ? 0 : bestSignal));

        holder.binding.btnDeleteScan.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSelect(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemWifiScanBinding binding;

        ViewHolder(ItemWifiScanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
