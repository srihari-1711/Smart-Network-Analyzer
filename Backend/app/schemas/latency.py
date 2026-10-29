from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field


class LatencyTestCreate(BaseModel):
    target_host: str = Field(..., min_length=1, max_length=100)
    avg_ms: float = Field(..., ge=0.0)
    min_ms: float = Field(..., ge=0.0)
    max_ms: float = Field(..., ge=0.0)
    jitter_ms: float = Field(..., ge=0.0)
    packet_loss_percent: float = Field(..., ge=0.0, le=100.0)
    packets_sent: int = Field(..., ge=1)
    packets_received: int = Field(..., ge=0)
    ssid: Optional[str] = Field(None, max_length=100)
    tested_at: Optional[datetime] = None


class LatencyTestResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    target_host: str
    avg_ms: float
    min_ms: float
    max_ms: float
    jitter_ms: float
    packet_loss_percent: float
    packets_sent: int
    packets_received: int
    ssid: Optional[str] = None
    tested_at: datetime
    created_at: datetime


class LatencyTrendPoint(BaseModel):
    date: str  # YYYY-MM-DD
    avg_ms: float


class LatencyStatsResponse(BaseModel):
    count: int
    avg_ms: float
    min_ms: float
    max_ms: float
    avg_jitter_ms: float
    avg_packet_loss_percent: float
    rating: str  # "excellent" | "good" | "fair" | "poor"
    trend: List[LatencyTrendPoint] = Field(default_factory=list)
