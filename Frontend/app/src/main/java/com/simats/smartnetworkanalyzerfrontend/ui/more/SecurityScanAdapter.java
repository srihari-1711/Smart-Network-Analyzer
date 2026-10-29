package com.simats.smartnetworkanalyzerfrontend.ui.more;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.SecurityScanDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.ItemSecurityScanBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SecurityScanAdapter extends RecyclerView.Adapter<SecurityScanAdapter.ViewHolder> {

    public interface OnScanClickListener {
        void onScanClick(SecurityScanDto scan);
    }

    private final List<SecurityScanDto> items = new ArrayList<>();
    private final OnScanClickListener listener;

    public SecurityScanAdapter(OnScanClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SecurityScanDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSecurityScanBinding binding = ItemSecurityScanBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SecurityScanDto item = items.get(position);
        holder.binding.tvNetworkSsid.setText(item.networkSsid != null && !item.networkSsid.isEmpty() ? item.networkSsid : "Direct Gateway Scan");
        holder.binding.tvScannedAt.setText(item.scannedAt != null ? item.scannedAt : "--");
        holder.binding.tvScore.setText(String.format(Locale.getDefault(), "Score: %d/100", item.securityScore));

        int issueCount = item.issues != null ? item.issues.size() : 0;
        holder.binding.tvIssueCount.setText(String.format(Locale.getDefault(), "%d Issue%s detected", issueCount, issueCount == 1 ? "" : "s"));

        String risk = item.riskLevel != null ? item.riskLevel.toUpperCase() : "LOW";
        holder.binding.tvRiskBadge.setText(risk);

        int riskColor;
        if ("CRITICAL".equals(risk) || "HIGH".equals(risk)) {
            riskColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.danger);
            holder.binding.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_weak);
        } else if ("MEDIUM".equals(risk)) {
            riskColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.warning);
            holder.binding.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_fair);
        } else {
            riskColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.success);
            holder.binding.tvRiskBadge.setBackgroundResource(R.drawable.bg_badge_strong);
        }
        holder.binding.tvRiskBadge.setTextColor(riskColor);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onScanClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSecurityScanBinding binding;

        ViewHolder(ItemSecurityScanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
