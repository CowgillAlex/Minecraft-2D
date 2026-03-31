package dev.alexco.minecraft.loot;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.level.item.Item;

public class LootEntry {
    private final Item item;
    private final int count;
    private final int weight;

    public LootEntry(Item item, int count) {
        this(item, count, 1);
    }

    public LootEntry(Item item, int count, int weight) {
        this.item = item;
        this.count = Math.max(1, count);
        this.weight = Math.max(1, weight);
    }

    /**
     * Creates a concrete item stack from this loot entry definition.
     */
    public ItemStack createStack() {
        return new ItemStack(item, count);
    }

    public int getWeight() {
        return weight;
    }
}
