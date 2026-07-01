package com.offtimer.model;

public enum TimerMode {
    COUNTDOWN("через"),
    ABSOLUTE("в точное время");

    private final String displayName;

    TimerMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
