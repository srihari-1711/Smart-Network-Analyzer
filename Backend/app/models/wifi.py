from datetime import datetime, timezone
from typing import List, Optional
from sqlalchemy import Boolean, DateTime, ForeignKey, Index, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class WifiScan(Base):
    __tablename__ = "wifi_scans"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("users.id", ondelete="CASCADE"), index=True, nullable=False
    )
    location_label: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    scanned_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)

    user: Mapped["User"] = relationship("User", back_populates="wifi_scans")
    networks: Mapped[List["WifiNetwork"]] = relationship(
        "WifiNetwork", back_populates="scan", cascade="all, delete-orphan", passive_deletes=True
    )

    __table_args__ = (
        Index("ix_wifi_scans_user_scanned_at", "user_id", "scanned_at"),
    )


class WifiNetwork(Base):
    __tablename__ = "wifi_networks"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    scan_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("wifi_scans.id", ondelete="CASCADE"), index=True, nullable=False
    )
    ssid: Mapped[str] = mapped_column(String(100), nullable=False)
    bssid: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    signal_dbm: Mapped[int] = mapped_column(Integer, nullable=False)
    frequency_mhz: Mapped[int] = mapped_column(Integer, nullable=False)
    channel: Mapped[int] = mapped_column(Integer, nullable=False)
    security: Mapped[str] = mapped_column(String(50), nullable=False)
    is_connected: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)

    scan: Mapped["WifiScan"] = relationship("WifiScan", back_populates="networks")
