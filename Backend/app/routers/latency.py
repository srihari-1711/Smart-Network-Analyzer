from collections import defaultdict
from datetime import datetime, timezone
from typing import Optional
from fastapi import APIRouter, Depends, Query, Response, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.errors import AppException
from app.core.pagination import PaginatedResponse, PaginationParams
from app.deps import get_current_user
from app.models.speed_latency import LatencyTest
from app.models.user import User
from app.schemas.latency import (
    LatencyStatsResponse,
    LatencyTestCreate,
    LatencyTestResponse,
    LatencyTrendPoint,
)
from app.services.thresholds import rate_latency

router = APIRouter(prefix="/latency-tests", tags=["latency-tests"])


@router.post("", response_model=LatencyTestResponse, status_code=status.HTTP_201_CREATED)
def create_latency_test(
    req: LatencyTestCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    tested_at = req.tested_at or datetime.now(timezone.utc)
    record = LatencyTest(
        user_id=current_user.id,
        target_host=req.target_host.strip(),
        avg_ms=round(req.avg_ms, 2),
        min_ms=round(req.min_ms, 2),
        max_ms=round(req.max_ms, 2),
        jitter_ms=round(req.jitter_ms, 2),
        packet_loss_percent=round(req.packet_loss_percent, 2),
        packets_sent=req.packets_sent,
        packets_received=req.packets_received,
        ssid=req.ssid,
        tested_at=tested_at,
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    return record


@router.get("/stats", response_model=LatencyStatsResponse)
def get_latency_stats(
    from_date: Optional[datetime] = Query(None),
    to_date: Optional[datetime] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(LatencyTest).filter(LatencyTest.user_id == current_user.id)
    if from_date:
        query = query.filter(LatencyTest.tested_at >= from_date)
    if to_date:
        query = query.filter(LatencyTest.tested_at <= to_date)

    records = query.order_by(LatencyTest.tested_at.asc()).all()
    count = len(records)
    if count == 0:
        return {
            "count": 0,
            "avg_ms": 0.0,
            "min_ms": 0.0,
            "max_ms": 0.0,
            "avg_jitter_ms": 0.0,
            "avg_packet_loss_percent": 0.0,
            "rating": "poor",
            "trend": [],
        }

    avgs = [r.avg_ms for r in records]
    mins = [r.min_ms for r in records]
    maxs = [r.max_ms for r in records]
    jitters = [r.jitter_ms for r in records]
    losses = [r.packet_loss_percent for r in records]

    avg_ms = round(sum(avgs) / count, 2)
    avg_loss = round(sum(losses) / count, 2)
    rating = rate_latency(avg_ms, avg_loss)

    by_date = defaultdict(list)
    for r in records:
        dt = r.tested_at
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
        d_str = dt.strftime("%Y-%m-%d")
        by_date[d_str].append(r.avg_ms)

    trend = [
        LatencyTrendPoint(
            date=d_str,
            avg_ms=round(sum(vals) / len(vals), 2),
        )
        for d_str, vals in sorted(by_date.items())
    ]

    return {
        "count": count,
        "avg_ms": avg_ms,
        "min_ms": round(min(mins), 2),
        "max_ms": round(max(maxs), 2),
        "avg_jitter_ms": round(sum(jitters) / count, 2),
        "avg_packet_loss_percent": avg_loss,
        "rating": rating,
        "trend": trend,
    }


@router.get("", response_model=PaginatedResponse[LatencyTestResponse])
def list_latency_tests(
    params: PaginationParams = Depends(),
    from_date: Optional[datetime] = Query(None),
    to_date: Optional[datetime] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(LatencyTest).filter(LatencyTest.user_id == current_user.id)
    if from_date:
        query = query.filter(LatencyTest.tested_at >= from_date)
    if to_date:
        query = query.filter(LatencyTest.tested_at <= to_date)

    total = query.count()
    items = (
        query.order_by(LatencyTest.tested_at.desc())
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


@router.get("/{id}", response_model=LatencyTestResponse)
def get_latency_test(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    record = (
        db.query(LatencyTest)
        .filter(LatencyTest.id == id, LatencyTest.user_id == current_user.id)
        .first()
    )
    if not record:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Latency test record not found",
        )
    return record


@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_latency_test(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    record = (
        db.query(LatencyTest)
        .filter(LatencyTest.id == id, LatencyTest.user_id == current_user.id)
        .first()
    )
    if not record:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Latency test record not found",
        )
    db.delete(record)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
