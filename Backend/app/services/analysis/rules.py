from typing import Any, Dict, List, Tuple
from app.services.thresholds import (
    JITTER_HIGH_THRESHOLD_MS,
    SIGNAL_FAIR_THRESHOLD,
    SPEED_SLOW_DOWNLOAD_MBPS,
)


def evaluate_rules(metrics: Dict[str, Any]) -> Tuple[int, str, List[Dict[str, str]], List[Dict[str, str]]]:
    """Pure rules engine that deterministically computes issues, recommendations, and health score."""
    issues: List[Dict[str, str]] = []
    recommendations: List[Dict[str, str]] = []

    # 1. Speed Rules
    speed = metrics.get("speed")
    if speed:
        avg_dl = speed.get("avg_download_mbps", 0)
        avg_ul = speed.get("avg_upload_mbps", 0)
        dl_cv = speed.get("download_cv", 0)
        wifi_dl = speed.get("wifi_avg_download_mbps")
        mob_dl = speed.get("mobile_avg_download_mbps")

        if avg_dl < SPEED_SLOW_DOWNLOAD_MBPS:
            issues.append({
                "severity": "high",
                "category": "speed",
                "title": "Slow Download Speeds",
                "description": f"Average download speed is {avg_dl} Mbps, which is below standard broadband expectations.",
            })
            recommendations.append({
                "priority": "high",
                "title": "Check Broadband Plan & Router Position",
                "description": "Verify your subscribed bandwidth with your ISP and ensure your device connects via 5 GHz Wi-Fi.",
            })

        if avg_dl > 40 and avg_ul > 0 and (avg_dl / avg_ul) > 10:
            issues.append({
                "severity": "medium",
                "category": "speed",
                "title": "Severe Speed Asymmetry",
                "description": f"Download speed ({avg_dl} Mbps) drastically outpaces upload speed ({avg_ul} Mbps), which may slow down cloud backups and video calls.",
            })
            recommendations.append({
                "priority": "low",
                "title": "Inspect Upstream Traffic",
                "description": "Check if devices on your network are heavily uploading data or consider upgrading to symmetric fiber broadband.",
            })

        if dl_cv > 0.5:
            issues.append({
                "severity": "medium",
                "category": "speed",
                "title": "Inconsistent Throughput",
                "description": f"High speed variance detected (variability coefficient {dl_cv}). Speeds fluctuate noticeably throughout the day.",
            })
            recommendations.append({
                "priority": "medium",
                "title": "Investigate Peak Hour Throttling",
                "description": "Observe whether speed drops occur during evening peak hours, which indicates local ISP node congestion.",
            })

        if wifi_dl is not None and mob_dl is not None and mob_dl > (wifi_dl * 1.5):
            issues.append({
                "severity": "info",
                "category": "speed",
                "title": "Mobile Data Faster Than Wi-Fi",
                "description": f"Mobile network average ({mob_dl} Mbps) is significantly faster than your Wi-Fi connection ({wifi_dl} Mbps).",
            })
            recommendations.append({
                "priority": "medium",
                "title": "Optimize Local Wi-Fi Hardware",
                "description": "Your internal Wi-Fi configuration or router placement may be bottlenecking your internet service.",
            })

    # 2. Latency Rules
    latency = metrics.get("latency")
    if latency:
        avg_ms = latency.get("avg_ms", 0)
        jitter = latency.get("avg_jitter_ms", 0)
        loss = latency.get("avg_packet_loss_percent", 0)

        if loss >= 2.0:
            issues.append({
                "severity": "critical",
                "category": "latency",
                "title": "Severe Packet Loss",
                "description": f"Observed {loss}% average packet loss. This causes audio dropouts, gaming lag, and broken web connections.",
            })
            recommendations.append({
                "priority": "high",
                "title": "Inspect Physical Cables & Reboot Hardware",
                "description": "Power cycle both your modem and router, and check all Ethernet and coax cable connections.",
            })
        elif loss > 0.5:
            issues.append({
                "severity": "medium",
                "category": "latency",
                "title": "Intermittent Packet Drops",
                "description": f"Packet loss is {loss}%, which is elevated above the normal zero-loss standard.",
            })

        if avg_ms > 100.0:
            issues.append({
                "severity": "high",
                "category": "latency",
                "title": "High Network Latency",
                "description": f"Average ping latency is {avg_ms} ms. Online gaming and interactive sessions will experience lag.",
            })
            recommendations.append({
                "priority": "medium",
                "title": "Use Wired Connection for Sensitive Tasks",
                "description": "Connect gaming consoles or work stations via direct Cat6 Ethernet cable rather than Wi-Fi.",
            })

        if jitter > JITTER_HIGH_THRESHOLD_MS:
            issues.append({
                "severity": "medium",
                "category": "latency",
                "title": "Unstable Jitter",
                "description": f"Average ping jitter is {jitter} ms, indicating inconsistent packet arrival timing.",
            })
            recommendations.append({
                "priority": "medium",
                "title": "Enable Quality of Service (QoS)",
                "description": "Enable QoS on your router to prioritize real-time voice and video traffic over background downloads.",
            })

    # 3. Wi-Fi & Interference Rules
    wifi = metrics.get("wifi")
    if wifi:
        sig = wifi.get("connected_signal_dbm")
        co_nets = wifi.get("same_channel_co_networks", 0)
        connected_ch = wifi.get("connected_channel")

        if sig is not None and sig < SIGNAL_FAIR_THRESHOLD:
            issues.append({
                "severity": "high",
                "category": "signal",
                "title": "Weak Wi-Fi Connection",
                "description": f"Current connected signal is {sig} dBm, which drops throughput and leads to frequent disconnects.",
            })
            recommendations.append({
                "priority": "high",
                "title": "Relocate Closer or Add Mesh Extender",
                "description": "Move your device closer to the router or add a Wi-Fi mesh node to eliminate dead zones.",
            })

        if co_nets >= 3:
            issues.append({
                "severity": "medium",
                "category": "interference",
                "title": "Co-Channel Wi-Fi Congestion",
                "description": f"There are {co_nets} neighboring networks broadcasting on the same channel ({connected_ch}).",
            })
            recommendations.append({
                "priority": "high",
                "title": "Switch Wi-Fi Channel",
                "description": "Access your router admin portal and change 2.4 GHz channel to the least crowded channel (1, 6, or 11), or switch to 5 GHz.",
            })

    # 4. Signal Map Coverage Rules
    signal_map = metrics.get("signal_map")
    if signal_map:
        weak_ratio = signal_map.get("weak_zone_ratio", 0)
        weak_pts = signal_map.get("weak_points_count", 0)
        if weak_ratio > 0.30:
            issues.append({
                "severity": "medium",
                "category": "signal",
                "title": "Excessive Weak Coverage Zones",
                "description": f"{int(weak_ratio * 100)}% of mapped locations ({weak_pts} spots) have poor Wi-Fi coverage below -75 dBm.",
            })
            recommendations.append({
                "priority": "high",
                "title": "Install Mesh Wi-Fi Nodes",
                "description": "Deploy a mesh network node near identified weak zones to provide seamless whole-home coverage.",
            })

    # 5. Security Rules
    security = metrics.get("security")
    if security:
        sec_score = security.get("latest_score", 100)
        sec_risk = security.get("risk_level", "low")
        if sec_score < 70 or sec_risk in ("critical", "high"):
            issues.append({
                "severity": "high" if sec_risk == "high" else "critical",
                "category": "security",
                "title": "Wi-Fi Security Deficiencies",
                "description": f"Latest security scan reported risk level '{sec_risk}' with a score of {sec_score}/100.",
            })
            recommendations.append({
                "priority": "high",
                "title": "Upgrade Wi-Fi Encryption",
                "description": "Ensure your router uses WPA2 or WPA3 and replace outdated protocols like WEP or Open access.",
            })

    # Compute Health Score
    penalties = {
        "critical": 30,
        "high": 15,
        "medium": 8,
        "low": 3,
        "info": 0,
    }
    score = 100
    for issue in issues:
        score -= penalties.get(issue["severity"], 5)
    health_score = max(0, min(100, score))

    # Deduplicate recommendations by title
    seen_titles = set()
    deduped_recs = []
    for r in recommendations:
        if r["title"] not in seen_titles:
            seen_titles.add(r["title"])
            deduped_recs.append(r)

    # General recommendation if network is in great shape
    if not deduped_recs:
        deduped_recs.append({
            "priority": "low",
            "title": "Maintain Current Network Configuration",
            "description": "Your network is operating within optimal performance and security parameters. Continue regular monitoring.",
        })

    # Generate human readable summary
    if health_score >= 85:
        verdict = f"Network is in great health with an overall score of {health_score}/100."
    elif health_score >= 65:
        verdict = f"Network health is fair ({health_score}/100) with {len(issues)} detected areas for improvement."
    else:
        verdict = f"Network health is degraded ({health_score}/100). Critical optimizations are recommended to restore performance and security."

    return health_score, verdict, issues, deduped_recs
