package com.offtimer.platform;

import com.offtimer.model.ActionType;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class PowerActions {

    private PowerActions() {
    }

    public static void execute(ActionType action) {
        Objects.requireNonNull(action, "action");
        boolean handled = false;
        if (isWindows()) {
            try {
                handled = WindowsPowerActions.tryExecute(action);
            } catch (Throwable t) {
                // JNA can fail at load/runtime time (missing native lib, etc.).
                // Never let that swallow the action: fall back to the OS command.
                System.err.println("OffTimer: native power action failed (" + t + "); falling back to system command");
            }
        }
        if (!handled) {
            runProcess(action.buildCommand());
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static void runProcess(List<String> command) {
        try {
            new ProcessBuilder(command).start();
        } catch (IOException e) {
            System.err.println("OffTimer: failed to start power command " + command + ": " + e.getMessage());
        }
    }
}
