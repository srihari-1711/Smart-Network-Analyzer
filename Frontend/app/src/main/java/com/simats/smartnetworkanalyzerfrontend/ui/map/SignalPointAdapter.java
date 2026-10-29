package com.simats.smartnetworkanalyzerfrontend.ui.map;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.SignalPointDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.ItemSignalPointBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SignalPointAdapter extends RecyclerView.Adapter<SignalPointAdapter.ViewHolder> {

    public interface OnPointActionListener {
        void onDelete(SignalPointDto point);
    }

    private final List<SignalPointDto> items = new ArrayList<>();
    private final OnPointActionListener listener;

    public SignalPointAdapter(OnPointActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SignalPointDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSignalPointBinding binding = ItemSignalPointBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SignalPointDto item = items.get(position);
        holder.binding.tvPointName.setText(item.locationName != null ? item.locationName : "Point #" + item.id);
        holder.binding.tvPointCoords.setText(String.format(Locale.getDefault(), "Pos: (%.2f, %.2f)", item.xCoord, item.yCoord));
        holder.binding.tvPointDbm.setText(String.format(Locale.getDefault(), "%d dBm", item.signalDbm));

        int color;
        if (item.signalDbm >= -60) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.success);
        } else if (item.signalDbm >= -75) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.warning);
        } else {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.error);
        }
        holder.binding.viewSignalIndicator.setBackgroundColor(color);

        holder.binding.btnDeletePoint.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSignalPointBinding binding;

        ViewHolder(ItemSignalPointBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
