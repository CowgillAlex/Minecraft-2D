package dev.alexco.minecraft.tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import dev.alexco.registry.ResourceLocation;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.biome.Biomes;

/**
 * Tag system for biomes, similar to BlockTags.
 * Tags can contain individual biomes or other tags for nesting.
 */
public class BiomeTags {
    private static TagContainer<Biome> container;
    private static int latestVersion;

    // Individual ocean biomes
    public static final Tag<Biome> OCEAN = Tag.Builder.<Biome>create()
        .add(Biomes.OCEAN)
        .build(new ResourceLocation("minecraft", "ocean"));

    public static final Tag<Biome> DEEP_OCEAN = Tag.Builder.<Biome>create()
        .add(Biomes.DEEP_OCEAN)
        .build(new ResourceLocation("minecraft", "deep_ocean"));

    public static final Tag<Biome> COLD_OCEAN = Tag.Builder.<Biome>create()
        .add(Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN)
        .build(new ResourceLocation("minecraft", "cold_ocean"));

    public static final Tag<Biome> FROZEN_OCEAN = Tag.Builder.<Biome>create()
        .add(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN)
        .build(new ResourceLocation("minecraft", "frozen_ocean"));

    public static final Tag<Biome> LUKEWARM_OCEAN = Tag.Builder.<Biome>create()
        .add(Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN)
        .build(new ResourceLocation("minecraft", "lukewarm_ocean"));

    public static final Tag<Biome> WARM_OCEAN = Tag.Builder.<Biome>create()
        .add(Biomes.WARM_OCEAN)
        .build(new ResourceLocation("minecraft", "warm_ocean"));

    // Nested tag: All ocean biomes combined
    public static final Tag<Biome> OCEAN_BIOMES = Tag.Builder.<Biome>create()
        .add(OCEAN)
        .add(DEEP_OCEAN)
        .add(COLD_OCEAN)
        .add(FROZEN_OCEAN)
        .add(LUKEWARM_OCEAN)
        .add(WARM_OCEAN)
        .build(new ResourceLocation("minecraft", "ocean_biomes"));

    // Deep ocean biomes (for structure spawning, etc.)
    public static final Tag<Biome> DEEP_OCEAN_BIOMES = Tag.Builder.<Biome>create()
        .add(DEEP_OCEAN)
        .add(COLD_OCEAN)
        .add(FROZEN_OCEAN)
        .add(LUKEWARM_OCEAN)
        .build(new ResourceLocation("minecraft", "deep_ocean_biomes"));

    // Beach and shore biomes
    public static final Tag<Biome> BEACH_BIOMES = Tag.Builder.<Biome>create()
        .add(Biomes.SNOWY_PLAINS) // Snowy beach
        .add(Biomes.PLAINS) // Beach
        .add(Biomes.SAVANNA) // Beach variant
        .add(Biomes.DESERT) // Beach
        .build(new ResourceLocation("minecraft", "beach_biomes"));

    // Cold biomes
    public static final Tag<Biome> SNOWY_BIOMES = Tag.Builder.<Biome>create()
        .add(Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.ICE_SPIKES)
        .build(new ResourceLocation("minecraft", "snowy_biomes"));

    public static final Tag<Biome> TAIGA_BIOMES = Tag.Builder.<Biome>create()
        .add(Biomes.TAIGA, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA)
        .build(new ResourceLocation("minecraft", "taiga_biomes"));

    // Forest biomes
    public static final Tag<Biome> FOREST_BIOMES = Tag.Builder.<Biome>create()
        .add(Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST)
        .add(Biomes.DARK_FOREST)
        .build(new ResourceLocation("minecraft", "forest_biomes"));

