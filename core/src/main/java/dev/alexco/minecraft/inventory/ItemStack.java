package dev.alexco.minecraft.inventory;

import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.BlockItem;

public class ItemStack {
    public Item item;//this is a variable
    public int amount; //this is a variable
    public int damage;

    public ItemStack(Item item, int amount) {
        this(item, amount, 0);
    }

    public ItemStack(Item item, int amount, int damage) {
        this.item = item;
        this.amount = amount;
        this.damage = Math.max(0, damage);
    }

    public ItemStack(Item item) {
        this(item, 1, 0);
    }

    public ItemStack(ItemStack other) {
        this(other.item, other.amount, other.damage);
    }

    /**
     * Merges another stack into this one and returns any overflow.
     */
    public ItemStack merge(ItemStack other) {
        String thisDesc = getItemDescription(this.item);
        String otherDesc = getItemDescription(other.item);
        //Logger.INFO("Attempting to merge %s to  %s", thisDesc, otherDesc);

        // we need to attempt to put the number of items into this stack
        // but we also need to return the items that we CANNOT put into this stack.
        int maxItems = this.item.getMaxStackSize();
        Logger.INFO("%s", maxItems);
        if (!this.item.equals(other.item)) {
            return other; // Can't merge different items - return unchanged
        }
        if (this.item.canBeDepleted() && this.damage != other.damage) {
            return other; // Can't merge damaged tools with different durability - return unchanged
        }
        if (this.amount + other.amount > maxItems) {
            int amountToBe = this.amount + other.amount;
            this.amount = maxItems;
            return new ItemStack(this.item, amountToBe - maxItems, this.damage);
        } else {
            this.amount += other.amount;
            return null;
        }
    }

    private static String getItemDescription(Item item) {
        if (item instanceof BlockItem) {
            return ((BlockItem) item).getFullDescriptionWithProperties();
        }
        return item.getDescriptionId();
    }
}
