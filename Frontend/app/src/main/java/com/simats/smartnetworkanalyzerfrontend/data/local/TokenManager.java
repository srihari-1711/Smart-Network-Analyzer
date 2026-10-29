package com.simats.smartnetworkanalyzerfrontend.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.simats.smartnetworkanalyzerfrontend.BuildConfig;
import com.simats.smartnetworkanalyzerfrontend.data.model.User;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "smart_network_secure_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_BASE_URL = "custom_base_url";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        SharedPreferences p = null;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            p = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, falling back to standard prefs", e);
            p = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
        this.prefs = p;
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveSession(String accessToken, String refreshToken, User user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        if (user != null) {
            editor.putInt(KEY_USER_ID, user.getId());
            editor.putString(KEY_USER_NAME, user.getName());
            editor.putString(KEY_USER_EMAIL, user.getEmail());
        }
        editor.apply();
    }

    public void saveTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public void saveUser(User user) {
        if (user != null) {
            prefs.edit()
                    .putInt(KEY_USER_ID, user.getId())
                    .putString(KEY_USER_NAME, user.getName())
                    .putString(KEY_USER_EMAIL, user.getEmail())
                    .apply();
        }
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null && getRefreshToken() != null;
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .apply();
    }

    public String getBaseUrl() {
        String custom = prefs.getString(KEY_BASE_URL, null);
        if (custom != null && !custom.trim().isEmpty()) {
            if (!custom.endsWith("/")) {
                custom += "/";
            }
            return custom;
        }
        return BuildConfig.BASE_URL;
    }

    public void setBaseUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            prefs.edit().remove(KEY_BASE_URL).apply();
        } else {
            String clean = url.trim();
            if (!clean.endsWith("/")) {
                clean += "/";
            }
            prefs.edit().putString(KEY_BASE_URL, clean).apply();
        }
    }
}
