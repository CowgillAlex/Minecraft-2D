package dev.alexco.minecraft.world.level.levelgen.density;

/**
 * Returns the minimum of two density functions at each point.
 */
public class MinDensityFunction implements DensityFunction {
    private final DensityFunction a;
    private final DensityFunction b;

    public MinDensityFunction(DensityFunction a, DensityFunction b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public double sample(double x, double y) {
        return Math.min(a.sample(x, y), b.sample(x, y));
    }
}
