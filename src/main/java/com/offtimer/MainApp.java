package com.offtimer;

import com.offtimer.model.ActionType;
import com.offtimer.model.TimerMode;
import com.offtimer.ui.AppTheme;
import com.offtimer.ui.CardPanel;
import com.offtimer.ui.SupportBar;
import com.offtimer.ui.TitleBar;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;

public class MainApp {

    private static final String VERSION = "1.0.5";

    private JFrame frame;
    private JComboBox<ActionType> actionCombo;
    private JRadioButton countdownRadio;
    private JRadioButton absoluteRadio;
    private JTextField hoursField;
    private JTextField minutesField;
    private JTextField timeField;
    private JButton startButton;
    private JButton cancelButton;
    private JLabel countdownValueLabel;
    private JLabel countdownHintLabel;

    private final SettingsManager settingsManager = new SettingsManager();
    private final ShutdownScheduler scheduler = new ShutdownScheduler();
    private TrayController trayController;

    public static void main(String[] args) {
        AppTheme.install();
        SwingUtilities.invokeLater(() -> new MainApp().start());
    }

    private void start() {
        buildUi();
        loadSettings();
        setupScheduler();
        setupTray();
        frame.setVisible(true);
    }

    private void buildUi() {
        frame = new JFrame("OffTimer");
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(400, 430);
        frame.setLocationRelativeTo(null);

        try (InputStream stream = getClass().getResourceAsStream("/icon.png")) {
            if (stream != null) {
                frame.setIconImage(ImageIO.read(stream));
            }
        } catch (Exception ignored) {
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray();
            }
        });

        JPanel shell = new JPanel(new BorderLayout());
        AppTheme.styleRoot(shell);
        shell.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));

        TitleBar titleBar = new TitleBar(
                frame,
                "OffTimer v" + VERSION,
                this::minimizeToTray,
                this::minimizeToTray
        );
        shell.add(titleBar, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        AppTheme.styleRoot(content);
        content.setBorder(new EmptyBorder(4, 14, 14, 14));

        content.add(buildActionSection());
        content.add(Box.createVerticalStrut(10));
        content.add(buildTimeSection());
        content.add(Box.createVerticalStrut(12));
        content.add(buildCountdownCard());
        content.add(Box.createVerticalStrut(12));
        content.add(buildFooter());

        shell.add(content, BorderLayout.CENTER);
        shell.add(new SupportBar(frame), BorderLayout.SOUTH);
        frame.setContentPane(shell);

        startButton.addActionListener(e -> startTimer());
        cancelButton.addActionListener(e -> cancelTimer());
        countdownRadio.addActionListener(e -> updateModeFields());
        absoluteRadio.addActionListener(e -> updateModeFields());
        updateModeFields();
    }

    private JPanel buildActionSection() {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        section.setOpaque(false);

        section.add(AppTheme.sectionLabel("Действие"), BorderLayout.NORTH);

        actionCombo = new JComboBox<>(ActionType.values());
        actionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                if (value instanceof ActionType type) {
                    setText(type.getDisplayName());
                }
                if (isSelected) {
                    setBackground(AppTheme.ACCENT);
                    setForeground(AppTheme.TEXT);
                } else {
                    setBackground(AppTheme.INPUT_BG);
                    setForeground(AppTheme.TEXT);
                }
                return this;
            }
        });
        AppTheme.styleComboBox(actionCombo);
        section.add(actionCombo, BorderLayout.CENTER);
        return section;
    }

    private JPanel buildTimeSection() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel header = AppTheme.sectionLabel("Время");
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(header);

        ButtonGroup modeGroup = new ButtonGroup();
        countdownRadio = new JRadioButton(TimerMode.COUNTDOWN.getDisplayName(), true);
        absoluteRadio = new JRadioButton(TimerMode.ABSOLUTE.getDisplayName());
        AppTheme.styleRadio(countdownRadio);
        AppTheme.styleRadio(absoluteRadio);
        modeGroup.add(countdownRadio);
        modeGroup.add(absoluteRadio);

        JPanel countdownRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        countdownRow.setOpaque(false);
        countdownRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        countdownRow.add(countdownRadio);
        hoursField = new JTextField("0", 2);
        minutesField = new JTextField("10", 3);
        AppTheme.styleTextField(hoursField);
        AppTheme.styleTextField(minutesField);
        countdownRow.add(hoursField);
        countdownRow.add(AppTheme.mutedLabel("ч"));
        countdownRow.add(minutesField);
        countdownRow.add(AppTheme.mutedLabel("мин"));

        JPanel absoluteRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        absoluteRow.setOpaque(false);
        absoluteRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        absoluteRow.add(absoluteRadio);
        timeField = new JTextField("22:30", 5);
        AppTheme.styleTextField(timeField);
        absoluteRow.add(timeField);

        card.add(countdownRow);
        card.add(Box.createVerticalStrut(6));
        card.add(absoluteRow);
        return card;
    }

    private JPanel buildCountdownCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        countdownValueLabel = new JLabel("—", SwingConstants.CENTER);
        countdownValueLabel.setFont(AppTheme.FONT_COUNTDOWN);
        countdownValueLabel.setForeground(AppTheme.ACCENT);
        countdownValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        countdownHintLabel = new JLabel("таймер не запущен", SwingConstants.CENTER);
        countdownHintLabel.setFont(AppTheme.FONT_COUNTDOWN_SUB);
        countdownHintLabel.setForeground(AppTheme.TEXT_MUTED);
        countdownHintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(countdownValueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(countdownHintLabel);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        startButton = AppTheme.accentButton("Запустить");
        cancelButton = AppTheme.ghostButton("Отменить");
        cancelButton.setEnabled(false);
        buttons.add(startButton);
        buttons.add(cancelButton);

        footer.add(buttons, BorderLayout.WEST);
        return footer;
    }

    private void updateModeFields() {
        boolean countdown = countdownRadio.isSelected();
        hoursField.setEnabled(countdown);
        minutesField.setEnabled(countdown);
        timeField.setEnabled(!countdown);
    }

    private void setupScheduler() {
        scheduler.setOnTick(seconds -> SwingUtilities.invokeLater(() -> {
            long min = seconds / 60;
            long sec = seconds % 60;
            if (min > 0) {
                countdownValueLabel.setText(String.format("%d:%02d", min, sec));
            } else {
                countdownValueLabel.setText(String.format("0:%02d", sec));
            }
            ActionType action = scheduler.getCurrentAction();
            if (action != null) {
                countdownHintLabel.setText("до " + action.getDisplayName().toLowerCase());
            }
            if (seconds <= 60) {
                countdownValueLabel.setForeground(AppTheme.DANGER);
            } else {
                countdownValueLabel.setForeground(AppTheme.ACCENT);
            }
        }));

        scheduler.setOnWarning(() -> SwingUtilities.invokeLater(() -> {
            if (trayController != null) {
                trayController.showWarning(scheduler.getCurrentAction(), this::cancelTimer);
            }
        }));

        scheduler.setOnComplete(() -> SwingUtilities.invokeLater(() -> {
            setTimerUiActive(false);
            saveSettings();
        }));

        scheduler.setOnCancelled(() -> SwingUtilities.invokeLater(() -> {
            setTimerUiActive(false);
            resetCountdownDisplay();
            if (trayController != null) {
                trayController.setTimerActive(false);
                trayController.showInfo("OffTimer", "Таймер отменён");
            }
        }));
    }

    private void resetCountdownDisplay() {
        countdownValueLabel.setText("—");
        countdownValueLabel.setForeground(AppTheme.ACCENT);
        countdownHintLabel.setText("таймер не запущен");
    }

    private void setupTray() {
        try {
            trayController = new TrayController();
            trayController.setOnShowWindow(this::showFromTray);
            trayController.setOnExit(this::exitApp);
            trayController.setOnCancelTimer(this::cancelTimer);
        } catch (Exception e) {
            trayController = null;
        }
    }

    private void startTimer() {
        ActionType action = (ActionType) actionCombo.getSelectedItem();
        long seconds;

        try {
            if (countdownRadio.isSelected()) {
                long minutes = ShutdownScheduler.parseCountdownMinutes(hoursField.getText(), minutesField.getText());
                seconds = minutes * 60;
            } else {
                seconds = ShutdownScheduler.secondsUntilAbsolute(timeField.getText());
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "OffTimer", JOptionPane.WARNING_MESSAGE);
            return;
        }

        saveSettings();
        scheduler.start(action, seconds);
        setTimerUiActive(true);
        countdownHintLabel.setText("до " + action.getDisplayName().toLowerCase());

        if (trayController != null) {
            trayController.setTimerActive(true);
        }
    }

    private void cancelTimer() {
        if (scheduler.isRunning()) {
            scheduler.cancel();
        }
    }

    private void setTimerUiActive(boolean active) {
        startButton.setEnabled(!active);
        cancelButton.setEnabled(active);
        actionCombo.setEnabled(!active);
        countdownRadio.setEnabled(!active);
        absoluteRadio.setEnabled(!active);
        updateModeFields();

        if (active) {
            startButton.setBackground(AppTheme.ACCENT_DISABLED);
        } else {
            startButton.setBackground(AppTheme.ACCENT);
            resetCountdownDisplay();
        }

        if (!active && trayController != null) {
            trayController.setTimerActive(false);
        }
    }

    private void loadSettings() {
        SettingsManager.AppSettings settings = settingsManager.load();
        actionCombo.setSelectedItem(settings.getAction());
        if (settings.getTimerMode() == TimerMode.ABSOLUTE) {
            absoluteRadio.setSelected(true);
        } else {
            countdownRadio.setSelected(true);
        }
        int totalMinutes = settings.countdownMinutes;
        hoursField.setText(String.valueOf(totalMinutes / 60));
        minutesField.setText(String.valueOf(totalMinutes % 60));
        timeField.setText(settings.absoluteTime);
        updateModeFields();
    }

    private void saveSettings() {
        SettingsManager.AppSettings settings = new SettingsManager.AppSettings();
        ActionType action = (ActionType) actionCombo.getSelectedItem();
        settings.action = action != null ? action.name() : ActionType.SHUTDOWN.name();
        settings.timerMode = absoluteRadio.isSelected() ? TimerMode.ABSOLUTE.name() : TimerMode.COUNTDOWN.name();
        try {
            long minutes = ShutdownScheduler.parseCountdownMinutes(hoursField.getText(), minutesField.getText());
            settings.countdownMinutes = (int) minutes;
        } catch (IllegalArgumentException e) {
            settings.countdownMinutes = 10;
        }
        settings.absoluteTime = timeField.getText().trim();
        settingsManager.save(settings);
    }

    private void minimizeToTray() {
        if (trayController != null) {
            frame.setVisible(false);
        } else {
            exitApp();
        }
    }

    private void showFromTray() {
        frame.setVisible(true);
        frame.setState(Frame.NORMAL);
        frame.toFront();
    }

    private void exitApp() {
        scheduler.shutdown();
        if (trayController != null) {
            trayController.remove();
        }
        frame.dispose();
        System.exit(0);
    }
}
