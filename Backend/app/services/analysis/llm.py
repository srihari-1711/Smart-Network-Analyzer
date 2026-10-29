import json
import logging
from abc import ABC, abstractmethod
from typing import Any, Dict, List, Optional
import httpx
from pydantic import BaseModel, Field
from app.core.config import get_settings

logger = logging.getLogger("smart_network.llm")
settings = get_settings()


class LLMIssue(BaseModel):
    severity: str = "medium"
    category: str = "general"
    title: str = "Network Observation"
    description: str = ""


class LLMRecommendation(BaseModel):
    priority: str = "medium"
    title: str = "Recommendation"
    description: str = ""


class LLMAnalysisOutput(BaseModel):
    summary: str
    issues: List[LLMIssue] = Field(default_factory=list)
    recommendations: List[LLMRecommendation] = Field(default_factory=list)


class LLMProvider(ABC):
    @abstractmethod
    def analyze(
        self,
        metrics_snapshot: Dict[str, Any],
        health_score: int,
        rules_issues: List[Dict[str, Any]],
        rules_recommendations: List[Dict[str, Any]],
    ) -> Optional[LLMAnalysisOutput]:
        pass


class GroqProvider(LLMProvider):
    def __init__(self):
        self.endpoint = "https://api.groq.com/openai/v1/chat/completions"

    def analyze(
        self,
        metrics_snapshot: Dict[str, Any],
        health_score: int,
        rules_issues: List[Dict[str, Any]],
        rules_recommendations: List[Dict[str, Any]],
    ) -> Optional[LLMAnalysisOutput]:
        current_settings = get_settings()
        api_key = (current_settings.GROQ_API_KEY or "").strip()
        if not api_key:
            logger.info("Groq API key not configured in .env. Falling back to rules engine.")
            return None

        primary_model = (current_settings.GROQ_MODEL or "openai/gpt-oss-20b").strip()
        candidate_models = [primary_model]
        for fallback in ("openai/gpt-oss-20b", "qwen/qwen3.8-27b", "openai/gpt-oss-120b"):
            if fallback not in candidate_models:
                candidate_models.append(fallback)

        timeout = float(current_settings.GROQ_TIMEOUT_SECONDS or 30.0)

        system_prompt = (
            "You are an expert network engineer assisting a non-technical home or office user. "
            "You will be given raw network metrics, an established health score, and detected issues/recommendations. "
            "Instructions:\n"
            "1. You MUST only use the numbers and findings provided; NEVER invent measurements or speculate on data not provided.\n"
            "2. Write clear, compassionate, and actionable language for non-technical users.\n"
            "3. DO NOT drop any critical or high severity issues identified in the rules input.\n"
            "4. Return strictly valid JSON matching this schema:\n"
            "{\n"
            '  "summary": "1-3 sentences summarizing the network status in plain English.",\n'
            '  "issues": [{"severity": "critical|high|medium|low|info", "category": "speed|latency|signal|interference|security|usage|general", "title": "string", "description": "string"}],\n'
            '  "recommendations": [{"priority": "high|medium|low", "title": "string", "description": "string"}]\n'
            "}"
        )

        user_content = json.dumps({
            "health_score": health_score,
            "metrics_snapshot": metrics_snapshot,
            "baseline_issues": rules_issues,
            "baseline_recommendations": rules_recommendations,
        }, default=str)

        headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        }

        for model in candidate_models:
            body = {
                "model": model,
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_content},
                ],
                "response_format": {"type": "json_object"},
                "temperature": 0.2,
            }

            try:
                logger.info(f"Invoking Groq LLM analysis using model '{model}'...")
                with httpx.Client(timeout=timeout) as client:
                    resp = client.post(self.endpoint, headers=headers, json=body)
                    if resp.status_code != 200:
                        logger.warning(f"Groq API returned HTTP {resp.status_code} for model {model}: {resp.text}")
                        continue

                    data = resp.json()
                    content_str = data["choices"][0]["message"]["content"]
                    
                    # Strip markdown code fences if model enclosed JSON
                    clean_str = content_str.strip()
                    if clean_str.startswith("```"):
                        lines = clean_str.splitlines()
                        if lines and lines[0].startswith("```"):
                            lines = lines[1:]
                        if lines and lines[-1].strip() == "```":
                            lines = lines[:-1]
                        clean_str = "\n".join(lines).strip()

                    parsed_dict = json.loads(clean_str)

                    # Normalize lists
                    if isinstance(parsed_dict, dict):
                        if "issues" in parsed_dict and isinstance(parsed_dict["issues"], list):
                            norm_issues = []
                            for it in parsed_dict["issues"]:
                                if isinstance(it, str):
                                    norm_issues.append({"severity": "medium", "category": "general", "title": it[:60], "description": it})
                                elif isinstance(it, dict):
                                    norm_issues.append({
                                        "severity": str(it.get("severity") or "medium"),
                                        "category": str(it.get("category") or "general"),
                                        "title": str(it.get("title") or "Network Observation"),
                                        "description": str(it.get("description") or ""),
                                    })
                            parsed_dict["issues"] = norm_issues

                        if "recommendations" in parsed_dict and isinstance(parsed_dict["recommendations"], list):
                            norm_recs = []
                            for it in parsed_dict["recommendations"]:
                                if isinstance(it, str):
                                    norm_recs.append({"priority": "medium", "title": it[:60], "description": it})
                                elif isinstance(it, dict):
                                    norm_recs.append({
                                        "priority": str(it.get("priority") or "medium"),
                                        "title": str(it.get("title") or "Recommendation"),
                                        "description": str(it.get("description") or ""),
                                    })
                            parsed_dict["recommendations"] = norm_recs

                    validated = LLMAnalysisOutput.model_validate(parsed_dict)
                    logger.info(f"Successfully generated AI network analysis via Groq ({model}).")
                    return validated

            except Exception as e:
                logger.warning(f"Groq API call with model '{model}' failed: {e}")

        logger.info("All Groq API attempts exhausted or failed. Returning fallback rules.")
        return None


# Factory function to get active LLM provider
def get_llm_provider() -> LLMProvider:
    return GroqProvider()
