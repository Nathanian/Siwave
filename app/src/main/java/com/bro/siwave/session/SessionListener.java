package com.bro.siwave.session;

public interface SessionListener {
    void onTick(int secondsRemaining);
    void onFrequencyChanged(int newHz);
    void onSessionStarted();
    void onSessionStopped();
}
