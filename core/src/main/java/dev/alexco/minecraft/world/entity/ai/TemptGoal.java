package dev.alexco.minecraft.world.entity.ai;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Mob;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.Items;

public abstract class TemptGoal implements AIGoal {
    private final float range;

    protected TemptGoal(float range) {
        this.range = Math.max(1.0f, range);
    }

    /**
     * Moves mobs toward players holding a valid tempting item.
     */
    @Override
    public void tick(Mob mob, World world) {
        Player player = Minecraft.getInstance().getPlayer();
        if (player == null || !isPlayerTempting(player)) {
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
            onTemptingPlayerNearby(mob, player);
            return;
        }
        float dir = (float) Math.signum(dx);
        mob.setMoveIntent(dir);
        MobMovementUtil.handleStepOrJump(mob, world, dir);
        onTemptingPlayerNearby(mob, player);
    }

    protected void onTemptingPlayerNearby(Mob mob, Player player) {
    }

    protected abstract boolean isTemptingItem(Item item);

    /**
     * Checks both main hand and offhand for tempting items.
     */
    private boolean isPlayerTempting(Player player) {
        if (isTemptingItem(player.blockInHand)) {
            return true;
        }
        ItemStack offhandStack = player.inventory.getItemAtSlot(40);
        Item offhandItem = offhandStack != null ? offhandStack.item : Items.AIR;
        return isTemptingItem(offhandItem);
    }
}
