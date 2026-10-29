package com.simats.smartnetworkanalyzerfrontend.ui.more;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.smartnetworkanalyzerfrontend.data.model.ReportDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.ItemReportBinding;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    public interface OnReportActionListener {
        void onDelete(ReportDto report);
        void onExportPdf(ReportDto report);
    }

    private final List<ReportDto> items = new ArrayList<>();
    private final OnReportActionListener listener;

    public ReportAdapter(OnReportActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ReportDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportBinding binding = ItemReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportDto item = items.get(position);
        holder.binding.tvReportTitle.setText(item.title != null ? item.title : "Network Diagnostics Report");
        holder.binding.tvReportRange.setText("Period: " + item.startDate + " to " + item.endDate);
        holder.binding.tvReportCreated.setText("Created: " + (item.createdAt != null ? item.createdAt : "--"));

        holder.binding.btnDeleteReport.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });

        holder.binding.btnExportPdf.setOnClickListener(v -> {
            if (listener != null) listener.onExportPdf(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemReportBinding binding;

        ViewHolder(ItemReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
