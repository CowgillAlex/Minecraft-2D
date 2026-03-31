package dev.alexco.minecraft.world.entity.spawn;

import static dev.alexco.minecraft.SharedConstants.CHUNK_HEIGHT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.tag.Tag;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Cow;
import dev.alexco.minecraft.world.entity.Mob;
import dev.alexco.minecraft.world.entity.Pig;
import dev.alexco.minecraft.world.entity.Zombie;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.registry.ResourceLocation;

public final class SpawnRulesManager {
    public static final class SpawnConfig {
        private final int localRangeChunks;
        private final int spawnCap;
        private final int spawnIntervalTicks;
        private final int attemptsPerCycle;
        private final float minPlayerDistance;
        private final boolean requireSurface;
        private final int spawnerIntervalTicks;
        private final int spawnerRadius;
        private final int spawnerCap;
        private final List<SpawnGroup> groups;

        private SpawnConfig(
            int localRangeChunks,
            int spawnCap,
            int spawnIntervalTicks,
            int attemptsPerCycle,
            float minPlayerDistance,
            boolean requireSurface,
            int spawnerIntervalTicks,
            int spawnerRadius,
            int spawnerCap,
            List<SpawnGroup> groups
        ) {
            this.localRangeChunks = Math.max(1, localRangeChunks);
            this.spawnCap = Math.max(0, spawnCap);
            this.spawnIntervalTicks = Math.max(1, spawnIntervalTicks);
            this.attemptsPerCycle = Math.max(1, attemptsPerCycle);
            this.minPlayerDistance = Math.max(0.0f, minPlayerDistance);
            this.requireSurface = requireSurface;
            this.spawnerIntervalTicks = Math.max(1, spawnerIntervalTicks);
            this.spawnerRadius = Math.max(1, spawnerRadius);
            this.spawnerCap = Math.max(1, spawnerCap);
            this.groups = Collections.unmodifiableList(groups);
        }

        public int getLocalRangeChunks() {
            return localRangeChunks;
        }

        public int getSpawnCap() {
            return spawnCap;
        }

        public int getSpawnIntervalTicks() {
            return spawnIntervalTicks;
        }

        public int getAttemptsPerCycle() {
            return attemptsPerCycle;
        }

        public float getMinPlayerDistance() {
            return minPlayerDistance;
        }

        public boolean isRequireSurface() {
            return requireSurface;
        }

        public int getSpawnerIntervalTicks() {
            return spawnerIntervalTicks;
        }

        public int getSpawnerRadius() {
            return spawnerRadius;
        }

        public int getSpawnerCap() {
            return spawnerCap;
        }

        public List<SpawnGroup> getGroups() {
            return groups;
        }
    }

    private static volatile boolean loaded;
    private static SpawnConfig passiveConfig = defaultPassiveConfig();
    private static SpawnConfig hostileConfig = defaultHostileConfig();

    private SpawnRulesManager() {
    }

    public static SpawnConfig getPassiveConfig() {
        ensureLoaded();
        return passiveConfig;
    }

    public static SpawnConfig getHostileConfig() {
        ensureLoaded();
        return hostileConfig;
    }

