package dev.alexco.minecraft.crafting;

import java.util.HashMap;
import java.util.Map;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.tag.ItemTags;
import dev.alexco.minecraft.tag.Tag;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.registry.ResourceLocation;

public class ShapedRecipe extends Recipe {
    private final String[] pattern;
    private final Map<Character, Ingredient> key;
    private final int width;
    private final int height;

    public ShapedRecipe(String category, String group, String[] pattern, Map<Character, Ingredient> key, ItemStack result) {
        super(result, category, group);
        this.pattern = pattern;
        this.key = key;
        this.height = pattern.length;
        this.width = pattern.length > 0 ? pattern[0].length() : 0;
    }

    /**
     * Checks if this shaped recipe matches anywhere in the input grid.
     */
    @Override
    public boolean matches(CraftingInput input) {
        int inputSize = input.getSize();
        if (inputSize < width || inputSize < height) {
            return false;
        }

        // Try all positions
        for (int startY = 0; startY <= inputSize - height; startY++) {
            for (int startX = 0; startX <= inputSize - width; startX++) {
                if (matchesAt(input, startX, startY)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the shaped pattern matches at a specific top-left offset.
     */
    private boolean matchesAt(CraftingInput input, int startX, int startY) {
        int inputSize = input.getSize();

        // First, check that the pattern matches at this position
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char patternChar = pattern[y].charAt(x);
                Item inputItem = input.getItemType(startX + x, startY + y);

                if (patternChar == ' ') {
                    // Empty space in pattern - input must be empty
                    if (inputItem != null) {
                        return false;
                    }
                } else {
                    Ingredient ingredient = key.get(patternChar);
                    if (ingredient == null) {
                        return false;
                    }
                    if (!ingredient.matches(inputItem)) {
                        return false;
                    }
                }
            }
        }

        // Then, check that everything OUTSIDE the pattern is empty
        for (int y = 0; y < inputSize; y++) {
            for (int x = 0; x < inputSize; x++) {
                // Skip cells that are part of the pattern
                if (y >= startY && y < startY + height && x >= startX && x < startX + width) {
                    continue;
                }

                // Check if cell outside pattern is empty
                if (input.getItemType(x, y) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean canCraft(CraftingInput input) {
        return matches(input);
    }

    public String[] getPattern() {
        return pattern;
    }

    public Map<Character, Ingredient> getKey() {
        return key;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static class Ingredient {
        private final Item item;
        private final Tag<Item> tag;
        private final boolean isTag;

        private Ingredient(Item item, Tag<Item> tag, boolean isTag) {
            this.item = item;
            this.tag = tag;
            this.isTag = isTag;
        }

        public static Ingredient ofItem(Item item) {
            return new Ingredient(item, null, false);
        }

        public static Ingredient ofTag(Tag<Item> tag) {
            return new Ingredient(null, tag, true);
        }

        public boolean matches(Item inputItem) {
            if (inputItem == null) return false;
            if (isTag) {
                return tag != null && tag.contains(inputItem);
            } else {
                return item != null && item.equals(inputItem);
            }
        }

        public boolean isTag() {
            return isTag;
        }

        public Item getItem() {
            return item;
        }

        public Tag<Item> getTag() {
            return tag;
        }
    }
}
