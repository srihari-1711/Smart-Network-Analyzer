from datetime import datetime
from enum import Enum
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field


class PeriodEnum(str, Enum):
    DAILY = "daily"
    WEEKLY = "weekly"
    MONTHLY = "monthly"


class DataUsageCreate(BaseModel):
    period_start: datetime
    period_end: datetime
    wifi_rx_bytes: int = Field(..., ge=0)
    wifi_tx_bytes: int = Field(..., ge=0)
    mobile_rx_bytes: int = Field(..., ge=0)
    mobile_tx_bytes: int = Field(..., ge=0)


class DataUsageResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    period_start: datetime
    period_end: datetime
    wifi_rx_bytes: int
    wifi_tx_bytes: int
    mobile_rx_bytes: int
    mobile_tx_bytes: int
    created_at: datetime


class UsageBucket(BaseModel):
    label: str
    start: str  # ISO string
    end: str    # ISO string
    wifi_bytes: int
    mobile_bytes: int
    total_bytes: int


class UsageTotals(BaseModel):
    wifi_bytes: int
    mobile_bytes: int
    total_bytes: int


class DataUsageSummaryResponse(BaseModel):
    period: str
    buckets: List[UsageBucket] = Field(default_factory=list)
    totals: UsageTotals
    avg_daily_bytes: int
    peak_bucket_label: Optional[str] = None
