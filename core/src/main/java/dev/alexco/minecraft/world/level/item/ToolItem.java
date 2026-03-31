package dev.alexco.minecraft.world.level.item;

import dev.alexco.minecraft.loot.ToolTier;
import dev.alexco.minecraft.loot.ToolType;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.world.level.block.Block;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ToolItem extends Item {
    public static class AttributeValue {
        private final String displayName;
        private float base;
        private float offset;

        public AttributeValue(String displayName, float base, float offset) {
            this.displayName = displayName;
            this.base = base;
            this.offset = offset;
        }

        public String getDisplayName() {
            return displayName;
        }

        public float getBase() {
            return base;
        }

        public float getOffset() {
            return offset;
        }

        public float getTotal() {
            return base + offset;
        }

        public void setBase(float base) {
            this.base = base;
        }

        public void setOffset(float offset) {
            this.offset = offset;
        }
    }

    public static final String ATTRIBUTE_ATTACK_DAMAGE = "attack_damage";
    public static final String ATTRIBUTE_MINING_SPEED = "mining_speed";

    private final ToolType toolType;
    private final ToolTier toolTier;
    private final Map<String, AttributeValue> attributes = new LinkedHashMap<>();

    public ToolItem(ToolType toolType, ToolTier toolTier, Properties properties) {
        super(properties);
        this.toolType = toolType;
        this.toolTier = toolTier;
        addAttribute(ATTRIBUTE_MINING_SPEED, "Mining Speed", speedForTier(toolTier), 0.0f);
        addAttribute(ATTRIBUTE_ATTACK_DAMAGE, "Attack Damage", attackDamageFor(toolType, toolTier), 0.0f);
    }

    public ToolType getToolType() {
        return toolType;
    }

    public ToolTier getToolTier() {
        return toolTier;
    }

    public float getMiningSpeed() {
        return getAttributeTotal(ATTRIBUTE_MINING_SPEED);
    }

    public float getAttackDamage() {
        return getAttributeTotal(ATTRIBUTE_ATTACK_DAMAGE);
    }

    public Map<String, AttributeValue> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public ToolItem addAttribute(String id, String displayName, float base, float offset) {
        attributes.put(id, new AttributeValue(displayName, base, offset));
        return this;
    }

    public ToolItem setAttributeBase(String id, float base) {
        AttributeValue value = attributes.get(id);
        if (value != null) {
            value.setBase(base);
        }
        return this;
    }

    public ToolItem setAttributeOffset(String id, float offset) {
        AttributeValue value = attributes.get(id);
        if (value != null) {
            value.setOffset(offset);
        }
        return this;
    }

    public float getAttributeTotal(String id) {
        AttributeValue value = attributes.get(id);
        return value == null ? 0.0f : value.getTotal();
    }

    /**
     * Returns true when this tool type can mine the given block category.
     */
    public boolean isCorrectToolForDrops(Block block) {
        return switch (toolType) {
            case PICKAXE -> BlockTags.MINEABLE_PICKAXE.contains(block);
            case AXE -> BlockTags.MINEABLE_AXE.contains(block);
            case HOE -> BlockTags.MINEABLE_HOE.contains(block);
            case SHOVEL -> BlockTags.MINEABLE_SHOVEL.contains(block);
            case SWORD -> BlockTags.MINEABLE_SWORD.contains(block);
            case NONE -> false;
        };
    }

    /**
     * Returns effective mining speed for held item against a block.
     */
    public static float getMiningSpeed(Item heldItem, Block block) {
        if (heldItem instanceof ToolItem toolItem && toolItem.isCorrectToolForDrops(block)) {
            return toolItem.getMiningSpeed();
        }
        return 1.0f;
    }

    /**
     * Checks if the held item can harvest drops from a block.
     */
    public static boolean canHarvest(Item heldItem, Block block) {
        ToolTier requiredTier = getRequiredTier(block);
        boolean mineableByFist = BlockTags.MINEABLE_FIST.contains(block);
        boolean hasToolRequirement = BlockTags.MINEABLE_PICKAXE.contains(block)
            || BlockTags.MINEABLE_AXE.contains(block)
            || BlockTags.MINEABLE_HOE.contains(block)
            || BlockTags.MINEABLE_SWORD.contains(block);

        if (heldItem instanceof ToolItem toolItem && toolItem.isCorrectToolForDrops(block)) {
            return toolItem.getToolTier().getLevel() >= requiredTier.getLevel();
        }

        if (mineableByFist) {
            return requiredTier == ToolTier.NONE;
        }

        return !hasToolRequirement;
    }

    /**
     * Resolves required tool tier from block tags.
     */
    private static ToolTier getRequiredTier(Block block) {
        if (BlockTags.NEEDS_DIAMOND_TOOL.contains(block)) {
            return ToolTier.DIAMOND;
        }
        if (BlockTags.NEEDS_IRON_TOOL.contains(block)) {
            return ToolTier.IRON;
        }
        if (BlockTags.NEEDS_WOOD_TOOL.contains(block)) {
            return ToolTier.WOOD;
        }
        if (BlockTags.NEEDS_STONE_TOOL.contains(block)) {
            return ToolTier.STONE;
        }
        if (BlockTags.NEEDS_FIST_TOOL.contains(block)) {
            return ToolTier.NONE;
        }
        if (BlockTags.MINEABLE_PICKAXE.contains(block)
            || BlockTags.MINEABLE_AXE.contains(block)
            || BlockTags.MINEABLE_HOE.contains(block)
            || BlockTags.MINEABLE_SWORD.contains(block)) {
            return ToolTier.WOOD;
        }
        return ToolTier.NONE;
    }

    /**
     * Returns base mining speed for a tool tier.
     */
    private static float speedForTier(ToolTier toolTier) {
        return switch (toolTier) {
            case WOOD -> 2.0f;
            case STONE -> 4.0f;
            case IRON -> 6.0f;
            case DIAMOND -> 8.0f;
            case NETHERITE -> 9.0f;
            case GOLD -> 12.0f;
            case NONE -> 1.0f;
        };
    }

    /**
     * Returns base attack damage for a tool type and tier.
     */
    private static float attackDamageFor(ToolType toolType, ToolTier toolTier) {
        float typeBase = switch (toolType) {
            case SWORD -> 3.0f;
            case AXE -> 4.5f;
            case PICKAXE -> 2.5f;
            case SHOVEL -> 1.5f;
            case HOE -> 1.0f;
            case NONE -> 1.0f;
        };
        float tierBonus = switch (toolTier) {
            case WOOD -> 0.5f;
            case STONE -> 1.0f;
            case IRON -> 1.5f;
            case DIAMOND -> 2.0f;
            case NETHERITE -> 2.5f;
            case GOLD -> 0.75f;
            case NONE -> 0.0f;
        };
        return typeBase + tierBonus;
    }
}
