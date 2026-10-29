package com.simats.smartnetworkanalyzerfrontend.ui.more;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.smartnetworkanalyzerfrontend.data.model.AnalysisDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.ItemAnalysisBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalysisAdapter extends RecyclerView.Adapter<AnalysisAdapter.ViewHolder> {

    public interface OnAnalysisClickListener {
        void onAnalysisClick(AnalysisDto analysis);
    }

    private final List<AnalysisDto> items = new ArrayList<>();
    private final OnAnalysisClickListener listener;

    public AnalysisAdapter(OnAnalysisClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AnalysisDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAnalysisBinding binding = ItemAnalysisBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnalysisDto item = items.get(position);
        holder.binding.tvWindowDays.setText(String.format(Locale.getDefault(), "%d-Day Diagnostic", item.windowDays));
        holder.binding.tvCreatedAt.setText(item.createdAt != null ? item.createdAt : "--");
        holder.binding.tvHealthScore.setText(String.format(Locale.getDefault(), "Score: %d/100", item.healthScore));
        holder.binding.tvEngineBadge.setText(item.engine != null ? item.engine.toUpperCase() : "RULES");

        int issueCount = item.issues != null ? item.issues.size() : 0;
        holder.binding.tvIssuesCount.setText(String.format(Locale.getDefault(), "%d Issue%s", issueCount, issueCount == 1 ? "" : "s"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAnalysisClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAnalysisBinding binding;

        ViewHolder(ItemAnalysisBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
