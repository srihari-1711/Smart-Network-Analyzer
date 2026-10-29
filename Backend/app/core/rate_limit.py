import time
from collections import defaultdict
from typing import Dict, List
from fastapi import Request
from app.core.errors import AppException

# IP -> list of timestamps
_rate_limit_records: Dict[str, List[float]] = defaultdict(list)


def check_rate_limit(request: Request, max_requests: int = 10, window_seconds: int = 60) -> None:
    client_ip = request.client.host if request.client else "unknown"
    now = time.time()
    
    # Filter out requests older than window_seconds
    timestamps = [t for t in _rate_limit_records[client_ip] if now - t < window_seconds]
    if len(timestamps) >= max_requests:
        raise AppException(
            status_code=429,
            code="rate_limited",
            message="Too many requests. Please try again later.",
        )
    
    timestamps.append(now)
    _rate_limit_records[client_ip] = timestamps


def reset_rate_limits() -> None:
    _rate_limit_records.clear()
