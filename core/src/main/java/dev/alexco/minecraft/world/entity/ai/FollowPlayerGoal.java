package dev.alexco.minecraft.world.entity.ai;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Mob;
import dev.alexco.minecraft.world.entity.Player;

public class FollowPlayerGoal implements AIGoal {
    private final float range;

    public FollowPlayerGoal(float range) {
        this.range = Math.max(1.0f, range);
    }

    /**
     * Moves a mob toward the player while inside follow range.
     */
    @Override
    public void tick(Mob mob, World world) {
        Player player = Minecraft.getInstance().getPlayer();
        if (player == null) {
            return;
        }

        double dx = player.x - mob.x;
        double dy = player.y - mob.y;
        double distSq = dx * dx + dy * dy;
        if (distSq > range * range) {
            return;
        }
        if (Math.abs(dx) < 0.1f) {
            mob.setMoveIntent(0.0f);
            return;
        }
        float dir = (float) Math.signum(dx);
        mob.setMoveIntent(dir);
        MobMovementUtil.handleStepOrJump(mob, world, dir);
    }
}
