from typing import Generator
from fastapi import Depends, Header
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.errors import AppException
from app.core.security import decode_access_token
from app.models.user import User


def get_current_user(
    authorization: str = Header(None),
    db: Session = Depends(get_db),
) -> User:
    if not authorization or not authorization.startswith("Bearer "):
        raise AppException(
            status_code=401,
            code="unauthorized",
            message="Missing or invalid Authorization header",
        )
    
    token = authorization[7:].strip()
    payload = decode_access_token(token)
    user_id = payload.get("sub")
    if not user_id:
        raise AppException(
            status_code=401,
            code="invalid_token",
            message="Token payload invalid",
        )
    
    user = db.query(User).filter(User.id == int(user_id)).first()
    if not user:
        raise AppException(
            status_code=401,
            code="user_not_found",
            message="User associated with token does not exist",
        )
    
    return user
