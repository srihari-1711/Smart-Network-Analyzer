import os
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path

# Ensure Backend root is in sys.path
backend_root = Path(__file__).resolve().parent.parent
if str(backend_root) not in sys.path:
    sys.path.insert(0, str(backend_root))

from app.core.database import Base, SessionLocal, engine
from app.core.security import hash_password
from app.models.analysis import AnalysisResult
from app.models.data_usage import DataUsageRecord
from app.models.report import Report
from app.models.security import ConnectedDevice, SecurityScan
from app.models.signal_map import SignalMap, SignalPoint
from app.models.speed_latency import LatencyTest, SpeedTest
from app.models.user import User
from app.models.wifi import WifiNetwork, WifiScan
from app.services.security_rules import evaluate_security


def seed():
    print("Starting database seeding...")
    db = SessionLocal()

    # Ensure tables exist
    Base.metadata.create_all(bind=engine)

    demo_email = "demo@example.com"
    existing_user = db.query(User).filter(User.email == demo_email).first()
    if existing_user:
        print(f"Cleaning up existing demo user: {demo_email}")
        db.delete(existing_user)
        db.commit()

    print("Creating demo user: demo@example.com / Demo@1234")
    user = User(
        name="Alex Mercer",
        email=demo_email,
        password_hash=hash_password("Demo@1234"),
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    now = datetime.now(timezone.utc)

    # 1. Seed 3 weeks of speed tests (approx 30 tests)
    print("Seeding speed tests...")
    speeds = [
        (95.2, 38.4, 18.0, "wifi", "HomeFiber_5G"),
        (88.0, 35.1, 21.0, "wifi", "HomeFiber_5G"),
        (42.5, 12.0, 32.0, "wifi", "HomeFiber_2.4G"),
        (102.4, 41.0, 15.0, "wifi", "HomeFiber_5G"),
        (98.1, 39.5, 16.0, "wifi", "HomeFiber_5G"),
        (15.2, 4.0, 65.0, "wifi", "CoffeeShop_Guest"),
        (75.0, 25.0, 22.0, "mobile", None),
        (82.4, 30.1, 25.0, "mobile", None),
        (110.5, 45.0, 14.0, "wifi", "HomeFiber_5G"),
        (105.0, 42.0, 16.0, "wifi", "HomeFiber_5G"),
        (92.0, 36.0, 19.0, "wifi", "HomeFiber_5G"),
        (35.0, 10.0, 45.0, "wifi", "HomeFiber_2.4G"),
        (115.0, 48.0, 13.0, "wifi", "HomeFiber_5G"),
        (99.0, 39.0, 17.0, "wifi", "HomeFiber_5G"),
        (94.0, 37.0, 18.0, "wifi", "HomeFiber_5G"),
    ]
    for i, s in enumerate(speeds):
        t_time = now - timedelta(days=21 - (i * 1.4), hours=(i % 5))
        st = SpeedTest(
            user_id=user.id,
            download_mbps=s[0],
            upload_mbps=s[1],
            ping_ms=s[2],
            server="Cloudflare - Mumbai",
            network_type=s[3],
            ssid=s[4],
            tested_at=t_time,
        )
        db.add(st)

    # 2. Seed 3 weeks of latency tests
    print("Seeding latency tests...")
    latencies = [
        (16.5, 14.0, 22.0, 1.8, 0.0, 20, 20),
        (18.2, 15.0, 28.0, 2.2, 0.0, 20, 20),
        (25.0, 18.0, 38.0, 4.5, 0.0, 20, 20),
        (95.0, 45.0, 180.0, 32.0, 4.0, 25, 24),  # bad latency day
        (15.8, 13.0, 20.0, 1.5, 0.0, 20, 20),
        (17.0, 14.0, 24.0, 2.0, 0.0, 20, 20),
        (16.2, 13.5, 21.0, 1.6, 0.0, 20, 20),
        (110.0, 60.0, 210.0, 45.0, 5.0, 20, 19), # bad latency day
        (15.0, 12.0, 19.0, 1.2, 0.0, 20, 20),
        (16.0, 13.0, 20.0, 1.4, 0.0, 20, 20),
    ]
    for i, lat in enumerate(latencies):
        t_time = now - timedelta(days=20 - (i * 2), hours=(i % 4))
        lt = LatencyTest(
            user_id=user.id,
            target_host="8.8.8.8",
            avg_ms=lat[0],
            min_ms=lat[1],
            max_ms=lat[2],
            jitter_ms=lat[3],
            packet_loss_percent=lat[4],
            packets_sent=lat[5],
            packets_received=lat[6],
            ssid="HomeFiber_5G",
            tested_at=t_time,
        )
        db.add(lt)

    # 3. Seed Wi-Fi scans
    print("Seeding Wi-Fi scans...")
    scan1 = WifiScan(
        user_id=user.id,
        location_label="Home Office",
        scanned_at=now - timedelta(days=2),
    )
    db.add(scan1)
    db.commit()
    db.refresh(scan1)

    networks_data = [
        ("HomeFiber_5G", "18:e8:29:44:a1:02", -48, 5180, 36, "WPA3", True),
        ("HomeFiber_2.4G", "18:e8:29:44:a1:01", -52, 2412, 1, "WPA2", False),
        ("Neighbor_Net1", "bc:4d:fb:11:22:33", -74, 2412, 1, "WPA2", False),
        ("Neighbor_Net2", "00:1a:2b:3c:4d:5e", -78, 2412, 1, "WPA2", False),
        ("Airtel_Broadband", "aa:bb:cc:dd:ee:ff", -68, 2437, 6, "WPA2", False),
        ("JioFiber_99", "12:34:56:78:9a:bc", -82, 2462, 11, "WPA2", False),
    ]
    for nd in networks_data:
        wn = WifiNetwork(
            scan_id=scan1.id,
            ssid=nd[0],
            bssid=nd[1],
            signal_dbm=nd[2],
            frequency_mhz=nd[3],
            channel=nd[4],
            security=nd[5],
            is_connected=nd[6],
        )
        db.add(wn)

    # 4. Seed Signal Map
    print("Seeding signal map...")
    sig_map = SignalMap(
        user_id=user.id,
        name="Main Residence (3BHK)",
        description="Ground floor flat Wi-Fi coverage map",
        ssid="HomeFiber_5G",
        created_at=now - timedelta(days=10),
    )
    db.add(sig_map)
    db.commit()
    db.refresh(sig_map)

    points_data = [
        ("Living Room (Router)", 0.20, 0.30, -42),
        ("Master Bedroom", 0.75, 0.25, -58),
        ("Kids Study Room", 0.60, 0.70, -66),
        ("Kitchen", 0.35, 0.85, -72),
        ("Balcony Garden", 0.90, 0.90, -82),  # weak spot
    ]
    for pd in points_data:
        pt = SignalPoint(
            map_id=sig_map.id,
            label=pd[0],
            x=pd[1],
            y=pd[2],
            rssi_dbm=pd[3],
            ssid="HomeFiber_5G",
            recorded_at=now - timedelta(days=10, hours=2),
        )
        db.add(pt)

    # 5. Seed 21 days of Data Usage records
    print("Seeding data usage records...")
    for day in range(21, 0, -1):
        p_start = (now - timedelta(days=day)).replace(hour=0, minute=0, second=0, microsecond=0)
        p_end = (now - timedelta(days=day)).replace(hour=23, minute=59, second=59, microsecond=0)
        
        # 1.5GB to 4GB wifi, 300MB to 800MB mobile
        w_rx = (1500 + (day * 80) % 2000) * 1024 * 1024
        w_tx = (300 + (day * 30) % 500) * 1024 * 1024
        m_rx = (400 + (day * 20) % 300) * 1024 * 1024
        m_tx = (50 + (day * 10) % 100) * 1024 * 1024

        rec = DataUsageRecord(
            user_id=user.id,
            period_start=p_start,
            period_end=p_end,
            wifi_rx_bytes=w_rx,
            wifi_tx_bytes=w_tx,
            mobile_rx_bytes=m_rx,
            mobile_tx_bytes=m_tx,
        )
        db.add(rec)

    # 6. Seed Security Scan
    print("Seeding security scan...")
    devs = [
        {"ip": "192.168.1.1", "hostname": "gateway.home", "mac": "18:e8:29:44:a1:00"},
        {"ip": "192.168.1.102", "hostname": "macbook-pro.home", "mac": "f0:18:98:33:11:aa"},
        {"ip": "192.168.1.105", "hostname": "pixel-phone.home", "mac": "ac:22:0b:55:77:cc"},
        {"ip": "192.168.1.110", "hostname": "samsung-smart-tv.home", "mac": "d4:9a:20:88:99:ee"},
    ]
    risk, score, issues, recs = evaluate_security(
        ssid="HomeFiber_5G",
        security_type="WPA2",
        signal_dbm=-48,
        gateway_ip="192.168.1.1",
        local_ip="192.168.1.105",
        devices=devs,
    )
    sec_scan = SecurityScan(
        user_id=user.id,
        ssid="HomeFiber_5G",
        bssid="18:e8:29:44:a1:02",
        security_type="WPA2",
        signal_dbm=-48,
        gateway_ip="192.168.1.1",
        local_ip="192.168.1.105",
        risk_level=risk,
        security_score=score,
        issues=issues,
        recommendations=recs,
        scanned_at=now - timedelta(days=1),
    )
    db.add(sec_scan)
    db.commit()
    db.refresh(sec_scan)

    for d in devs:
        cd = ConnectedDevice(
            scan_id=sec_scan.id,
            ip=d["ip"],
            hostname=d["hostname"],
            mac=d["mac"],
        )
        db.add(cd)

    # 7. Seed Analysis Result
    print("Seeding analysis result...")
    analysis = AnalysisResult(
        user_id=user.id,
        source="rules",
        health_score=88,
        summary="Your network health score is 88/100. Overall internet throughput and latency are excellent. One weak coverage area was detected in Balcony Garden.",
        issues=[
            {
                "severity": "medium",
                "category": "signal",
                "title": "Weak Signal in Balcony Garden",
                "description": "Signal drops below -75 dBm in peripheral rooms.",
            },
            {
                "severity": "info",
                "category": "speed",
                "title": "Minor Evening Speed Variance",
                "description": "Throughput drops approximately 15% between 8 PM and 10 PM.",
            },
        ],
        recommendations=[
            {
                "priority": "medium",
                "title": "Relocate Router Closer to Central Living Space",
                "description": "Placing the router at an elevated height in the living room improves signal across all bedrooms.",
            },
            {
                "priority": "low",
                "title": "Switch 2.4 GHz Channel to 11",
                "description": "Neighboring networks are crowding Channel 1.",
            },
        ],
        metrics_snapshot={
            "speed": {"avg_download_mbps": 94.5, "avg_upload_mbps": 38.2},
            "latency": {"avg_ms": 16.5, "avg_jitter_ms": 2.1, "avg_packet_loss_percent": 0.0},
        },
        created_at=now - timedelta(days=1),
    )
    db.add(analysis)

    # 8. Seed Report
    print("Seeding report...")
    report = Report(
        user_id=user.id,
        title="Bi-Weekly Home Network Audit",
        start_date=now - timedelta(days=14),
        end_date=now,
        sections={
            "speed": {"count": 15, "avg_download_mbps": 94.5, "avg_upload_mbps": 38.2},
            "latency": {"count": 10, "avg_ms": 16.5, "avg_jitter_ms": 2.1, "avg_packet_loss_percent": 0.0},
            "signal": {"scan_count": 1, "avg_signal_dbm": -52.0},
            "data_usage": {"total_wifi_bytes": 48234496000, "total_mobile_bytes": 10485760000},
            "security": {"security_score": 100, "risk_level": "low"},
            "issues": analysis.issues,
            "recommendations": analysis.recommendations,
        },
        health_score=88,
        source="rules",
        created_at=now - timedelta(days=1),
    )
    db.add(report)

    db.commit()
    print("Seeding successfully completed! Demo credentials: demo@example.com / Demo@1234")
    db.close()


if __name__ == "__main__":
    seed()
