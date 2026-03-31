package dev.alexco.minecraft.world.entity.spawn;

import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;

public class GrassSurfaceSpawnCondition implements SpawnCondition {
    /**
     * Requires two air blocks above grass with no fluid overlap.
     */
    @Override
    public boolean canSpawn(World world, int x, int y) {
        if (y <= 1 || y >= SharedConstants.CHUNK_HEIGHT - 2) {
            return false;
        }

        BlockState feet = world.getBlock(x, y);
        BlockState head = world.getBlock(x, y + 1);
        BlockState below = world.getBlock(x, y - 1);

        boolean airSpace = feet.getBlock().isAir() && head.getBlock().isAir();
        boolean onGrass = below.getBlock().equals(Blocks.GRASS_BLOCK);
        boolean notInFluid = !BlockTags.FLUID.contains(feet.getBlock()) && !BlockTags.FLUID.contains(head.getBlock());
        return airSpace && onGrass && notInFluid;
    }
}
