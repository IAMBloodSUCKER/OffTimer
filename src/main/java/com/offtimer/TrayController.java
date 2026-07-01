package com.offtimer;

import com.offtimer.model.ActionType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class TrayController {

    private final SystemTray systemTray;
    private final TrayIcon trayIcon;
    private final Image normalIcon;
    private final Image activeIcon;
    private Runnable onShowWindow;
    private Runnable onExit;
    private Runnable onCancelTimer;

    public TrayController() throws AWTException, IOException {
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException("System tray не поддерживается");
        }

        normalIcon = loadIcon(16);
        activeIcon = createActiveIcon();
        systemTray = SystemTray.getSystemTray();

        PopupMenu menu = new PopupMenu();
        MenuItem showItem = new MenuItem("Открыть");
        showItem.addActionListener(e -> {
            if (onShowWindow != null) {
                onShowWindow.run();
            }
        });
        MenuItem cancelItem = new MenuItem("Отменить таймер");
        cancelItem.addActionListener(e -> {
            if (onCancelTimer != null) {
                onCancelTimer.run();
            }
        });
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.addActionListener(e -> {
            if (onExit != null) {
                onExit.run();
            }
        });

        menu.add(showItem);
        menu.add(cancelItem);
        menu.addSeparator();
        menu.add(exitItem);

        trayIcon = new TrayIcon(normalIcon, "OffTimer", menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && onShowWindow != null) {
                    onShowWindow.run();
                }
            }
        });

        systemTray.add(trayIcon);
    }

    public void setOnShowWindow(Runnable onShowWindow) {
        this.onShowWindow = onShowWindow;
    }

    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }

    public void setOnCancelTimer(Runnable onCancelTimer) {
        this.onCancelTimer = onCancelTimer;
    }

    public void setTimerActive(boolean active) {
        trayIcon.setImage(active ? activeIcon : normalIcon);
        trayIcon.setToolTip(active ? "OffTimer — таймер активен" : "OffTimer");
    }

    public void showWarning(ActionType action, Runnable onCancel) {
        String message = "Через 60 секунд: " + action.getDisplayName().toLowerCase();
        trayIcon.displayMessage("OffTimer", message, TrayIcon.MessageType.WARNING);

        int result = JOptionPane.showConfirmDialog(
                null,
                message + "\n\nОтменить?",
                "OffTimer",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION && onCancel != null) {
            onCancel.run();
        }

        Toolkit.getDefaultToolkit().beep();
    }

    public void showInfo(String title, String message) {
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }

    public void remove() {
        systemTray.remove(trayIcon);
    }

    private Image loadIcon(int size) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/icon.png")) {
            if (stream == null) {
                return createFallbackIcon(size, new Color(26, 39, 68));
            }
            BufferedImage source = ImageIO.read(stream);
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(source, 0, 0, size, size, null);
            g.dispose();
            return scaled;
        }
    }

    private Image createActiveIcon() throws IOException {
        return createFallbackIcon(16, new Color(220, 80, 60));
    }

    private Image createFallbackIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.fillOval(1, 1, size - 2, size - 2);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.5f));
        int cx = size / 2;
        g.drawLine(cx, size / 4, cx, size / 2);
        g.drawArc(size / 4, size / 4, size / 2, size / 2, 45, 270);
        g.dispose();
        return image;
    }
}
