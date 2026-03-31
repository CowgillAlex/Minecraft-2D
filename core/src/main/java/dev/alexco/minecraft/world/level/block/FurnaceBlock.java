package dev.alexco.minecraft.world.level.block;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.block.entity.BlockEntity;
import dev.alexco.minecraft.world.level.block.entity.FurnaceBlockEntity;
import dev.alexco.minecraft.world.level.block.state.StateDefinition;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.block.state.properties.BooleanProperty;

public class FurnaceBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public FurnaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public int getLightEmission(BlockState blockState) {
        return blockState.getValue(LIT) ? 14 : 0;
    }

    /**
     * Creates a furnace block entity when placed.
     */

    public void onPlace(int x, int y) {
        if (Minecraft.getInstance().getWorld() != null) {
            // Only create if doesn't exist (prevents recreation during state changes)
            if (!Minecraft.getInstance().getWorld().getBlockEntityManager().hasBlockEntity(x, y)) {
                FurnaceBlockEntity blockEntity = new FurnaceBlockEntity(x, y);
                Minecraft.getInstance().getWorld().getBlockEntityManager().addBlockEntity(x, y, blockEntity);
            }
        }
    }

    /**
     * Removes the furnace block entity at this position.
     */
    public void onRemove(int x, int y) {
        if (Minecraft.getInstance().getWorld() != null) {
            Minecraft.getInstance().getWorld().getBlockEntityManager().removeBlockEntity(x, y);
        }
    }

    /**
     * Returns the furnace block entity at a position if present.
     */
    public static FurnaceBlockEntity getBlockEntity(int x, int y) {
        if (Minecraft.getInstance().getWorld() != null) {
            BlockEntity entity = Minecraft.getInstance().getWorld().getBlockEntityManager().getBlockEntity(x, y);
            if (entity instanceof FurnaceBlockEntity) {
                return (FurnaceBlockEntity) entity;
            }
        }
        return null;
    }

    /**
     * Toggles furnace lit state without replacing block entity data.
     */
    public static void updateLitState(int x, int y, boolean lit) {
        if (Minecraft.getInstance().getWorld() == null) return;

        World world = Minecraft.getInstance().getWorld();
        BlockState currentState = world.getBlock(x, y);

        if (!(currentState.getBlock() instanceof FurnaceBlock)) return;

        boolean currentLit = currentState.getValue(LIT);
        if (currentLit == lit) return;


        BlockState newState = currentState.setValue(LIT, lit);
        world.updateBlockState(x, y, newState);
    }
}
