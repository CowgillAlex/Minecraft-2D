package dev.alexco.minecraft.tests;

import dev.alexco.minecraft.Assert;
import dev.alexco.minecraft.TestCase;

public class TestTest extends TestCase {
    public TestTest(){
        super("Test Test");

    }

    @Override
    protected void test() throws Exception {
        Assert.success("Test functions properly");
    }
}
