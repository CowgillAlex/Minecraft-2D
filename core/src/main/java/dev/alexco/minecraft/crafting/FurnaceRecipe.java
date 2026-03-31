package dev.alexco.minecraft.crafting;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.level.item.Item;

public class FurnaceRecipe {
    private final Item ingredient;
    private final int cookingTime;
    private final float experience;
    private final String category;
    private final String group;
    private final ItemStack result;

    public FurnaceRecipe(Item ingredient, ItemStack result, int cookingTime, float experience, String category, String group) {
        this.ingredient = ingredient;
        this.result = result;
        this.cookingTime = cookingTime;
        this.experience = experience;
        this.category = category;
        this.group = group;
    }


    public boolean matches(Item item) {
        return this.ingredient.equals(item);
    }


    public boolean matches(ItemStack stack) {
        return stack != null && matches(stack.item);
    }

    public Item getIngredient() {
        return ingredient;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public float getExperience() {
        return experience;
    }

    public String getCategory() {
        return category;
    }

    public String getGroup() {
        return group;
    }

    public ItemStack getResult() {
        return result;
    }

    public Item getResultItem() {
        return result.item;
    }

    public int getResultCount() {
        return result.amount;
    }
}
