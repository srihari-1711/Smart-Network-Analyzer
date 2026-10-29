package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ErrorResponse {
    @SerializedName("error")
    private ErrorDetails error;

    public ErrorDetails getError() {
        return error;
    }

    public static class ErrorDetails {
        @SerializedName("code")
        private String code;

        @SerializedName("message")
        private String message;

        @SerializedName("details")
        private Object details;

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public Object getDetails() {
            return details;
        }
    }
}
