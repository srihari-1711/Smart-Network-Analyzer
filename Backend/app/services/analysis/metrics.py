import math
from datetime import datetime, timedelta, timezone
from typing import Any, Dict
from sqlalchemy.orm import Session
from app.models.data_usage import DataUsageRecord
from app.models.security import SecurityScan
from app.models.signal_map import SignalMap, SignalPoint
from app.models.speed_latency import LatencyTest, SpeedTest
from app.models.wifi import WifiNetwork, WifiScan
from app.services.thresholds import classify_rssi


def _calc_cv(values: list[float]) -> float:
    if not values or len(values) < 2:
        return 0.0
    mean = sum(values) / len(values)
    if mean == 0:
        return 0.0
    variance = sum((x - mean) ** 2 for x in values) / (len(values) - 1)
    std_dev = math.sqrt(variance)
    return round(std_dev / mean, 3)


def aggregate_metrics(db: Session, user_id: int, days: int = 7) -> Dict[str, Any]:
    cutoff = datetime.now(timezone.utc) - timedelta(days=days)
    snapshot: Dict[str, Any] = {}
    total_data_points = 0

    # 1. Speed tests
    speed_tests = (
        db.query(SpeedTest)
        .filter(SpeedTest.user_id == user_id, SpeedTest.tested_at >= cutoff)
        .all()
    )
    if speed_tests:
        total_data_points += len(speed_tests)
        dls = [t.download_mbps for t in speed_tests]
        uls = [t.upload_mbps for t in speed_tests]
        wifi_dls = [t.download_mbps for t in speed_tests if t.network_type == "wifi"]
        mob_dls = [t.download_mbps for t in speed_tests if t.network_type == "mobile"]

        snapshot["speed"] = {
            "count": len(speed_tests),
            "avg_download_mbps": round(sum(dls) / len(dls), 2),
            "min_download_mbps": round(min(dls), 2),
            "max_download_mbps": round(max(dls), 2),
            "download_cv": _calc_cv(dls),
            "avg_upload_mbps": round(sum(uls) / len(uls), 2),
            "min_upload_mbps": round(min(uls), 2),
            "max_upload_mbps": round(max(uls), 2),
            "upload_cv": _calc_cv(uls),
            "wifi_avg_download_mbps": round(sum(wifi_dls) / len(wifi_dls), 2) if wifi_dls else None,
            "mobile_avg_download_mbps": round(sum(mob_dls) / len(mob_dls), 2) if mob_dls else None,
        }

    # 2. Latency tests
    latency_tests = (
        db.query(LatencyTest)
        .filter(LatencyTest.user_id == user_id, LatencyTest.tested_at >= cutoff)
        .all()
    )
    if latency_tests:
        total_data_points += len(latency_tests)
        avgs = [t.avg_ms for t in latency_tests]
        jitters = [t.jitter_ms for t in latency_tests]
        losses = [t.packet_loss_percent for t in latency_tests]

        snapshot["latency"] = {
            "count": len(latency_tests),
            "avg_ms": round(sum(avgs) / len(avgs), 2),
            "min_ms": round(min(t.min_ms for t in latency_tests), 2),
            "max_ms": round(max(t.max_ms for t in latency_tests), 2),
            "avg_jitter_ms": round(sum(jitters) / len(jitters), 2),
            "avg_packet_loss_percent": round(sum(losses) / len(losses), 2),
        }

    # 3. Wi-Fi scans
    scans = (
        db.query(WifiScan)
        .filter(WifiScan.user_id == user_id, WifiScan.scanned_at >= cutoff)
        .order_by(WifiScan.scanned_at.desc())
        .all()
    )
    if scans:
        total_data_points += len(scans)
        latest_scan = scans[0]
        networks = latest_scan.networks
        connected_net = next((n for n in networks if n.is_connected), None)

        channel_counts: Dict[int, int] = {}
        for n in networks:
            channel_counts[n.channel] = channel_counts.get(n.channel, 0) + 1

        connected_channel = connected_net.channel if connected_net else None
        same_channel_count = (
            channel_counts.get(connected_channel, 1) - 1 if connected_channel else 0
        )

        # 2.4 GHz main channels
        ch1 = channel_counts.get(1, 0)
        ch6 = channel_counts.get(6, 0)
        ch11 = channel_counts.get(11, 0)
        five_ghz_count = sum(cnt for ch, cnt in channel_counts.items() if ch > 14)

        snapshot["wifi"] = {
            "scan_count": len(scans),
            "nearby_networks_count": len(networks),
            "connected_signal_dbm": connected_net.signal_dbm if connected_net else None,
            "connected_channel": connected_channel,
            "connected_security": connected_net.security if connected_net else None,
            "same_channel_co_networks": same_channel_count,
            "channel_1_count": ch1,
            "channel_6_count": ch6,
            "channel_11_count": ch11,
            "five_ghz_count": five_ghz_count,
        }

    # 4. Signal Maps
    maps = (
        db.query(SignalMap)
        .filter(SignalMap.user_id == user_id, SignalMap.created_at >= cutoff)
        .all()
    )
    points = (
        db.query(SignalPoint)
        .join(SignalMap)
        .filter(SignalMap.user_id == user_id, SignalPoint.recorded_at >= cutoff)
        .all()
    )
    if points:
        total_data_points += len(points)
        weak_count = sum(1 for p in points if classify_rssi(p.rssi_dbm) == "weak")
        weak_ratio = round(weak_count / len(points), 2)
        snapshot["signal_map"] = {
            "map_count": len(maps),
            "total_points": len(points),
            "weak_points_count": weak_count,
            "weak_zone_ratio": weak_ratio,
            "avg_rssi_dbm": round(sum(p.rssi_dbm for p in points) / len(points), 1),
        }

    # 5. Security Scans
    latest_sec = (
        db.query(SecurityScan)
        .filter(SecurityScan.user_id == user_id, SecurityScan.scanned_at >= cutoff)
        .order_by(SecurityScan.scanned_at.desc())
        .first()
    )
    if latest_sec:
        total_data_points += 1
        snapshot["security"] = {
            "latest_score": latest_sec.security_score,
            "risk_level": latest_sec.risk_level,
            "issue_count": len(latest_sec.issues) if latest_sec.issues else 0,
        }

    # 6. Data usage
    usage_records = (
        db.query(DataUsageRecord)
        .filter(DataUsageRecord.user_id == user_id, DataUsageRecord.period_start >= cutoff)
        .all()
    )
    if usage_records:
        total_data_points += len(usage_records)
        wifi_tot = sum(r.wifi_rx_bytes + r.wifi_tx_bytes for r in usage_records)
        mobile_tot = sum(r.mobile_rx_bytes + r.mobile_tx_bytes for r in usage_records)
        snapshot["data_usage"] = {
            "record_count": len(usage_records),
            "wifi_bytes": wifi_tot,
            "mobile_bytes": mobile_tot,
            "total_bytes": wifi_tot + mobile_tot,
        }

    snapshot["_meta"] = {
        "days": days,
        "total_data_points": total_data_points,
    }
    return snapshot
