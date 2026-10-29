from datetime import datetime, timezone
from typing import Optional
from fastapi import APIRouter, Depends, Response, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.errors import AppException
from app.core.pagination import PaginatedResponse, PaginationParams
from app.deps import get_current_user
from app.models.signal_map import SignalMap, SignalPoint
from app.models.user import User
from app.schemas.signal_map import (
    PointZoneInfo,
    SignalMapCreate,
    SignalMapDetailResponse,
    SignalMapListItem,
    SignalMapResponse,
    SignalMapSummaryResponse,
    SignalPointCreate,
    SignalPointResponse,
    ZoneCounts,
)
from app.services.thresholds import classify_rssi

router = APIRouter(prefix="/signal-maps", tags=["signal-maps"])


@router.post("", response_model=SignalMapResponse, status_code=status.HTTP_201_CREATED)
def create_signal_map(
    req: SignalMapCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    map_obj = SignalMap(
        user_id=current_user.id,
        name=req.name.strip(),
        description=req.description.strip() if req.description else None,
        ssid=req.ssid.strip() if req.ssid else None,
    )
    db.add(map_obj)
    db.commit()
    db.refresh(map_obj)

    return {
        "id": map_obj.id,
        "name": map_obj.name,
        "description": map_obj.description,
        "ssid": map_obj.ssid,
        "point_count": 0,
        "created_at": map_obj.created_at,
    }


@router.get("", response_model=PaginatedResponse[SignalMapListItem])
def list_signal_maps(
    params: PaginationParams = Depends(),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(SignalMap).filter(SignalMap.user_id == current_user.id)
    total = query.count()
    maps = (
        query.order_by(SignalMap.created_at.desc())
        .offset(params.offset)
        .limit(params.limit)
        .all()
    )

    items = []
    for m in maps:
        pts = m.points
        p_count = len(pts)
        avg_rssi = round(sum(p.rssi_dbm for p in pts) / p_count, 1) if p_count > 0 else None
        items.append(
            SignalMapListItem(
                id=m.id,
                name=m.name,
                description=m.description,
                ssid=m.ssid,
                point_count=p_count,
                avg_rssi_dbm=avg_rssi,
                created_at=m.created_at,
            )
        )

    return PaginatedResponse(
        items=items,
        total=total,
        page=params.page,
        page_size=params.page_size,
    )


@router.get("/{id}", response_model=SignalMapDetailResponse)
def get_signal_map(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    map_obj = (
        db.query(SignalMap)
        .filter(SignalMap.id == id, SignalMap.user_id == current_user.id)
        .first()
    )
    if not map_obj:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Signal map not found",
        )

    pts = sorted(map_obj.points, key=lambda p: p.recorded_at)
    return {
        "id": map_obj.id,
        "name": map_obj.name,
        "description": map_obj.description,
        "ssid": map_obj.ssid,
        "point_count": len(pts),
        "created_at": map_obj.created_at,
        "points": pts,
    }


@router.post("/{id}/points", response_model=SignalPointResponse, status_code=status.HTTP_201_CREATED)
def add_signal_point(
    id: int,
    req: SignalPointCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    map_obj = (
        db.query(SignalMap)
        .filter(SignalMap.id == id, SignalMap.user_id == current_user.id)
        .first()
    )
    if not map_obj:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Signal map not found",
        )

    recorded_at = req.recorded_at or datetime.now(timezone.utc)
    point = SignalPoint(
        map_id=map_obj.id,
        label=req.label.strip(),
        x=req.x,
        y=req.y,
        rssi_dbm=req.rssi_dbm,
        ssid=req.ssid or map_obj.ssid,
        recorded_at=recorded_at,
    )
    db.add(point)
    db.commit()
    db.refresh(point)
    return point


@router.delete("/{id}/points/{point_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_signal_point(
    id: int,
    point_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    map_obj = (
        db.query(SignalMap)
        .filter(SignalMap.id == id, SignalMap.user_id == current_user.id)
        .first()
    )
    if not map_obj:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Signal map not found",
        )

    point = (
        db.query(SignalPoint)
        .filter(SignalPoint.id == point_id, SignalPoint.map_id == map_obj.id)
        .first()
    )
    if not point:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Signal point not found",
        )

    db.delete(point)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_signal_map(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    map_obj = (
        db.query(SignalMap)
        .filter(SignalMap.id == id, SignalMap.user_id == current_user.id)
        .first()
    )
    if not map_obj:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Signal map not found",
        )

    db.delete(map_obj)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get("/{id}/summary", response_model=SignalMapSummaryResponse)
def get_signal_map_summary(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    map_obj = (
        db.query(SignalMap)
        .filter(SignalMap.id == id, SignalMap.user_id == current_user.id)
        .first()
    )
    if not map_obj:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Signal map not found",
        )

    points = map_obj.points
    if not points:
        return {
            "total_points": 0,
            "avg_rssi_dbm": None,
            "strongest_point": None,
            "weakest_point": None,
            "zone_counts": {"strong": 0, "fair": 0, "weak": 0},
            "points": [],
            "verdict": "No signal points recorded yet for this map.",
        }

    point_infos = [
        PointZoneInfo(
            id=p.id,
            label=p.label,
            rssi_dbm=p.rssi_dbm,
            zone=classify_rssi(p.rssi_dbm),
        )
        for p in points
    ]

    strong_count = sum(1 for p in point_infos if p.zone == "strong")
    fair_count = sum(1 for p in point_infos if p.zone == "fair")
    weak_count = sum(1 for p in point_infos if p.zone == "weak")

    strongest = max(point_infos, key=lambda p: p.rssi_dbm)
    weakest = min(point_infos, key=lambda p: p.rssi_dbm)
    avg_rssi = round(sum(p.rssi_dbm for p in points) / len(points), 1)

    # Plain English verdict sentence
    if weak_count == 0 and fair_count == 0:
        verdict = "All recorded locations have excellent Wi-Fi coverage."
    elif weak_count > 0:
        weak_spots = [p.label for p in point_infos if p.zone == "weak"]
        spot_names = ", ".join(weak_spots[:3])
        if strong_count >= fair_count:
            verdict = f"Overall strong coverage, but {weak_count} spot{'s are' if weak_count > 1 else ' is'} weak: {spot_names}."
        else:
            verdict = f"Most of this area has fair coverage, but {weak_count} spot{'s are' if weak_count > 1 else ' is'} weak: {spot_names}."
    else:
        verdict = f"Coverage is generally good across all {len(points)} locations with no weak spots detected."

    return {
        "total_points": len(points),
        "avg_rssi_dbm": avg_rssi,
        "strongest_point": strongest,
        "weakest_point": weakest,
        "zone_counts": ZoneCounts(strong=strong_count, fair=fair_count, weak=weak_count),
        "points": point_infos,
        "verdict": verdict,
    }
