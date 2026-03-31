package dev.alexco.minecraft.blaze2d.gui.inventory;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.level.item.Item;

public class DragDropHandler {
    public boolean holdingItem = false;
    private ItemStack heldItem = null;
    private float heldX, heldY = 0f;
    private int sourceSlotIndex = -1;

    /**
     * Starts a drag operation using the provided stack and source slot index.
     */
    public void pickupItem(ItemStack item, int slotIndex) {
        this.heldItem = item;
        this.holdingItem = true;
        this.sourceSlotIndex = slotIndex;
    }

    /**
     * Ends the current drag operation and returns the held stack.
     */
    public ItemStack dropItem() {
        ItemStack item = this.heldItem;
        this.heldItem = null;
        this.holdingItem = false;
        this.sourceSlotIndex = -1;
        return item;
    }

    /**
     * Updates screen-space drag coordinates for rendering the held stack.
     */
    public void updatePosition(float x, float y) {
        this.heldX = x;
        this.heldY = y;
    }

    public int getSourceSlotIndex() {
        return sourceSlotIndex;
    }

    public boolean isHoldingItem() {
        return holdingItem;
    }

    public boolean isDragging() {
        return holdingItem;
    }

    public ItemStack getDraggedItem() {
        return heldItem;
    }

    public float getDragX() {
        return heldX;
    }

    public float getDragY() {
        return heldY;
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public float getHeldX() {
        return heldX;
    }

    public float getHeldY() {
        return heldY;
    }
}
