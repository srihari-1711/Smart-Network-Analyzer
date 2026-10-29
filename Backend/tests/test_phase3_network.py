import pytest


def get_auth_header(client, email, name="Test User"):
    res = client.post(
        "/api/v1/auth/signup",
        json={"name": name, "email": email, "password": "Password123!"},
    )
    if res.status_code == 409:
        res = client.post(
            "/api/v1/auth/login",
            json={"email": email, "password": "Password123!"},
        )
    token = res.json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


def test_wifi_scan_crud_and_isolation(client):
    headers_a = get_auth_header(client, "user_a_wifi@example.com")
    headers_b = get_auth_header(client, "user_b_wifi@example.com")

    # User A creates a scan
    scan_data = {
        "location_label": "Office Desk",
        "networks": [
            {
                "ssid": "HomeNet_5G",
                "bssid": "aa:bb:cc:dd:ee:01",
                "signal_dbm": -52,
                "frequency_mhz": 5180,
                "channel": 36,
                "security": "WPA2",
                "is_connected": True,
            },
            {
                "ssid": "Guest_2G",
                "bssid": "aa:bb:cc:dd:ee:02",
                "signal_dbm": -78,
                "frequency_mhz": 2412,
                "channel": 1,
                "security": "OPEN",
                "is_connected": False,
            },
        ],
    }
    res = client.post("/api/v1/wifi/scans", json=scan_data, headers=headers_a)
    assert res.status_code == 201
    created_scan = res.json()
    assert created_scan["network_count"] == 2
    scan_id = created_scan["id"]

    # User A lists scans
    res_list = client.get("/api/v1/wifi/scans", headers=headers_a)
    assert res_list.status_code == 200
    list_data = res_list.json()
    assert list_data["total"] >= 1
    item = next(i for i in list_data["items"] if i["id"] == scan_id)
    assert item["strongest_signal_dbm"] == -52
    assert item["connected_ssid"] == "HomeNet_5G"

    # User A gets scan details
    res_get = client.get(f"/api/v1/wifi/scans/{scan_id}", headers=headers_a)
    assert res_get.status_code == 200
    assert len(res_get.json()["networks"]) == 2

    # User B cannot access User A's scan (isolation returns 404)
    res_b = client.get(f"/api/v1/wifi/scans/{scan_id}", headers=headers_b)
    assert res_b.status_code == 404

    # User A deletes scan
    res_del = client.delete(f"/api/v1/wifi/scans/{scan_id}", headers=headers_a)
    assert res_del.status_code == 204

    # Now it is gone
    assert client.get(f"/api/v1/wifi/scans/{scan_id}", headers=headers_a).status_code == 404


def test_speed_test_stats_and_isolation(client):
    headers_a = get_auth_header(client, "user_a_speed@example.com")
    headers_b = get_auth_header(client, "user_b_speed@example.com")

    # Add 2 speed tests for User A
    t1 = {
        "download_mbps": 100.0,
        "upload_mbps": 50.0,
        "ping_ms": 15.0,
        "network_type": "wifi",
        "ssid": "FastWifi",
        "tested_at": "2026-10-20T10:00:00Z",
    }
    t2 = {
        "download_mbps": 50.0,
        "upload_mbps": 20.0,
        "ping_ms": 25.0,
        "network_type": "wifi",
        "ssid": "FastWifi",
        "tested_at": "2026-10-21T10:00:00Z",
    }
    res1 = client.post("/api/v1/speed-tests", json=t1, headers=headers_a)
    res2 = client.post("/api/v1/speed-tests", json=t2, headers=headers_a)
    assert res1.status_code == 201
    assert res2.status_code == 201
    id1 = res1.json()["id"]

    # Verify Stats calculation
    res_stats = client.get("/api/v1/speed-tests/stats", headers=headers_a)
    assert res_stats.status_code == 200
    stats = res_stats.json()
    assert stats["count"] == 2
    assert stats["avg_download_mbps"] == 75.0
    assert stats["avg_upload_mbps"] == 35.0
    assert stats["max_download_mbps"] == 100.0
    assert stats["min_download_mbps"] == 50.0
    assert stats["best_test_id"] == id1

    # User B stats should be 0 (isolated)
    res_stats_b = client.get("/api/v1/speed-tests/stats", headers=headers_b)
    assert res_stats_b.status_code == 200
    assert res_stats_b.json()["count"] == 0


def test_latency_test_stats_and_rating(client):
    headers = get_auth_header(client, "user_latency@example.com")

    # Add excellent latency test
    lat_data = {
        "target_host": "8.8.8.8",
        "avg_ms": 18.5,
        "min_ms": 15.0,
        "max_ms": 24.0,
        "jitter_ms": 2.5,
        "packet_loss_percent": 0.0,
        "packets_sent": 20,
        "packets_received": 20,
        "ssid": "TestWifi",
    }
    res = client.post("/api/v1/latency-tests", json=lat_data, headers=headers)
    assert res.status_code == 201

    res_stats = client.get("/api/v1/latency-tests/stats", headers=headers)
    assert res_stats.status_code == 200
    stats = res_stats.json()
    assert stats["count"] == 1
    assert stats["avg_ms"] == 18.5
    assert stats["rating"] == "excellent"


def test_validation_errors(client):
    headers = get_auth_header(client, "validation_user@example.com")

    # Invalid signal dBm (> 0)
    bad_scan = {
        "networks": [
            {
                "ssid": "InvalidSignal",
                "signal_dbm": 10,
                "frequency_mhz": 2412,
                "channel": 1,
                "security": "OPEN",
            }
        ]
    }
    res = client.post("/api/v1/wifi/scans", json=bad_scan, headers=headers)
    assert res.status_code == 422
    assert res.json()["error"]["code"] == "validation_error"

    # Negative speed test
    bad_speed = {
        "download_mbps": -5.0,
        "upload_mbps": 10.0,
        "network_type": "wifi",
    }
    res = client.post("/api/v1/speed-tests", json=bad_speed, headers=headers)
    assert res.status_code == 422
