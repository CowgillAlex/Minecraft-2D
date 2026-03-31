package dev.alexco.minecraft.world.biome;

/**
 * Biome selection based on official Minecraft OverworldBiomeBuilder.
 * Uses 5 parameters: continentalness, temperature, humidity, weirdness, and erosion.
 *
 * Erosion is critical for terrain alignment:
 * - Low erosion (0-2): Mountains, peaks, slopes, jagged terrain
 * - Mid erosion (3-4): Hills, forests, normal terrain
 * - High erosion (5-6): Shattered, windswept, valleys, rivers
 */
public class BiomeManager {

    // Erosion thresholds matching Minecraft's 7 levels
    private static final double EROSION_EXTREME = -0.78;    // erosion[0]
    private static final double EROSION_HIGH = -0.375;      // erosion[1]
    private static final double EROSION_MODERATE = -0.2225; // erosion[2]
    private static final double EROSION_LOW = 0.05;         // erosion[3]
    private static final double EROSION_MINIMAL = 0.45;     // erosion[4]
    private static final double EROSION_SOME = 0.55;        // erosion[5]
    // erosion[6] is everything above 0.55

    /**
     * Buckets erosion noise into the seven vanilla erosion bands.
     */
    private static int getErosionLevel(double erosion) {
        if (erosion < EROSION_EXTREME) return 0;      // Extremely eroded (peaks)
        if (erosion < EROSION_HIGH) return 1;         // High erosion (slopes)
        if (erosion < EROSION_MODERATE) return 2;     // Moderate erosion (plateaus)
        if (erosion < EROSION_LOW) return 3;          // Low erosion (hills)
        if (erosion < EROSION_MINIMAL) return 4;      // Minimal erosion (flat)
        if (erosion < EROSION_SOME) return 5;         // Some erosion (shattered)
        return 6;                                      // Valleys/rivers
    }

    /**
     * Buckets temperature noise into discrete biome climate bands.
     */
    private static int temperature(double temp) {
        if (temp < -0.45) return 0; // Frozen
        if (temp < -0.15) return 1; // Cold
        if (temp < 0.2) return 2;   // Temperate
        if (temp < 0.55) return 3;  // Warm
        return 4;                   // Hot
    }

    /**
     * Buckets humidity noise into discrete biome climate bands.
     */
    private static int humidity(double hum) {
        if (hum < -0.35) return 0; // Arid
        if (hum < -0.1) return 1;  // Dry
        if (hum < 0.1) return 2;   // Average
        if (hum < 0.3) return 3;   // Humid
        return 4;                  // Wet
    }

    /**
     * Picks one of two biomes based on weirdness sign.
     */
    private static Biome weirdness(double weirdness, Biome less, Biome more) {
        return weirdness < 0 ? less : more;
    }

