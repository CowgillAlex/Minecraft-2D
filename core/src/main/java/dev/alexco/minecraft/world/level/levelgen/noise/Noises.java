package dev.alexco.minecraft.world.level.levelgen.noise;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.world.level.levelgen.noise.NormalNoise.NoiseParameters;
import dev.alexco.minecraft.world.level.levelgen.vanilla.VanillaWorldgenConfig;

public class Noises {
    public static Noise CONTINENTALNESS;
    public static Noise EROSION;
    public static Noise TEMPERATURE;
    public static Noise HUMIDITY;
    public static Noise WEIRDNESS;
    private static long currentSeed = Long.MIN_VALUE;
    private static boolean registered = false;

    static {
        initDefaults();
    }

    private static void initDefaults() {
        VanillaWorldgenConfig config = VanillaWorldgenConfig.get(0);

        CONTINENTALNESS = config.createNoise("continentalness",
                new NoiseParameters(-9, new double[] { 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0 }));
        EROSION = config.createNoise("erosion", new NoiseParameters(-9, new double[] { 1.0, 1.0, 0.0, 1.0, 1.0 }));
        TEMPERATURE = config.createNoise("temperature", new NoiseParameters(-10, new double[] { 1.5, 0.0, 1.0, 0.0, 0.0, 0.0 }));
        HUMIDITY = config.createNoise("vegetation", new NoiseParameters(-8, new double[] { 1.0, 1.0, 0.0, 0.0, 0.0, 0.0 }));
        WEIRDNESS = config.createNoise("ridge", new NoiseParameters(-7, new double[] { 1.0, 1.0, 1.0, 0.0, 0.0, 0.0 }));

        CONTINENTALNESS = register("continentalness", CONTINENTALNESS);
        EROSION = register("erosion", EROSION);
        TEMPERATURE = register("temperature", TEMPERATURE);
        HUMIDITY = register("humidity", HUMIDITY);
        WEIRDNESS = register("weirdness", WEIRDNESS);
        registered = true;
    }

    public static synchronized void ensureInitialized() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWorld() == null) {
            return;
        }
        long seed = mc.getWorld().seed;
        if (seed == currentSeed) {
            return;
        }

        VanillaWorldgenConfig config = VanillaWorldgenConfig.get(seed);

        CONTINENTALNESS = config.createNoise("continentalness",
                new NoiseParameters(-9, new double[] { 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0 }));
        EROSION = config.createNoise("erosion", new NoiseParameters(-9, new double[] { 1.0, 1.0, 0.0, 1.0, 1.0 }));
        TEMPERATURE = config.createNoise("temperature", new NoiseParameters(-10, new double[] { 1.5, 0.0, 1.0, 0.0, 0.0, 0.0 }));
        HUMIDITY = config.createNoise("vegetation", new NoiseParameters(-8, new double[] { 1.0, 1.0, 0.0, 0.0, 0.0, 0.0 }));
        WEIRDNESS = config.createNoise("ridge", new NoiseParameters(-7, new double[] { 1.0, 1.0, 1.0, 0.0, 0.0, 0.0 }));

        currentSeed = seed;
    }

    private static Noise register(String string, Noise noise) {
        return dev.alexco.registry.Registry.register(Registry.NOISE, string, noise);
    }
}
