from datetime import datetime, timedelta, timezone
from fastapi import APIRouter, Depends, Response, status
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.errors import AppException
from app.core.pagination import PaginatedResponse, PaginationParams
from app.deps import get_current_user
from app.models.data_usage import DataUsageRecord
from app.models.report import Report
from app.models.security import SecurityScan
from app.models.signal_map import SignalMap, SignalPoint
from app.models.speed_latency import LatencyTest, SpeedTest
from app.models.user import User
from app.models.wifi import WifiScan
from app.schemas.report import (
    ReportCreate,
    ReportListItem,
    ReportResponse,
)
from app.services.analysis.llm import get_llm_provider
from app.services.analysis.rules import evaluate_rules
from app.services.pdf import generate_report_pdf
from app.services.thresholds import classify_rssi

router = APIRouter(prefix="/reports", tags=["reports"])


@router.post("", response_model=ReportResponse, status_code=status.HTTP_201_CREATED)
def create_report(
    req: ReportCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if req.start_date > req.end_date:
        raise AppException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            code="invalid_date_range",
            message="start_date must be before or equal to end_date",
        )

    delta = req.end_date - req.start_date
    if delta.days > 365:
        raise AppException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            code="range_exceeded",
            message="Date range cannot exceed 365 days",
        )

    # Gather data across all domains within [start_date, end_date]
    speed_tests = (
        db.query(SpeedTest)
        .filter(
            SpeedTest.user_id == current_user.id,
            SpeedTest.tested_at >= req.start_date,
            SpeedTest.tested_at <= req.end_date,
        )
        .all()
    )

    latency_tests = (
        db.query(LatencyTest)
        .filter(
            LatencyTest.user_id == current_user.id,
            LatencyTest.tested_at >= req.start_date,
            LatencyTest.tested_at <= req.end_date,
        )
        .all()
    )

    wifi_scans = (
        db.query(WifiScan)
        .filter(
            WifiScan.user_id == current_user.id,
            WifiScan.scanned_at >= req.start_date,
            WifiScan.scanned_at <= req.end_date,
        )
        .all()
    )

    usage_records = (
        db.query(DataUsageRecord)
        .filter(
            DataUsageRecord.user_id == current_user.id,
            DataUsageRecord.period_start >= req.start_date,
            DataUsageRecord.period_end <= req.end_date,
        )
        .all()
    )

    sec_scan = (
        db.query(SecurityScan)
        .filter(
            SecurityScan.user_id == current_user.id,
            SecurityScan.scanned_at >= req.start_date,
            SecurityScan.scanned_at <= req.end_date,
        )
        .order_by(SecurityScan.scanned_at.desc())
        .first()
    )

    total_items = len(speed_tests) + len(latency_tests) + len(wifi_scans) + len(usage_records) + (1 if sec_scan else 0)
    if total_items == 0:
        raise AppException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            code="no_data",
            message="No measurements found in the selected date range to generate a report.",
        )

    # Build section data
    sections = {}

    # Speed section
    if speed_tests:
        dls = [t.download_mbps for t in speed_tests]
        uls = [t.upload_mbps for t in speed_tests]
        sections["speed"] = {
            "count": len(speed_tests),
            "avg_download_mbps": round(sum(dls) / len(dls), 2),
            "max_download_mbps": round(max(dls), 2),
            "avg_upload_mbps": round(sum(uls) / len(uls), 2),
            "max_upload_mbps": round(max(uls), 2),
        }
    else:
        sections["speed"] = None

    # Latency section
    if latency_tests:
        avgs = [t.avg_ms for t in latency_tests]
        jitters = [t.jitter_ms for t in latency_tests]
        losses = [t.packet_loss_percent for t in latency_tests]
        sections["latency"] = {
            "count": len(latency_tests),
            "avg_ms": round(sum(avgs) / len(avgs), 2),
            "avg_jitter_ms": round(sum(jitters) / len(jitters), 2),
            "avg_packet_loss_percent": round(sum(losses) / len(losses), 2),
        }
    else:
        sections["latency"] = None

    # Signal & Wi-Fi section
    if wifi_scans:
        all_signals = [n.signal_dbm for s in wifi_scans for n in s.networks]
        sections["signal"] = {
            "scan_count": len(wifi_scans),
            "avg_signal_dbm": round(sum(all_signals) / len(all_signals), 1) if all_signals else None,
            "max_signal_dbm": max(all_signals) if all_signals else None,
        }
    else:
        sections["signal"] = None

    # Data usage section
    if usage_records:
        sections["data_usage"] = {
            "total_wifi_bytes": sum(r.wifi_rx_bytes + r.wifi_tx_bytes for r in usage_records),
            "total_mobile_bytes": sum(r.mobile_rx_bytes + r.mobile_tx_bytes for r in usage_records),
        }
    else:
        sections["data_usage"] = None

    # Security section
    if sec_scan:
        sections["security"] = {
            "security_score": sec_scan.security_score,
            "risk_level": sec_scan.risk_level,
            "security_type": sec_scan.security_type,
        }
    else:
        sections["security"] = None

    # Run analysis engine for this window
    metrics_snapshot = {
        "speed": sections["speed"],
        "latency": sections["latency"],
        "wifi": {"scan_count": len(wifi_scans)},
        "security": sections["security"],
    }
    health_score, summary, rules_issues, rules_recs = evaluate_rules(metrics_snapshot)

    llm = get_llm_provider()
    ai_result = llm.analyze(metrics_snapshot, health_score, rules_issues, rules_recs)
    if ai_result:
        source = "ai"
        final_issues = [i.model_dump() for i in ai_result.issues]
        final_recs = [r.model_dump() for r in ai_result.recommendations]
    else:
        source = "rules"
        final_issues = rules_issues
        final_recs = rules_recs

    sections["issues"] = final_issues
    sections["recommendations"] = final_recs

    start_str = req.start_date.strftime("%Y-%m-%d")
    end_str = req.end_date.strftime("%Y-%m-%d")
    default_title = f"Network Report, {start_str} to {end_str}"
    title = req.title.strip() if req.title and req.title.strip() else default_title

    report = Report(
        user_id=current_user.id,
        title=title,
        start_date=req.start_date,
        end_date=req.end_date,
        sections=sections,
        health_score=health_score,
        source=source,
    )
    db.add(report)
    db.commit()
    db.refresh(report)

    return report