    // Plains biomes
    public static final Tag<Biome> PLAINS_BIOMES = Tag.Builder.<Biome>create()
        .add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS)
        .build(new ResourceLocation("minecraft", "plains_biomes"));

    // Warm biomes
    public static final Tag<Biome> JUNGLE_BIOMES = Tag.Builder.<Biome>create()
        .add(Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE)
        .build(new ResourceLocation("minecraft", "jungle_biomes"));

    public static final Tag<Biome> SAVANNA_BIOMES = Tag.Builder.<Biome>create()
        .add(Biomes.SAVANNA)
        .build(new ResourceLocation("minecraft", "savanna_biomes"));

    public static final Tag<Biome> DESERT_BIOMES = Tag.Builder.<Biome>create()
        .add(Biomes.DESERT)
        .build(new ResourceLocation("minecraft", "desert_biomes"));

    // All land biomes (non-ocean)
    public static final Tag<Biome> LAND_BIOMES = Tag.Builder.<Biome>create()
        .add(SNOWY_BIOMES)
        .add(TAIGA_BIOMES)
        .add(FOREST_BIOMES)
        .add(PLAINS_BIOMES)
        .add(JUNGLE_BIOMES)
        .add(SAVANNA_BIOMES)
        .add(DESERT_BIOMES)
        .build(new ResourceLocation("minecraft", "land_biomes"));

    // Biomes suitable for villages
    public static final Tag<Biome> VILLAGE_BIOMES = Tag.Builder.<Biome>create()
        .add(PLAINS_BIOMES)
        .add(FOREST_BIOMES)
        .add(TAIGA_BIOMES)
        .add(SAVANNA_BIOMES)
        .add(DESERT_BIOMES)
        .build(new ResourceLocation("minecraft", "village_biomes"));

    // Biomes with trees
    public static final Tag<Biome> HAS_TREES = Tag.Builder.<Biome>create()
        .add(FOREST_BIOMES)
        .add(TAIGA_BIOMES)
        .add(JUNGLE_BIOMES)
        .add(SAVANNA_BIOMES)
        .build(new ResourceLocation("minecraft", "has_trees"));

    // Biomes without trees
    public static final Tag<Biome> NO_TREES = Tag.Builder.<Biome>create()
        .add(OCEAN_BIOMES)
        .add(BEACH_BIOMES)
        .add(PLAINS_BIOMES)
        .add(DESERT_BIOMES)
        .add(SNOWY_BIOMES)
        .build(new ResourceLocation("minecraft", "no_trees"));

    // Mushroom island (special case)
    public static final Tag<Biome> MUSHROOM_ISLAND = Tag.Builder.<Biome>create()
        .add(Biomes.MUSHROOM_FIELDS)
        .build(new ResourceLocation("minecraft", "mushroom_island"));

    public static void setContainer(final TagContainer<Biome> container) {
        BiomeTags.container = container;
        ++BiomeTags.latestVersion;
    }

    public static TagContainer<Biome> getContainer() {
        return BiomeTags.container;
    }

    /**
     * Inserts or replaces one biome tag entry in the shared container.
     */
    private static Tag<Biome> register(final String id, Tag<Biome> tag) {
        Map<ResourceLocation, Tag<Biome>> map = new HashMap<>(BiomeTags.container.getEntries());
        map.put(tag.getId(), tag);
        BiomeTags.container.method_20735(map);
        return tag;
    }

    static {
        BiomeTags.container = new TagContainer<Biome>(arg -> Optional.empty(), "biome", false, "biome");

        register(OCEAN.getId().toString(), OCEAN);
        register(DEEP_OCEAN.getId().toString(), DEEP_OCEAN);
        register(COLD_OCEAN.getId().toString(), COLD_OCEAN);
        register(FROZEN_OCEAN.getId().toString(), FROZEN_OCEAN);
        register(LUKEWARM_OCEAN.getId().toString(), LUKEWARM_OCEAN);
        register(WARM_OCEAN.getId().toString(), WARM_OCEAN);
        register(OCEAN_BIOMES.getId().toString(), OCEAN_BIOMES);
        register(DEEP_OCEAN_BIOMES.getId().toString(), DEEP_OCEAN_BIOMES);
        register(BEACH_BIOMES.getId().toString(), BEACH_BIOMES);
        register(SNOWY_BIOMES.getId().toString(), SNOWY_BIOMES);
        register(TAIGA_BIOMES.getId().toString(), TAIGA_BIOMES);
        register(FOREST_BIOMES.getId().toString(), FOREST_BIOMES);
        register(PLAINS_BIOMES.getId().toString(), PLAINS_BIOMES);
        register(JUNGLE_BIOMES.getId().toString(), JUNGLE_BIOMES);
        register(SAVANNA_BIOMES.getId().toString(), SAVANNA_BIOMES);
        register(DESERT_BIOMES.getId().toString(), DESERT_BIOMES);
        register(LAND_BIOMES.getId().toString(), LAND_BIOMES);
        register(VILLAGE_BIOMES.getId().toString(), VILLAGE_BIOMES);
        register(HAS_TREES.getId().toString(), HAS_TREES);
        register(NO_TREES.getId().toString(), NO_TREES);
        register(MUSHROOM_ISLAND.getId().toString(), MUSHROOM_ISLAND);
    }

    static class CachingTag extends Tag<Biome> {
        private int version;
        private Tag<Biome> delegate;

        public CachingTag(final ResourceLocation id) {
            super(id);
            this.version = -1;
        }

        /**
         * Refreshes cached delegate tag when biome tags are updated.
         */
        @Override
        public boolean contains(final Biome entry) {
            if (this.version != BiomeTags.latestVersion) {
                this.delegate = BiomeTags.container.getOrCreate(this.getId());
                this.version = BiomeTags.latestVersion;
            }
            return this.delegate.contains(entry);
        }

        /**
         * Returns cached biome values and refreshes after tag reloads.
         */
        @Override
        public Collection<Biome> values() {
            if (this.version != BiomeTags.latestVersion) {
                this.delegate = BiomeTags.container.getOrCreate(this.getId());
                this.version = BiomeTags.latestVersion;
            }
            return this.delegate.values();
        }

        /**
         * Returns cached biome entries and refreshes after tag reloads.
         */
        @Override
        public Collection<Entry<Biome>> entries() {
            if (this.version != BiomeTags.latestVersion) {
                this.delegate = BiomeTags.container.getOrCreate(this.getId());
                this.version = BiomeTags.latestVersion;
            }
            return this.delegate.entries();
        }
    }
}
