from datetime import datetime, timedelta, timezone
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.models.analysis import AnalysisResult
from app.models.data_usage import DataUsageRecord
from app.models.report import Report
from app.models.security import ConnectedDevice, SecurityScan
from app.models.signal_map import SignalMap, SignalPoint
from app.models.speed_latency import LatencyTest, SpeedTest
from app.models.user import PasswordResetToken, RefreshToken, User
from app.models.wifi import WifiNetwork, WifiScan


def test_full_account_deletion_cascade(client):
    email = "cascade_full@example.com"
    res = client.post(
        "/api/v1/auth/signup",
        json={"name": "Cascade User", "email": email, "password": "Password123!"},
    )
    assert res.status_code == 201
    user_id = res.json()["user"]["id"]
    token = res.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}

    # Populate data in all tables
    # 1. WifiScan + Network
    client.post(
        "/api/v1/wifi/scans",
        json={
            "location_label": "Desk",
            "networks": [{"ssid": "Net1", "signal_dbm": -50, "frequency_mhz": 5180, "channel": 36, "security": "WPA2"}],
        },
        headers=headers,
    )
    # 2. Speed test
    client.post("/api/v1/speed-tests", json={"download_mbps": 100, "upload_mbps": 50, "network_type": "wifi"}, headers=headers)
    # 3. Latency test
    client.post(
        "/api/v1/latency-tests",
        json={"target_host": "8.8.8.8", "avg_ms": 20, "min_ms": 15, "max_ms": 25, "jitter_ms": 2, "packet_loss_percent": 0, "packets_sent": 10, "packets_received": 10},
        headers=headers,
    )
    # 4. Signal Map + Point
    map_res = client.post("/api/v1/signal-maps", json={"name": "Map 1"}, headers=headers)
    map_id = map_res.json()["id"]
    client.post(f"/api/v1/signal-maps/{map_id}/points", json={"label": "P1", "rssi_dbm": -60}, headers=headers)
    # 5. Data usage
    now = datetime.now(timezone.utc)
    client.post(
        "/api/v1/data-usage",
        json={
            "period_start": now.isoformat(),
            "period_end": (now + timedelta(hours=1)).isoformat(),
            "wifi_rx_bytes": 100,
            "wifi_tx_bytes": 100,
            "mobile_rx_bytes": 0,
            "mobile_tx_bytes": 0,
        },
        headers=headers,
    )
    # 6. Security scan
    client.post(
        "/api/v1/security-scans",
        json={"ssid": "SecNet", "security_type": "WPA2", "devices": [{"ip": "192.168.1.1"}]},
        headers=headers,
    )
    # 7. Analysis
    client.post("/api/v1/analysis", json={"days": 7}, headers=headers)
    # 8. Report
    client.post(
        "/api/v1/reports",
        json={"start_date": (now - timedelta(days=1)).isoformat(), "end_date": (now + timedelta(days=1)).isoformat()},
        headers=headers,
    )

    # Now DELETE /users/me
    del_res = client.delete("/api/v1/users/me", headers=headers)
    assert del_res.status_code == 204

    # Verify everything scoped to user_id is now completely removed from DB
    from app.main import app
    db_gen = app.dependency_overrides[get_db]()
    db: Session = next(db_gen)

    assert db.query(User).filter(User.id == user_id).count() == 0
    assert db.query(RefreshToken).filter(RefreshToken.user_id == user_id).count() == 0
    assert db.query(PasswordResetToken).filter(PasswordResetToken.user_id == user_id).count() == 0
    assert db.query(WifiScan).filter(WifiScan.user_id == user_id).count() == 0
    assert db.query(SpeedTest).filter(SpeedTest.user_id == user_id).count() == 0
    assert db.query(LatencyTest).filter(LatencyTest.user_id == user_id).count() == 0
    assert db.query(SignalMap).filter(SignalMap.user_id == user_id).count() == 0
    assert db.query(DataUsageRecord).filter(DataUsageRecord.user_id == user_id).count() == 0
    assert db.query(SecurityScan).filter(SecurityScan.user_id == user_id).count() == 0
    assert db.query(AnalysisResult).filter(AnalysisResult.user_id == user_id).count() == 0
    assert db.query(Report).filter(Report.user_id == user_id).count() == 0
