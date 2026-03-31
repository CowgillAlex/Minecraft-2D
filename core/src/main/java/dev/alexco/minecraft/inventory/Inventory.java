package dev.alexco.minecraft.inventory;

import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.BlockItem;

public class Inventory {
    public ItemStack[] inventory;

    public Inventory() {
        inventory = new ItemStack[514]; // this is 514 because it will encompass all items for all inventories, not just
        // the players.

    }

    public ItemStack[] getInventory() {
        return this.inventory;
    }

    public ItemStack getItemAtSlot(int slotIdx) {
        if (slotIdx > inventory.length || slotIdx < 0) {
            return null;
        }
        return inventory[slotIdx];
    }

    public void setItemSlot(int slotIdx, ItemStack stack) {
        if (slotIdx > inventory.length || slotIdx < 0) {
            return;
        }
        inventory[slotIdx] = stack;
    }

   /**
    * Adds a single item to inventory stacks, then empty slots.
    */
   public boolean addItemToInventory(Item item) {
  //  String desc = getItemDescription(item);
   // Logger.INFO("The item i am trying to add, %s, has a stack size of %s", desc, item.getMaxStackSize());
   // Logger.INFO("Attempting to add %s", desc);
    return addItemToInventory(new ItemStack(item));
}

/**
 * Adds an item stack, merging first and placing overflow into free slots.
 */
public boolean addItemToInventory(ItemStack item) {
    ItemStack remaining = item;

    for (int i = 0; i < 36; i++) {
        if (getItemAtSlot(i) != null && getItemAtSlot(i).item.equals(remaining.item)) {
            ItemStack overflow = getItemAtSlot(i).merge(remaining);
            if (overflow == null) {
                // Item was fully merged into existing stack
                //logInventoryAddition(getItemAtSlot(i));
                return true;
            }
            remaining = overflow;
        }
    }

    while (remaining != null && remaining.amount > 0) {
        boolean placed = false;
        for (int i = 0; i < 36; i++) {
            if (getItemAtSlot(i) == null) {
                int toPlace = Math.min(remaining.amount, remaining.item.getMaxStackSize());
                setItemSlot(i, new ItemStack(remaining.item, toPlace));
                remaining.amount -= toPlace;

                if (remaining.amount <= 0) {
                    //logInventoryAddition(new ItemStack(remaining.item, toPlace));
                    return true;
                }
                placed = true;
                break;
            }
        }

        if (!placed) {
            return false;
        }
    }

    return true;
}

/**
 * Logs a user-friendly summary for an added stack.
 */
private void logInventoryAddition(ItemStack stack) {
    String desc = getItemDescription(stack.item);
    Logger.INFO("%s x%d", desc, stack.amount);
}

private String getItemDescription(Item item) {
    if (item instanceof BlockItem) {
        return ((BlockItem) item).getFullDescriptionWithProperties();
    }
    return item.getDescriptionId();
}

    public boolean addItemStackToInventory(ItemStack items) {
        return false;
    }
}