@router.get("", response_model=PaginatedResponse[ReportListItem])
def list_reports(
    params: PaginationParams = Depends(),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(Report).filter(Report.user_id == current_user.id)
    total = query.count()
    items = (
        query.order_by(Report.created_at.desc())
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


@router.get("/{id}", response_model=ReportResponse)
def get_report(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    report = (
        db.query(Report)
        .filter(Report.id == id, Report.user_id == current_user.id)
        .first()
    )
    if not report:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Report not found",
        )
    return report


@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_report(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    report = (
        db.query(Report)
        .filter(Report.id == id, Report.user_id == current_user.id)
        .first()
    )
    if not report:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Report not found",
        )
    db.delete(report)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get("/{id}/export")
def export_report_pdf(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    report = (
        db.query(Report)
        .filter(Report.id == id, Report.user_id == current_user.id)
        .first()
    )
    if not report:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Report not found",
        )

    report_dict = {
        "id": report.id,
        "title": report.title,
        "start_date": report.start_date,
        "end_date": report.end_date,
        "health_score": report.health_score,
        "source": report.source,
        "sections": report.sections,
    }

    pdf_buffer = generate_report_pdf(report_dict)

    headers = {
        "Content-Disposition": f'attachment; filename="network_report_{report.id}.pdf"'
    }
    return StreamingResponse(
        pdf_buffer,
        media_type="application/pdf",
        headers=headers,
    )
