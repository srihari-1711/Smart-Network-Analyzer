import pytest
from app.core.rate_limit import reset_rate_limits


@pytest.fixture(autouse=True)
def clean_rate_limits():
    reset_rate_limits()


def test_signup_and_duplicate_email(client):
    email = "testuser_phase2@example.com"
    payload = {
        "name": "Phase 2 User",
        "email": email,
        "password": "Password123!",
    }
    # Signup
    res = client.post("/api/v1/auth/signup", json=payload)
    assert res.status_code == 201
    data = res.json()
    assert "access_token" in data
    assert "refresh_token" in data
    assert data["token_type"] == "bearer"
    assert data["user"]["email"] == email
    assert data["user"]["name"] == "Phase 2 User"

    # Duplicate signup should fail with 409 email_taken
    res_dup = client.post("/api/v1/auth/signup", json=payload)
    assert res_dup.status_code == 409
    assert res_dup.json()["error"]["code"] == "email_taken"


def test_login_success_and_failure(client):
    email = "login_test@example.com"
    signup_payload = {
        "name": "Login Tester",
        "email": email,
        "password": "CorrectPassword123!",
    }
    res = client.post("/api/v1/auth/signup", json=signup_payload)
    assert res.status_code == 201

    # Bad password
    res_bad = client.post("/api/v1/auth/login", json={"email": email, "password": "WrongPassword"})
    assert res_bad.status_code == 401
    assert res_bad.json()["error"]["code"] == "invalid_credentials"

    # Correct credentials
    res_good = client.post("/api/v1/auth/login", json={"email": email, "password": "CorrectPassword123!"})
    assert res_good.status_code == 200
    assert "access_token" in res_good.json()


def test_refresh_token_rotation_and_logout(client):
    email = "refresh_test@example.com"
    res = client.post(
        "/api/v1/auth/signup",
        json={"name": "Refresh Tester", "email": email, "password": "Password123!"},
    )
    tokens = res.json()
    old_refresh = tokens["refresh_token"]

    # Rotate refresh token
    res_refresh = client.post("/api/v1/auth/refresh", json={"refresh_token": old_refresh})
    assert res_refresh.status_code == 200
    new_tokens = res_refresh.json()
    new_refresh = new_tokens["refresh_token"]
    assert new_refresh != old_refresh

    # Old refresh token should now be rejected
    res_old = client.post("/api/v1/auth/refresh", json={"refresh_token": old_refresh})
    assert res_old.status_code == 401

    # Logout with new refresh token
    res_logout = client.post("/api/v1/auth/logout", json={"refresh_token": new_refresh})
    assert res_logout.status_code == 204

    # Now new refresh token should also be rejected
    res_logged_out = client.post("/api/v1/auth/refresh", json={"refresh_token": new_refresh})
    assert res_logged_out.status_code == 401


def test_forgot_and_reset_password(client):
    email = "forgot_test@example.com"
    client.post(
        "/api/v1/auth/signup",
        json={"name": "Forgot Tester", "email": email, "password": "OldPassword123!"},
    )

    # Forgot password request
    res_forgot = client.post("/api/v1/auth/forgot-password", json={"email": email})
    assert res_forgot.status_code == 200
    data = res_forgot.json()
    assert "reset_token" in data
    reset_token = data["reset_token"]
    assert reset_token is not None

    # Reset password with valid token
    res_reset = client.post(
        "/api/v1/auth/reset-password",
        json={"token": reset_token, "new_password": "NewBrandPassword123!"},
    )
    assert res_reset.status_code == 200

    # Old password no longer works
    res_old_login = client.post("/api/v1/auth/login", json={"email": email, "password": "OldPassword123!"})
    assert res_old_login.status_code == 401

    # New password works
    res_new_login = client.post("/api/v1/auth/login", json={"email": email, "password": "NewBrandPassword123!"})
    assert res_new_login.status_code == 200

    # Reusing reset token should fail
    res_reuse = client.post(
        "/api/v1/auth/reset-password",
        json={"token": reset_token, "new_password": "AnotherPassword123!"},
    )
    assert res_reuse.status_code == 400


def test_user_profile_update_and_delete(client):
    email = "profile_test@example.com"
    signup_res = client.post(
        "/api/v1/auth/signup",
        json={"name": "Profile Tester", "email": email, "password": "Password123!"},
    )
    token = signup_res.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}

    # Get profile
    res_me = client.get("/api/v1/users/me", headers=headers)
    assert res_me.status_code == 200
    assert res_me.json()["email"] == email

    # Update profile
    res_update = client.put("/api/v1/users/me", headers=headers, json={"name": "Updated Name"})
    assert res_update.status_code == 200
    assert res_update.json()["name"] == "Updated Name"

    # Change password
    res_pwd = client.put(
        "/api/v1/users/me/password",
        headers=headers,
        json={"current_password": "Password123!", "new_password": "UpdatedPassword123!"},
    )
    assert res_pwd.status_code == 200

    # Delete account
    res_del = client.delete("/api/v1/users/me", headers=headers)
    assert res_del.status_code == 204

    # After deletion, token should return 401
    res_me_after = client.get("/api/v1/users/me", headers=headers)
    assert res_me_after.status_code == 401
