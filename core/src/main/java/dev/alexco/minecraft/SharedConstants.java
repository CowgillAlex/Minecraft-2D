package dev.alexco.minecraft;

public class SharedConstants {
    /**
     * How tall a chunk should be.
     */
    public static final int CHUNK_HEIGHT = 384;
    /**
     * How wide a chunk should be.
     */
    public static final int CHUNK_WIDTH = 16;
    /**
     * How big a block texture is
     */
    public static final float BLOCK_PIXEL_SIZE = 16f;
    /**
     * How far should the player be able to see. It is left aligned.
     */
    public static final int TICKING_DISTANCE = 12;
    /**
     * How far should the player be able to see. It is centred around the player.
     */
    public static final int RENDER_DISTANCE = 16;
    /**
     * How far should the player be able to see. It is centred around the player and is used for world generation.
     */
    public static final int WORLDGEN_DISTANCE = 22;
    /**
     * How fast should the game run.
     */
    public static final int FPS_CAP = 60;
    /**
     * The maximum light level in the game.
     */
    public static final int MAX_LIGHT_LEVEL = 15;
}
