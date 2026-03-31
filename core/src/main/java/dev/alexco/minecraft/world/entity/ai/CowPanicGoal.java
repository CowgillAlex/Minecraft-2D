package dev.alexco.minecraft.world.entity.ai;

import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Cow;
import dev.alexco.minecraft.world.entity.Mob;

public class CowPanicGoal implements AIGoal {
    /**
     * Drives panic movement while a cow is frightened.
     */
    @Override
    public void tick(Mob mob, World world) {
        if (!(mob instanceof Cow cow)) {
            return;
        }
        if (cow.getPanicTicks() <= 0) {
            return;
        }
        float dir = cow.getPanicDirection();
        cow.setMoveIntent(dir);
        MobMovementUtil.handleStepOrJump(cow, world, dir);
        cow.decrementPanic();
    }
}
