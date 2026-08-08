package com.pulseradar.client.screen;

import com.pulseradar.client.config.RadarConfig;
import com.pulseradar.client.config.RadarConfigManager;
import com.pulseradar.client.hud.RadarHudRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class RadarLayoutScreen extends Screen {
    private static final int[] COLORS = {
            0xFF7CFF92, 0xFFFF6B6B, 0xFF63D9FF, 0xFFFFD45C,
            0xFFFF78E1, 0xFFFFFFFF, 0xFFFF9C52, 0xFFB68CFF
    };
    private static final String[] COLOR_NAMES = {
            "Green", "Red", "Cyan", "Yellow", "Pink", "White", "Orange", "Purple"
    };
    private final Screen parent;
    private final RadarConfig config;
    private final RadarConfigManager manager;
    private final RadarHudRenderer renderer;
    private DragMode dragMode = DragMode.NONE;
    private double dragOffsetX;
    private double dragOffsetY;
    private ButtonWidget playerColorButton;
    private ButtonWidget playerDistanceButton;
    private ButtonWidget mobColorButton;
    private ButtonWidget mobDistanceButton;

    public RadarLayoutScreen(Screen parent, RadarConfig config, RadarConfigManager manager, RadarHudRenderer renderer) {
        super(Text.translatable("screen.pulse-radar.layout"));
        this.parent = parent;
        this.config = config;
        this.manager = manager;
        this.renderer = renderer;
    }

    @Override
    protected void init() {
        int controlsX = 10;
        int controlsWidth = 170;
        addDrawableChild(ButtonWidget.builder(toggleLabel("Radar", config.enabled), button -> {
            config.enabled = !config.enabled;
            button.setMessage(toggleLabel("Radar", config.enabled));
        }).dimensions(controlsX, 48, controlsWidth, 20).build());

        addDrawableChild(ButtonWidget.builder(toggleLabel("Show Players", config.showPlayers), button -> {
            config.showPlayers = !config.showPlayers;
            button.setMessage(toggleLabel("Show Players", config.showPlayers));
            playerColorButton.active = config.showPlayers;
            playerDistanceButton.active = config.showPlayers;
        }).dimensions(controlsX, 72, controlsWidth, 20).build());

        playerColorButton = addDrawableChild(ButtonWidget.builder(colorLabel("Player Colour", config.playerColor), button -> {
            config.playerColor = nextColor(config.playerColor);
            button.setMessage(colorLabel("Player Colour", config.playerColor));
        }).dimensions(controlsX, 96, controlsWidth, 20).build());
        playerColorButton.active = config.showPlayers;

        playerDistanceButton = addDrawableChild(ButtonWidget.builder(toggleLabel("Player Distances", config.showPlayerDistances), button -> {
            config.showPlayerDistances = !config.showPlayerDistances;
            button.setMessage(toggleLabel("Player Distances", config.showPlayerDistances));
        }).dimensions(controlsX, 120, controlsWidth, 20).build());
        playerDistanceButton.active = config.showPlayers;

        addDrawableChild(ButtonWidget.builder(toggleLabel("Show Mobs", config.showMobs), button -> {
            config.showMobs = !config.showMobs;
            button.setMessage(toggleLabel("Show Mobs", config.showMobs));
            mobColorButton.active = config.showMobs;
            mobDistanceButton.active = config.showMobs;
        }).dimensions(controlsX, 144, controlsWidth, 20).build());

        mobColorButton = addDrawableChild(ButtonWidget.builder(colorLabel("Mob Colour", config.mobColor), button -> {
            config.mobColor = nextColor(config.mobColor);
            button.setMessage(colorLabel("Mob Colour", config.mobColor));
        }).dimensions(controlsX, 168, controlsWidth, 20).build());
        mobColorButton.active = config.showMobs;

        mobDistanceButton = addDrawableChild(ButtonWidget.builder(toggleLabel("Mob Distances", config.showMobDistances), button -> {
            config.showMobDistances = !config.showMobDistances;
            button.setMessage(toggleLabel("Mob Distances", config.showMobDistances));
        }).dimensions(controlsX, 192, controlsWidth, 20).build());
        mobDistanceButton.active = config.showMobs;

        int buttonY = height - 28;
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.pulse-radar.reset"), button -> {
            config.positionX = 1.0;
            config.positionY = 0.0;
            config.diameter = 150;
        }).dimensions(width / 2 - 154, buttonY, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.pulse-radar.done"), button -> close())
                .dimensions(width / 2 + 4, buttonY, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.pulse-radar.instructions"), width / 2, 24, 0xFFB0B0B0);
        renderer.renderPreview(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        double mouseX = click.x();
        double mouseY = click.y();
        if (click.button() == 0) {
            int radius = previewRadius();
            int cx = previewCenterX(radius);
            int cy = previewCenterY(radius);
            if (Math.abs(mouseX - (cx + radius)) <= 10 && Math.abs(mouseY - (cy + radius)) <= 10) {
                dragMode = DragMode.RESIZE;
                return true;
            }
            double dx = mouseX - cx;
            double dy = mouseY - cy;
            if (dx * dx + dy * dy <= radius * radius) {
                dragMode = DragMode.MOVE;
                dragOffsetX = dx;
                dragOffsetY = dy;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (click.button() == 0 && dragMode != DragMode.NONE) {
            int oldRadius = previewRadius();
            int centerX = previewCenterX(oldRadius);
            int centerY = previewCenterY(oldRadius);
            if (dragMode == DragMode.RESIZE) {
                int newRadius = MathHelper.clamp((int) Math.round(Math.max(mouseX - centerX, mouseY - centerY)), 45, 150);
                config.diameter = newRadius * 2;
                setPositionFromCenter(centerX, centerY, newRadius);
            } else {
                setPositionFromCenter((int) Math.round(mouseX - dragOffsetX),
                        (int) Math.round(mouseY - dragOffsetY), oldRadius);
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && dragMode != DragMode.NONE) {
            dragMode = DragMode.NONE;
            return true;
        }
        return super.mouseReleased(click);
    }

    private void setPositionFromCenter(int centerX, int centerY, int radius) {
        int edge = radius + config.margin;
        int horizontalSpan = Math.max(1, width - edge * 2);
        int verticalSpan = Math.max(1, height - edge * 2);
        config.positionX = MathHelper.clamp((centerX - edge) / (double) horizontalSpan, 0.0, 1.0);
        config.positionY = MathHelper.clamp((centerY - edge) / (double) verticalSpan, 0.0, 1.0);
    }

    private static Text toggleLabel(String name, boolean enabled) {
        return Text.literal(name + ": " + (enabled ? "ON" : "OFF"));
    }

    private static Text colorLabel(String name, int color) {
        return Text.literal(name + ": " + colorName(color));
    }

    private static String colorName(int color) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == color) return COLOR_NAMES[i];
        }
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private static int nextColor(int color) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == color) return COLORS[(i + 1) % COLORS.length];
        }
        return COLORS[0];
    }

    private int previewRadius() {
        int availableRadius = Math.min((width - config.margin * 2) / 2, (height - config.margin * 2) / 2);
        return Math.min(config.diameter / 2, availableRadius);
    }

    private int previewCenterX(int radius) {
        int edge = radius + config.margin;
        return edge + (int) Math.round(config.positionX * Math.max(0, width - edge * 2));
    }

    private int previewCenterY(int radius) {
        int edge = radius + config.margin;
        return edge + (int) Math.round(config.positionY * Math.max(0, height - edge * 2));
    }

    @Override
    public void close() {
        config.validate();
        manager.save();
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void removed() {
        config.validate();
        manager.save();
    }

    private enum DragMode { NONE, MOVE, RESIZE }
}
