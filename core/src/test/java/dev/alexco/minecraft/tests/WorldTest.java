package dev.alexco.minecraft.tests;

import dev.alexco.minecraft.Assert;
import dev.alexco.minecraft.TestCase;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.chunk.Chunk;

public class WorldTest extends TestCase {
    public WorldTest(){
        super("World");
    }

    @Override
    protected void test() throws Exception {
        World w = new World();
        Assert.assertNotNull(w, "World not null");
        Assert.assertNotNull(w, "World not null");
        Assert.assertNotNull(w, "World not null");
        Chunk  c = w.getChunk(0);
        Assert.assertNotNull(c, "World not null");
    }
}
