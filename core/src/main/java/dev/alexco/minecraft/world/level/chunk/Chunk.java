package dev.alexco.minecraft.world.level.chunk;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;
import static dev.alexco.minecraft.SharedConstants.CHUNK_WIDTH;
import static dev.alexco.minecraft.SharedConstants.RENDER_DISTANCE;
import static dev.alexco.minecraft.SharedConstants.WORLDGEN_DISTANCE;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.badlogic.gdx.Gdx;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.MeshChunk;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.levelgen.ChunkGenerator;
import dev.alexco.minecraft.world.biome.Biome;

public class Chunk {
    private final ChunkPos chunkPos;

    private final ChunkData chunkData;
    public volatile boolean isGenerating;
    private boolean isDirty;
    public ChunkGenerator chunkGenerator;
        private ChunkStatus status = ChunkStatus.EMPTY;
    private ChunkTicker chunkTicker;
    public MeshChunk chunkMesh;
    private List<Entity> entities;
    private volatile boolean unloaded;

    private final int[] surfaceYCache = new int[CHUNK_WIDTH];
    private final Biome[] biomeCache = new Biome[CHUNK_WIDTH];
    private boolean surfaceCachePopulated = false;
    private boolean biomeCachePopulated = false;

    // Timing metrics for each generation step (in milliseconds)
    private final Map<ChunkStatus, Long> generationTimings = new EnumMap<>(ChunkStatus.class);

    public Chunk(int chunkX) {
        this.chunkPos = new ChunkPos(chunkX);
        this.chunkData = new ChunkData(chunkX);
        this.chunkGenerator = new ChunkGenerator(this);
        this.chunkTicker = new ChunkTicker(this);
        this.isDirty = true;
        this.entities = new CopyOnWriteArrayList<>();
        chunkMesh = new MeshChunk(this, Minecraft.getInstance().atlas);
        this.unloaded = false;


    }

    public Chunk(ChunkPos pos) {
        this(pos.x);
    }

    public ChunkData getChunkData() {
        return this.chunkData;
    }

    public ChunkStatus getStatus() {
        return this.status;
    }

    public void setStatus(ChunkStatus status) {
        this.status = status;
        this.isDirty = true;
    }

    /**
     * Returns true if this chunk can advance toward a target status.
     */
    public boolean canGenerateTo(ChunkStatus targetStatus) {
        if (isGenerating || status.ordinal() >= targetStatus.ordinal()) {
            return false;
        }
        return true;
    }
    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    public boolean amiGenerating() {
        return this.isGenerating;
    }

    /**
     * Releases render/entity resources and detaches chunk entities from world.
     */
    public void unload(dev.alexco.minecraft.world.World world) {
        unloaded = true;
        isGenerating = false;
        isDirty = false;
        if (chunkMesh != null) {
            chunkMesh.dispose();
        }
        for (Entity entity : entities) {
            entity.destroy();
            world.entities.remove(entity);
        }
        entities.clear();
    }

    /**
     * Returns the block state at local coordinates without neighbour remapping.
     */
    public synchronized BlockState unsafeGetBlockAt(int x, int y) {
        if (x < 0 || y < 0 || x>CHUNK_WIDTH-1 || y > CHUNK_HEIGHT-1) return null;
        return chunkData.unsafeGetBlockAt(x, y);
    }
    public synchronized BlockState getBlockAt(int x, int y) {
        return chunkData.getBlockAt(x, y);
    }
    public synchronized BlockState getBlockAt(int x, int y, boolean unsafe) {
        return chunkData.unsafeGetBlockAt(x, y);
    }

    public synchronized BlockState getBackgroundBlockAt(int x, int y) {
        return chunkData.getBackgroundBlockAt(x, y);
    }

    public synchronized void setBlockAt(int x, int y, BlockState blockState) {
        chunkData.setBlockAt(x, y, blockState);
        this.isDirty = true;
    }
    public synchronized void safeSetBlockAt(int x, int y, BlockState blockState) {
        if (y < 0 || y > CHUNK_HEIGHT) return;
        chunkData.setBlockAt(x, y, blockState);
        this.isDirty = true;
    }

    public synchronized void setBlockAt(int x, int y, BlockState blockState, boolean setDirty) {
        chunkData.setBlockAt(x, y, blockState);
        if (setDirty) {
            this.isDirty = true;
        }
    }

    public synchronized void setBackgroundBlockAt(int x, int y, BlockState blockState) {
        chunkData.setBackgroundBlockAt(x, y, blockState);
        this.isDirty = true;
    }

    public synchronized byte getSkyLightAt(int x, int y) {
        return chunkData.getSkyLightAt(x, y);
    }

    public synchronized void setSkyLightAt(int x, int y, byte level) {
        chunkData.setSkyLightAt(x, y, level);
        this.isDirty = true;
    }

    public synchronized byte getBlockLightAt(int x, int y) {
        return chunkData.getBlockLightAt(x, y);
    }

    public synchronized void setBlockLightAt(int x, int y, byte level) {
        chunkData.setBlockLightAt(x, y, level);
        this.isDirty = true;
    }

    public ChunkPos getPos() {
        return this.chunkPos;
    }

