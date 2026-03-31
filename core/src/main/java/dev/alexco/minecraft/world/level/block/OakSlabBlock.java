package dev.alexco.minecraft.world.level.block;

import dev.alexco.minecraft.phys.AABB;
import dev.alexco.minecraft.world.level.block.state.StateDefinition;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.block.state.properties.BooleanProperty;

public class OakSlabBlock extends Block {
    public static final BooleanProperty TOP = BlockStateProperties.TOP;

    public OakSlabBlock(Block.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any()).setValue(TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP);
    }

    @Override
    public void setCollisionShape(BlockState blockState, int x, int y, AABB aabb) {
        boolean top = blockState.getValue(TOP);
        if (top) {
            aabb.set(x, y + 0.5, x + 1.0, y + 1.0);
        } else {
            aabb.set(x, y, x + 1.0, y + 0.5);
        }
    }
}
