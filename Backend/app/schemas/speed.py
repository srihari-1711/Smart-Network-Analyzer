from datetime import datetime
from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field


class NetworkType(str, Enum):
    WIFI = "wifi"
    MOBILE = "mobile"


class SpeedTestCreate(BaseModel):
    download_mbps: float = Field(..., ge=0.0)
    upload_mbps: float = Field(..., ge=0.0)
    ping_ms: Optional[float] = Field(None, ge=0.0)
    server: Optional[str] = Field(None, max_length=100)
    network_type: Optional[NetworkType] = Field(default=NetworkType.WIFI)
    ssid: Optional[str] = Field(None, max_length=100)
    tested_at: Optional[datetime] = None


class SpeedTestResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    download_mbps: float
    upload_mbps: float
    ping_ms: Optional[float] = None
    server: Optional[str] = None
    network_type: str
    ssid: Optional[str] = None
    tested_at: datetime
    created_at: datetime


class SpeedTrendPoint(BaseModel):
    date: str  # YYYY-MM-DD
    avg_download_mbps: float
    avg_upload_mbps: float


class SpeedStatsResponse(BaseModel):
    count: int
    avg_download_mbps: float
    avg_upload_mbps: float
    max_download_mbps: float
    min_download_mbps: float
    max_upload_mbps: float
    min_upload_mbps: float
    best_test_id: Optional[int] = None
    worst_test_id: Optional[int] = None
    trend: List[SpeedTrendPoint] = Field(default_factory=list)
