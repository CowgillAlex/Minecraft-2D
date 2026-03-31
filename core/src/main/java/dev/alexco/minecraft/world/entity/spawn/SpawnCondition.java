package dev.alexco.minecraft.world.entity.spawn;

import dev.alexco.minecraft.world.World;

public interface SpawnCondition {
    boolean canSpawn(World world, int x, int y);
}
