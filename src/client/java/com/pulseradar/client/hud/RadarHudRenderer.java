package com.pulseradar.client.hud;

import com.pulseradar.client.config.RadarConfig;
import com.pulseradar.client.tracking.PlayerTracker;
import com.pulseradar.client.tracking.RadarTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;

public final class RadarHudRenderer {
    private static final int GREEN = 0xB020E060;
    private static final int DIM_GREEN = 0x7040C060;
    private static final int BRIGHT_GREEN = 0xFF7CFF92;
    private static final int BACKGROUND = 0xA608160C;
    private static final int SEGMENTS = 96;

    private final MinecraftClient client;
    private final RadarConfig config;
    private final PlayerTracker tracker;

    public RadarHudRenderer(MinecraftClient client, RadarConfig config, PlayerTracker tracker) {
        this.client = client;
        this.config = config;
        this.tracker = tracker;
    }

    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!config.enabled || client.player == null || client.world == null || client.options.hudHidden) return;

        int radius = radius(context);
        if (radius < 35) return;
        int centerX = centerX(context, radius);
        int centerY = centerY(context, radius);

        fillCircle(context, centerX, centerY, radius, BACKGROUND);
        for (int ring = 1; ring <= 4; ring++) {
            drawCircle(context, centerX, centerY, radius * ring / 4, ring == 4 ? GREEN : DIM_GREEN);
        }
        context.fill(centerX - radius, centerY, centerX + radius + 1, centerY + 1, DIM_GREEN);
        context.fill(centerX, centerY - radius, centerX + 1, centerY + radius + 1, DIM_GREEN);

        drawSweep(context, centerX, centerY, radius);
        drawCompass(context, centerX, centerY, radius);
        drawTargets(context, centerX, centerY, radius);

        context.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 3, BRIGHT_GREEN);
    }

    public void renderPreview(DrawContext context) {
        int radius = radius(context);
        if (radius < 35) return;
        int centerX = centerX(context, radius);
        int centerY = centerY(context, radius);
        fillCircle(context, centerX, centerY, radius, BACKGROUND);
        for (int ring = 1; ring <= 4; ring++) {
            drawCircle(context, centerX, centerY, radius * ring / 4, ring == 4 ? GREEN : DIM_GREEN);
        }
        context.fill(centerX - radius, centerY, centerX + radius + 1, centerY + 1, DIM_GREEN);
        context.fill(centerX, centerY - radius, centerX + 1, centerY + radius + 1, DIM_GREEN);
        drawSweep(context, centerX, centerY, radius);
        drawPreviewCompass(context, centerX, centerY, radius);
        if (config.showPlayers) {
            drawPreviewBlip(context, centerX + radius / 3, centerY - radius / 4,
                    config.showPlayerDistances ? "42m" : "", config.playerColor);
        }
        if (config.showMobs) {
            String mobLabel = (config.showMobDistances ? "67m " : "") + (config.showElevation ? "↓" : "");
            drawPreviewBlip(context, centerX - radius / 2, centerY + radius / 3, mobLabel, config.mobColor);
        }
        context.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 3, BRIGHT_GREEN);
        context.fill(centerX + radius - 5, centerY + radius - 5, centerX + radius + 4, centerY + radius + 4, 0xFFFFFFFF);
    }

    public int radius(DrawContext context) {
        int availableRadius = Math.min(
                (context.getScaledWindowWidth() - config.margin * 2) / 2,
                (context.getScaledWindowHeight() - config.margin * 2) / 2
        );
        return Math.min(config.diameter / 2, availableRadius);
    }

    public int centerX(DrawContext context, int radius) {
        int edge = radius + config.margin;
        return edge + (int) Math.round(config.positionX * Math.max(0, context.getScaledWindowWidth() - edge * 2));
    }

    public int centerY(DrawContext context, int radius) {
        int edge = radius + config.margin;
        return edge + (int) Math.round(config.positionY * Math.max(0, context.getScaledWindowHeight() - edge * 2));
    }

    private void drawPreviewCompass(DrawContext context, int cx, int cy, int radius) {
        context.drawCenteredTextWithShadow(client.textRenderer, "N", cx, cy - radius + 5, BRIGHT_GREEN);
        context.drawCenteredTextWithShadow(client.textRenderer, "E", cx + radius - 7, cy - 4, BRIGHT_GREEN);
        context.drawCenteredTextWithShadow(client.textRenderer, "S", cx, cy + radius - 13, BRIGHT_GREEN);
        context.drawCenteredTextWithShadow(client.textRenderer, "W", cx - radius + 7, cy - 4, BRIGHT_GREEN);
    }

    private void drawPreviewBlip(DrawContext context, int x, int y, String label, int color) {
        context.fill(x - 3, y - 3, x + 4, y + 4, withAlpha(color, 0x40));
        context.fill(x - 1, y - 1, x + 2, y + 2, color);
        if (!label.isEmpty()) context.drawTextWithShadow(client.textRenderer, label, x + 4, y - 4, color);
    }

    private void drawTargets(DrawContext context, int centerX, int centerY, int radius) {
        double yaw = Math.toRadians(client.player.getYaw());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double scale = (radius - 7.0) / config.range;

        for (RadarTarget target : tracker.targets()) {
            double screenX = target.relativeX() * cos - target.relativeZ() * sin;
            double screenForward = target.relativeX() * sin + target.relativeZ() * cos;
            int x = centerX + (int) Math.round(screenX * scale);
            int y = centerY - (int) Math.round(screenForward * scale);

            int blipColor = target.type() == RadarTarget.Type.PLAYER ? config.playerColor : config.mobColor;
            context.fill(x - 3, y - 3, x + 4, y + 4, withAlpha(blipColor, 0x40));
            context.fill(x - 1, y - 1, x + 2, y + 2, blipColor);

            StringBuilder label = new StringBuilder();
            boolean showDistance = target.type() == RadarTarget.Type.PLAYER
                    ? config.showPlayerDistances
                    : config.showMobDistances;
            if (showDistance) label.append(Math.round(target.distance())).append('m');
            if (config.showElevation && Math.abs(target.relativeY()) >= 3.0) {
                if (!label.isEmpty()) label.append(' ');
                label.append(target.relativeY() > 0 ? '↑' : '↓');
            }
            if (!label.isEmpty()) {
                int labelX = MathHelper.clamp(x + 4, centerX - radius + 3, centerX + radius - client.textRenderer.getWidth(label.toString()) - 3);
                context.drawTextWithShadow(client.textRenderer, label.toString(), labelX, y - 4, blipColor);
            }
        }
    }

    private void drawCompass(DrawContext context, int cx, int cy, int radius) {
        String[] labels = {"N", "E", "S", "W"};
        double[] bearings = {Math.PI, Math.PI / 2.0, 0.0, -Math.PI / 2.0};
        double yaw = Math.toRadians(client.player.getYaw());
        for (int i = 0; i < labels.length; i++) {
            double angle = bearings[i] - yaw;
            int x = cx + (int) Math.round(Math.sin(angle) * (radius - 9));
            int y = cy - (int) Math.round(Math.cos(angle) * (radius - 9));
            context.drawCenteredTextWithShadow(client.textRenderer, labels[i], x, y - 4, BRIGHT_GREEN);
        }
    }

    private void drawSweep(DrawContext context, int cx, int cy, int radius) {
        double head = (System.nanoTime() / 1_000_000_000.0) * 1.15;
        for (int trail = 0; trail < 12; trail++) {
            double angle = head - trail * 0.018;
            int alpha = 90 - trail * 6;
            int color = (alpha << 24) | 0x20FF60;
            int endX = cx + (int) Math.round(Math.sin(angle) * (radius - 3));
            int endY = cy - (int) Math.round(Math.cos(angle) * (radius - 3));
            drawLine(context, cx, cy, endX, endY, color);
        }
    }

    private static void fillCircle(DrawContext context, int cx, int cy, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
            context.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 1, color);
        }
    }

    private static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static void drawCircle(DrawContext context, int cx, int cy, int radius, int color) {
        int previousX = cx;
        int previousY = cy - radius;
        for (int i = 1; i <= SEGMENTS; i++) {
            double angle = Math.PI * 2.0 * i / SEGMENTS;
            int x = cx + (int) Math.round(Math.sin(angle) * radius);
            int y = cy - (int) Math.round(Math.cos(angle) * radius);
            drawLine(context, previousX, previousY, x, y, color);
            previousX = x;
            previousY = y;
        }
    }

    private static void drawLine(DrawContext context, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0);
        int sy = y0 < y1 ? 1 : -1;
        int error = dx + dy;
        while (true) {
            context.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int twiceError = error * 2;
            if (twiceError >= dy) { error += dy; x0 += sx; }
            if (twiceError <= dx) { error += dx; y0 += sy; }
        }
    }
}
