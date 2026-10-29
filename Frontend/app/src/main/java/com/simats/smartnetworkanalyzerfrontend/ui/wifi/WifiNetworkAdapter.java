package com.simats.smartnetworkanalyzerfrontend.ui.wifi;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.smartnetworkanalyzerfrontend.data.model.WifiNetworkDto;
import com.simats.smartnetworkanalyzerfrontend.databinding.ItemWifiNetworkBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WifiNetworkAdapter extends RecyclerView.Adapter<WifiNetworkAdapter.ViewHolder> {

    private final List<WifiNetworkDto> items = new ArrayList<>();

    public void setItems(List<WifiNetworkDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<WifiNetworkDto> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWifiNetworkBinding binding = ItemWifiNetworkBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WifiNetworkDto item = items.get(position);
        holder.binding.tvSsid.setText(item.ssid == null || item.ssid.isEmpty() ? "(Hidden Network)" : item.ssid);
        holder.binding.tvBssid.setText(item.bssid != null ? item.bssid : "--");
        holder.binding.tvSignalDbm.setText(String.format(Locale.getDefault(), "%d dBm", item.signalDbm));

        String band = item.frequencyMhz >= 5000 ? "5 GHz" : "2.4 GHz";
        int channel = item.channel > 0 ? item.channel : frequencyToChannel(item.frequencyMhz);
        holder.binding.tvChannelInfo.setText(String.format(Locale.getDefault(), "Ch %d (%s)", channel, band));
        holder.binding.tvSecurityInfo.setText(item.securityType != null ? item.securityType : "OPEN");
    }

    private int frequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemWifiNetworkBinding binding;

        ViewHolder(ItemWifiNetworkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
