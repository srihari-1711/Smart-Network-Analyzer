from datetime import datetime
from typing import List, Optional
from pydantic import AliasChoices, BaseModel, ConfigDict, Field


class DeviceCreate(BaseModel):
    ip: str = Field(..., validation_alias=AliasChoices("ip", "ip_address"), max_length=50)
    hostname: Optional[str] = Field(None, max_length=100)
    mac: Optional[str] = Field(None, validation_alias=AliasChoices("mac", "mac_address"), max_length=50)


class DeviceResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    ip: str
    hostname: Optional[str] = None
    mac: Optional[str] = None


class SecurityIssue(BaseModel):
    severity: str  # "info" | "low" | "medium" | "high" | "critical"
    title: str
    description: str


class SecurityRecommendation(BaseModel):
    priority: str  # "high" | "medium" | "low"
    title: str
    description: str


class SecurityScanCreate(BaseModel):
    ssid: str = Field(..., validation_alias=AliasChoices("ssid", "network_ssid", "networkSsid"), min_length=1, max_length=100)
    bssid: Optional[str] = Field(None, max_length=50)
    security_type: str = Field(..., validation_alias=AliasChoices("security_type", "securityType"), max_length=50)
    signal_dbm: Optional[int] = Field(None, validation_alias=AliasChoices("signal_dbm", "signalDbm"), ge=-120, le=0)
    gateway_ip: Optional[str] = Field(None, validation_alias=AliasChoices("gateway_ip", "gatewayIp"), max_length=50)
    local_ip: Optional[str] = Field(None, validation_alias=AliasChoices("local_ip", "localIp"), max_length=50)
    devices: List[DeviceCreate] = Field(default_factory=list)
    scanned_at: Optional[datetime] = None


class SecurityScanResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    ssid: str
    security_type: str
    risk_level: str
    security_score: int
    issues: List[SecurityIssue]
    recommendations: List[SecurityRecommendation]
    devices: List[DeviceResponse]
    scanned_at: datetime


class SecurityScanListItem(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    ssid: str
    security_type: str
    risk_level: str
    security_score: int
    scanned_at: datetime
    issue_count: int
    device_count: int
