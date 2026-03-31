package dev.alexco.minecraft.world.level.block.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.alexco.minecraft.util.MapFiller;
import dev.alexco.minecraft.world.level.block.state.properties.Property;

public class StateDefinition<O, S extends StateHolder<S>> {
  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private final O owner;
    private final ImmutableSortedMap<String, Property<?>> propertiesByName;
    private final ImmutableList<S> states;

         /**
            * Builds every valid state permutation for the owner's properties.
            */
     protected <A extends AbstractStateHolder<O, S>> StateDefinition(O o, Factory<O, S, A> factory, Map<String, Property<?>> map) {
        this.owner = o;
        this.propertiesByName = ImmutableSortedMap.copyOf(map);
        LinkedHashMap linkedHashMap = Maps.newLinkedHashMap();
        ArrayList arrayList = Lists.newArrayList();
        Stream<List<List<Object>>> stream = Stream.of(Collections.emptyList());
        for (Object object : this.propertiesByName.values()) {
            stream = stream.flatMap(arg_0 -> StateDefinition.lambda$new$1((Property)object, arg_0));
        }
        stream.forEach(list2 -> {
            Map map2 = MapFiller.linkedHashMapFrom(this.propertiesByName.values(), list2);
            Object a = factory.create(o, ImmutableMap.copyOf(map2));
            linkedHashMap.put(map2, a);
            arrayList.add(a);
        });
        for (Object object : arrayList) {
            ((AbstractStateHolder)object).populateNeighbours(linkedHashMap);
        }
        this.states = ImmutableList.copyOf((Iterable)arrayList);
    }

    public ImmutableList<S> getPossibleStates() {
        return this.states;
    }

    public S any() {
        return (S)((StateHolder)this.states.get(0));
    }

    public O getOwner() {
        return this.owner;
    }

    public Collection<Property<?>> getProperties() {
        return this.propertiesByName.values();
    }

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("block", this.owner).add("properties", this.propertiesByName.values().stream().map(Property::getName).collect(Collectors.toList())).toString();
    }

    @Nullable
    public Property<?> getProperty(String string) {
        return (Property)this.propertiesByName.get((Object)string);
    }

    private static /* synthetic */ Stream lambda$new$1(Property property, List list) {
        return property.getPossibleValues().stream().map(comparable -> {
            ArrayList arrayList = Lists.newArrayList((Iterable)list);
            arrayList.add(comparable);
            return arrayList;
        });
    }

    public static class Builder<O, S extends StateHolder<S>> {
        private final O owner;
        private final Map<String, Property<?>> properties = Maps.newHashMap();

        public Builder(O o) {
            this.owner = o;
        }

        public Builder<O, S> add(Property<?> ... propertyArray) {
            for (Property<?> property : propertyArray) {
                this.validateProperty(property);
                this.properties.put(property.getName(), property);
            }
            return this;
        }

        /**
         * Validates property naming, values and duplicate registration.
         */
        private <T extends Comparable<T>> void validateProperty(Property<T> property) {
            String string = property.getName();
            if (!NAME_PATTERN.matcher(string).matches()) {
                throw new IllegalArgumentException(this.owner + " has invalidly named property: " + string);
            }
            Collection<T> collection = property.getPossibleValues();
            if (collection.size() <= 1) {
                throw new IllegalArgumentException(this.owner + " attempted use property " + string + " with <= 1 possible values");
            }
            for (Comparable<T> comparable : collection) {
                String string2 = property.getName((T)comparable);
                if (NAME_PATTERN.matcher(string2).matches()) continue;
                throw new IllegalArgumentException(this.owner + " has property: " + string + " with invalidly named value: " + string2);
            }
            if (this.properties.containsKey(string)) {
                throw new IllegalArgumentException(this.owner + " has duplicate property: " + string);
            }
        }

        /**
         * Creates an immutable state definition from the collected properties.
         */
        public <A extends AbstractStateHolder<O, S>> StateDefinition<O, S> create(Factory<O, S, A> factory) {
            return new StateDefinition<O, S>(this.owner, factory, this.properties);
        }
    }

    public static interface Factory<O, S extends StateHolder<S>, A extends AbstractStateHolder<O, S>> {
        public A create(O var1, ImmutableMap<Property<?>, Comparable<?>> var2);
    }
}
