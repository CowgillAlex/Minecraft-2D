package dev.alexco.minecraft.tests;

import dev.alexco.minecraft.Assert;
import dev.alexco.minecraft.TestCase;
import dev.alexco.minecraft.util.BlockPos;
import dev.alexco.minecraft.world.level.chunk.ChunkPos;

public class ChunkPosInstance extends TestCase{
    public ChunkPosInstance(){
        super("Chunk Positions are equal");
    }

    @Override
    protected void test() throws Exception {
        ChunkPos zero = new ChunkPos(0);
        ChunkPos zeroZero = new ChunkPos(new BlockPos(0, 0));
        Assert.assertEquals(zero, zeroZero);
    }
}
