package dev.alexco.minecraft.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.registry.ResourceLocation;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.Blocks;

public class BlockTags {
    private static TagContainer<Block> container;
    private static int latestVersion;
    private static volatile boolean loaded = false;

    public static final Tag<Block> STONE = new CachingTag(new ResourceLocation("minecraft", "stone"));
    public static final Tag<Block> UNBREAKABLE = new CachingTag(new ResourceLocation("minecraft", "unbreakable"));
    public static final Tag<Block> UNSELECTABLE = new CachingTag(new ResourceLocation("minecraft", "unselectable"));
    public static final Tag<Block> FLUID = new CachingTag(new ResourceLocation("minecraft", "fluid"));
    public static final Tag<Block> CAN_PLACE_THROUGH = new CachingTag(new ResourceLocation("minecraft", "can_place_through"));
    public static final Tag<Block> WORLD_GEN_REPLACEABLE = new CachingTag(new ResourceLocation("minecraft", "world_gen_replaceable"));
    public static final Tag<Block> WORLD_GEN_STRUCTURE_REPLACEABLE = new CachingTag(new ResourceLocation("minecraft", "world_gen_structure_replaceable"));
    public static final Tag<Block> LEAVES = new CachingTag(new ResourceLocation("minecraft", "leaves"));
    public static final Tag<Block> GREEN_TINT = new CachingTag(new ResourceLocation("minecraft", "needs_tinting"));

    public static final Tag<Block> MINEABLE_AXE = new CachingTag(new ResourceLocation("minecraft", "mineable_axe"));
    public static final Tag<Block> MINEABLE_PICKAXE = new CachingTag(new ResourceLocation("minecraft", "mineable_pickaxe"));
    public static final Tag<Block> MINEABLE_HOE = new CachingTag(new ResourceLocation("minecraft", "mineable_hoe"));
    public static final Tag<Block> MINEABLE_SWORD = new CachingTag(new ResourceLocation("minecraft", "mineable_sword"));
    public static final Tag<Block> MINEABLE_SHOVEL = new CachingTag(new ResourceLocation("minecraft", "mineable_shovel"));
    public static final Tag<Block> MINEABLE_FIST = new CachingTag(new ResourceLocation("minecraft", "mineable_fist"));

    public static final Tag<Block> NEEDS_FIST_TOOL = new CachingTag(new ResourceLocation("minecraft", "needs_fist_tool"));
    public static final Tag<Block> NEEDS_WOOD_TOOL = new CachingTag(new ResourceLocation("minecraft", "needs_wood_tool"));
    public static final Tag<Block> NEEDS_STONE_TOOL = new CachingTag(new ResourceLocation("minecraft", "needs_stone_tool"));
    public static final Tag<Block> NEEDS_IRON_TOOL = new CachingTag(new ResourceLocation("minecraft", "needs_iron_tool"));
    public static final Tag<Block> NEEDS_DIAMOND_TOOL = new CachingTag(new ResourceLocation("minecraft", "needs_diamond_tool"));
    public static final Tag<Block> PLANKS = new CachingTag(new ResourceLocation("minecraft", "planks"));
    public static final Tag<Block> FARMABLE_BLOCKS = new CachingTag(new ResourceLocation("minecraft", "farmable_blocks"));
    public static final Tag<Block> DROPS_GRASS_ON_HOE = new CachingTag(new ResourceLocation("minecraft", "drops_grass_on_hoe"));
    public static final Tag<Block> NEEDS_BASE_SUPPORT = new CachingTag(new ResourceLocation("minecraft", "needs_base_support"));

    public static void setContainer(final TagContainer<Block> container) {
        BlockTags.container = container;
        ++BlockTags.latestVersion;
    }

    public static TagContainer<Block> getContainer() {
        return BlockTags.container;
    }

    public static Tag<Block> getTag(ResourceLocation id) {
        return BlockTags.container.getOrCreate(id);
    }

