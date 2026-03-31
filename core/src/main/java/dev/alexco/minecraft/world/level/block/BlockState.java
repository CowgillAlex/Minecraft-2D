package dev.alexco.minecraft.world.level.block;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import dev.alexco.minecraft.util.BlockPos;
import dev.alexco.minecraft.world.level.block.state.AbstractStateHolder;
import dev.alexco.minecraft.world.level.block.state.properties.Property;
import dev.alexco.minecraft.world.level.material.Material;

public class BlockState extends AbstractStateHolder<Block, BlockState> {
    @Nullable
    private Cache cache;

    public void initCache() {

            this.cache = new Cache(this);

    }

    public BlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap) {
        super(block, immutableMap);

    }

  public Block getBlock() {
        return (Block)this.owner;
    }

    public Material getMaterial() {
        return this.getBlock().getMaterial(this);
    }

    public int getLightEmission() {
        return this.getBlock().getLightEmission(this);
    }

    static final class Cache {

        private Cache(BlockState blockState) {
        }
    }
}
