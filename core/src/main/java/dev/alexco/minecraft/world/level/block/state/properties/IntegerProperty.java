package dev.alexco.minecraft.world.level.block.state.properties;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class IntegerProperty extends AbstractProperty<Integer> {
     private final ImmutableSet<Integer> values;

    protected IntegerProperty(String string, int n, int n2) {
        super(string, Integer.class);
        if (n < 0) {
            throw new IllegalArgumentException("Min value of " + string + " must be 0 or greater");
        }
        if (n2 <= n) {
            throw new IllegalArgumentException("Max value of " + string + " must be greater than min (" + n + ")");
        }
        HashSet<Integer> hashSet = Sets.newHashSet();
        for (int i = n; i <= n2; ++i) {
            hashSet.add(i);
        }
        this.values = ImmutableSet.copyOf((Collection)hashSet);
    }

    @Override
    public Collection<Integer> getPossibleValues() {
        return this.values;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof IntegerProperty && super.equals(object)) {
            IntegerProperty integerProperty = (IntegerProperty)object;
            return this.values.equals(integerProperty.values);
        }
        return false;
    }

    @Override
    public int generateHashCode() {
        return 31 * super.generateHashCode() + this.values.hashCode();
    }

    public static IntegerProperty create(String string, int min, int max) {
        return new IntegerProperty(string, min, max);
    }

    @Override
    public Optional<Integer> getValue(String string) {
        try {
            Integer n = Integer.valueOf(string);
            return this.values.contains((Object)n) ? Optional.of(n) : Optional.empty();
        }
        catch (NumberFormatException numberFormatException) {
            return Optional.empty();
        }
    }

    @Override
    public String getName(Integer n) {
        return n.toString();
    }
}
