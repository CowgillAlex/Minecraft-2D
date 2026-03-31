package dev.alexco.minecraft.world.level.levelgen.vanilla;

import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.levelgen.noise.Noise;
import dev.alexco.registry.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Faithful port of Minecraft's SurfaceRules system, adapted for 2D world generation.
 *
 * In the official Minecraft implementation (net.minecraft.world.level.levelgen.SurfaceRules),
 * surface rules are applied per-column (x,z) iterating from top to bottom (y).
 *
 * For this 2D project, we treat Z as always 0, effectively creating a slice through the world.
 */
public class SurfaceRules {

    /**
     * Context holds all the state needed during surface rule evaluation.
     * Mirrors Minecraft's SurfaceRules.Context exactly.
     */
    public static class Context {
        private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;

        // Dependencies
        private final VanillaNoiseStep noiseStep;
        private final int seaLevel;
        private final Function<Integer, Integer> surfaceDepthGetter;
        private final Function<Integer, Double> surfaceSecondaryGetter;
        private final Function<Integer, Biome> biomeGetter;
        private final VanillaWorldgenConfig worldgen;

        // Current position state
        private int blockX;
        private int blockY;

        // Cached per-XZ values (updated when X changes)
        private int surfaceDepth;
        private double surfaceSecondary;
        private int minSurfaceLevel;
        private int preliminarySurfaceY;

        // Cached per-Y values (updated when Y changes)
        public Biome biome;
        private int waterHeight;
        private int stoneDepthBelow;
        private int stoneDepthAbove;

        // Lazy condition instances
        private final Condition temperature = new TemperatureHelperCondition(this);
        private final Condition steep = new SteepMaterialCondition(this);
        private final Condition hole = new HoleCondition(this);
        private final Condition abovePreliminarySurface;

        // Update tracking for lazy evaluation
        private long lastUpdateXZ = Long.MIN_VALUE;
        private long lastUpdateY = Long.MIN_VALUE;

        public Context(VanillaNoiseStep noiseStep, int seaLevel,
                      Function<Integer, Integer> surfaceDepthGetter,
                      Function<Integer, Double> surfaceSecondaryGetter,
                      Function<Integer, Biome> biomeGetter,
                      VanillaWorldgenConfig worldgen) {
            this.noiseStep = noiseStep;
            this.seaLevel = seaLevel;
            this.surfaceDepthGetter = surfaceDepthGetter;
            this.surfaceSecondaryGetter = surfaceSecondaryGetter;
            this.biomeGetter = biomeGetter;
            this.worldgen = worldgen;
            this.abovePreliminarySurface = new AbovePreliminarySurfaceCondition(this);
        }

        /**
         * Update context for a new XZ position (called once per column before iterating Y)
         */
        protected void updateXZ(int blockX) {
            this.lastUpdateXZ++;
            this.blockX = blockX;

            // Calculate surface depth from noise
            this.surfaceDepth = surfaceDepthGetter.apply(blockX);
            this.surfaceSecondary = surfaceSecondaryGetter.apply(blockX);

            // Calculate preliminary surface level (simplified for 2D)
            this.preliminarySurfaceY = noiseStep.estimateSurfaceY(blockX);
            this.minSurfaceLevel = preliminarySurfaceY + surfaceDepth - HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE;
        }

        /**
         * Update context for a new Y position (called for each block in the column)
         */
        protected void updateY(int stoneDepthAbove, int stoneDepthBelow, int waterHeight, int blockY) {
            this.lastUpdateY++;
            this.blockY = blockY;
            this.stoneDepthAbove = stoneDepthAbove;
            this.stoneDepthBelow = stoneDepthBelow;
            this.waterHeight = waterHeight;
            this.biome = biomeGetter.apply(blockX);
        }

        public int getSeaLevel() {
            return seaLevel;
        }

        public int getMinSurfaceLevel() {
            return minSurfaceLevel;
        }

        public double getSurfaceSecondary() {
            return surfaceSecondary;
        }
    }

    // ========================================================================
    // LAZY CONDITION BASE CLASSES
    // ========================================================================

    /**
     * Base class for conditions that cache their result.
     * The condition is re-evaluated only when the context's update counter changes.
     */
    private static abstract class LazyCondition implements Condition {
        protected final Context context;
        private long lastUpdate;
        private Boolean result;

