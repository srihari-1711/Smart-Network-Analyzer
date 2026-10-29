from collections import defaultdict
from datetime import datetime, timedelta, timezone
from typing import Optional
from fastapi import APIRouter, Depends, Query, Response, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.pagination import PaginatedResponse, PaginationParams
from app.deps import get_current_user
from app.models.data_usage import DataUsageRecord
from app.models.user import User
from app.schemas.data_usage import (
    DataUsageCreate,
    DataUsageResponse,
    DataUsageSummaryResponse,
    PeriodEnum,
    UsageBucket,
    UsageTotals,
)

router = APIRouter(prefix="/data-usage", tags=["data-usage"])


@router.post("", response_model=DataUsageResponse)
def upsert_data_usage(
    req: DataUsageCreate,
    response: Response,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # Normalize start/end
    start_dt = req.period_start
    end_dt = req.period_end

    existing = (
        db.query(DataUsageRecord)
        .filter(
            DataUsageRecord.user_id == current_user.id,
            DataUsageRecord.period_start == start_dt,
            DataUsageRecord.period_end == end_dt,
        )
        .first()
    )

    if existing:
        existing.wifi_rx_bytes = req.wifi_rx_bytes
        existing.wifi_tx_bytes = req.wifi_tx_bytes
        existing.mobile_rx_bytes = req.mobile_rx_bytes
        existing.mobile_tx_bytes = req.mobile_tx_bytes
        db.commit()
        db.refresh(existing)
        response.status_code = status.HTTP_200_OK
        return existing
    else:
        record = DataUsageRecord(
            user_id=current_user.id,
            period_start=start_dt,
            period_end=end_dt,
            wifi_rx_bytes=req.wifi_rx_bytes,
            wifi_tx_bytes=req.wifi_tx_bytes,
            mobile_rx_bytes=req.mobile_rx_bytes,
            mobile_tx_bytes=req.mobile_tx_bytes,
        )
        db.add(record)
        db.commit()
        db.refresh(record)
        response.status_code = status.HTTP_201_CREATED
        return record


@router.get("/summary", response_model=DataUsageSummaryResponse)
def get_data_usage_summary(
    period: PeriodEnum = PeriodEnum.DAILY,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    now = datetime.now(timezone.utc)
    if period == PeriodEnum.DAILY:
        window_start = now - timedelta(days=14)
        window_days = 14
    elif period == PeriodEnum.WEEKLY:
        window_start = now - timedelta(weeks=8)
        window_days = 56
    else:  # MONTHLY
        window_start = now - timedelta(days=180)
        window_days = 180

    records = (
        db.query(DataUsageRecord)
        .filter(
            DataUsageRecord.user_id == current_user.id,
            DataUsageRecord.period_start >= window_start,
        )
        .order_by(DataUsageRecord.period_start.asc())
        .all()
    )

    if not records:
        return {
            "period": period.value,
            "buckets": [],
            "totals": UsageTotals(wifi_bytes=0, mobile_bytes=0, total_bytes=0),
            "avg_daily_bytes": 0,
            "peak_bucket_label": None,
        }

    # Group into buckets
    buckets_data = defaultdict(lambda: {"wifi": 0, "mobile": 0, "starts": [], "ends": []})

    for r in records:
        p_start = r.period_start
        if p_start.tzinfo is None:
            p_start = p_start.replace(tzinfo=timezone.utc)
        p_end = r.period_end
        if p_end.tzinfo is None:
            p_end = p_end.replace(tzinfo=timezone.utc)

        if period == PeriodEnum.DAILY:
            key = p_start.strftime("%Y-%m-%d")
        elif period == PeriodEnum.WEEKLY:
            year, week, _ = p_start.isocalendar()
            key = f"{year}-W{week:02d}"
        else:
            key = p_start.strftime("%Y-%m")

        wifi = r.wifi_rx_bytes + r.wifi_tx_bytes
        mobile = r.mobile_rx_bytes + r.mobile_tx_bytes
        buckets_data[key]["wifi"] += wifi
        buckets_data[key]["mobile"] += mobile
        buckets_data[key]["starts"].append(p_start)
        buckets_data[key]["ends"].append(p_end)

    bucket_list: list[UsageBucket] = []
    total_wifi = 0
    total_mobile = 0

    for key, data in sorted(buckets_data.items()):
        w = data["wifi"]
        m = data["mobile"]
        tot = w + m
        total_wifi += w
        total_mobile += m
        min_start = min(data["starts"]).isoformat()
        max_end = max(data["ends"]).isoformat()
        bucket_list.append(
            UsageBucket(
                label=key,
                start=min_start,
                end=max_end,
                wifi_bytes=w,
                mobile_bytes=m,
                total_bytes=tot,
            )
        )

    grand_total = total_wifi + total_mobile
    avg_daily = grand_total // max(1, window_days)
    peak_bucket = max(bucket_list, key=lambda b: b.total_bytes) if bucket_list else None

    return {
        "period": period.value,
        "buckets": bucket_list,
        "totals": UsageTotals(
            wifi_bytes=total_wifi,
            mobile_bytes=total_mobile,
            total_bytes=grand_total,
        ),
        "avg_daily_bytes": avg_daily,
        "peak_bucket_label": peak_bucket.label if peak_bucket else None,
    }


@router.get("", response_model=PaginatedResponse[DataUsageResponse])
def list_data_usage(
    params: PaginationParams = Depends(),
    from_date: Optional[datetime] = Query(None),
    to_date: Optional[datetime] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(DataUsageRecord).filter(DataUsageRecord.user_id == current_user.id)
    if from_date:
        query = query.filter(DataUsageRecord.period_start >= from_date)
    if to_date:
        query = query.filter(DataUsageRecord.period_end <= to_date)

    total = query.count()
    items = (
        query.order_by(DataUsageRecord.period_start.desc())
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
