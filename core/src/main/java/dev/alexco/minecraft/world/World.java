package dev.alexco.minecraft.world;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;
import static dev.alexco.minecraft.SharedConstants.CHUNK_WIDTH;
import static dev.alexco.minecraft.SharedConstants.RENDER_DISTANCE;
import static dev.alexco.minecraft.SharedConstants.TICKING_DISTANCE;
import static dev.alexco.minecraft.SharedConstants.WORLDGEN_DISTANCE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Gdx;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.blaze2d.special.SolidityAABB;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.loot.LootTableManager;
import dev.alexco.minecraft.phys.AABB;
import dev.alexco.minecraft.phys.AABBPool;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.world.entity.BlockItemEntity;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.entity.ItemEntity;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.entity.spawn.HostileMobSpawner;
import dev.alexco.minecraft.world.entity.spawn.PassiveMobSpawner;
import dev.alexco.minecraft.world.entity.spawn.SpawnRulesManager;
import dev.alexco.minecraft.world.level.block.BarrelBlock;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.block.DoorBlock;
import dev.alexco.minecraft.world.level.block.FurnaceBlock;
import dev.alexco.minecraft.world.level.block.entity.FurnaceBlockEntity;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkPos;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;
import dev.alexco.minecraft.world.level.item.BlockItem;
import dev.alexco.minecraft.world.level.item.Items;
import dev.alexco.minecraft.world.level.levelgen.structure.StructureManager;
import dev.alexco.minecraft.world.level.levelgen.structure.StructurePlacement;
import dev.alexco.minecraft.world.level.levelgen.structure.TerrainFollowingStructure;
import dev.alexco.minecraft.world.level.levelgen.structure.structures.DirtPath;
import dev.alexco.minecraft.world.level.levelgen.structure.structures.Dungeon;
import dev.alexco.minecraft.world.level.levelgen.structure.structures.HouseStructure;
import dev.alexco.minecraft.world.level.block.entity.BarrelBlockEntity;
import dev.alexco.minecraft.world.level.block.entity.BlockEntityManager;
import dev.alexco.minecraft.world.level.light.LightEngine;
import dev.alexco.minecraft.world.serialisation.ChunkFile;
import dev.alexco.minecraft.world.serialisation.WorldFile;
import net.querz.nbt.tag.CompoundTag;
/**
 * A world contains all the data for a single world, including the player, the loaded chunks,
 * entities, and the world data. It is responsible for ticking the world, loading and unloading
 * chunks, and saving the world to disk. It also contains the structure manager and block entity manager
 * which are responsible for managing structures and block entities in the world.
 *
 * The world does not directly manage the camera, but it does store the camera offset in the world data.
 *
 */
public class World {
    //metadata
    public WorldData worldData;
    //player
    public Player player;
    //chunks
    public Map<ChunkPos, Chunk> worldChunks;
    //mobs and entities that are NOT the player
    public List<Entity> entities;
    //current world seed.
    public long seed;
    //how long the world has been ticked for (daynight cycle)
    public long gameTime = 0;

    public StructureManager structureManager;
    private BlockEntityManager blockEntityManager;
    private PassiveMobSpawner passiveMobSpawner;
    private HostileMobSpawner hostileMobSpawner;
    //world info that is stored separate but nice to have
    private String worldName;
    private String saveFolderName;
    private boolean isNewWorld = true;

