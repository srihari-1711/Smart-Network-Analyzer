from datetime import datetime, timezone
from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.errors import AppException
from app.core.pagination import PaginatedResponse, PaginationParams
from app.deps import get_current_user
from app.models.analysis import AnalysisResult
from app.models.user import User
from app.schemas.analysis import (
    AnalysisListItem,
    AnalysisRequest,
    AnalysisResponse,
)
from app.services.analysis.llm import get_llm_provider
from app.services.analysis.metrics import aggregate_metrics
from app.services.analysis.rules import evaluate_rules

router = APIRouter(prefix="/analysis", tags=["analysis"])


@router.post("", response_model=AnalysisResponse, status_code=status.HTTP_201_CREATED)
def run_analysis(
    req: AnalysisRequest = AnalysisRequest(),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # 1. Aggregate metrics
    metrics = aggregate_metrics(db, current_user.id, days=req.days)
    if metrics["_meta"]["total_data_points"] == 0:
        raise AppException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            code="no_data",
            message=f"No network measurements found within the last {req.days} days. Please run a speed test, latency test, or Wi-Fi scan first.",
        )

    clean_snapshot = {k: v for k, v in metrics.items() if not k.startswith("_")}

    # 2. Deterministic rules engine
    health_score, summary, rules_issues, rules_recs = evaluate_rules(metrics)

    # 3. LLM provider (with fallback)
    llm = get_llm_provider()
    ai_result = llm.analyze(clean_snapshot, health_score, rules_issues, rules_recs)

    if ai_result:
        source = "ai"
        final_summary = ai_result.summary
        final_issues = [i.model_dump() for i in ai_result.issues]
        final_recs = [r.model_dump() for r in ai_result.recommendations]

        # Preserve critical and high severity findings
        existing_titles = {i["title"] for i in final_issues}
        for rule_issue in rules_issues:
            if rule_issue["severity"] in ("critical", "high") and rule_issue["title"] not in existing_titles:
                final_issues.append(rule_issue)
    else:
        source = "rules"
        final_summary = summary
        final_issues = rules_issues
        final_recs = rules_recs

    # 4. Persist analysis
    analysis = AnalysisResult(
        user_id=current_user.id,
        source=source,
        health_score=health_score,
        summary=final_summary,
        issues=final_issues,
        recommendations=final_recs,
        metrics_snapshot=clean_snapshot,
    )
    db.add(analysis)
    db.commit()
    db.refresh(analysis)

    return analysis


@router.get("", response_model=PaginatedResponse[AnalysisListItem])
def list_analyses(
    params: PaginationParams = Depends(),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(AnalysisResult).filter(AnalysisResult.user_id == current_user.id)
    total = query.count()
    items = (
        query.order_by(AnalysisResult.created_at.desc())
        .offset(params.offset)
        .limit(params.limit)
        .all()
    )

    list_items = [
        AnalysisListItem(
            id=a.id,
            source=a.source,
            health_score=a.health_score,
            summary=a.summary,
            issue_count=len(a.issues) if a.issues else 0,
            created_at=a.created_at,
        )
        for a in items
    ]

    return PaginatedResponse(
        items=list_items,
        total=total,
        page=params.page,
        page_size=params.page_size,
    )


@router.get("/{id}", response_model=AnalysisResponse)
def get_analysis(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    analysis = (
        db.query(AnalysisResult)
        .filter(AnalysisResult.id == id, AnalysisResult.user_id == current_user.id)
        .first()
    )
    if not analysis:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="not_found",
            message="Analysis record not found",
        )
    return analysis
