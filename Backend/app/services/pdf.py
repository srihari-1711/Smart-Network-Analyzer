import io
from datetime import datetime, timezone
from typing import Any, Dict
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.platypus import (
    HRFlowable,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)


def generate_report_pdf(report_data: Dict[str, Any]) -> io.BytesIO:
    buffer = io.BytesIO()
    doc = SimpleDocTemplate(
        buffer,
        pagesize=letter,
        rightMargin=36,
        leftMargin=36,
        topMargin=36,
        bottomMargin=36,
    )

    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        "ReportTitle",
        parent=styles["Heading1"],
        fontSize=20,
        leading=24,
        textColor=colors.HexColor("#1A365D"),
    )
    h2_style = ParagraphStyle(
        "SectionHeading",
        parent=styles["Heading2"],
        fontSize=13,
        leading=16,
        textColor=colors.HexColor("#2B6CB0"),
        spaceBefore=10,
        spaceAfter=4,
    )
    body_style = ParagraphStyle(
        "ReportBody",
        parent=styles["Normal"],
        fontSize=9,
        leading=12,
        textColor=colors.HexColor("#2D3748"),
    )
    bold_style = ParagraphStyle(
        "ReportBold",
        parent=body_style,
        fontName="Helvetica-Bold",
    )

    story = []

    # Title & Metadata
    title = report_data.get("title", "Smart Network Analyzer Report")
    start_str = str(report_data.get("start_date", ""))[:10]
    end_str = str(report_data.get("end_date", ""))[:10]
    health_score = report_data.get("health_score", 0)
    source = report_data.get("source", "rules").upper()

    story.append(Paragraph(f"<b>{title}</b>", title_style))
    story.append(Spacer(1, 4))
    story.append(Paragraph(f"Date Range: {start_str} to {end_str} &nbsp;|&nbsp; Engine: {source}", body_style))
    story.append(Spacer(1, 8))

    # Health Score Box
    score_color = colors.HexColor("#2E7D32") if health_score >= 80 else (
        colors.HexColor("#F57C00") if health_score >= 60 else colors.HexColor("#D32F2F")
    )
    summary_table_data = [
        [
            Paragraph("<b>Overall Health Score:</b>", bold_style),
            Paragraph(f"<b><font color='{score_color.hexval()}'>{health_score} / 100</font></b>", bold_style),
        ]
    ]
    summary_table = Table(summary_table_data, colWidths=[150, 390])
    summary_table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), colors.HexColor("#EDF2F7")),
        ("PADDING", (0, 0), (-1, -1), 6),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
    ]))
    story.append(summary_table)
    story.append(Spacer(1, 10))

    sections = report_data.get("sections", {})

    # Speed Section
    speed = sections.get("speed")
    if speed:
        story.append(Paragraph("<b>1. Internet Speed Performance</b>", h2_style))
        speed_data = [
            ["Metric", "Value"],
            ["Tests Conducted", str(speed.get("count", 0))],
            ["Avg Download Speed", f"{speed.get('avg_download_mbps', 0)} Mbps"],
            ["Max Download Speed", f"{speed.get('max_download_mbps', 0)} Mbps"],
            ["Avg Upload Speed", f"{speed.get('avg_upload_mbps', 0)} Mbps"],
        ]
        t = Table(speed_data, colWidths=[200, 340])
        t.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#2B6CB0")),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5E0")),
            ("PADDING", (0, 0), (-1, -1), 4),
        ]))
        story.append(t)
        story.append(Spacer(1, 8))

    # Latency Section
    latency = sections.get("latency")
    if latency:
        story.append(Paragraph("<b>2. Latency & Connection Stability</b>", h2_style))
        latency_data = [
            ["Metric", "Value"],
            ["Avg Latency", f"{latency.get('avg_ms', 0)} ms"],
            ["Jitter", f"{latency.get('avg_jitter_ms', 0)} ms"],
            ["Packet Loss", f"{latency.get('avg_packet_loss_percent', 0)} %"],
        ]
        t = Table(latency_data, colWidths=[200, 340])
        t.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#2B6CB0")),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5E0")),
            ("PADDING", (0, 0), (-1, -1), 4),
        ]))
        story.append(t)
        story.append(Spacer(1, 8))

    # Issues Section
    issues = sections.get("issues", [])
    if issues:
        story.append(Paragraph("<b>3. Detected Network Issues</b>", h2_style))
        issue_data = [["Severity", "Category", "Title", "Details"]]
        for iss in issues:
            sev = iss.get("severity", "info").upper()
            cat = iss.get("category", "general").capitalize()
            ttl = iss.get("title", "")
            desc = iss.get("description", "")
            issue_data.append([sev, cat, Paragraph(ttl, bold_style), Paragraph(desc, body_style)])
        t = Table(issue_data, colWidths=[55, 65, 140, 280])
        t.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#718096")),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5E0")),
            ("PADDING", (0, 0), (-1, -1), 4),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ]))
        story.append(t)
        story.append(Spacer(1, 8))

    # Recommendations Section
    recs = sections.get("recommendations", [])
    if recs:
        story.append(Paragraph(f"<b>4. Recommendations ({source} Guided)</b>", h2_style))
        rec_data = [["Priority", "Action", "Guidance"]]
        for r in recs:
            prio = r.get("priority", "low").upper()
            ttl = r.get("title", "")
            desc = r.get("description", "")
            rec_data.append([prio, Paragraph(ttl, bold_style), Paragraph(desc, body_style)])
        t = Table(rec_data, colWidths=[60, 160, 320])
        t.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#2B6CB0")),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#CBD5E0")),
            ("PADDING", (0, 0), (-1, -1), 4),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ]))
        story.append(t)
        story.append(Spacer(1, 10))

    # Footer note
    gen_time = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")
    story.append(HRFlowable(width="100%", thickness=0.5, color=colors.HexColor("#CBD5E0"), spaceBefore=10, spaceAfter=6))
    story.append(Paragraph(f"Smart Network Analyzer System &nbsp;|&nbsp; Generated on: {gen_time}", body_style))

    doc.build(story)
    buffer.seek(0)
    return buffer
