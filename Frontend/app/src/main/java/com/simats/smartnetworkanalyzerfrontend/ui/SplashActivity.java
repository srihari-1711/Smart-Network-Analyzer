package com.simats.smartnetworkanalyzerfrontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.simats.smartnetworkanalyzerfrontend.MainActivity;
import com.simats.smartnetworkanalyzerfrontend.data.local.TokenManager;
import com.simats.smartnetworkanalyzerfrontend.databinding.ActivitySplashBinding;
import com.simats.smartnetworkanalyzerfrontend.ui.auth.AuthActivity;
import com.simats.smartnetworkanalyzerfrontend.ui.motion.MotionUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });

        // Apple-style logo intro animation (smooth scale up + fade in)
        if (MotionUtils.areAnimationsEnabled(this)) {
            binding.logoContainer.setAlpha(0f);
            binding.logoContainer.setScaleX(0.88f);
            binding.logoContainer.setScaleY(0.88f);
            binding.logoContainer.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator(1.8f))
                    .start();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TokenManager tokenManager = TokenManager.getInstance(this);
            Intent target;
            if (tokenManager.isLoggedIn()) {
                target = new Intent(this, MainActivity.class);
            } else {
                target = new Intent(this, AuthActivity.class);
            }
            startActivity(target);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 900);
    }
}
