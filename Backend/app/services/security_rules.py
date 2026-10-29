from typing import Any, Dict, List, Optional, Tuple


def evaluate_security(
    ssid: str,
    security_type: str,
    signal_dbm: Optional[int] = None,
    gateway_ip: Optional[str] = None,
    local_ip: Optional[str] = None,
    devices: Optional[List[Dict[str, Any]]] = None,
) -> Tuple[str, int, List[Dict[str, str]], List[Dict[str, str]]]:
    """Pure security rules engine for Wi-Fi and local network security analysis.
    
    Returns:
        (risk_level, security_score, issues, recommendations)
    """
    issues: List[Dict[str, str]] = []
    recommendations: List[Dict[str, str]] = []
    score = 100
    sec_upper = security_type.upper().strip()

    # Rule 1: Security Type Evaluation
    if sec_upper == "OPEN":
        issues.append({
            "severity": "critical",
            "title": "Unsecured Open Network",
            "description": "Network has no encryption. Any network traffic can be monitored or intercepted by nearby actors.",
        })
        recommendations.append({
            "priority": "high",
            "title": "Enable Wi-Fi Encryption",
            "description": "Configure the router with WPA2-AES or WPA3 security with a strong passphrase.",
        })
        recommendations.append({
            "priority": "high",
            "title": "Use a Trusted VPN",
            "description": "Always connect through an encrypted VPN tunnel when connected to open or public Wi-Fi.",
        })
        score -= 50

        # Sub-rule: Weak signal on open network -> Rogue AP risk
        if signal_dbm is not None and signal_dbm < -80:
            issues.append({
                "severity": "medium",
                "title": "Possible Rogue Access Point",
                "description": "The open network has a very weak signal (-80 dBm or lower), which can indicate an illegitimate 'Evil Twin' or rogue hotspot.",
            })
            recommendations.append({
                "priority": "medium",
                "title": "Avoid Sensitive Logins",
                "description": "Do not enter passwords, banking details, or sensitive personal information on weak open hotspots.",
            })
            score -= 15

    elif sec_upper == "WEP":
        issues.append({
            "severity": "critical",
            "title": "Broken WEP Encryption",
            "description": "WEP encryption has severe cryptographic vulnerabilities and can be cracked in minutes using automated tools.",
        })
        recommendations.append({
            "priority": "high",
            "title": "Upgrade to WPA2 or WPA3",
            "description": "Replace WEP with WPA2-Personal (AES) or WPA3-Personal in router settings immediately.",
        })
        score -= 45

    elif sec_upper == "WPA":
        issues.append({
            "severity": "high",
            "title": "Outdated WPA Protocol",
            "description": "Original WPA uses TKIP which is deprecated and vulnerable to modern cryptanalytic attacks.",
        })
        recommendations.append({
            "priority": "high",
            "title": "Upgrade to WPA2 or WPA3",
            "description": "Switch router security mode to WPA2-AES or WPA3.",
        })
        score -= 25

    elif sec_upper == "WPA2":
        issues.append({
            "severity": "info",
            "title": "WPA2 Standard Protection",
            "description": "WPA2 provides solid security. Consider enabling WPA3 if your hardware supports it for next-generation protection.",
        })
        recommendations.append({
            "priority": "low",
            "title": "Consider WPA3 Upgrade",
            "description": "If your router and devices support WPA3, enable WPA3 or WPA2/WPA3 mixed mode for enhanced protection against brute-force attacks.",
        })

    elif sec_upper in ("WPA3", "WPA2_WPA3"):
        # Modern state-of-the-art encryption
        pass

    elif sec_upper == "UNKNOWN":
        issues.append({
            "severity": "medium",
            "title": "Unknown Security Protocol",
            "description": "Unable to determine the security encryption type for this network.",
        })
        recommendations.append({
            "priority": "medium",
            "title": "Verify Router Encryption",
            "description": "Check your router management console to verify that WPA2 or WPA3 is enabled.",
        })
        score -= 15

    # Rule 2: Device Count Rule (> 15 devices)
    num_devices = len(devices) if devices else 0
    if num_devices > 15:
        issues.append({
            "severity": "info",
            "title": "High Connected Device Count",
            "description": f"Detected {num_devices} active devices on the local subnet. High device counts may cause network congestion or indicate unauthorized users.",
        })
        recommendations.append({
            "priority": "medium",
            "title": "Audit Connected Devices",
            "description": "Review the list of connected devices in your router admin panel and revoke access for unfamiliar hardware.",
        })

    # Standard security recommendations for all networks
    recommendations.append({
        "priority": "medium",
        "title": "Keep Router Firmware Updated",
        "description": "Check for and apply router manufacturer firmware updates to patch known security vulnerabilities.",
    })
    recommendations.append({
        "priority": "medium",
        "title": "Change Default Router Admin Password",
        "description": "Ensure the router administration panel does not use default credentials (e.g., admin/admin).",
    })
    recommendations.append({
        "priority": "low",
        "title": "Disable Wi-Fi Protected Setup (WPS)",
        "description": "Disable PIN-based WPS in router settings to prevent PIN brute-force attacks.",
    })

    # Deduplicate recommendations by title
    seen_titles = set()
    deduped_recs = []
    for rec in recommendations:
        if rec["title"] not in seen_titles:
            seen_titles.add(rec["title"])
            deduped_recs.append(rec)

    # Calculate overall risk_level and bound score
    score = max(0, min(100, score))

    severities = {issue["severity"] for issue in issues}
    if "critical" in severities or score < 50:
        risk_level = "critical"
    elif "high" in severities or score < 70:
        risk_level = "high"
    elif "medium" in severities or score < 85:
        risk_level = "medium"
    else:
        risk_level = "low"

    return risk_level, score, issues, deduped_recs
