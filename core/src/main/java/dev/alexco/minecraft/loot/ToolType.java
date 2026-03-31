package dev.alexco.minecraft.loot;

public enum ToolType {
    PICKAXE,
    AXE,
    SHOVEL,
    HOE,
    SWORD,
    NONE;

    /**
     * Parses a tool type token from loot json into a ToolType enum value.
     */
    public static ToolType fromString(String value) {
        if (value == null) {
            return NONE;
        }
        return switch (value.toLowerCase()) {
            case "pickaxe" -> PICKAXE;
            case "axe" -> AXE;
            case "shovel" -> SHOVEL;
            case "hoe" -> HOE;
            case "sword" -> SWORD;
            default -> NONE;
        };
    }
}
