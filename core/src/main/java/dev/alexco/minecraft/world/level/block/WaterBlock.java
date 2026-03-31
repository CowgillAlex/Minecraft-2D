package dev.alexco.minecraft.world.level.block;

import dev.alexco.minecraft.world.level.block.state.StateDefinition;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.block.state.properties.BooleanProperty;
import dev.alexco.minecraft.world.level.block.state.properties.IntegerProperty;

public class WaterBlock extends Block {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final BooleanProperty SOURCE = BlockStateProperties.SOURCE;
    public WaterBlock(Properties properties) {
        super(properties);
         this.registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any()).setValue(LEVEL, 1).setValue(FALLING, false).setValue(SOURCE, true));
    }
     @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FALLING).add(LEVEL).add(SOURCE);
    }

}