        protected LazyCondition(Context context) {
            this.context = context;
            this.lastUpdate = getContextLastUpdate() - 1;
        }

        @Override
        public boolean test() {
            long currentUpdate = getContextLastUpdate();
            if (currentUpdate == lastUpdate) {
                if (result == null) {
                    throw new IllegalStateException("Update triggered but result is null");
                }
                return result;
            }
            lastUpdate = currentUpdate;
            result = compute();
            return result;
        }

        protected abstract long getContextLastUpdate();
        protected abstract boolean compute();
    }

    private static abstract class LazyXZCondition extends LazyCondition {
        protected LazyXZCondition(Context context) {
            super(context);
        }

        @Override
        protected long getContextLastUpdate() {
            return context.lastUpdateXZ;
        }
    }

    private static abstract class LazyYCondition extends LazyCondition {
        protected LazyYCondition(Context context) {
            super(context);
        }

        @Override
        protected long getContextLastUpdate() {
            return context.lastUpdateY;
        }
    }

    // ========================================================================
    // BUILT-IN CONDITIONS
    // ========================================================================

    private static class HoleCondition extends LazyXZCondition {
        private HoleCondition(Context context) {
            super(context);
        }

        @Override
        protected boolean compute() {
            return context.surfaceDepth <= 0;
        }
    }

    private static class AbovePreliminarySurfaceCondition implements Condition {
        private final Context context;

        private AbovePreliminarySurfaceCondition(Context context) {
            this.context = context;
        }

        @Override
        public boolean test() {
            return context.blockY >= context.minSurfaceLevel;
        }
    }

    private static class TemperatureHelperCondition extends LazyYCondition {
        private TemperatureHelperCondition(Context context) {
            super(context);
        }

        @Override
        protected boolean compute() {
            if (context.biome == null) {
                return false;
            }
            String biomeName = context.biome.getName().toString();
            return biomeName.contains("snowy") || biomeName.contains("frozen") || biomeName.contains("ice");
        }
    }

    private static class SteepMaterialCondition extends LazyXZCondition {
        private SteepMaterialCondition(Context context) {
            super(context);
        }

        @Override
        protected boolean compute() {
            // For 2D: check if adjacent columns have a height difference of 4+ blocks
            int leftY = estimateSurface(context.blockX - 1);
            int rightY = estimateSurface(context.blockX + 1);
            return Math.abs(leftY - rightY) >= 4;
        }

        private int estimateSurface(int worldX) {
            return context.noiseStep.estimateSurfaceY(worldX);
        }
    }

    // ========================================================================
    // CONDITION TYPES
    // ========================================================================

    /**
     * Condition that negates another condition
     */
    private static class NotCondition implements Condition {
        private final Condition target;

        private NotCondition(Condition target) {
            this.target = target;
        }

        @Override
        public boolean test() {
            return !target.test();
        }
    }

    /**
     * Condition for stone depth checks (ON_FLOOR, UNDER_FLOOR, ON_CEILING, etc.)
     */
    private static class StoneDepthCondition extends LazyYCondition {
        private final int offset;
        private final boolean addSurfaceDepth;
        private final int secondaryDepthRange;
        private final CaveSurface surfaceType;

        private StoneDepthCondition(Context context, int offset, boolean addSurfaceDepth,
                                   int secondaryDepthRange, CaveSurface surfaceType) {
            super(context);
            this.offset = offset;
            this.addSurfaceDepth = addSurfaceDepth;
            this.secondaryDepthRange = secondaryDepthRange;
            this.surfaceType = surfaceType;
        }

        @Override
        protected boolean compute() {
            int stoneDepth = surfaceType == CaveSurface.CEILING ? context.stoneDepthBelow : context.stoneDepthAbove;
            int surfaceDepth = addSurfaceDepth ? context.surfaceDepth : 0;
            int secondaryDepth = secondaryDepthRange == 0 ? 0 :
                (int) map(context.getSurfaceSecondary(), -1.0, 1.0, 0.0, secondaryDepthRange);

            return stoneDepth <= 1 + offset + surfaceDepth + secondaryDepth;
        }
    }

    /**
     * Condition for Y level checks
     */
    private static class YCondition extends LazyYCondition {
        private final int anchorY;
        private final int surfaceDepthMultiplier;
        private final boolean addStoneDepth;

