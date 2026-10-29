from datetime import datetime, timezone
from typing import Optional
from fastapi import APIRouter, Depends, Query, Response, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.errors import AppException
from app.core.pagination import PaginatedResponse, PaginationParams
from app.deps import get_current_user
from app.models.user import User
from app.models.wifi import WifiNetwork, WifiScan
from app.schemas.wifi import (
    WifiNetworkResponse,
    WifiScanCreate,
    WifiScanListItem,
    WifiScanResponse,
)

router = APIRouter(prefix="/wifi", tags=["wifi"])


@router.post("/scans", response_model=WifiScanResponse, status_code=status.HTTP_201_CREATED)
def create_wifi_scan(
    req: WifiScanCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    scanned_at = req.scanned_at or datetime.now(timezone.utc)
    scan = WifiScan(
        user_id=current_user.id,
        scanned_at=scanned_at,
        location_label=req.location_label,
    )
    db.add(scan)
    db.commit()
    db.refresh(scan)

    networks = []
    for net_data in req.networks:
        net = WifiNetwork(
            scan_id=scan.id,
            ssid=net_data.ssid,
            bssid=net_data.bssid,
            signal_dbm=net_data.signal_dbm,
            frequency_mhz=net_data.frequency_mhz,
            channel=net_data.channel,
            security=net_data.security.value,
            is_connected=net_data.is_connected,
        )
        db.add(net)
        networks.append(net)
    
    db.commit()
    db.refresh(scan)

    return {
        "id": scan.id,
        "scanned_at": scan.scanned_at,
        "location_label": scan.location_label,
        "network_count": len(networks),
        "networks": networks,
    }


@router.get("/scans", response_model=PaginatedResponse[WifiScanListItem])
def list_wifi_scans(
    params: PaginationParams = Depends(),
    from_date: Optional[datetime] = Query(None),
    to_date: Optional[datetime] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(WifiScan).filter(WifiScan.user_id == current_user.id)
    if from_date:
        query = query.filter(WifiScan.scanned_at >= from_date)
    if to_date:
        query = query.filter(WifiScan.scanned_at <= to_date)

    total = query.count()
    scans = (
        query.order_by(WifiScan.scanned_at.desc())
        .offset(params.offset)
        .limit(params.limit)
        .all()
    )

    items = []
    for scan in scans:
        network_count = len(scan.networks)
        strongest = (
            max(n.signal_dbm for n in scan.networks) if scan.networks else None
        )
        connected = next((n.ssid for n in scan.networks if n.is_connected), None)
        items.append(
            WifiScanListItem(
                id=scan.id,
                scanned_at=scan.scanned_at,
                location_label=scan.location_label,
                network_count=network_count,
                strongest_signal_dbm=strongest,
                connected_ssid=connected,
            )
        )

    return PaginatedResponse(
        items=items,
        total=total,
        page=params.page,
        page_size=params.page_size,
    )


@router.get("/scans/{id}", response_model=WifiScanResponse)
def get_wifi_scan(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    scan = (
        db.query(WifiScan)
        .filter(WifiScan.id == id, WifiScan.user_id == current_user.id)
        .first()
    )
    if not scan:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Wi-Fi scan not found",
        )

    return {
        "id": scan.id,
        "scanned_at": scan.scanned_at,
        "location_label": scan.location_label,
        "network_count": len(scan.networks),
        "networks": scan.networks,
    }


@router.delete("/scans/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_wifi_scan(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    scan = (
        db.query(WifiScan)
        .filter(WifiScan.id == id, WifiScan.user_id == current_user.id)
        .first()
    )
    if not scan:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Wi-Fi scan not found",
        )

    db.delete(scan)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
