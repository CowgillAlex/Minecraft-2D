package dev.alexco.minecraft.world.level.item;

import java.util.Map;

import javax.annotation.Nullable;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Maps;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.util.Util;
import dev.alexco.minecraft.world.level.block.Block;

public class Item {
    public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
    private final int maxStackSize;
    private final int maxDamage;
    @Nullable
    private String descriptionId;
    @Nullable
    private final Vector2 atlasCoords;

    public Item(Properties properties) {
        this.maxDamage = properties.maxDamage;
        this.maxStackSize = properties.maxStackSize;
        this.atlasCoords = properties.atlasCoords;
    }

    @Nullable
    public Vector2 getAtlasCoords() {
        return atlasCoords;
    }

    public final int getMaxStackSize() {
        return this.maxStackSize;
    }

    public final int getMaxDamage() {
        return this.maxDamage;
    }

    public boolean canBeDepleted() {
        return this.maxDamage > 0;
    }

    /**
     * Lazily builds the translation-style description id for this item.
     */
    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("item", Registry.ITEM.getKey(this));
        }
        return this.descriptionId;
    }

    public String toString() {
        return Registry.ITEM.getKey(this).getPath();
    }

    /**
     * @return the descriptionId
     */
    public String getDescriptionId() {
        return descriptionId;
    }

    public static class Properties {
        private int maxStackSize = 64;
        private int maxDamage;
        private Item craftingRemainingItem;
        @Nullable
        private Vector2 atlasCoords;

        /**
         * Sets max stack size and rejects stackable + damageable combinations.
         */
        public Properties stacksTo(int to) {
            if (this.maxDamage > 0) {
                throw new RuntimeException("Cannot be both stackable and damageable");
            }
            this.maxStackSize = to;
            return this;

        }

        /**
         * Applies default durability only if explicit durability is still unset.
         */
        public Properties defaultDurability(int durability) {
            return this.maxDamage == 0 ? this.durability(durability) : this;

        }

        public Properties durability(int n) {
            this.maxDamage = n;
            this.maxStackSize = 1;
            return this;
        }

        public Properties craftRemainder(Item item) {
            this.craftingRemainingItem = item;
            return this;
        }

        public Properties atlas(float x, float y) {
            this.atlasCoords = new Vector2(x, y);
            return this;
        }

    }

}
