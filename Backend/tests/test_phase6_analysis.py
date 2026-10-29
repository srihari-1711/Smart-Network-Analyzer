from unittest.mock import MagicMock, patch
import pytest
from app.services.analysis.llm import GroqProvider, LLMAnalysisOutput
from app.services.analysis.rules import evaluate_rules


def get_auth_header(client, email):
    res = client.post(
        "/api/v1/auth/signup",
        json={"name": "Analysis User", "email": email, "password": "Password123!"},
    )
    if res.status_code == 409:
        res = client.post(
            "/api/v1/auth/login",
            json={"email": email, "password": "Password123!"},
        )
    return {"Authorization": f"Bearer {res.json()['access_token']}"}


def test_rules_engine_evaluations():
    # 1. Healthy network
    metrics_healthy = {
        "speed": {"avg_download_mbps": 150.0, "avg_upload_mbps": 50.0, "download_cv": 0.1},
        "latency": {"avg_ms": 15.0, "avg_jitter_ms": 2.0, "avg_packet_loss_percent": 0.0},
        "wifi": {"connected_signal_dbm": -50, "same_channel_co_networks": 0, "connected_channel": 36},
        "signal_map": {"weak_zone_ratio": 0.05, "weak_points_count": 0},
        "security": {"latest_score": 100, "risk_level": "low"},
    }
    score, summary, issues, recs = evaluate_rules(metrics_healthy)
    assert score == 100
    assert len(issues) == 0
    assert "great health" in summary

    # 2. Degraded network with multiple severe issues
    metrics_degraded = {
        "speed": {"avg_download_mbps": 5.0, "avg_upload_mbps": 0.2, "download_cv": 0.7},
        "latency": {"avg_ms": 150.0, "avg_jitter_ms": 45.0, "avg_packet_loss_percent": 3.5},
        "wifi": {"connected_signal_dbm": -82, "same_channel_co_networks": 5, "connected_channel": 6},
        "signal_map": {"weak_zone_ratio": 0.45, "weak_points_count": 5},
        "security": {"latest_score": 35, "risk_level": "critical"},
    }
    score_deg, summary_deg, issues_deg, recs_deg = evaluate_rules(metrics_degraded)
    assert score_deg <= 40
    assert len(issues_deg) >= 5
    severities = [i["severity"] for i in issues_deg]
    assert "critical" in severities
    assert "high" in severities
    assert "Degraded" in summary_deg or "degraded" in summary_deg


def test_analysis_no_data(client):
    headers = get_auth_header(client, "empty_analysis@example.com")
    res = client.post("/api/v1/analysis", json={"days": 7}, headers=headers)
    assert res.status_code == 422
    assert res.json()["error"]["code"] == "no_data"


def test_analysis_fallback_rules(client):
    headers = get_auth_header(client, "rules_analysis@example.com")

    # Add a speed test so data exists
    client.post(
        "/api/v1/speed-tests",
        json={"download_mbps": 85.0, "upload_mbps": 30.0, "network_type": "wifi"},
        headers=headers,
    )

    # Call analysis without Groq key -> source="rules"
    res = client.post("/api/v1/analysis", json={"days": 7}, headers=headers)
    assert res.status_code == 201
    data = res.json()
    assert data["source"] == "rules"
    assert "health_score" in data
    assert "summary" in data
    assert "metrics_snapshot" in data


def test_llm_groq_success_mock(client):
    headers = get_auth_header(client, "groq_success@example.com")

    # Add speed test
    client.post(
        "/api/v1/speed-tests",
        json={"download_mbps": 85.0, "upload_mbps": 30.0, "network_type": "wifi"},
        headers=headers,
    )

    mock_ai_output = LLMAnalysisOutput(
        summary="AI Analysis: Your broadband throughput is strong and stable.",
        issues=[],
        recommendations=[
            {"priority": "low", "title": "Maintain Router", "description": "All parameters look optimal."}
        ],
    )

    with patch.object(GroqProvider, "analyze", return_value=mock_ai_output):
        res = client.post("/api/v1/analysis", json={"days": 7}, headers=headers)
        assert res.status_code == 201
        data = res.json()
        assert data["source"] == "ai"
        assert "AI Analysis" in data["summary"]
