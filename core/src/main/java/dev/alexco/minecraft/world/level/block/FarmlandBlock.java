package dev.alexco.minecraft.world.level.block;

import dev.alexco.minecraft.world.level.block.state.StateDefinition.Builder;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.block.state.properties.IntegerProperty;

public class FarmlandBlock  extends Block{
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    protected FarmlandBlock(Block.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 0));
    }
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

}
