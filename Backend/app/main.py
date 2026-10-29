from fastapi import FastAPI, APIRouter
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import get_settings
from app.core.errors import register_exception_handlers
from app.core.logging import LoggingMiddleware
from app.routers import (
    analysis,
    auth,
    dashboard,
    data_usage,
    health,
    latency,
    reports,
    security,
    signal_map,
    speed,
    users,
    wifi,
)

settings = get_settings()

app = FastAPI(
    title="Smart Network Analyzer API",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
)

# Logging middleware
app.add_middleware(LoggingMiddleware)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins_list,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Exception handlers
register_exception_handlers(app)

# API v1 Router
api_v1_router = APIRouter(prefix="/api/v1")
api_v1_router.include_router(health.router)
api_v1_router.include_router(auth.router)
api_v1_router.include_router(users.router)
api_v1_router.include_router(wifi.router)
api_v1_router.include_router(speed.router)
api_v1_router.include_router(latency.router)
api_v1_router.include_router(signal_map.router)
api_v1_router.include_router(data_usage.router)
api_v1_router.include_router(security.router)
api_v1_router.include_router(analysis.router)
api_v1_router.include_router(reports.router)
api_v1_router.include_router(dashboard.router)

# Mount API v1 router
app.include_router(api_v1_router)
