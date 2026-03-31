package dev.alexco.minecraft.world.biome;

import com.badlogic.gdx.graphics.Color;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.registry.ResourceLocation;

/**
 * Registry of all biome instances.
 *
 * Terrain shape parameters (baseHeight, heightVariation, roughness, hasSurface)
 * have been removed from all registrations. Those values were set here but never
 * actually used by the terrain generator — VanillaNoiseStep owns terrain shape
 * entirely. Having them here created false documentation and tempted code paths
 * to call getBaseHeight() and get an answer that contradicted the density splines.
 */
public class Biomes {

    // -------------------------------------------------------------------------
    // OCEAN BIOMES — below sea level, continentalness < ~-0.19
    // -------------------------------------------------------------------------

    public static final Biome MUSHROOM_FIELDS = register("mushroom_fields",
        new Biome(new ResourceLocation("mushroom_fields"))
            .create());

    public static final Biome FROZEN_OCEAN = register("frozen_ocean",
        new Biome(new ResourceLocation("frozen_ocean"))
            .create());

    public static final Biome DEEP_FROZEN_OCEAN = register("deep_frozen_ocean",
        new Biome(new ResourceLocation("deep_frozen_ocean"))
            .create());

    public static final Biome COLD_OCEAN = register("cold_ocean",
        new Biome(new ResourceLocation("cold_ocean"))
            .create());

    public static final Biome DEEP_COLD_OCEAN = register("deep_cold_ocean",
        new Biome(new ResourceLocation("deep_cold_ocean"))
            .create());

    public static final Biome OCEAN = register("ocean",
        new Biome(new ResourceLocation("ocean"))
            .create());

    public static final Biome DEEP_OCEAN = register("deep_ocean",
        new Biome(new ResourceLocation("deep_ocean"))
            .create());

    public static final Biome LUKEWARM_OCEAN = register("lukewarm_ocean",
        new Biome(new ResourceLocation("lukewarm_ocean"))
            .create());

    public static final Biome DEEP_LUKEWARM_OCEAN = register("deep_lukewarm_ocean",
        new Biome(new ResourceLocation("deep_lukewarm_ocean"))
            .create());

    public static final Biome WARM_OCEAN = register("warm_ocean",
        new Biome(new ResourceLocation("warm_ocean"))
            .create());

    // -------------------------------------------------------------------------
    // BEACH / SHORE — near sea level, continentalness ~-0.19 to 0.03
    // -------------------------------------------------------------------------

    public static final Biome BEACH = register("beach",
        new Biome(new ResourceLocation("beach"))
            .create());

    public static final Biome SNOWY_BEACH = register("snowy_beach",
        new Biome(new ResourceLocation("snowy_beach"))
            .create());

    public static final Biome STONY_SHORE = register("stony_shore",
        new Biome(new ResourceLocation("stony_shore"))
            .create());

    // -------------------------------------------------------------------------
    // WETLAND / SWAMP
    // -------------------------------------------------------------------------

    public static final Biome SWAMP = register("swamp",
        new Biome(new ResourceLocation("swamp"))
            .create());

    public static final Biome MANGROVE_SWAMP = register("mangrove_swamp",
        new Biome(new ResourceLocation("mangrove_swamp"))
            .create());

    // -------------------------------------------------------------------------
    // MOUNTAIN / PEAK BIOMES — low erosion, high continentalness
    // -------------------------------------------------------------------------

    public static final Biome FROZEN_PEAKS = register("frozen_peaks",
        new Biome(new ResourceLocation("frozen_peaks"))
            .create());

    public static final Biome JAGGED_PEAKS = register("jagged_peaks",
        new Biome(new ResourceLocation("jagged_peaks"))
            .create());

    public static final Biome STONY_PEAKS = register("stony_peaks",
        new Biome(new ResourceLocation("stony_peaks"))
            .create());

    public static final Biome SNOWY_SLOPES = register("snowy_slopes",
        new Biome(new ResourceLocation("snowy_slopes"))
            .create());

    public static final Biome GROVE = register("grove",
        new Biome(new ResourceLocation("grove"))
            .create());

    // -------------------------------------------------------------------------
    // BADLANDS
    // -------------------------------------------------------------------------

    public static final Biome BADLANDS = register("badlands",
        new Biome(new ResourceLocation("badlands"))
            .create());

