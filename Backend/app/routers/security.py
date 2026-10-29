from datetime import datetime, timezone
from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.errors import AppException
from app.core.pagination import PaginatedResponse, PaginationParams
from app.deps import get_current_user
from app.models.security import ConnectedDevice, SecurityScan
from app.models.user import User
from app.schemas.security import (
    DeviceResponse,
    SecurityScanCreate,
    SecurityScanListItem,
    SecurityScanResponse,
)
from app.services.security_rules import evaluate_security

router = APIRouter(prefix="/security-scans", tags=["security-scans"])


@router.post("", response_model=SecurityScanResponse, status_code=status.HTTP_201_CREATED)
def create_security_scan(
    req: SecurityScanCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    scanned_at = req.scanned_at or datetime.now(timezone.utc)
    raw_devices = [d.model_dump() for d in req.devices]

    risk_level, score, issues, recs = evaluate_security(
        ssid=req.ssid,
        security_type=req.security_type,
        signal_dbm=req.signal_dbm,
        gateway_ip=req.gateway_ip,
        local_ip=req.local_ip,
        devices=raw_devices,
    )

    scan = SecurityScan(
        user_id=current_user.id,
        ssid=req.ssid.strip(),
        bssid=req.bssid.strip() if req.bssid else None,
        security_type=req.security_type.strip(),
        signal_dbm=req.signal_dbm,
        gateway_ip=req.gateway_ip.strip() if req.gateway_ip else None,
        local_ip=req.local_ip.strip() if req.local_ip else None,
        risk_level=risk_level,
        security_score=score,
        issues=issues,
        recommendations=recs,
        scanned_at=scanned_at,
    )
    db.add(scan)
    db.commit()
    db.refresh(scan)

    saved_devices = []
    for d in req.devices:
        dev = ConnectedDevice(
            scan_id=scan.id,
            ip=d.ip.strip(),
            hostname=d.hostname.strip() if d.hostname else None,
            mac=d.mac.strip() if d.mac else None,
        )
        db.add(dev)
        saved_devices.append(dev)

    db.commit()
    db.refresh(scan)

    return {
        "id": scan.id,
        "ssid": scan.ssid,
        "security_type": scan.security_type,
        "risk_level": scan.risk_level,
        "security_score": scan.security_score,
        "issues": scan.issues,
        "recommendations": scan.recommendations,
        "devices": saved_devices,
        "scanned_at": scan.scanned_at,
    }


@router.get("", response_model=PaginatedResponse[SecurityScanListItem])
def list_security_scans(
    params: PaginationParams = Depends(),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(SecurityScan).filter(SecurityScan.user_id == current_user.id)
    total = query.count()
    scans = (
        query.order_by(SecurityScan.scanned_at.desc())
        .offset(params.offset)
        .limit(params.limit)
        .all()
    )

    items = [
        SecurityScanListItem(
            id=s.id,
            ssid=s.ssid,
            security_type=s.security_type,
            risk_level=s.risk_level,
            security_score=s.security_score,
            scanned_at=s.scanned_at,
            issue_count=len(s.issues) if s.issues else 0,
            device_count=len(s.devices) if s.devices else 0,
        )
        for s in scans
    ]

    return PaginatedResponse(
        items=items,
        total=total,
        page=params.page,
        page_size=params.page_size,
    )


@router.get("/{id}", response_model=SecurityScanResponse)
def get_security_scan(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    scan = (
        db.query(SecurityScan)
        .filter(SecurityScan.id == id, SecurityScan.user_id == current_user.id)
        .first()
    )
    if not scan:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Security scan not found",
        )

    return {
        "id": scan.id,
        "ssid": scan.ssid,
        "security_type": scan.security_type,
        "risk_level": scan.risk_level,
        "security_score": scan.security_score,
        "issues": scan.issues,
        "recommendations": scan.recommendations,
        "devices": scan.devices,
        "scanned_at": scan.scanned_at,
    }
