package dev.alexco.minecraft.world.level.levelgen.structure;

import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Defines rules for where and how often a structure should spawn
 */
public class StructurePlacement {
    private final Structure structure;
    /**Chunks between the structure attempts */
    private final int spacing;        // Chunks between structure attempts
    /**Minimum number of chukns between the structures */
    private final int separation;     // Minimum chunks between structures
    public final float spawnChance;  // 0.0 to 1.0, chance to spawn when conditions met

    // Placement constraints
    private int minY = 0;
    private int maxY = 256;
    private boolean needsSolidGround = true;
    private boolean needsAir = true;
    private Set<Block> validGroundBlocks = new HashSet<>();
    private Predicate<StructurePlacementContext> customCondition = null;
    private PlacementMode placementMode = PlacementMode.SURFACE;
    private boolean allowChaining = false;
    private int maxChainDistance = 50; // Max blocks between structures in a chain

    /**
     * Defines where structures can spawn
     */
    public enum PlacementMode {
        SURFACE,           // On top of ground (air above, solid below)
        UNDERGROUND,       // Buried in solid blocks
        CAVE,              // In air pockets underground (air above and below)
        ANYWHERE,          // Any valid location within Y range
        /**Follows the contours of the surface of the terrain, like a path */
        TERRAIN_FOLLOWING
    }
    /**
     * Creates a structure placement
     * @param structure The structure to place
     * @param spacing how many chunks in between structure attempts
     * @param separation minimum number of chunks apart to try place
     * @param spawnChance how likely it is to succeed, from 0-1f
     */
    public StructurePlacement(Structure structure, int spacing, int separation, float spawnChance) {
        this.structure = structure;
        this.spacing = spacing;
        this.separation = separation;
        this.spawnChance = Math.max(0f, Math.min(1f, spawnChance));

        // Auto-detect terrain-following structures
        if (structure instanceof TerrainFollowingStructure) {
            TerrainFollowingStructure tfs = (TerrainFollowingStructure) structure;
            if (tfs.hasTerrainLayers()) {
                // Automatically set to terrain-following mode if not already set
                if (this.placementMode == PlacementMode.SURFACE) {
                    this.placementMode = PlacementMode.TERRAIN_FOLLOWING;
                }
            }
        }
    }

    /**
     * Set valid Y level range
     */
    public StructurePlacement setYRange(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
        return this;
    }

    /**
     * Set if structure needs solid ground below
     */
    public StructurePlacement setNeedsSolidGround(boolean needs) {
        this.needsSolidGround = needs;
        return this;
    }

    /**
     * Set if structure needs air space above ground
     */
    public StructurePlacement setNeedsAir(boolean needs) {
        this.needsAir = needs;
        return this;
    }

    /**
     * Add valid ground blocks for placement
     */
    public StructurePlacement addValidGroundBlock(Block block) {
        this.validGroundBlocks.add(block);
        return this;
    }

    /**
     * Set custom placement condition
     */
    public StructurePlacement setCustomCondition(Predicate<StructurePlacementContext> condition) {
        this.customCondition = condition;
        return this;
    }

    /**
     * Set placement mode (surface, underground, cave, anywhere, terrain_following)
     */
    public StructurePlacement setPlacementMode(PlacementMode mode) {
        this.placementMode = mode;
        return this;
    }

    /**
     * Enable structure chaining (connecting to nearby structures)
     */
    public StructurePlacement setAllowChaining(boolean allow) {
        this.allowChaining = allow;
        return this;
    }

    /**
     * Set max distance for chaining structures together
     */
    public StructurePlacement setMaxChainDistance(int distance) {
        this.maxChainDistance = distance;
        return this;
    }

    /**
     * Get placement mode
     */
    public PlacementMode getPlacementMode() {
        return placementMode;
    }

    /**
     * Check if this is a terrain-following structure
     */
    public boolean isTerrainFollowing() {
        return structure instanceof TerrainFollowingStructure &&
               ((TerrainFollowingStructure) structure).hasTerrainLayers();
    }

    /**
     * Get structure as TerrainFollowingStructure if it is one
     */
    public TerrainFollowingStructure getAsTerrainFollowing() {
        if (structure instanceof TerrainFollowingStructure) {
            return (TerrainFollowingStructure) structure;
        }
        return null;
    }

    public boolean allowsChaining() {
        return allowChaining;
    }

    public int getMaxChainDistance() {
        return maxChainDistance;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    /**
     * Check if this chunk should attempt to place a structure
     */
    public boolean shouldAttemptPlacement(int chunkX, long worldSeed) {
        // Use chunk coordinate and world seed to determine if this is a valid structure chunk
        Xoroshiro random = new Xoroshiro(chunkX * 341873128712L + worldSeed + structure.getName().hashCode());

        // Check spacing (every N chunks can have a structure)
        if (chunkX % spacing != 0) {
            return false;
        }

        // Check spawn chance
        return random.nextFloat() < spawnChance;
    }

    /**
     * Check if structure can be placed at specific position
     */
    public boolean canPlaceAt(StructurePlacementContext context) {
        // Check Y range
        if (context.y < minY || context.y > maxY) {
            return false;
        }

        // Check solid ground if required
        if (needsSolidGround && !hasValidGround(context)) {
            return false;
        }

        // Check air space if required
        if (needsAir && !hasAirSpace(context)) {
            return false;
        }

        // Check custom condition
        if (customCondition != null && !customCondition.test(context)) {
            return false;
        }

        return true;
    }

    private boolean hasValidGround(StructurePlacementContext context) {
        BlockState groundBlock = context.getBlock(context.x, context.y - 1);
        if (groundBlock == null) return false;

        // If no valid ground blocks specified, any solid block is fine
        if (validGroundBlocks.isEmpty()) {
            return !groundBlock.getBlock().isAir();
        }

        return validGroundBlocks.contains(groundBlock.getBlock());
    }

    private boolean hasAirSpace(StructurePlacementContext context) {
        // Check if there's enough air above for the structure
        int requiredHeight = structure.getHeight();
        for (int dy = 0; dy < requiredHeight; dy++) {
            BlockState block = context.getBlock(context.x, context.y + dy);
            if (block != null && !block.getBlock().isAir()) {
                return false;
            }
        }
        return true;
    }

    public Structure getStructure() {
        return structure;
    }

    public int getSpacing() {
        return spacing;
    }

    public int getSeparation() {
        return separation;
    }

    /**
     * Context object passed to placement checks
     */
    public static class StructurePlacementContext {
        public final int x, y; // Global coordinates
        private final BlockGetter blockGetter;

        public StructurePlacementContext(int x, int y, BlockGetter blockGetter) {
            this.x = x;
            this.y = y;
            this.blockGetter = blockGetter;
        }

        public BlockState getBlock(int globalX, int globalY) {
            return blockGetter.getBlock(globalX, globalY);
        }
    }

    /**
     * Interface for getting blocks during placement checks
     */
    public interface BlockGetter {
        BlockState getBlock(int globalX, int globalY);
    }
}
