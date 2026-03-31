package dev.alexco.minecraft.world.level.levelgen.structure;

import dev.alexco.minecraft.phys.AABB;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkPos;
import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



/**
 * Manages structure generation and placement across chunks
 */
public class StructureManager {
    private final World world;
    public final List<StructurePlacement> structurePlacements;
    public final Map<ChunkPos, List<StructureStart>> structureStarts;

    public StructureManager(World world) {
        this.world = world;
        this.structurePlacements = new ArrayList<>();
        this.structureStarts = new ConcurrentHashMap<>();
        Logger.INFO("Finished creating structure manaegr");
    }

    public void registerStructure(StructurePlacement placement) {
        Logger.INFO("Registering structure " + placement.getStructure().getName());
        structurePlacements.add(placement);
    }

    /**
     * Calculate structure starts for a chunk (STRUCTURES phase)
     */
    public void calculateStructureStarts(Chunk chunk) {
        ChunkPos pos = chunk.getChunkPos();
        if (structureStarts.containsKey(pos)) return;

        List<StructureStart> starts = new ArrayList<>();
        for (StructurePlacement placement : structurePlacements) {
            if (placement.shouldAttemptPlacement(pos.x, world.seed)) {
                StructureStart start = tryPlaceStructure(chunk, placement);
                if (start != null) starts.add(start);
            }
        }
        structureStarts.put(pos, starts);
    }

    private StructureStart tryPlaceStructure(Chunk chunk, StructurePlacement placement) {
        Xoroshiro random = new Xoroshiro(world.seed + chunk.getChunkPos().x * 341873128712L +
                                         placement.getStructure().getName().hashCode());
        int chunkStartX = chunk.getChunkPos().x * 16;

        // Try random positions
        for (int attempt = 0; attempt < 10; attempt++) {
            int globalX = chunkStartX + random.nextInt(16);
            Integer validY = findValidYLevel(globalX, placement, random);
            if (validY != null) {
                return new StructureStart(placement.getStructure(), globalX, validY, chunk.getChunkPos());
            }
        }
        return null;
    }

    private Integer findValidYLevel(int globalX, StructurePlacement placement, Xoroshiro random) {
        StructurePlacement.BlockGetter blockGetter = (x, y) -> {
            try {
                Chunk chunk = world.getChunkIfExists(World.getChunkX(x));
                return chunk != null ? chunk.getBlockAt(World.getLocalX(x), y, true) : null;
            } catch (Exception e) {
                return null;
            }
        };

        int minY = Math.max(10, placement.getMinY());
        int maxY = Math.min(250, placement.getMaxY());

        switch (placement.getPlacementMode()) {
            case SURFACE:
            case TERRAIN_FOLLOWING:
                return findSurface(globalX, minY, maxY, placement, blockGetter);
            case UNDERGROUND:
                return findYWithCondition(globalX, minY, maxY, placement, blockGetter, random,
                    (current, above, below) -> !current.getBlock().isAir() &&
                                               !above.getBlock().isAir() &&
                                               !below.getBlock().isAir());
            case CAVE:
                return findCave(globalX, minY, maxY, placement, blockGetter, random);
            case ANYWHERE:
                return findAnywhere(globalX, minY, maxY, placement, blockGetter, random);
            default:
                return findSurface(globalX, minY, maxY, placement, blockGetter);
        }
    }

    private Integer findSurface(int globalX, int minY, int maxY, StructurePlacement placement,
                                StructurePlacement.BlockGetter blockGetter) {
        for (int y = maxY; y > minY; y--) {
            BlockState current = blockGetter.getBlock(globalX, y);
            BlockState below = blockGetter.getBlock(globalX, y - 1);

            if (current != null && current.getBlock().isAir() &&
                below != null && !below.getBlock().isAir()) {
                var context = new StructurePlacement.StructurePlacementContext(globalX, y, blockGetter);
                if (placement.canPlaceAt(context)) return y;
                return null;
            }
        }
        return null;
    }

