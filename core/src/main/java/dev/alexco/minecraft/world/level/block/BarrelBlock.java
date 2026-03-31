package dev.alexco.minecraft.world.level.block;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.level.block.entity.BarrelBlockEntity;
import dev.alexco.minecraft.world.level.block.entity.BlockEntity;

public class BarrelBlock extends Block {

    public BarrelBlock(Properties properties) {
        super(properties);
    }

    /**
     * Creates a barrel block entity when placed.
     */
    public void onPlace(int x, int y) {
        if (Minecraft.getInstance().getWorld() != null) {
            // Only create if doesn't exist
            if (!Minecraft.getInstance().getWorld().getBlockEntityManager().hasBlockEntity(x, y)) {
                BarrelBlockEntity blockEntity = new BarrelBlockEntity(x, y);
                Minecraft.getInstance().getWorld().getBlockEntityManager().addBlockEntity(x, y, blockEntity);
            }
        }
    }

    /**
     * Removes the barrel block entity at this position.
     */
    public void onRemove(int x, int y) {
        if (Minecraft.getInstance().getWorld() != null) {
            Minecraft.getInstance().getWorld().getBlockEntityManager().removeBlockEntity(x, y);
        }
    }

    /**
     * Returns the barrel block entity at a position if present.
     */
    public static BarrelBlockEntity getBlockEntity(int x, int y) {
        if (Minecraft.getInstance().getWorld() != null) {
            BlockEntity entity = Minecraft.getInstance().getWorld().getBlockEntityManager().getBlockEntity(x, y);
            if (entity instanceof BarrelBlockEntity) {
                return (BarrelBlockEntity) entity;
            }
        }
        return null;
    }
}
