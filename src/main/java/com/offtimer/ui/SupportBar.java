package com.offtimer.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class SupportBar extends JPanel {

    public static final String WALLET = "0xe4a1bf07aa8c2194ab94d72812364968ac5b58e3";
    public static final String WALLET_URL = "https://etherscan.io/address/" + WALLET;

    public SupportBar(Component parent) {
        setLayout(new BorderLayout(8, 0));
        setBackground(AppTheme.PANEL_ELEVATED);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                new EmptyBorder(8, 12, 8, 10)
        ));

        JPanel textColumn = new JPanel();
        textColumn.setLayout(new BoxLayout(textColumn, BoxLayout.Y_AXIS));
        textColumn.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRow.add(label("Support the project", AppTheme.TEXT, true));
        topRow.add(label(" Author: BloodSUCKER", AppTheme.TEXT_MUTED, false));

        JLabel wallet = walletLabel(parent);
        wallet.setAlignmentX(Component.LEFT_ALIGNMENT);
        wallet.setBorder(new EmptyBorder(4, 0, 0, 0));

        textColumn.add(topRow);
        textColumn.add(wallet);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);

        JButton copyButton = AppTheme.outlineButton("Copy");
        copyButton.addActionListener(e -> copyWallet(parent));

        JButton detailsButton = AppTheme.outlineButton("Details");
        detailsButton.addActionListener(e -> openWallet(parent));

        buttons.add(copyButton);
        buttons.add(detailsButton);

        add(textColumn, BorderLayout.CENTER);
        add(buttons, BorderLayout.EAST);
    }

    private static JLabel label(String text, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.FONT_LABEL.deriveFont(bold ? Font.BOLD : Font.PLAIN));
        label.setForeground(color);
        return label;
    }

    private static JLabel walletLabel(Component parent) {
        JLabel wallet = new JLabel(WALLET);
        wallet.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        wallet.setForeground(AppTheme.ACCENT);
        wallet.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wallet.setToolTipText("Open wallet on Etherscan");
        wallet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openWallet(parent);
            }
        });
        return wallet;
    }

    private static void copyWallet(Component parent) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(WALLET), null);
        JOptionPane.showMessageDialog(parent, "Address copied to clipboard", "OffTimer",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static void openWallet(Component parent) {
        try {
            Desktop.getDesktop().browse(URI.create(WALLET_URL));
        } catch (Exception ex) {
            copyWallet(parent);
            JOptionPane.showMessageDialog(parent,
                    "Could not open browser. Address copied to clipboard.\n" + WALLET_URL,
                    "OffTimer", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
