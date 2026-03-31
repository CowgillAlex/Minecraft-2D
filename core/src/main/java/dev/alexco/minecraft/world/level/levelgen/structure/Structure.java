package dev.alexco.minecraft.world.level.levelgen.structure;

import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.block.BlockState;
import java.util.ArrayList;
import java.util.List;


public class Structure {
    private final String name;
    private final List<StructureBlock> blocks;
    private final List<StructureBlock> backgroundBlocks;
    private final int width;
    private final int height;

    public Structure(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.blocks = new ArrayList<>();
        this.backgroundBlocks = new ArrayList<>();
    }

    /**
     * Add a block to the structure at relative coordinates
     */
    public Structure addBlock(int relX, int relY, BlockState state) {
        blocks.add(new StructureBlock(relX, relY, state));
        return this;
    }

    /**
     * Add a background block to the structure
     */
    public Structure addBackgroundBlock(int relX, int relY, BlockState state) {
        backgroundBlocks.add(new StructureBlock(relX, relY, state));
        return this;
    }

    public String getName() {
        return name;
    }

    public List<StructureBlock> getBlocks() {
        return blocks;
    }

    public List<StructureBlock> getBackgroundBlocks() {
        return backgroundBlocks;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Represents a single block in a structure template
     */
    public static class StructureBlock {
        public final int relX, relY;
        public final BlockState state;

        public StructureBlock(int relX, int relY, BlockState state) {
            this.relX = relX;
            this.relY = relY;
            this.state = state;
        }
    }
}
