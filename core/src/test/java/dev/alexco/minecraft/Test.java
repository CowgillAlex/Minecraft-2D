package dev.alexco.minecraft;

import dev.alexco.minecraft.tests.AirNotEqualNull;
import dev.alexco.minecraft.tests.ChunkPosInstance;
import dev.alexco.minecraft.tests.TestTest;
import dev.alexco.minecraft.tests.WorldTest;

public class Test {
    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.addTest(new TestTest());
        runner.addTest(new AirNotEqualNull());
        runner.addTest(new ChunkPosInstance());
       // runner.addTest(new WorldTest());
        runner.runAll();
    }
}
