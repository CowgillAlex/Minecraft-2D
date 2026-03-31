package dev.alexco.minecraft.loot;

import java.util.Map;
import java.util.Optional;

import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.state.properties.Property;

public class BlockStateCondition implements LootCondition {
    private final Map<String, String> expectedProperties;
    private final boolean shouldMatch;

    public BlockStateCondition(Map<String, String> expectedProperties) {
        this(expectedProperties, true);
    }

    public BlockStateCondition(Map<String, String> expectedProperties, boolean shouldMatch) {
        this.expectedProperties = expectedProperties;
        this.shouldMatch = shouldMatch;
    }

    /**
     * Checks whether the current block state matches every configured property value.
     */
    @Override
    public boolean test(LootContext context) {
        BlockState blockState = context.getBlockState();
        if (blockState == null) {
            return false;
        }

        boolean matched = true;
        for (Map.Entry<String, String> expected : expectedProperties.entrySet()) {
            Property<?> property = findPropertyByName(blockState, expected.getKey());
            if (property == null) {
                matched = false;
                break;
            }

            if (!matchesExpectedValue(blockState, property, expected.getValue())) {
                matched = false;
                break;
            }
        }
        return shouldMatch ? matched : !matched;
    }

    /**
     * Finds a state property by name on the supplied block state.
     */
    private Property<?> findPropertyByName(BlockState blockState, String propertyName) {
        for (Property<?> property : blockState.getProperties()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Parses and compares a raw expected value string against the state's actual value.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean matchesExpectedValue(BlockState state, Property property, String expectedValueRaw) {
        Optional parsedExpected = property.getValue(expectedValueRaw);
        if (parsedExpected.isEmpty()) {
            return false;
        }
        Comparable expectedValue = (Comparable) parsedExpected.get();
        Comparable actualValue = state.getValue(property);
        return actualValue.equals(expectedValue);
    }
}
