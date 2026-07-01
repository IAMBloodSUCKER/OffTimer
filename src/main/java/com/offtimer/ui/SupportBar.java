package com.offtimer.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class SupportBar extends JPanel {

    public static final String WALLET = "0xe4a1bf07aa8c2194ab94d72812364968ac5b58e3";

    public SupportBar(JComponent parent) {
        setLayout(new BorderLayout(8, 0));
        setBackground(AppTheme.PANEL_ELEVATED);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                new EmptyBorder(7, 12, 7, 10)
        ));

        JLabel line = new JLabel(
                "<html><span style='color:#e2e8f0; font-size:11px'><b>Support the project</b></span>"
                        + "<span style='color:#94a3b8; font-size:11px'> Author: BloodSUCKER </span>"
                        + "<span style='color:#f8fafc; font-family:monospace; font-size:10px'>" + WALLET + "</span></html>"
        );
        line.setBorder(new EmptyBorder(2, 0, 2, 0));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);

        JButton copyButton = AppTheme.outlineButton("Copy");
        copyButton.addActionListener(e -> copyWallet(parent));

        JButton detailsButton = AppTheme.outlineButton("Details");
        detailsButton.addActionListener(e -> showDetails(parent));

        buttons.add(copyButton);
        buttons.add(detailsButton);

        add(line, BorderLayout.CENTER);
        add(buttons, BorderLayout.EAST);
    }

    private static void copyWallet(Component parent) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(WALLET), null);
        JOptionPane.showMessageDialog(parent, "Address copied to clipboard", "OffTimer",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showDetails(Component parent) {
        JOptionPane.showMessageDialog(
                parent,
                """
                        OffTimer is a free utility for scheduling PC shutdown.

                        If you find it useful, you can support development
                        by sending crypto to:

                        %s

                        Thank you!""".formatted(WALLET),
                "Support the project",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
