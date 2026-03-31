package dev.alexco.minecraft.world.level.levelgen.noise;

import com.badlogic.gdx.utils.JsonValue;

import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;

public class NormalNoise implements Noise{
    private static final double INPUT_FACTOR = 1.0181268882175227;

    public final double valueFactor;
    public final PerlinNoise first;
    public final PerlinNoise second;
    public final double maxValue;

    public NormalNoise(Xoroshiro random, NoiseParameters params)  {
        this.first = new PerlinNoise(random, params.firstOctave, params.amplitudes, false);
        this.second = new PerlinNoise(random, params.firstOctave, params.amplitudes, false);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < params.amplitudes.length; i++) {
            if (params.amplitudes[i] != 0) {
                min = Math.min(min, i);
                max = Math.max(max, i);
            }
        }

        double expectedDeviation = 0.1 * (1 + 1.0 / (max - min + 1));
        this.valueFactor = (1.0 / 6.0) / expectedDeviation;
        this.maxValue = (this.first.getMaxValue() + this.second.getMaxValue()) * this.valueFactor;
    }

    public double sample(double x) {
        return sample(x, 0.0, 0.0);
    }

    public double sample(double x, double y) {
        return sample(x, y, 0.0);
    }

    public double sample(double x, double y, double z) {
        double x2 = x * INPUT_FACTOR;
        double y2 = y * INPUT_FACTOR;
        double z2 = z * INPUT_FACTOR;
        return (this.first.sample(x, y, z) + this.second.sample(x2, y2, z2)) * this.valueFactor * 2f;
    }

    public static class NoiseParameters {
        public final int firstOctave;
        public final double[] amplitudes;

        public NoiseParameters(int firstOctave, double[] amplitudes) {
            this.firstOctave = firstOctave;
            this.amplitudes = amplitudes;
        }

        public static NoiseParameters create(int firstOctave, double[] amplitudes) {
            return new NoiseParameters(firstOctave, amplitudes);
        }

        public static NoiseParameters fromJson(JsonValue json) {
            if (json == null) {
                return new NoiseParameters(0, new double[0]);
            }

            int firstOctave = json.getInt("firstOctave", 0);

            JsonValue ampsJson = json.get("amplitudes");
            double[] amplitudes;
            if (ampsJson != null && ampsJson.isArray()) {
                int size = ampsJson.size;
                amplitudes = new double[size];
                for (int i = 0; i < size; i++) {
                    amplitudes[i] = ampsJson.get(i).asDouble();
                }
            } else {
                amplitudes = new double[0];
            }

            return new NoiseParameters(firstOctave, amplitudes);
        }
    }
    public double getMaxValue() {
        return maxValue;
    }



}
