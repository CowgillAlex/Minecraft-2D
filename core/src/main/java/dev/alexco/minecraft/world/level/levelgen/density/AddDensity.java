package dev.alexco.minecraft.world.level.levelgen.density;

public class AddDensity implements DensityFunction {
    private final DensityFunction a, b;
    public AddDensity(DensityFunction a, DensityFunction b) { this.a = a; this.b = b; }
    @Override
    public double sample(double x, double y) {
        return a.sample(x, y) + b.sample(x, y);
    }
}
