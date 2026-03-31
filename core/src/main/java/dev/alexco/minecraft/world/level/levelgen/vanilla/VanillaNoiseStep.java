package dev.alexco.minecraft.world.level.levelgen.vanilla;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.levelgen.noise.ImprovedNoise;
import dev.alexco.minecraft.world.level.levelgen.noise.Noise;
import dev.alexco.minecraft.world.level.levelgen.noise.NormalNoise;
import dev.alexco.minecraft.world.level.levelgen.noise.PerlinNoise;
import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VanillaNoiseStep {
    private static volatile VanillaNoiseStep INSTANCE;

    private final long seed;
    private final VanillaWorldgenConfig config;
    private final Map<String, DensityNode> densityById = new HashMap<>();
    private final Map<String, Noise> noiseById = new HashMap<>();
    private final DensityNode finalDensity;
    
    // Climate parameter density functions from noise_router
    private DensityNode temperatureDensity;
    private DensityNode humidityDensity;
    private DensityNode continentalnessDensity;
    private DensityNode erosionDensity;
    private DensityNode weirdnessDensity;

    private VanillaNoiseStep(long seed) {
        this.seed = seed;
        this.config = VanillaWorldgenConfig.get(seed);

        JsonObject noiseSettings = readJson("data/minecraft/worldgen/noise_settings/overworld.json");
        JsonObject router = noiseSettings.getAsJsonObject("noise_router");
        this.finalDensity = parseNode(router.get("final_density"));
        
        // Parse climate parameters from noise_router
        this.temperatureDensity = parseNode(router.get("temperature"));
        this.humidityDensity = parseNode(router.get("vegetation")); // humidity uses vegetation noise
        this.continentalnessDensity = parseNode(router.get("continents"));
        this.erosionDensity = parseNode(router.get("erosion"));
        this.weirdnessDensity = parseNode(router.get("ridges")); // weirdness uses ridges
    }

    public static VanillaNoiseStep get(long seed) {
        VanillaNoiseStep current = INSTANCE;
        if (current == null || current.seed != seed) {
            synchronized (VanillaNoiseStep.class) {
                current = INSTANCE;
                if (current == null || current.seed != seed) {
                    INSTANCE = current = new VanillaNoiseStep(seed);
                }
            }
        }
        return current;
    }

    public double sampleFinalDensity(int blockX, int blockY, int blockZ) {
        return finalDensity.sample(new Context(blockX, blockY, blockZ));
    }

    public double sampleFinalDensity2D(int blockX, int blockY) {
        return sampleFinalDensity(blockX, blockY, 0);
    }
    
    /**
     * Sample climate parameters at a block position.
     * IMPORTANT: Uses BLOCK coordinates directly, not quarter coordinates!
     * This matches Minecraft's Climate.Sampler which converts quart coords 
     * back to block coords before evaluating DensityFunctions.
     * Using block coordinates ensures biomes align with terrain.
     */
    public double sampleTemperatureAtBlock(int blockX, int blockY, int blockZ) {
        // Sample at BLOCK coordinates (not quarter) to match terrain sampling
        return temperatureDensity.sample(new Context(blockX, blockY, blockZ));
    }
    
    public double sampleHumidityAtBlock(int blockX, int blockY, int blockZ) {
        return humidityDensity.sample(new Context(blockX, blockY, blockZ));
    }
    
    public double sampleContinentalnessAtBlock(int blockX, int blockY, int blockZ) {
        return continentalnessDensity.sample(new Context(blockX, blockY, blockZ));
    }
    
    public double sampleErosionAtBlock(int blockX, int blockY, int blockZ) {
        return erosionDensity.sample(new Context(blockX, blockY, blockZ));
    }
    
    public double sampleWeirdnessAtBlock(int blockX, int blockY, int blockZ) {
        return weirdnessDensity.sample(new Context(blockX, blockY, blockZ));
    }

    /**
     * Sample climate parameters at quarter position (already divided by 4).
     * Use this when you already have quarter coordinates.
     */
    public double sampleTemperature(int quartX, int quartY, int quartZ) {
        return temperatureDensity.sample(new Context(quartX, quartY, quartZ));
    }
    
    public double sampleHumidity(int quartX, int quartY, int quartZ) {
        return humidityDensity.sample(new Context(quartX, quartY, quartZ));
    }
    
    public double sampleContinentalness(int quartX, int quartY, int quartZ) {
        return continentalnessDensity.sample(new Context(quartX, quartY, quartZ));
    }
    
    public double sampleErosion(int quartX, int quartY, int quartZ) {
        return erosionDensity.sample(new Context(quartX, quartY, quartZ));
    }
    
    public double sampleWeirdness(int quartX, int quartY, int quartZ) {
        return weirdnessDensity.sample(new Context(quartX, quartY, quartZ));
    }

    private DensityNode parseNode(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return new ConstantNode(0.0);
        }
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                return new ConstantNode(element.getAsDouble());
            }
            if (element.getAsJsonPrimitive().isString()) {
                return resolveReference(element.getAsString());
            }
        }

        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("type") && obj.has("coordinate") && obj.has("points")) {
            return parseSpline(obj);
        }
        String typeId = obj.has("type") ? stripNamespace(obj.get("type").getAsString()) : "constant";

        return switch (typeId) {
            case "noise" -> parseNoise(obj);
            case "old_blended_noise" -> parseOldBlendedNoise(obj);
            case "shifted_noise" -> new ShiftedNoiseNode(
                    parseNode(obj.get("shift_x")),
                    parseNode(obj.get("shift_y")),
                    parseNode(obj.get("shift_z")),
                    obj.get("xz_scale").getAsDouble(),
                    obj.get("y_scale").getAsDouble(),
                    getOrCreateNoise(stripNamespace(obj.get("noise").getAsString())));
            case "shift_a" -> new ShiftANode(getOrCreateNoise(stripNamespace(obj.get("argument").getAsString())));
            case "shift_b" -> new ShiftBNode(getOrCreateNoise(stripNamespace(obj.get("argument").getAsString())));
            case "add" -> new AddNode(parseNode(obj.get("argument1")), parseNode(obj.get("argument2")));
            case "mul" -> new MulNode(parseNode(obj.get("argument1")), parseNode(obj.get("argument2")));
            case "min" -> new MinNode(parseNode(obj.get("argument1")), parseNode(obj.get("argument2")));
            case "max" -> new MaxNode(parseNode(obj.get("argument1")), parseNode(obj.get("argument2")));
            case "abs" -> new UnaryNode(parseNode(obj.get("argument")), UnaryType.ABS);
            case "square" -> new UnaryNode(parseNode(obj.get("argument")), UnaryType.SQUARE);
            case "cube" -> new UnaryNode(parseNode(obj.get("argument")), UnaryType.CUBE);
            case "half_negative" -> new UnaryNode(parseNode(obj.get("argument")), UnaryType.HALF_NEGATIVE);
            case "quarter_negative" -> new UnaryNode(parseNode(obj.get("argument")), UnaryType.QUARTER_NEGATIVE);
            case "squeeze" -> new UnaryNode(parseNode(obj.get("argument")), UnaryType.SQUEEZE);
            case "clamp" -> new ClampNode(parseNode(obj.get("input")), obj.get("min").getAsDouble(), obj.get("max").getAsDouble());
            case "range_choice" -> new RangeChoiceNode(
                    parseNode(obj.get("input")),
                    obj.get("min_inclusive").getAsDouble(),
                    obj.get("max_exclusive").getAsDouble(),
                    parseNode(obj.get("when_in_range")),
                    parseNode(obj.get("when_out_of_range")));
            case "y_clamped_gradient" -> new YGradientNode(
                    obj.get("from_y").getAsDouble(), obj.get("to_y").getAsDouble(),
                    obj.get("from_value").getAsDouble(), obj.get("to_value").getAsDouble());
            case "vertical_gradient" -> new YGradientNode(
                    obj.getAsJsonObject("from_y").get("absolute").getAsDouble(),
                    obj.getAsJsonObject("to_y").get("absolute").getAsDouble(),
                    obj.get("from_value").getAsDouble(),
                    obj.get("to_value").getAsDouble());
            case "interpolated", "cache_2d", "cache_once", "flat_cache", "blend_density" -> parseNode(obj.get("argument"));
            case "blend_alpha" -> new ConstantNode(1.0);
            case "blend_offset" -> new ConstantNode(0.0);
            case "spline" -> parseSpline(obj.getAsJsonObject("spline"));
            case "weird_scaled_sampler" -> new WeirdScaledNode(
                    parseNode(obj.get("input")),
                    getOrCreateNoise(stripNamespace(obj.get("noise").getAsString())),
                    obj.get("rarity_value_mapper").getAsString());
            default -> {
                Logger.ERROR("Unsupported density function type: %s", typeId);
                yield new ConstantNode(0.0);
            }
        };
    }

    private DensityNode parseNoise(JsonObject obj) {
        String noiseName = stripNamespace(obj.get("noise").getAsString());
        Noise noise = getOrCreateNoise(noiseName);
        double xzScale = obj.has("xz_scale") ? obj.get("xz_scale").getAsDouble() : 1.0;
        double yScale = obj.has("y_scale") ? obj.get("y_scale").getAsDouble() : 1.0;
        return new NoiseNode(noise, xzScale, yScale);
    }

    private DensityNode parseOldBlendedNoise(JsonObject obj) {
        double xzScale = obj.get("xz_scale").getAsDouble();
        double yScale = obj.get("y_scale").getAsDouble();
        double xzFactor = obj.get("xz_factor").getAsDouble();
        double yFactor = obj.get("y_factor").getAsDouble();
        double smear = obj.get("smear_scale_multiplier").getAsDouble();
        return new OldBlendedNoiseNode(seed, xzScale, yScale, xzFactor, yFactor, smear);
    }

    private DensityNode parseSpline(JsonObject splineObj) {
        DensityNode coordinate = parseNode(splineObj.get("coordinate"));
        JsonArray points = splineObj.getAsJsonArray("points");

        float[] locations = new float[points.size()];
        float[] derivatives = new float[points.size()];
        List<DensityNode> values = new ArrayList<>(points.size());

        for (int i = 0; i < points.size(); i++) {
            JsonObject point = points.get(i).getAsJsonObject();
            locations[i] = point.get("location").getAsFloat();
            derivatives[i] = point.get("derivative").getAsFloat();
            values.add(parseNode(point.get("value")));
        }

        return new SplineNode(coordinate, locations, values, derivatives);
    }

    private DensityNode resolveReference(String id) {
        String path = stripNamespace(id);

        if (path.equals("y")) {
            return new YNode();
        }
        if (path.equals("zero")) {
            return new ConstantNode(0.0);
        }

        DensityNode cached = densityById.get(path);
        if (cached != null) {
            return cached;
        }

        FileHandle file = Gdx.files.internal("data/minecraft/worldgen/density_function/" + path + ".json");
        if (!file.exists()) {
            Logger.ERROR("Missing density function reference: %s", path);
            DensityNode fallback = new ConstantNode(0.0);
            densityById.put(path, fallback);
            return fallback;
        }

        JsonObject obj = JsonParser.parseString(file.readString()).getAsJsonObject();
        DensityNode parsed = parseNode(obj);
        densityById.put(path, parsed);
        return parsed;
    }

    private Noise getOrCreateNoise(String name) {
        Noise cached = noiseById.get(name);
        if (cached != null) {
            return cached;
        }

        Noise created = config.createNoise(name, new NormalNoise.NoiseParameters(0, new double[] {1.0}));
        noiseById.put(name, created);
        return created;
    }

    private static String stripNamespace(String id) {
        int idx = id.indexOf(':');
        return idx >= 0 ? id.substring(idx + 1) : id;
    }

    private static JsonObject readJson(String path) {
        return JsonParser.parseString(Gdx.files.internal(path).readString()).getAsJsonObject();
    }

    private record Context(int x, int y, int z) {
    }

    private interface DensityNode {
        double sample(Context context);
    }

    private record ConstantNode(double value) implements DensityNode {
        @Override
        public double sample(Context context) {
            return value;
        }
    }

    private static final class YNode implements DensityNode {
        @Override
        public double sample(Context context) {
            return context.y;
        }
    }

    private record AddNode(DensityNode a, DensityNode b) implements DensityNode {
        @Override
        public double sample(Context context) {
            return a.sample(context) + b.sample(context);
        }
    }

    private record MulNode(DensityNode a, DensityNode b) implements DensityNode {
        @Override
        public double sample(Context context) {
            return a.sample(context) * b.sample(context);
        }
    }

    private record MinNode(DensityNode a, DensityNode b) implements DensityNode {
        @Override
        public double sample(Context context) {
            return Math.min(a.sample(context), b.sample(context));
        }
    }

    private record MaxNode(DensityNode a, DensityNode b) implements DensityNode {
        @Override
        public double sample(Context context) {
            return Math.max(a.sample(context), b.sample(context));
        }
    }

    private enum UnaryType {
        ABS,
        SQUARE,
        CUBE,
        HALF_NEGATIVE,
        QUARTER_NEGATIVE,
        SQUEEZE
    }

    private record UnaryNode(DensityNode node, UnaryType type) implements DensityNode {
        @Override
        public double sample(Context context) {
            double v = node.sample(context);
            return switch (type) {
                case ABS -> Math.abs(v);
                case SQUARE -> v * v;
                case CUBE -> v * v * v;
                case HALF_NEGATIVE -> v > 0.0 ? v : v * 0.5;
                case QUARTER_NEGATIVE -> v > 0.0 ? v : v * 0.25;
                case SQUEEZE -> {
                    double c = clamp(v, -1.0, 1.0);
                    yield c / 2.0 - c * c * c / 24.0;
                }
            };
        }
    }

    private record ClampNode(DensityNode node, double min, double max) implements DensityNode {
        @Override
        public double sample(Context context) {
            return clamp(node.sample(context), min, max);
        }
    }

    private record RangeChoiceNode(DensityNode input, double minInclusive, double maxExclusive, DensityNode inRange,
                                   DensityNode outOfRange) implements DensityNode {
        @Override
        public double sample(Context context) {
            double v = input.sample(context);
            if (v >= minInclusive && v < maxExclusive) {
                return inRange.sample(context);
            }
            return outOfRange.sample(context);
        }
    }

    private record YGradientNode(double fromY, double toY, double fromValue, double toValue) implements DensityNode {
        @Override
        public double sample(Context context) {
            if (context.y <= fromY) {
                return fromValue;
            }
            if (context.y >= toY) {
                return toValue;
            }
            double t = (context.y - fromY) / (toY - fromY);
            return lerp(t, fromValue, toValue);
        }
    }

    private record NoiseNode(Noise noise, double xzScale, double yScale) implements DensityNode {
        @Override
        public double sample(Context context) {
            return noise.sample(context.x * xzScale, context.y * yScale, context.z * xzScale);
        }
    }

    private record ShiftANode(Noise offsetNoise) implements DensityNode {
        @Override
        public double sample(Context context) {
            return offsetNoise.sample(context.x * 0.25, 0.0, context.z * 0.25) * 4.0;
        }
    }

    private record ShiftBNode(Noise offsetNoise) implements DensityNode {
        @Override
        public double sample(Context context) {
            return offsetNoise.sample(context.z * 0.25, context.x * 0.25, 0.0) * 4.0;
        }
    }

    private record ShiftedNoiseNode(DensityNode shiftX, DensityNode shiftY, DensityNode shiftZ, double xzScale,
                                    double yScale, Noise noise) implements DensityNode {
        @Override
        public double sample(Context context) {
            double x = context.x * xzScale + shiftX.sample(context);
            double y = context.y * yScale + shiftY.sample(context);
            double z = context.z * xzScale + shiftZ.sample(context);
            return noise.sample(x, y, z);
        }
    }

    private record WeirdScaledNode(DensityNode input, Noise noise, String mapper) implements DensityNode {
        @Override
        public double sample(Context context) {
            double v = input.sample(context);
            double rarity = mapper.equals("type_1") ? rarity3D(v) : rarity2D(v);
            return rarity * Math.abs(noise.sample(context.x / rarity, context.y / rarity, context.z / rarity));
        }
    }

    private static final class SplineNode implements DensityNode {
        private final DensityNode coordinate;
        private final float[] locations;
        private final List<DensityNode> values;
        private final float[] derivatives;

        private SplineNode(DensityNode coordinate, float[] locations, List<DensityNode> values, float[] derivatives) {
            this.coordinate = coordinate;
            this.locations = locations;
            this.values = values;
            this.derivatives = derivatives;
        }

        @Override
        public double sample(Context context) {
            float input = (float) coordinate.sample(context);
            int start = findIntervalStart(locations, input);

            if (start < 0) {
                return linearExtend(input, values.get(0).sample(context), derivatives[0], locations[0]);
            }

            int last = locations.length - 1;
            if (start == last) {
                return linearExtend(input, values.get(last).sample(context), derivatives[last], locations[last]);
            }

            float l = locations[start];
            float h = locations[start + 1];
            float t = (input - l) / (h - l);

            double y1 = values.get(start).sample(context);
            double y2 = values.get(start + 1).sample(context);
            float d1 = derivatives[start];
            float d2 = derivatives[start + 1];

            double slope = h - l;
            double a = d1 * slope - (y2 - y1);
            double b = -d2 * slope + (y2 - y1);

            return lerp(t, y1, y2) + t * (1.0 - t) * lerp(t, a, b);
        }

        private static double linearExtend(float input, double value, float derivative, float location) {
            if (derivative == 0.0f) {
                return value;
            }
            return value + derivative * (input - location);
        }

        private static int findIntervalStart(float[] locations, float input) {
            int idx = Arrays.binarySearch(locations, input);
            return idx < 0 ? -idx - 2 : idx;
        }
    }

    private static final class OldBlendedNoiseNode implements DensityNode {
        private final PerlinNoise minLimitNoise;
        private final PerlinNoise maxLimitNoise;
        private final PerlinNoise mainNoise;
        private final double xzMultiplier;
        private final double yMultiplier;
        private final double xzFactor;
        private final double yFactor;
        private final double smearScaleMultiplier;

        private OldBlendedNoiseNode(long seed, double xzScale, double yScale, double xzFactor, double yFactor,
                                    double smearScaleMultiplier) {
            Xoroshiro random = new Xoroshiro(seed).fromHashOf("minecraft:blended_noise");
            this.minLimitNoise = new PerlinNoise(random, -15, ones(16), true);
            this.maxLimitNoise = new PerlinNoise(random, -15, ones(16), true);
            this.mainNoise = new PerlinNoise(random, -7, ones(8), true);

            this.xzMultiplier = 684.412 * xzScale;
            this.yMultiplier = 684.412 * yScale;
            this.xzFactor = xzFactor;
            this.yFactor = yFactor;
            this.smearScaleMultiplier = smearScaleMultiplier;
        }

        @Override
        public double sample(Context context) {
            double limitX = context.x * xzMultiplier;
            double limitY = context.y * yMultiplier;
            double limitZ = context.z * xzMultiplier;

            double mainX = limitX / xzFactor;
            double mainY = limitY / yFactor;
            double mainZ = limitZ / xzFactor;

            double limitSmear = yMultiplier * smearScaleMultiplier;
            double mainSmear = limitSmear / yFactor;

            double blendMin = 0.0;
            double blendMax = 0.0;
            double mainNoiseValue = 0.0;

            double pow = 1.0;
            for (int i = 0; i < 8; i++) {
                ImprovedNoise noise = mainNoise.getOctaveNoise(i);
                if (noise != null) {
                    mainNoiseValue += noise.sample(
                            PerlinNoise.wrap(mainX * pow),
                            PerlinNoise.wrap(mainY * pow),
                            PerlinNoise.wrap(mainZ * pow),
                            mainSmear * pow,
                            mainY * pow) / pow;
                }
                pow /= 2.0;
            }

            double factor = (mainNoiseValue / 10.0 + 1.0) / 2.0;
            boolean isMax = factor >= 1.0;
            boolean isMin = factor <= 0.0;

            pow = 1.0;
            for (int i = 0; i < 16; i++) {
                double wx = PerlinNoise.wrap(limitX * pow);
                double wy = PerlinNoise.wrap(limitY * pow);
                double wz = PerlinNoise.wrap(limitZ * pow);
                double yScalePow = limitSmear * pow;

                if (!isMax) {
                    ImprovedNoise minNoise = minLimitNoise.getOctaveNoise(i);
                    if (minNoise != null) {
                        blendMin += minNoise.sample(wx, wy, wz, yScalePow, limitY * pow) / pow;
                    }
                }
                if (!isMin) {
                    ImprovedNoise maxNoise = maxLimitNoise.getOctaveNoise(i);
                    if (maxNoise != null) {
                        blendMax += maxNoise.sample(wx, wy, wz, yScalePow, limitY * pow) / pow;
                    }
                }
                pow /= 2.0;
            }

            return clampedLerp(factor, blendMin / 512.0, blendMax / 512.0) / 128.0;
        }

        private static double[] ones(int count) {
            double[] values = new double[count];
            Arrays.fill(values, 1.0);
            return values;
        }
    }

    private static double rarity2D(double factor) {
        if (factor < -0.75) return 0.5;
        if (factor < -0.5) return 0.75;
        if (factor < 0.5) return 1.0;
        if (factor < 0.75) return 2.0;
        return 3.0;
    }

    private static double rarity3D(double factor) {
        if (factor < -0.5) return 0.75;
        if (factor < 0.0) return 1.0;
        if (factor < 0.5) return 1.5;
        return 2.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double clampedLerp(double t, double a, double b) {
        if (t < 0.0) return a;
        if (t > 1.0) return b;
        return lerp(t, a, b);
    }

    /**
     * Estimate the surface Y level at a given world X coordinate.
     * Finds where the density transitions from positive to negative.
     */
    public int estimateSurfaceY(int worldX) {
        for (int y = SharedConstants.CHUNK_HEIGHT - 2; y >= 1; y--) {
            int vanillaY = config.getMinY() + y;
            double here = sampleFinalDensity2D(worldX, vanillaY);
            double above = sampleFinalDensity2D(worldX, vanillaY + 1);
            if (here > 0.0 && above <= 0.0) {
                return y;
            }
        }
        return config.getSeaLevelInternal();
    }
}
