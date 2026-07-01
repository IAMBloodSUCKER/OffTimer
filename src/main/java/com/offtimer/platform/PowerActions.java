package com.offtimer.platform;

import com.offtimer.model.ActionType;

import java.io.IOException;
import java.util.List;

public final class PowerActions {

    private PowerActions() {
    }

    public static void execute(ActionType action) {
        if (isWindows() && WindowsPowerActions.tryExecute(action)) {
            return;
        }
        runProcess(action.buildCommand());
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static void runProcess(List<String> command) {
        try {
            new ProcessBuilder(command).start();
        } catch (IOException ignored) {
        }
    }
}
