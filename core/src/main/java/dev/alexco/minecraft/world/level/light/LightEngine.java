package dev.alexco.minecraft.world.level.light;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;
import static dev.alexco.minecraft.SharedConstants.CHUNK_WIDTH;
import static dev.alexco.minecraft.SharedConstants.MAX_LIGHT_LEVEL;

import com.badlogic.gdx.Gdx;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;

public class LightEngine {
    private static final int SKY_HORIZONTAL_PENALTY = 1;

    private final Chunk chunk;
    private final World world;
    private final int chunkX;

    private final Queue<int[]> blockLightQueue = new ArrayDeque<>();
    private final Queue<int[]> skyLightQueue = new ArrayDeque<>();
    private final Set<Integer> modifiedChunks = new HashSet<>();

    public LightEngine(Chunk chunk) {
        this.chunk = chunk;
        this.world = Minecraft.getInstance() != null ? Minecraft.getInstance().getWorld() : null;
        this.chunkX = chunk.getChunkPos().x;
    }

    /**
     * Recalculates sky and block light around this chunk.
     */
    public void calculateLight() {
        // Keep generation-time lighting consistent with runtime relight behaviour:
        // recalc current chunk + ready neighbours to avoid border propagation artifacts.
        fullRecalculate();
        rebuildModifiedChunks();
    }

    /**
     * Schedules mesh rebuilds for chunks modified by lighting.
     */
    private void rebuildModifiedChunks() {
        if (world == null) return;

        Gdx.app.postRunnable(() -> {
            for (int cx : modifiedChunks) {
                Chunk c = world.getChunkIfExists(cx);
                if (c != null && !c.isUnloaded()) {
                    c.build();
                }
            }
        });
    }



    private int getLightFilter(Block block) {
        return Math.max(0, Math.min(MAX_LIGHT_LEVEL, block.getLightFilter()));
    }

    private int getSkyLoss(Block block, int extraPenalty) {
        return getLightFilter(block) + extraPenalty;
    }

    private int getBlockLoss(Block block) {
        // Block light must always decay by at least 1 per step to preserve finite radius.
        return Math.max(1, getLightFilter(block));
    }

    /**
     * Flood-fills queued skylight updates across neighbours.
     */
    private void propagateSkyLight() {
        while (!skyLightQueue.isEmpty()) {
            int[] pos = skyLightQueue.poll();
            int worldX = pos[0];
            int y = pos[1];

            int current = getSkyLightWorld(worldX, y);
            if (current <= 0) continue;

            // Downward first, then both horizontal directions: flood-fill style.
            tryPropagateSky(worldX, y - 1, current, 0);
            tryPropagateSky(worldX - 1, y, current, SKY_HORIZONTAL_PENALTY);
            tryPropagateSky(worldX + 1, y, current, SKY_HORIZONTAL_PENALTY);
        }
    }

    /**
     * Applies one skylight propagation step into a target position.
     */
    private void tryPropagateSky(int worldX, int y, int sourceLight, int extraPenalty) {
        if (y < 0 || y >= CHUNK_HEIGHT) return;

        int targetChunkX = World.getChunkX(worldX);
        int localX = World.getLocalX(worldX);

        Chunk target = getWritableChunk(targetChunkX);
        if (target == null) return;

        Block targetBlock = target.getBlockAt(localX, y).getBlock();
        int newLight = sourceLight - getSkyLoss(targetBlock, extraPenalty);
        if (newLight <= 0) return;

        int existing = target.getSkyLightAt(localX, y) & 0xFF;
        if (newLight > existing) {
            target.getChunkData().setSkyLightAt(localX, y, (byte) newLight);
            skyLightQueue.add(new int[] { worldX, y });
            modifiedChunks.add(targetChunkX);
        }
    }

    private int getSkyLightWorld(int worldX, int y) {
        int targetChunkX = World.getChunkX(worldX);
        int localX = World.getLocalX(worldX);

        Chunk target = getWritableChunk(targetChunkX);
        if (target == null) return 0;

        return target.getSkyLightAt(localX, y) & 0xFF;
    }

    /**
     * Flood-fills queued block-light updates from emitting blocks.
     */
    private void propagateBlockLight() {
        while (!blockLightQueue.isEmpty()) {
            int[] pos = blockLightQueue.poll();
            int worldX = pos[0];
            int y = pos[1];

            int currentLight = getBlockLightWorld(worldX, y);
            if (currentLight <= 0) continue;

            tryPropagate(worldX - 1, y, currentLight);
            tryPropagate(worldX + 1, y, currentLight);
            tryPropagate(worldX, y - 1, currentLight);
            tryPropagate(worldX, y + 1, currentLight);
        }
    }

