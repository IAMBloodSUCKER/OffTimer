package com.offtimer;

import com.offtimer.model.ActionType;
import com.offtimer.model.TimerMode;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;

public class MainApp {

    private JFrame frame;
    private JComboBox<ActionType> actionCombo;
    private JRadioButton countdownRadio;
    private JRadioButton absoluteRadio;
    private JTextField hoursField;
    private JTextField minutesField;
    private JTextField timeField;
    private JButton startButton;
    private JButton cancelButton;
    private JLabel countdownLabel;

    private final SettingsManager settingsManager = new SettingsManager();
    private final ShutdownScheduler scheduler = new ShutdownScheduler();
    private TrayController trayController;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new MainApp().start();
        });
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
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(340, 260);
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(340, 260));

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

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel title = new JLabel("⏱ OffTimer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(10));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        actionPanel.add(new JLabel("Действие:"));
        actionCombo = new JComboBox<>(ActionType.values());
        actionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ActionType type) {
                    setText(type.getDisplayName());
                }
                return this;
            }
        });
        actionPanel.add(actionCombo);
        root.add(actionPanel);
        root.add(Box.createVerticalStrut(8));

        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
        modePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        modePanel.setBorder(BorderFactory.createTitledBorder("Время"));

        ButtonGroup modeGroup = new ButtonGroup();
        countdownRadio = new JRadioButton(TimerMode.COUNTDOWN.getDisplayName(), true);
        absoluteRadio = new JRadioButton(TimerMode.ABSOLUTE.getDisplayName());
        modeGroup.add(countdownRadio);
        modeGroup.add(absoluteRadio);

        JPanel countdownRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        countdownRow.add(countdownRadio);
        hoursField = new JTextField("0", 2);
        minutesField = new JTextField("10", 3);
        countdownRow.add(hoursField);
        countdownRow.add(new JLabel("ч"));
        countdownRow.add(minutesField);
        countdownRow.add(new JLabel("мин"));

        JPanel absoluteRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        absoluteRow.add(absoluteRadio);
        timeField = new JTextField("22:30", 5);
        absoluteRow.add(timeField);

        modePanel.add(countdownRow);
        modePanel.add(absoluteRow);
        root.add(modePanel);
        root.add(Box.createVerticalStrut(10));

        JPanel controlPanel = new JPanel(new BorderLayout(0, 8));
        controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        startButton = new JButton("Запустить");
        cancelButton = new JButton("Отменить");
        cancelButton.setEnabled(false);
        buttons.add(startButton);
        buttons.add(cancelButton);
        controlPanel.add(buttons, BorderLayout.NORTH);

        countdownLabel = new JLabel("Осталось: —", SwingConstants.CENTER);
        countdownLabel.setFont(countdownLabel.getFont().deriveFont(Font.PLAIN, 13f));
        controlPanel.add(countdownLabel, BorderLayout.CENTER);

        root.add(controlPanel);

        frame.setContentPane(root);

        startButton.addActionListener(e -> startTimer());
        cancelButton.addActionListener(e -> cancelTimer());
        countdownRadio.addActionListener(e -> updateModeFields());
        absoluteRadio.addActionListener(e -> updateModeFields());
        updateModeFields();
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
            countdownLabel.setText(String.format("Осталось: %d мин %02d сек", min, sec));
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
            countdownLabel.setText("Осталось: —");
            if (trayController != null) {
                trayController.setTimerActive(false);
                trayController.showInfo("OffTimer", "Таймер отменён");
            }
        }));
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
        if (!active) {
            if (trayController != null) {
                trayController.setTimerActive(false);
            }
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
