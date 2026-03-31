package dev.alexco.minecraft.loot;

import java.util.ArrayList;
import java.util.List;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.registry.ResourceLocation;

public class LootTable {
    private final ResourceLocation id;
    private final List<LootPool> pools;

    public LootTable(ResourceLocation id, List<LootPool> pools) {
        this.id = id;
        this.pools = pools;
    }

    public ResourceLocation getId() {
        return id;
    }

    /**
     * Runs all pools and combines their generated drops.
     */
    public List<ItemStack> generateDrops(LootContext context) {
        List<ItemStack> drops = new ArrayList<>();
        for (LootPool pool : pools) {
            drops.addAll(pool.generate(context));
        }
        return drops;
    }
}