    /**
     * Creates a world with default runtime data and structure registration.
     */
    public World() {
        Logger.INFO("Creating world data.");
        this.worldData = new WorldData();
        SpawnRulesManager.SpawnConfig passiveSpawnConfig = SpawnRulesManager.getPassiveConfig();
        SpawnRulesManager.SpawnConfig hostileSpawnConfig = SpawnRulesManager.getHostileConfig();
        worldData.passiveSpawnIntervalTicks = passiveSpawnConfig.getSpawnIntervalTicks();
        worldData.hostileSpawnIntervalTicks = hostileSpawnConfig.getSpawnIntervalTicks();
        worldData.passiveMobCap = passiveSpawnConfig.getSpawnCap();
        worldData.hostileMobCap = hostileSpawnConfig.getSpawnCap();
        worldData.blockSize = 32f;
        worldData.cameraX = 0 * worldData.blockSize;
        worldData.cameraY = worldData.blockSize * 128f;
        this.worldChunks = new ConcurrentHashMap<>();
        this.entities = new ArrayList<>();
        seed = new Random().nextLong();
        // this.player = new Player(0, 128);
        Logger.INFO("Creating block entity manager");
        this.blockEntityManager = new BlockEntityManager(this);
        Logger.INFO("Creating structure manager");
        this.structureManager = new StructureManager(this);
        this.passiveMobSpawner = new PassiveMobSpawner();
        this.hostileMobSpawner = new HostileMobSpawner();

        Logger.INFO("Registering structures");
        structureManager.registerStructure(Dungeon.dungeonPlacement);
        Logger.INFO("Registered 1/3");
        structureManager.registerStructure(DirtPath.dirt_path);
        Logger.INFO("Registered 2/3");
        structureManager.registerStructure(HouseStructure.house_placement);
        Logger.INFO("Registered 3/3");

    Logger.INFO("=== World Initialised ===");
    Logger.INFO("Seed: %d", seed);
    Logger.INFO("Registered structure types: %d", structureManager.structurePlacements.size());
    for (StructurePlacement p : structureManager.structurePlacements) {
        Logger.INFO("  - %s (spacing=%d, separation=%d, chance=%.2f)",
            p.getStructure().getName(), p.getSpacing(), p.getSeparation(), p.spawnChance);
    }

        }

