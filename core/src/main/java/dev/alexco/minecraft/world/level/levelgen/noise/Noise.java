package dev.alexco.minecraft.world.level.levelgen.noise;

public interface Noise {
    public double sample(double x);
    public double sample(double x, double y);
    public double sample(double x, double y, double z);
}
