package dev.alexco.minecraft.world.level.levelgen.density;

public class AbsDensityFunction implements DensityFunction {
    private final DensityFunction child;
    public AbsDensityFunction(DensityFunction child) { this.child = child; }
    @Override
    public double sample(double x, double y) { return Math.abs(child.sample(x, y)); }
}
