package com.offtimer.ui;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public final class AppIcon {

    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color BLUE_DARK = new Color(37, 99, 235);

    private AppIcon() {
    }

    public static BufferedImage renderTrayIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        drawSymbol(g, size, true);
        g.dispose();
        return image;
    }

    public static BufferedImage renderAppIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(BLUE);
        g.fillRect(0, 0, size, size);
        drawSymbol(g, size, false);
        g.dispose();
        return image;
    }

    private static void drawSymbol(Graphics2D g, int size, boolean transparentBackground) {
        if (transparentBackground) {
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, size, size);
            g.setComposite(AlphaComposite.SrcOver);
        }

        int inset = Math.max(1, size / 10);
        g.setColor(BLUE_DARK);
        g.fill(new Ellipse2D.Float(inset, inset, size - inset * 2f, size - inset * 2f));

        g.setColor(Color.WHITE);
        float stroke = Math.max(1.6f, size * 0.09f);
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int cx = size / 2;
        int cy = size / 2 + Math.max(1, size / 24);
        int radius = Math.max(3, size * 3 / 10);
        g.drawArc(cx - radius, cy - radius, radius * 2, radius * 2, 52, 256);
        g.drawLine(cx, cy - radius - size / 14, cx, cy - size / 10);
    }
}
