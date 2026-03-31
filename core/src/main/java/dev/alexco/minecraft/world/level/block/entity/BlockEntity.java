package dev.alexco.minecraft.world.level.block.entity;

import net.querz.nbt.tag.CompoundTag;

/**
 * Base class for all block entities.
 * Block entities hold data for blocks like inventories, processing progress, etc.
 */
public abstract class BlockEntity {
    protected int x;
    protected int y;
    protected boolean removed = false;

    public BlockEntity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Called every tick to update the block entity
     */
    public abstract void tick();

    /**
     * Save the block entity's data to NBT
     */
    public abstract CompoundTag saveToNBT(CompoundTag tag);

    /**
     * Load the block entity's data from NBT
     */
    public abstract void loadFromNBT(CompoundTag tag);

    /**
     * Called when the block entity is removed from the world
     */
    public void onRemove() {
        this.removed = true;
    }

    /**
     * Called when the block containing this entity is broken
     * Should drop contents into the world
     */
    public abstract void onBlockBroken();

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isRemoved() {
        return removed;
    }

    /**
     * Get the type identifier for this block entity
     */
    public abstract String getTypeId();
}
