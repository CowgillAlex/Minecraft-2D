package dev.alexco.minecraft.world.level.block;

import java.util.List;

import dev.alexco.minecraft.blaze2d.special.SolidityAABB;
import dev.alexco.minecraft.phys.AABB;
import dev.alexco.minecraft.phys.AABBPool;
import dev.alexco.minecraft.util.Direction;
import dev.alexco.minecraft.world.level.block.state.StateDefinition;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.block.state.properties.BooleanProperty;
import dev.alexco.minecraft.world.level.block.state.properties.EnumProperty;

public class OakStairsBlock extends Block {
    public static final BooleanProperty TOP = BlockStateProperties.TOP;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public OakStairsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(TOP, false)
                .setValue(FACING, Direction.RIGHT)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP, FACING);
    }

    /**
     * Adds two collision boxes that form the stair shape.
     */
    @Override
    public void addCollisionBoxes(BlockState blockState, int x, int y, List<SolidityAABB> out) {
        boolean top = blockState.getValue(TOP);
        Direction facing = blockState.getValue(FACING);

        if (!top) {
            addBox(out, x, y, x + 1.0, y + 0.5);
            if (facing == Direction.LEFT) {
                addBox(out, x, y + 0.5, x + 0.5, y + 1.0);
            } else {
                addBox(out, x + 0.5, y + 0.5, x + 1.0, y + 1.0);
            }
            return;
        }

        addBox(out, x, y + 0.5, x + 1.0, y + 1.0);
        if (facing == Direction.LEFT) {
            addBox(out, x, y, x + 0.5, y + 0.5);
        } else {
            addBox(out, x + 0.5, y, x + 1.0, y + 0.5);
        }
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

    /**
     * Adds one pooled collision box to the output list.
     */
    private void addBox(List<SolidityAABB> out, double x0, double y0, double x1, double y1) {
        AABB aabb = AABBPool.AABBpool.get(0, 0, 0, 0);
        aabb.set(x0, y0, x1, y1);
        out.add(SolidityAABB.create(aabb, getSolidity()));
    }
}
