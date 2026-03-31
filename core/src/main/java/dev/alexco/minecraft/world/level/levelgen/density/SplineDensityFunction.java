package dev.alexco.minecraft.world.level.levelgen.density;

import java.util.ArrayList;
import java.util.List;

/**
 * Cubic spline density function for smooth transitions between values.
 * Uses Catmull-Rom splines for C2 continuity.
 */
public class SplineDensityFunction implements DensityFunction {
    
    public static class ControlPoint {
        public final double input;
        public final double output;
        
        public ControlPoint(double input, double output) {
            this.input = input;
            this.output = output;
        }
    }
    
    private final DensityFunction input;
    private final List<ControlPoint> points;
    private final double[] derivatives;
    
    public SplineDensityFunction(DensityFunction input, List<ControlPoint> points) {
        this.input = input;
        this.points = new ArrayList<>(points);
        this.derivatives = computeDerivatives();
    }
    
    private double[] computeDerivatives() {
        int n = points.size();
        double[] d = new double[n];
        
        if (n < 2) return d;
        
        // For Catmull-Rom splines, compute derivatives at each point
        for (int i = 0; i < n; i++) {
            if (i == 0) {
                // Forward difference at start
                d[i] = (points.get(i + 1).output - points.get(i).output) / 
                       (points.get(i + 1).input - points.get(i).input);
            } else if (i == n - 1) {
                // Backward difference at end
                d[i] = (points.get(i).output - points.get(i - 1).output) / 
                       (points.get(i).input - points.get(i - 1).input);
            } else {
                // Central difference for interior points
                double h1 = points.get(i).input - points.get(i - 1).input;
                double h2 = points.get(i + 1).input - points.get(i).input;
                double s1 = (points.get(i).output - points.get(i - 1).output) / h1;
                double s2 = (points.get(i + 1).output - points.get(i).output) / h2;
                d[i] = (s1 * h2 + s2 * h1) / (h1 + h2);
            }
        }
        
        return d;
    }
    
    @Override
    public double sample(double x, double y) {
        double t = input.sample(x, y);
        
        if (points.isEmpty()) return 0;
        if (points.size() == 1) return points.get(0).output;
        
        // Find the interval
        int i = 0;
        while (i < points.size() - 1 && t > points.get(i + 1).input) {
            i++;
        }
        
        // Clamp to range
        if (t <= points.get(0).input) return points.get(0).output;
        if (t >= points.get(points.size() - 1).input) return points.get(points.size() - 1).output;
        
        // Cubic Hermite interpolation
        ControlPoint p0 = points.get(i);
        ControlPoint p1 = points.get(i + 1);
        double d0 = derivatives[i];
        double d1 = derivatives[i + 1];
        
        double h = p1.input - p0.input;
        double s = (t - p0.input) / h;
        
        // Hermite basis functions
        double h00 = 2 * s * s * s - 3 * s * s + 1;
        double h10 = s * s * s - 2 * s * s + s;
        double h01 = -2 * s * s * s + 3 * s * s;
        double h11 = s * s * s - s * s;
        
        return h00 * p0.output + h10 * h * d0 + h01 * p1.output + h11 * h * d1;
    }
    
    /**
     * Builder for creating spline density functions
     */
    public static class Builder {
        private final DensityFunction input;
        private final List<ControlPoint> points = new ArrayList<>();
        
        public Builder(DensityFunction input) {
            this.input = input;
        }
        
        public Builder addPoint(double input, double output) {
            points.add(new ControlPoint(input, output));
            return this;
        }
        
        public SplineDensityFunction build() {
            return new SplineDensityFunction(input, points);
        }
    }
}