    /**
     * Applies one block-light propagation step into a target position.
     */
    private void tryPropagate(int worldX, int y, int sourceLight) {
        if (y < 0 || y >= CHUNK_HEIGHT) return;

        int targetChunkX = World.getChunkX(worldX);
        int localX = World.getLocalX(worldX);

        Chunk target = getWritableChunk(targetChunkX);
        if (target == null) return;

        Block targetBlock = target.getBlockAt(localX, y).getBlock();
        int newLight = sourceLight - getBlockLoss(targetBlock);
        if (newLight <= 0) return;

        int existing = target.getBlockLightAt(localX, y) & 0xFF;
        if (existing < newLight) {
            target.getChunkData().setBlockLightAt(localX, y, (byte) newLight);
            modifiedChunks.add(targetChunkX);
            blockLightQueue.add(new int[]{worldX, y});
        }
    }

    private int getBlockLightWorld(int worldX, int y) {
        int targetChunkX = World.getChunkX(worldX);
        int localX = World.getLocalX(worldX);

        Chunk target = getWritableChunk(targetChunkX);
        if (target == null) return 0;

        return target.getBlockLightAt(localX, y) & 0xFF;
    }

    /**
     * Returns this chunk or a loaded neighbour suitable for writing light.
     */
    private Chunk getWritableChunk(int targetChunkX) {
        if (targetChunkX == chunkX) return chunk;
        if (world == null) return null;

        Chunk neighbour = world.getChunkIfExists(targetChunkX);
        if (neighbour == null) return null;
        if (!neighbour.getStatus().isAtLeast(ChunkStatus.FEATURES)) return null;

        return neighbour;
    }

    /**
     * Re-lights around a world position after a block change.
     */
    public static void updateLightAt(int worldX, int worldY) {
        World world = Minecraft.getInstance().getWorld();
        if (world == null) return;

        int centerChunkX = World.getChunkX(worldX);

        Chunk centerChunk = world.getChunkIfExists(centerChunkX);
        if (centerChunk == null || !centerChunk.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
            return;
        }

        LightEngine engine = new LightEngine(centerChunk);
        engine.fullRecalculate();
        engine.rebuildModifiedChunks();
    }

    /**
     * Clears and recomputes sky and block light for centre and border chunks.
     */
    private void fullRecalculate() {
        modifiedChunks.clear();
        blockLightQueue.clear();
        skyLightQueue.clear();

        recalculateSkyLightInChunk(chunk);
        modifiedChunks.add(chunkX);

        Chunk left = getWritableChunk(chunkX - 1);
        if (left != null) {
            recalculateSkyLightInChunk(left);
            modifiedChunks.add(chunkX - 1);
        }

        Chunk right = getWritableChunk(chunkX + 1);
        if (right != null) {
            recalculateSkyLightInChunk(right);
            modifiedChunks.add(chunkX + 1);
        }

        propagateSkyLightCrossChunk();

        clearBlockLightInChunk(chunk);
        if (left != null) clearBlockLightInChunk(left);
        if (right != null) clearBlockLightInChunk(right);

        findLightSourcesInChunk(chunk);
        if (left != null) findLightSourcesInChunk(left);
        if (right != null) findLightSourcesInChunk(right);

        propagateBlockLight();
    }

    /**
     * Seeds skylight for a chunk and clears previous values.
     */
    private void recalculateSkyLightInChunk(Chunk target) {
        int targetChunkX = target.getChunkPos().x;
        int topY = CHUNK_HEIGHT - 1;

        // Reset current skylight before flood-fill.
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                target.getChunkData().setSkyLightAt(x, y, (byte) 0);
            }
        }

        // Seed from sky at top row and let flood-fill handle the rest.
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            Block topBlock = target.getBlockAt(x, topY).getBlock();
            int seed = MAX_LIGHT_LEVEL - getLightFilter(topBlock);
            if (seed <= 0) continue;

            target.getChunkData().setSkyLightAt(x, topY, (byte) seed);
            int worldX = targetChunkX * CHUNK_WIDTH + x;
            skyLightQueue.add(new int[] { worldX, topY });
            modifiedChunks.add(targetChunkX);
        }
    }

    /**
     * Continues queued skylight updates across chunk boundaries.
     */
    private void propagateSkyLightCrossChunk() {
        propagateSkyLight();
    }

    /**
     * Resets all block-light values in a chunk.
     */
    private void clearBlockLightInChunk(Chunk target) {
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                target.getChunkData().setBlockLightAt(x, y, (byte) 0);
            }
        }
    }

    /**
     * Queues emissive blocks as initial block-light sources.
     */
    private void findLightSourcesInChunk(Chunk target) {
        int targetChunkX = target.getChunkPos().x;
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                BlockState state = target.getBlockAt(x, y);
                int emission = state.getLightEmission();
                if (emission > 0) {
                    target.getChunkData().setBlockLightAt(x, y, (byte) emission);
                    int worldX = targetChunkX * CHUNK_WIDTH + x;
                    blockLightQueue.add(new int[]{worldX, y});
                }
            }
        }
    }

    public static void onChunkLoaded(Chunk loadedChunk) {
    }
}
