from datetime import datetime, timezone
from sqlalchemy import BigInteger, DateTime, ForeignKey, Index, Integer, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class DataUsageRecord(Base):
    __tablename__ = "data_usage_records"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("users.id", ondelete="CASCADE"), index=True, nullable=False
    )
    period_start: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    period_end: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    wifi_rx_bytes: Mapped[int] = mapped_column(BigInteger, default=0, nullable=False)
    wifi_tx_bytes: Mapped[int] = mapped_column(BigInteger, default=0, nullable=False)
    mobile_rx_bytes: Mapped[int] = mapped_column(BigInteger, default=0, nullable=False)
    mobile_tx_bytes: Mapped[int] = mapped_column(BigInteger, default=0, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, nullable=False)

    user: Mapped["User"] = relationship("User", back_populates="data_usage_records")

    __table_args__ = (
        UniqueConstraint("user_id", "period_start", "period_end", name="uq_user_period"),
        Index("ix_data_usage_user_period_start", "user_id", "period_start"),
    )
