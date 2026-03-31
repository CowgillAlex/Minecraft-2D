package dev.alexco.minecraft.world.level.levelgen.feature;

import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.chunk.Chunk;

public abstract class Feature {
    
    public abstract boolean place(FeatureContext context);
    
    public static class FeatureContext {
        private final Chunk chunk;
        private final int chunkWorldX;
        
        public FeatureContext(Chunk chunk, int chunkWorldX) {
            this.chunk = chunk;
            this.chunkWorldX = chunkWorldX;
        }
        
        public Chunk getChunk() {
            return chunk;
        }
        
        public int getChunkWorldX() {
            return chunkWorldX;
        }
        
        public BlockState getBlock(int localX, int y) {
            return chunk.getBlockAt(localX, y);
        }
        
        public boolean setBlock(int localX, int y, BlockState state) {
            chunk.setBlockAt(localX, y, state);
            return true;
        }
    }
}
