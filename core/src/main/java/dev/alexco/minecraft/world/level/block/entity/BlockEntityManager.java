package dev.alexco.minecraft.world.level.block.entity;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.World;

/**
 * Manages all block entities in the world.
 * Tracks block entities by their position and handles ticking.
 */
public class BlockEntityManager {
    private final World world;
    private final Map<Long, BlockEntity> blockEntities = new ConcurrentHashMap<>();
    
    public BlockEntityManager(World world) {
        this.world = world;
    }
    
    /**
     * Create a unique key for a block position
     */
    private static long getKey(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
    
    /**
     * Add a block entity at the specified position
     */
    public void addBlockEntity(int x, int y, BlockEntity blockEntity) {
        long key = getKey(x, y);
        BlockEntity existing = blockEntities.get(key);
        if (existing != null) {
            existing.onRemove();
        }
        blockEntities.put(key, blockEntity);
    }
    
    /**
     * Get the block entity at the specified position
     */
    public BlockEntity getBlockEntity(int x, int y) {
        return blockEntities.get(getKey(x, y));
    }
    
    /**
     * Check if there's a block entity at the specified position
     */
    public boolean hasBlockEntity(int x, int y) {
        return blockEntities.containsKey(getKey(x, y));
    }
    
    /**
     * Remove the block entity at the specified position
     * Calls onBlockBroken to drop items before removing
     */
    public void removeBlockEntity(int x, int y) {
        long key = getKey(x, y);
        BlockEntity entity = blockEntities.get(key);
        if (entity != null) {
            entity.onBlockBroken();
            entity.onRemove();
            blockEntities.remove(key);
        }
    }
    
    /**
     * Remove the block entity without dropping items (used when loading from save)
     */
    public void removeBlockEntityNoDrops(int x, int y) {
        long key = getKey(x, y);
        BlockEntity entity = blockEntities.remove(key);
        if (entity != null) {
            entity.onRemove();
        }
    }
    
    /**
     * Tick all block entities
     */
    public void tick() {
        for (BlockEntity entity : blockEntities.values()) {
            if (!entity.isRemoved()) {
                try {
                    entity.tick();
                } catch (Exception e) {
                    Logger.ERROR("Error ticking block entity at %d, %d: %s", 
                        entity.getX(), entity.getY(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get all block entities
     */
    public Collection<BlockEntity> getAllBlockEntities() {
        return blockEntities.values();
    }
    
    /**
     * Clear all block entities
     */
    public void clear() {
        for (BlockEntity entity : blockEntities.values()) {
            entity.onRemove();
        }
        blockEntities.clear();
    }
}
