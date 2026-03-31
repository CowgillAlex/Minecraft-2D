package dev.alexco.minecraft.world.level.levelgen.density;

public class ConstantDensity implements DensityFunction {
    private final double value;

    public ConstantDensity(double value) { this.value = value; }

    @Override
    public double sample(double x, double y) {
        return value;
    }

}
