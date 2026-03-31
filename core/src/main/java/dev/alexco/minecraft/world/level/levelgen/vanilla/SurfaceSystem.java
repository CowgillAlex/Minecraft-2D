package dev.alexco.minecraft.world.level.levelgen.vanilla;

import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.levelgen.noise.Noise;
import dev.alexco.minecraft.world.level.levelgen.noise.NormalNoise;
import dev.alexco.registry.ResourceLocation;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Faithful port of Minecraft's SurfaceSystem, adapted for 2D world generation.
 *
 * This class is responsible for applying surface rules to build the surface layer
 * of the world, including handling special features like badlands terracotta bands.
 *
 * Official Minecraft: net.minecraft.world.level.levelgen.SurfaceSystem
 *
 * Key fixes over previous version:
 *  - Removed isConnectedToMainTerrain() - it would skip true surface blocks when
 *    a shallow cave existed just below the surface (< 8 solid blocks between surface
 *    and cave roof = no surface rule applied at all).
 *  - Removed the aggressive gap-tracking break inside buildSurfaceInternal. The
 *    original logic stopped processing a column the moment it found a 4+ block air
 *    gap anywhere below the first solid block, which meant any cave touching the
 *    surface would prevent dirt/grass from appearing.
 *  - findSurfaceY now simply returns the topmost solid, non-fluid block without any
 *    connectivity check. Surface rules then apply from that point downward as normal.
 */
public class SurfaceSystem {
    private final BlockState defaultBlock;
    private final int seaLevel;
    private final long seed;

    // Noise instances for surface generation
    private final Noise surfaceNoise;
    private final Noise surfaceSecondaryNoise;
    private final Noise clayBandsOffsetNoise;

    // Pre-computed clay bands for badlands
    private final BlockState[] clayBands;

    // Block state cache for badlands bands
    private final BlockState TERRACOTTA;
    private final BlockState ORANGE_TERRACOTTA;
    private final BlockState YELLOW_TERRACOTTA;
    private final BlockState BROWN_TERRACOTTA;
    private final BlockState RED_TERRACOTTA;
    private final BlockState WHITE_TERRACOTTA;
    private final BlockState LIGHT_GRAY_TERRACOTTA;

    private final VanillaWorldgenConfig worldgen;
    private final VanillaNoiseStep noiseStep;

    public SurfaceSystem(long seed, VanillaWorldgenConfig worldgen, VanillaNoiseStep noiseStep) {
        this.seed = seed;
        this.worldgen = worldgen;
        this.noiseStep = noiseStep;
        this.defaultBlock = worldgen.getDefaultBlock();
        this.seaLevel = worldgen.getSeaLevelInternal();

        // Initialise block states for badlands
        this.TERRACOTTA = resolveBlock("minecraft:terracotta");
        this.ORANGE_TERRACOTTA = resolveBlock("minecraft:orange_terracotta");
        this.YELLOW_TERRACOTTA = resolveBlock("minecraft:yellow_terracotta");
        this.BROWN_TERRACOTTA = resolveBlock("minecraft:brown_terracotta");
        this.RED_TERRACOTTA = resolveBlock("minecraft:red_terracotta");
        this.WHITE_TERRACOTTA = resolveBlock("minecraft:white_terracotta");
        this.LIGHT_GRAY_TERRACOTTA = resolveBlock("minecraft:light_gray_terracotta");

        // Create noise instances
        this.surfaceNoise = createNoise("surface", -6, new double[]{1.0, 1.0, 1.0});
        this.surfaceSecondaryNoise = createNoise("surface_secondary", -6, new double[]{1.0, 1.0, 1.0});
        this.clayBandsOffsetNoise = createNoise("clay_bands_offset", -8, new double[]{1.0});

        // Generate clay bands
        Random random = new Random(seed ^ "clay_bands".hashCode());
        this.clayBands = generateBands(random);
    }

