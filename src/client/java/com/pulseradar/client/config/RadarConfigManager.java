package com.pulseradar.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RadarConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path = FabricLoader.getInstance().getConfigDir().resolve("pulse-radar.json");
    private RadarConfig config;

    public RadarConfig load() {
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                boolean legacyConfig = !json.has("configVersion");
                config = GSON.fromJson(json, RadarConfig.class);
                if (legacyConfig) {
                    config.range = 1000.0;
                    config.configVersion = 2;
                }
            } catch (IOException | RuntimeException ignored) {
                config = null;
            }
        }
        if (config == null) config = new RadarConfig();
        config.configVersion = 2;
        config.validate();
        save();
        return config;
    }

    public void save() {
        if (config == null) return;
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {
            // A read-only config folder should not prevent the HUD from running.
        }
    }
}
