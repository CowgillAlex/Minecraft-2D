package dev.alexco.minecraft.world.level.levelgen.density;

/**
 * Creates a tent-shaped gradient centred on a specific Y level.
 * Peak value at centerY, fades to 0 at centerY Â± range.
 * Useful for coupling effects to surface height.
 */
public class TentDensityFunction implements DensityFunction {
    private final double centerY;
    private final double range;
    private final double peakValue;

    public TentDensityFunction(double centerY, double range, double peakValue) {
        this.centerY = centerY;
        this.range = range;
        this.peakValue = peakValue;
    }

    @Override
    public double sample(double x, double y) {
        double distance = Math.abs(y - centerY);
        if (distance >= range) {
            return 0.0;
        }
        // Linear interpolation: peak at centre, 0 at edges
        return peakValue * (1.0 - distance / range);
    }
}
