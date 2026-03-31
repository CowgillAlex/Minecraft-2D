package dev.alexco.minecraft.world.level.chunk;

import dev.alexco.minecraft.util.BlockPos;

public class ChunkPos {
    public final int x;

    public ChunkPos(int x) {
        this.x = x;
    }

    public ChunkPos(BlockPos blockPos) {
        this.x = blockPos.getX() >> 4;
    }

    public ChunkPos(long x) {
        this.x = (int) x;
    }

    public int getX() {
        return x;
    }

    @Override
    public int hashCode() {
        return (1664525 * this.x + 1013904223) ^ (1664525 * (0xDEADBEEF) + 1013904223);
    }

    public int getMinBlockX() {
        return this.x << 4;
    }

    public int getMaxBlockX() {
        return (this.x << 4) + 15;
    }

    @Override
    public String toString() {
        return "[" + this.x + "]";
    }

    public BlockPos getBlockAt(int x, int y) {
        return new BlockPos(this.x << 4, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ChunkPos))
            return false;
        ChunkPos other = (ChunkPos) obj;
        return this.x == other.x;
    }
}
