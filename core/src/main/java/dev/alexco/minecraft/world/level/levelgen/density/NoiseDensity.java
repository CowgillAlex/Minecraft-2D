package dev.alexco.minecraft.world.level.levelgen.density;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.world.level.levelgen.noise.Noise;
import dev.alexco.registry.ResourceLocation;

public class NoiseDensity implements DensityFunction {
    private static final Map<ResourceLocation, Noise> NOISE_CACHE = new ConcurrentHashMap<>();

    private final Noise noise;
    private final double xScale;
    private final double yScale;

    public NoiseDensity(String noiseId, double xScale, double yScale) {
        this(ResourceLocation.tryParse(noiseId), xScale, yScale);
    }

    public NoiseDensity(ResourceLocation noiseId, double xScale, double yScale) {
        this.noise = NOISE_CACHE.computeIfAbsent(noiseId, Registry.NOISE::get);
        if (this.noise == null) {
            throw new IllegalArgumentException("Unknown noise: " + noiseId);
        }
        this.xScale = xScale;
        this.yScale = yScale;
    }

    @Override
    public double sample(double x, double y) {
        return noise.sample(x * xScale, y * yScale);
    }
}
