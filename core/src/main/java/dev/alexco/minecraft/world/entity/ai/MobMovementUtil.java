package dev.alexco.minecraft.world.entity.ai;

import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Mob;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Solidity;

public final class MobMovementUtil {
    private MobMovementUtil() {
    }

    /**
     * Applies simple ledge/obstacle handling for horizontal mob movement.
     */
    public static void handleStepOrJump(Mob mob, World world, float dir) {
        if (Math.abs(dir) < 0.01f) {
            return;
        }
        int aheadX = (int) Math.floor(mob.x + dir * 0.6f);
        int feetY = (int) Math.floor(mob.y);
        if (feetY <= 1 || feetY >= SharedConstants.CHUNK_HEIGHT - 2) {
            return;
        }

        BlockState aheadFeet = world.getBlock(aheadX, feetY);
        BlockState aheadHead = world.getBlock(aheadX, feetY + 1);
        BlockState aheadTwo = world.getBlock(aheadX, feetY + 2);
        BlockState aheadFloor = world.getBlock(aheadX, feetY - 1);

        boolean feetBlocked = isWalkBlocking(aheadFeet);
        boolean oneAboveClear = !isWalkBlocking(aheadHead);
        boolean twoAboveClear = !isWalkBlocking(aheadTwo);
        boolean canLandAhead = isWalkBlocking(aheadFloor);

        if (feetBlocked && oneAboveClear && twoAboveClear && canLandAhead) {
            mob.requestJump();
            return;
        }
        if (feetBlocked && !oneAboveClear) {
            mob.setMoveIntent(0.0f);
        }
    }

    /**
     * Returns true when a state blocks walking movement.
     */
    private static boolean isWalkBlocking(BlockState state) {
        Solidity solidity = state.getBlock().getSolidity();
        return solidity == Solidity.SOLID || solidity == Solidity.SCAFFOLD;
    }
}
