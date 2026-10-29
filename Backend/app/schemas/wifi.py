from datetime import datetime
from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field


class SecurityType(str, Enum):
    OPEN = "OPEN"
    WEP = "WEP"
    WPA = "WPA"
    WPA2 = "WPA2"
    WPA3 = "WPA3"
    WPA2_WPA3 = "WPA2_WPA3"
    UNKNOWN = "UNKNOWN"


class WifiNetworkCreate(BaseModel):
    ssid: str = Field(..., min_length=1, max_length=100)
    bssid: Optional[str] = Field(None, max_length=50)
    signal_dbm: int = Field(..., ge=-120, le=0)
    frequency_mhz: int = Field(..., ge=2000, le=7500)
    channel: int = Field(..., ge=1, le=233)
    security: SecurityType
    is_connected: bool = False


class WifiScanCreate(BaseModel):
    scanned_at: Optional[datetime] = None
    location_label: Optional[str] = Field(None, max_length=100)
    networks: List[WifiNetworkCreate] = Field(default_factory=list)


class WifiNetworkResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    ssid: str
    bssid: Optional[str] = None
    signal_dbm: int
    frequency_mhz: int
    channel: int
    security: str
    is_connected: bool


class WifiScanResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    scanned_at: datetime
    location_label: Optional[str] = None
    network_count: int
    networks: List[WifiNetworkResponse]


class WifiScanListItem(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    scanned_at: datetime
    location_label: Optional[str] = None
    network_count: int
    strongest_signal_dbm: Optional[int] = None
    connected_ssid: Optional[str] = None
