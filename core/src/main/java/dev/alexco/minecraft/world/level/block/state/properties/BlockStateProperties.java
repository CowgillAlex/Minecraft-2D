package dev.alexco.minecraft.world.level.block.state.properties;

import dev.alexco.minecraft.util.Direction;

public class BlockStateProperties {
    public static final BooleanProperty TEST = BooleanProperty.create("test");
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);
    public static final BooleanProperty FALLING = BooleanProperty.create("falling");
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 8);
    public static final BooleanProperty PERSISTENT = BooleanProperty.create("persistent");
    public static final BooleanProperty SOURCE = BooleanProperty.create("source");
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final IntegerProperty MOISTURE = IntegerProperty.create("moisture", 0, 7);
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
}
