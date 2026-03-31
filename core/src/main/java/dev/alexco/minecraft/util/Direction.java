package dev.alexco.minecraft.util;

public enum Direction implements StringRepresentable {
    UP("up"),
    RIGHT("right"),
    DOWN("down"),
    LEFT("left");

    private final String serializedName;

    Direction(String serializedName) {
        this.serializedName = serializedName;

    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public Direction getOpposite() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            default:
                return this;
        }
    }

    public Direction rotateClockwise() {
        switch (this) {
            case UP:
                return RIGHT;
            case RIGHT:
                return DOWN;
            case DOWN:
                return LEFT;
            case LEFT:
                return UP;
            default:
                return this;
        }
    }

    public Direction rotateCounterClockwise() {
        switch (this) {
            case UP:
                return LEFT;
            case LEFT:
                return DOWN;
            case DOWN:
                return RIGHT;
            case RIGHT:
                return UP;
            default:
                return this;
        }
    }
}
