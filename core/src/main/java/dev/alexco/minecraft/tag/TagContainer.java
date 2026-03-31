package dev.alexco.minecraft.tag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import dev.alexco.registry.ResourceLocation;

public class TagContainer<T> {
      private static final Gson GSON;
    private static final int JSON_EXTENSION_LENGTH;
    private Map<ResourceLocation, Tag<T>> entries;
    private final Function<ResourceLocation, Optional<T>> getter;
    private final String dataType;
    private final boolean ordered;
    private final String entryType;

      public TagContainer(final Function<ResourceLocation, Optional<T>> getter, final String dataType, final boolean ordered, final String entryType) {
        this.entries = ImmutableMap.of();
        this.getter = getter;
        this.dataType = dataType;
        this.ordered = ordered;
        this.entryType = entryType;
    }
    @Nullable
    public Tag<T> get(final ResourceLocation id) {
        return this.entries.get(id);
    }

    /**
     * Returns an existing tag or an empty placeholder when it is not registered.
     */
    public Tag<T> getOrCreate(final ResourceLocation id) {
        final Tag<T> lv = this.entries.get(id);
        if (lv == null) {
            return new Tag<T>(id);
        }
        return lv;
    }

    public Collection<ResourceLocation> getKeys() {
        return this.entries.keySet();
    }


    /**
     * Collects all tag ids that currently contain the supplied object.
     */
    public Collection<ResourceLocation> getTagsFor(final T object) {
        final List<ResourceLocation> list = Lists.newArrayList();
        for (final Map.Entry<ResourceLocation, Tag<T>> entry : this.entries.entrySet()) {
            if (entry.getValue().contains(object)) {
                list.add(entry.getKey());
            }
        }
        return list;
    }



    /**
     * Replaces the full tag mapping with a resolved immutable copy.
     */
    protected void method_20735(final Map<ResourceLocation, Tag<T>> map) {
        this.entries = ImmutableMap.copyOf(map);
    }

    public Map<ResourceLocation, Tag<T>> getEntries() {
        return this.entries;
    }

    static {

        GSON = new Gson();
        JSON_EXTENSION_LENGTH = ".json".length();
    }
}
