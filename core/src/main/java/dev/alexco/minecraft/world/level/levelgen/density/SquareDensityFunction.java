package dev.alexco.minecraft.world.level.levelgen.density;

public class SquareDensityFunction implements DensityFunction {
    private final DensityFunction child;
    public SquareDensityFunction(DensityFunction child) { this.child = child; }
    @Override
    public double sample(double x, double y){
        double val = child.sample(x, y);
        return val * val;
    }
}
