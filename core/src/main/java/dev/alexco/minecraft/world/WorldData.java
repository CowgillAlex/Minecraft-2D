package dev.alexco.minecraft.world;

/**
 * WorldData contains the data that each individual world needs in order to
 * render properly in the state that it should be. It does not store a camera,
 * or any blocks, but the offset of the camera and how big the blocks should be
 * rendered in pixels
 */
public class WorldData {
    /**
     * The offset of the camera in the x axis.
     */
    public double cameraX;
    /**
     * The offset of the camera in the y axis.
     */
    public double cameraY;
    /**
     * Block Size is measured in pixels. Rendering will round down to the nearest
     * pixel, and resizing will round to the nearest whole number.
     */
    public float blockSize;
    /**
     * Number of random block updates to run per chunk tick.
     */
    public int randomTickSpeed = 3;
    /**
     * Whether passive mobs should spawn.
     */
    public boolean passiveMobSpawningEnabled = true;
    /**
     * Whether hostile mobs should spawn.
     */
    public boolean hostileMobSpawningEnabled = true;
    /**
     * The interval at which passive mobs spawn, in ticks.
     */
    public int passiveSpawnIntervalTicks = 120;
    /**
     * The interval at which hostile mobs spawn, in ticks.
     */
    public int hostileSpawnIntervalTicks = 8;
    /**
     * How many passive mobs can be around the player at a time.
     */
    public int passiveMobCap = 6;
    /**
     * How many hostile mobs can be around the player at a time.
     */
    public int hostileMobCap = 18;

}
