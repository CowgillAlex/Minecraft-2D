package dev.alexco.minecraft.world.level.levelgen.noise;

import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;

public class SimplexNoise {
    public static final int[][] GRADIENT = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}, {1, 1, 0}, {0, -1, 1}, {-1, 1, 0}, {0, -1, -1}};
    public static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    public static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;

    public final int[] p;
    public final double xo, yo, zo;

    public SimplexNoise(Xoroshiro random) {
        this.xo = random.nextDouble() * 256;
        this.yo = random.nextDouble() * 256;
        this.zo = random.nextDouble() * 256;
        this.p = new int[256];

        for (int i = 0; i < 256; i++) {
            this.p[i] = i;
        }

        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256 - i);
            int b = this.p[i];
            this.p[i] = this.p[i + j];
            this.p[i + j] = b;
        }
    }

    public double sample2D(double d, double d2) {
        double d6 = (d + d2) * F2;
        int n4 = (int) Math.floor(d + d6);
        int n3 = (int) Math.floor(d2 + d6);
        double d3 = (n4 + n3) * G2;
        double d7 = n4 - d3;
        double d8 = d - d7;
        double d4 = d2 - (n3 - d3);
        int a, b;

        if (d8 > d4) {
            a = 1;
            b = 0;
        } else {
            a = 0;
            b = 1;
        }

        int n5 = n4 & 0xFF;
        int n6 = n3 & 0xFF;
        int n7 = P(n5 + P(n6)) % 12;
        int n8 = P(n5 + a + P(n6 + b)) % 12;
        int n9 = P(n5 + 1 + P(n6 + 1)) % 12;

        double d13 = getCornerNoise3D(n7, d8, d4, 0.0, 0.5);
        double d14 = getCornerNoise3D(n8, d8 - a + G2, d4 - b + G2, 0.0, 0.5);
        double d15 = getCornerNoise3D(n9, d8 - 1.0 + 2.0 * G2, d4 - 1.0 + 2.0 * G2, 0.0, 0.5);

        return 70.0 * (d13 + d14 + d15);
    }

    public int P(int i) {
        return p[i & 0xFF];
    }

    public double getCornerNoise3D(int i, double a, double b, double c, double d) {
        double e = d - a * a - b * b - c * c;
        if (e < 0.0) return 0.0;
        e *= e;
        return e * e * gradDot(i, a, b, c);
    }

    public static double gradDot(int a, double b, double c, double d) {
        int[] grad = GRADIENT[a & 15];
        return grad[0] * b + grad[1] * c + grad[2] * d;
    }
    public double getXo() {
        return this.xo;
    }

    public double getYo() {
        return this.yo;
    }

}