    public static final Biome ERODED_BADLANDS = register("eroded_badlands",
        new Biome(new ResourceLocation("eroded_badlands"))
            .create());

    public static final Biome WOODED_BADLANDS = register("wooded_badlands",
        new Biome(new ResourceLocation("wooded_badlands"))
            .create());

    // -------------------------------------------------------------------------
    // WINDSWEPT HILLS
    // -------------------------------------------------------------------------

    public static final Biome WINDSWEPT_HILLS = register("windswept_hills",
        new Biome(new ResourceLocation("windswept_hills"))
            .create());

    public static final Biome WINDSWEPT_GRAVELLY_HILLS = register("windswept_gravelly_hills",
        new Biome(new ResourceLocation("windswept_gravelly_hills"))
            .create());

    public static final Biome WINDSWEPT_SAVANNA = register("windswept_savanna",
        new Biome(new ResourceLocation("windswept_savanna"))
            .create());

    // -------------------------------------------------------------------------
    // CAVE BIOMES
    // -------------------------------------------------------------------------

    public static final Biome DRIPSTONE_CAVES = register("dripstone_caves",
        new Biome(new ResourceLocation("dripstone_caves"))
            .create());

    // -------------------------------------------------------------------------
    // COLD / SNOWY LAND BIOMES
    // -------------------------------------------------------------------------

    public static final Biome SNOWY_PLAINS = register("snowy_plains",
        new Biome(new ResourceLocation("snowy_plains"))
            .create());

    public static final Biome ICE_SPIKES = register("ice_spikes",
        new Biome(new ResourceLocation("ice_spikes"))
            .create());

    public static final Biome SNOWY_TAIGA = register("snowy_taiga",
        new Biome(new ResourceLocation("snowy_taiga"))
            .create());

    public static final Biome TAIGA = register("taiga",
        new Biome(new ResourceLocation("taiga"))
            .create());

    // -------------------------------------------------------------------------
    // TEMPERATE LAND BIOMES
    // -------------------------------------------------------------------------

    public static final Biome PLAINS = register("plains",
        new Biome(new ResourceLocation("plains"))
            .create());

    public static final Biome SUNFLOWER_PLAINS = register("sunflower_plains",
        new Biome(new ResourceLocation("sunflower_plains"))
            .create());

    public static final Biome FOREST = register("forest",
        new Biome(new ResourceLocation("forest"))
            .create());

    public static final Biome FLOWER_FOREST = register("flower_forest",
        new Biome(new ResourceLocation("flower_forest"))
            .create());

    public static final Biome BIRCH_FOREST = register("birch_forest",
        new Biome(new ResourceLocation("birch_forest"))
            .create());

    public static final Biome OLD_GROWTH_BIRCH_FOREST = register("old_growth_birch_forest",
        new Biome(new ResourceLocation("old_growth_birch_forest"))
            .create());

    public static final Biome DARK_FOREST = register("dark_forest",
        new Biome(new ResourceLocation("dark_forest"))
            .create());

    public static final Biome OLD_GROWTH_SPRUCE_TAIGA = register("old_growth_spruce_taiga",
        new Biome(new ResourceLocation("old_growth_spruce_taiga"))
            .create());

    public static final Biome OLD_GROWTH_PINE_TAIGA = register("old_growth_pine_taiga",
        new Biome(new ResourceLocation("old_growth_pine_taiga"))
            .create());

    // -------------------------------------------------------------------------
    // WARM LAND BIOMES
    // -------------------------------------------------------------------------

    public static final Biome SAVANNA = register("savanna",
        new Biome(new ResourceLocation("savanna"))
            .create());

    public static final Biome JUNGLE = register("jungle",
        new Biome(new ResourceLocation("jungle"))
            .create());

    public static final Biome SPARSE_JUNGLE = register("sparse_jungle",
        new Biome(new ResourceLocation("sparse_jungle"))
            .create());

    public static final Biome BAMBOO_JUNGLE = register("bamboo_jungle",
        new Biome(new ResourceLocation("bamboo_jungle"))
            .create());

    public static final Biome DESERT = register("desert",
        new Biome(new ResourceLocation("desert"))
            .create());

    // -------------------------------------------------------------------------

    public static Biome register(String name, Biome biome) {
        return dev.alexco.registry.Registry.register(Registry.BIOME, name, biome);
    }
}