    /**
     * Build the surface for a chunk by applying surface rules.
     * This mirrors Minecraft's SurfaceSystem.buildSurface() method.
     */
    public void buildSurface(Chunk chunk, IntFunction<Biome> biomeAtWorldX, SurfaceRules.RuleSource ruleSource) {
        try {
            buildSurfaceInternal(chunk, biomeAtWorldX, ruleSource);
        } catch (Exception e) {
            Logger.ERROR("Surface generation failed for chunk %s: %s", chunk.getChunkPos(), e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void buildSurfaceInternal(Chunk chunk, IntFunction<Biome> biomeAtWorldX, SurfaceRules.RuleSource ruleSource) {
        // Create a biome getter that provides a fallback if the original returns null
        Function<Integer, Biome> safeBiomeGetter = x -> {
            Biome biome = biomeAtWorldX.apply(x);
            if (biome == null) {
                Logger.INFO("Biome was null at X=%d, using PLAINS as fallback", x);
                return dev.alexco.minecraft.world.biome.Biomes.PLAINS;
            }
            return biome;
        };

        SurfaceRules.Context context = new SurfaceRules.Context(
            noiseStep,
            seaLevel,
            this::getSurfaceDepth,
            this::getSurfaceSecondary,
            safeBiomeGetter,
            worldgen
        );

        SurfaceRules.SurfaceRule rule = ruleSource.apply(context);

        for (int localX = 0; localX < SharedConstants.CHUNK_WIDTH; localX++) {
            int worldX = chunk.getChunkPos().x * SharedConstants.CHUNK_WIDTH + localX;

            context.updateXZ(worldX);

            // Find the topmost solid block to start iterating from.
            // FIXED: no connectivity check â€” we just want the highest solid block.
            // The old isConnectedToMainTerrain() check would reject the true surface
            // whenever a cave existed fewer than 8 blocks below it.
            int startingHeight = findSurfaceY(chunk, localX);
            if (startingHeight < 0) {
                continue;
            }

            int stoneAboveDepth = 0;
            int waterHeight = Integer.MIN_VALUE;
            int nextCeilingStoneY = Integer.MAX_VALUE;

            // Iterate from the surface downward. We apply surface rules to every
            // solid block in the column until we have gone deep enough that no
            // surface rule could possibly match (stoneAboveDepth > max possible depth).
            // We do NOT break early on air gaps â€” caves below the surface are normal
            // and should not prevent surface material from being placed.
            for (int y = startingHeight; y >= 0; y--) {
                BlockState currentBlock = chunk.getBlockAt(localX, y);

                // Air block â€” reset stone-above counter and water height, then continue.
                // Do NOT break here: there may be more surface blocks below (e.g. cave
                // ceilings) that still need a surface rule applied.
                if (currentBlock.getBlock().equals(Blocks.AIR)) {
                    stoneAboveDepth = 0;
                    waterHeight = Integer.MIN_VALUE;
                    continue;
                }

                // Fluid block â€” record the water height so water-condition rules work.
                if (BlockTags.FLUID.contains(currentBlock.getBlock()) ||
                    currentBlock.getBlock().equals(Blocks.WATER) ||
                    currentBlock.getBlock().equals(Blocks.FLOWING_WATER)) {
                    if (waterHeight == Integer.MIN_VALUE) {
                        waterHeight = y + 1;
                    }
                    stoneAboveDepth = 0;
                    continue;
                }

                // Solid block â€” update the ceiling tracker and depth counter.
                if (nextCeilingStoneY >= y) {
                    nextCeilingStoneY = Integer.MIN_VALUE;
                    for (int lookY = y - 1; lookY >= -1; lookY--) {
                        if (lookY < 0 || !isStone(chunk.getBlockAt(localX, lookY))) {
                            nextCeilingStoneY = lookY + 1;
                            break;
                        }
                    }
                }

                stoneAboveDepth++;
                int stoneBelowDepth = y - nextCeilingStoneY + 1;

                context.updateY(stoneAboveDepth, stoneBelowDepth, waterHeight, y);

                // Only apply rules to the default block (stone / deepslate).
                // This matches vanilla: surface rules replace stone, not pre-existing
                // surface material.
                if (!isDefaultBlock(currentBlock)) {
                    continue;
                }

                BlockState newState = rule.tryApply(worldX, y, 0);
                if (newState != null) {
                    chunk.setBlockAt(localX, y, newState);
                }

                // Once we are deep enough that no surface rule can reach us, stop.
                // The deepest vanilla stone_depth check is ~30 blocks, so 32 is safe.
                if (stoneAboveDepth > 32) {
                    break;
                }
            }
        }
    }

    /**
     * Get the surface depth at a given X position.
     * This affects how deep the surface material (dirt/sand) extends.
     */
    protected int getSurfaceDepth(int blockX) {
        double noiseValue = surfaceNoise.sample(blockX, 0.0, 0.0);
        return (int) (noiseValue * 2.75 + 3.0 + randomAt("minecraft:surface_depth", blockX, 0) * 0.25);
    }

    /**
     * Get the secondary surface value at a given X position.
     * This affects the depth variation of surface materials.
     */
    protected double getSurfaceSecondary(int blockX) {
        return surfaceSecondaryNoise.sample(blockX, 0.0, 0.0);
    }

    /**
     * Get a terracotta band for badlands biomes.
     */
    protected BlockState getBand(int worldX, int y) {
        double offset = clayBandsOffsetNoise.sample(worldX * 0.05, 0.0, 0.0) * 4.0;
        int index = Math.floorMod((int) Math.floor(y + offset), clayBands.length);
        return clayBands[index];
    }

    /**
     * Get the sea level.
     */
    public int getSeaLevel() {
        return seaLevel;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private boolean isStone(BlockState state) {
        return !state.getBlock().equals(Blocks.AIR) &&
               !BlockTags.FLUID.contains(state.getBlock()) &&
               !state.getBlock().equals(Blocks.WATER) &&
               !state.getBlock().equals(Blocks.FLOWING_WATER);
    }

    private boolean isDefaultBlock(BlockState state) {
        return state.getBlock().equals(defaultBlock.getBlock()) ||
               state.getBlock().equals(Blocks.STONE);
    }

    private boolean isSolid(BlockState state) {
        return !state.getBlock().equals(Blocks.AIR) &&
               !BlockTags.FLUID.contains(state.getBlock()) &&
               !state.getBlock().equals(Blocks.WATER) &&
               !state.getBlock().equals(Blocks.FLOWING_WATER);
    }

    private Noise createNoise(String name, int octaves, double[] amplitudes) {
        return worldgen.createNoise(name, new NormalNoise.NoiseParameters(octaves, amplitudes));
    }

    private BlockState resolveBlock(String name) {
        var block = Registry.BLOCK.get(new ResourceLocation(name));
        return block != null ? block.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }

    private static long mix(long x) {
        x = (x ^ (x >>> 30)) * -4658895280553007687L;
        x = (x ^ (x >>> 27)) * -7723592293110705685L;
        return x ^ (x >>> 31);
    }

    private double randomAt(String key, int x, int y) {
        long h = seed ^ key.hashCode();
        h = mix(h + x * 341873128712L + y * 132897987541L);
        return ((h >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
    }

    /**
     * Find the topmost solid, non-fluid block in a column.
     *
     * FIXED: removed isConnectedToMainTerrain() call. The connectivity check
     * required 8 solid blocks below the candidate surface with no gap larger than 2.
     * This is too strict â€” vanilla terrain regularly has caves within 8 blocks of
     * the surface, causing the check to fail and leaving the surface as bare stone.
     *
     * We simply return the highest Y with a solid block. The surface rule system
     * itself is robust enough to handle whatever terrain exists below.
     */
    private int findSurfaceY(Chunk chunk, int localX) {
        for (int y = SharedConstants.CHUNK_HEIGHT - 1; y >= 0; y--) {
            BlockState state = chunk.getBlockAt(localX, y);
            if (isSolid(state)) {
                return y;
            }
        }
        return -1;
    }

    /**
     * Generate the terracotta clay bands for badlands biomes.
     * This matches Minecraft's band generation algorithm.
     */
    private BlockState[] generateBands(Random random) {
        BlockState[] bands = new BlockState[192];
        Arrays.fill(bands, TERRACOTTA);

        // Add orange terracotta patches
        for (int i = 0; i < bands.length; i++) {
            i += random.nextInt(5) + 1;
            if (i < bands.length) {
                bands[i] = ORANGE_TERRACOTTA;
            }
        }

        // Add coloured bands
        makeBands(random, bands, 1, YELLOW_TERRACOTTA);
        makeBands(random, bands, 2, BROWN_TERRACOTTA);
        makeBands(random, bands, 1, RED_TERRACOTTA);

        // Add white terracotta patches
        int whiteBandCount = random.nextInt(7) + 9;
        for (int i = 0, start = 0; i < whiteBandCount && start < bands.length; i++, start += random.nextInt(16) + 4) {
            bands[start] = WHITE_TERRACOTTA;
            if (start - 1 > 0 && random.nextBoolean()) {
                bands[start - 1] = LIGHT_GRAY_TERRACOTTA;
            }
            if (start + 1 < bands.length && random.nextBoolean()) {
                bands[start + 1] = LIGHT_GRAY_TERRACOTTA;
            }
        }

        return bands;
    }

    private void makeBands(Random random, BlockState[] bands, int baseWidth, BlockState state) {
        int bandCount = random.nextInt(10) + 6;
        for (int i = 0; i < bandCount; i++) {
            int width = baseWidth + random.nextInt(3);
            int start = random.nextInt(bands.length);

            for (int p = 0; start + p < bands.length && p < width; p++) {
                bands[start + p] = state;
            }
        }
    }
}
