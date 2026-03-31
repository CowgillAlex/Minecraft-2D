package dev.alexco.minecraft.blaze2d.gui.inventory;

import java.util.ArrayList;
import java.util.List;

//thanks old project :)
public class InventoryGrid {
     private final List<InventorySlot> slots;
        private final int gridX, gridY;
        public InventoryGrid(int baseX, int baseY) {
        this.slots = new ArrayList<>();
        this.gridX = baseX;
        this.gridY = baseY;
    }
    public void addSlot(InventorySlot slot) {
        slots.add(slot);
    }

    /**
     * Appends a regular rows x cols slot region with sequential slot indices.
     */
    public void addRegularGrid(int startSlot, int rows, int cols, int slotSize, int spacing) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int slotIndex = startSlot + (row * cols) + col;
                int x = gridX + (col * (slotSize + spacing));
                int y = gridY + (row * (slotSize + spacing));
                addSlot(new InventorySlot(slotIndex, x, y, slotSize, slotSize, InventorySlot.SlotType.REGULAR));
            }
        }
    }

    public List<InventorySlot> getSlots() {
        return slots;
    }

    /**
     * Returns the first slot containing the given mouse coordinates.
     */
    public InventorySlot getSlotAt(int x, int y) {
        for (InventorySlot slot : slots) {
            if (slot.isMouseOver(x, y)) {
                return slot;
            }
        }
        return null;
    }
}
