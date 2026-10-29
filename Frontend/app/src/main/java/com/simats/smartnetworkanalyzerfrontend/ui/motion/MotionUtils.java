package com.simats.smartnetworkanalyzerfrontend.ui.motion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import java.util.Locale;

public class MotionUtils {

    /**
     * Checks if system animations are enabled.
     */
    public static boolean areAnimationsEnabled(Context context) {
        if (context == null) return true;
        try {
            float durationScale = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1.0f
            );
            return durationScale > 0.0f;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Adds an Apple-like tactile spring press feedback to any interactive button or card.
     * Scales down to 0.96 on touch down with light haptic, bounces back with natural spring on release.
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void applyPressSpring(View view, @Nullable Runnable onClick) {
        if (view == null) return;

        SpringAnimation scaleXUp = new SpringAnimation(view, DynamicAnimation.SCALE_X, 1.0f);
        SpringAnimation scaleYUp = new SpringAnimation(view, DynamicAnimation.SCALE_Y, 1.0f);
        SpringForce force = new SpringForce(1.0f);
        force.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        force.setStiffness(SpringForce.STIFFNESS_MEDIUM);
        scaleXUp.setSpring(force);
        scaleYUp.setSpring(force);

        SpringAnimation scaleXDown = new SpringAnimation(view, DynamicAnimation.SCALE_X, 0.96f);
        SpringAnimation scaleYDown = new SpringAnimation(view, DynamicAnimation.SCALE_Y, 0.96f);
        SpringForce forceDown = new SpringForce(0.96f);
        forceDown.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
        forceDown.setStiffness(SpringForce.STIFFNESS_HIGH);
        scaleXDown.setSpring(forceDown);
        scaleYDown.setSpring(forceDown);

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    scaleXUp.cancel();
                    scaleYUp.cancel();
                    scaleXDown.start();
                    scaleYDown.start();
                    performLightHaptic(v);
                    break;
                case MotionEvent.ACTION_UP:
                    scaleXDown.cancel();
                    scaleYDown.cancel();
                    scaleXUp.start();
                    scaleYUp.start();
                    if (onClick != null) {
                        v.postDelayed(onClick, 50);
                    }
                    v.performClick();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    scaleXDown.cancel();
                    scaleYDown.cancel();
                    scaleXUp.start();
                    scaleYUp.start();
                    break;
            }
            return true;
        });
    }

    /**
     * Performs a subtle, satisfying click haptic.
     */
    public static void performLightHaptic(View view) {
        if (view == null) return;
        view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        );
    }

    /**
     * Performs a success / confirmation haptic pattern.
     */
    public static void performSuccessHaptic(Context context) {
        if (context == null) return;
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(35);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Smooth count-up animation for numbers (e.g. speed, latency, health score).
     */
    public static void animateCountUp(
            TextView textView,
            float startValue,
            float endValue,
            String format,
            String suffix,
            long durationMs
    ) {
        if (textView == null) return;
        if (!areAnimationsEnabled(textView.getContext())) {
            textView.setText(String.format(Locale.getDefault(), format, endValue) + suffix);
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(startValue, endValue);
        animator.setDuration(durationMs);
        animator.setInterpolator(new DecelerateInterpolator(1.8f));
        animator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            textView.setText(String.format(Locale.getDefault(), format, val) + suffix);
        });
        animator.start();
    }

    /**
     * Overload for integer counts (e.g., health score, item counts).
     */
    public static void animateIntCountUp(
            TextView textView,
            int startValue,
            int endValue,
            String suffix,
            long durationMs
    ) {
        if (textView == null) return;
        if (!areAnimationsEnabled(textView.getContext())) {
            textView.setText(String.valueOf(endValue) + suffix);
            return;
        }

        ValueAnimator animator = ValueAnimator.ofInt(startValue, endValue);
        animator.setDuration(durationMs);
        animator.setInterpolator(new DecelerateInterpolator(1.8f));
        animator.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(val) + suffix);
        });
        animator.start();
    }

    /**
     * Applies a staggered slide-and-fade entrance for container child views.
     */
    public static void applyStaggeredEntrance(ViewGroup parent) {
        if (parent == null || !areAnimationsEnabled(parent.getContext())) return;

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(40f);
            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setStartDelay(i * 45L)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .start();
        }
    }

    /**
     * Smooth crossfade transition between two views.
     */
    public static void crossfade(View outView, View inView, long durationMs) {
        if (outView == null || inView == null) return;

        inView.setAlpha(0f);
        inView.setVisibility(View.VISIBLE);

        inView.animate()
                .alpha(1f)
                .setDuration(durationMs)
                .setListener(null);

        outView.animate()
                .alpha(0f)
                .setDuration(durationMs)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        outView.setVisibility(View.GONE);
                    }
                });
    }
}
