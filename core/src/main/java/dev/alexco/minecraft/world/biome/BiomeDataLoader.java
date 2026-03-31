package dev.alexco.minecraft.world.biome;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.alexco.minecraft.util.Logger;
import dev.alexco.registry.ResourceLocation;

public class BiomeDataLoader {
    private static final Map<String, BiomeColours> COLOUR_CACHE = new HashMap<>();
    private static boolean initialised = false;

    public static class BiomeColours {
        public final Color grassColour;
        public final Color foliageColour;
        public final Color skyColour;

        public BiomeColours(Color grass, Color foliage, Color sky) {
            this.grassColour = grass;
            this.foliageColour = foliage;
            this.skyColour = sky;
        }
    }

    /**
     * Returns cached biome colours, loading and caching on first request.
     */
    public static BiomeColours getBiomeColours(ResourceLocation biomeName) {
        if (!initialised) {
            initialised = true;
        }

        String name = biomeName.getPath();
        if (COLOUR_CACHE.containsKey(name)) {
            return COLOUR_CACHE.get(name);
        }

        BiomeColours colours = loadBiomeColours(name);
        COLOUR_CACHE.put(name, colours);
        return colours;
    }

    /**
     * Loads biome colour values from biome JSON, with fallback defaults.
     */
    private static BiomeColours loadBiomeColours(String name) {
        String path = "data/minecraft/worldgen/biome/" + name + ".json";
        FileHandle fileHandle = Gdx.files.internal(path);

        if (!fileHandle.exists()) {
            return getFallbackColours(name);
        }

        try {
            String json = fileHandle.readString();
            JsonObject biomeJson = JsonParser.parseString(json).getAsJsonObject();

            if (!biomeJson.has("effects")) {
                return getFallbackColours(name);
            }

            JsonObject effects = biomeJson.getAsJsonObject("effects");

            Color grass = parseColour(effects, "grass_color", Color.GREEN);
            Color foliage = parseColour(effects, "foliage_color", grass);
            Color sky = parseColour(effects, "sky_color", Color.CYAN);

            return new BiomeColours(grass, foliage, sky);
        } catch (Exception e) {
            Logger.ERROR("Failed to load biome colours for %s: %s", name, e.getMessage());
            return getFallbackColours(name);
        }
    }

    /**
     * Parses an RGB integer colour entry from the effects object.
     */
    private static Color parseColour(JsonObject effects, String key, Color fallback) {
        if (!effects.has(key)) {
            return fallback;
        }

        JsonElement element = effects.get(key);
        if (element.isJsonPrimitive()) {
            int colourInt = element.getAsInt();
            return intToColor(colourInt);
        }

        return fallback;
    }

    /**
     * Converts packed RGB integer values into LibGDX Colour.
     */
    private static Color intToColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return new Color(r / 255f, g / 255f, b / 255f, 1f);
    }

    /**
     * Returns hardcoded colour presets when biome JSON is missing.
     */
    private static BiomeColours getFallbackColours(String name) {
        name = name.toLowerCase();
        Logger.INFO("Falling back for %s", name);
        if (name.contains("desert") || name.contains("badlands") || name.contains("savanna")) {
            return new BiomeColours(
                new Color(0.58f, 0.57f, 0.10f, 1f),
                new Color(0.64f, 0.58f, 0.22f, 1f),
                new Color(0.45f, 0.64f, 0.45f, 1f)
            );
        } else if (name.contains("taiga") || name.contains("snow") || name.contains("ice") || name.contains("frozen")) {
            return new BiomeColours(
                new Color(0.70f, 0.80f, 0.80f, 1f),
                new Color(0.30f, 0.50f, 0.40f, 1f),
                new Color(0.60f, 0.70f, 0.80f, 1f)
            );
        } else if (name.contains("jungle")) {
            return new BiomeColours(
                new Color(0.10f, 0.40f, 0.10f, 1f),
                new Color(0.05f, 0.35f, 0.05f, 1f),
                new Color(0.40f, 0.70f, 0.90f, 1f)
            );
        } else if (name.contains("swamp") || name.contains("mangrove")) {
            return new BiomeColours(
                new Color(0.45f, 0.50f, 0.25f, 1f),
                new Color(0.40f, 0.45f, 0.20f, 1f),
                new Color(0.50f, 0.55f, 0.45f, 1f)
            );
        } else if (name.contains("ocean") || name.contains("river") || name.contains("beach") || name.contains("shore")) {
            return new BiomeColours(
                new Color(0.60f, 0.70f, 0.30f, 1f),
                new Color(0.30f, 0.50f, 0.30f, 1f),
                new Color(0.50f, 0.65f, 0.80f, 1f)
            );
        } else if (name.contains("mountain") || name.contains("peak") || name.contains("stone") || name.contains("gravel")) {
            return new BiomeColours(
                new Color(0.55f, 0.55f, 0.45f, 1f),
                new Color(0.45f, 0.45f, 0.40f, 1f),
                new Color(0.50f, 0.60f, 0.70f, 1f)
            );
        }

        return new BiomeColours(Color.GREEN, new Color(0.13f, 0.55f, 0.13f, 1f), Color.CYAN);
    }
}