    /**
     * Loads block tags from defaults plus json resources under tags/blocks.
     */
    public static void loadTags() {
        if (loaded) {
            return;
        }
        synchronized (BlockTags.class) {
            if (loaded) {
                return;
            }
            Map<ResourceLocation, Tag.Builder<Block>> builders = createDefaultBuilders();

        FileHandle assetHandle = Gdx.files.internal("assets.txt");
        if (assetHandle.exists()) {
            String[] lines = assetHandle.readString().split("\n");
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (!line.startsWith("tags/blocks/") || !line.endsWith(".json")) {
                    continue;
                }

                try {
                    FileHandle tagFile = Gdx.files.internal(line);
                    if (!tagFile.exists()) {
                        continue;
                    }
                    JsonObject json = JsonParser.parseString(tagFile.readString()).getAsJsonObject();
                    ResourceLocation id = idFromPath(line);
                    Tag.Builder<Block> builder = builders.computeIfAbsent(id, key -> Tag.Builder.create());
                    builder.fromJson(key -> Optional.ofNullable(Registry.BLOCK.get(key)), json);
                } catch (Exception e) {
                    Logger.ERROR("Failed to load block tag %s: %s", line, e.getMessage());
                }
            }
        }

        Map<ResourceLocation, Tag<Block>> resolved = resolveBuilders(builders);
        BlockTags.container.method_20735(resolved);
        ++BlockTags.latestVersion;
        loaded = true;
        Logger.INFO("Loaded %d block tags", resolved.size());
        }
    }

    static {
        BlockTags.container = new TagContainer<Block>(id -> Optional.ofNullable(Registry.BLOCK.get(id)), "blocks", false, "block");
    }

    /**
     * Seeds builtin block tags used when no external data is present.
     */
    private static Map<ResourceLocation, Tag.Builder<Block>> createDefaultBuilders() {
        Map<ResourceLocation, Tag.Builder<Block>> builders = new HashMap<>();
        builders.put(new ResourceLocation("minecraft", "stone"), Tag.Builder.<Block>create().add(Blocks.DEEPSLATE, Blocks.STONE));
        builders.put(new ResourceLocation("minecraft", "unbreakable"), Tag.Builder.<Block>create().add(Blocks.BEDROCK, Blocks.AIR, Blocks.WATER));
        builders.put(new ResourceLocation("minecraft", "unselectable"), Tag.Builder.<Block>create().add(Blocks.AIR, Blocks.WATER));
        builders.put(new ResourceLocation("minecraft", "fluid"), Tag.Builder.<Block>create().add(Blocks.WATER, Blocks.FLOWING_WATER));
        builders.put(new ResourceLocation("minecraft", "can_place_through"), Tag.Builder.<Block>create().add(Blocks.WATER, Blocks.FLOWING_WATER, Blocks.AIR));
        builders.put(new ResourceLocation("minecraft", "world_gen_replaceable"), Tag.Builder.<Block>create().add(Blocks.STONE, Blocks.DEEPSLATE, Blocks.DIRT));
        builders.put(new ResourceLocation("minecraft", "world_gen_structure_replaceable"), Tag.Builder.<Block>create().add(Blocks.STONE, Blocks.DEEPSLATE, Blocks.DIRT, Blocks.AIR));
        builders.put(new ResourceLocation("minecraft", "leaves"), Tag.Builder.<Block>create().add(Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES));
        builders.put(new ResourceLocation("minecraft", "needs_tinting"), Tag.Builder.<Block>create().add(new Tag.TagEntry<>(new ResourceLocation("minecraft", "leaves"))));

        builders.put(new ResourceLocation("minecraft", "mineable_axe"), Tag.Builder.<Block>create());
        builders.put(new ResourceLocation("minecraft", "mineable_pickaxe"), Tag.Builder.<Block>create());
        builders.put(new ResourceLocation("minecraft", "mineable_hoe"), Tag.Builder.<Block>create());
        builders.put(new ResourceLocation("minecraft", "mineable_sword"), Tag.Builder.<Block>create());
        builders.put(new ResourceLocation("minecraft", "mineable_fist"), Tag.Builder.<Block>create());
        builders.put(new ResourceLocation("minecraft", "needs_fist_tool"), Tag.Builder.<Block>create());
        builders.put(new ResourceLocation("minecraft", "needs_stone_tool"), Tag.Builder.<Block>create());
        builders.put(new ResourceLocation("minecraft", "needs_iron_tool"), Tag.Builder.<Block>create());
        builders.put(new ResourceLocation("minecraft", "needs_diamond_tool"), Tag.Builder.<Block>create());
        builders.put(new ResourceLocation("minecraft", "planks"), Tag.Builder.<Block>create());
        return builders;
    }

    /**
     * Resolves inter-tag references by repeatedly building tags whose dependencies are ready.
     */
    private static Map<ResourceLocation, Tag<Block>> resolveBuilders(Map<ResourceLocation, Tag.Builder<Block>> builders) {
        Map<ResourceLocation, Tag<Block>> resolved = new HashMap<>();
        Set<ResourceLocation> unresolved = new HashSet<>(builders.keySet());

        while (!unresolved.isEmpty()) {
            boolean progressed = false;
            List<ResourceLocation> resolvedThisPass = new ArrayList<>();

            for (ResourceLocation id : unresolved) {
                Tag.Builder<Block> builder = builders.get(id);
                if (builder == null) {
                    resolvedThisPass.add(id);
                    progressed = true;
                    continue;
                }
                if (builder.applyTagGetter(resolved::get)) {
                    resolved.put(id, builder.build(id));
                    resolvedThisPass.add(id);
                    progressed = true;
                }
            }

            unresolved.removeAll(resolvedThisPass);
            if (!progressed) {
                for (ResourceLocation id : unresolved) {
                    Logger.ERROR("Unresolved block tag dependencies for %s", id);
                }
                break;
            }
        }

        return resolved;
    }

    /**
     * Converts a tag file path into its minecraft namespace id.
     */
    private static ResourceLocation idFromPath(String path) {
        String local = path.substring("tags/blocks/".length(), path.length() - ".json".length());
        return new ResourceLocation("minecraft", local);
    }

    static class CachingTag extends Tag<Block> {
        private int version;
        private Tag<Block> delegate;

        public CachingTag(final ResourceLocation id) {
            super(id);
            this.version = -1;
        }

        /**
         * Refreshes cached tag data when the global tag version changes.
         */
        @Override
        public boolean contains(final Block entry) {
            if (this.version != BlockTags.latestVersion) {
                this.delegate = BlockTags.container.getOrCreate(this.getId());
                this.version = BlockTags.latestVersion;
            }
            return this.delegate.contains(entry);
        }

        /**
         * Returns cached values, rebuilding the delegate when tags were reloaded.
         */
        @Override
        public Collection<Block> values() {
            if (this.version != BlockTags.latestVersion) {
                this.delegate = BlockTags.container.getOrCreate(this.getId());
                this.version = BlockTags.latestVersion;
            }
            return this.delegate.values();
        }

        /**
         * Returns cached entries, rebuilding the delegate when tags were reloaded.
         */
        @Override
        public Collection<Entry<Block>> entries() {
            if (this.version != BlockTags.latestVersion) {
                this.delegate = BlockTags.container.getOrCreate(this.getId());
                this.version = BlockTags.latestVersion;
            }
            return this.delegate.entries();
        }
    }
}
