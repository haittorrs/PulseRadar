package com.pulseradar.client.compat;

import com.pulseradar.client.PulseRadarClient;
import com.pulseradar.client.screen.RadarLayoutScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new RadarLayoutScreen(parent, PulseRadarClient.config(),
                PulseRadarClient.configManager(), PulseRadarClient.renderer());
    }
}
