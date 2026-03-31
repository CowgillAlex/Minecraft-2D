package dev.alexco.minecraft.world.level.levelgen.noise;

import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;

public class ImprovedNoise implements Noise {
     private final int[] p = new int[256];
    private final double xo, yo, zo;

    public ImprovedNoise(Xoroshiro random) {
        this.xo = random.nextDouble() * 256;
        this.yo = random.nextDouble() * 256;
        this.zo = random.nextDouble() * 256;

        for (int i = 0; i < 256; i++) {
            p[i] = i > 127 ? i - 256 : i;
        }
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256 - i);
            int b = p[i];
            p[i] = p[i + j];
            p[i + j] = b;
        }
    }

    public double sample(double x, double y, double z, double yScale, double yLimit) {
        double x2 = x + xo;
        double y2 = y + yo;
        double z2 = z + zo;
        int x3 = (int) Math.floor(x2);
        int y3 = (int) Math.floor(y2);
        int z3 = (int) Math.floor(z2);
        double x4 = x2 - x3;
        double y4 = y2 - y3;
        double z4 = z2 - z3;

        double y6 = 0;
        if (yScale != 0) {
            double t = (yLimit >= 0 && yLimit < y4) ? yLimit : y4;
            y6 = Math.floor(t / yScale + 1e-7) * yScale;
        }

        return sampleAndLerp(x3, y3, z3, x4, y4 - y6, z4, y4);
    }

    private double sampleAndLerp(int a, int b, int c, double d, double e, double f, double g) {
        int h = P(a);
        int i = P(a + 1);
        int j = P(h + b);
        int k = P(h + b + 1);
        int l = P(i + b);
        int m = P(i + b + 1);

        double n = SimplexNoise.gradDot(P(j + c), d, e, f);
        double o = SimplexNoise.gradDot(P(l + c), d - 1.0, e, f);
        double p = SimplexNoise.gradDot(P(k + c), d, e - 1.0, f);
        double q = SimplexNoise.gradDot(P(m + c), d - 1.0, e - 1.0, f);
        double r = SimplexNoise.gradDot(P(j + c + 1), d, e, f - 1.0);
        double s = SimplexNoise.gradDot(P(l + c + 1), d - 1.0, e, f - 1.0);
        double t = SimplexNoise.gradDot(P(k + c + 1), d, e - 1.0, f - 1.0);
        double u = SimplexNoise.gradDot(P(m + c + 1), d - 1.0, e - 1.0, f - 1.0);

        double v = smoothstep(d);
        double w = smoothstep(g);
        double x = smoothstep(f);

        return lerp3(v, w, x, n, o, p, q, r, s, t, u);
    }

    private int P(int i) {
        return p[i & 0xFF] & 0xFF;
    }

    private static double smoothstep(double x) {

        return x * x * x * (x * (x * 6 - 15) + 10);
    }

    private static double lerp3(double a, double b, double c, double d, double e, double f, double g, double h, double i, double j, double k) {

        return lerp(c, lerp2(a, b, d, e, f, g), lerp2(a, b, h, i, j, k));
    }
    private static double lerp2(double a, double b, double c, double d, double e, double f){
        return lerp(b, lerp(a, c, d), lerp(a, e, f));
    }
    private static double lerp(double a, double b, double c) {
        return b + a * (c - b);
    }
    public double getXo() {
        return xo;
    }
    public int[] getP() {
        return p;
    }
    public double getYo() {
        return yo;
    }
    public double getZo() {
        return zo;
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
        return sample(x, y, z, 1, 0);
    }
}
