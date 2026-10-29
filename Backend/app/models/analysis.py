from datetime import datetime, timezone
from typing import Any
from sqlalchemy import JSON, DateTime, ForeignKey, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class AnalysisResult(Base):
    __tablename__ = "analysis_results"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("users.id", ondelete="CASCADE"), index=True, nullable=False
    )
    source: Mapped[str] = mapped_column(String(20), nullable=False)  # "ai" or "rules"
    health_score: Mapped[int] = mapped_column(Integer, nullable=False)  # 0 to 100
    summary: Mapped[str] = mapped_column(Text, nullable=False)
    issues: Mapped[Any] = mapped_column(JSON, default=list, nullable=False)
    recommendations: Mapped[Any] = mapped_column(JSON, default=list, nullable=False)
    metrics_snapshot: Mapped[Any] = mapped_column(JSON, default=dict, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)

    user: Mapped["User"] = relationship("User", back_populates="analysis_results")
