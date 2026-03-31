package dev.alexco.minecraft.gui;

public enum ScreenState {
    TITLE,
    NONE,
    INVENTORY,
    CRAFTING_TABLE,
    FURNACE,
    BARREL,
    WORLD_SELECTION,
    WORLD_CREATION,
    MORE_WORLD_OPTIONS,
    WORLD_LOADING,
    PAUSE,
    SAVING_WORLD;

    /**
     * Returns true for in-game container screens that should block normal controls.
     */
    public boolean isScreenOpen() {
        return this != NONE
            && this != TITLE
            && this != WORLD_SELECTION
            && this != WORLD_CREATION
            && this != MORE_WORLD_OPTIONS
            && this != PAUSE
            && this != SAVING_WORLD;
    }

    public boolean isTitleScreen() {
        return this == TITLE;
    }

    public boolean isInventoryOpen() {
        return this == INVENTORY;
    }

    public boolean isCraftingTableOpen() {
        return this == CRAFTING_TABLE;
    }

    public boolean isFurnaceOpen() {
        return this == FURNACE;
    }
    public boolean isBarrelOpen() {
        return this == BARREL;
    }

    /**
     * Returns true for non-world menu screens (title/world selection/creation/loading).
     */
    public boolean isMenuScreen() {
        return this == TITLE || this == WORLD_SELECTION || this == WORLD_CREATION || this == MORE_WORLD_OPTIONS || this == WORLD_LOADING;
    }

    public boolean isPauseScreen() {
        return this == PAUSE;
    }

    public boolean isSavingWorld() {
        return this == SAVING_WORLD;
    }
}
