package dev.alexco.minecraft.world.level.levelgen.noise;

import java.util.HashSet;
import java.util.Set;

import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;

public class PerlinSimplexNoise implements Noise{
    private final SimplexNoise[] noiseLevels;
    private final double highestFreqInputFactor;
    private final double highestFreqValueFactor;

    public PerlinSimplexNoise(Xoroshiro random, int[] octaves) {
        int lastOctave = octaves[octaves.length - 1];
        int negFirstOctave = -octaves[0];
        int range = negFirstOctave + lastOctave + 1;
        Set<Integer> octavesSet = new HashSet<>();
        for (int octave : octaves) {
            octavesSet.add(octave);
        }

        SimplexNoise noise = new SimplexNoise(random);
        noiseLevels = new SimplexNoise[range];
        if (lastOctave >= 0 && lastOctave < range && octavesSet.contains(0)) {
            noiseLevels[lastOctave] = noise;
        }

        for (int i = lastOctave + 1; i < range; i++) {
            if (i >= 0 && octavesSet.contains(lastOctave - i)) {
                noiseLevels[i] = new SimplexNoise(random);
            } else {
                random.consume(262);
            }
        }

        if (lastOctave > 0) {
            throw new IllegalArgumentException("Positive octaves are not allowed");
        }

        highestFreqInputFactor = Math.pow(2, lastOctave);
        highestFreqValueFactor = 1.0 / (Math.pow(2, range) - 1);
    }

    public double sample(double x, double y, boolean useOffsets) {
        double value = 0;
        double inputF = highestFreqInputFactor;
        double valueF = highestFreqValueFactor;
        for (SimplexNoise noise : noiseLevels) {
            if (noise != null) {
                value += valueF * noise.sample2D(
                    x * inputF + (useOffsets ? noise.getXo() : 0),
                    y * inputF + (useOffsets ? noise.getYo() : 0)
                );
            }
            inputF /= 2;
            valueF *= 2;
        }
        return value;
    }

    @Override
    public double sample(double x) {
        return sample(x, 0);
    }

    @Override
    public double sample(double x, double y) {
        return sample(x, 0, y);
    }

    @Override
    public double sample(double x, double y, double z) {
        return sample(x, y, false);
    }
}

