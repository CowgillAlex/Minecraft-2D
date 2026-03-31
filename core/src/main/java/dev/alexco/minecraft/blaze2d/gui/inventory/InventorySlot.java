package dev.alexco.minecraft.blaze2d.gui.inventory;

import com.badlogic.gdx.Gdx;

public class InventorySlot {
    public final int slotIndex;
    public final int x, y, width, height;
    public final SlotType type;
    private String emptyTextureName;

    public enum SlotType {
        REGULAR, // normal slot any item
        CRAFTING_INPUT, // crafting input, any item
        CRAFTING_OUTPUT, // crafting output, any craftable item, output only
        ARMOUR, // only armour items
        OFFHAND // any item
    }

    public InventorySlot(int slotIndex, int x, int y, int width, int height, SlotType type) {
        this.slotIndex = slotIndex;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.emptyTextureName = null;
    }

    /**
     * Set the texture name to display when this slot is empty
     * (e.g., "helmet", "chestplate", "boots", "leggings")
     */
    public void setEmptyTextureName(String textureName) {
        this.emptyTextureName = textureName;
    }

    /**
     * Get the empty texture name for this slot
     */
    public String getEmptyTextureName() {
        return emptyTextureName;
    }

    /**
     * Check if this slot has an empty texture defined
     */
    public boolean hasEmptyTexture() {
        return emptyTextureName != null;
    }

    public boolean isMouseOver() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        return isMouseOver(mouseX, mouseY);
    }
    
    public boolean isMouseOver(int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height);
    }
}
