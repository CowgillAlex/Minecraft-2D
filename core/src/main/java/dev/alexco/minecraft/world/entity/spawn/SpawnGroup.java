package dev.alexco.minecraft.world.entity.spawn;

import dev.alexco.minecraft.world.entity.Mob;
import dev.alexco.registry.ResourceLocation;

public class SpawnGroup {
    @FunctionalInterface
    public interface MobFactory {
        Mob create(double x, double y);
    }

    private final ResourceLocation id;
    private final int minCount;
    private final int maxCount;
    private final int weight;
    private final SpawnCondition condition;
    private final MobFactory factory;

    /**
     * Creates a spawn group definition and normalizes count/weight bounds.
     */
    public SpawnGroup(ResourceLocation id, int minCount, int maxCount, int weight, SpawnCondition condition, MobFactory factory) {
        this.id = id;
        this.minCount = Math.max(1, minCount);
        this.maxCount = Math.max(this.minCount, maxCount);
        this.weight = Math.max(1, weight);
        this.condition = condition;
        this.factory = factory;
    }

    public ResourceLocation getId() {
        return id;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getWeight() {
        return weight;
    }

    public SpawnCondition getCondition() {
        return condition;
    }

    /**
     * Instantiates a mob at the requested world coordinates.
     */
    public Mob create(double x, double y) {
        return factory.create(x, y);
    }
}
