from datetime import datetime
from typing import Any, Dict, Optional
from pydantic import BaseModel, ConfigDict, Field


class ReportCreate(BaseModel):
    title: Optional[str] = Field(None, max_length=200)
    start_date: datetime
    end_date: datetime


class ReportListItem(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    title: str
    start_date: datetime
    end_date: datetime
    health_score: int
    source: str
    created_at: datetime


class ReportResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    title: str
    start_date: datetime
    end_date: datetime
    created_at: datetime
    sections: Dict[str, Any]
    health_score: int
    source: str
