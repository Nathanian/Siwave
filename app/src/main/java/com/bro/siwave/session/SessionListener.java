package com.bro.siwave.session;

public interface SessionListener {
    void onSessionStarted();
    void onTick(int remainingSec);
    void onFrequencyChanged(int hz);
    void onStopped();
}
