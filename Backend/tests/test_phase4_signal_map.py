import pytest


def get_auth_header(client, email):
    res = client.post(
        "/api/v1/auth/signup",
        json={"name": "Map User", "email": email, "password": "Password123!"},
    )
    if res.status_code == 409:
        res = client.post(
            "/api/v1/auth/login",
            json={"email": email, "password": "Password123!"},
        )
    return {"Authorization": f"Bearer {res.json()['access_token']}"}


def test_signal_map_empty_summary(client):
    headers = get_auth_header(client, "map_empty@example.com")
    # Create map
    res = client.post(
        "/api/v1/signal-maps",
        json={"name": "Apartment", "description": "2 BHK", "ssid": "Home5G"},
        headers=headers,
    )
    assert res.status_code == 201
    map_id = res.json()["id"]

    # Summary on empty map
    res_summary = client.get(f"/api/v1/signal-maps/{map_id}/summary", headers=headers)
    assert res_summary.status_code == 200
    summary = res_summary.json()
    assert summary["total_points"] == 0
    assert summary["avg_rssi_dbm"] is None
    assert summary["strongest_point"] is None
    assert summary["zone_counts"]["strong"] == 0
    assert "No signal points" in summary["verdict"]


def test_signal_map_points_and_boundaries(client):
    headers = get_auth_header(client, "map_points@example.com")
    res = client.post(
        "/api/v1/signal-maps",
        json={"name": "Office Floor", "ssid": "WorkWifi"},
        headers=headers,
    )
    map_id = res.json()["id"]

    # Boundary point 1: -60 dBm -> strong
    p1 = client.post(
        f"/api/v1/signal-maps/{map_id}/points",
        json={"label": "Desk", "x": 0.2, "y": 0.3, "rssi_dbm": -60},
        headers=headers,
    )
    assert p1.status_code == 201
    p1_id = p1.json()["id"]

    # Boundary point 2: -75 dBm -> fair
    p2 = client.post(
        f"/api/v1/signal-maps/{map_id}/points",
        json={"label": "Kitchen", "x": 0.5, "y": 0.5, "rssi_dbm": -75},
        headers=headers,
    )
    assert p2.status_code == 201

    # Point 3: -85 dBm -> weak
    p3 = client.post(
        f"/api/v1/signal-maps/{map_id}/points",
        json={"label": "Balcony", "x": 0.9, "y": 0.9, "rssi_dbm": -85},
        headers=headers,
    )
    assert p3.status_code == 201

    # Summary
    res_sum = client.get(f"/api/v1/signal-maps/{map_id}/summary", headers=headers)
    assert res_sum.status_code == 200
    summary = res_sum.json()
    assert summary["total_points"] == 3
    assert summary["zone_counts"]["strong"] == 1
    assert summary["zone_counts"]["fair"] == 1
    assert summary["zone_counts"]["weak"] == 1
    assert summary["strongest_point"]["label"] == "Desk"
    assert summary["weakest_point"]["label"] == "Balcony"
    assert "Balcony" in summary["verdict"]

    # Delete point 1
    del_p1 = client.delete(f"/api/v1/signal-maps/{map_id}/points/{p1_id}", headers=headers)
    assert del_p1.status_code == 204

    # Verify count is now 2
    res_map = client.get(f"/api/v1/signal-maps/{map_id}", headers=headers)
    assert res_map.status_code == 200
    assert len(res_map.json()["points"]) == 2


def test_signal_map_isolation(client):
    headers_a = get_auth_header(client, "map_user_a@example.com")
    headers_b = get_auth_header(client, "map_user_b@example.com")

    res = client.post(
        "/api/v1/signal-maps",
        json={"name": "User A Map"},
        headers=headers_a,
    )
    map_id = res.json()["id"]

    # User B cannot access User A's map or summary
    assert client.get(f"/api/v1/signal-maps/{map_id}", headers=headers_b).status_code == 404
    assert client.get(f"/api/v1/signal-maps/{map_id}/summary", headers=headers_b).status_code == 404
    assert client.delete(f"/api/v1/signal-maps/{map_id}", headers=headers_b).status_code == 404