    /**
     * Sample biome based on climate parameters.
     * This now includes erosion which is critical for terrain-biome alignment.
     */
    public static Biome sampleBiome(double continentalness, double temperature,
                                     double humidity, double weirdness, double erosion) {
        int temp = temperature(temperature);
        int humid = humidity(humidity);
        int erode = getErosionLevel(erosion);

        // OCEAN ZONE: continentalness < 0 (erosion doesn't apply to oceans)
        if (continentalness < 0) {
            if (continentalness < -1.05) {
                return Biomes.MUSHROOM_FIELDS;
            } else if (continentalness < -0.6) {
                // Deep oceans
                if (temp == 0) return Biomes.DEEP_FROZEN_OCEAN;
                if (temp == 1) return Biomes.DEEP_COLD_OCEAN;
                if (temp == 2) return Biomes.DEEP_OCEAN;
                if (temp == 3) return Biomes.DEEP_LUKEWARM_OCEAN;
                return Biomes.WARM_OCEAN;
            } else {
                // Shallow oceans
                if (temp == 0) return Biomes.FROZEN_OCEAN;
                if (temp == 1) return Biomes.COLD_OCEAN;
                if (temp == 2) return Biomes.OCEAN;
                if (temp == 3) return Biomes.LUKEWARM_OCEAN;
                return Biomes.WARM_OCEAN;
            }
        }

        // BEACH/SHORE ZONE: continentalness 0 to 0.11
        if (continentalness < 0.11) {
            if (temp == 0) return Biomes.SNOWY_BEACH;
            // Stony shore in areas with high weirdness (mountainous coast)
            if (weirdness > 0.3 && erode <= 3) return Biomes.STONY_SHORE;
            return Biomes.BEACH;
        }

        // COASTAL TO NEAR INLAND: continentalness 0.11 to 0.3
        if (continentalness < 0.3) {
            // Swamps in low-lying coastal areas with high humidity and erosion
            if (humid >= 3 && temp >= 2 && temp <= 3 && erode >= 5) {
                return weirdness(weirdness, Biomes.SWAMP, Biomes.MANGROVE_SWAMP);
            }

            // Frozen areas
            if (temp == 0) {
                // Groves appear near slopes in cold areas
                if (erode <= 2 && weirdness > 0) return Biomes.GROVE;
                return Biomes.SNOWY_PLAINS;
            }

            // Default coastal biomes based on erosion
            if (erode <= 2) {
                // Low erosion = slopes/hills near coast
                if (temp == 1) return weirdness(weirdness, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS);
                if (temp == 2) return Biomes.WINDSWEPT_HILLS;
                if (temp == 3) return Biomes.WINDSWEPT_SAVANNA;
            }

            if (temp == 1) return weirdness(weirdness, Biomes.PLAINS, Biomes.FOREST);
            if (temp == 2) return weirdness(weirdness, Biomes.PLAINS, Biomes.FOREST);
            if (temp == 3) return weirdness(weirdness, Biomes.SAVANNA, Biomes.WINDSWEPT_SAVANNA);
            return Biomes.DESERT;
        }

        // MID INLAND: continentalness 0.3 to 1.0 (full range)
        // This is where erosion really matters for terrain alignment

        // EROSION 0-1: Extreme peaks and slopes (jagged terrain)
        if (erode <= 1) {
            if (temp == 0) {
                // Frozen peaks in cold areas
                return weirdness(weirdness, Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS);
            }
            if (temp == 1) {
                return weirdness(weirdness, Biomes.SNOWY_SLOPES, Biomes.GROVE);
            }
            if (temp == 2) return Biomes.STONY_PEAKS;
            if (temp == 3) return Biomes.STONY_PEAKS;
            // Hot areas with low erosion = badlands
            return pickBadlands(humid, weirdness);
        }

        // EROSION 2-3: Slopes, hills, and plateaus
        if (erode <= 3) {
            if (temp == 0) {
                if (erode == 2) return Biomes.SNOWY_SLOPES;
                return weirdness(weirdness, Biomes.SNOWY_PLAINS, Biomes.GROVE);
            }
            if (temp == 1) {
                if (humid <= 1) return Biomes.WINDSWEPT_HILLS;
                return weirdness(weirdness, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS);
            }
            if (temp == 2) {
                if (humid <= 2) return Biomes.WINDSWEPT_HILLS;
                return Biomes.FOREST;  // WINDSWEPT_FOREST doesn't exist, use FOREST
            }
            if (temp == 3) {
                return weirdness(weirdness, Biomes.SAVANNA, Biomes.WINDSWEPT_SAVANNA);
            }
            return pickBadlands(humid, weirdness);
        }

        // EROSION 4: Flat terrain (minimal erosion)
        if (erode == 4) {
            return pickFlatBiome(temp, humid, weirdness, continentalness);
        }

        // EROSION 5: Shattered/windswept terrain
        if (erode == 5) {
            if (temp == 0) return Biomes.WINDSWEPT_GRAVELLY_HILLS;
            if (temp == 1) return Biomes.WINDSWEPT_GRAVELLY_HILLS;
            if (temp == 2) return Biomes.WINDSWEPT_HILLS;
            if (temp == 3) return Biomes.WINDSWEPT_SAVANNA;
            return Biomes.DESERT;
        }

        // EROSION 6: Valleys, rivers, swamps
        // Only appears in coastal and near-inland areas
        if (temp == 0) return Biomes.SNOWY_PLAINS;  // FROZEN_RIVER doesn't exist
        if (humid >= 3 && temp >= 2 && temp <= 3) {
            return weirdness(weirdness, Biomes.SWAMP, Biomes.MANGROVE_SWAMP);
        }
        return Biomes.OCEAN;  // RIVER doesn't exist, use OCEAN as water placeholder
    }

    /**
     * Pick flat terrain biomes (erosion level 4)
     */
    private static Biome pickFlatBiome(int temp, int humid, double weirdness, double continentalness) {
        if (temp == 0) {
            if (humid == 0) return weirdness(weirdness, Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES);
            if (humid <= 2) return Biomes.SNOWY_PLAINS;
            if (humid == 3) return Biomes.SNOWY_TAIGA;
            return Biomes.TAIGA;
        }
        if (temp == 1) {
            if (humid <= 1) return Biomes.PLAINS;
            if (humid == 2) return Biomes.FOREST;
            if (humid == 3) return Biomes.TAIGA;
            return weirdness(weirdness, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA);
        }
        if (temp == 2) {
            if (humid == 0) return weirdness(weirdness, Biomes.FLOWER_FOREST, Biomes.SUNFLOWER_PLAINS);
            if (humid <= 2) return Biomes.FOREST;
            if (humid == 3) return weirdness(weirdness, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST);
            return Biomes.DARK_FOREST;
        }
        if (temp == 3) {
            if (humid <= 2) return Biomes.SAVANNA;
            if (humid == 3) return weirdness(weirdness, Biomes.FOREST, Biomes.PLAINS);
            return weirdness(weirdness, Biomes.JUNGLE, Biomes.SPARSE_JUNGLE);
        }
        // temp == 4 (hot)
        if (humid <= 1) return Biomes.DESERT;
        if (humid == 2) return weirdness(weirdness, Biomes.SAVANNA, Biomes.PLAINS);
        return weirdness(weirdness, Biomes.JUNGLE, Biomes.BAMBOO_JUNGLE);
    }

    /**
     * Pick badlands variant based on humidity and weirdness
     */
    private static Biome pickBadlands(int humid, double weirdness) {
        if (humid < 2) {
            return weirdness(weirdness, Biomes.BADLANDS, Biomes.ERODED_BADLANDS);
        }
        if (humid == 2) return Biomes.BADLANDS;
        return Biomes.WOODED_BADLANDS;
    }



}
