package dev.alexco.minecraft.world.level.block;

import dev.alexco.minecraft.util.Direction;
import dev.alexco.minecraft.world.level.block.state.StateDefinition;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.block.state.properties.EnumProperty;

public class DeepslateBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    protected DeepslateBlock(Block.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any()).setValue(FACING, Direction.UP));
    }
     @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
