package com.simats.smartnetworkanalyzerfrontend.ui.map;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.simats.smartnetworkanalyzerfrontend.R;
import com.simats.smartnetworkanalyzerfrontend.data.model.SignalPointDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SignalHeatView extends View {

    public interface OnMapTapListener {
        void onMapTapped(float xRatio, float yRatio);
    }

    private Paint gridPaint;
    private Paint borderPaint;
    private Paint textPaint;
    private Paint pointFillPaint;
    private Paint pointGlowPaint;
    private Paint dotPaint;

    private int strongColor;
    private int fairColor;
    private int weakColor;

    private final List<SignalPointDto> points = new ArrayList<>();
    private OnMapTapListener tapListener;

    public SignalHeatView(Context context) {
        super(context);
        init();
    }

    public SignalHeatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SignalHeatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strongColor = ContextCompat.getColor(getContext(), R.color.status_strong);
        fairColor = ContextCompat.getColor(getContext(), R.color.status_fair);
        weakColor = ContextCompat.getColor(getContext(), R.color.status_weak);
        int strokeColor = ContextCompat.getColor(getContext(), R.color.card_stroke);
        int textColor = ContextCompat.getColor(getContext(), R.color.text_primary);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(strokeColor);
        gridPaint.setStrokeWidth(1.5f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(strokeColor);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        pointFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointFillPaint.setStyle(Paint.Style.FILL);

        pointGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointGlowPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.WHITE);
    }

    public void setOnMapTapListener(OnMapTapListener listener) {
        this.tapListener = listener;
    }

    public void setPoints(List<SignalPointDto> newPoints) {
        this.points.clear();
        if (newPoints != null) {
            this.points.addAll(newPoints);
        }
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && tapListener != null) {
            float width = getWidth();
            float height = getHeight();
            if (width > 0 && height > 0) {
                float xRatio = Math.max(0.05f, Math.min(0.95f, event.getX() / width));
                float yRatio = Math.max(0.05f, Math.min(0.95f, event.getY() / height));
                tapListener.onMapTapped(xRatio, yRatio);
                return true;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 1. Draw subtle bounding border with 20dp rounded corners
        canvas.drawRoundRect(2f, 2f, w - 2f, h - 2f, 20f, 20f, borderPaint);

        // 2. Draw 4x4 coordinate grid
        int gridDivisions = 4;
        for (int i = 1; i < gridDivisions; i++) {
            float x = (w / gridDivisions) * i;
            canvas.drawLine(x, 0, x, h, gridPaint);

            float y = (h / gridDivisions) * i;
            canvas.drawLine(0, y, w, y, gridPaint);
        }

        // 3. Draw recorded signal points
        for (SignalPointDto pt : points) {
            float x = (pt.xCoord > 0f && pt.xCoord <= 1f) ? pt.xCoord : 0.5f;
            float y = (pt.yCoord > 0f && pt.yCoord <= 1f) ? pt.yCoord : 0.5f;
            float px = x * w;
            float py = y * h;

            int color = getColorForRssi(pt.signalDbm);

            // Outer soft ambient ring
            pointGlowPaint.setColor(color);
            pointGlowPaint.setAlpha(45);
            canvas.drawCircle(px, py, 42f, pointGlowPaint);

            // Secondary pulse ring
            pointGlowPaint.setAlpha(90);
            canvas.drawCircle(px, py, 26f, pointGlowPaint);

            // Inner solid core
            pointFillPaint.setColor(color);
            pointFillPaint.setAlpha(255);
            canvas.drawCircle(px, py, 14f, pointFillPaint);

            // Center white highlight dot
            canvas.drawCircle(px, py, 4.5f, dotPaint);

            // Clean floating label
            String name = (pt.locationName != null && !pt.locationName.isEmpty()) ? pt.locationName : "Spot";
            String label = String.format(Locale.getDefault(), "%s (%d)", name, pt.signalDbm);
            canvas.drawText(label, px, Math.max(30f, py - 22f), textPaint);
        }
    }

    private int getColorForRssi(int rssi) {
        if (rssi >= -60) return strongColor;
        if (rssi >= -75) return fairColor;
        return weakColor;
    }
}
