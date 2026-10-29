from datetime import datetime, timezone
from typing import List, Optional
from sqlalchemy import DateTime, Float, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class SignalMap(Base):
    __tablename__ = "signal_maps"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("users.id", ondelete="CASCADE"), index=True, nullable=False
    )
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    description: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    ssid: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)

    user: Mapped["User"] = relationship("User", back_populates="signal_maps")
    points: Mapped[List["SignalPoint"]] = relationship(
        "SignalPoint", back_populates="signal_map", cascade="all, delete-orphan", passive_deletes=True
    )


class SignalPoint(Base):
    __tablename__ = "signal_points"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    map_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("signal_maps.id", ondelete="CASCADE"), index=True, nullable=False
    )
    label: Mapped[str] = mapped_column(String(100), nullable=False)
    x: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    y: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    rssi_dbm: Mapped[int] = mapped_column(Integer, nullable=False)
    ssid: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    recorded_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)

    signal_map: Mapped["SignalMap"] = relationship("SignalMap", back_populates="points")
