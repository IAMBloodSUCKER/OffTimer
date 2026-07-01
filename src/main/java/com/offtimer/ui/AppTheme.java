package com.offtimer.ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class AppTheme {

    public static final Color BG = new Color(18, 24, 38);
    public static final Color PANEL = new Color(26, 35, 50);
    public static final Color PANEL_ELEVATED = new Color(30, 41, 59);
    public static final Color BORDER = new Color(51, 65, 85);
    public static final Color ACCENT = new Color(59, 130, 246);
    public static final Color ACCENT_HOVER = new Color(37, 99, 235);
    public static final Color ACCENT_DISABLED = new Color(45, 74, 122);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color DANGER_HOVER = new Color(220, 38, 38);
    public static final Color TEXT = new Color(248, 250, 252);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);
    public static final Color INPUT_BG = new Color(15, 23, 42);

    public static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font FONT_LABEL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font FONT_COUNTDOWN = new Font(Font.MONOSPACED, Font.BOLD, 28);
    public static final Font FONT_COUNTDOWN_SUB = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    private AppTheme() {
    }

    public static void install() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("Button.default.background", ACCENT);
            UIManager.put("Button.default.foreground", TEXT);
            UIManager.put("Button.default.focusedBackground", ACCENT_HOVER);
            UIManager.put("Button.default.hoverBackground", ACCENT_HOVER);
            UIManager.put("Button.default.pressedBackground", ACCENT_HOVER);
        } catch (Exception ignored) {
        }
    }

    public static void stylePanel(JPanel panel) {
        panel.setBackground(PANEL);
        panel.setOpaque(true);
    }

    public static void styleRoot(JPanel panel) {
        panel.setBackground(BG);
        panel.setOpaque(true);
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_MUTED);
        label.setFont(FONT_LABEL);
        return label;
    }

    public static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(FONT_LABEL);
        return label;
    }

    public static void styleTextField(JTextField field) {
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 10, 6, 10)
        ));
        field.setFont(FONT_LABEL);
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(INPUT_BG);
        combo.setForeground(TEXT);
        combo.setFont(FONT_LABEL);
        combo.putClientProperty("JComponent.roundRect", true);
    }

    public static JButton accentButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setFocusable(false);
        button.setFont(FONT_LABEL);
        button.setBackground(ACCENT);
        button.setForeground(TEXT);
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton ghostButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setFocusable(false);
        button.setFont(FONT_LABEL);
        button.setBackground(PANEL_ELEVATED);
        button.setForeground(TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 18, 8, 18)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void styleRadio(JRadioButton radio) {
        radio.setBackground(PANEL);
        radio.setForeground(TEXT);
        radio.setFont(FONT_LABEL);
        radio.setFocusable(false);
        radio.setOpaque(false);
    }
}
