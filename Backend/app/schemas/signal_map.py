from datetime import datetime
from typing import Dict, List, Optional
from pydantic import AliasChoices, BaseModel, ConfigDict, Field


class SignalMapCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    description: Optional[str] = Field(None, max_length=255)
    ssid: Optional[str] = Field(None, max_length=100)


class SignalPointCreate(BaseModel):
    label: str = Field(..., validation_alias=AliasChoices("label", "location_label", "location_name", "locationLabel"), min_length=1, max_length=100)
    x: Optional[float] = Field(None, ge=0.0, le=1.0)
    y: Optional[float] = Field(None, ge=0.0, le=1.0)
    rssi_dbm: int = Field(..., validation_alias=AliasChoices("rssi_dbm", "signal_dbm", "signalDbm"), ge=-120, le=0)
    ssid: Optional[str] = Field(None, max_length=100)
    recorded_at: Optional[datetime] = None


class SignalPointResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    label: str
    x: Optional[float] = None
    y: Optional[float] = None
    rssi_dbm: int
    ssid: Optional[str] = None
    recorded_at: datetime


class SignalMapResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    description: Optional[str] = None
    ssid: Optional[str] = None
    point_count: int
    created_at: datetime


class SignalMapListItem(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    description: Optional[str] = None
    ssid: Optional[str] = None
    point_count: int
    avg_rssi_dbm: Optional[float] = None
    created_at: datetime


class SignalMapDetailResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    description: Optional[str] = None
    ssid: Optional[str] = None
    point_count: int
    created_at: datetime
    points: List[SignalPointResponse] = Field(default_factory=list)


class PointZoneInfo(BaseModel):
    id: int
    label: str
    rssi_dbm: int
    zone: str  # "strong" | "fair" | "weak"


class ZoneCounts(BaseModel):
    strong: int
    fair: int
    weak: int


class SignalMapSummaryResponse(BaseModel):
    total_points: int
    avg_rssi_dbm: Optional[float] = None
    strongest_point: Optional[PointZoneInfo] = None
    weakest_point: Optional[PointZoneInfo] = None
    zone_counts: ZoneCounts
    points: List[PointZoneInfo] = Field(default_factory=list)
    verdict: str
