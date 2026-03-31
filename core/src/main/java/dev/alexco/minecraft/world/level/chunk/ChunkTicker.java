package dev.alexco.minecraft.world.level.chunk;

import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import static dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties.*;
import static dev.alexco.minecraft.SharedConstants.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.loot.LootTableManager;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.BlockItemEntity;
import dev.alexco.minecraft.world.entity.ItemEntity;
import dev.alexco.minecraft.world.level.item.BlockItem;
import dev.alexco.minecraft.world.level.item.Items;
import dev.alexco.registry.ResourceLocation;

public class ChunkTicker {
    private static final int MAX_DEPTH = 8;
    private Chunk chunkRef;
    private final Random random = new Random();

    public ChunkTicker(Chunk chunkRef) {
        this.chunkRef = chunkRef;
    }

    private static class WaterSpreadPos {
        int x, y;
        WaterSpreadPos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Runs random block ticks and rebuilds the chunk mesh when changed.
     */
    public void tick() {
        int randomTickSpeed = Math.max(0, Minecraft.getInstance().getWorld().worldData.randomTickSpeed);
        boolean changed = false;
        for (int i = 0; i < randomTickSpeed; i++) {
            int x = random.nextInt(CHUNK_WIDTH);
            int y = random.nextInt(CHUNK_HEIGHT - 1);
            changed |= randomTickBlock(x, y);
        }

        if (changed) {
            chunkRef.build();
        }

        // Simulate water flow
       // simulateWater();
    }

    /**
     * Applies random-tick behaviour for one block position.
     */
    private boolean randomTickBlock(int x, int y) {
        if (y <= 0 || y >= CHUNK_HEIGHT - 1) {
            return false;
        }
        BlockState current = chunkRef.getBlockAt(x, y);
        BlockState above = chunkRef.getBlockAt(x, y + 1);

        if (current.getBlock().equals(Blocks.FARMLAND)) {
            int targetMoisture = getFarmlandMoistureFromNearbyWater(x, y);
            int currentMoisture = current.getValue(MOISTURE);
            boolean changed = false;

            if (targetMoisture != currentMoisture) {
                current = current.setValue(MOISTURE, targetMoisture);
                chunkRef.setBlockAt(x, y, current);
                changed = true;
            }

            if (targetMoisture == 0 && !isGrowingCrop(above)) {
                chunkRef.setBlockAt(x, y, Blocks.DIRT.defaultBlockState());
                return true;
            }
            return changed;
        }

        if (current.getBlock().equals(Blocks.GRASS_BLOCK) && !above.getBlock().isAir()) {
            chunkRef.setBlockAt(x, y, Blocks.DIRT.defaultBlockState());
            return true;
        }
        if (current.getBlock().equals(Blocks.DIRT) && above.getBlock().isAir()) {
            chunkRef.setBlockAt(x, y, Blocks.GRASS_BLOCK.defaultBlockState());
            return true;
        }
        if (current.getBlock().equals(Blocks.WHEAT)) {
            int age = current.getValue(AGE);
            if (age < 7) {
                BlockState below = chunkRef.getBlockAt(x, y - 1);
                if (below.getBlock().equals(Blocks.FARMLAND)) {
                    int moisture = below.getValue(MOISTURE);
                    int growthChance = moisture > 1 ? 2 : 4; // 2x faster when moisture is above 1
                    if (random.nextInt(growthChance) == 0) {
                        chunkRef.setBlockAt(x, y, current.setValue(AGE, age + 1));
                        return true;
                    }
                }
            }
        }

        if (current.getBlock().equals(Blocks.OAK_LEAVES)
                && current.hasProperty(PERSISTENT)
                && !current.getValue(PERSISTENT)
                && !hasNearbyLog(x, y, 4)) {
            int globalX = chunkRef.getChunkPos().x * CHUNK_WIDTH + x;
            dropLeafDecayLoot(globalX, y, current);
            chunkRef.setBlockAt(x, y, Blocks.AIR.defaultBlockState());
            return true;
        }
        return false;
    }

