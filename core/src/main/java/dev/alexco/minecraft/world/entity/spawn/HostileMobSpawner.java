package dev.alexco.minecraft.world.entity.spawn;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;
import static dev.alexco.minecraft.SharedConstants.CHUNK_WIDTH;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.entity.Mob;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.entity.Zombie;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;

public class HostileMobSpawner {
    private final Random random = new Random();
    private final SpawnRulesManager.SpawnConfig config;
    private final List<SpawnGroup> groups;
    private int naturalCounter = 0;
    private int spawnerCounter = 0;

    public HostileMobSpawner() {
        this.config = SpawnRulesManager.getHostileConfig();
        this.groups = new ArrayList<>(config.getGroups());
    }

    /**
     * Runs hostile spawn/despawn cycles around the player.
     */
    public void tick(World world, Player player) {
        if (player == null || !world.worldData.hostileMobSpawningEnabled) {
            return;
        }
        despawnFarHostiles(world, player);
        if (groups.isEmpty()) {
            naturalCounter = 0;
        }

        naturalCounter++;
        int hostileSpawnIntervalTicks = Math.max(1, world.worldData.hostileSpawnIntervalTicks);
        if (!groups.isEmpty() && naturalCounter >= hostileSpawnIntervalTicks) {
            naturalCounter = 0;
            spawnNaturalHostiles(world, player);
        }

        spawnerCounter++;
        if (spawnerCounter >= config.getSpawnerIntervalTicks()) {
            spawnerCounter = 0;
            spawnFromSpawners(world, player);
        }
    }

    /**
     * Attempts natural hostile spawns using weighted spawn groups.
     */
    private void spawnNaturalHostiles(World world, Player player) {
        int hostileCap = Math.max(0, world.worldData.hostileMobCap);
        if (countNaturalHostilesNearPlayer(world, player) >= hostileCap) {
            return;
        }

        int playerChunkX = World.getChunkX(player.x);
        for (int attempt = 0; attempt < config.getAttemptsPerCycle(); attempt++) {
            if (countNaturalHostilesNearPlayer(world, player) >= hostileCap) {
                return;
            }
            SpawnGroup group = pickWeightedGroup();
            if (group == null) {
                return;
            }
            int localRange = config.getLocalRangeChunks();
            int spawnChunkX = playerChunkX + random.nextInt(localRange * 2 + 1) - localRange;
            Chunk chunk = world.getChunkIfExists(spawnChunkX);
            if (chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
                continue;
            }
            int x = spawnChunkX * CHUNK_WIDTH + random.nextInt(CHUNK_WIDTH);
            int y = config.isRequireSurface() ? findSurfaceSpawnY(world, x) : (2 + random.nextInt(CHUNK_HEIGHT - 4));
            if (y < 0 || !group.getCondition().canSpawn(world, x, y)) {
                continue;
            }
            if (Math.abs(player.x - x) < config.getMinPlayerDistance()) {
                continue;
            }
            world.entities.add(group.create(x + 0.5D, y));
        }
    }

