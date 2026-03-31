package dev.alexco.minecraft.loot;

public class AlwaysTrueCondition implements LootCondition {
    @Override
    public boolean test(LootContext context) {
        return true;
    }
}
