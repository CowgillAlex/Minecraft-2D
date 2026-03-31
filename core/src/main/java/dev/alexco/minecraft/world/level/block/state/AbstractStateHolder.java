package dev.alexco.minecraft.world.level.block.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import dev.alexco.minecraft.world.level.block.state.properties.Property;

public class AbstractStateHolder<O, S> implements StateHolder<S> {
  private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Map.Entry<Property<?>, Comparable<?>>, String>(){

        @Override
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            }
            Property<?> property = entry.getKey();
            return property.getName() + "=" + this.getName(property, entry.getValue());
        }

        private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
            return property.getName((T)comparable);
        }


    };

     protected final O owner;
    private final ImmutableMap<Property<?>, Comparable<?>> values;
    private final int hashCode;
    private Table<Property<?>, Comparable<?>, S> neighbours;

    protected AbstractStateHolder(O o, ImmutableMap<Property<?>, Comparable<?>> immutableMap) {
        this.owner = o;
        this.values = immutableMap;
        this.hashCode = immutableMap.hashCode();
    }
    /**
     * Cycles a property
     * @param <T> the property to cycle
     * @param property the property to cycle
     * @return the VALUE of the next property
     * @implNote THEYRE IMMUTABLE SO MAKE SURE TO SET THE PROPERTY, NOT LOOK IT UP
     */
    public <T extends Comparable<T>> S cycle(Property<T> property) {
        return this.setValue(property, (T) AbstractStateHolder.findNextInCollection(property.getPossibleValues(), this.getValue(property)));
    }

    /**
     * Returns the element after the current one, wrapping at collection end.
     */
    protected static <T> T findNextInCollection(Collection<T> collection, T t) {
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().equals(t)) continue;
            if (iterator.hasNext()) {
                return iterator.next();
            }
            return collection.iterator().next();
        }
        return iterator.next();
    }

/**
 * Cycles a property forward or backward.
 */
public <T extends Comparable<T>> S cycle(Property<T> property, boolean reverse) {
    if (reverse) {
        return this.setValue(property, (T) AbstractStateHolder.findPreviousInCollection(property.getPossibleValues(), this.getValue(property)));
    } else {
        return this.cycle(property);
    }
}

/**
 * Returns the element before the current one, wrapping at start.
 */
protected static <T> T findPreviousInCollection(Collection<T> collection, T t) {
    List<T> list = new ArrayList<>(collection);
    int index = list.indexOf(t);
    if (index == -1) return list.get(0);
    int previousIndex = (index - 1 + list.size()) % list.size();
    return list.get(previousIndex);
}
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        if (!this.getValues().isEmpty()) {
            stringBuilder.append('[');
            stringBuilder.append(this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    public Collection<Property<?>> getProperties() {
        return Collections.unmodifiableCollection(this.values.keySet());
    }

    public <T extends Comparable<T>> boolean hasProperty(Property<T> property) {
        return this.values.containsKey(property);
    }

    @Override
    public <T extends Comparable<T>> T getValue(Property<T> property) {
        Comparable<T> comparable = (Comparable)this.values.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + this.owner);
        }
        return (T)((Comparable)property.getValueClass().cast(comparable));
    }

    @Override
    public <T extends Comparable<T>, V extends T> S setValue(Property<T> property, V v) {
        Comparable<T> comparable = (Comparable)this.values.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.owner);
        }
        if (comparable == v) {
            return (S)this;
        }
        Object object = this.neighbours.get(property, v);
        if (object == null) {
            throw new IllegalArgumentException("Cannot set property " + property + " to " + v + " on " + this.owner + ", it is not an allowed value");
        }
        return (S)object;
    }

    /**
     * Precomputes neighbouring states for property value transitions.
     */
    public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> map) {
        if (this.neighbours != null) {
            throw new IllegalStateException();
        }
        HashBasedTable hashBasedTable = HashBasedTable.create();
        for (Map.Entry<Property<?>, Comparable<?>> entry : this.values.entrySet()) {
            Property<?> property = entry.getKey();
            for (Comparable<?> comparable : property.getPossibleValues()) {
                if (comparable == entry.getValue()) continue;
                hashBasedTable.put(property, comparable, map.get(this.makeNeighbourValues(property, comparable)));
            }
        }
        this.neighbours = hashBasedTable.isEmpty() ? hashBasedTable : ArrayTable.create((Table)hashBasedTable);
    }

    private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> property, Comparable<?> comparable) {
        HashMap hashMap = Maps.newHashMap(this.values);
        hashMap.put(property, comparable);
        return hashMap;
    }

    @Override
    public ImmutableMap<Property<?>, Comparable<?>> getValues() {
        return this.values;
    }

    public boolean equals(Object object) {
        return this == object;
    }

    public int hashCode() {
        return this.hashCode;
    }
}
