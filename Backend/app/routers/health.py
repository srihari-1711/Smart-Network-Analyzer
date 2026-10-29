from datetime import datetime, timezone
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter(tags=["health"])


class HealthResponse(BaseModel):
    status: str
    version: str
    time: str


@router.get("/health", response_model=HealthResponse)
def get_health():
    return {
        "status": "ok",
        "version": "1.0.0",
        "time": datetime.now(timezone.utc).isoformat(),
    }
