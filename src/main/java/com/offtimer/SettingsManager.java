package com.offtimer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.offtimer.model.ActionType;
import com.offtimer.model.TimerMode;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path settingsPath;

    public SettingsManager() {
        settingsPath = resolveSettingsPath();
    }

    private static Path resolveSettingsPath() {
        String command = ProcessHandle.current().info().command().orElse(null);
        if (command != null) {
            Path parent = Path.of(command).getParent();
            if (parent != null && Files.isDirectory(parent)) {
                return parent.resolve("offtimer-settings.json");
            }
        }
        return Path.of(System.getProperty("user.dir"), "offtimer-settings.json");
    }

    public AppSettings load() {
        if (!Files.exists(settingsPath)) {
            return AppSettings.defaults();
        }
        try (Reader reader = Files.newBufferedReader(settingsPath)) {
            AppSettings settings;
            try {
                settings = GSON.fromJson(reader, AppSettings.class);
            } catch (RuntimeException e) {
                return AppSettings.defaults();
            }
            if (settings == null) {
                return AppSettings.defaults();
            }
            sanitize(settings);
            return settings;
        } catch (IOException e) {
            return AppSettings.defaults();
        }
    }

    private static void sanitize(AppSettings s) {
        if (s.action == null || s.action.isBlank()) {
            s.action = ActionType.SHUTDOWN.name();
        }
        if (s.timerMode == null || s.timerMode.isBlank()) {
            s.timerMode = TimerMode.COUNTDOWN.name();
        }
        if (s.countdownMinutes < 0 || s.countdownMinutes > 24 * 60) {
            s.countdownMinutes = 10;
        }
        if (s.absoluteTime == null) {
            s.absoluteTime = "22:30";
        }
    }

    public void save(AppSettings settings) {
        try {
            Files.createDirectories(settingsPath.getParent());
            try (Writer writer = Files.newBufferedWriter(settingsPath)) {
                GSON.toJson(settings, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static class AppSettings {
        public String action = ActionType.SHUTDOWN.name();
        public String timerMode = TimerMode.COUNTDOWN.name();
        public int countdownMinutes = 10;
        public String absoluteTime = "22:30";

        public static AppSettings defaults() {
            return new AppSettings();
        }

        public ActionType getAction() {
            if (action == null || action.isBlank()) {
                return ActionType.SHUTDOWN;
            }
            try {
                return ActionType.valueOf(action);
            } catch (IllegalArgumentException e) {
                return ActionType.SHUTDOWN;
            }
        }

        public TimerMode getTimerMode() {
            if (timerMode == null || timerMode.isBlank()) {
                return TimerMode.COUNTDOWN;
            }
            try {
                return TimerMode.valueOf(timerMode);
            } catch (IllegalArgumentException e) {
                return TimerMode.COUNTDOWN;
            }
        }
    }
}
