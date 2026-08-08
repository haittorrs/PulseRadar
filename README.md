# Pulse Radar

Pulse Radar is a client-only Fabric mod for Minecraft 1.21.11. It displays only player entities already tracked by the client as blips on a circular, camera-relative radar HUD. It never scans chunks, terrain, blocks, structures, or biomes.

## Controls

- Press **R** to toggle the radar. The binding can be changed in Minecraft's Controls screen.

## Configuration

After the first launch, edit `config/pulse-radar.json`:

- `range`: horizontal detection range in blocks (default 100)
- `diameter`: HUD diameter in scaled GUI pixels (default 150)
- `margin`: distance from the top-right corner
- `positionX` / `positionY`: proportional screen position (normally set through Mod Menu)
- `showElevation`: show ↑/↓ when a target differs by at least three blocks vertically
- `showPlayers` / `playerColor` / `showPlayerDistances`: player blip visibility, colour, and distance labels
- `showMobs` / `mobColor` / `showMobDistances`: loaded mob blip visibility, colour, and distance labels (mobs default off)

## Mod Menu integration

With Mod Menu 17.0.0 or newer installed, open **Mods → Pulse Radar → Configure**. Drag the radar to move it, or drag the white square on its lower-right edge to resize it. The same screen controls radar visibility and player/mob blip visibility and colours. Mod Menu is optional.

## Build

Use Java 21 and run `./gradlew build`. The mod JAR is written to `build/libs/`.
