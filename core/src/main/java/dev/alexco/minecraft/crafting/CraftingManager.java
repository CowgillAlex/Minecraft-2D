package dev.alexco.minecraft.crafting;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.level.item.Item;

public class CraftingManager {

    /**
     * Finds a matching recipe for the given crafting input
     * @param input The crafting grid input
     * @return The matching recipe, or null if no match
     */
    public static Recipe findRecipe(CraftingInput input) {
        return RecipeLoader.findMatchingRecipe(input);
    }

    /**
     * Crafts an item using the given input
     * @param input The crafting grid input
     * @return The resulting ItemStack, or null if no recipe matches
     */
    public static ItemStack craft(CraftingInput input) {
        Recipe recipe = findRecipe(input);
        if (recipe != null) {
            // Return a new ItemStack with the result item and correct amount
            // The amount is limited by the item's max stack size
            Item resultItem = recipe.getResultItem();
            int desiredAmount = recipe.getResultCount();
            int maxStack = resultItem.getMaxStackSize();
            int actualAmount = Math.min(desiredAmount, maxStack);

            return new ItemStack(resultItem, actualAmount);
        }
        return null;
    }

    /**
     * Checks if the input matches any recipe
     * @param input The crafting grid input
     * @return true if a matching recipe exists
     */
    public static boolean canCraft(CraftingInput input) {
        return findRecipe(input) != null;
    }

    /**
     * Consumes the ingredients from the input based on a matched recipe.
     * Note: This doesn't check if the recipe actually matches - call canCraft() first!
     *
     * @param input The crafting grid input to consume from
     * @return true if consumption was successful
     */
    public static boolean consumeIngredients(CraftingInput input) {
        Recipe recipe = findRecipe(input);
        if (recipe == null) {
            return false;
        }

        int size = input.getSize();

        // For shapeless recipes, consume one of each ingredient type
        if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
            boolean[][] consumed = new boolean[size][size];

            for (ShapedRecipe.Ingredient ingredient : shapeless.getIngredients()) {
                boolean found = false;
                for (int y = 0; y < size && !found; y++) {
                    for (int x = 0; x < size && !found; x++) {
                        if (!consumed[y][x] && input.getItem(x, y) != null) {
                            if (ingredient.matches(input.getItem(x, y).item)) {
                                consumed[y][x] = true;
                                found = true;
                            }
                        }
                    }
                }
            }

            // Now actually consume the items (decrement by 1)
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (consumed[y][x]) {
                        ItemStack stack = input.getItem(x, y);
                        if (stack != null) {
                            stack.amount--;
                            if (stack.amount <= 0) {
                                // Item is depleted, set slot to null
                                // Note: We can't actually set to null here without access to the inventory
                                // The caller should check for depleted stacks
                            }
                        }
                    }
                }
            }
            return true;
        }

        // For shaped recipes, consume based on pattern
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shaped = (ShapedRecipe) recipe;
            String[] pattern = shaped.getPattern();

            // Find where the pattern matches
            for (int startY = 0; startY <= size - shaped.getHeight(); startY++) {
                for (int startX = 0; startX <= size - shaped.getWidth(); startX++) {
                    if (matchesAt(shaped, input, startX, startY)) {
                        // Consume ingredients at this position
                        for (int y = 0; y < shaped.getHeight(); y++) {
                            for (int x = 0; x < shaped.getWidth(); x++) {
                                char patternChar = pattern[y].charAt(x);
                                if (patternChar != ' ') {
                                    ItemStack stack = input.getItem(startX + x, startY + y);
                                    if (stack != null) {
                                        stack.amount--;
                                    }
                                }
                            }
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks whether a shaped recipe matches at a specific grid offset.
     */
    private static boolean matchesAt(ShapedRecipe recipe, CraftingInput input, int startX, int startY) {
        String[] pattern = recipe.getPattern();
        for (int y = 0; y < recipe.getHeight(); y++) {
            for (int x = 0; x < recipe.getWidth(); x++) {
                char patternChar = pattern[y].charAt(x);
                Item inputItem = input.getItemType(startX + x, startY + y);

                if (patternChar == ' ') {
                    if (inputItem != null) {
                        return false;
                    }
                } else {
                    ShapedRecipe.Ingredient ingredient = recipe.getKey().get(patternChar);
                    if (ingredient == null || !ingredient.matches(inputItem)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Gets the remaining items after crafting (for recipes with container items)
     * @param input The crafting input
     * @return Array of remaining items (same size as input), may contain nulls
     */
    public static ItemStack[][] getRemainingItems(CraftingInput input) {
        int size = input.getSize();
        ItemStack[][] remaining = new ItemStack[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                ItemStack stack = input.getItem(x, y);
                if (stack != null && stack.item != null) {
                    // Check if item has a crafting remainder (like buckets)
                    // For now, just return depleted stacks
                    if (stack.amount > 1) {
                        remaining[y][x] = new ItemStack(stack.item, stack.amount - 1);
                    }
                    // TODO: Check for craftingRemainder item in Item.Properties
                }
            }
        }

        return remaining;
    }
}
