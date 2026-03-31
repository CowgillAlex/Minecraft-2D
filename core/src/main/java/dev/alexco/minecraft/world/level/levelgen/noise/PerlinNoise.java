package dev.alexco.minecraft.world.level.levelgen.noise;

import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;

public class PerlinNoise implements Noise {
    public final ImprovedNoise[] noiseLevels;
    public final double[] amplitudes;
    public final double lowestFreqInputFactor;
    public final double lowestFreqValueFactor;
    public final double maxValue;

    public PerlinNoise(Xoroshiro random, int firstOctave, double[] amplitudes, boolean forceLegacy) {
        if (forceLegacy && amplitudes.length > (1 - firstOctave)) {
            throw new IllegalArgumentException("Too many octaves for legacy mode");
        }

        this.amplitudes = new double[amplitudes.length];
        this.noiseLevels = new ImprovedNoise[amplitudes.length];

        for (int i = 0; i < amplitudes.length; i++) {
            this.amplitudes[i] = computeAmplitude(amplitudes[i], i, amplitudes.length);
        }

        if (!forceLegacy) {
            Xoroshiro forkedRandom = random.forkPositional();
            for (int i = 0; i < amplitudes.length; i++) {
                if (this.amplitudes[i] != 0.0) {
                    int octave = firstOctave + i;
                    this.noiseLevels[i] = new ImprovedNoise(forkedRandom.fromHashOf("octave_" + octave));
                }
            }
        } else {
            for (int i = -firstOctave; i >= 0; i--) {
                if (i < amplitudes.length && this.amplitudes[i] != 0.0) {
                    this.noiseLevels[i] = new ImprovedNoise(random);
                } else {
                    random.consume(262);
                }
            }
        }

        this.lowestFreqInputFactor = Math.pow(2, firstOctave);
        this.lowestFreqValueFactor = computeLowestFreqValueFactor(amplitudes.length);
        this.maxValue = edgeValue(2);
    }

    private static double computeAmplitude(double valueAtIndex, int index, int size) {
        return 1.04 * valueAtIndex * (Math.pow(2, (size - index - 1)) / (Math.pow(2, size) - 1));
    }

    private static double computeLowestFreqValueFactor(int size) {
        return Math.pow(2, (size - 1)) / (Math.pow(2, size) - 1);
    }

    public double sample(double x, double y, double z, double yScale, double yLimit, boolean fixY) {
        double value = 0.0;
        double inputF = Math.pow(2, -this.noiseLevels.length);
        double valueF = this.lowestFreqValueFactor;

        for (int i = 0; i < this.noiseLevels.length; i++) {
            ImprovedNoise noise = this.noiseLevels[i];
            if (noise != null) {
                value += this.amplitudes[i] * valueF * noise.sample(
                    wrap(x * inputF),
                    fixY ? -noise.getYo() : wrap(y * inputF),
                    wrap(z * inputF),
                    yScale * inputF,
                    yLimit * inputF
                );
            }
            inputF *= 2.0;
            valueF /= 2.0;
        }
        return value;
    }

    public ImprovedNoise getOctaveNoise(int i) {
        return this.noiseLevels[this.noiseLevels.length - 1 - i];
    }

    public double edgeValue(double x) {
        double value = 0.0;
        double valueF = 1.0;
        int nonZeroCount = 0;

        for (int i = 0; i < this.noiseLevels.length; i++) {
            if (this.amplitudes[i] != 0.0) {
                nonZeroCount++;
                value += this.amplitudes[i] * x * valueF;
            }
            valueF /= 2.0;
        }
        double m = nonZeroCount == 0 ? 1 : nonZeroCount;
        return 10.0 * value / (3.0 * (1.0 + 1.0 / m));
    }

    public static double wrap(double value) {
        return value - Math.floor(value / 3.3554432E7 + 0.5) * 3.3554432E7;
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
        return sample(x, y, z, 1, 1, false);
    }

    public double getMaxValue(){
        return this.maxValue;
    }
}
