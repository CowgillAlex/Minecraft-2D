package dev.alexco.minecraft.phys;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Reuses AABB instances to reduce allocation churn during collision checks.
 */
public class AABBPool {
    private final Deque<AABB> pool;
    private final int MAX_POOL_SIZE = 50000;
    public static AABBPool AABBpool = new AABBPool();

    // Debug counters
    private static int totalAcquired = 0;
    private static int totalReleased = 0;

    public AABBPool() {
        pool = new ArrayDeque<>(MAX_POOL_SIZE);
        warmUp(MAX_POOL_SIZE);
    }

    /**
     * Returns an AABB from the pool, creating one if the pool is empty.
     */
    public AABB get(double x0, double y0, double x1, double y1) {
        totalAcquired++;
        AABB aabb = pool.pollFirst(); // O(1) operation
        if (aabb == null) {
            return new AABB(x0, y0, x1, y1);
        }

        aabb.set(x0, y0, x1, y1); // Reset the values
        return aabb;
    }

    /**
     * Returns an AABB to the pool for future reuse.
     */
    public void release(AABB aabb) {
        if (aabb == null) return; // Safety check

        totalReleased++;

        if (pool.size() < MAX_POOL_SIZE) {
            pool.offerFirst(aabb); // O(1) operation
        }
        // If pool is full, just let the AABB be garbage collected
    }

    /**
     * Pre-populates the pool up to the requested count.
     */
    public void warmUp(int count) {
        count = Math.min(count, MAX_POOL_SIZE);
        for (int i = 0; i < count; i++) {
            pool.offer(new AABB(0, 0, 0, 0));
        }
    }

    /**
     * Returns the current number of pooled AABB instances.
     */
    public int getPoolSize() {
        return pool.size();
    }

    // Debug getters
    public static int getTotalAcquired() {
        return totalAcquired;
    }

    public static int getTotalReleased() {
        return totalReleased;
    }

    public static int getNetChange() {
        return totalAcquired - totalReleased;
    }
}
