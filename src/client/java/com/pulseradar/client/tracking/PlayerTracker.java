package com.pulseradar.client.tracking;

import com.pulseradar.client.config.RadarConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;

import java.util.ArrayList;
import java.util.List;

public final class PlayerTracker {
    private final List<RadarTarget> targets = new ArrayList<>();

    public void update(MinecraftClient client, RadarConfig config) {
        targets.clear();
        if (client.player == null || client.world == null) return;

        double rangeSquared = config.range * config.range;
        if (config.showPlayers) {
            for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                addIfInRange(client, player, rangeSquared, RadarTarget.Type.PLAYER);
            }
        }
        if (config.showMobs) {
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof MobEntity mob) {
                    addIfInRange(client, mob, rangeSquared, RadarTarget.Type.MOB);
                }
            }
        }
    }

    private void addIfInRange(MinecraftClient client, Entity entity, double rangeSquared, RadarTarget.Type type) {
        if (entity == client.player || entity.isRemoved()) return;
        double dx = entity.getX() - client.player.getX();
        double dz = entity.getZ() - client.player.getZ();
        double distanceSquared = dx * dx + dz * dz;
        if (distanceSquared <= rangeSquared) {
            targets.add(new RadarTarget(dx, dz, entity.getY() - client.player.getY(),
                    Math.sqrt(distanceSquared), type));
        }
    }

    public List<RadarTarget> targets() {
        return targets;
    }
}
