package dev.alexco.minecraft.world.level.levelgen;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;
import static dev.alexco.minecraft.SharedConstants.CHUNK_WIDTH;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.biome.BiomeManager;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;
import dev.alexco.minecraft.world.level.levelgen.density.DensityFunction;
import dev.alexco.minecraft.world.level.levelgen.density.DensityFunctionBuilder;
import dev.alexco.minecraft.world.level.levelgen.density.NoiseDensity;
import dev.alexco.minecraft.world.level.levelgen.density.YGradientDensity;
import dev.alexco.minecraft.world.level.levelgen.noise.Noises;
import dev.alexco.minecraft.world.level.levelgen.random.Xoroshiro;
import dev.alexco.minecraft.world.level.levelgen.feature.FeatureRegistry;
import dev.alexco.minecraft.world.level.levelgen.vanilla.VanillaNoiseStep;
import dev.alexco.minecraft.world.level.levelgen.vanilla.VanillaSurfaceRuleEngine;
import dev.alexco.minecraft.world.level.levelgen.vanilla.VanillaWorldgenConfig;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.light.LightEngine;
import java.util.HashMap;
import java.util.Map;

public class ChunkGenerator {
    public Chunk chunkRef;

    private static final int BEDROCK_LEVEL = 3;
    private Xoroshiro random = new Xoroshiro(Minecraft.getInstance().getWorld().seed);
    private Xoroshiro featurerandom;
    private final VanillaWorldgenConfig worldgen;
    private final int worldHeight;
    private final int seaLevel;
    private final int deepslateLevel;
    private final VanillaNoiseStep vanillaNoiseStep;
    private final VanillaSurfaceRuleEngine surfaceRuleEngine;
    private final FeatureRegistry featureRegistry;

    public ChunkGenerator(Chunk chunkReference) {
        this.chunkRef = chunkReference;
        long seed = Minecraft.getInstance().getWorld().seed;
        this.worldgen = VanillaWorldgenConfig.get(seed);
        this.worldHeight = CHUNK_HEIGHT;
        this.seaLevel = worldgen.getSeaLevelInternal();
        this.deepslateLevel = worldgen.toInternalY(0);
        worldgen.toInternalY(-28);
        worldgen.toInternalY(256);
        this.vanillaNoiseStep = VanillaNoiseStep.get(seed);
        this.surfaceRuleEngine = VanillaSurfaceRuleEngine.get(seed);
        this.featureRegistry = FeatureRegistry.get(seed);
        this.featurerandom = new Xoroshiro(seed + (chunkRef.getPos().x << 16));
        Noises.ensureInitialized();
    }

    /**
     * Generate chunk to specific status
     */
    public void generateStatus(ChunkStatus status) {

    switch (status) {
        case NOISE:
            generateBaseTerrain();
            smoothNoiseCaves();
            applyWaterLayer();
            generateBedrock(random);
            snapshotBackgroundFromNoiseStep();
            break;

        case SURFACE:
            generateDeepslate(random);
            try {
                generateSurfaceLayers(false);
            } catch (Exception e) {
                Logger.ERROR("Failed to generate surface for chunk %s: %s", chunkRef.getChunkPos(), e.getMessage());
                e.printStackTrace();
                // Continue generation even if surface fails
            }
            break;

        case STRUCTURES:
            Minecraft.getInstance().getWorld().structureManager.calculateStructureStarts(chunkRef);
            break;

        case FEATURES:

            placeOres(featurerandom);
            placeBiomeFeatures();
            placeTrees();
            Minecraft.getInstance().getWorld().structureManager.placeStructures(chunkRef);
            break;

        case LIGHT:
            LightEngine lightEngine = new LightEngine(chunkRef);
            lightEngine.calculateLight();
            break;

        case FULL:
            break;

        default:
            break;
    }

}

