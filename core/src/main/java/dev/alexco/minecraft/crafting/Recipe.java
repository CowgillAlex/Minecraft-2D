package dev.alexco.minecraft.crafting;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.level.item.Item;

public abstract class Recipe {
    protected final ItemStack result;
    protected final String category;
    protected final String group;

    public Recipe(ItemStack result, String category, String group) {
        this.result = result;
        this.category = category;
        this.group = group;
    }

    public Recipe(ItemStack result, String category) {
        this(result, category, "");
    }

    public abstract boolean matches(CraftingInput input);

    public abstract boolean canCraft(CraftingInput input);

    public ItemStack getResult() {
        return result;
    }

    public String getCategory() {
        return category;
    }

    public String getGroup() {
        return group;
    }

    public int getResultCount() {
        return result.amount;
    }

    public Item getResultItem() {
        return result.item;
    }
}
