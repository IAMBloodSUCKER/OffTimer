package com.offtimer.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;

public class SupportBar extends JPanel {

    public static final String WALLET = "0xe4a1bf07aa8c2194ab94d72812364968ac5b58e3";

    public SupportBar(JComponent parent) {
        setLayout(new BorderLayout(10, 0));
        setBackground(AppTheme.PANEL_ELEVATED);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("Поддержать проект");
        title.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD));
        title.setForeground(AppTheme.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel meta = new JLabel("Автор: BloodSUCKER  ·  " + WALLET);
        meta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        meta.setForeground(AppTheme.TEXT_MUTED);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(meta);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);

        JButton copyButton = AppTheme.outlineButton("Копировать");
        copyButton.addActionListener(e -> copyWallet(parent));

        JButton detailsButton = AppTheme.outlineButton("Подробнее");
        detailsButton.addActionListener(e -> showDetails(parent));

        buttons.add(copyButton);
        buttons.add(detailsButton);

        add(textPanel, BorderLayout.CENTER);
        add(buttons, BorderLayout.EAST);
    }

    private static void copyWallet(Component parent) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(WALLET), null);
        JOptionPane.showMessageDialog(parent, "Адрес скопирован в буфер обмена", "OffTimer",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showDetails(Component parent) {
        JOptionPane.showMessageDialog(
                parent,
                """
                        OffTimer — бесплатная утилита для планирования выключения ПК.

                        Если программа полезна, можно поддержать разработку
                        переводом на криптокошелёк:

                        %s

                        Спасибо!""".formatted(WALLET),
                "Поддержать проект",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