    private void snapshotBackgroundFromNoiseStep() {
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < worldHeight; y++) {
                chunkRef.setBackgroundBlockAt(x, y, chunkRef.getBlockAt(x, y));
            }
        }
    }

    private void smoothNoiseCaves() {
        int size = CHUNK_WIDTH * worldHeight;
        boolean[] solid = new boolean[size];
        boolean[] nextSolid = new boolean[size];

        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < worldHeight; y++) {
                int idx = y * CHUNK_WIDTH + x;
                solid[idx] = !chunkRef.getBlockAt(x, y).getBlock().equals(Blocks.AIR);
                nextSolid[idx] = solid[idx];
            }
        }

        int minY = BEDROCK_LEVEL + 1;
        int maxY = Math.min(worldHeight - 2, seaLevel + 96);

        for (int x = 1; x < CHUNK_WIDTH - 1; x++) {
            for (int y = minY; y <= maxY; y++) {
                int idx = y * CHUNK_WIDTH + x;
                int solidNeighbors = 0;

                for (int ox = -1; ox <= 1; ox++) {
                    for (int oy = -1; oy <= 1; oy++) {
                        if (ox == 0 && oy == 0) {
                            continue;
                        }
                        int nidx = (y + oy) * CHUNK_WIDTH + (x + ox);
                        if (solid[nidx]) {
                            solidNeighbors++;
                        }
                    }
                }

                if (!solid[idx] && solidNeighbors >= 7) {
                    nextSolid[idx] = true;
                } else if (solid[idx] && solidNeighbors <= 1) {
                    nextSolid[idx] = false;
                }
            }
        }

        for (int x = 1; x < CHUNK_WIDTH - 1; x++) {
            double worldX = x + (CHUNK_WIDTH * chunkRef.getChunkPos().x);
            Biome biome = sampleBiomeAt(worldX);
            for (int y = minY; y <= maxY; y++) {
                int idx = y * CHUNK_WIDTH + x;
                boolean wasSolid = solid[idx];
                boolean nowSolid = nextSolid[idx];

                if (wasSolid == nowSolid) {
                    continue;
                }

                if (nowSolid) {
                    chunkRef.setBlockAt(x, y, getBlockForDepthAndBiome(y, biome));
                } else {
                    chunkRef.setBlockAt(x, y, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    // Legacy density function - kept for reference but not used
    public static final DensityFunction legacyFunction = new DensityFunctionBuilder(
            new NoiseDensity("minecraft:continentalness", 0.25, 1)).abs()
            .add(new YGradientDensity(0, 220, 1, -1))
            .add(new NoiseDensity("minecraft:erosion", 0.25, 0.25))
            .build();

    /**
     * Samples the biome at a specific world X coordinate.
     *
     * CRITICAL: Uses Math.floor for blockX conversion to handle negative coordinates
     * correctly. Plain (int) cast truncates toward zero, causing a 1-block discontinuity
     * at negative X values (e.g. worldX=-0.5 â†’ blockX=0 instead of -1).
     *
     * Passes Y=0 in vanilla Y space (which maps to internalY of seaLevel roughly),
     * consistent with how vanilla Climate.Sampler samples at the sea level plane.
     */
    private Biome sampleBiomeAt(double worldX) {
        // FIXED: use Math.floor to correctly handle negative X coordinates.
        // (int) truncates toward zero; Math.floor always rounds toward negative infinity.
        int blockX = (int) Math.floor(worldX);
        double continentalness = vanillaNoiseStep.sampleContinentalness(blockX, 0, 0);
        double temperature = vanillaNoiseStep.sampleTemperature(blockX, 0, 0);
        double humidity = vanillaNoiseStep.sampleHumidity(blockX, 0, 0);
        double weirdness = vanillaNoiseStep.sampleWeirdness(blockX, 0, 0);
        double erosion = vanillaNoiseStep.sampleErosion(blockX, 0, 0);
        return BiomeManager.sampleBiome(continentalness, temperature, humidity, weirdness, erosion);
    }


    private void placeOres(Xoroshiro r) {
        Map<Integer, Integer> surfaceCache = new HashMap<>();
        for (var feature : worldgen.getOreFeatures()) {
            int attempts = Math.max(0, feature.count()/4);
            for (int i = 0; i < attempts; i++) {
                if (feature.rarity() > 1 && r.nextInt(feature.rarity()) != 0) {
                    continue;
                }

                int localX = r.nextInt(CHUNK_WIDTH);
                int y = sampleOreY(feature.range(), r);
                int worldX = localX + (CHUNK_WIDTH * chunkRef.getChunkPos().x);
                placeOreVein(localX, y, worldX, feature, r, surfaceCache);
            }
        }
    }

    private void placeBiomeFeatures() {
        int chunkWorldX = CHUNK_WIDTH * chunkRef.getChunkPos().x;

        for (int localX = 0; localX < CHUNK_WIDTH; localX++) {
            int worldX = chunkWorldX + localX;
            Biome biome = sampleBiomeAt(worldX);

            var featureStages = featureRegistry.getBiomeFeatures(biome);
            if (featureStages == null || featureStages.isEmpty()) {
                continue;
            }

            for (int stageIdx = 0; stageIdx < featureStages.size(); stageIdx++) {
                var stage = featureStages.get(stageIdx);
                if (stage == null) continue;

                for (var placedFeature : stage) {
                    if (placedFeature == null) continue;

                    String featureType = placedFeature.featureType();

                    if (!placedFeature.shouldPlace(featurerandom)) {
                        continue;
                    }

                    switch (featureType) {
                        case "tree" -> placeTreeFeature(localX, worldX, placedFeature);
                        case "random_selector" -> placeTreeFeature(localX, worldX, placedFeature);
                        case "flower", "random_patch" -> placeFlowerFeature(localX, worldX, placedFeature);
                        case "ore" -> placeOreFeature(worldX, placedFeature);
                        case "spring_feature" -> placeSpringFeature(worldX, placedFeature);
                        case "lake" -> placeLakeFeature(worldX, placedFeature);
                        case "disk" -> placeDiskFeature(worldX, placedFeature);
                        case "block_pile" -> placeBlockPileFeature(worldX, placedFeature);
                        case "huge_fungus" -> placeMushroomFeature(worldX, placedFeature, true);
                        case "mushroom" -> placeMushroomFeature(worldX, placedFeature, false);
                        case "glow_lichen" -> placeGlowLichenFeature(worldX, placedFeature);
                        case "simple_block" -> placeSimpleBlockFeature(worldX, placedFeature);
                        case "vegetation" -> placeVegetationFeature(worldX, placedFeature);
                        default -> {
                        }
                    }
                }
            }
        }
    }

    private void placeTreeFeature(int localX, int worldX, FeatureRegistry.PlacedFeature feature) {
        int surfaceY = findSurfaceY(worldX);
        if (surfaceY < 1 || surfaceY >= CHUNK_HEIGHT - 5) return;

        BlockState below = chunkRef.getBlockAt(localX, surfaceY - 1);

        if (!below.getBlock().equals(Blocks.GRASS_BLOCK) && !below.getBlock().equals(Blocks.DIRT)) {
            return;
        }

        FeatureRegistry.TreeType treeType = featureRegistry.detectTreeType(feature);

        int treeHeight;
        Block logBlock;
        Block leafBlock;

        switch (treeType) {
            case BIRCH -> {
                treeHeight = 5 + featurerandom.nextInt(2);
                logBlock = Blocks.OAK_LOG;
                leafBlock = Blocks.OAK_LEAVES;
            }
            case SPRUCE -> {
                treeHeight = 6 + featurerandom.nextInt(3);
                logBlock = Blocks.OAK_LOG;
                leafBlock = Blocks.OAK_LEAVES;
            }
            case JUNGLE -> {
                treeHeight = 8 + featurerandom.nextInt(4);
                logBlock = Blocks.OAK_LOG;
                leafBlock = Blocks.OAK_LEAVES;
            }
            case MEGA -> {
                treeHeight = 10 + featurerandom.nextInt(5);
                logBlock = Blocks.OAK_LOG;
                leafBlock = Blocks.OAK_LEAVES;
            }
            default -> {
                treeHeight = 4 + featurerandom.nextInt(3);
                logBlock = Blocks.OAK_LOG;
                leafBlock = Blocks.OAK_LEAVES;
            }
        }

        for (int i = 0; i < treeHeight; i++) {
            if (surfaceY + i < CHUNK_HEIGHT) {
                chunkRef.setBlockAt(localX, surfaceY + i, logBlock.defaultBlockState());
            }
        }

        int leafStart = surfaceY + treeHeight - 2;
        int leafEnd = surfaceY + treeHeight + 1;

        for (int ly = leafStart; ly <= leafEnd; ly++) {
            if (ly < 0 || ly >= CHUNK_HEIGHT) continue;
            int radius = ly == leafStart ? 1 : (ly == leafEnd ? 1 : 2);
            for (int lx = -radius; lx <= radius; lx++) {
                int wx = localX + lx;
                if (wx < 0 || wx >= CHUNK_WIDTH) continue;
                BlockState existing = chunkRef.getBlockAt(wx, ly);
                if (existing.getBlock().equals(Blocks.AIR)) {
                    chunkRef.setBlockAt(wx, ly, getNaturalLeafState(leafBlock.defaultBlockState()));
                }
            }
        }

        chunkRef.setBlockAt(localX, surfaceY - 1, Blocks.DIRT.defaultBlockState());
    }

    private void placeFlowerFeature(int localX, int worldX, FeatureRegistry.PlacedFeature feature) {
        int surfaceY = findSurfaceY(worldX);
        if (surfaceY < 1 || surfaceY >= CHUNK_HEIGHT - 1) return;

        BlockState surface = chunkRef.getBlockAt(localX, surfaceY);

        if (!surface.getBlock().equals(Blocks.GRASS_BLOCK) && !surface.getBlock().equals(Blocks.DIRT)) {
            return;
        }

        int flowerType = featurerandom.nextInt(14);
        Block flower = switch (flowerType) {
            case 0 -> Blocks.ROSE;
            case 1 -> Blocks.CORNFLOWER;
            case 2 -> Blocks.ALLIUM;
            case 3 -> Blocks.AZURE_BLUET;
            case 4 -> Blocks.LILY_OF_THE_VALLEY;
            case 5 -> Blocks.OXEYE_DAISY;
            case 6 -> Blocks.PURPLE_TULIP;
            case 7 -> Blocks.WHITE_TULIP;
            case 8 -> Blocks.RED_TULIP;
            case 9 -> Blocks.ORANGE_TULIP;
            case 10 -> Blocks.DANDILION;
            case 11 -> Blocks.LILAC_TOP;
            case 12 -> Blocks.PEONY;
            default -> Blocks.TALL_GRASS;
        };

        chunkRef.setBlockAt(localX, surfaceY + 1, flower.defaultBlockState());
    }

    private void placeOreFeature(int worldX, FeatureRegistry.PlacedFeature feature) {
    }

    private void placeSpringFeature(int worldX, FeatureRegistry.PlacedFeature feature) {
    }

    private void placeLakeFeature(int worldX, FeatureRegistry.PlacedFeature feature) {
        int localX = worldX - CHUNK_WIDTH * chunkRef.getChunkPos().x;
        int surfaceY = findSurfaceY(worldX);

        if (surfaceY > 0 && surfaceY < CHUNK_HEIGHT - 1) {
            BlockState surface = chunkRef.getBlockAt(localX, surfaceY);
            if (surface.getBlock().equals(Blocks.AIR) || BlockTags.FLUID.contains(surface.getBlock())) {
                if (surfaceY <= seaLevel) {
                    chunkRef.setBlockAt(localX, surfaceY, Blocks.WATER.defaultBlockState());
                }
            }
        }
    }

    private void placeDiskFeature(int worldX, FeatureRegistry.PlacedFeature feature) {
        int localX = worldX - CHUNK_WIDTH * chunkRef.getChunkPos().x;

        for (int y = 1; y < CHUNK_HEIGHT - 1; y++) {
            BlockState here = chunkRef.getBlockAt(localX, y);
            BlockState below = chunkRef.getBlockAt(localX, y - 1);

            if (below.getBlock().equals(Blocks.DIRT) || below.getBlock().equals(Blocks.GRASS_BLOCK)) {
                if (here.getBlock().equals(Blocks.AIR) || BlockTags.FLUID.contains(here.getBlock())) {
                    if (y <= seaLevel + 1) {
                        chunkRef.setBlockAt(localX, y, Blocks.SAND.defaultBlockState());
                    }
                }
                break;
            }
        }
    }

    private void placeBlockPileFeature(int worldX, FeatureRegistry.PlacedFeature feature) {
    }

    private void placeMushroomFeature(int worldX, FeatureRegistry.PlacedFeature feature, boolean huge) {
    }

    private void placeGlowLichenFeature(int worldX, FeatureRegistry.PlacedFeature feature) {
    }

    private void placeSimpleBlockFeature(int worldX, FeatureRegistry.PlacedFeature feature) {
    }

    private void placeVegetationFeature(int worldX, FeatureRegistry.PlacedFeature feature) {
        int localX = worldX - CHUNK_WIDTH * chunkRef.getChunkPos().x;
        placeFlowerFeature(localX, worldX, feature);
    }

    private int findSurfaceY(int worldX) {
        int localX = worldX - CHUNK_WIDTH * chunkRef.getChunkPos().x;

        for (int y = CHUNK_HEIGHT - 2; y >= 1; y--) {
            BlockState here = chunkRef.getBlockAt(localX, y);
            BlockState above = chunkRef.getBlockAt(localX, y + 1);
            if (!here.getBlock().equals(Blocks.AIR) && !BlockTags.FLUID.contains(here.getBlock())
                    && (above.getBlock().equals(Blocks.AIR) || BlockTags.FLUID.contains(above.getBlock()))) {
                return y;
            }
        }
        return seaLevel;
    }

    private int sampleOreY(VanillaWorldgenConfig.HeightRange range, Xoroshiro random) {
        int minY = range.minY();
        int maxY = range.maxY();
        if (maxY <= minY) {
            return minY;
        }
        if (!range.trapezoid()) {
            return minY + random.nextInt(maxY - minY + 1);
        }
        int a = minY + random.nextInt(maxY - minY + 1);
        int b = minY + random.nextInt(maxY - minY + 1);
        return (a + b) / 2;
    }

    private void placeOreVein(int localX, int y, int worldX, VanillaWorldgenConfig.OreFeature feature, Xoroshiro random,
                              Map<Integer, Integer> surfaceCache) {
        int gx = worldX;
        int gy = y;
        int size = Math.max(1, feature.size());
        for (int i = 0; i < size; i++) {
            BlockState target = selectOreTarget(gy, feature);
            placeOreBlock(gx, gy, target, surfaceCache);

            gx += random.nextInt(3) - 1;
            gy += random.nextInt(3) - 1;
        }
    }

    private BlockState selectOreTarget(int y, VanillaWorldgenConfig.OreFeature feature) {
        boolean wantDeepslate = y < deepslateLevel;
        BlockState fallback = feature.targets().get(0).state();
        for (var target : feature.targets()) {
            if (target.deepslatePreferred() == wantDeepslate) {
                return target.state();
            }
        }
        return fallback;
    }

    private void placeOreBlock(int worldX, int y, BlockState state, Map<Integer, Integer> surfaceCache) {
        if (y < 0 || y >= CHUNK_HEIGHT) return;
        if (isInsideStructure(worldX, y)) return;

        int surfaceY = getSurfaceYAtWorldX(worldX, surfaceCache);
        if (y >= surfaceY - 3) return;

        int chunkX = World.getChunkX(worldX);
        int localX = World.getLocalX(worldX);
        Chunk targetChunk = Minecraft.getInstance().getWorld().getChunkIfExists(chunkX);
        if (targetChunk == null || !targetChunk.getStatus().isAtLeast(ChunkStatus.NOISE)) {
            return;
        }

        BlockState current = targetChunk.getBlockAt(localX, y);
        if (current.getBlock().equals(Blocks.GRASS_BLOCK) || current.getBlock().equals(Blocks.DIRT)
                || current.getBlock().equals(Blocks.SAND) || BlockTags.FLUID.contains(current.getBlock())) {
            return;
        }

        if (current.getBlock().equals(Blocks.STONE) || current.getBlock().equals(Blocks.DEEPSLATE)) {
            targetChunk.setBlockAt(localX, y, state);
        }
    }

    private int getSurfaceYAtWorldX(int worldX, Map<Integer, Integer> surfaceCache) {
        Integer cached = surfaceCache.get(worldX);
        if (cached != null) {
            return cached;
        }

        int chunkX = World.getChunkX(worldX);
        int localX = World.getLocalX(worldX);
        Chunk chunk = Minecraft.getInstance().getWorld().getChunkIfExists(chunkX);
        int surface = seaLevel;

        if (chunk != null && chunk.getStatus().isAtLeast(ChunkStatus.NOISE)) {
            for (int y = CHUNK_HEIGHT - 2; y >= 1; y--) {
                BlockState here = chunk.getBlockAt(localX, y);
                BlockState above = chunk.getBlockAt(localX, y + 1);
                if (!here.getBlock().equals(Blocks.AIR) && !BlockTags.FLUID.contains(here.getBlock())
                        && (above.getBlock().equals(Blocks.AIR) || BlockTags.FLUID.contains(above.getBlock()))) {
                    surface = y;
                    break;
                }
            }
        } else {
            int minY = worldgen.getMinY();
            for (int y = CHUNK_HEIGHT - 2; y >= 1; y--) {
                int vanillaY = minY + y;
                double here = vanillaNoiseStep.sampleFinalDensity2D(worldX, vanillaY);
                double above = vanillaNoiseStep.sampleFinalDensity2D(worldX, vanillaY + 1);
                if (here > 0.0 && above <= 0.0) {
                    surface = y;
                    break;
                }
            }
        }

        surfaceCache.put(worldX, surface);
        return surface;
    }

    private void generateBaseTerrain() {
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            if (Thread.currentThread().isInterrupted()) {
                Logger.INFO("Interrupted!");
                return;
            }

            double worldX = x + (CHUNK_WIDTH * chunkRef.getChunkPos().x);

            // Also get the primary biome for surface block decisions
            Biome primaryBiome = sampleBiomeAt(worldX);
            chunkRef.setBiome(x, primaryBiome);

            for (int y = 0; y < worldHeight; y++) {
                int vanillaY = worldgen.getMinY() + y;
                double density = vanillaNoiseStep.sampleFinalDensity2D((int) Math.floor(worldX), vanillaY);

                if (density > 0) {
                    // Solid block - determine type based on biome and depth
                    BlockState blockState = getBlockForDepthAndBiome(y, primaryBiome);
                    chunkRef.setBlockAt(x, y, blockState);
                } else {
                    // Air/water
                    chunkRef.setBlockAt(x, y, Blocks.AIR.defaultBlockState());
                }
            }
        }
        chunkRef.markBiomeCachePopulated();

    }

    /**
     * Determines the appropriate block type based on depth and biome.
     */
    private BlockState getBlockForDepthAndBiome(int y, Biome biome) {
        if (y < deepslateLevel) {
            return Blocks.DEEPSLATE.defaultBlockState();
        }

        if (y < deepslateLevel + 5) {
            return Blocks.STONE.defaultBlockState();
        }

        return worldgen.getDefaultBlock();
    }

    private void applyWaterLayer() {
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < seaLevel; y++) {
                if (chunkRef.getBlockAt(x, y).getBlock().equals(Blocks.AIR)) {
                    chunkRef.setBlockAt(x, y, worldgen.getDefaultFluid());
                }
            }
        }
    }

    private void generateSurfaceLayers(boolean placeTrees) {
        if (Thread.currentThread().isInterrupted()) {
            Logger.INFO("Interrupted!");
            return;
        }
        surfaceRuleEngine.applyToChunk(chunkRef, wx -> sampleBiomeAt(wx));
    }


    /**
     * Separate tree placement that can access neighbour chunks
     */
    private void placeTrees() {
        int maxTreeHeight = worldHeight - 20;
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            double worldX = x + (CHUNK_WIDTH * chunkRef.getChunkPos().x);
            Biome biome = sampleBiomeAt(worldX);

            // Only place trees up to max tree height
            for (int y = 2; y < maxTreeHeight; y++) {
                BlockState currentBlock = chunkRef.getBlockAt(x, y);
                BlockState belowBlock = chunkRef.getBlockAt(x, y - 1);

                if (canPlaceTreeInBiome(biome) &&
                        belowBlock.getBlock().equals(Blocks.GRASS_BLOCK) &&
                        currentBlock.getBlock().equals(Blocks.AIR) &&
                        shouldPlaceTreeInBiome(biome) &&
                        !isInsideStructure((int) worldX, y)) { // Check structure bounds

                    placeTree(x, y, biome);
                }
            }
        }
    }

    /**
     * Checks if a point is inside any structure's bounding box.
     * Structures have priority over features.
     */
    private boolean isInsideStructure(int worldX, int worldY) {
        var structureStarts = Minecraft.getInstance().getWorld().structureManager.getStructureStartsForChunk(chunkRef.getChunkPos());
        for (var start : structureStarts) {
            if (start.contains(worldX, worldY)) {
                return true;
            }
        }
        return false;
    }



    /**
     * Checks if tree can go in biome
     */
    private boolean canPlaceTreeInBiome(Biome biome) {
        String biomeName = biome.getName().toString();

        // No trees in deserts or oceans
        if (biomeName.contains("desert") || biomeName.contains("ocean")) {
            return false;
        }

        return true;
    }


    private boolean shouldPlaceTreeInBiome(Biome biome) {
        String biomeName = biome.getName().toString();
        float threshold = 0.9f; // Default: sparse trees

        // Dense forests
        if (biomeName.contains("jungle") || biomeName.contains("dark_forest")) {
            threshold = 0.9f;
        }
        // Regular forests
        else if (biomeName.contains("forest") || biomeName.contains("taiga")) {
            threshold = 0.95f;
        }
        // Sparse biomes
        else if (biomeName.contains("plains") || biomeName.contains("savanna")) {
            threshold = 0.98f;
        }

        return featurerandom.nextFloat() > threshold;
    }

    private void placeTree(int localX, int baseY, Biome biome) {
        // dont place them too high //FIXES crash
        if (baseY < 5 || baseY > worldHeight - 20) {
            return;
        }

        int globalX = localX + (chunkRef.getChunkPos().x * CHUNK_WIDTH);
        String biomeName = biome.getName().toString();


        Minecraft minecraft = Minecraft.getInstance();

        TreeType treeType = getTreeTypeForBiome(biomeName);

        int trunkHeight = treeType.trunkHeight + featurerandom.nextInt(treeType.trunkVariation);
        int leafHeight = treeType.leafHeight;
        int totalTreeHeight = trunkHeight + leafHeight;

        if (baseY + totalTreeHeight >= worldHeight - 5) {
            trunkHeight = Math.min(trunkHeight, worldHeight - baseY - leafHeight - 5);
            if (trunkHeight < 3) return;
        }

        if (isInsideStructure(globalX, baseY - 1)) {
            return;
        }

        minecraft.getWorld().setBlock(globalX, baseY - 1, Blocks.DIRT.defaultBlockState());

        for (int i = 0; i < trunkHeight; i++) {
            int y = baseY + i;
            if (y >= worldHeight) break;

            if (isInsideStructure(globalX, y)) continue;
            if (i < 4) {
                minecraft.getWorld().setBlock(globalX, y, treeType.logBlock);
            } else {
                minecraft.getWorld().setBlockSafe(globalX, y, treeType.logBlock);
            }
        }


        int leafStart = baseY + trunkHeight - 2;

        for (int ly = 0; ly < leafHeight; ly++) {
            int y = leafStart + ly;
            if (y < 0 || y >= worldHeight) continue;

            int radius = (leafHeight - ly) / 2 + 1;

            for (int lx = -radius; lx <= radius; lx++) {
                if (Math.abs(lx) <= radius - (ly % 2)) {
                    int leafX = globalX + lx;
                    if (leafX < Integer.MIN_VALUE / 16 || leafX > Integer.MAX_VALUE / 16) continue;
                    if (!isInsideStructure(leafX, y)) {
                        minecraft.getWorld().setBlockSafe(leafX, y, getNaturalLeafState(treeType.leafBlock));
                    }
                }
            }
        }
    }

   /**
    * Pick a tree
    * @param biomeName - the biome
    * @return tree
    */
    private TreeType getTreeTypeForBiome(String biomeName) {
        // Taiga biomes - spruce
        if (biomeName.contains("taiga") || biomeName.contains("snowy")) {
            return TreeType.SPRUCE;
        }
        // Birch forests
        else if (biomeName.contains("birch")) {
            return TreeType.BIRCH;
        }
        // Jungles
        else if (biomeName.contains("jungle")) {
            return TreeType.JUNGLE;
        }
        // Savanna
        else if (biomeName.contains("savanna")) {
            return TreeType.ACACIA;
        }
        // Default - oak
        else {
            return TreeType.OAK;
        }
    }

    private BlockState getNaturalLeafState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.PERSISTENT)) {
            return state.setValue(BlockStateProperties.PERSISTENT, false);
        }
        return state;
    }

    /**
     * Enum representing different tree types with their properties.
     * Currently only oak blocks are available, so we vary the shape.
     */
    private enum TreeType {
        OAK(Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState(), 4, 2, 4),
        SPRUCE(Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState(), 6, 3, 5), // Tall and thin
        BIRCH(Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState(), 5, 2, 4),  // Medium
        JUNGLE(Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState(), 8, 4, 6), // Tall with big canopy
        ACACIA(Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState(), 4, 2, 3); // Short with flat top

        final BlockState logBlock;
        final BlockState leafBlock;
        final int trunkHeight;
        final int trunkVariation;
        final int leafHeight;

        TreeType(BlockState log, BlockState leaves, int trunkHeight, int trunkVar, int leafHeight) {
            this.logBlock = log;
            this.leafBlock = leaves;
            this.trunkHeight = trunkHeight;
            this.trunkVariation = trunkVar;
            this.leafHeight = leafHeight;
        }
    }

    private void generateDeepslate(Xoroshiro random) {
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < deepslateLevel; y++) {
                BlockState currentBlock = chunkRef.getBlockAt(x, y);

                if (currentBlock.getBlock().equals(Blocks.DEEPSLATE)) {
                    BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();

                    for (int i = 0; i < featurerandom.nextInt(4); i++) {
                        deepslate = deepslate.cycle(BlockStateProperties.FACING);
                    }

                    chunkRef.setBlockAt(x, y, deepslate);
                }
            }

            //blend a bit of stone
            for (int y = deepslateLevel; y < deepslateLevel + 5; y++) {
                if (featurerandom.nextFloat() > 0.5f) {
                    BlockState currentBlock = chunkRef.getBlockAt(x, y);
                    if (currentBlock.getBlock().equals(Blocks.STONE)) {
                        BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
                        for (int i = 0; i < featurerandom.nextInt(4); i++) {
                            deepslate = deepslate.cycle(BlockStateProperties.FACING);
                        }
                        chunkRef.setBlockAt(x, y, deepslate);
                    }
                }
            }
        }
    }

    private void generateBedrock(Xoroshiro random) {
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 1; y < 5; y++) {
                if (chunkRef.getBlockAt(x, y).getBlock().equals(Blocks.AIR)) {
                    chunkRef.setBlockAt(x, y, worldgen.getDefaultFluid());
                }
            }
           //bottom is bedrock
            chunkRef.setBlockAt(x, 0, Blocks.BEDROCK.defaultBlockState());
            for (int y = 1; y < BEDROCK_LEVEL; y++) {
                if (featurerandom.nextFloat() > 0.5f) {
                    chunkRef.setBlockAt(x, y, Blocks.BEDROCK.defaultBlockState());
                }
            }
        }
    }
}
