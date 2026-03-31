package dev.alexco.minecraft.world.level.levelgen.density;

public class DensityFunctionBuilder {
    private DensityFunction current;

    public DensityFunctionBuilder(DensityFunction base) {
        this.current = base;
    }

    public DensityFunctionBuilder abs() {
        current = new AbsDensityFunction(current);
        return this;
    }

    public DensityFunctionBuilder square() {
        current = new SquareDensityFunction(current);
        return this;
    }

    public DensityFunctionBuilder add(DensityFunction other) {
        current = new AddDensity(current, other);
        return this;
    }

    public DensityFunctionBuilder multiply(DensityFunction other) {
        current = new MultiplyDensity(current, other);
        return this;
    }

    public DensityFunctionBuilder multiply(double scalar) {
        current = new MultiplyDensity(current, new ConstantDensity(scalar));
        return this;
    }

    public DensityFunctionBuilder min(DensityFunction other) {
        current = new MinDensityFunction(current, other);
        return this;
    }

    public DensityFunction build() {
        return current;
    }
}
