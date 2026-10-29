package com.simats.smartnetworkanalyzerfrontend.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    public static final int REQ_CODE_WIFI_PERMISSIONS = 101;

    public static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static boolean hasLocationPermissions(Context context) {
        return hasWifiScanPermissions(context);
    }

    public static boolean hasWifiScanPermissions(Context context) {
        boolean fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean nearby = ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED;
            return fineLoc && nearby;
        }
        return fineLoc;
    }

    public static void requestWifiScanPermissions(Activity activity) {
        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }
        ActivityCompat.requestPermissions(activity, perms.toArray(new String[0]), REQ_CODE_WIFI_PERMISSIONS);
    }

    public static boolean isLocationServiceEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return false;
        boolean gps = false;
        boolean network = false;
        try {
            gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {}
        try {
            network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {}
        return gps || network;
    }

    public static void showLocationSettingsDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Location Services Required")
                .setMessage("Android requires Location Services to be enabled in order to detect nearby Wi-Fi networks.")
                .setPositiveButton("Enable in Settings", (dialog, which) -> {
                    context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void showPermissionSettingsDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Permissions Needed")
                .setMessage("Wi-Fi scanning permission has been permanently denied. Please enable Location and Nearby Devices permissions in App Settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                    context.startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
