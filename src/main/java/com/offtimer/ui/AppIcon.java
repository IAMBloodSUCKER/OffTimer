package com.offtimer.ui;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public final class AppIcon {

    private static final Color BG_TOP = new Color(30, 41, 59);
    private static final Color BG_BOTTOM = new Color(15, 23, 42);
    private static final Color RING = new Color(56, 189, 248);
    private static final Color RING_GLOW = new Color(14, 165, 233, 80);

    private AppIcon() {
    }

    public static BufferedImage renderTrayIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        drawTimerSymbol(g, size, true);
        g.dispose();
        return image;
    }

    public static BufferedImage renderAppIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        float radius = size * 0.24f;
        g.setPaint(new GradientPaint(0, 0, BG_TOP, size, size, BG_BOTTOM));
        g.fill(new RoundRectangle2D.Float(0, 0, size, size, radius, radius));

        g.setColor(new Color(255, 255, 255, 18));
        g.fill(new RoundRectangle2D.Float(size * 0.06f, size * 0.05f, size * 0.88f, size * 0.35f, radius, radius));

        drawTimerSymbol(g, size, false);
        g.dispose();
        return image;
    }

    private static void drawTimerSymbol(Graphics2D g, int size, boolean tray) {
        int cx = size / 2;
        int cy = size / 2 + Math.max(0, size / 24);
        int outer = Math.max(6, (int) (size * (tray ? 0.72 : 0.62)));
        int ring = Math.max(4, outer / 2);

        if (!tray) {
            g.setColor(RING_GLOW);
            g.fill(new Ellipse2D.Float(cx - outer * 0.55f, cy - outer * 0.55f, outer * 1.1f, outer * 1.1f));
        }

        float stroke = Math.max(1.4f, size * 0.08f);
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(RING);
        g.draw(new Arc2D.Float(
                cx - outer / 2f, cy - outer / 2f, outer, outer,
                50, 280, Arc2D.OPEN
        ));

        g.setColor(Color.WHITE);
        float handStroke = Math.max(1.2f, stroke * 0.75f);
        g.setStroke(new BasicStroke(handStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx, cy, cx, cy - ring / 2);

        g.setStroke(new BasicStroke(Math.max(1f, handStroke * 0.7f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx, cy, cx + ring / 3, cy + ring / 4);
    }
}
