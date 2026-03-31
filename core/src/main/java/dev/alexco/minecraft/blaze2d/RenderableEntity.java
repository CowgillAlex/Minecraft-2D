package dev.alexco.minecraft.blaze2d;

import static dev.alexco.minecraft.SharedConstants.MAX_LIGHT_LEVEL;
import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.level.chunk.Chunk;

public class RenderableEntity {
    /**
     * Computes current sky brightness multiplier from the world day-night cycle.
     */
 public static float getSkyIntensity() {
        long ticks = Minecraft.getInstance().getTotalTicks() % 24000;

        if (ticks < 2000) {
            return 0.1f + 0.4f * (ticks / 2000f);
        } else if (ticks < 6000) {
            return 0.5f + 0.5f * ((ticks - 2000) / 4000f);
        } else if (ticks < 12000) {
            return 1.0f;
        } else if (ticks < 14000) {
            return 1.0f - 0.3f * ((ticks - 12000) / 2000f);
        } else if (ticks < 18000) {
            return 0.7f - 0.6f * ((ticks - 14000) / 4000f);
        } else {
            return 0.1f;
        }
    }

    /**
     * Samples combined light for the centre of an entity bounding box.
     */
    public static float getLightBrightness(Entity entity) {
        if (entity.bb == null) return 1.0f;//shhh
        int worldX = (int) Math.floor((entity.bb.x0 + entity.bb.x1) / 2.0);
        int worldY = (int) Math.floor((entity.bb.y0 + entity.bb.y1) / 2.0);
         if (Gdx.input.isKeyPressed(Keys.G)){
            return 15.0f;
        }
        return getLightBrightnessAt(worldX, worldY);
    }

    /**
     * Samples sky-light level for the centre of an entity bounding box.
     */
    public static int getSkyLight(Entity entity) {
        int worldX = (int) Math.floor((entity.bb.x0 + entity.bb.x1) / 2.0);
        int worldY = (int) Math.floor((entity.bb.y0 + entity.bb.y1) / 2.0);
        return getSkyLightAt(worldX, worldY);
    }

    /**
     * Reads sky-light value at a world coordinate, falling back to full light.
     */
    public static int getSkyLightAt(int worldX, int worldY) {
        int chunkX = World.getChunkX(worldX);
        int localX = World.getLocalX(worldX);

        Chunk chunk = Minecraft.getInstance().getWorld().getChunkIfExists(chunkX);
        if (chunk == null) return 15;

        return chunk.getSkyLightAt(localX, worldY) & 0xFF;
    }

    /**
     * Combines sky and block light at a world coordinate into a 0-1 brightness value.
     */
    public static float getLightBrightnessAt(int worldX, int worldY) {
        if (worldY < 0 || worldY >= CHUNK_HEIGHT) {
            return 1.0f;
        }
        int chunkX = World.getChunkX(worldX);
        int localX = World.getLocalX(worldX);

        Chunk chunk = Minecraft.getInstance().getWorld().getChunkIfExists(chunkX);
        if (chunk == null) return 1.0f;

        int skyLight = chunk.getSkyLightAt(localX, worldY) & 0xFF;
        int blockLight = chunk.getBlockLightAt(localX, worldY) & 0xFF;

        float skyBrightness = (float) skyLight / MAX_LIGHT_LEVEL * getSkyIntensity();
        float blockBrightness = (float) blockLight / MAX_LIGHT_LEVEL;

        return Math.min(1.0f, Math.max(skyBrightness, blockBrightness));
    }

}
