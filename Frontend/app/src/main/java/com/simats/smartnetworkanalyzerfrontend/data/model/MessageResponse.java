package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;

public class MessageResponse {
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }
}