    private Integer findYWithCondition(int globalX, int minY, int maxY, StructurePlacement placement,
                                      StructurePlacement.BlockGetter blockGetter, Xoroshiro random,
                                      BlockCondition condition) {
        List<Integer> validPositions = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            BlockState current = blockGetter.getBlock(globalX, y);
            BlockState above = blockGetter.getBlock(globalX, y + 1);
            BlockState below = blockGetter.getBlock(globalX, y - 1);

            if (current != null && above != null && below != null &&
                condition.test(current, above, below)) {
                validPositions.add(y);
            }
        }

        if (!validPositions.isEmpty()) {
            int y = validPositions.get(random.nextInt(validPositions.size()));
            var context = new StructurePlacement.StructurePlacementContext(globalX, y, blockGetter);
            if (placement.canPlaceAt(context)) return y;
        }
        return null;
    }

    private Integer findCave(int globalX, int minY, int maxY, StructurePlacement placement,
                            StructurePlacement.BlockGetter blockGetter, Xoroshiro random) {
        List<Integer> cavePositions = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            BlockState current = blockGetter.getBlock(globalX, y);
            BlockState below = blockGetter.getBlock(globalX, y - 1);

            if (current != null && current.getBlock().isAir() &&
                below != null && !below.getBlock().isAir()) {
                // Check for solid blocks above (cave ceiling)
                for (int checkY = y + 1; checkY < Math.min(y + 50, maxY); checkY++) {
                    BlockState check = blockGetter.getBlock(globalX, checkY);
                    if (check != null && !check.getBlock().isAir()) {
                        cavePositions.add(y);
                        break;
                    }
                }
            }
        }

        if (!cavePositions.isEmpty()) {
            int y = cavePositions.get(random.nextInt(cavePositions.size()));
            var context = new StructurePlacement.StructurePlacementContext(globalX, y, blockGetter);
            if (placement.canPlaceAt(context)) return y;
        }
        return null;
    }

    private Integer findAnywhere(int globalX, int minY, int maxY, StructurePlacement placement,
                                StructurePlacement.BlockGetter blockGetter, Xoroshiro random) {
        List<Integer> allPositions = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            var context = new StructurePlacement.StructurePlacementContext(globalX, y, blockGetter);
            if (placement.canPlaceAt(context)) allPositions.add(y);
        }
        return allPositions.isEmpty() ? null : allPositions.get(random.nextInt(allPositions.size()));
    }

    @FunctionalInterface
    private interface BlockCondition {
        boolean test(BlockState current, BlockState above, BlockState below);
    }

    /**
     * Place structures for a chunk (FEATURES phase)
     */
    public void placeStructures(Chunk chunk) {
        ChunkPos pos = chunk.getChunkPos();
        for (int dx = -2; dx <= 2; dx++) {
            List<StructureStart> starts = structureStarts.get(new ChunkPos(pos.x + dx));
            if (starts != null) {
                for (StructureStart start : starts) {
                    placeStructure(start, chunk);
                }
            }
        }
    }

    private void placeStructure(StructureStart start, Chunk chunk) {
        if (start.structure instanceof TerrainFollowingStructure) {
            TerrainFollowingStructure tfs = (TerrainFollowingStructure) start.structure;
            if (tfs.hasTerrainLayers()) {
                placeTerrainFollowing(start, chunk, tfs);
                return;
            }
        }
        placeNormal(start, chunk);
    }

    private void placeNormal(StructureStart start, Chunk chunk) {
        placeBlocks(start, chunk, start.structure.getBlocks(), false);
        placeBlocks(start, chunk, start.structure.getBackgroundBlocks(), true);
    }

    private void placeBlocks(StructureStart start, Chunk chunk, List<Structure.StructureBlock> blocks,
                            boolean isBackground) {
        for (var block : blocks) {
            int globalX = start.x + block.relX;
            int globalY = start.y + block.relY;

            if (World.getChunkX(globalX) == chunk.getChunkPos().x) {
                int localX = World.getLocalX(globalX);
                if (localX >= 0 && localX < 16 && globalY >= 0 && globalY < 384) {
                    if (isBackground) {
                        chunk.setBackgroundBlockAt(localX, globalY, block.state);
                    } else {
                        chunk.setBlockAt(localX, globalY, block.state);
                    }
                }
            }
        }
    }

    private void placeTerrainFollowing(StructureStart start, Chunk chunk,
                                      TerrainFollowingStructure structure) {
        // Place normal blocks first
        if (!structure.getBlocks().isEmpty() || !structure.getBackgroundBlocks().isEmpty()) {
            placeNormal(start, chunk);
        }

        // Place terrain layers
        for (var entry : structure.getTerrainLayers().entrySet()) {
            int yOffset = entry.getKey();
            var layer = entry.getValue();

            for (var blockEntry : layer.getBlocks().entrySet()) {
                int xOffset = blockEntry.getKey();
                int globalX = start.x + xOffset;

                if (World.getChunkX(globalX) == chunk.getChunkPos().x) {
                    Integer surfaceY = findSurfaceYAt(globalX, chunk);
                    if (surfaceY != null) {
                        int placementY = surfaceY + yOffset;
                        int localX = World.getLocalX(globalX);

                        if (localX >= 0 && localX < 16 && placementY >= 0 && placementY < 384) {
                            if (layer.shouldReplaceExisting()) {
                                chunk.setBlockAt(localX, placementY, blockEntry.getValue());
                            } else {
                                try {
                                    BlockState current = chunk.getBlockAt(localX, placementY);
                                    if (current != null && current.getBlock().isAir()) {
                                        chunk.setBlockAt(localX, placementY, blockEntry.getValue());
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
            }
        }
    }

    private Integer findSurfaceYAt(int globalX, Chunk chunk) {
        if (World.getChunkX(globalX) != chunk.getChunkPos().x) {
            chunk = world.getChunkIfExists(World.getChunkX(globalX));
            if (chunk == null) return null;
        }

        int localX = World.getLocalX(globalX);
        for (int y = 250; y > 10; y--) {
            try {
                BlockState current = chunk.getBlockAt(localX, y);
                BlockState below = chunk.getBlockAt(localX, y - 1);
                if (current != null && current.getBlock().isAir() &&
                    below != null && !below.getBlock().isAir()) {
                    return y;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public List<StructureStart> getStructureStartsForChunk(ChunkPos pos) {
        List<StructureStart> result = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            List<StructureStart> starts = structureStarts.get(new ChunkPos(pos.x + dx));
            if (starts != null) result.addAll(starts);
        }
        return result;
    }

    public static class StructureStart {
        public final Structure structure;
        public final int x, y;
        public final ChunkPos originChunk;
        public final AABB boundingBox;

        public StructureStart(Structure structure, int x, int y, ChunkPos originChunk) {
            this.structure = structure;
            this.x = x;
            this.y = y;
            this.originChunk = originChunk;
            // Calculate bounding box based on structure size
            int halfWidth = structure.getWidth() / 2;
            int halfHeight = structure.getHeight() / 2;
            this.boundingBox = new AABB(
                x - halfWidth,
                y - halfHeight,
                x + halfWidth + (structure.getWidth() % 2), // Account for odd widths
                y + halfHeight + (structure.getHeight() % 2) // Account for odd heights
            );
        }
        
        /**
         * Checks if a point is inside this structure's bounding box
         */
        public boolean contains(int worldX, int worldY) {
            return worldX >= boundingBox.x0 && worldX < boundingBox.x1 &&
                   worldY >= boundingBox.y0 && worldY < boundingBox.y1;
        }
        
        /**
         * Gets the bounding box for this structure
         */
        public AABB getBoundingBox() {
            return boundingBox;
        }
    }
}
