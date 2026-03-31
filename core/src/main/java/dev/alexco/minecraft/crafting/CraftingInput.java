package dev.alexco.minecraft.crafting;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.level.item.Item;

public class CraftingInput {
    public static final int SIZE_2X2 = 2;
    public static final int SIZE_3X3 = 3;

    private final ItemStack[][] grid;
    private final int size;

    public CraftingInput(int size) {
        if (size != SIZE_2X2 && size != SIZE_3X3) {
            throw new IllegalArgumentException("Crafting grid must be 2x2 or 3x3");
        }
        this.size = size;
        this.grid = new ItemStack[size][size];
    }

    public void setItem(int x, int y, ItemStack stack) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            grid[y][x] = stack;
        }
    }

    public ItemStack getItem(int x, int y) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            return grid[y][x];
        }
        return null;
    }

    public Item getItemType(int x, int y) {
        ItemStack stack = getItem(x, y);
        return stack != null ? stack.item : null;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (grid[y][x] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Creates a compact representation of the grid, removing empty rows/columns
     * This is useful for matching shaped recipes that don't use the full grid
     */
    public CraftingInput getCompactInput() {
        if (isEmpty()) return this;

        int minX = size, minY = size, maxX = -1, maxY = -1;

        // Find bounds of non-empty cells
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (grid[y][x] != null) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX) return this; // All empty

        int compactSize = Math.max(maxX - minX + 1, maxY - minY + 1);
        CraftingInput compact = new CraftingInput(compactSize <= SIZE_2X2 ? SIZE_2X2 : SIZE_3X3);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                compact.setItem(x - minX, y - minY, grid[y][x]);
            }
        }

        return compact;
    }

            /**
             * Creates a deep copy of the crafting grid contents.
             */
    public CraftingInput copy() {
        CraftingInput copy = new CraftingInput(size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (grid[y][x] != null) {
                    copy.setItem(x, y, new ItemStack(grid[y][x].item, grid[y][x].amount));
                }
            }
        }
        return copy;
    }
}
