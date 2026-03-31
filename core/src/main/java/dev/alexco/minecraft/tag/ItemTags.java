package dev.alexco.minecraft.tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import dev.alexco.registry.ResourceLocation;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.Items;

public class ItemTags {
    private static TagContainer<Item> container;
    private static int latestVersion;

    public static final Tag<Item> PLANKS = Tag.Builder.<Item>create()
        .add(Items.OAK_PLANKS, Items.BAMBOO_PLANKS, Items.JUNGLE_PLANKS)
        .build(new ResourceLocation("minecraft", "planks"));

    public static final Tag<Item> LOGS = Tag.Builder.<Item>create()
        .add(Items.OAK_LOG, Items.BAMBOO_BLOCK, Items.JUNGLE_LOG)
        .build(new ResourceLocation("minecraft", "logs"));

    public static final Tag<Item> HELMETS = Tag.Builder.<Item>create().add(Items.GOLD_HELMET).add(Items.IRON_HELMET).add(Items.DIAMOND_HELMET).add(Items.NETHERITE_HELMET).build(new ResourceLocation("minecraft", "helmets"));
    public static final Tag<Item> CHESTPLATES = Tag.Builder.<Item>create().add(Items.GOLD_CHESTPLATE).add(Items.IRON_CHESTPLATE).add(Items.DIAMOND_CHESTPLATE).add(Items.NETHERITE_CHESTPLATE).build(new ResourceLocation("minecraft", "chestplates"));
    public static final Tag<Item> LEGGINGS = Tag.Builder.<Item>create().add(Items.GOLD_LEGGINGS).add(Items.IRON_LEGGINGS).add(Items.DIAMOND_LEGGINGS).add(Items.NETHERITE_LEGGINGS).build(new ResourceLocation("minecraft", "leggings"));
    public static final Tag<Item> BOOTS = Tag.Builder.<Item>create().add(Items.GOLD_BOOTS).add(Items.IRON_BOOTS).add(Items.DIAMOND_BOOTS).add(Items.NETHERITE_BOOTS).build(new ResourceLocation("minecraft", "boots"));

    public static final Tag<Item> FURNACE_FUELS = Tag.Builder.<Item>create()
        .add(Items.COAL)
        .add(Items.CHARCOAL)
        .add(Items.OAK_LOG)
        .add(Items.BAMBOO_BLOCK)
        .add(Items.JUNGLE_LOG)
        .add(Items.OAK_PLANKS)
        .add(Items.BAMBOO_PLANKS)
        .add(Items.JUNGLE_PLANKS)
        .add(Items.STICK)
        .build(new ResourceLocation("minecraft", "furnace_fuels"));

    public static void setContainer(final TagContainer<Item> container) {
        ItemTags.container = container;
        ++ItemTags.latestVersion;
    }

    public static TagContainer<Item> getContainer() {
        return ItemTags.container;
    }

    /**
     * Inserts or replaces one tag entry in the shared item tag container.
     */
    private static Tag<Item> register(final String id, Tag<Item> tag) {
        Map<ResourceLocation, Tag<Item>> map = new HashMap<>(ItemTags.container.getEntries());
        map.put(tag.getId(), tag);
        ItemTags.container.method_20735(map);
        return tag;
    }

    static {
        ItemTags.container = new TagContainer<Item>(arg -> Optional.empty(), "", false, "");
        register(PLANKS.getId().toString(), PLANKS);
        register(LOGS.getId().toString(), LOGS);
        register(HELMETS.getId().toString(), HELMETS);
        register(CHESTPLATES.getId().toString(), CHESTPLATES);
        register(LEGGINGS.getId().toString(), LEGGINGS);
        register(BOOTS.getId().toString(), BOOTS);
        register(FURNACE_FUELS.getId().toString(), FURNACE_FUELS);
    }

    public static Tag<Item> getTag(ResourceLocation id) {
        return ItemTags.container.getOrCreate(id);
    }

    public static boolean isInTag(Item item, Tag<Item> tag) {
        return tag.contains(item);
    }

    static class CachingTag extends Tag<Item> {
        private int version;
        private Tag<Item> delegate;

        public CachingTag(final ResourceLocation id) {
            super(id);
            this.version = -1;
        }

        /**
         * Refreshes the delegate tag when the global item tag version changes.
         */
        @Override
        public boolean contains(final Item entry) {
            if (this.version != ItemTags.latestVersion) {
                this.delegate = ItemTags.container.getOrCreate(this.getId());
                this.version = ItemTags.latestVersion;
            }
            return this.delegate.contains(entry);
        }

        /**
         * Returns cached values and rebuilds the delegate when tags are reloaded.
         */
        @Override
        public Collection<Item> values() {
            if (this.version != ItemTags.latestVersion) {
                this.delegate = ItemTags.container.getOrCreate(this.getId());
                this.version = ItemTags.latestVersion;
            }
            return this.delegate.values();
        }

        /**
         * Returns cached entries and rebuilds the delegate when tags are reloaded.
         */
        @Override
        public Collection<Entry<Item>> entries() {
            if (this.version != ItemTags.latestVersion) {
                this.delegate = ItemTags.container.getOrCreate(this.getId());
                this.version = ItemTags.latestVersion;
            }
            return this.delegate.entries();
        }
    }
}
