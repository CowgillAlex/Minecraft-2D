package dev.alexco.minecraft.world.level.levelgen.vanilla;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.registry.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * Parses JSON surface rules and applies them using the official Minecraft SurfaceRules system.
 *
 * This class replaces the old implementation with one based on the official
 * Minecraft SurfaceRules and SurfaceSystem classes.
 */
public final class VanillaSurfaceRuleEngine {
    private static volatile VanillaSurfaceRuleEngine INSTANCE;

    private final long seed;
    private final VanillaWorldgenConfig worldgen;
    private final VanillaNoiseStep noiseStep;
    private final SurfaceSystem surfaceSystem;
    private final SurfaceRules.RuleSource rootRule;

    // Block cache for resolving block names
    private final Map<String, BlockState> blockCache = new HashMap<>();

    // Noise cache for rule conditions
    private final Map<String, dev.alexco.minecraft.world.level.levelgen.noise.Noise> noiseCache = new HashMap<>();

    private VanillaSurfaceRuleEngine(long seed) {
        this.seed = seed;
        this.worldgen = VanillaWorldgenConfig.get(seed);
        this.noiseStep = VanillaNoiseStep.get(seed);
        this.surfaceSystem = new SurfaceSystem(seed, worldgen, noiseStep);

        // Parse the surface rules from JSON
        JsonObject noiseSettings = readJson("data/minecraft/worldgen/noise_settings/overworld.json");
        this.rootRule = parseRuleSource(noiseSettings.getAsJsonObject("surface_rule"));
    }

    public static VanillaSurfaceRuleEngine get(long seed) {
        VanillaSurfaceRuleEngine current = INSTANCE;
        if (current == null || current.seed != seed) {
            synchronized (VanillaSurfaceRuleEngine.class) {
                current = INSTANCE;
                if (current == null || current.seed != seed) {
                    INSTANCE = current = new VanillaSurfaceRuleEngine(seed);
                }
            }
        }
        return current;
    }

