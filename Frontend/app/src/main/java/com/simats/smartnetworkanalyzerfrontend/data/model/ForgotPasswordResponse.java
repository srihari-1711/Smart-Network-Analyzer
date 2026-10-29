package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("reset_token")
    private String resetToken;

    public String getMessage() {
        return message;
    }

    public String getResetToken() {
        return resetToken;
    }
}
