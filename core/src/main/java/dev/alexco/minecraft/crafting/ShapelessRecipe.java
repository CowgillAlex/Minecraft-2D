package dev.alexco.minecraft.crafting;

import java.util.ArrayList;
import java.util.List;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.tag.ItemTags;
import dev.alexco.minecraft.tag.Tag;
import dev.alexco.minecraft.world.level.item.Item;

public class ShapelessRecipe extends Recipe {
    private final List<ShapedRecipe.Ingredient> ingredients;

    public ShapelessRecipe(String category, String group, List<ShapedRecipe.Ingredient> ingredients, ItemStack result) {
        super(result, category, group);
        this.ingredients = ingredients;
    }

    /**
     * Checks if input items can satisfy this recipe in any order.
     */
    @Override
    public boolean matches(CraftingInput input) {
        List<ShapedRecipe.Ingredient> remainingIngredients = new ArrayList<>(ingredients);
        int size = input.getSize();
        boolean[][] used = new boolean[size][size];

        // Count non-null items in input
        int inputItemCount = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (input.getItem(x, y) != null) {
                    inputItemCount++;
                }
            }
        }

        // Must have exactly the right number of items
        if (inputItemCount != ingredients.size()) {
            return false;
        }

        // Try to match each ingredient to an input item
        for (ShapedRecipe.Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int y = 0; y < size && !found; y++) {
                for (int x = 0; x < size && !found; x++) {
                    if (!used[y][x] && input.getItem(x, y) != null) {
                        if (ingredient.matches(input.getItem(x, y).item)) {
                            used[y][x] = true;
                            found = true;
                        }
                    }
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canCraft(CraftingInput input) {
        return matches(input);
    }

    public List<ShapedRecipe.Ingredient> getIngredients() {
        return ingredients;
    }

    public int getIngredientCount() {
        return ingredients.size();
    }
}
