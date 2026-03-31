package dev.alexco.minecraft.world.level.levelgen.density;

public interface DensityFunction {

    double sample(double x, double y);
}
