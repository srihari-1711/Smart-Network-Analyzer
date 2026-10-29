from datetime import datetime
from typing import Any, Dict, List, Optional
from pydantic import BaseModel
from app.schemas.latency import LatencyTestResponse
from app.schemas.speed import SpeedTestResponse


class WifiScanSummary(BaseModel):
    id: int
    scanned_at: datetime
    location_label: Optional[str] = None
    network_count: int
    strongest_signal_dbm: Optional[int] = None
    connected_ssid: Optional[str] = None


class LatestAnalysisSummary(BaseModel):
    id: int
    health_score: int
    summary: str
    issues: List[Any]
    source: str
    created_at: datetime


class DashboardCounts(BaseModel):
    speed_tests: int
    latency_tests: int
    wifi_scans: int
    signal_maps: int
    reports: int


class DashboardResponse(BaseModel):
    latest_speed_test: Optional[SpeedTestResponse] = None
    latest_latency_test: Optional[LatencyTestResponse] = None
    latest_wifi_scan_summary: Optional[WifiScanSummary] = None
    current_signal_dbm: Optional[int] = None
    latest_analysis: Optional[LatestAnalysisSummary] = None
    counts: DashboardCounts
