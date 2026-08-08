package com.pulseradar.client;

import com.pulseradar.client.config.RadarConfig;
import com.pulseradar.client.config.RadarConfigManager;
import com.pulseradar.client.hud.RadarHudRenderer;
import com.pulseradar.client.input.RadarKeybinds;
import com.pulseradar.client.tracking.PlayerTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public final class PulseRadarClient implements ClientModInitializer {
    private static RadarConfig config;
    private static RadarConfigManager configManager;
    private static RadarHudRenderer renderer;

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        configManager = new RadarConfigManager();
        config = configManager.load();
        PlayerTracker tracker = new PlayerTracker();
        renderer = new RadarHudRenderer(client, config, tracker);

        RadarKeybinds.register(config, configManager);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (config.enabled) tracker.update(mc, config);
        });
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of("pulse-radar", "player_radar"),
                renderer::render
        );
    }

    public static RadarConfig config() { return config; }
    public static RadarConfigManager configManager() { return configManager; }
    public static RadarHudRenderer renderer() { return renderer; }
}
