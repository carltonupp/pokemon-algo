package com.github.carltonupp;

public class CaptureResult {
    private String message;
    private final boolean success;

    public CaptureResult(boolean success) {
        this.success = success;
    }

    public CaptureResult(String message) {
        this.success = false;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}
