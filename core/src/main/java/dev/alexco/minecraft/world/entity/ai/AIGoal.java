package dev.alexco.minecraft.world.entity.ai;

import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Mob;

public interface AIGoal {
    void tick(Mob mob, World world);
}
