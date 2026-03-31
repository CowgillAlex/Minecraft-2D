package dev.alexco.minecraft.world.level.levelgen.density;

public class YGradientDensity implements DensityFunction {
    private final double startY, endY, startValue, endValue;
    public YGradientDensity(double startY, double endY, double startValue, double endValue){
        this.startY = startY; this.endY = endY;
        this.startValue = startValue; this.endValue = endValue;
    }
    @Override
    public double sample(double x, double y){
        double t = (y - startY)/(endY - startY);
        return startValue + t*(endValue - startValue);
    }
}
