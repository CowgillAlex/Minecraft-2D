package dev.alexco.minecraft.world.level.block.entity;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.world.entity.ItemEntity;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.registry.ResourceLocation;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;

public class BarrelBlockEntity extends BlockEntity {
    public static final String TYPE_ID = "minecraft:barrel";

    // Inventory size: 3 rows × 9 columns = 27 slots
    public static final int INVENTORY_SIZE = 27;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];

    public BarrelBlockEntity(int x, int y) {
        super(x, y);
    }

    @Override
    public void tick() {
        // only store no tick
    }

    @Override
    public CompoundTag saveToNBT(CompoundTag tag) {
        tag.putString("type", TYPE_ID);
        tag.putInt("x", x);
        tag.putInt("y", y);

        // Save inventory
        ListTag<CompoundTag> inventoryTag = new ListTag<>(CompoundTag.class);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventory[i] != null) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("slot", i);
                itemTag.putString("item", Registry.ITEM.getKey(inventory[i].item).toString());
                itemTag.putInt("count", inventory[i].amount);
                if (inventory[i].damage > 0) {
                    itemTag.putInt("damage", inventory[i].damage);
                }
                inventoryTag.add(itemTag);
            }
        }
        tag.put("inventory", inventoryTag);

        return tag;
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        ListTag<CompoundTag> inventoryTag = tag.getListTag("inventory").asCompoundTagList();
        for (CompoundTag itemTag : inventoryTag) {
            int slot = itemTag.getInt("slot");
            String itemId = itemTag.getString("item");
            int count = itemTag.getInt("count");
            int damage = itemTag.containsKey("damage") ? itemTag.getInt("damage") : 0;

            Item item = Registry.ITEM.get(new ResourceLocation(itemId));
            if (item != null && slot >= 0 && slot < INVENTORY_SIZE) {
                inventory[slot] = new ItemStack(item, count, damage);
            }
        }
    }

    @Override
    public void onBlockBroken() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack stack = inventory[i];
            if (stack != null && stack.amount > 0) {
                for (int j = 0; j < stack.amount; j++) {
                    Minecraft.getInstance().getWorld().entities.add(
                            new ItemEntity(stack.item, x + 0.5D, y));
                }
            }
        }
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }


    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= INVENTORY_SIZE)
            return null;
        return inventory[slot];
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INVENTORY_SIZE)
            return;
        inventory[slot] = stack;
    }


    public boolean canPlaceItemInSlot(int slot, ItemStack stack) {
        return slot >= 0 && slot < INVENTORY_SIZE;
    }

    /**
     * Returns true if the item was successfully inserted or stacked
     */
    public boolean insertItem(int slot, ItemStack stack) {
        if (stack == null || stack.amount <= 0)
            return false;
        if (!canPlaceItemInSlot(slot, stack))
            return false;

        ItemStack existing = inventory[slot];

        if (existing == null) {
            inventory[slot] = new ItemStack(stack.item, stack.amount, stack.damage);
            return true;
        } else if (existing.item.equals(stack.item)) {
            int maxStack = stack.item.getMaxStackSize();
            int space = maxStack - existing.amount;

            if (space > 0) {
                int toAdd = Math.min(stack.amount, space);
                existing.amount += toAdd;
                return true;
            }
        }

        return false;
    }

    /**
     * Removes up to amount items from a slot and returns them.
     */

    public ItemStack extractItem(int slot, int amount) {
        if (amount <= 0)
            return null;

        ItemStack existing = inventory[slot];
        if (existing == null || existing.amount <= 0)
            return null;

        int toExtract = Math.min(amount, existing.amount);
        ItemStack extracted = new ItemStack(existing.item, toExtract, existing.damage);

        existing.amount -= toExtract;
        if (existing.amount <= 0) {
            inventory[slot] = null;
        }

        return extracted;
    }
}
