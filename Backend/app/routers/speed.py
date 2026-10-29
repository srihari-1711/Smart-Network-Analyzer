from collections import defaultdict
from datetime import datetime, timezone
from typing import Optional
from fastapi import APIRouter, Depends, Query, Response, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.errors import AppException
from app.core.pagination import PaginatedResponse, PaginationParams
from app.deps import get_current_user
from app.models.speed_latency import SpeedTest
from app.models.user import User
from app.schemas.speed import (
    SpeedStatsResponse,
    SpeedTestCreate,
    SpeedTestResponse,
    SpeedTrendPoint,
)

router = APIRouter(prefix="/speed-tests", tags=["speed-tests"])


@router.post("", response_model=SpeedTestResponse, status_code=status.HTTP_201_CREATED)
def create_speed_test(
    req: SpeedTestCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    tested_at = req.tested_at or datetime.now(timezone.utc)
    record = SpeedTest(
        user_id=current_user.id,
        download_mbps=round(req.download_mbps, 2),
        upload_mbps=round(req.upload_mbps, 2),
        ping_ms=round(req.ping_ms, 2) if req.ping_ms is not None else None,
        server=req.server,
        network_type=req.network_type.value,
        ssid=req.ssid,
        tested_at=tested_at,
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    return record


@router.get("/stats", response_model=SpeedStatsResponse)
def get_speed_stats(
    from_date: Optional[datetime] = Query(None),
    to_date: Optional[datetime] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(SpeedTest).filter(SpeedTest.user_id == current_user.id)
    if from_date:
        query = query.filter(SpeedTest.tested_at >= from_date)
    if to_date:
        query = query.filter(SpeedTest.tested_at <= to_date)

    records = query.order_by(SpeedTest.tested_at.asc()).all()
    count = len(records)
    if count == 0:
        return {
            "count": 0,
            "avg_download_mbps": 0.0,
            "avg_upload_mbps": 0.0,
            "max_download_mbps": 0.0,
            "min_download_mbps": 0.0,
            "max_upload_mbps": 0.0,
            "min_upload_mbps": 0.0,
            "best_test_id": None,
            "worst_test_id": None,
            "trend": [],
        }

    downloads = [r.download_mbps for r in records]
    uploads = [r.upload_mbps for r in records]

    # Best test defined by highest download speed
    best_test = max(records, key=lambda r: r.download_mbps)
    worst_test = min(records, key=lambda r: r.download_mbps)

    # Trend grouped by UTC date
    by_date = defaultdict(lambda: {"downloads": [], "uploads": []})
    for r in records:
        dt = r.tested_at
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
        d_str = dt.strftime("%Y-%m-%d")
        by_date[d_str]["downloads"].append(r.download_mbps)
        by_date[d_str]["uploads"].append(r.upload_mbps)

    trend = [
        SpeedTrendPoint(
            date=d_str,
            avg_download_mbps=round(sum(vals["downloads"]) / len(vals["downloads"]), 2),
            avg_upload_mbps=round(sum(vals["uploads"]) / len(vals["uploads"]), 2),
        )
        for d_str, vals in sorted(by_date.items())
    ]

    return {
        "count": count,
        "avg_download_mbps": round(sum(downloads) / count, 2),
        "avg_upload_mbps": round(sum(uploads) / count, 2),
        "max_download_mbps": round(max(downloads), 2),
        "min_download_mbps": round(min(downloads), 2),
        "max_upload_mbps": round(max(uploads), 2),
        "min_upload_mbps": round(min(uploads), 2),
        "best_test_id": best_test.id,
        "worst_test_id": worst_test.id,
        "trend": trend,
    }


@router.get("", response_model=PaginatedResponse[SpeedTestResponse])
def list_speed_tests(
    params: PaginationParams = Depends(),
    from_date: Optional[datetime] = Query(None),
    to_date: Optional[datetime] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(SpeedTest).filter(SpeedTest.user_id == current_user.id)
    if from_date:
        query = query.filter(SpeedTest.tested_at >= from_date)
    if to_date:
        query = query.filter(SpeedTest.tested_at <= to_date)

    total = query.count()
    items = (
        query.order_by(SpeedTest.tested_at.desc())
        .offset(params.offset)
        .limit(params.limit)
        .all()
    )

    return PaginatedResponse(
        items=items,
        total=total,
        page=params.page,
        page_size=params.page_size,
    )


@router.get("/{id}", response_model=SpeedTestResponse)
def get_speed_test(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    record = (
        db.query(SpeedTest)
        .filter(SpeedTest.id == id, SpeedTest.user_id == current_user.id)
        .first()
    )
    if not record:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Speed test record not found",
        )
    return record


@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_speed_test(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    record = (
        db.query(SpeedTest)
        .filter(SpeedTest.id == id, SpeedTest.user_id == current_user.id)
        .first()
    )
    if not record:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Speed test record not found",
        )
    db.delete(record)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