    public void setWorldInfo(String name, String folderName) {
        this.worldName = name;
        this.saveFolderName = folderName;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getSaveFolderName() {
        return saveFolderName;
    }

    public void setIsNewWorld(boolean isNew) {
        this.isNewWorld = isNew;
    }

    public boolean isNewWorld() {
        return isNewWorld;
    }

    /**
     * Saves world metadata, generated chunks and player data to disk.
     */
    public void saveWorld() {
        if (saveFolderName == null) {
            Logger.ERROR("Cannot save world: no save folder name set");
            return;
        }

        Logger.INFO("Saving world: %s", worldName);
        WorldFile.writeWorldToDisk(this, saveFolderName, worldName);

        for (Chunk chunk : worldChunks.values()) {
            if (chunk.getStatus().ordinal() >= ChunkStatus.FEATURES.ordinal()) {
                ChunkFile.writeChunkToDisk(chunk, saveFolderName);
            }
        }

        if (Minecraft.getInstance().getPlayer() != null) {
            dev.alexco.minecraft.world.serialisation.PlayerFile.writePlayerToDisk(
                Minecraft.getInstance().getPlayer(), saveFolderName);
        }

        Logger.INFO("World saved successfully");
    }

    public void saveChunk(Chunk chunk) {
        if (saveFolderName == null) return;
        ChunkFile.writeChunkToDisk(chunk, saveFolderName);
    }



    private final Map<Integer, ChunkPos> chunkPosCache = new ConcurrentHashMap<>();

    /**
     * Returns a cached ChunkPos instance for a chunk X coordinate.
     */
    private ChunkPos getCachedChunkPos(int x) {
        return chunkPosCache.computeIfAbsent(x, ChunkPos::new);
    }

    /**
     * Gets or creates a chunk, loading persisted data when available.
     */
 public Chunk getChunk(ChunkPos position) {
        return worldChunks.computeIfAbsent(position, pos -> {
            Chunk c = new Chunk(pos);

            if (saveFolderName != null && !isNewWorld) {
                CompoundTag chunkData = ChunkFile.readChunkData(saveFolderName, pos.x);
                if (chunkData != null) {
                    ChunkFile.deserialiseChunk(c, chunkData, this);
                    c.build();
                }
            }
            return c;
        });
    }

    /**
     * Checks whether neighbour requirements are met for the next generation stage.
     */
     private boolean areNeighborsReady(ChunkPos pos, ChunkStatus requiredStatus) {
        // For FEATURES status, we need neighbours at SURFACE at minimum
        // int range = requiredStatus == ChunkStatus.FEATURES ? 1 : 0;

        // for (int dx = -range; dx <= range; dx++) {
        //     if (dx == 0) continue;

        //     Chunk neighbour = getChunkIfExists(pos.x + dx);
        //     if (neighbour == null || !neighbour.getStatus().isAtLeast(requiredStatus)) {
        //         return false;
        //     }
        // }
        return true;
    }
    public Chunk getChunk(int x) {
        return getChunk(getCachedChunkPos(x));
    }

    public static int getChunkX(double x) {
        return (int) Math.floor((double) x / SharedConstants.CHUNK_WIDTH);
    }

    public static int getLocalX(int x) {
        int localX = x % SharedConstants.CHUNK_WIDTH;
        if (localX < 0) {
            localX += SharedConstants.CHUNK_WIDTH;
        }
        return localX;
    }

    public BlockState getBlock(int globalX, int globalY) {
        if (globalY < 0 || globalY >= CHUNK_HEIGHT) {
            return Blocks.AIR.defaultBlockState();
        }
        Chunk chunk = worldChunks.get(getCachedChunkPos(getChunkX(globalX)));
        if (chunk == null) {
            return Blocks.AIR.defaultBlockState();
        }
        return chunk.getBlockAt(getLocalX(globalX), globalY);
    }

    public BlockState getBackgroundBlock(int globalX, int globalY) {
        if (globalY < 0 || globalY >= CHUNK_HEIGHT) {
            return Blocks.AIR.defaultBlockState();
        }
        Chunk chunk = worldChunks.get(getCachedChunkPos(getChunkX(globalX)));
        if (chunk == null) {
            return Blocks.AIR.defaultBlockState();
        }
        return chunk.getBackgroundBlockAt(getLocalX(globalX), globalY);
    }

    public void setBlock(int globalX, int globalY, BlockState blockState) {
        BlockState oldState = getBlock(globalX, globalY);
        if (oldState != null && (oldState.getBlock() instanceof FurnaceBlock||oldState.getBlock() instanceof BarrelBlock) ) {
            blockEntityManager.removeBlockEntity(globalX, globalY);
        }

        worldChunks.get(new ChunkPos(getChunkX(globalX))).setBlockAt(getLocalX(globalX), globalY, blockState);

        if (blockState.getBlock() instanceof FurnaceBlock) {
            FurnaceBlockEntity blockEntity = new FurnaceBlockEntity(globalX, globalY);
            blockEntityManager.addBlockEntity(globalX, globalY, blockEntity);
        }
        if (blockState.getBlock() instanceof BarrelBlock) {
            BarrelBlockEntity blockEntity = new BarrelBlockEntity(globalX, globalY);
            blockEntityManager.addBlockEntity(globalX, globalY, blockEntity);
        }

        if (globalY > 0 && !blockState.getBlock().isAir() && !blockState.getBlock().equals(Blocks.WHEAT)) {
            BlockState below = getBlock(globalX, globalY - 1);
            if (below.getBlock().equals(Blocks.FARMLAND)) {
                worldChunks.get(new ChunkPos(getChunkX(globalX))).setBlockAt(getLocalX(globalX), globalY - 1, Blocks.DIRT.defaultBlockState());
            }
        }

        breakUnsupportedBlocksAbove(globalX, globalY + 1);

        // Any block change can affect skylight and block-light occlusion.
        LightEngine.updateLightAt(globalX, globalY);
    }

    /**
     * Breaks support-dependent blocks when their base is removed.
     */
    private void breakUnsupportedBlocksAbove(int globalX, int startY) {
        for (int y = startY; y < CHUNK_HEIGHT; y++) {
            BlockState current = getBlock(globalX, y);
            if (!BlockTags.NEEDS_BASE_SUPPORT.contains(current.getBlock())) {
                break;
            }

            BlockState below = getBlock(globalX, y - 1);
            boolean unsupported = below.getBlock().isAir() || BlockTags.FLUID.contains(below.getBlock());
            if (!unsupported) {
                break;
            }

            Chunk chunk = worldChunks.get(new ChunkPos(getChunkX(globalX)));
            if (chunk == null) {
                break;
            }

            if (current.getBlock() instanceof DoorBlock) {
                breakUnsupportedDoor(globalX, y, current, chunk);
                break;
            }

            dropBlockStateAsEntities(current, globalX, y);
            chunk.setBlockAt(getLocalX(globalX), y, Blocks.AIR.defaultBlockState());
            LightEngine.updateLightAt(globalX, y);
        }
    }

    /**
     * Breaks both halves of an unsupported door and drops the item.
     */
    private void breakUnsupportedDoor(int globalX, int y, BlockState doorState, Chunk chunk) {
        int localX = getLocalX(globalX);
        boolean isTopHalf = doorState.getValue(dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties.TOP);
        int bottomY = isTopHalf ? y - 1 : y;
        int topY = bottomY + 1;

        if (bottomY >= 0 && bottomY < CHUNK_HEIGHT && getBlock(globalX, bottomY).getBlock() instanceof DoorBlock) {
            chunk.setBlockAt(localX, bottomY, Blocks.AIR.defaultBlockState());
            LightEngine.updateLightAt(globalX, bottomY);
        }
        if (topY >= 0 && topY < CHUNK_HEIGHT && getBlock(globalX, topY).getBlock() instanceof DoorBlock) {
            chunk.setBlockAt(localX, topY, Blocks.AIR.defaultBlockState());
            LightEngine.updateLightAt(globalX, topY);
        }

        entities.add(new ItemEntity(Items.OAK_DOOR_ITEM, globalX + 0.5D, bottomY));
    }

    /**
     * Spawns dropped item entities for a block state's loot table.
     */
    private void dropBlockStateAsEntities(BlockState blockState, int globalX, int globalY) {
        List<ItemStack> drops = LootTableManager.getDrops(blockState, Items.AIR);
        for (ItemStack drop : drops) {
            for (int i = 0; i < drop.amount; i++) {
                if (drop.item instanceof BlockItem blockItem) {
                    entities.add(new BlockItemEntity(blockItem, globalX + 0.5D, globalY));
                } else {
                    entities.add(new ItemEntity(drop.item, globalX + 0.5D, globalY));
                }
            }
        }
    }

    /**
     * Sets a block only if the target chunk exists and has terrain data.
     */
public void setBlockSafe(int globalX, int globalY, BlockState blockState) {
    if (globalY < 0 || globalY >= CHUNK_HEIGHT) return;

    ChunkPos pos = getCachedChunkPos(getChunkX(globalX));
    Chunk chunk = worldChunks.get(pos);

    // Just check if chunk exists and has terrain generated
    if (chunk != null && chunk.getStatus().ordinal() >= ChunkStatus.NOISE.ordinal()) {
        chunk.setBlockAt(getLocalX(globalX), globalY, blockState);
    }
}

    /**
     * Sets a background block if its chunk is currently loaded.
     */
public void setBackgroundBlock(int globalX, int globalY, BlockState blockState) {
        Chunk chunk = worldChunks.get(new ChunkPos(getChunkX(globalX)));
        if (chunk != null) {
            chunk.setBackgroundBlockAt(getLocalX(globalX), globalY, blockState);
        }
    }
    /**
     * Sets a background block only when the chunk is generated enough.
     */
        public void setBackgroundBlockSafe(int globalX, int globalY, BlockState blockState) {
        if (globalY < 0 || globalY >= CHUNK_HEIGHT) return;

        ChunkPos pos = getCachedChunkPos(getChunkX(globalX));
        Chunk chunk = worldChunks.get(pos);

        // Only set if chunk exists and is at least at SURFACE status
        if (chunk != null && chunk.getStatus().isAtLeast(ChunkStatus.SURFACE)) {
            chunk.setBackgroundBlockAt(getLocalX(globalX), globalY, blockState);
        }
    }

     public Chunk getChunkIfExists(int chunkX) {
        return worldChunks.get(getCachedChunkPos(chunkX));
    }

    /**
     * Persists and removes a chunk from memory.
     */
    private void unloadChunk(ChunkPos chunk) {
        if (worldChunks.get(chunk) == null) {
            worldChunks.remove(chunk);
            return;
        }

        Chunk chunkToUnload = worldChunks.get(chunk);
        if (chunkToUnload.getStatus().ordinal() >= ChunkStatus.FEATURES.ordinal()) {
            saveChunk(chunkToUnload);
        }

        chunkToUnload.unload(this);
        worldChunks.remove(chunk);
    }

    /**
     * Keeps chunk generation/unloading in sync with camera position.
     */
    public void calculateLoadedChunks() {
   int camChunkX = (int) (Math.floor(Minecraft.getInstance().getWorld().worldData.cameraX + Gdx.graphics.getWidth()/2f)
                / (CHUNK_WIDTH * Minecraft.getInstance().getWorld().worldData.blockSize));

        // Load chunks in worldgen distance (render + generation buffer).
        for (int chunkX = camChunkX - WORLDGEN_DISTANCE; chunkX <= camChunkX + WORLDGEN_DISTANCE; chunkX++) {
            Chunk chunk = getChunk(chunkX);

            // Progress chunk generation in stages
            progressChunkGeneration(chunk, camChunkX);
        }

        // Unload chunks outside worldgen distance.
        for (Chunk chunk : worldChunks.values()) {
            if (!chunk.amIInWorldgenDistance()) {
                unloadChunk(chunk.getChunkPos());
            }
        }
    }


   /**
    * Advances one chunk toward its target generation status.
    */
   private void progressChunkGeneration(Chunk chunk, int camChunkX) {
    if (chunk.amiGenerating()) {
        return;
    }

    ChunkStatus currentStatus = chunk.getStatus();

    // get distance
    int distance = Math.abs(chunk.getChunkPos().x - camChunkX);

    ChunkStatus targetStatus;
    if (distance <= TICKING_DISTANCE) {
        targetStatus = ChunkStatus.FULL;
    } else if (distance <= RENDER_DISTANCE) {
        targetStatus = ChunkStatus.FEATURES;
    } else if (distance <= RENDER_DISTANCE + 2) {
        targetStatus = ChunkStatus.STRUCTURES;
    } else if (distance <= WORLDGEN_DISTANCE) {
        targetStatus = ChunkStatus.SURFACE;
    } else {
        targetStatus = ChunkStatus.NOISE;
    }

    // Logger.INFO("Chunk %d: current=%s, target=%s, distance=%d",
    //     chunk.getChunkPos().x, currentStatus, targetStatus, distance);

    // Try to progress to next status if neighbours are ready
    if (currentStatus.ordinal() < targetStatus.ordinal()) {
        ChunkStatus nextStatus = currentStatus.getNext();

        // Logger.INFO("  Attempting to progress chunk %d from %s to %s",
        //     chunk.getChunkPos().x, currentStatus, nextStatus);

        // Check if neighbours are ready for this status
        if (nextStatus.getRange() > 0 && !areNeighborsReady(chunk.getChunkPos(), nextStatus)) {
            // Logger.INFO("  -> BLOCKED: Neighbours not ready for %s", nextStatus);
            return;
        }


        // Generate asynchronously
        final ChunkStatus statusToGenerate = nextStatus;
        final int scheduledChunkX = chunk.getChunkPos().x;
        Minecraft.getInstance().threads.execute(() -> {
            chunk.generateToStatus(statusToGenerate);
            if (!chunk.amiGenerating() && chunk.getStatus().isAtLeast(statusToGenerate) && chunk.amIDirty()) {
                Gdx.app.postRunnable(() -> {
                    Chunk live = getChunkIfExists(scheduledChunkX);
                    if (live == null || live != chunk || live.isUnloaded()) {
                        return;
                    }
                    live.build();
                });
            }
        });
    }
}
/**
 * Do not interact with the blockstate attached
 */
public void updateBlockState(int x, int y, BlockState newState) {
    BlockState oldState = getBlock(x, y);

    // it has to be the same block broadly
    if (oldState.getBlock() != newState.getBlock()) {
        throw new IllegalArgumentException(
            "updateBlockState() requires same block type. Use setBlock() for different blocks.");
    }

    int chunkX = World.getChunkX(x);
    Chunk chunk = getChunkIfExists(chunkX);
    if (chunk != null) {
        chunk.setBlockAt(getLocalX(x), y, newState);
        LightEngine.updateLightAt(x, y);
        chunk.build(); //rebuild
    }

}
    /**
     * Ticks chunks, spawners, block entities and runtime entities.
     */
    public void tick() {
        int playerChunkX = Minecraft.getInstance().getPlayer() != null
            ? World.getChunkX(Minecraft.getInstance().getPlayer().x)
            : (int) (Math.floor(Minecraft.getInstance().getWorld().worldData.cameraX + Gdx.graphics.getWidth()/2f)
                / (CHUNK_WIDTH * Minecraft.getInstance().getWorld().worldData.blockSize));

        for (Chunk chunk : worldChunks.values()) {
            if (Math.abs(chunk.getChunkPos().x - playerChunkX) > TICKING_DISTANCE) {
                continue;
            }
            chunk.tick();
        }

        Player sessionPlayer = Minecraft.getInstance().getPlayer();
        if (sessionPlayer != null) {
            passiveMobSpawner.tick(this, sessionPlayer);
            hostileMobSpawner.tick(this, sessionPlayer);
        }

        // Tick block entities
        blockEntityManager.tick();

        List<Entity> entitiesToRemove = new ArrayList<Entity>();

        for (Entity entity : new ArrayList<>(entities)) {
            if (entity.removed) continue;
            entity.tick(0);
            if (entity instanceof ItemEntity itemEntity) {
                double a = (entity.x - Minecraft.getInstance().getPlayer().xo);
                double b = (entity.y - Minecraft.getInstance().getPlayer().yo);

                if (Math.sqrt(a * a + b * b) < 1f) {
                    if (Minecraft.getInstance().getPlayer().inventory
                    .addItemToInventory(itemEntity.item)) {
                        entity.destroy();
                        entitiesToRemove.add(entity);
                        if (Minecraft.getInstance().getPlayer().inventory.getItemAtSlot(Minecraft.getInstance().getPlayer().slotSelected - 1) != null) {

                            Minecraft.getInstance().getPlayer().blockInHand = Minecraft.getInstance().getPlayer().inventory.getItemAtSlot(Minecraft.getInstance().getPlayer().slotSelected - 1).item;
                        } else {
                            Minecraft.getInstance().getPlayer().blockInHand = Items.AIR;
                        }
                    }
                }
            }
        }
        for (Entity entity : entitiesToRemove) {
            entities.remove(entity);
        }
        for (Entity entity : entities) {
            if (entity.removed) {
                entity.destroy();
            }
        }
        entities.removeIf(entity -> entity.removed);
    }

    /**
     * Collects collidable block boxes near a query AABB.
     */
    public ArrayList<SolidityAABB> getCubes(AABB aABB) {
        ArrayList<SolidityAABB> result = new ArrayList<>();
        int x0 = (int) aABB.x0 - 2;
        int x1 = (int) (aABB.x1 + 2);
        int y0 = (int) Math.floor(aABB.y0 - 2);
        int y1 = (int) Math.floor(aABB.y1 + 2);
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                if (y < 0 || y > CHUNK_HEIGHT - 1) {
                    continue;
                }
                try {

                    BlockState tile = getBlock(x, y);
                    Block block = tile.getBlock();
                    if (block.isAir()) {
                        continue;
                    }
                    block.addCollisionBoxes(tile, x, y, result);

                } catch (Exception e) {
                }
            }
        }

        return result;
    }

    /**
     * Updates camera offsets from player position and mouse sway.
     */
    public void moveCamera() {
        worldData.cameraX = Minecraft.getInstance().getPlayer().xo * worldData.blockSize;
        worldData.cameraX -= Gdx.graphics.getWidth() / 2f;
        if (!Minecraft.getInstance().isScreenOpen()){

            worldData.cameraX += (Gdx.input.getX() - (Gdx.graphics.getWidth() / 2f)) / 8f;
        }
        worldData.cameraY = (Minecraft.getInstance().getPlayer().yo+Minecraft.getInstance().getPlayer().heightOffset) * worldData.blockSize; //offset a bit to the player head
        worldData.cameraY -= Gdx.graphics.getHeight() / 2f;
                if (!Minecraft.getInstance().isScreenOpen()){

        worldData.cameraY -= (Gdx.input.getY() - (Gdx.graphics.getHeight() / 2f)) / 8f;
                }
    }

    public BlockEntityManager getBlockEntityManager() {
        return blockEntityManager;
    }
}