        private YCondition(Context context, int anchorY, int surfaceDepthMultiplier, boolean addStoneDepth) {
            super(context);
            this.anchorY = anchorY;
            this.surfaceDepthMultiplier = surfaceDepthMultiplier;
            this.addStoneDepth = addStoneDepth;
        }

        @Override
        protected boolean compute() {
            int y = context.blockY + (addStoneDepth ? context.stoneDepthAbove : 0);
            return y >= anchorY + context.surfaceDepth * surfaceDepthMultiplier;
        }
    }

    /**
     * Condition for water level checks
     */
    private static class WaterCondition extends LazyYCondition {
        private final int offset;
        private final int surfaceDepthMultiplier;
        private final boolean addStoneDepth;

        private WaterCondition(Context context, int offset, int surfaceDepthMultiplier, boolean addStoneDepth) {
            super(context);
            this.offset = offset;
            this.surfaceDepthMultiplier = surfaceDepthMultiplier;
            this.addStoneDepth = addStoneDepth;
        }

        @Override
        protected boolean compute() {
            if (context.waterHeight == Integer.MIN_VALUE) {
                // No water detected - for offset 0, this means we're above water
                // For negative offset, we might be checking "not underwater"
                if (offset <= 0) {
                    // When offset is 0 or negative and no water, we're effectively "above" it
                    return true;
                }
                return false;
            }
            int y = context.blockY + (addStoneDepth ? context.stoneDepthAbove : 0);
            boolean result = y >= context.waterHeight + offset + context.surfaceDepth * surfaceDepthMultiplier;
            // Debug log
            if (context.blockX % 16 == 0 && context.blockY % 20 == 0) {
                System.out.println("[WaterCondition] y=" + y + " waterHeight=" + context.waterHeight + " offset=" + offset + " result=" + result);
            }
            return result;
        }
    }

    /**
     * Condition that checks if current biome is in a list
     */
    private static class BiomeCondition extends LazyYCondition {
        private final List<String> biomes;

        private BiomeCondition(Context context, List<String> biomes) {
            super(context);
            this.biomes = biomes;
        }

