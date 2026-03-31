package dev.alexco.minecraft.loot;

import java.util.Random;

public class RandomChanceCondition implements LootCondition {
    private final float chance;
    private static final Random RANDOM = new Random();

    public RandomChanceCondition(float chance) {
        this.chance = Math.max(0f, Math.min(1f, chance));
    }

    /**
     * Passes when a random roll is below the configured chance.
     */
    @Override
    public boolean test(LootContext context) {
        return RANDOM.nextFloat() < chance;
    }
}
