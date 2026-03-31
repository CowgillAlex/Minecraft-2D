package dev.alexco.minecraft.blaze2d.special;

import dev.alexco.minecraft.phys.AABB;
import dev.alexco.minecraft.world.level.block.Solidity;

import java.util.ArrayList;
import java.util.List;

public class SolidityAABB {
    public AABB boundingbox;
    public Solidity solidity;

    // Public constructor for creating fresh instances
    public SolidityAABB(AABB boundingBox, Solidity solidness) {
        this.boundingbox = boundingBox;
        this.solidity = solidness;
    }

    /**
     * Create a fresh SolidityAABB instance (not from pool)
     */
    public static SolidityAABB create(AABB boundingBox, Solidity solidness) {
        return new SolidityAABB(boundingBox, solidness);
    }

    /**
     * Get a SolidityAABB instance from the pool (DEPRECATED - use create() instead)
     * @deprecated This method pools instances which causes issues when multiple callers share them.
     * Use {@link #create(AABB, Solidity)} instead.
     */
    @Deprecated
    public static SolidityAABB acquire(AABB boundingBox, Solidity solidness) {
        return new SolidityAABB(boundingBox, solidness);
    }

    /**
     * Return this instance to the pool for reuse (DEPRECATED - no-op now)
     * @deprecated Pooling is deprecated. Instances are now created fresh and will be GC'd.
     */
    @Deprecated
    public void release() {
        // No-op - we no longer pool instances
    }

    /**
     * Clear the entire pool (no-op now)
     */
    public static void clearPool() {
        // No-op - pooling disabled
    }

    /**
     * Get current pool size (returns -1 to indicate pooling disabled)
     */
    public static int getPoolSize() {
        return -1;
    }
}
