package dev.alexco.minecraft.world.level.levelgen.vanilla;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
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
import dev.alexco.minecraft.world.level.levelgen.noise.Noise;
import dev.alexco.minecraft.world.level.levelgen.noise.NormalNoise;
import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;
import dev.alexco.registry.ResourceLocation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VanillaWorldgenConfig {
    private static final String WORLDGEN_ROOT = "data/minecraft/worldgen";
    private static volatile VanillaWorldgenConfig INSTANCE;

    private final long seed;
    private final int minY;
    private final int height;
    private final int seaLevel;
    private final BlockState defaultBlock;
    private final BlockState defaultFluid;
    private final Map<String, NormalNoise.NoiseParameters> noiseParameters;
    private final List<OreFeature> oreFeatures;

    private VanillaWorldgenConfig(long seed) {
        this.seed = seed;

        JsonObject noiseSettings = readJsonIfExists(WORLDGEN_ROOT + "/noise_settings/overworld.json");
        JsonObject noiseObject = noiseSettings == null ? null : noiseSettings.getAsJsonObject("noise");

        this.minY = noiseObject != null && noiseObject.has("min_y") ? noiseObject.get("min_y").getAsInt() : -64;
        this.height = noiseObject != null && noiseObject.has("height") ? noiseObject.get("height").getAsInt() : 384;
        this.seaLevel = noiseSettings != null && noiseSettings.has("sea_level") ? noiseSettings.get("sea_level").getAsInt() : 63;

        String defaultBlockName = resolveBlockName(noiseSettings, "default_block", "minecraft:stone");
        String defaultFluidName = resolveBlockName(noiseSettings, "default_fluid", "minecraft:water");
        this.defaultBlock = blockStateFromName(defaultBlockName, Blocks.STONE.defaultBlockState());
        this.defaultFluid = blockStateFromName(defaultFluidName, Blocks.WATER.defaultBlockState());

        this.noiseParameters = loadNoiseParameters();
        this.oreFeatures = loadOreFeatures();

        applyBiomeVisuals();

        Logger.INFO("Loaded vanilla worldgen config: minY=%d, height=%d, seaLevel=%d, ores=%d", minY, height, seaLevel,
                oreFeatures.size());
    }

    public static VanillaWorldgenConfig get(long seed) {
        VanillaWorldgenConfig current = INSTANCE;
        if (current == null || current.seed != seed) {
            synchronized (VanillaWorldgenConfig.class) {
                current = INSTANCE;
                if (current == null || current.seed != seed) {
                    INSTANCE = current = new VanillaWorldgenConfig(seed);
                }
            }
        }
        return current;
    }

    public int getMinY() {
        return minY;
    }

    public int getHeight() {
        return height;
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public int toInternalY(int vanillaY) {
        return vanillaY - minY;
    }

    public int getWorldTopYExclusive() {
        return minY + height;
    }

    public int getSeaLevelInternal() {
        return toInternalY(seaLevel);
    }

    public BlockState getDefaultBlock() {
        return defaultBlock;
    }

    public BlockState getDefaultFluid() {
        return defaultFluid;
    }

    public List<OreFeature> getOreFeatures() {
        return oreFeatures;
    }

    public Noise createNoise(String noiseName, NormalNoise.NoiseParameters fallback) {
        NormalNoise.NoiseParameters parameters = noiseParameters.getOrDefault(noiseName, fallback);
        Xoroshiro source = new Xoroshiro(seed).fromHashOf("minecraft:" + noiseName);
        return new NormalNoise(source, parameters);
    }

    private void applyBiomeVisuals() {
        for (Biome biome : Registry.BIOME) {
            ResourceLocation key = Registry.BIOME.getKey(biome);
            if (key == null) {
                continue;
            }

            JsonObject biomeJson = readJsonIfExists(WORLDGEN_ROOT + "/biome/" + key.getPath() + ".json");
            if (biomeJson == null || !biomeJson.has("effects")) {
                continue;
            }

            JsonObject effects = biomeJson.getAsJsonObject("effects");
            if (effects.has("water_color")) {
                biome.setWater(rgbToColor(effects.get("water_color").getAsInt(), 1.0f));
            }
            if (effects.has("sky_color")) {
                biome.setSky(rgbToColor(effects.get("sky_color").getAsInt(), 1.0f));
            }
            if (effects.has("grass_color")) {
                biome.setGrass(rgbToColor(effects.get("grass_color").getAsInt(), 1.0f));
            }
            if (effects.has("foliage_color")) {
                biome.setFoiliage(rgbToColor(effects.get("foliage_color").getAsInt(), 1.0f));
            }
        }
    }

    private static Color rgbToColor(int rgb, float alpha) {
        float r = ((rgb >> 16) & 255) / 255.0f;
        float g = ((rgb >> 8) & 255) / 255.0f;
        float b = (rgb & 255) / 255.0f;
        return new Color(r, g, b, alpha);
    }

    private Map<String, NormalNoise.NoiseParameters> loadNoiseParameters() {
        Map<String, NormalNoise.NoiseParameters> result = new HashMap<>();

        FileHandle assetHandle = Gdx.files.internal("assets.txt");
        if (!assetHandle.exists()) {
            Logger.ERROR("assets.txt not found! Cannot load noise parameters.");
            return result;
        }

        String text = assetHandle.readString();
        String[] lines = text.split("\n");
        String noisePrefix = "data/minecraft/worldgen/noise/";

        int loaded = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith(noisePrefix) && line.endsWith(".json")) {
                try {
                    FileHandle file = Gdx.files.internal(line);
                    if (!file.exists()) {
                        continue;
                    }
                    JsonObject obj = JsonParser.parseString(file.readString()).getAsJsonObject();
                    int firstOctave = obj.has("firstOctave") ? obj.get("firstOctave").getAsInt() : 0;

                    JsonArray ampsArray = obj.has("amplitudes") ? obj.getAsJsonArray("amplitudes") : new JsonArray();
                    double[] amplitudes = new double[ampsArray.size()];
                    for (int i = 0; i < ampsArray.size(); i++) {
                        amplitudes[i] = ampsArray.get(i).getAsDouble();
                    }

                    String name = line.substring(noisePrefix.length(), line.length() - 5);
                    result.put(name, new NormalNoise.NoiseParameters(firstOctave, amplitudes));
                    loaded++;
                } catch (Exception e) {
                    Logger.ERROR("Failed to parse noise json %s: %s", line, e.getMessage());
                }
            }
        }

        Logger.DEBUG("Loaded %d noise parameter files", loaded);
        return result;
    }

    private List<OreFeature> loadOreFeatures() {
        FileHandle assetHandle = Gdx.files.internal("assets.txt");
        if (!assetHandle.exists()) {
            Logger.ERROR("assets.txt not found! Cannot load ore features.");
            return Collections.emptyList();
        }

        String text = assetHandle.readString();
        String[] lines = text.split("\n");
        String placedPrefix = "data/minecraft/worldgen/placed_feature/ore_";

        List<OreFeature> features = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (!line.startsWith(placedPrefix) || !line.endsWith(".json")) {
                continue;
            }

            try {
                FileHandle placed = Gdx.files.internal(line);
                if (!placed.exists()) {
                    continue;
                }

                String placedId = line.substring(placedPrefix.length(), line.length() - 5);
                JsonObject placedJson = JsonParser.parseString(placed.readString()).getAsJsonObject();
                if (!placedJson.has("feature") || !placedJson.has("placement")) {
                    continue;
                }

                String configuredName = stripNamespace(placedJson.get("feature").getAsString());
                JsonObject configuredJson = readJsonIfExists(
                        WORLDGEN_ROOT + "/configured_feature/" + configuredName + ".json");
                if (configuredJson == null || !configuredJson.has("config")) {
                    continue;
                }

                JsonObject config = configuredJson.getAsJsonObject("config");
                int size = config.has("size") ? config.get("size").getAsInt() : 8;
                List<OreTarget> targets = parseOreTargets(config);
                if (targets.isEmpty()) {
                    continue;
                }

                PlacementData placementData = parsePlacementData(placedJson.getAsJsonArray("placement"));
                features.add(new OreFeature(placedId, targets, size, placementData.count, placementData.rarity,
                        placementData.heightRange));
            } catch (Exception e) {
                Logger.ERROR("Failed to parse ore placed feature %s: %s", line, e.getMessage());
            }
        }

        return features;
    }

    private List<OreTarget> parseOreTargets(JsonObject config) {
        if (!config.has("targets")) {
            return Collections.emptyList();
        }

        List<OreTarget> targets = new ArrayList<>();
        JsonArray targetArray = config.getAsJsonArray("targets");

        for (JsonElement targetElement : targetArray) {
            if (!targetElement.isJsonObject()) {
                continue;
            }
            JsonObject targetObj = targetElement.getAsJsonObject();
            if (!targetObj.has("state")) {
                continue;
            }

            JsonObject stateObj = targetObj.getAsJsonObject("state");
            String stateName = stateObj.has("Name") ? stateObj.get("Name").getAsString() : "minecraft:stone";
            if (!stateName.contains("_ore")) {
                continue;
            }
            BlockState state = blockStateFromName(stateName, null);
            if (state == null) {
                continue;
            }

            boolean prefersDeepslate = stateName.contains("deepslate");
            targets.add(new OreTarget(state, prefersDeepslate));
        }

        return targets;
    }

    private PlacementData parsePlacementData(JsonArray placements) {
        int count = 1;
        int rarity = 1;
        HeightRange range = new HeightRange(toInternalY(-64), toInternalY(320), false);

        for (JsonElement placementElement : placements) {
            if (!placementElement.isJsonObject()) {
                continue;
            }

            JsonObject placement = placementElement.getAsJsonObject();
            if (!placement.has("type")) {
                continue;
            }

            String type = stripNamespace(placement.get("type").getAsString());
            if (type.equals("count") && placement.has("count")) {
                if (placement.get("count").isJsonPrimitive()) {
                    count = placement.get("count").getAsInt();
                }
            } else if (type.equals("count_extra")) {
                int baseCount = placement.has("count") ? placement.get("count").getAsInt() : 0;
                int extraCount = placement.has("extra_count") ? placement.get("extra_count").getAsInt() : 0;
                float extraChance = placement.has("extra_chance") ? placement.get("extra_chance").getAsFloat() : 0;
                count = baseCount + Math.round(extraCount * extraChance);
            } else if (type.equals("rarity_filter") && placement.has("chance")) {
                rarity = Math.max(1, placement.get("chance").getAsInt());
            } else if (type.equals("height_range") && placement.has("height")) {
                range = parseHeightRange(placement.getAsJsonObject("height"));
            }
        }

        return new PlacementData(count, rarity, range);
    }

    private HeightRange parseHeightRange(JsonObject heightObject) {
        String type = heightObject.has("type") ? stripNamespace(heightObject.get("type").getAsString()) : "uniform";

        JsonObject minAnchor = heightObject.has("min_inclusive") ? heightObject.getAsJsonObject("min_inclusive")
                : new JsonObject();
        JsonObject maxAnchor = heightObject.has("max_inclusive") ? heightObject.getAsJsonObject("max_inclusive")
                : new JsonObject();

        int min = toInternalY(resolveAnchor(minAnchor));
        int max = toInternalY(resolveAnchor(maxAnchor));

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        min = clamp(min, 0, height - 1);
        max = clamp(max, 0, height - 1);

        return new HeightRange(min, max, type.equals("trapezoid"));
    }

    private int resolveAnchor(JsonObject anchor) {
        if (anchor.has("absolute")) {
            return anchor.get("absolute").getAsInt();
        }
        if (anchor.has("above_bottom")) {
            return minY + anchor.get("above_bottom").getAsInt();
        }
        if (anchor.has("below_top")) {
            return getWorldTopYExclusive() - 1 - anchor.get("below_top").getAsInt();
        }
        return minY;
    }

    private JsonObject readJsonIfExists(String path) {
        try {
            FileHandle handle = Gdx.files.internal(path);
            if (!handle.exists()) {
                return null;
            }
            return JsonParser.parseString(handle.readString()).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveBlockName(JsonObject root, String key, String fallback) {
        if (root == null || !root.has(key)) {
            return fallback;
        }

        JsonObject blockObj = root.getAsJsonObject(key);
        if (blockObj == null || !blockObj.has("Name")) {
            return fallback;
        }
        return blockObj.get("Name").getAsString();
    }

    private static String stripNamespace(String id) {
        int index = id.indexOf(':');
        return index >= 0 ? id.substring(index + 1) : id;
    }

    private static BlockState blockStateFromName(String id, BlockState fallback) {
        String mapped = switch (id) {
            case "minecraft:water" -> "minecraft:water_still";
            default -> id;
        };

        Block block = Registry.BLOCK.get(new ResourceLocation(mapped));
        if (block == null) {
            return fallback;
        }
        return block.defaultBlockState();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record PlacementData(int count, int rarity, HeightRange heightRange) {
    }

    public record HeightRange(int minY, int maxY, boolean trapezoid) {
    }

    public record OreTarget(BlockState state, boolean deepslatePreferred) {
    }

    public record OreFeature(String id, List<OreTarget> targets, int size, int count, int rarity, HeightRange range) {
    }
}
