package dev.alexco.minecraft.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import dev.alexco.minecraft.inventory.ItemStack;

public class LootPool {
    private final List<LootCondition> conditions;
    private final List<LootEntry> entries;
    private final boolean selectOne;
    private final Random random = new Random();

    public LootPool(List<LootCondition> conditions, List<LootEntry> entries) {
        this(conditions, entries, false);
    }

    public LootPool(List<LootCondition> conditions, List<LootEntry> entries, boolean selectOne) {
        this.conditions = conditions;
        this.entries = entries;
        this.selectOne = selectOne;
    }

    /**
     * Evaluates pool conditions and returns either all entries or one weighted entry.
     */
    public List<ItemStack> generate(LootContext context) {
        for (LootCondition condition : conditions) {
            if (!condition.test(context)) {
                return Collections.emptyList();
            }
        }

        List<ItemStack> drops = new ArrayList<>();
        if (entries.isEmpty()) {
            return drops;
        }

        if (selectOne) {
            LootEntry selected = pickWeightedEntry();
            if (selected != null) {
                drops.add(selected.createStack());
            }
            return drops;
        }

        for (LootEntry entry : entries) {
            drops.add(entry.createStack());
        }
        return drops;
    }

    /**
     * Picks a single entry using each entry's weight as its selection chance.
     */
    private LootEntry pickWeightedEntry() {
        int totalWeight = 0;
        for (LootEntry entry : entries) {
            totalWeight += entry.getWeight();
        }
        if (totalWeight <= 0) {
            return null;
        }

        int target = random.nextInt(totalWeight);
        int running = 0;
        for (LootEntry entry : entries) {
            running += entry.getWeight();
            if (target < running) {
                return entry;
            }
        }
        return entries.get(entries.size() - 1);
    }
}