    public boolean amIInRenderDistance() {
       int camChunkX = (int) (Math.floor(Minecraft.getInstance().getWorld().worldData.cameraX + Gdx.graphics.getWidth()/2f)
                / (CHUNK_WIDTH * Minecraft.getInstance().getWorld().worldData.blockSize));
        int chunkX = this.chunkPos.x;
        return Math.abs(chunkX - camChunkX) <= RENDER_DISTANCE;
    }

    public boolean amIInWorldgenDistance() {
       int camChunkX = (int) (Math.floor(Minecraft.getInstance().getWorld().worldData.cameraX + Gdx.graphics.getWidth()/2f)
                / (CHUNK_WIDTH * Minecraft.getInstance().getWorld().worldData.blockSize));
        int chunkX = this.chunkPos.x;
        return Math.abs(chunkX - camChunkX) <= WORLDGEN_DISTANCE;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public boolean amIDirty() {
        return this.isDirty;
    }

    /**
     * Rebuilds chunk mesh data from current block and light state.
     */
    public void build() {
        if (unloaded) {
            return;
        }
        try {
            if (chunkMesh != null) {
                chunkMesh.build();
            }
            this.isDirty = false;
            isGenerating = false;
        } catch (Exception e) {
            Logger.ERROR("Could not build buffer for chunk %d because %s" , chunkPos.x, e.toString());
            // if (frameBuffer==null){
            //     Logger.INFO("I am going to try and remake the buffer, if it fails then oh well");
            //     frameBuffer = new ChunkFrameBuffer(this);
            //     build();
            // }
            //!todo work out why
        }
    }

    public boolean isUnloaded() {
        return unloaded;
    }

    /**
     * Records the time taken for a specific generation step.
     * @param status The status that was just completed
     * @param timeMs Time in milliseconds
     */
    public void recordGenerationTiming(ChunkStatus status, long timeMs) {
        generationTimings.put(status, timeMs);
    }

    /**
     * Gets the timing for a specific generation step.
     * @param status The status to get timing for
     * @return Time in milliseconds, or 0 if not recorded
     */
    public long getGenerationTiming(ChunkStatus status) {
        return generationTimings.getOrDefault(status, 0L);
    }

    /**
     * Gets all recorded generation timings.
     * @return Map of status to timing in milliseconds
     */
    public Map<ChunkStatus, Long> getAllGenerationTimings() {
        return new EnumMap<>(generationTimings);
    }

    /**
     * Gets the total generation time for all completed steps.
     * @return Total time in milliseconds
     */
    public long getTotalGenerationTime() {
        return generationTimings.values().stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Ticks random block updates and entities owned by this chunk.
     */
    public synchronized void tick() {
        if (isGenerating)
            return;

        this.chunkTicker.tick();

        // Tick all entities in this chunk
        for (Entity entity : new ArrayList<>(entities)) {
            if (entity.removed) continue;
            entity.tick(0);
        }
        for (Entity entity : entities) {
            if (entity.removed) {
                entity.destroy();
            }
        }
        entities.removeIf(entity -> entity.removed);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        this.isDirty = true;
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
        this.isDirty = true;
    }

    public int getSurfaceY(int localX) {
        if (!surfaceCachePopulated || localX < 0 || localX >= CHUNK_WIDTH) {
            return -1;
        }
        return surfaceYCache[localX];
    }

    public void setSurfaceY(int localX, int surfaceY) {
        if (localX >= 0 && localX < CHUNK_WIDTH) {
            surfaceYCache[localX] = surfaceY;
        }
    }

    public void markSurfaceCachePopulated() {
        this.surfaceCachePopulated = true;
    }

    public boolean isSurfaceCachePopulated() {
        return surfaceCachePopulated;
    }

    public Biome getBiome(int localX) {
        if (!biomeCachePopulated || localX < 0 || localX >= CHUNK_WIDTH) {
            return null;
        }
        return biomeCache[localX];
    }

    public void setBiome(int localX, Biome biome) {
        if (localX >= 0 && localX < CHUNK_WIDTH) {
            biomeCache[localX] = biome;
        }
    }

    public void markBiomeCachePopulated() {
        this.biomeCachePopulated = true;
    }

    public boolean isBiomeCachePopulated() {
        return biomeCachePopulated;
    }

    /**
     * Generate chunk to specific status
     */
    public void generateToStatus(ChunkStatus targetStatus) {
        if (unloaded || status.isAtLeast(targetStatus)) {
            return; // Already at or past this status
        }

        isGenerating = true;

        try {
            ChunkStatus current = status;

            // Progress through each status sequentially
            while (current.ordinal() < targetStatus.ordinal()) {
                if (unloaded) {
                    return;
                }
                ChunkStatus next = current.getNext();

                // Record timing for this generation step
                long startTime = System.nanoTime();
                try {
                    chunkGenerator.generateStatus(next);
                } catch (Exception e) {
                    // Critical: Log on System.err to ensure visibility in background threads
                    System.err.println("[CHUNK GENERATION ERROR] Chunk " + chunkPos + " failed at status " + next + ": " + e.getMessage());
                    e.printStackTrace();
                    // Re-throw to stop generation and make error visible
                    throw new RuntimeException("Chunk generation failed for " + chunkPos + " at " + next, e);
                }
                long endTime = System.nanoTime();
                long durationMs = (endTime - startTime) / 1_000_000;
                recordGenerationTiming(next, durationMs);

                setStatus(next);
                current = next;
            }
        } finally {
            isGenerating = false;
        }
    }

}
