package com.offtimer.ui;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public final class AppIcon {

    private static final Color BLUE_LIGHT = new Color(96, 165, 250);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color BLUE_DARK = new Color(29, 78, 216);

    private AppIcon() {
    }

    public static BufferedImage renderTrayIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        drawCircleBadge(g, size);
        g.dispose();
        return image;
    }

    public static BufferedImage renderAppIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        float radius = size * 0.22f;
        g.setPaint(new GradientPaint(0, 0, BLUE_LIGHT, size, size, BLUE_DARK));
        g.fill(new RoundRectangle2D.Float(0, 0, size, size, radius, radius));

        drawCircleBadge(g, size);
        g.dispose();
        return image;
    }

    private static void drawCircleBadge(Graphics2D g, int size) {
        int pad = Math.max(2, size / 8);
        int d = size - pad * 2;

        g.setPaint(new RadialGradientPaint(
                new Point(size / 2, size / 3),
                d * 0.75f,
                new float[]{0f, 1f},
                new Color[]{BLUE_LIGHT, BLUE_DARK}
        ));
        g.fill(new Ellipse2D.Float(pad, pad, d, d));

        g.setColor(new Color(255, 255, 255, 50));
        g.fill(new Ellipse2D.Float(pad + d * 0.18f, pad + d * 0.12f, d * 0.35f, d * 0.22f));

        g.setColor(Color.WHITE);
        float stroke = Math.max(1.5f, size * 0.075f);
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int cx = size / 2;
        int cy = size / 2 + Math.max(1, size / 28);
        int r = Math.max(3, d / 4);

        g.drawArc(cx - r, cy - r, r * 2, r * 2, 55, 250);
        g.drawLine(cx, cy - r - size / 16, cx, cy - size / 12);

        g.setStroke(new BasicStroke(Math.max(1f, stroke * 0.55f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx, cy, cx + r / 2, cy + r / 3);
    }
}
