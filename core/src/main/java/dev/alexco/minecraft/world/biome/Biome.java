package dev.alexco.minecraft.world.biome;

import com.badlogic.gdx.graphics.Color;
import dev.alexco.registry.ResourceLocation;

/**
 * Represents a biome with its visual/environmental properties.
 *
 * NOTE: Terrain generation parameters (baseHeight, heightVariation, roughness,
 * hasSurface) have been intentionally removed. Terrain shape is now exclusively
 * driven by VanillaNoiseStep.sampleFinalDensity2D() via the vanilla JSON density
 * function network. Keeping those fields alongside the vanilla system caused silent
 * conflicts: any code path that called biome.getBaseHeight() would produce a second
 * independent height estimate that disagreed with the density splines, resulting in
 * biome boundaries appearing at different positions than the terrain features they
 * were supposed to match.
 *
 * If you need to know terrain height for a given X position, call:
 *   VanillaNoiseStep.get(seed).estimateSurfaceY(blockX)
 * This returns the actual ground-truth height the density function will generate.
 */
public class Biome {
    protected ResourceLocation name;
    protected Color grassColour  = Color.GREEN;
    protected Color foliageColour = Color.GREEN;
    protected Color waterColour  = Color.BLUE;
    protected Color skyColour    = Color.CYAN;

    public Biome(ResourceLocation name) {
        this.name = name;

        BiomeDataLoader.BiomeColours colours = BiomeDataLoader.getBiomeColours(name);
        this.grassColour = colours.grassColour;
        this.foliageColour = colours.foliageColour;
        this.skyColour = colours.skyColour;
    }

    /** Fluent builder entry point — kept for chaining compatibility with Biomes.java. */
    public Biome create() {
        return this;
    }

    // -------------------------------------------------------------------------
    // Visual / environmental property setters
    // -------------------------------------------------------------------------

    public Biome setGrass(Color color) {
        this.grassColour = color;
        return this;
    }

    /** Accept both spellings so existing call sites don't need updating. */
    public Biome setFoiliage(Color color) {
        return setFoliage(color);
    }

    public Biome setFoliage(Color color) {
        this.foliageColour = color;
        return this;
    }

    public Biome setWater(Color color) {
        this.waterColour = color;
        return this;
    }

    public Biome setSky(Color color) {
        this.skyColour = color;
        return this;
    }

    // -------------------------------------------------------------------------
    // Visual / environmental property getters
    // -------------------------------------------------------------------------

    public Color getGrassColour()   { return grassColour;   }
    public Color getFoliageColour() { return foliageColour; }
    /** Compatibility alias for callers that use the old spelling. */
    public Color getFoiliageColour() { return foliageColour; }
    public Color getWaterColour()   { return waterColour;   }
    public Color getSkyColour()     { return skyColour;     }
    public ResourceLocation getName() { return name;        }
}
