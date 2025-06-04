package com.bro.siwave.util;

public class DoubleClickHandler {
    private long lastClickTime = 0;
    private final long thresholdMs;

    public DoubleClickHandler(long thresholdMs) {
        this.thresholdMs = thresholdMs;
    }

    public boolean isDoubleClick() {
        long now = System.currentTimeMillis();
        boolean result = now - lastClickTime < thresholdMs;
        lastClickTime = now;
        return result;
    }
}
