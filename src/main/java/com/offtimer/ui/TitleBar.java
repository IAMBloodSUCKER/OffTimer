package com.offtimer.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TitleBar extends JPanel {

    private Point dragOffset;

    public TitleBar(JFrame frame, String title, Runnable onMinimize, Runnable onClose) {
        AppTheme.stylePanel(this);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 14, 10, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.TEXT);
        add(titleLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);
        controls.add(windowButton("—", AppTheme.TEXT_MUTED, onMinimize));
        controls.add(windowButton("×", AppTheme.DANGER, onClose));
        add(controls, BorderLayout.EAST);

        MouseAdapter dragger = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset != null) {
                    Point location = frame.getLocation();
                    frame.setLocation(
                            location.x + e.getX() - dragOffset.x,
                            location.y + e.getY() - dragOffset.y
                    );
                }
            }
        };
        addMouseListener(dragger);
        addMouseMotionListener(dragger);
        titleLabel.addMouseListener(dragger);
        titleLabel.addMouseMotionListener(dragger);
    }

    private static JButton windowButton(String text, Color color, Runnable action) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setForeground(color);
        button.setBackground(AppTheme.PANEL_ELEVATED);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                new EmptyBorder(0, 0, 2, 0)
        ));
        button.setPreferredSize(new Dimension(30, 26));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> action.run());
        return button;
    }
}
