package dev.alexco.minecraft.loot;

public interface LootCondition {
    boolean test(LootContext context);
}
