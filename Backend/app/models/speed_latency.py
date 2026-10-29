from datetime import datetime, timezone
from typing import Optional
from sqlalchemy import DateTime, Float, ForeignKey, Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class SpeedTest(Base):
    __tablename__ = "speed_tests"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("users.id", ondelete="CASCADE"), index=True, nullable=False
    )
    download_mbps: Mapped[float] = mapped_column(Float, nullable=False)
    upload_mbps: Mapped[float] = mapped_column(Float, nullable=False)
    ping_ms: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    server: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    network_type: Mapped[str] = mapped_column(String(20), nullable=False)  # "wifi" or "mobile"
    ssid: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    tested_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)

    user: Mapped["User"] = relationship("User", back_populates="speed_tests")

    __table_args__ = (
        Index("ix_speed_tests_user_tested_at", "user_id", "tested_at"),
    )


class LatencyTest(Base):
    __tablename__ = "latency_tests"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("users.id", ondelete="CASCADE"), index=True, nullable=False
    )
    target_host: Mapped[str] = mapped_column(String(100), nullable=False)
    avg_ms: Mapped[float] = mapped_column(Float, nullable=False)
    min_ms: Mapped[float] = mapped_column(Float, nullable=False)
    max_ms: Mapped[float] = mapped_column(Float, nullable=False)
    jitter_ms: Mapped[float] = mapped_column(Float, nullable=False)
    packet_loss_percent: Mapped[float] = mapped_column(Float, nullable=False)
    packets_sent: Mapped[int] = mapped_column(Integer, nullable=False)
    packets_received: Mapped[int] = mapped_column(Integer, nullable=False)
    ssid: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    tested_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)

    user: Mapped["User"] = relationship("User", back_populates="latency_tests")

    __table_args__ = (
        Index("ix_latency_tests_user_tested_at", "user_id", "tested_at"),
    )