    /**
     * Attempts hostile spawns around active spawner blocks.
     */
    private void spawnFromSpawners(World world, Player player) {
        int playerChunkX = World.getChunkX(player.x);
        int localRange = config.getLocalRangeChunks();
        for (int cx = playerChunkX - localRange; cx <= playerChunkX + localRange; cx++) {
            Chunk chunk = world.getChunkIfExists(cx);
            if (chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
                continue;
            }
            int startX = cx * CHUNK_WIDTH;
            for (int lx = 0; lx < CHUNK_WIDTH; lx++) {
                for (int y = 1; y < CHUNK_HEIGHT - 2; y++) {
                    if (!chunk.getBlockAt(lx, y).getBlock().equals(Blocks.SPAWNER)) {
                        continue;
                    }
                    int sx = startX + lx;
                    int nearby = countZombiesNear(world, sx, y, config.getSpawnerRadius(), true);
                    if (nearby >= config.getSpawnerCap()) {
                        continue;
                    }

                    for (int attempts = 0; attempts < 6; attempts++) {
                        int ox = random.nextInt(config.getSpawnerRadius() * 2 + 1) - config.getSpawnerRadius();
                        int oy = random.nextInt(5) - 2;
                        int spawnX = sx + ox;
                        int spawnY = Math.max(2, Math.min(CHUNK_HEIGHT - 3, y + oy));
                        if (!canSpawnSpawnerZombieAt(world, spawnX, spawnY)) {
                            continue;
                        }
                        world.entities.add(new Zombie(spawnX + 0.5D, spawnY, true));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Validates light and block rules for a spawner zombie spawn.
     */
    private boolean canSpawnSpawnerZombieAt(World world, int x, int y) {
        BlockState feet = world.getBlock(x, y);
        BlockState head = world.getBlock(x, y + 1);
        BlockState below = world.getBlock(x, y - 1);
        if (!feet.getBlock().isAir() || !head.getBlock().isAir()) {
            return false;
        }
        if (below.getBlock().isAir() || below.getBlock().equals(Blocks.WATER) || below.getBlock().equals(Blocks.FLOWING_WATER)) {
            return false;
        }

        int chunkX = World.getChunkX(x);
        Chunk chunk = world.getChunkIfExists(chunkX);
        if (chunk == null) {
            return false;
        }
        int localX = World.getLocalX(x);
        int sky = chunk.getSkyLightAt(localX, y) & 0xFF;
        int block = chunk.getBlockLightAt(localX, y) & 0xFF;
        int effectiveSky = SpawnLightUtil.effectiveSkyLight(sky, Minecraft.getInstance().getTotalTicks());
        return effectiveSky == 0 && block == 0;
    }

    /**
     * Counts naturally spawned hostile mobs near the player.
     */
    private int countNaturalHostilesNearPlayer(World world, Player player) {
        int playerChunkX = World.getChunkX(player.x);
        int count = 0;
        for (Entity entity : world.entities) {
            if (!(entity instanceof Mob mob) || !mob.isHostile() || mob.removed) {
                continue;
            }
            if (entity instanceof Zombie zombie && zombie.isSpawnerSpawned()) {
                continue;
            }
            int chunkX = World.getChunkX(entity.x);
            if (Math.abs(chunkX - playerChunkX) <= config.getLocalRangeChunks()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts zombies within a radius, optionally filtering to spawner zombies.
     */
    private int countZombiesNear(World world, int x, int y, int radius, boolean onlySpawnerZombies) {
        int count = 0;
        for (Entity entity : world.entities) {
            if (!(entity instanceof Zombie zombie) || entity.removed) {
                continue;
            }
            if (onlySpawnerZombies && !zombie.isSpawnerSpawned()) {
                continue;
            }
            double dx = entity.x - x;
            double dy = entity.y - y;
            if ((dx * dx + dy * dy) <= radius * radius) {
                count++;
            }
        }
        return count;
    }

    /**
     * Marks distant hostile mobs for removal.
     */
    private void despawnFarHostiles(World world, Player player) {
        List<Entity> toRemove = new ArrayList<>();
        for (Entity entity : world.entities) {
            if (!(entity instanceof Mob mob) || !mob.isHostile()) {
                continue;
            }
            double dx = mob.x - player.x;
            double dy = mob.y - player.y;
            if ((dx * dx + dy * dy) > 32 * 32) {
                mob.removed = true;
                toRemove.add(mob);
            }
        }
        if (!toRemove.isEmpty()) {
            world.entities.removeAll(toRemove);
        }
    }

    /**
     * Chooses a spawn group by weighted random selection.
     */
    private SpawnGroup pickWeightedGroup() {
        int totalWeight = 0;
        for (SpawnGroup group : groups) {
            totalWeight += group.getWeight();
        }
        if (totalWeight <= 0) {
            return null;
        }
        int target = random.nextInt(totalWeight);
        int running = 0;
        for (SpawnGroup group : groups) {
            running += group.getWeight();
            if (target < running) {
                return group;
            }
        }
        return groups.get(groups.size() - 1);
    }

    /**
     * Finds a two-block-tall air column above solid ground.
     */
    private int findSurfaceSpawnY(World world, int x) {
        for (int y = CHUNK_HEIGHT - 3; y >= 2; y--) {
            if (world.getBlock(x, y - 1).getBlock().isAir()) {
                continue;
            }
            if (world.getBlock(x, y).getBlock().isAir() && world.getBlock(x, y + 1).getBlock().isAir()) {
                return y;
            }
        }
        return -1;
    }
}
