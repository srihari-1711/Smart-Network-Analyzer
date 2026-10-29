from datetime import datetime, timezone
from typing import Any, List, Optional
from sqlalchemy import JSON, DateTime, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class SecurityScan(Base):
    __tablename__ = "security_scans"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("users.id", ondelete="CASCADE"), index=True, nullable=False
    )
    ssid: Mapped[str] = mapped_column(String(100), nullable=False)
    bssid: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    security_type: Mapped[str] = mapped_column(String(50), nullable=False)
    signal_dbm: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    gateway_ip: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    local_ip: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    risk_level: Mapped[str] = mapped_column(String(20), nullable=False)  # low|medium|high|critical
    security_score: Mapped[int] = mapped_column(Integer, nullable=False)  # 0 to 100
    issues: Mapped[Any] = mapped_column(JSON, default=list, nullable=False)
    recommendations: Mapped[Any] = mapped_column(JSON, default=list, nullable=False)
    scanned_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)

    user: Mapped["User"] = relationship("User", back_populates="security_scans")
    devices: Mapped[List["ConnectedDevice"]] = relationship(
        "ConnectedDevice", back_populates="security_scan", cascade="all, delete-orphan", passive_deletes=True
    )


class ConnectedDevice(Base):
    __tablename__ = "connected_devices"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    scan_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("security_scans.id", ondelete="CASCADE"), index=True, nullable=False
    )
    ip: Mapped[str] = mapped_column(String(50), nullable=False)
    hostname: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    mac: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)

    security_scan: Mapped["SecurityScan"] = relationship("SecurityScan", back_populates="devices")
