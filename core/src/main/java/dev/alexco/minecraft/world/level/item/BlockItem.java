package dev.alexco.minecraft.world.level.item;

import java.util.Map;

import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;

public class BlockItem extends Item {
    private final Block block;
    private final BlockState blockState;

    public BlockItem(Block block, Properties properties) {
        super(properties);
        this.block = block;
        this.blockState = block.defaultBlockState();

        //Logger.INFO("As a %s, I can stack to %s",getBlock().getDescriptionId(),  this.getMaxStackSize());

    }

    public BlockItem(BlockState blockState, Properties properties) {
        super(properties);
        this.block = blockState.getBlock();
        this.blockState = blockState;
    }

    @Override
    public String getDescriptionId() {
        return this.getBlock().getDescriptionId();
    }

    public Block getBlock() {
        return this.block;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    /**
     * Compares block items including state unless ignored by registry rules.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockItem)) {
            return false;
        }
        BlockItem other = (BlockItem) obj;

        if (!this.block.equals(other.block)) {
            return false;
        }

        if (Items.IGNORE_STATE_PROPERTIES.contains(this.block)) {
            return true;
        }

        var thisProps = this.blockState.getProperties();
        var otherProps = other.blockState.getProperties();

        if (thisProps.size() != otherProps.size()) {
            return false;
        }

        for (var property : thisProps) {
            if (!otherProps.contains(property)) {
                return false;
            }
            Comparable<?> thisValue = this.blockState.getValue(property);
            Comparable<?> otherValue = other.blockState.getValue(property);
            if (!thisValue.equals(otherValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Hashes block and optionally state properties for stack compatibility.
     */
    @Override
    public int hashCode() {
        int hash = this.block.hashCode();
        if (Items.IGNORE_STATE_PROPERTIES.contains(this.block)) {
            return hash;
        }
        for (var property : this.blockState.getProperties()) {
            hash = 31 * hash + this.blockState.getValue(property).hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return getFullDescriptionWithProperties();
    }

    /**
     * Returns block id with serialised state properties.
     */
    public String getFullDescriptionWithProperties() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.block.getKey().toString());

        if (!this.blockState.getProperties().isEmpty()) {
            sb.append("[");
            boolean first = true;
            for (var property : this.blockState.getProperties()) {
                if (!first) {
                    sb.append(", ");
                }
                Comparable<?> value = this.blockState.getValue(property);
                sb.append(property.getName())
                  .append("=")
                  .append(value);
                first = false;
            }
            sb.append("]");
        }

        return sb.toString();
    }

    public void registerBlocks(Map<Block, Item> map, Item item) {
        map.put(this.getBlock(), item);
    }
}
