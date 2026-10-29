import logging

logger = logging.getLogger("smart_network.email")


def send_password_reset(email: str, token: str) -> None:
    """Send password reset token to user email.
    
    In development, this logs to console. In production, connect an SMTP
    or third-party email provider (e.g., SendGrid, AWS SES) here.
    """
    logger.info(f"[EMAIL] Password reset requested for '{email}'. Reset Token: {token}")
