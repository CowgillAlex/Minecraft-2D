package dev.alexco.minecraft.world.level.block.state;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import dev.alexco.minecraft.world.level.block.state.properties.Property;
public interface StateHolder<C> {
    public <T extends Comparable<T>> T getValue(Property<T> property);
    public <T extends Comparable<T>, V extends T> C setValue(Property<T> var1, V var2);
    public ImmutableMap<Property<?>, Comparable<?>> getValues();
    public static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
        return property.getName((T) comparable);
    }

    /**
     * Parses and applies a property value string if valid.
     */
     public static <S extends StateHolder<S>, T extends Comparable<T>> S setValueHelper(S s, Property<T> property, String propertyS, String input, String val) {
        Optional<T> optional = property.getValue(input);
        if (optional.isPresent()) {
            return s.setValue(property, optional.get());
        }
        System.err.println(String.format("Unable to read property: %s with value: %s for input: %s", propertyS, val, input));
        return s;
    }
}
