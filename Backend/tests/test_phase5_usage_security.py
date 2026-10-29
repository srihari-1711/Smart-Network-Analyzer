import pytest
from app.services.security_rules import evaluate_security


def get_auth_header(client, email):
    res = client.post(
        "/api/v1/auth/signup",
        json={"name": "Phase5 User", "email": email, "password": "Password123!"},
    )
    if res.status_code == 409:
        res = client.post(
            "/api/v1/auth/login",
            json={"email": email, "password": "Password123!"},
        )
    return {"Authorization": f"Bearer {res.json()['access_token']}"}


def test_security_rules_unit():
    # OPEN network with weak signal
    risk, score, issues, recs = evaluate_security(
        ssid="FreeWifi",
        security_type="OPEN",
        signal_dbm=-85,
    )
    assert risk == "critical"
    assert score <= 35
    severities = [i["severity"] for i in issues]
    assert "critical" in severities
    assert any("Rogue" in i["title"] for i in issues)

    # WEP
    risk_wep, score_wep, issues_wep, _ = evaluate_security(ssid="OldAP", security_type="WEP")
    assert risk_wep == "critical"
    assert any("WEP" in i["title"] for i in issues_wep)

    # WPA
    risk_wpa, score_wpa, issues_wpa, _ = evaluate_security(ssid="OldWpa", security_type="WPA")
    assert risk_wpa in ("high", "critical")
    assert any("WPA" in i["title"] for i in issues_wpa)

    # WPA2
    risk_wpa2, score_wpa2, issues_wpa2, _ = evaluate_security(ssid="HomeWPA2", security_type="WPA2")
    assert score_wpa2 == 100
    assert risk_wpa2 == "low"

    # WPA3
    risk_wpa3, score_wpa3, issues_wpa3, _ = evaluate_security(ssid="HomeWPA3", security_type="WPA3")
    assert score_wpa3 == 100
    assert len(issues_wpa3) == 0

    # Many devices (> 15)
    devices = [{"ip": f"192.168.1.{i}"} for i in range(20)]
    _, _, issues_dev, _ = evaluate_security(ssid="OfficeNet", security_type="WPA2", devices=devices)
    assert any("High Connected Device Count" in i["title"] for i in issues_dev)


def test_data_usage_upsert_and_summary(client):
    headers = get_auth_header(client, "usage_user@example.com")

    usage1 = {
        "period_start": "2026-10-24T00:00:00Z",
        "period_end": "2026-10-24T23:59:59Z",
        "wifi_rx_bytes": 1000000,
        "wifi_tx_bytes": 500000,
        "mobile_rx_bytes": 200000,
        "mobile_tx_bytes": 100000,
    }
    # First post -> 201
    res1 = client.post("/api/v1/data-usage", json=usage1, headers=headers)
    assert res1.status_code == 201

    # Upsert with new numbers for the same period -> 200
    usage1_updated = {
        "period_start": "2026-10-24T00:00:00Z",
        "period_end": "2026-10-24T23:59:59Z",
        "wifi_rx_bytes": 2000000,
        "wifi_tx_bytes": 1000000,
        "mobile_rx_bytes": 400000,
        "mobile_tx_bytes": 200000,
    }
    res2 = client.post("/api/v1/data-usage", json=usage1_updated, headers=headers)
    assert res2.status_code == 200
    assert res2.json()["wifi_rx_bytes"] == 2000000

    # Test daily summary
    res_daily = client.get("/api/v1/data-usage/summary?period=daily", headers=headers)
    assert res_daily.status_code == 200
    daily_data = res_daily.json()
    assert daily_data["period"] == "daily"
    assert len(daily_data["buckets"]) >= 1
    assert daily_data["totals"]["wifi_bytes"] == 3000000

    # Test weekly summary
    res_weekly = client.get("/api/v1/data-usage/summary?period=weekly", headers=headers)
    assert res_weekly.status_code == 200
    assert res_weekly.json()["period"] == "weekly"

    # Test monthly summary
    res_monthly = client.get("/api/v1/data-usage/summary?period=monthly", headers=headers)
    assert res_monthly.status_code == 200
    assert res_monthly.json()["period"] == "monthly"


def test_security_scan_api_and_isolation(client):
    headers_a = get_auth_header(client, "security_a@example.com")
    headers_b = get_auth_header(client, "security_b@example.com")

    scan_payload = {
        "ssid": "CoffeeShop_Free",
        "bssid": "00:11:22:33:44:55",
        "security_type": "OPEN",
        "signal_dbm": -82,
        "gateway_ip": "192.168.1.1",
        "local_ip": "192.168.1.105",
        "devices": [
            {"ip": "192.168.1.1", "hostname": "router.local"},
            {"ip": "192.168.1.105", "hostname": "pixel-phone"},
        ],
    }

    res = client.post("/api/v1/security-scans", json=scan_payload, headers=headers_a)
    assert res.status_code == 201
    scan = res.json()
    assert scan["risk_level"] == "critical"
    assert len(scan["devices"]) == 2
    scan_id = scan["id"]

    # List
    res_list = client.get("/api/v1/security-scans", headers=headers_a)
    assert res_list.status_code == 200
    items = res_list.json()["items"]
    assert len(items) >= 1
    item = next(i for i in items if i["id"] == scan_id)
    assert item["device_count"] == 2

    # User B cannot see User A's security scan (isolation)
    res_b = client.get(f"/api/v1/security-scans/{scan_id}", headers=headers_b)
    assert res_b.status_code == 404