    /**
     * Apply surface rules to a chunk using the official Minecraft SurfaceSystem.
     */
    public void applyToChunk(Chunk chunk, IntFunction<Biome> biomeAtWorldX) {
        try {
            surfaceSystem.buildSurface(chunk, biomeAtWorldX, rootRule);
        } catch (Exception e) {
            Logger.ERROR("Surface rule application failed for chunk %s: %s", chunk.getChunkPos(), e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw so it propagates up
        }
    }

    // ========================================================================
    // JSON PARSING
    // ========================================================================

    /**
     * Parse a RuleSource from JSON.
     * This converts the JSON representation into the official Minecraft RuleSource types.
     */
    private SurfaceRules.RuleSource parseRuleSource(JsonObject obj) {
        String type = stripNamespace(obj.get("type").getAsString());

        return switch (type) {
            case "sequence" -> {
                List<SurfaceRules.RuleSource> rules = new ArrayList<>();
                for (JsonElement el : obj.getAsJsonArray("sequence")) {
                    rules.add(parseRuleSource(el.getAsJsonObject()));
                }
                yield SurfaceRules.sequence(rules.toArray(new SurfaceRules.RuleSource[0]));
            }
            case "condition" -> {
                SurfaceRules.ConditionSource condition = parseConditionSource(obj.getAsJsonObject("if_true"));
                SurfaceRules.RuleSource thenRun = parseRuleSource(obj.getAsJsonObject("then_run"));
                yield SurfaceRules.ifTrue(condition, thenRun);
            }
            case "block" -> {
                BlockState state = resolveBlockState(obj.getAsJsonObject("result_state"));
                yield SurfaceRules.state(state);
            }
            case "bandlands", "badlands" -> SurfaceRules.bandlands(surfaceSystem);
            default -> {
                Logger.ERROR("Unsupported surface rule type: %s", type);
                yield ctx -> null; // No-op rule
            }
        };
    }

    /**
     * Parse a ConditionSource from JSON.
     * This converts the JSON representation into the official Minecraft ConditionSource types.
     */
    private SurfaceRules.ConditionSource parseConditionSource(JsonObject obj) {
        String type = stripNamespace(obj.get("type").getAsString());

        return switch (type) {
            case "biome" -> {
                List<String> biomes = new ArrayList<>();
                JsonArray arr = obj.getAsJsonArray("biome_is");
                for (JsonElement el : arr) {
                    biomes.add(el.getAsString());
                }
                yield SurfaceRules.isBiome(biomes);
            }
            case "noise_threshold" -> {
                String noiseName = stripNamespace(obj.get("noise").getAsString());
                double minThreshold = obj.get("min_threshold").getAsDouble();
                double maxThreshold = obj.get("max_threshold").getAsDouble();
                dev.alexco.minecraft.world.level.levelgen.noise.Noise noise = getOrCreateNoise(noiseName);
                yield SurfaceRules.noiseCondition(noise, minThreshold, maxThreshold);
            }
            case "y_above" -> {
                int anchorY = parseAnchor(obj.getAsJsonObject("anchor"));
                int surfaceDepthMultiplier = obj.has("surface_depth_multiplier") ?
                    obj.get("surface_depth_multiplier").getAsInt() : 0;
                boolean addStoneDepth = obj.has("add_stone_depth") &&
                    obj.get("add_stone_depth").getAsBoolean();
                yield SurfaceRules.yBlockCheck(anchorY, surfaceDepthMultiplier, addStoneDepth);
            }
            case "water" -> {
                int offset = obj.has("offset") ? obj.get("offset").getAsInt() : 0;
                int surfaceDepthMultiplier = obj.has("surface_depth_multiplier") ?
                    obj.get("surface_depth_multiplier").getAsInt() : 0;
                boolean addStoneDepth = obj.has("add_stone_depth") &&
                    obj.get("add_stone_depth").getAsBoolean();
                yield SurfaceRules.waterBlockCheck(offset, surfaceDepthMultiplier, addStoneDepth);
            }
            case "not" -> {
                SurfaceRules.ConditionSource invert = parseConditionSource(obj.getAsJsonObject("invert"));
                yield SurfaceRules.not(invert);
            }
            case "stone_depth" -> {
                int offset = obj.has("offset") ? obj.get("offset").getAsInt() : 0;
                boolean addSurfaceDepth = obj.has("add_surface_depth") &&
                    obj.get("add_surface_depth").getAsBoolean();
                int secondaryDepthRange = obj.has("secondary_depth_range") ?
                    obj.get("secondary_depth_range").getAsInt() : 0;
                String surfaceType = obj.has("surface_type") ?
                    obj.get("surface_type").getAsString() : "floor";
                SurfaceRules.CaveSurface caveSurface = surfaceType.equals("ceiling") ?
                    SurfaceRules.CaveSurface.CEILING : SurfaceRules.CaveSurface.FLOOR;
                yield SurfaceRules.stoneDepthCheck(offset, addSurfaceDepth, secondaryDepthRange, caveSurface);
            }
            case "steep" -> SurfaceRules.steep();
            case "hole" -> SurfaceRules.hole();
            case "temperature" -> SurfaceRules.temperature();
            case "vertical_gradient" -> {
                String randomName = obj.has("random_name") ?
                    obj.get("random_name").getAsString() : "minecraft:surface";
                int trueAtAndBelow = parseAnchor(obj.getAsJsonObject("true_at_and_below"));
                int falseAtAndAbove = parseAnchor(obj.getAsJsonObject("false_at_and_above"));
                yield SurfaceRules.verticalGradient(randomName, trueAtAndBelow, falseAtAndAbove, seed);
            }
            case "above_preliminary_surface" -> SurfaceRules.abovePreliminarySurface();
            default -> {
                Logger.ERROR("Unsupported surface condition type: %s", type);
                yield new SurfaceRules.ConditionSource() {
                    @Override
                    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
                        return () -> false;
                    }
                };
            }
        };
    }

    private int parseAnchor(JsonObject anchor) {
        if (anchor.has("absolute")) {
            return worldgen.toInternalY(anchor.get("absolute").getAsInt());
        }
        if (anchor.has("above_bottom")) {
            return anchor.get("above_bottom").getAsInt();
        }
        if (anchor.has("below_top")) {
            return anchor.get("below_top").getAsInt();
        }
        return 0;
    }

    private BlockState resolveBlockState(JsonObject stateObj) {
        String blockName = stateObj.get("Name").getAsString();

        // Check cache
        BlockState cached = blockCache.get(blockName);
        if (cached != null) {
            return cached;
        }



        Block block = Registry.BLOCK.get(new ResourceLocation(blockName));

        BlockState state = block != null ? block.defaultBlockState() : Blocks.STONE.defaultBlockState();
        System.out.println(block == null? "NULL": block.getDescriptionId());
        blockCache.put(blockName, state);
        return state;
    }

    private dev.alexco.minecraft.world.level.levelgen.noise.Noise getOrCreateNoise(String name) {
        dev.alexco.minecraft.world.level.levelgen.noise.Noise cached = noiseCache.get(name);
        if (cached != null) {
            return cached;
        }

        // Create noise with default parameters (will be overridden by config if found)
        dev.alexco.minecraft.world.level.levelgen.noise.Noise created = worldgen.createNoise(
            name,
            new dev.alexco.minecraft.world.level.levelgen.noise.NormalNoise.NoiseParameters(0, new double[]{1.0})
        );
        noiseCache.put(name, created);
        return created;
    }

    private static JsonObject readJson(String path) {
        FileHandle file = Gdx.files.classpath(path);
        return JsonParser.parseString(file.readString()).getAsJsonObject();
    }

    private static String stripNamespace(String id) {
        int idx = id.indexOf(':');
        return idx >= 0 ? id.substring(idx + 1) : id;
    }
}
