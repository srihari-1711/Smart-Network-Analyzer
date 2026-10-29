"""Shared thresholds for latency, signal quality, and network health."""

# Signal thresholds (RSSI dBm)
SIGNAL_STRONG_THRESHOLD = -60
SIGNAL_FAIR_THRESHOLD = -75
# rssi >= -60 -> "strong"
# -75 <= rssi < -60 -> "fair"
# rssi < -75 -> "weak"


def classify_rssi(rssi: int) -> str:
    if rssi >= SIGNAL_STRONG_THRESHOLD:
        return "strong"
    elif rssi >= SIGNAL_FAIR_THRESHOLD:
        return "fair"
    else:
        return "weak"


# Latency rating thresholds
LATENCY_EXCELLENT_MAX_MS = 30.0
LATENCY_EXCELLENT_MAX_LOSS = 1.0

LATENCY_GOOD_MAX_MS = 60.0
LATENCY_GOOD_MAX_LOSS = 2.0

LATENCY_FAIR_MAX_MS = 100.0
LATENCY_FAIR_MAX_LOSS = 5.0


def rate_latency(avg_ms: float, packet_loss_percent: float) -> str:
    if avg_ms < LATENCY_EXCELLENT_MAX_MS and packet_loss_percent < LATENCY_EXCELLENT_MAX_LOSS:
        return "excellent"
    elif avg_ms < LATENCY_GOOD_MAX_MS and packet_loss_percent < LATENCY_GOOD_MAX_LOSS:
        return "good"
    elif avg_ms < LATENCY_FAIR_MAX_MS and packet_loss_percent < LATENCY_FAIR_MAX_LOSS:
        return "fair"
    else:
        return "poor"


# Speed thresholds (Mbps)
SPEED_SLOW_DOWNLOAD_MBPS = 10.0
SPEED_FAST_DOWNLOAD_MBPS = 100.0

# Jitter threshold
JITTER_HIGH_THRESHOLD_MS = 30.0
