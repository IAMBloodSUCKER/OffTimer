package com.offtimer.model;

import java.util.List;

public enum ActionType {
    SHUTDOWN("Выключение"),
    RESTART("Перезагрузка"),
    HIBERNATE("Гибернация"),
    SLEEP("Сон");

    private final String displayName;

    ActionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> buildCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return windowsCommand();
        }
        if (os.contains("mac")) {
            return macCommand();
        }
        return linuxCommand();
    }

    public List<String> buildCancelCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return List.of("shutdown", "/a");
        }
        if (os.contains("mac")) {
            return List.of("killall", "shutdown");
        }
        return List.of("shutdown", "-c");
    }

    private List<String> windowsCommand() {
        return switch (this) {
            case SHUTDOWN -> List.of("shutdown", "/s", "/t", "0");
            case RESTART -> List.of("shutdown", "/r", "/t", "0");
            case HIBERNATE -> List.of("shutdown", "/h");
            case SLEEP -> List.of("rundll32", "powrprof.dll,SetSuspendState", "0", "1", "0");
        };
    }

    private List<String> linuxCommand() {
        return switch (this) {
            case SHUTDOWN -> List.of("shutdown", "-h", "now");
            case RESTART -> List.of("shutdown", "-r", "now");
            case HIBERNATE -> List.of("systemctl", "hibernate");
            case SLEEP -> List.of("systemctl", "suspend");
        };
    }

    private List<String> macCommand() {
        return switch (this) {
            case SHUTDOWN -> List.of("osascript", "-e", "tell app \"System Events\" to shut down");
            case RESTART -> List.of("osascript", "-e", "tell app \"System Events\" to restart");
            case HIBERNATE -> List.of("pmset", "sleepnow");
            case SLEEP -> List.of("pmset", "sleepnow");
        };
    }
}
