package com.offtimer.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class CardPanel extends JPanel {

    public CardPanel() {
        AppTheme.stylePanel(this);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppTheme.BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
    }
}
