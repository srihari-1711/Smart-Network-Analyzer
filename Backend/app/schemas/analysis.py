from datetime import datetime
from typing import Any, Dict, List, Optional
from pydantic import BaseModel, ConfigDict, Field


class AnalysisRequest(BaseModel):
    days: int = Field(default=7, ge=1, le=90)


class AnalysisIssue(BaseModel):
    severity: str  # "critical" | "high" | "medium" | "low" | "info"
    category: str  # "speed" | "latency" | "signal" | "interference" | "security" | "usage" | "general"
    title: str
    description: str


class AnalysisRecommendation(BaseModel):
    priority: str  # "high" | "medium" | "low"
    title: str
    description: str


class AnalysisResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    source: str  # "ai" | "rules"
    health_score: int
    summary: str
    issues: List[AnalysisIssue]
    recommendations: List[AnalysisRecommendation]
    metrics_snapshot: Dict[str, Any]
    created_at: datetime


class AnalysisListItem(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    source: str
    health_score: int
    summary: str
    issue_count: int
    created_at: datetime
