package dev.alexco.minecraft.world.entity.ai;

import dev.alexco.minecraft.world.entity.Cow;
import dev.alexco.minecraft.world.entity.Mob;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.Items;

public class CowTemptGoal extends TemptGoal {
    public CowTemptGoal(float range) {
        super(range);
    }

    /**
     * Marks cows as tempted while a valid player lure is nearby.
     */
    @Override
    protected void onTemptingPlayerNearby(Mob mob, Player player) {
        if (!(mob instanceof Cow cow)) {
            return;
        }
        cow.markTempted();
    }

    /**
     * Accepts wheat items as tempting food.
     */
    @Override
    protected boolean isTemptingItem(Item item) {
        return item != null && (item.equals(Items.WHEAT_ITEM) || item.equals(Items.WHEAT));
    }
}
