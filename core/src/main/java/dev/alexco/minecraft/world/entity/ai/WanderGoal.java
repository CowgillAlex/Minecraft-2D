package dev.alexco.minecraft.world.entity.ai;

import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Mob;

public class WanderGoal implements AIGoal {
    private final int radius;
    private int cooldown = 0;
    private double targetX;

    public WanderGoal(int radius) {
        this.radius = Math.max(2, radius);
    }

    /**
     * Periodically picks a nearby target X and walks toward it.
     */
    @Override
    public void tick(Mob mob, World world) {
        if (cooldown > 0) {
            cooldown--;
        }

        if (cooldown <= 0 || Math.abs(mob.x - targetX) < 0.35f) {
            targetX = mob.x + (mob.randomInt(radius * 2 + 1) - radius);
            cooldown = 60 + mob.randomInt(120);
        }

        float dir = (float) Math.signum(targetX - mob.x);
        if (Math.abs(targetX - mob.x) > 0.2f) {
            mob.setMoveIntent(dir);
        }

        MobMovementUtil.handleStepOrJump(mob, world, dir);
    }
}
