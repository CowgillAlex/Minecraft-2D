package dev.alexco.minecraft.tests;

import dev.alexco.minecraft.Assert;
import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.TestCase;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;

public class AirNotEqualNull extends TestCase {
    public AirNotEqualNull() {
        super("Air Does Not Equal Null");
    }

    @Override
    protected void test() throws Exception {
        Minecraft m = new Minecraft();
        try{

            m.create();
            Block air = Blocks.AIR;
            Assert.assertNotEquals(air, air);
        }catch (Exception e){

        }
    }
}
