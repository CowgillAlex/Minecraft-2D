package dev.alexco.minecraft.tag;

import javax.annotation.Nullable;
import com.google.gson.JsonParseException;

import dev.alexco.registry.ResourceLocation;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.util.Util;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;

import java.util.Optional;
import java.util.List;
import com.google.common.collect.Lists;
import java.util.Random;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Iterator;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Collection;
import java.util.Set;

public class Tag<T> {
    private final ResourceLocation id;
    private final Set<T> values;
    private final Collection<Entry<T>> entries;

    public Tag(final ResourceLocation id) {
        this.id = id;
        this.values = Collections.<T>emptySet();
        this.entries = Collections.emptyList();
    }

    public Tag(final ResourceLocation id, final Collection<Entry<T>> entries, final boolean ordered) {
        this.id = id;
        this.values = (Set<T>) (ordered ? Sets.newLinkedHashSet() : Sets.newHashSet());
        this.entries = entries;
        for (final Entry<T> lv : entries) {
            lv.build(this.values);
        }
    }

    /**
     * Serialises this tag back to json using either item ids or nested tag references.
     */
    public JsonObject toJson(final Function<T, ResourceLocation> idGetter) {
        final JsonObject jsonObject = new JsonObject();
        final JsonArray jsonArray = new JsonArray();
        for (final Entry<T> lv : this.entries) {
            lv.toJson(jsonArray, idGetter);
        }
        jsonObject.addProperty("replace", false);
        jsonObject.add("values", jsonArray);
        return jsonObject;
    }

    public boolean contains(final T entry) {
        return this.values.contains(entry);
    }

    public Collection<T> values() {
        return this.values;
    }

    public Collection<Entry<T>> entries() {
        return this.entries;
    }

    /**
     * Picks a random value from this tag using java.util.Random.
     */
    public T getRandom(final Random random) {
        final List<T> list = Lists.newArrayList(this.values());
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Picks a random value from this tag using the xoroshiro rng implementation.
     */
    public T getRandom(final Xoroshiro random) {
        final List<T> list = Lists.newArrayList(this.values());
        return list.get(random.nextInt(list.size()));
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public static class Builder<T> {
        private final Set<Entry<T>> entries;
        private boolean ordered;

        public Builder() {
            this.entries = Sets.newLinkedHashSet();
        }

        public static <T> Builder<T> create() {
            return new Builder<T>();
        }

        public Builder<T> add(final Entry<T> arg) {
            this.entries.add(arg);
            return this;
        }

        public Builder<T> add(final T object) {
            this.entries.add(new CollectionEntry<T>(Collections.<T>singleton(object)));
            return this;
        }

        @SafeVarargs
        public final Builder<T> add(final T... objects) {
            this.entries.add(new CollectionEntry<T>(Lists.<T>newArrayList(objects)));
            return this;
        }

        public Builder<T> add(final Tag<T> arg) {
            this.entries.add(new TagEntry<T>(arg));
            return this;
        }

        public Builder<T> ordered(final boolean bl) {
            this.ordered = bl;
            return this;
        }

        /**
         * Resolves referenced tags for every builder entry.
         */
        public boolean applyTagGetter(final Function<ResourceLocation, Tag<T>> function) {
            for (final Entry<T> lv : this.entries) {
                if (!lv.applyTagGetter(function)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Builds an immutable tag snapshot from collected entries.
         */
        public Tag<T> build(final ResourceLocation arg) {
           // String joined = String.join(", ", this.entries.stream().map(Object::toString).toList());
           // Logger.INFO(String.format("Tag: %s: %s", arg, joined));
            return new Tag<T>(arg, this.entries, this.ordered);

        }

        /**
         * Parses tag entries from tag json, resolving direct values immediately.
         */
        public Builder<T> fromJson(final Function<ResourceLocation, Optional<T>> function,
                final JsonObject jsonObject) {
            final JsonArray jsonArray = Util.getArray(jsonObject, "values");
            final List<Entry<T>> list = Lists.newArrayList();
            for (final JsonElement jsonElement : jsonArray) {
                final String string = Util.asString(jsonElement, "value");
                if (string.startsWith("#")) {
                    list.add(new TagEntry<T>(new ResourceLocation(string.substring(1))));
                } else {
                    final ResourceLocation lv = new ResourceLocation(string);
                    list.add(new CollectionEntry<T>(Collections.<T>singleton(
                            function.apply(lv)
                                    .orElseThrow(() -> new JsonParseException("Unknown value '" + lv + "'")))));
                }
            }
            if (Util.getBoolean(jsonObject, "replace", false)) {
                this.entries.clear();
            }
            this.entries.addAll(list);
            return this;
        }
    }

    public interface Entry<T> {
        default boolean applyTagGetter(final Function<ResourceLocation, Tag<T>> function) {
            return true;
        }

        void build(final Collection<T> collection);

        void toJson(final JsonArray jsonArray, final Function<T, ResourceLocation> function);
    }

    public static class CollectionEntry<T> implements Entry<T> {
        private final Collection<T> values;

        public CollectionEntry(final Collection<T> collection) {
            this.values = collection;
        }

        /**
         * Adds all concrete values represented by this entry.
         */
        @Override
        public void build(final Collection<T> collection) {
            collection.addAll(this.values);
        }

        /**
         * Writes all referenced concrete values to json.
         */
        @Override
        public void toJson(final JsonArray jsonArray, final Function<T, ResourceLocation> function) {
            for (final T object : this.values) {
                final ResourceLocation lv = function.apply(object);
                if (lv == null) {
                    throw new IllegalStateException("Unable to serialise an anonymous value to json!");
                }
                jsonArray.add(lv.toString());
            }
        }

        public Collection<T> getValues() {
            return this.values;
        }

@Override
public String toString() {
    return values.stream()
        .map(block -> {
            if (block instanceof Block b) {
                return b.getKey().toString();
            }
            return block.toString();
        })
        .collect(Collectors.joining(", ", "[", "]"));
}



    }

    public static class TagEntry<T> implements Entry<T> {
        @Nullable
        private final ResourceLocation id;
        @Nullable
        private Tag<T> tag;

        public TagEntry(final ResourceLocation arg) {
            this.id = arg;
        }

        public TagEntry(final Tag<T> arg) {
            this.id = arg.getId();
            this.tag = arg;
        }

        /**
         * Resolves the referenced tag id into a concrete tag instance.
         */
        @Override
        public boolean applyTagGetter(final Function<ResourceLocation, Tag<T>> function) {
            if (this.tag == null) {
                this.tag = function.apply(this.id);
            }
            return this.tag != null;
        }

        /**
         * Expands this tag reference into its concrete values.
         */
        @Override
        public void build(final Collection<T> collection) {
            if (this.tag == null) {
                throw new IllegalStateException("Cannot build unresolved tag entry");
            }
            collection.addAll(this.tag.values());
        }

        /**
         * Returns the id used for serialising this tag reference.
         */
        public ResourceLocation getId() {
            if (this.tag != null) {
                return this.tag.getId();
            }
            if (this.id != null) {
                return this.id;
            }
            throw new IllegalStateException("Cannot serialise an anonymous tag to json!");
        }

        @Override
        public void toJson(final JsonArray jsonArray, final Function<T, ResourceLocation> function) {
            jsonArray.add("#" + this.getId());
        }
    }
}
