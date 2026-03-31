package dev.alexco.minecraft.loot;

import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.item.Item;

public class LootContext {
    private final BlockState blockState;
    private final Item toolItem;

    public LootContext(BlockState blockState, Item toolItem) {
        this.blockState = blockState;
        this.toolItem = toolItem;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public Item getToolItem() {
        return toolItem;
    }
}
