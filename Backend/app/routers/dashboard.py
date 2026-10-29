from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.deps import get_current_user
from app.models.analysis import AnalysisResult
from app.models.report import Report
from app.models.signal_map import SignalMap
from app.models.speed_latency import LatencyTest, SpeedTest
from app.models.user import User
from app.models.wifi import WifiScan
from app.schemas.dashboard import (
    DashboardCounts,
    DashboardResponse,
    LatestAnalysisSummary,
    WifiScanSummary,
)

router = APIRouter(prefix="/dashboard", tags=["dashboard"])


@router.get("", response_model=DashboardResponse)
def get_dashboard(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # 1. Latest items
    latest_speed = (
        db.query(SpeedTest)
        .filter(SpeedTest.user_id == current_user.id)
        .order_by(SpeedTest.tested_at.desc())
        .first()
    )

    latest_latency = (
        db.query(LatencyTest)
        .filter(LatencyTest.user_id == current_user.id)
        .order_by(LatencyTest.tested_at.desc())
        .first()
    )

    latest_scan = (
        db.query(WifiScan)
        .filter(WifiScan.user_id == current_user.id)
        .order_by(WifiScan.scanned_at.desc())
        .first()
    )

    scan_summary = None
    current_signal = None
    if latest_scan:
        nets = latest_scan.networks
        strongest = max(n.signal_dbm for n in nets) if nets else None
        conn_net = next((n for n in nets if n.is_connected), None)
        conn_ssid = conn_net.ssid if conn_net else None
        current_signal = conn_net.signal_dbm if conn_net else strongest

        scan_summary = WifiScanSummary(
            id=latest_scan.id,
            scanned_at=latest_scan.scanned_at,
            location_label=latest_scan.location_label,
            network_count=len(nets),
            strongest_signal_dbm=strongest,
            connected_ssid=conn_ssid,
        )

    latest_analysis_obj = (
        db.query(AnalysisResult)
        .filter(AnalysisResult.user_id == current_user.id)
        .order_by(AnalysisResult.created_at.desc())
        .first()
    )
    latest_analysis = (
        LatestAnalysisSummary(
            id=latest_analysis_obj.id,
            health_score=latest_analysis_obj.health_score,
            summary=latest_analysis_obj.summary,
            issues=latest_analysis_obj.issues,
            source=latest_analysis_obj.source,
            created_at=latest_analysis_obj.created_at,
        )
        if latest_analysis_obj
        else None
    )

    # 2. Counts
    c_speed = db.query(SpeedTest).filter(SpeedTest.user_id == current_user.id).count()
    c_latency = db.query(LatencyTest).filter(LatencyTest.user_id == current_user.id).count()
    c_scans = db.query(WifiScan).filter(WifiScan.user_id == current_user.id).count()
    c_maps = db.query(SignalMap).filter(SignalMap.user_id == current_user.id).count()
    c_reports = db.query(Report).filter(Report.user_id == current_user.id).count()

    counts = DashboardCounts(
        speed_tests=c_speed,
        latency_tests=c_latency,
        wifi_scans=c_scans,
        signal_maps=c_maps,
        reports=c_reports,
    )

    return DashboardResponse(
        latest_speed_test=latest_speed,
        latest_latency_test=latest_latency,
        latest_wifi_scan_summary=scan_summary,
        current_signal_dbm=current_signal,
        latest_analysis=latest_analysis,
        counts=counts,
    )
