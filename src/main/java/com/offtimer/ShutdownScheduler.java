package com.offtimer;

import com.offtimer.model.ActionType;
import com.offtimer.platform.PowerActions;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ShutdownScheduler {

    private static final int WARNING_SECONDS = 60;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "offtimer-scheduler");
        t.setDaemon(true);
        return t;
    });

    private ScheduledFuture<?> tickFuture;
    private ScheduledFuture<?> executeFuture;
    private final AtomicBoolean warningShown = new AtomicBoolean(false);
    private volatile long remainingSeconds;
    private volatile ActionType currentAction;

    private Consumer<Long> onTick;
    private Runnable onWarning;
    private Runnable onComplete;
    private Runnable onCancelled;

    public void setOnTick(Consumer<Long> onTick) {
        this.onTick = onTick;
    }

    public void setOnWarning(Runnable onWarning) {
        this.onWarning = onWarning;
    }

    public void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    public void setOnCancelled(Runnable onCancelled) {
        this.onCancelled = onCancelled;
    }

    public boolean isRunning() {
        return tickFuture != null && !tickFuture.isDone();
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public ActionType getCurrentAction() {
        return currentAction;
    }

    public static long parseCountdownMinutes(String hoursText, String minutesText) throws IllegalArgumentException {
        int hours = parseNonNegative(hoursText, "часы");
        int minutes = parseNonNegative(minutesText, "минуты");
        if (hours == 0 && minutes == 0) {
            throw new IllegalArgumentException("Укажите время больше нуля");
        }
        long total = (long) hours * 60 + minutes;
        if (total > 24 * 60) {
            throw new IllegalArgumentException("Максимум — 24 часа");
        }
        return total;
    }

    public static long secondsUntilAbsolute(String timeText) throws IllegalArgumentException {
        LocalTime target;
        try {
            target = LocalTime.parse(timeText.trim(), DateTimeFormatter.ofPattern("H:mm"));
        } catch (DateTimeParseException e) {
            try {
                target = LocalTime.parse(timeText.trim(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Неверный формат времени. Используйте ЧЧ:ММ");
            }
        }

        LocalTime now = LocalTime.now();
        long seconds = Duration.between(now, target).getSeconds();
        if (seconds <= 0) {
            seconds += 24 * 60 * 60;
        }
        if (seconds < 5) {
            throw new IllegalArgumentException("Укажите время минимум через 5 секунд");
        }
        return seconds;
    }

    public void start(ActionType action, long totalSeconds) {
        cancel(false);
        currentAction = action;
        remainingSeconds = totalSeconds;
        warningShown.set(false);

        tickFuture = executor.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
        executeFuture = executor.schedule(this::execute, totalSeconds, TimeUnit.SECONDS);
    }

    public void cancel() {
        cancel(true);
    }

    private void cancel(boolean notify) {
        if (tickFuture != null) {
            tickFuture.cancel(false);
            tickFuture = null;
        }
        if (executeFuture != null) {
            executeFuture.cancel(false);
            executeFuture = null;
        }
        runCancelCommand();
        remainingSeconds = 0;
        currentAction = null;
        warningShown.set(false);
        if (notify && onCancelled != null) {
            onCancelled.run();
        }
    }

    private void tick() {
        if (remainingSeconds <= 0) {
            return;
        }

        if (remainingSeconds == WARNING_SECONDS && warningShown.compareAndSet(false, true) && onWarning != null) {
            onWarning.run();
        }

        if (onTick != null) {
            onTick.accept(remainingSeconds);
        }

        remainingSeconds--;

        if (remainingSeconds < 0 && tickFuture != null) {
            tickFuture.cancel(false);
            tickFuture = null;
        }
    }

    private void execute() {
        if (tickFuture != null) {
            tickFuture.cancel(false);
            tickFuture = null;
        }
        executeFuture = null;
        remainingSeconds = 0;

        if (onComplete != null) {
            onComplete.run();
        }

        PowerActions.execute(currentAction);
    }

    private void runCancelCommand() {
        // Таймер внутри приложения — отмена системной команды не нужна.
    }

    private static int parseNonNegative(String text, String field) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value < 0) {
                throw new IllegalArgumentException("Поле «" + field + "» не может быть отрицательным");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + field + "» должно быть числом");
        }
    }

    public void shutdown() {
        cancel(false);
        executor.shutdownNow();
    }
}
