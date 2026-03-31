package dev.alexco.minecraft.world.level.block;

import dev.alexco.minecraft.world.level.block.state.StateDefinition;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.block.state.properties.BooleanProperty;

public class OakLeafBlock extends Block{
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
   protected OakLeafBlock(Block.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any()).setValue(PERSISTENT, true));
    }
     @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PERSISTENT);
    }
}
