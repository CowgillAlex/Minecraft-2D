package dev.alexco.minecraft.world.entity.spawn;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;
import static dev.alexco.minecraft.SharedConstants.CHUNK_WIDTH;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.entity.Mob;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;

public class PassiveMobSpawner {
    private final Random random = new Random();
    private final SpawnRulesManager.SpawnConfig config;
    private final List<SpawnGroup> groups;
    private int tickCounter = 0;

    public PassiveMobSpawner() {
        this.config = SpawnRulesManager.getPassiveConfig();
        this.groups = new ArrayList<>(config.getGroups());
    }

    /**
     * Runs passive mob spawn attempts around the player.
     */
    public void tick(World world, Player player) {
        if (player == null || !world.worldData.passiveMobSpawningEnabled || groups.isEmpty()) {
            return;
        }
        int spawnIntervalTicks = Math.max(1, world.worldData.passiveSpawnIntervalTicks);
        int passiveMobCap = Math.max(0, world.worldData.passiveMobCap);
        tickCounter++;
        if (tickCounter < spawnIntervalTicks) {
            return;
        }
        tickCounter = 0;

        int existing = countPassiveMobsNearPlayer(world, player);
        if (existing >= passiveMobCap) {
            return;
        }

        int attempts = Math.max(1, config.getAttemptsPerCycle() - (existing > 1 ? 1 : 0));
        for (int attempt = 0; attempt < attempts; attempt++) {
            SpawnGroup group = pickWeightedGroup();
            if (group == null) {
                return;
            }
            int playerChunkX = World.getChunkX(player.x);
            int localRange = config.getLocalRangeChunks();
            int spawnChunkX = playerChunkX + random.nextInt(localRange * 2 + 1) - localRange;
            Chunk chunk = world.getChunkIfExists(spawnChunkX);
            if (chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
                continue;
            }

            int baseX = spawnChunkX * CHUNK_WIDTH + random.nextInt(CHUNK_WIDTH);
            int baseY = config.isRequireSurface() ? findSurfaceSpawnY(world, baseX) : (2 + random.nextInt(CHUNK_HEIGHT - 4));
            if (baseY < 0 || !group.getCondition().canSpawn(world, baseX, baseY)) {
                continue;
            }

            int toSpawn = group.getMinCount() + random.nextInt(group.getMaxCount() - group.getMinCount() + 1);
            for (int i = 0; i < toSpawn; i++) {
                if (countPassiveMobsNearPlayer(world, player) >= passiveMobCap) {
                    return;
                }
                int offsetX = random.nextInt(9) - 4;
                int x = baseX + offsetX;
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
    }

    /**
     * Chooses a passive spawn group by weighted random selection.
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
     * Counts passive mobs near the player's loaded area.
     */
    private int countPassiveMobsNearPlayer(World world, Player player) {
        int playerChunkX = World.getChunkX(player.x);
        int count = 0;
        for (Entity entity : world.entities) {
            if (!(entity instanceof Mob mob) || mob.isHostile() || entity.removed) {
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
