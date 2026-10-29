from datetime import datetime, timedelta, timezone
from fastapi import APIRouter, Depends, Request, Response, status
from sqlalchemy.orm import Session
from app.core.config import get_settings
from app.core.database import get_db
from app.core.errors import AppException
from app.core.rate_limit import check_rate_limit
from app.core.security import (
    create_access_token,
    generate_random_token,
    hash_password,
    hash_token,
    verify_password,
)
from app.models.user import PasswordResetToken, RefreshToken, User
from app.schemas.auth import (
    AuthResponse,
    ForgotPasswordRequest,
    ForgotPasswordResponse,
    LoginRequest,
    LogoutRequest,
    MessageResponse,
    RefreshRequest,
    RefreshResponse,
    ResetPasswordRequest,
    SignupRequest,
)
from app.services.email import send_password_reset

router = APIRouter(prefix="/auth", tags=["auth"])
settings = get_settings()


def _create_refresh_token_record(db: Session, user_id: int) -> str:
    raw_token = generate_random_token(32)
    token_hash = hash_token(raw_token)
    expires_at = datetime.now(timezone.utc) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
    
    db_token = RefreshToken(
        user_id=user_id,
        token_hash=token_hash,
        expires_at=expires_at,
        revoked=False,
    )
    db.add(db_token)
    db.commit()
    return raw_token


@router.post("/signup", response_model=AuthResponse, status_code=status.HTTP_201_CREATED)
def signup(req: SignupRequest, db: Session = Depends(get_db)):
    clean_email = req.email.strip().lower()
    existing = db.query(User).filter(User.email == clean_email).first()
    if existing:
        raise AppException(
            status_code=status.HTTP_409_CONFLICT,
            code="email_taken",
            message="An account with this email already exists",
        )
    
    hashed_pwd = hash_password(req.password)
    new_user = User(
        name=req.name.strip(),
        email=clean_email,
        password_hash=hashed_pwd,
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    access_token = create_access_token(new_user.id)
    refresh_token = _create_refresh_token_record(db, new_user.id)

    return {
        "user": new_user,
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer",
    }


@router.post("/login", response_model=AuthResponse)
def login(req: LoginRequest, request: Request, db: Session = Depends(get_db)):
    check_rate_limit(request)
    clean_email = req.email.strip().lower()
    user = db.query(User).filter(User.email == clean_email).first()
    if not user or not verify_password(req.password, user.password_hash):
        raise AppException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            code="invalid_credentials",
            message="Invalid email or password",
        )
    
    access_token = create_access_token(user.id)
    refresh_token = _create_refresh_token_record(db, user.id)

    return {
        "user": user,
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer",
    }


@router.post("/refresh", response_model=RefreshResponse)
def refresh(req: RefreshRequest, db: Session = Depends(get_db)):
    token_hash = hash_token(req.refresh_token)
    now = datetime.now(timezone.utc)
    db_token = (
        db.query(RefreshToken)
        .filter(RefreshToken.token_hash == token_hash)
        .first()
    )

    if not db_token or db_token.revoked:
        raise AppException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            code="invalid_refresh_token",
            message="Refresh token is invalid or revoked",
        )

    # SQLite returns naive datetime; ensure timezone awareness for comparison
    expires_at = db_token.expires_at
    if expires_at.tzinfo is None:
        expires_at = expires_at.replace(tzinfo=timezone.utc)

    if expires_at < now:
        raise AppException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            code="refresh_token_expired",
            message="Refresh token has expired",
        )

    # Rotate refresh token: revoke current
    db_token.revoked = True
    db.commit()

    # Issue new pair
    new_access_token = create_access_token(db_token.user_id)
    new_refresh_token = _create_refresh_token_record(db, db_token.user_id)

    return {
        "access_token": new_access_token,
        "refresh_token": new_refresh_token,
        "token_type": "bearer",
    }


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
def logout(req: LogoutRequest, db: Session = Depends(get_db)):
    token_hash = hash_token(req.refresh_token)
    db_token = (
        db.query(RefreshToken)
        .filter(RefreshToken.token_hash == token_hash)
        .first()
    )
    if db_token:
        db_token.revoked = True
        db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.post("/forgot-password", response_model=ForgotPasswordResponse)
def forgot_password(req: ForgotPasswordRequest, request: Request, db: Session = Depends(get_db)):
    check_rate_limit(request)
    clean_email = req.email.strip().lower()
    user = db.query(User).filter(User.email == clean_email).first()

    raw_token = generate_random_token(32)
    token_hash = hash_token(raw_token)
    expires_at = datetime.now(timezone.utc) + timedelta(minutes=30)

    if user:
        reset_record = PasswordResetToken(
            user_id=user.id,
            token_hash=token_hash,
            expires_at=expires_at,
            used=False,
        )
        db.add(reset_record)
        db.commit()
        send_password_reset(clean_email, raw_token)

    return {
        "message": "If an account exists for this email, password reset instructions have been sent.",
        "reset_token": raw_token if settings.APP_ENV == "development" else None,
    }


@router.post("/reset-password", response_model=MessageResponse)
def reset_password(req: ResetPasswordRequest, db: Session = Depends(get_db)):
    token_hash = hash_token(req.token)
    now = datetime.now(timezone.utc)
    
    reset_record = (
        db.query(PasswordResetToken)
        .filter(PasswordResetToken.token_hash == token_hash)
        .first()
    )

    if not reset_record or reset_record.used:
        raise AppException(
            status_code=status.HTTP_400_BAD_REQUEST,
            code="invalid_reset_token",
            message="Password reset token is invalid or has already been used",
        )

    expires_at = reset_record.expires_at
    if expires_at.tzinfo is None:
        expires_at = expires_at.replace(tzinfo=timezone.utc)

    if expires_at < now:
        raise AppException(
            status_code=status.HTTP_400_BAD_REQUEST,
            code="reset_token_expired",
            message="Password reset token has expired",
        )

    user = db.query(User).filter(User.id == reset_record.user_id).first()
    if not user:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            code="user_not_found",
            message="User not found",
        )

    # Mark reset token as used
    reset_record.used = True
    user.password_hash = hash_password(req.new_password)

    # Revoke all active refresh tokens for the user
    db.query(RefreshToken).filter(RefreshToken.user_id == user.id).update({"revoked": True})

    db.commit()
    return {"message": "Password has been successfully reset"}