    /**
     * Lazily loads passive and hostile spawn configs from JSON.
     */
    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (SpawnRulesManager.class) {
            if (loaded) {
                return;
            }
            passiveConfig = loadConfig("spawn_rules/passive.json", defaultPassiveConfig(), false);
            hostileConfig = loadConfig("spawn_rules/hostile.json", defaultHostileConfig(), true);
            loaded = true;
            Logger.INFO("Loaded spawn rules: passive=%d group(s), hostile=%d group(s)",
                passiveConfig.getGroups().size(), hostileConfig.getGroups().size());
        }
    }

    /**
     * Loads one spawn config file and falls back on parse failure.
     */
    private static SpawnConfig loadConfig(String path, SpawnConfig fallback, boolean hostile) {
        try {
            FileHandle file = Gdx.files.internal(path);
            if (!file.exists()) {
                Logger.ERROR("Spawn rules file missing: %s (using defaults)", path);
                return fallback;
            }
            JsonObject root = JsonParser.parseString(file.readString()).getAsJsonObject();
            JsonObject overall = root.has("overall") && root.get("overall").isJsonObject()
                ? root.getAsJsonObject("overall")
                : root;

            int localRangeChunks = intOr(overall, "local_range_chunks", fallback.getLocalRangeChunks());
            int spawnCap = intOr(overall, "spawn_cap", fallback.getSpawnCap());
            int spawnIntervalTicks = intOr(overall, "spawn_interval_ticks", fallback.getSpawnIntervalTicks());
            int attemptsPerCycle = intOr(overall, "attempts_per_cycle", fallback.getAttemptsPerCycle());
            float minPlayerDistance = floatOr(overall, "min_player_distance", fallback.getMinPlayerDistance());
            boolean requireSurface = boolOr(overall, "require_surface", fallback.isRequireSurface());
            int spawnerIntervalTicks = intOr(overall, "spawner_interval_ticks", fallback.getSpawnerIntervalTicks());
            int spawnerRadius = intOr(overall, "spawner_radius", fallback.getSpawnerRadius());
            int spawnerCap = intOr(overall, "spawner_cap", fallback.getSpawnerCap());

            List<SpawnGroup> groups = parseGroups(root, hostile, fallback.getGroups());
            return new SpawnConfig(localRangeChunks, spawnCap, spawnIntervalTicks, attemptsPerCycle,
                minPlayerDistance, requireSurface, spawnerIntervalTicks, spawnerRadius, spawnerCap, groups);
        } catch (Exception e) {
            Logger.ERROR("Failed loading spawn rules from %s: %s", path, e.getMessage());
            return fallback;
        }
    }

    /**
    * Parses group definitions using either mobs[] or groups[].
    */
    private static List<SpawnGroup> parseGroups(JsonObject root, boolean hostile, List<SpawnGroup> fallback) {
        if (root.has("mobs") && root.get("mobs").isJsonArray()) {
            List<SpawnGroup> mobs = parseGroupArray(root.getAsJsonArray("mobs"), hostile);
            return mobs.isEmpty() ? fallback : mobs;
        }
        if (!root.has("groups") || !root.get("groups").isJsonArray()) {
            return fallback;
        }
        List<SpawnGroup> groups = parseGroupArray(root.getAsJsonArray("groups"), hostile);
        return groups.isEmpty() ? fallback : groups;
    }

    /**
     * Parses a JSON array of spawn groups.
     */
    private static List<SpawnGroup> parseGroupArray(com.google.gson.JsonArray array, boolean hostile) {
        List<SpawnGroup> groups = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject obj = element.getAsJsonObject();
            SpawnGroup group = parseGroup(obj, hostile);
            if (group != null) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * Parses one spawn group entry and resolves its factory/condition.
     */
    private static SpawnGroup parseGroup(JsonObject obj, boolean hostile) {
        if (!boolOr(obj, "enabled", true)) {
            return null;
        }
        String entityRaw = stringOr(obj, "entity", null);
        String idRaw = stringOr(obj, "id", entityRaw == null ? null : entityRaw + "_spawn");
        if (idRaw == null || entityRaw == null) {
            return null;
        }

        SpawnGroup.MobFactory factory = factoryFor(entityRaw, hostile);
        if (factory == null) {
            Logger.ERROR("Unknown spawn entity %s in group %s", entityRaw, idRaw);
            return null;
        }

        int minCount = intOr(obj, "min_count", 1);
        int maxCount = intOr(obj, "max_count", minCount);
        int weight = intOr(obj, "weight", 1);

        SpawnCondition condition = new JsonSpawnCondition(obj.has("conditions") && obj.get("conditions").isJsonObject()
            ? obj.getAsJsonObject("conditions")
            : new JsonObject());

        return new SpawnGroup(new ResourceLocation(idRaw), minCount, maxCount, weight, condition, factory);
    }

    /**
     * Maps an entity id to a mob factory for spawning.
     */
    private static SpawnGroup.MobFactory factoryFor(String entityRaw, boolean hostile) {
        ResourceLocation id = new ResourceLocation(entityRaw);
        if (id.equals(Cow.TYPE)) {
            return Cow::new;
        }
        if (id.equals(Pig.TYPE)) {
            return Pig::new;
        }
        if (id.equals(Zombie.TYPE)) {
            if (hostile) {
                return (x, y) -> new Zombie(x, y, false);
            }
            return null;
        }
        return null;
    }

    private static SpawnConfig defaultPassiveConfig() {
        List<SpawnGroup> groups = new ArrayList<>();
        groups.add(new SpawnGroup(
            new ResourceLocation("minecraft", "cow_group"),
            1,
            2,
            10,
            new GrassSurfaceSpawnCondition(),
            Cow::new
        ));
        return new SpawnConfig(4, 6, 120, 2, 12.0f, true, 20, 8, 8, groups);
    }

    private static SpawnConfig defaultHostileConfig() {
        List<SpawnGroup> groups = new ArrayList<>();
        JsonObject conditions = new JsonObject();
        conditions.addProperty("require_air_space", true);
        conditions.addProperty("require_solid_below", true);
        conditions.addProperty("deny_fluids", true);
        conditions.addProperty("max_block_light", 0);
        conditions.addProperty("max_sky_light", 0);
        conditions.addProperty("use_sky_intensity", true);
        groups.add(new SpawnGroup(
            new ResourceLocation("minecraft", "zombie_group"),
            1,
            2,
            12,
            new JsonSpawnCondition(conditions),
            (x, y) -> new Zombie(x, y, false)
        ));
        return new SpawnConfig(5, 40, 3, 18, 10.0f, false, 20, 8, 8, groups);
    }

    private static int intOr(JsonObject obj, String key, int fallback) {
        return obj.has(key) ? obj.get(key).getAsInt() : fallback;
    }

    private static float floatOr(JsonObject obj, String key, float fallback) {
        return obj.has(key) ? obj.get(key).getAsFloat() : fallback;
    }

    private static boolean boolOr(JsonObject obj, String key, boolean fallback) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : fallback;
    }

    private static String stringOr(JsonObject obj, String key, String fallback) {
        return obj.has(key) ? obj.get(key).getAsString() : fallback;
    }

    private static final class JsonSpawnCondition implements SpawnCondition {
        private final boolean requireAirSpace;
        private final boolean requireSolidBelow;
        private final boolean denyFluids;
        private final ResourceLocation blockBelow;
        private final ResourceLocation blockBelowTag;
        private final int minSkyLight;
        private final int maxSkyLight;
        private final int minBlockLight;
        private final int maxBlockLight;
        private final boolean useSkyIntensity;

        private JsonSpawnCondition(JsonObject json) {
            this.requireAirSpace = boolOr(json, "require_air_space", true);
            this.requireSolidBelow = boolOr(json, "require_solid_below", true);
            this.denyFluids = boolOr(json, "deny_fluids", true);
            this.blockBelow = json.has("block_below") ? new ResourceLocation(json.get("block_below").getAsString()) : null;
            this.blockBelowTag = json.has("block_below_tag") ? new ResourceLocation(json.get("block_below_tag").getAsString()) : null;
            this.minSkyLight = Math.max(0, intOr(json, "min_sky_light", 0));
            this.maxSkyLight = Math.max(0, intOr(json, "max_sky_light", 15));
            this.minBlockLight = Math.max(0, intOr(json, "min_block_light", 0));
            this.maxBlockLight = Math.max(0, intOr(json, "max_block_light", 15));
            this.useSkyIntensity = boolOr(json, "use_sky_intensity", false);
        }

        /**
         * Evaluates block and light constraints for a candidate spawn point.
         */
        @Override
        public boolean canSpawn(World world, int x, int y) {
            if (y <= 1 || y >= CHUNK_HEIGHT - 2) {
                return false;
            }

            BlockState feet = world.getBlock(x, y);
            BlockState head = world.getBlock(x, y + 1);
            BlockState below = world.getBlock(x, y - 1);

            if (requireAirSpace && (!feet.getBlock().isAir() || !head.getBlock().isAir())) {
                return false;
            }
            if (requireSolidBelow && (below.getBlock().isAir() || BlockTags.FLUID.contains(below.getBlock()))) {
                return false;
            }
            if (denyFluids && (BlockTags.FLUID.contains(feet.getBlock()) || BlockTags.FLUID.contains(head.getBlock()))) {
                return false;
            }

            if (blockBelow != null) {
                Block block = Registry.BLOCK.get(blockBelow);
                if (block == null || !below.getBlock().equals(block)) {
                    return false;
                }
            }
            if (blockBelowTag != null) {
                Tag<Block> tag = BlockTags.getTag(blockBelowTag);
                if (tag == null || !tag.contains(below.getBlock())) {
                    return false;
                }
            }

            Chunk chunk = world.getChunkIfExists(World.getChunkX(x));
            if (chunk == null) {
                return false;
            }
            int localX = World.getLocalX(x);
            int sky = chunk.getSkyLightAt(localX, y) & 0xFF;
            if (useSkyIntensity) {
                sky = SpawnLightUtil.effectiveSkyLight(sky, Minecraft.getInstance().getTotalTicks());
            }
            int block = chunk.getBlockLightAt(localX, y) & 0xFF;
            return sky >= minSkyLight && sky <= maxSkyLight && block >= minBlockLight && block <= maxBlockLight;
        }
    }
}
