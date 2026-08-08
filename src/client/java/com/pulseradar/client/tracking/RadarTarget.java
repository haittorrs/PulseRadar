package com.pulseradar.client.tracking;

public record RadarTarget(double relativeX, double relativeZ, double relativeY, double distance, Type type) {
    public enum Type { PLAYER, MOB }
}
