package dev.alexco.minecraft.world.level.block.state.properties;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.alexco.minecraft.util.StringRepresentable;

public class EnumProperty<T extends Enum<T>>
extends AbstractProperty<T> {
    private final ImmutableSet<T> values;
    private final Map<String, T> names = Maps.newHashMap();

    protected EnumProperty(String string, Class<T> clazz, Collection<T> collection) {
        super(string, clazz);
        this.values = ImmutableSet.copyOf(collection);
        for (T enum_ : collection) {
            String string2 = ((StringRepresentable)(enum_)).getSerializedName();
            if (this.names.containsKey(string2)) {
                throw new IllegalArgumentException("Multiple values have the same name '" + string2 + "'");
            }
            this.names.put(string2, enum_);
        }
    }

    @Override
    public Collection<T> getPossibleValues() {
        return this.values;
    }

    @Override
    public Optional<T> getValue(String string) {
        return Optional.ofNullable(this.names.get(string));
    }

    @Override
    public String getName(T t) {
        return ((StringRepresentable)t).getSerializedName();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof EnumProperty && super.equals(object)) {
            EnumProperty enumProperty = (EnumProperty)object;
            return this.values.equals(enumProperty.values) && this.names.equals(enumProperty.names);
        }
        return false;
    }

    @Override
    public int generateHashCode() {
        int n = super.generateHashCode();
        n = 31 * n + this.values.hashCode();
        n = 31 * n + this.names.hashCode();
        return n;
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> clazz) {
        return EnumProperty.create(string, clazz, Predicates.alwaysTrue());
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> clazz, Predicate<T> predicate) {
        return EnumProperty.create(string, clazz, Arrays.stream(clazz.getEnumConstants()).filter(predicate).collect(Collectors.toList()));
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> clazz, T ... TArray) {
        return EnumProperty.create(string, clazz, (Predicate<T>) Lists.newArrayList((Object[])TArray));
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> clazz, Collection<T> collection) {
        return new EnumProperty<T>(string, clazz, collection);
    }
}