    /**
     * Returns true if farmland currently has a crop above it.
     */
    private boolean isGrowingCrop(BlockState stateAbove) {
        return stateAbove.getBlock().equals(Blocks.WHEAT);
    }

    /**
     * Computes farmland moisture based on nearby horizontal water.
     */
    private int getFarmlandMoistureFromNearbyWater(int localX, int y) {
        int globalX = chunkRef.getChunkPos().x * CHUNK_WIDTH + localX;
        int nearestDistance = Integer.MAX_VALUE;

        for (int dx = -7; dx <= 7; dx++) {
            BlockState scanned = Minecraft.getInstance().getWorld().getBlock(globalX + dx, y);
            if (scanned.getBlock().equals(Blocks.WATER) || scanned.getBlock().equals(Blocks.FLOWING_WATER)) {
                nearestDistance = Math.min(nearestDistance, Math.abs(dx));
            }
        }

        if (nearestDistance == Integer.MAX_VALUE) {
            return 0;
        }

        // Distance 1..7 maps to moisture 7..1. Water at same x is treated as max moisture.
        return Math.max(1, 8 - nearestDistance);
    }

    /**
     * Checks if there is a supporting log near a leaf block.
     */
    private boolean hasNearbyLog(int localX, int y, int range) {
        int globalX = chunkRef.getChunkPos().x * CHUNK_WIDTH + localX;
        World world = Minecraft.getInstance().getWorld();
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                int checkY = y + dy;
                if (checkY < 0 || checkY >= CHUNK_HEIGHT) {
                    continue;
                }
                BlockState candidate = world.getBlock(globalX + dx, checkY);
                if (candidate.getBlock().equals(Blocks.OAK_LOG) || candidate.getBlock().equals(Blocks.JUNGLE_LOG)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Spawns loot for leaf decay at the given world position.
     */
    private void dropLeafDecayLoot(int globalX, int y, BlockState state) {
        List<ItemStack> drops = LootTableManager.getDropsFromTable(
            new ResourceLocation("minecraft", "gameplay/oak_leaf_decay"),
            state,
            Items.AIR
        );
        for (ItemStack drop : drops) {
            for (int i = 0; i < drop.amount; i++) {
                if (drop.item instanceof BlockItem blockItem) {
                    Minecraft.getInstance().getWorld().entities.add(new BlockItemEntity(blockItem, globalX + 0.5D, y));
                } else {
                    Minecraft.getInstance().getWorld().entities.add(new ItemEntity(drop.item, globalX + 0.5D, y));
                }
            }
        }
    }

    /**
     * Simulates simple water spread and source conversion.
     */
    private void simulateWater() {
        // Find all water blocks that need to spread
        List<WaterSpreadPos> activeWater = new ArrayList<>();
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                BlockState block = chunkRef.getBlockAt(x, y);
                if (block != null && block.getBlock().equals(Blocks.WATER)) {
                    activeWater.add(new WaterSpreadPos(x, y));
                }
            }
        }

        Set<String> processedThisTick = new HashSet<>();

        for (WaterSpreadPos pos : activeWater) {
            String key = pos.x + "," + pos.y;
            if (processedThisTick.contains(key)) continue;
            processedThisTick.add(key);

            if (!inBounds(pos.x, pos.y)) continue;

            BlockState block = chunkRef.getBlockAt(pos.x, pos.y);
            if (block == null || !block.getBlock().equals(Blocks.WATER)) continue;

            // PRIORITY 1: Try to flow DOWNWARDS FIRST
            if (inBounds(pos.x, pos.y - 1)) {
                BlockState below = chunkRef.getBlockAt(pos.x, pos.y - 1);

                if (below.getBlock().equals(Blocks.AIR)) {
                    // Flow down into air - ALWAYS do this first
                    chunkRef.setBlockAt(pos.x, pos.y - 1,
                        Blocks.WATER.defaultBlockState()
                            .setValue(LEVEL, 8)
                            .setValue(FALLING, true)
                            .setValue(SOURCE, false));
                    continue; // Don't spread horizontally, water is falling
                } else if (below.getBlock().equals(Blocks.WATER)) {
                    int belowDepth = below.getValue(LEVEL);

                    // If water below is shallower, make it deeper
                    if (8 > belowDepth) {
                        chunkRef.setBlockAt(pos.x, pos.y - 1,
                            below.setValue(LEVEL, 8).setValue(FALLING, true));
                        continue; // Don't spread horizontally, water is falling
                    }
                }
            }

            // PRIORITY 2: Only spread horizontally if we CAN'T go down
            // This means there's a solid block or full water below
            boolean canSpreadHorizontally = false;

            if (inBounds(pos.x, pos.y - 1)) {
                BlockState below = chunkRef.getBlockAt(pos.x, pos.y - 1);

                // Can only spread horizontally if below is solid or full source water
                if (isSolid(below) ||
                    (below.getBlock().equals(Blocks.WATER) && below.getValue(LEVEL) == 8)) {
                    canSpreadHorizontally = true;
                }
            } else {
                // At bottom of world
                canSpreadHorizontally = true;
            }

            if (canSpreadHorizontally) {
                // Reset to level 8 if on solid ground
                if (block.getValue(LEVEL) != 8 && block.getValue(SOURCE)) {
                    chunkRef.setBlockAt(pos.x, pos.y, block.setValue(LEVEL, 8));
                }

                // Spread left and right
                int currentDepth = block.getValue(LEVEL);
                int[] directions = {-1, 1};

                for (int dir : directions) {
                    int nx = pos.x + dir;
                    if (!inBounds(nx, pos.y)) continue;

                    BlockState neighbour = chunkRef.getBlockAt(nx, pos.y);

                    if (neighbour.getBlock().equals(Blocks.AIR)) {
                        // Spread into air with decreased depth
                        int newDepth = currentDepth - 1;
                        if (newDepth >= 1) {
                            chunkRef.setBlockAt(nx, pos.y,
                                Blocks.WATER.defaultBlockState()
                                    .setValue(LEVEL, newDepth)
                                    .setValue(FALLING, false)
                                    .setValue(SOURCE, false));
                        }
                    } else if (neighbour.getBlock().equals(Blocks.WATER)) {
                        // Merge with existing water - higher level = deeper
                        int proposedDepth = currentDepth - 1;
                        int neighbourDepth = neighbour.getValue(LEVEL);

                        if (proposedDepth > neighbourDepth && proposedDepth >= 1) {
                            chunkRef.setBlockAt(nx, pos.y,
                                neighbour.setValue(LEVEL, proposedDepth));
                        }
                    }
                }

                // Source conversion
                if (!block.getValue(SOURCE)) {
                    int sourceNeighbors = 0;

                    for (int dir : directions) {
                        int nx = pos.x + dir;
                        if (inBounds(nx, pos.y)) {
                            BlockState neighborBlock = chunkRef.getBlockAt(nx, pos.y);
                            if (neighborBlock.getBlock().equals(Blocks.WATER)
                                && neighborBlock.getValue(SOURCE)) {
                                sourceNeighbors++;
                            }
                        }
                    }

                    if (sourceNeighbors >= 2 && inBounds(pos.x, pos.y - 1)) {
                        BlockState below = chunkRef.getBlockAt(pos.x, pos.y - 1);
                        boolean validBase = isSolid(below)
                            || (below.getBlock().equals(Blocks.WATER) && below.getValue(SOURCE));

                        if (validBase) {
                            chunkRef.setBlockAt(pos.x, pos.y,
                                block.setValue(SOURCE, true).setValue(LEVEL, 8));
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns true for blocks treated as solid fluid support.
     */
    private boolean isSolid(BlockState block) {
        return block.getBlock().equals(Blocks.STONE)
            || block.getBlock().equals(Blocks.DIRT)
            || block.getBlock().equals(Blocks.GRASS_BLOCK);
    }

    /**
     * Returns true when local coordinates are within chunk bounds.
     */
    private boolean inBounds(int x, int y) {
        return x >= 0 && x < CHUNK_WIDTH && y >= 0 && y < CHUNK_HEIGHT;
    }
}
