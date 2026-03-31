package dev.alexco.minecraft.loot;

public class ToolTierAtLeastCondition implements LootCondition {
    private final ToolType requiredToolType;
    private final ToolTier minimumTier;

    public ToolTierAtLeastCondition(ToolType requiredToolType, ToolTier minimumTier) {
        this.requiredToolType = requiredToolType;
        this.minimumTier = minimumTier;
    }

    /**
     * Verifies that the held tool matches the required type and minimum tier.
     */
    @Override
    public boolean test(LootContext context) {
        ToolInfo toolInfo = ToolInfo.fromItem(context.getToolItem());
        if (toolInfo.getType() != requiredToolType) {
            return false;
        }
        return toolInfo.getTier().getLevel() >= minimumTier.getLevel();
    }
}
