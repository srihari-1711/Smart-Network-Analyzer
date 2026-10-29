package com.simats.smartnetworkanalyzerfrontend.utils;

import android.os.Handler;
import android.os.Looper;
import com.simats.smartnetworkanalyzerfrontend.data.model.LatencyTestDto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LatencyTester {

    public interface LatencyListener {
        void onProbe(int probeIndex, int totalProbes, Float rttMs);
        void onComplete(LatencyTestDto result);
        void onError(String message);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private volatile boolean isCancelled = false;

    public void startTest(String targetHost, int probeCount, LatencyListener listener) {
        isCancelled = false;
        executor.execute(() -> {
            try {
                String host = (targetHost != null && !targetHost.trim().isEmpty()) ? targetHost.trim() : "8.8.8.8";
                int total = probeCount > 0 ? probeCount : 20;

                List<Float> successfulRtts = new ArrayList<>();
                int lostCount = 0;
                double jitter = 0.0;
                Float previousRtt = null;

                for (int i = 1; i <= total; i++) {
                    if (isCancelled) return;

                    Float rtt = measureSingleProbe(host);
                    final int probeIndex = i;

                    if (rtt != null && rtt >= 0) {
                        successfulRtts.add(rtt);
                        if (previousRtt != null) {
                            double delta = Math.abs(rtt - previousRtt);
                            jitter = jitter + (delta - jitter) / 16.0; // RFC 3550 EWMA
                        }
                        previousRtt = rtt;
                        mainHandler.post(() -> listener.onProbe(probeIndex, total, rtt));
                    } else {
                        lostCount++;
                        mainHandler.post(() -> listener.onProbe(probeIndex, total, null));
                    }

                    Thread.sleep(80); // Probe spacing
                }

                if (isCancelled) return;

                LatencyTestDto dto = new LatencyTestDto();
                dto.targetHost = host;
                dto.packetLossPercent = (float) ((lostCount * 100.0) / total);

                if (!successfulRtts.isEmpty()) {
                    float sum = 0f;
                    float min = Float.MAX_VALUE;
                    float max = 0f;
                    for (float r : successfulRtts) {
                        sum += r;
                        if (r < min) min = r;
                        if (r > max) max = r;
                    }
                    dto.latencyMs = sum / successfulRtts.size();
                    dto.minLatencyMs = min;
                    dto.maxLatencyMs = max;
                    dto.jitterMs = (float) jitter;
                } else {
                    dto.latencyMs = 999f;
                    dto.minLatencyMs = 999f;
                    dto.maxLatencyMs = 999f;
                    dto.jitterMs = 0f;
                    dto.packetLossPercent = 100f;
                }

                mainHandler.post(() -> listener.onComplete(dto));

            } catch (Exception e) {
                if (!isCancelled) {
                    mainHandler.post(() -> listener.onError(e.getMessage() != null ? e.getMessage() : "Latency test failed"));
                }
            }
        });
    }

    private Float measureSingleProbe(String host) {
        // First try socket TCP probe to port 53 (DNS) or 443
        long start = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, 53), 1000);
            long elapsed = System.currentTimeMillis() - start;
            return (float) Math.max(1, elapsed);
        } catch (Exception e) {
            // Second try ping command
            try {
                long pingStart = System.currentTimeMillis();
                Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -W 1 " + host);
                int exitCode = process.waitFor();
                long pingElapsed = System.currentTimeMillis() - pingStart;
                if (exitCode == 0) {
                    return (float) Math.max(1, pingElapsed);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public void cancel() {
        isCancelled = true;
    }
}
