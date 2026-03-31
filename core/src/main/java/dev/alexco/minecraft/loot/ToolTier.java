package dev.alexco.minecraft.loot;

public enum ToolTier {
    NONE(0),
    WOOD(1),
    GOLD(1),
    STONE(2),
    IRON(3),
    DIAMOND(4),
    NETHERITE(5);

    private final int level;

    ToolTier(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Parses textual tier names from data files into tier enum values.
     */
    public static ToolTier fromString(String value) {
        if (value == null) {
            return NONE;
        }
        return switch (value.toLowerCase()) {
            case "wood", "wooden" -> WOOD;
            case "gold", "golden" -> GOLD;
            case "stone" -> STONE;
            case "iron" -> IRON;
            case "diamond" -> DIAMOND;
            case "netherite" -> NETHERITE;
            default -> NONE;
        };
    }
}
