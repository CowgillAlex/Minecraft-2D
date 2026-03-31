package dev.alexco.minecraft.loot;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.ToolItem;
import dev.alexco.registry.ResourceLocation;

public class ToolInfo {
    private final ToolType type;
    private final ToolTier tier;

    public ToolInfo(ToolType type, ToolTier tier) {
        this.type = type;
        this.tier = tier;
    }

    public ToolType getType() {
        return type;
    }

    public ToolTier getTier() {
        return tier;
    }

    /**
     * Infers tool category and tier from runtime item metadata and registry id.
     */
    public static ToolInfo fromItem(Item item) {
        if (item == null) {
            return new ToolInfo(ToolType.NONE, ToolTier.NONE);
        }
        if (item instanceof ToolItem toolItem) {
            return new ToolInfo(toolItem.getToolType(), toolItem.getToolTier());
        }
        ResourceLocation key = Registry.ITEM.getKey(item);
        if (key == null) {
            return new ToolInfo(ToolType.NONE, ToolTier.NONE);
        }

        String path = key.getPath();
        ToolType toolType = ToolType.NONE;

        if (path.endsWith("_pickaxe")) {
            toolType = ToolType.PICKAXE;
        } else if (path.endsWith("_axe")) {
            toolType = ToolType.AXE;
        } else if (path.endsWith("_shovel")) {
            toolType = ToolType.SHOVEL;
        } else if (path.endsWith("_hoe")) {
            toolType = ToolType.HOE;
        } else if (path.endsWith("_sword")) {
            toolType = ToolType.SWORD;
        }

        if (toolType == ToolType.NONE) {
            return new ToolInfo(ToolType.NONE, ToolTier.NONE);
        }

        ToolTier tier = ToolTier.NONE;
        if (path.startsWith("wood_") || path.startsWith("wooden_")) {
            tier = ToolTier.WOOD;
        } else if (path.startsWith("stone_")) {
            tier = ToolTier.STONE;
        } else if (path.startsWith("iron_")) {
            tier = ToolTier.IRON;
        } else if (path.startsWith("diamond_")) {
            tier = ToolTier.DIAMOND;
        } else if (path.startsWith("gold_") || path.startsWith("golden_")) {
            tier = ToolTier.GOLD;
        } else if (path.startsWith("netherite_")) {
            tier = ToolTier.NETHERITE;
        }

        return new ToolInfo(toolType, tier);
    }
}
