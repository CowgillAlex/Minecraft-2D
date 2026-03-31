package dev.alexco.minecraft.world.level.levelgen.feature;

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
import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;
import dev.alexco.minecraft.world.level.levelgen.vanilla.VanillaWorldgenConfig;
import dev.alexco.minecraft.world.level.levelgen.vanilla.VanillaNoiseStep;
import dev.alexco.registry.ResourceLocation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureRegistry {
    private static final String WORLDGEN_ROOT = "data/minecraft/worldgen";
    private static FeatureRegistry INSTANCE;
    
    private final long seed;
    private final Xoroshiro random;
    private final Map<String, PlacedFeature> placedFeatures;
    private final Map<Biome, List<PlacedFeature[]>> biomeFeatures;
    
    private FeatureRegistry(long seed) {
        this.seed = seed;
        this.random = new Xoroshiro(seed);
        this.placedFeatures = new HashMap<>();
        this.biomeFeatures = new HashMap<>();
        
        loadPlacedFeatures();
        loadBiomeFeatures();
        
        Logger.INFO("Loaded %d placed features for %d biomes", placedFeatures.size(), biomeFeatures.size());
    }
    
    public static FeatureRegistry get(long seed) {
        if (INSTANCE == null || INSTANCE.seed != seed) {
            INSTANCE = new FeatureRegistry(seed);
        }
        return INSTANCE;
    }
    
    private void loadPlacedFeatures() {
        FileHandle placedDir = Gdx.files.internal(WORLDGEN_ROOT + "/placed_feature");
        if (!placedDir.exists() || !placedDir.isDirectory()) {
            return;
        }
        
        for (FileHandle placed : placedDir.list()) {
            if (!placed.extension().equals("json")) {
                continue;
            }
            
            String placedId = placed.nameWithoutExtension();
            try {
                JsonObject placedJson = JsonParser.parseString(placed.readString()).getAsJsonObject();
                if (!placedJson.has("feature") || !placedJson.has("placement")) {
                    continue;
                }
                
                String featureType = stripNamespace(placedJson.get("feature").getAsString());
                JsonArray placement = placedJson.getAsJsonArray("placement");
                
                PlacedFeature feature = parsePlacedFeature(placedId, featureType, placement);
                if (feature != null) {
                    placedFeatures.put(placedId, feature);
                }
            } catch (Exception e) {
                Logger.ERROR("Failed to parse placed feature %s: %s", placedId, e.getMessage());
            }
        }
    }
    
    private PlacedFeature parsePlacedFeature(String id, String featureType, JsonArray placement) {
        JsonObject configuredJson = readJson(WORLDGEN_ROOT + "/configured_feature/" + featureType + ".json");
        
        int count = 1;
        int rarity = 1;
        HeightRange range = new HeightRange(0, 256, false);
        
        for (JsonElement elem : placement) {
            if (!elem.isJsonObject()) continue;
            JsonObject p = elem.getAsJsonObject();
            if (!p.has("type")) continue;
            
            String type = stripNamespace(p.get("type").getAsString());
            
            switch (type) {
                case "count", "count_extra" -> {
                    if (p.has("count")) {
                        count = p.get("count").getAsInt();
                    }
                }
                case "rarity_filter" -> {
                    if (p.has("chance")) {
                        int chance = p.get("chance").getAsInt();
                        rarity = chance > 0 ? chance : 1;
                    }
                }
                case "height_range", "biome_biome" -> {
                    if (p.has("height")) {
                        range = parseHeightRange(p.getAsJsonObject("height"));
                    }
                }
            }
        }
        
        return new PlacedFeature(id, featureType, configuredJson, count, rarity, range);
    }
    
    private HeightRange parseHeightRange(JsonObject heightObject) {
        int min = 0;
        int max = 256;
        
        if (heightObject.has("min_inclusive")) {
            JsonObject minObj = heightObject.getAsJsonObject("min_inclusive");
            if (minObj.has("absolute")) {
                min = minObj.get("absolute").getAsInt();
            } else if (minObj.has("above_bottom")) {
                min = VanillaWorldgenConfig.get(seed).getMinY() + minObj.get("above_bottom").getAsInt();
            }
        }
        
        if (heightObject.has("max_inclusive")) {
            JsonObject maxObj = heightObject.getAsJsonObject("max_inclusive");
            if (maxObj.has("absolute")) {
                max = maxObj.get("absolute").getAsInt();
            } else if (maxObj.has("below_top")) {
                max = VanillaWorldgenConfig.get(seed).getWorldTopYExclusive() - maxObj.get("below_top").getAsInt();
            }
        }
        
        return new HeightRange(min, max, false);
    }
    
    private void loadBiomeFeatures() {
        for (Biome biome : Registry.BIOME) {
            ResourceLocation key = Registry.BIOME.getKey(biome);
            if (key == null) continue;
            
            JsonObject biomeJson = readJson(WORLDGEN_ROOT + "/biome/" + key.getPath() + ".json");
            if (biomeJson == null || !biomeJson.has("features")) {
                biomeFeatures.put(biome, Collections.emptyList());
                continue;
            }
            
            JsonArray featuresArray = biomeJson.getAsJsonArray("features");
            List<PlacedFeature[]> stages = new ArrayList<>();
            
            for (JsonElement stageElem : featuresArray) {
                if (!stageElem.isJsonArray()) {
                    stages.add(new PlacedFeature[0]);
                    continue;
                }
                
                JsonArray stage = stageElem.getAsJsonArray();
                List<PlacedFeature> stageFeatures = new ArrayList<>();
                
                for (JsonElement featureElem : stage) {
                    if (!featureElem.isJsonPrimitive()) continue;
                    String featureId = featureElem.getAsString();
                    featureId = stripNamespace(featureId);
                    
                    PlacedFeature feature = placedFeatures.get(featureId);
                    if (feature != null) {
                        stageFeatures.add(feature);
                    }
                }
                
                stages.add(stageFeatures.toArray(new PlacedFeature[0]));
            }
            
            biomeFeatures.put(biome, stages);
        }
    }
    
    public List<PlacedFeature[]> getBiomeFeatures(Biome biome) {
        return biomeFeatures.getOrDefault(biome, Collections.emptyList());
    }
    
    public PlacedFeature getFeature(String id) {
        return placedFeatures.get(id);
    }
    
    public TreeType detectTreeType(PlacedFeature feature) {
        if (feature == null) return TreeType.OAK;
        
        String id = feature.id().toLowerCase();
        
        if (id.contains("birch")) return TreeType.BIRCH;
        if (id.contains("spruce") || id.contains("taiga")) return TreeType.SPRUCE;
        if (id.contains("jungle")) return TreeType.JUNGLE;
        if (id.contains("acacia")) return TreeType.ACACIA;
        if (id.contains("dark_oak") || id.contains("dark")) return TreeType.DARK_OAK;
        if (id.contains("cherry")) return TreeType.CHERRY;
        if (id.contains("mega") || id.contains("large")) return TreeType.MEGA;
        
        return TreeType.OAK;
    }
    
    public enum TreeType {
        OAK, BIRCH, SPRUCE, JUNGLE, ACACIA, DARK_OAK, CHERRY, MEGA
    }
    
    public Xoroshiro getRandom() {
        return new Xoroshiro(seed);
    }
    
    private JsonObject readJson(String path) {
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
    
    private static String stripNamespace(String id) {
        int idx = id.indexOf(':');
        return idx >= 0 ? id.substring(idx + 1) : id;
    }
    
    public record PlacedFeature(
        String id,
        String featureType,
        JsonObject config,
        int count,
        int rarity,
        HeightRange range
    ) {
        public boolean shouldPlace(Xoroshiro random) {
            return rarity <= 1 || random.nextInt(rarity) == 0;
        }
    }
    
    public record HeightRange(int minY, int maxY, boolean trapezoid) {
        public boolean isInRange(int y) {
            return y >= minY && y < maxY;
        }
    }
}
