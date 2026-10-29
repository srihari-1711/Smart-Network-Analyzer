package com.simats.smartnetworkanalyzerfrontend.ui.tests;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.simats.smartnetworkanalyzerfrontend.R;

import java.util.Locale;

public class SpeedGaugeView extends View {

    private Paint arcBackgroundPaint;
    private Paint arcProgressPaint;
    private Paint arcGlowPaint;
    private Paint tickPaint;
    private Paint textSpeedPaint;
    private Paint textUnitPaint;
    private RectF arcRect;

    private float currentSpeed = 0f;
    private float maxSpeed = 100f;
    private String unitText = "Mbps";
    private ValueAnimator animator;

    private int primaryColor;
    private int accentColor;
    private int trackColor;
    private int textColor;
    private int textSecColor;

    public SpeedGaugeView(Context context) {
        super(context);
        init();
    }

    public SpeedGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeedGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        accentColor = ContextCompat.getColor(getContext(), R.color.accent);
        trackColor = ContextCompat.getColor(getContext(), R.color.card_stroke);
        textColor = ContextCompat.getColor(getContext(), R.color.text_primary);
        textSecColor = ContextCompat.getColor(getContext(), R.color.text_secondary);

        arcBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcBackgroundPaint.setStyle(Paint.Style.STROKE);
        arcBackgroundPaint.setStrokeWidth(26f);
        arcBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        arcBackgroundPaint.setColor(trackColor);

        arcProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcProgressPaint.setStyle(Paint.Style.STROKE);
        arcProgressPaint.setStrokeWidth(26f);
        arcProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        arcProgressPaint.setColor(primaryColor);

        arcGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcGlowPaint.setStyle(Paint.Style.STROKE);
        arcGlowPaint.setStrokeWidth(36f);
        arcGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        arcGlowPaint.setColor(accentColor);
        arcGlowPaint.setAlpha(45);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeWidth(3f);
        tickPaint.setColor(trackColor);
        tickPaint.setAlpha(120);

        textSpeedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textSpeedPaint.setColor(textColor);
        textSpeedPaint.setTextAlign(Paint.Align.CENTER);
        textSpeedPaint.setTextSize(76f);
        textSpeedPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        textUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textUnitPaint.setColor(textSecColor);
        textUnitPaint.setTextAlign(Paint.Align.CENTER);
        textUnitPaint.setTextSize(30f);
        textUnitPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        arcRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = 44f;
        arcRect.set(padding, padding, w - padding, h - padding);

        // Gradient shader for active arc
        SweepGradient gradient = new SweepGradient(
                w / 2f,
                h / 2f,
                new int[]{primaryColor, accentColor, primaryColor},
                new float[]{0.3f, 0.7f, 1.0f}
        );
        Matrix matrix = new Matrix();
        matrix.setRotate(90f, w / 2f, h / 2f);
        gradient.setLocalMatrix(matrix);
        arcProgressPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float startAngle = 135f;
        float sweepTotal = 270f;
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - 44f;

        // Draw elegant ticks around the perimeter
        int tickCount = 21;
        float angleStep = sweepTotal / (tickCount - 1);
        for (int i = 0; i < tickCount; i++) {
            float angle = (float) Math.toRadians(startAngle + (i * angleStep));
            float startX = cx + (radius - 22f) * (float) Math.cos(angle);
            float startY = cy + (radius - 22f) * (float) Math.sin(angle);
            float stopX = cx + (radius - 32f) * (float) Math.cos(angle);
            float stopY = cy + (radius - 32f) * (float) Math.sin(angle);
            canvas.drawLine(startX, startY, stopX, stopY, tickPaint);
        }

        // Draw track
        canvas.drawArc(arcRect, startAngle, sweepTotal, false, arcBackgroundPaint);

        // Draw active sweep
        float ratio = Math.min(1f, Math.max(0f, currentSpeed / maxSpeed));
        float progressSweep = sweepTotal * ratio;
        if (progressSweep > 0.5f) {
            // Draw soft glow under progress
            canvas.drawArc(arcRect, startAngle, progressSweep, false, arcGlowPaint);
            // Draw main active gradient sweep
            canvas.drawArc(arcRect, startAngle, progressSweep, false, arcProgressPaint);
        }

        // Draw Center Speed & Unit Text
        String speedStr = String.format(Locale.getDefault(), "%.1f", currentSpeed);
        canvas.drawText(speedStr, cx, cy + 12f, textSpeedPaint);
        canvas.drawText(unitText, cx, cy + 62f, textUnitPaint);
    }

    public void setSpeed(float speed) {
        if (speed > maxSpeed) {
            maxSpeed = (float) Math.ceil(speed / 50.0) * 50f;
        }
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(this.currentSpeed, speed);
        animator.setDuration(220);
        animator.setInterpolator(new DecelerateInterpolator(1.5f));
        animator.addUpdateListener(animation -> {
            this.currentSpeed = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void setUnitText(String unit) {
        this.unitText = unit;
        invalidate();
    }

    public void reset() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        this.currentSpeed = 0f;
        this.maxSpeed = 100f;
        invalidate();
    }
}
