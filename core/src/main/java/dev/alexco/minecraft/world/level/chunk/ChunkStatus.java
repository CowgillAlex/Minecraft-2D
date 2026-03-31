package dev.alexco.minecraft.world.level.chunk;

public enum ChunkStatus {
    EMPTY, NOISE, SURFACE, CARVERS, STRUCTURES, FEATURES, LIGHT, FULL;

    /**
     * Returns the next generation status, or this status if final.
     */
    public ChunkStatus getNext() {
        ChunkStatus[] values = values();
        int nextIndex = this.ordinal() + 1;
        return nextIndex < values.length ? values[nextIndex] : this;
    }

    /**
     * Returns true if this status is at least as complete as another.
     */
    public boolean isAtLeast(ChunkStatus status) {
        return this.ordinal() >= status.ordinal();
    }

    /**
     * Returns neighbour dependency range required for this stage.
     */
    public int getRange() {
        switch (this) {
            case STRUCTURES: return 2;
            case FEATURES: return 1;
            case LIGHT: return 1;
            default: return 0;
        }
    }
}
