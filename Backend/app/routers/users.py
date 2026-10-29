from datetime import datetime, timezone
from fastapi import APIRouter, Depends, Response, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.errors import AppException
from app.core.security import hash_password, verify_password
from app.deps import get_current_user
from app.models.user import User
from app.schemas.auth import MessageResponse
from app.schemas.user import PasswordUpdate, UserResponse, UserUpdate

router = APIRouter(prefix="/users", tags=["users"])


@router.get("/me", response_model=UserResponse)
def get_profile(current_user: User = Depends(get_current_user)):
    return current_user


@router.put("/me", response_model=UserResponse)
def update_profile(
    req: UserUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if req.email:
        clean_email = req.email.strip().lower()
        if clean_email != current_user.email:
            existing = db.query(User).filter(User.email == clean_email, User.id != current_user.id).first()
            if existing:
                raise AppException(
                    status_code=status.HTTP_409_CONFLICT,
                    code="email_taken",
                    message="An account with this email already exists",
                )
            current_user.email = clean_email

    if req.name is not None:
        current_user.name = req.name.strip()

    current_user.updated_at = datetime.now(timezone.utc)
    db.commit()
    db.refresh(current_user)
    return current_user


@router.put("/me/password", response_model=MessageResponse)
def change_password(
    req: PasswordUpdate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if not verify_password(req.current_password, current_user.password_hash):
        raise AppException(
            status_code=status.HTTP_400_BAD_REQUEST,
            code="invalid_password",
            message="Current password is incorrect",
        )

    current_user.password_hash = hash_password(req.new_password)
    current_user.updated_at = datetime.now(timezone.utc)
    db.commit()
    return {"message": "Password updated successfully"}


@router.delete("/me", status_code=status.HTTP_204_NO_CONTENT)
def delete_account(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    db.delete(current_user)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