        @Override
        protected boolean compute() {
            if (context.biome == null) {
                return false;
            }
            String key = context.biome.getName().toString();
            // Debug: Log first few biome checks to see what's happening
            // if (context.blockX % 16 == 0 && context.blockY % 20 == 0) {
            //     System.out.println("[BiomeCondition] Checking biome '" + key + "' against: " + biomes);
            // }
            for (String biomeName : biomes) {
                String stripped = stripNamespace(biomeName);
                boolean matches = key.equals(biomeName) || key.equals(stripped) || key.endsWith(":" + stripped);
                if (matches) {
                    if (context.blockX % 16 == 0 && context.blockY % 20 == 0) {
                        System.out.println("[BiomeCondition] MATCH: '" + key + "' matches '" + biomeName + "' (stripped: '" + stripped + "')");
                    }
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Condition that checks noise value against thresholds
     */
    private static class NoiseThresholdCondition extends LazyXZCondition {
        private final Noise noise;
        private final double minThreshold;
        private final double maxThreshold;

        private NoiseThresholdCondition(Context context, Noise noise, double minThreshold, double maxThreshold) {
            super(context);
            this.noise = noise;
            this.minThreshold = minThreshold;
            this.maxThreshold = maxThreshold;
        }

        @Override
        protected boolean compute() {
            double value = noise.sample(context.blockX, 0.0, 0.0);
            return value >= minThreshold && value <= maxThreshold;
        }
    }

    /**
     * Condition for vertical gradient (used for bedrock)
     */
    private static class VerticalGradientCondition extends LazyYCondition {
        private final int trueAtAndBelow;
        private final int falseAtAndAbove;
        private final String randomName;
        private final long seed;

        private VerticalGradientCondition(Context context, int trueAtAndBelow, int falseAtAndAbove,
                                         String randomName, long seed) {
            super(context);
            this.trueAtAndBelow = trueAtAndBelow;
            this.falseAtAndAbove = falseAtAndAbove;
            this.randomName = randomName;
            this.seed = seed;
        }

        @Override
        protected boolean compute() {
            if (context.blockY <= trueAtAndBelow) {
                return true;
            }
            if (context.blockY >= falseAtAndAbove) {
                return false;
            }

            double t = (context.blockY - trueAtAndBelow) / (double) (falseAtAndAbove - trueAtAndBelow);
            double chance = 1.0 - t;
            return randomAt(randomName, context.blockX, context.blockY, seed) < chance;
        }
    }

    // ========================================================================
    // RULE TYPES
    // ========================================================================

    /**
     * Interface for surface rules that can provide a block state.
     * Returns null if this rule doesn't apply.
     */
    public interface SurfaceRule {
        BlockState tryApply(int blockX, int blockY, int blockZ);
    }

    /**
     * Interface for conditions that test the current context.
     */
    public interface Condition {
        boolean test();
    }

    /**
     * Simple rule that returns a fixed block state
     */
    private static class StateRule implements SurfaceRule {
        private final BlockState state;

        private StateRule(BlockState state) {
            this.state = state;
        }

        @Override
        public BlockState tryApply(int blockX, int blockY, int blockZ) {
            return state;
        }
    }

    /**
     * Rule that applies another rule only if a condition passes
     */
    private static class TestRule implements SurfaceRule {
        private final Condition condition;
        private final SurfaceRule followup;

        private TestRule(Condition condition, SurfaceRule followup) {
            this.condition = condition;
            this.followup = followup;
        }

        @Override
        public BlockState tryApply(int blockX, int blockY, int blockZ) {
            if (!condition.test()) {
                return null;
            }
            return followup.tryApply(blockX, blockY, blockZ);
        }
    }

    /**
     * Rule that tries multiple rules in sequence, returning the first non-null result
     */
    private static class SequenceRule implements SurfaceRule {
        private final List<SurfaceRule> rules;

        private SequenceRule(List<SurfaceRule> rules) {
            this.rules = rules;
        }

        @Override
        public BlockState tryApply(int blockX, int blockY, int blockZ) {
            for (SurfaceRule rule : rules) {
                BlockState state = rule.tryApply(blockX, blockY, blockZ);
                if (state != null) {
                    return state;
                }
            }
            return null;
        }
    }

    /**
     * Rule that returns terracotta bands for badlands biomes
     */
    private static class BandlandsRule implements SurfaceRule {
        private final SurfaceSystem system;

        private BandlandsRule(SurfaceSystem system) {
            this.system = system;
        }

        @Override
        public BlockState tryApply(int blockX, int blockY, int blockZ) {
            return system.getBand(blockX, blockY);
        }
    }

    // ========================================================================
    // RULE SOURCE INTERFACES
    // ========================================================================

    /**
     * Interface for sources that create SurfaceRules when given a context.
     * This is the JSON-parseable representation of a rule.
     */
    public interface RuleSource {
        SurfaceRule apply(Context context);
    }

    /**
     * Interface for sources that create Conditions when given a context.
     * This is the JSON-parseable representation of a condition.
     */
    public interface ConditionSource {
        Condition apply(Context context);
    }

    // ========================================================================
    // RULE SOURCE IMPLEMENTATIONS
    // ========================================================================

    private static class BlockRuleSource implements RuleSource {
        private final BlockState state;

        private BlockRuleSource(BlockState state) {
            this.state = state;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return new StateRule(state);
        }
    }

    private static class TestRuleSource implements RuleSource {
        private final ConditionSource ifTrue;
        private final RuleSource thenRun;

        private TestRuleSource(ConditionSource ifTrue, RuleSource thenRun) {
            this.ifTrue = ifTrue;
            this.thenRun = thenRun;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return new TestRule(ifTrue.apply(context), thenRun.apply(context));
        }
    }

    private static class SequenceRuleSource implements RuleSource {
        private final List<RuleSource> sequence;

        private SequenceRuleSource(List<RuleSource> sequence) {
            this.sequence = sequence;
        }

        @Override
        public SurfaceRule apply(Context context) {
            if (sequence.size() == 1) {
                return sequence.get(0).apply(context);
            }

            List<SurfaceRule> rules = new ArrayList<>();
            for (RuleSource source : sequence) {
                rules.add(source.apply(context));
            }
            return new SequenceRule(rules);
        }
    }

    private static class BandlandsRuleSource implements RuleSource {
        private final SurfaceSystem system;

        private BandlandsRuleSource(SurfaceSystem system) {
            this.system = system;
        }

        @Override
        public SurfaceRule apply(Context context) {
            return new BandlandsRule(system);
        }
    }

    // ========================================================================
    // CONDITION SOURCE IMPLEMENTATIONS
    // ========================================================================

    private static class NotConditionSource implements ConditionSource {
        private final ConditionSource target;

        private NotConditionSource(ConditionSource target) {
            this.target = target;
        }

        @Override
        public Condition apply(Context context) {
            return new NotCondition(target.apply(context));
        }
    }

    private static class StoneDepthCheckSource implements ConditionSource {
        private final int offset;
        private final boolean addSurfaceDepth;
        private final int secondaryDepthRange;
        private final CaveSurface surfaceType;

        private StoneDepthCheckSource(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType) {
            this.offset = offset;
            this.addSurfaceDepth = addSurfaceDepth;
            this.secondaryDepthRange = secondaryDepthRange;
            this.surfaceType = surfaceType;
        }

        @Override
        public Condition apply(Context context) {
            return new StoneDepthCondition(context, offset, addSurfaceDepth, secondaryDepthRange, surfaceType);
        }
    }

    private static class YConditionSource implements ConditionSource {
        private final int anchorY;
        private final int surfaceDepthMultiplier;
        private final boolean addStoneDepth;

        private YConditionSource(int anchorY, int surfaceDepthMultiplier, boolean addStoneDepth) {
            this.anchorY = anchorY;
            this.surfaceDepthMultiplier = surfaceDepthMultiplier;
            this.addStoneDepth = addStoneDepth;
        }

        @Override
        public Condition apply(Context context) {
            return new YCondition(context, anchorY, surfaceDepthMultiplier, addStoneDepth);
        }
    }

    private static class WaterConditionSource implements ConditionSource {
        private final int offset;
        private final int surfaceDepthMultiplier;
        private final boolean addStoneDepth;

        private WaterConditionSource(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) {
            this.offset = offset;
            this.surfaceDepthMultiplier = surfaceDepthMultiplier;
            this.addStoneDepth = addStoneDepth;
        }

        @Override
        public Condition apply(Context context) {
            return new WaterCondition(context, offset, surfaceDepthMultiplier, addStoneDepth);
        }
    }

    private static class BiomeConditionSource implements ConditionSource {
        private final List<String> biomes;

        private BiomeConditionSource(List<String> biomes) {
            this.biomes = biomes;
        }

        @Override
        public Condition apply(Context context) {
            return new BiomeCondition(context, biomes);
        }
    }

    private static class NoiseThresholdConditionSource implements ConditionSource {
        private final Noise noise;
        private final double minThreshold;
        private final double maxThreshold;

        private NoiseThresholdConditionSource(Noise noise, double minThreshold, double maxThreshold) {
            this.noise = noise;
            this.minThreshold = minThreshold;
            this.maxThreshold = maxThreshold;
        }

        @Override
        public Condition apply(Context context) {
            return new NoiseThresholdCondition(context, noise, minThreshold, maxThreshold);
        }
    }

    private static class VerticalGradientConditionSource implements ConditionSource {
        private final int trueAtAndBelow;
        private final int falseAtAndAbove;
        private final String randomName;
        private final long seed;

        private VerticalGradientConditionSource(int trueAtAndBelow, int falseAtAndAbove, String randomName, long seed) {
            this.trueAtAndBelow = trueAtAndBelow;
            this.falseAtAndAbove = falseAtAndAbove;
            this.randomName = randomName;
            this.seed = seed;
        }

        @Override
        public Condition apply(Context context) {
            return new VerticalGradientCondition(context, trueAtAndBelow, falseAtAndAbove, randomName, seed);
        }
    }

    private static class SteepConditionSource implements ConditionSource {
        @Override
        public Condition apply(Context context) {
            return context.steep;
        }
    }

    private static class HoleConditionSource implements ConditionSource {
        @Override
        public Condition apply(Context context) {
            return context.hole;
        }
    }

    private static class TemperatureConditionSource implements ConditionSource {
        @Override
        public Condition apply(Context context) {
            return context.temperature;
        }
    }

    private static class AbovePreliminarySurfaceConditionSource implements ConditionSource {
        @Override
        public Condition apply(Context context) {
            return context.abovePreliminarySurface;
        }
    }

    // ========================================================================
    // STATIC FACTORY METHODS (mirroring Minecraft's SurfaceRules class)
    // ========================================================================

    public static final ConditionSource ON_FLOOR = stoneDepthCheck(0, false, 0, CaveSurface.FLOOR);
    public static final ConditionSource UNDER_FLOOR = stoneDepthCheck(0, true, 0, CaveSurface.FLOOR);
    public static final ConditionSource DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
    public static final ConditionSource VERY_DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 30, CaveSurface.FLOOR);
    public static final ConditionSource ON_CEILING = stoneDepthCheck(0, false, 0, CaveSurface.CEILING);
    public static final ConditionSource UNDER_CEILING = stoneDepthCheck(0, true, 0, CaveSurface.CEILING);

    public static ConditionSource stoneDepthCheck(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType) {
        return new StoneDepthCheckSource(offset, addSurfaceDepth, secondaryDepthRange, surfaceType);
    }

    public static ConditionSource not(ConditionSource target) {
        return new NotConditionSource(target);
    }

    public static ConditionSource yBlockCheck(int anchorY, int surfaceDepthMultiplier) {
        return yBlockCheck(anchorY, surfaceDepthMultiplier, false);
    }

    public static ConditionSource yBlockCheck(int anchorY, int surfaceDepthMultiplier, boolean addStoneDepth) {
        return new YConditionSource(anchorY, surfaceDepthMultiplier, addStoneDepth);
    }

    public static ConditionSource waterBlockCheck(int offset, int surfaceDepthMultiplier) {
        return waterBlockCheck(offset, surfaceDepthMultiplier, false);
    }

    public static ConditionSource waterBlockCheck(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) {
        return new WaterConditionSource(offset, surfaceDepthMultiplier, addStoneDepth);
    }

    public static ConditionSource isBiome(String... biomes) {
        return new BiomeConditionSource(List.of(biomes));
    }

    public static ConditionSource isBiome(List<String> biomes) {
        return new BiomeConditionSource(biomes);
    }

    public static ConditionSource noiseCondition(Noise noise, double minRange) {
        return noiseCondition(noise, minRange, Double.MAX_VALUE);
    }

    public static ConditionSource noiseCondition(Noise noise, double minRange, double maxRange) {
        return new NoiseThresholdConditionSource(noise, minRange, maxRange);
    }

    public static ConditionSource verticalGradient(String randomName, int trueAtAndBelow, int falseAtAndAbove, long seed) {
        return new VerticalGradientConditionSource(trueAtAndBelow, falseAtAndAbove, randomName, seed);
    }

    public static ConditionSource steep() {
        return new SteepConditionSource();
    }

    public static ConditionSource hole() {
        return new HoleConditionSource();
    }

    public static ConditionSource abovePreliminarySurface() {
        return new AbovePreliminarySurfaceConditionSource();
    }

    public static ConditionSource temperature() {
        return new TemperatureConditionSource();
    }

    public static RuleSource ifTrue(ConditionSource condition, RuleSource next) {
        return new TestRuleSource(condition, next);
    }

    public static RuleSource sequence(RuleSource... rules) {
        if (rules.length == 0) {
            throw new IllegalArgumentException("Need at least 1 rule for a sequence");
        }
        return new SequenceRuleSource(List.of(rules));
    }

    public static RuleSource state(BlockState state) {
        return new BlockRuleSource(state);
    }

    public static RuleSource bandlands(SurfaceSystem system) {
        return new BandlandsRuleSource(system);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private static String stripNamespace(String id) {
        int idx = id.indexOf(':');
        return idx >= 0 ? id.substring(idx + 1) : id;
    }

    private static double map(double value, double fromMin, double fromMax, double toMin, double toMax) {
        double t = (value - fromMin) / (fromMax - fromMin);
        return toMin + t * (toMax - toMin);
    }

    private static long mix(long x) {
        x = (x ^ (x >>> 30)) * -4658895280553007687L;
        x = (x ^ (x >>> 27)) * -7723592293110705685L;
        return x ^ (x >>> 31);
    }

    private static double randomAt(String key, int x, int y, long seed) {
        long h = seed ^ key.hashCode();
        h = mix(h + x * 341873128712L + y * 132897987541L);
        return ((h >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
    }

    public enum CaveSurface {
        FLOOR,
        CEILING
    }
}
