package com.pulseradar.client.input;

import com.pulseradar.client.config.RadarConfig;
import com.pulseradar.client.config.RadarConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class RadarKeybinds {
    private RadarKeybinds() {}

    public static void register(RadarConfig config, RadarConfigManager manager) {
        KeyBinding toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pulse-radar.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyBinding.Category.create(Identifier.of("pulse-radar", "controls"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggle.wasPressed()) {
                config.enabled = !config.enabled;
                manager.save();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Player Radar: " + (config.enabled ? "ON" : "OFF")), true);
                }
            }
        });
    }
}
