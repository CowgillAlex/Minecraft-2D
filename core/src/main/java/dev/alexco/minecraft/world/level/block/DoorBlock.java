package dev.alexco.minecraft.world.level.block;

import java.util.List;

import dev.alexco.minecraft.blaze2d.special.SolidityAABB;
import dev.alexco.minecraft.phys.AABB;
import dev.alexco.minecraft.world.level.block.state.StateDefinition;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.block.state.properties.BooleanProperty;

public class DoorBlock extends Block{
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty TOP = BlockStateProperties.TOP;

    public DoorBlock(Block.Properties properties){
        super(properties);
        this.registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any()).setValue(OPEN, false).setValue(TOP, false));
    }
     @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN).add(TOP);

    }

    /**
     * Sets a thin closed-door collider, or no collider when open.
     */
    @Override
    public void setCollisionShape(BlockState blockState, int x, int y, AABB aabb) {
        boolean open = blockState.getValue(OPEN);
        if (open) {
            aabb.set(x, y, x, y);
            return;
        }
        aabb.set(x, y, x + 0.125, y + 1.0);
    }

    /**
     * Adds collision boxes only while the door is closed.
     */
    @Override
    public void addCollisionBoxes(BlockState blockState, int x, int y, List<SolidityAABB> out) {
        if (blockState.getValue(OPEN)) {
            return;
        }
        super.addCollisionBoxes(blockState, x, y, out);
    }
}
