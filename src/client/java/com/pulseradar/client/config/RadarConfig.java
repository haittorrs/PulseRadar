package com.pulseradar.client.config;

public final class RadarConfig {
    public int configVersion = 2;
    public boolean enabled = true;
    public double range = 1000.0;
    public int diameter = 150;
    public int margin = 10;
    public double positionX = 1.0;
    public double positionY = 0.0;
    public boolean showElevation = true;
    public boolean showPlayers = true;
    public boolean showPlayerDistances = true;
    public int playerColor = 0xFF7CFF92;
    public boolean showMobs = false;
    public boolean showMobDistances = true;
    public int mobColor = 0xFFFF6B6B;

    public void validate() {
        range = Math.clamp(range, 10.0, 1000.0);
        diameter = Math.clamp(diameter, 90, 300);
        margin = Math.clamp(margin, 0, 100);
        positionX = Math.clamp(positionX, 0.0, 1.0);
        positionY = Math.clamp(positionY, 0.0, 1.0);
    }
}
