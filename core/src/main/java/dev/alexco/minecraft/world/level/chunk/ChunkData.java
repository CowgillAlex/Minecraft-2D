package dev.alexco.minecraft.world.level.chunk;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;

import static dev.alexco.minecraft.SharedConstants.*;

import dev.alexco.minecraft.Minecraft;

public class ChunkData {
    /**
     * A Chunk is comprised of blocks.
     */

    private final BlockState[] backgroundBlocks = new BlockState[CHUNK_HEIGHT * CHUNK_WIDTH];
    private final BlockState[] blocks = new BlockState[CHUNK_HEIGHT * CHUNK_WIDTH];
    private final byte[] skyLight = new byte[CHUNK_HEIGHT * CHUNK_WIDTH];
    private final byte[] blockLight = new byte[CHUNK_HEIGHT * CHUNK_WIDTH];
    private final int chunkX;
    public ChunkData(int chunkX) {
        this.chunkX = chunkX;
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = Blocks.AIR.defaultBlockState();
            backgroundBlocks[i] = Blocks.AIR.defaultBlockState();
            skyLight[i] = MAX_LIGHT_LEVEL;
            blockLight[i] = 0;
        }
    }

    /**
     * Converts coordinates to an index
     *
     * @param x
     * @param y
     * @return
     */
    private int index(int x, int y) {
        return CHUNK_WIDTH * y + x;
    }

    /**
     * Returns true when local coordinates are inside chunk bounds.
     */
    private boolean amIInRange(int x, int y) {
        if (x < 0 || x >= CHUNK_WIDTH || y < 0 || y >= CHUNK_HEIGHT) {
            return false;
        }
        return true;
    }

    private void throwIfOutOfBounds(int x, int y) {
        if (!amIInRange(x, y)) {
            throw new IndexOutOfBoundsException(
                    String.format("Out of bounds @%s, %s: %s isnt in the array!", x, y, index(x, y)));
        }
    }

    /**
     * Gets a block, delegating to neighbour chunks for cross-border x values.
     */
    public BlockState getBlockAt(int x, int y) {

        if (x < 0 || x >= CHUNK_WIDTH) {
    int worldX = chunkX * CHUNK_WIDTH + x;
    int neighbourChunkX = World.getChunkX(worldX);
    int localX = World.getLocalX(worldX);

    return Minecraft.getInstance().getWorld().worldChunks
        .get(new ChunkPos(neighbourChunkX))
        .getBlockAt(localX, y);
}

        return blocks[index(x, y)];
    }

    /**
     * Gets a block without bounds checks.
     */
    public BlockState unsafeGetBlockAt(int x, int y) {



        return blocks[index(x, y)];
    }

    public BlockState getBackgroundBlockAt(int x, int y) {
        throwIfOutOfBounds(x, y);
        return backgroundBlocks[index(x, y)];
    }

   /**
    * Sets a block, delegating to neighbour chunks for cross-border x values.
    */
   public void setBlockAt(int x, int y, BlockState blockState) {
    if (x < 0 || x >= CHUNK_WIDTH) {
        int worldX = chunkX * CHUNK_WIDTH + x;
        int neighbourChunkX = World.getChunkX(worldX);
        int localX = Math.floorMod(worldX, CHUNK_WIDTH);

        Minecraft.getInstance().getWorld().getChunk(new ChunkPos(neighbourChunkX))
            .setBlockAt(localX, y, blockState);
        return;
    }

    blocks[index(x, y)] = blockState;
}

    public void setBackgroundBlockAt(int x, int y, BlockState blockState) {
        throwIfOutOfBounds(x, y);
        backgroundBlocks[index(x, y)] = blockState;
    }

    public byte getSkyLightAt(int x, int y) {
        throwIfOutOfBounds(x, y);
        return skyLight[index(x, y)];
    }

    public void setSkyLightAt(int x, int y, byte level) {
        throwIfOutOfBounds(x, y);
        skyLight[index(x, y)] = level;
    }

    public byte getBlockLightAt(int x, int y) {
        throwIfOutOfBounds(x, y);
        return blockLight[index(x, y)];
    }

    public void setBlockLightAt(int x, int y, byte level) {
        throwIfOutOfBounds(x, y);
        blockLight[index(x, y)] = level;
    }

    public byte[] getSkyLightArray() {
        return skyLight;
    }

    public byte[] getBlockLightArray() {
        return blockLight;
    }

    public void setSkyLightArray(byte[] data) {
        System.arraycopy(data, 0, skyLight, 0, Math.min(data.length, skyLight.length));
    }

    public void setBlockLightArray(byte[] data) {
        System.arraycopy(data, 0, blockLight, 0, Math.min(data.length, blockLight.length));
    }
}
