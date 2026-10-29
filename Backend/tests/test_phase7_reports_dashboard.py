from datetime import datetime, timedelta, timezone
import pytest


def get_auth_header(client, email):
    res = client.post(
        "/api/v1/auth/signup",
        json={"name": "Reports User", "email": email, "password": "Password123!"},
    )
    if res.status_code == 409:
        res = client.post(
            "/api/v1/auth/login",
            json={"email": email, "password": "Password123!"},
        )
    return {"Authorization": f"Bearer {res.json()['access_token']}"}


def test_dashboard_empty_and_with_data(client):
    headers = get_auth_header(client, "dash_user@example.com")

    # 1. Empty dashboard
    res_empty = client.get("/api/v1/dashboard", headers=headers)
    assert res_empty.status_code == 200
    dash = res_empty.json()
    assert dash["latest_speed_test"] is None
    assert dash["latest_latency_test"] is None
    assert dash["latest_wifi_scan_summary"] is None
    assert dash["counts"]["speed_tests"] == 0

    # 2. Add speed test and scan
    client.post(
        "/api/v1/speed-tests",
        json={"download_mbps": 95.0, "upload_mbps": 40.0, "network_type": "wifi"},
        headers=headers,
    )
    client.post(
        "/api/v1/wifi/scans",
        json={
            "location_label": "Desk",
            "networks": [
                {
                    "ssid": "MyWifi",
                    "signal_dbm": -55,
                    "frequency_mhz": 5200,
                    "channel": 40,
                    "security": "WPA3",
                    "is_connected": True,
                }
            ],
        },
        headers=headers,
    )

    # 3. Non-empty dashboard
    res_full = client.get("/api/v1/dashboard", headers=headers)
    assert res_full.status_code == 200
    dash_full = res_full.json()
    assert dash_full["latest_speed_test"]["download_mbps"] == 95.0
    assert dash_full["latest_wifi_scan_summary"]["connected_ssid"] == "MyWifi"
    assert dash_full["current_signal_dbm"] == -55
    assert dash_full["counts"]["speed_tests"] == 1
    assert dash_full["counts"]["wifi_scans"] == 1


def test_report_creation_and_pdf_export(client):
    headers = get_auth_header(client, "report_user@example.com")
    now = datetime.now(timezone.utc)
    start_str = (now - timedelta(days=5)).isoformat()
    end_str = now.isoformat()

    # Add data in range
    client.post(
        "/api/v1/speed-tests",
        json={
            "download_mbps": 120.0,
            "upload_mbps": 60.0,
            "network_type": "wifi",
            "tested_at": (now - timedelta(days=2)).isoformat(),
        },
        headers=headers,
    )

    # Create report
    res_report = client.post(
        "/api/v1/reports",
        json={"start_date": start_str, "end_date": end_str, "title": "5-Day Performance Audit"},
        headers=headers,
    )
    assert res_report.status_code == 201
    report = res_report.json()
    assert report["title"] == "5-Day Performance Audit"
    assert "sections" in report
    assert report["sections"]["speed"]["avg_download_mbps"] == 120.0
    report_id = report["id"]

    # Export PDF
    res_pdf = client.get(f"/api/v1/reports/{report_id}/export", headers=headers)
    assert res_pdf.status_code == 200
    assert res_pdf.headers["content-type"] == "application/pdf"
    # PDF files start with %PDF
    assert res_pdf.content.startswith(b"%PDF")


def test_report_validations(client):
    headers = get_auth_header(client, "report_validations@example.com")
    now = datetime.now(timezone.utc)

    # start_date > end_date
    res_bad_dates = client.post(
        "/api/v1/reports",
        json={
            "start_date": now.isoformat(),
            "end_date": (now - timedelta(days=1)).isoformat(),
        },
        headers=headers,
    )
    assert res_bad_dates.status_code == 422
    assert res_bad_dates.json()["error"]["code"] == "invalid_date_range"

    # range > 365 days
    res_long = client.post(
        "/api/v1/reports",
        json={
            "start_date": (now - timedelta(days=400)).isoformat(),
            "end_date": now.isoformat(),
        },
        headers=headers,
    )
    assert res_long.status_code == 422
    assert res_long.json()["error"]["code"] == "range_exceeded"
