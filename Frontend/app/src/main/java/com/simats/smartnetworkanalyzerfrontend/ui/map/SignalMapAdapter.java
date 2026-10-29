package com.simats.smartnetworkanalyzerfrontend.ui.map;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.smartnetworkanalyzerfrontend.data.model.SignalMapDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.ItemSignalMapBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SignalMapAdapter extends RecyclerView.Adapter<SignalMapAdapter.ViewHolder> {

    public interface OnMapActionListener {
        void onSelect(SignalMapDto map);
        void onDelete(SignalMapDto map);
    }

    private final List<SignalMapDto> items = new ArrayList<>();
    private final OnMapActionListener listener;

    public SignalMapAdapter(OnMapActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SignalMapDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSignalMapBinding binding = ItemSignalMapBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SignalMapDto item = items.get(position);
        holder.binding.tvMapName.setText(item.name != null ? item.name : "Untitled Map");
        holder.binding.tvMapDesc.setText(item.description != null && !item.description.isEmpty() ? item.description : "No description");
        holder.binding.tvPointCount.setText(String.format(Locale.getDefault(), "%d Points", item.pointCount));

        if (item.avgRssiDbm != null) {
            holder.binding.tvAvgSignal.setText(String.format(Locale.getDefault(), "Avg: %.0f dBm", item.avgRssiDbm));
        } else {
            holder.binding.tvAvgSignal.setText("No data");
        }

        holder.binding.btnDeleteMap.setOnClickListener(v -> {
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
        final ItemSignalMapBinding binding;

        ViewHolder(ItemSignalMapBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
