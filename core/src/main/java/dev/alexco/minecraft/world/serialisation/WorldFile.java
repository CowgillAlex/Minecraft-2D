package dev.alexco.minecraft.world.serialisation;

import com.badlogic.gdx.files.FileHandle;
import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.Version;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.spawn.SpawnRulesManager;
import dev.alexco.minecraft.world.serialisation.WorldSaveManager.WorldInfo;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.IOException;

public class WorldFile {
    /**
     * Writes world metadata to the level file.
     */
    public static void writeWorldToDisk(World world, String worldFolderName, String displayName) {
        CompoundTag worldTag = serialiseWorld(world, displayName);
        FileHandle levelFile = WorldSaveManager.getLevelFile(worldFolderName);

        try {
            levelFile.parent().mkdirs();
            NBTUtil.write(new NamedTag(null, worldTag), levelFile.file());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save world data: " + e.getMessage(), e);
        }
    }

    /**
     * Serialises world settings and runtime counters into NBT.
     */
    public static CompoundTag serialiseWorld(World world, String displayName) {
        CompoundTag tag = new CompoundTag();

        tag.putString("DataVersion", Version.DATA_VERSION);
        tag.putString("LastVersion", Version.VERSION_STRING);
        tag.putString("LevelName", displayName);
        tag.putLong("Seed", world.seed);
        tag.putLong("LastPlayed", System.currentTimeMillis());
        tag.putLong("GameTime", Minecraft.getInstance().getSession().getTimer().totalTicks);
        tag.putInt("RandomTickSpeed", world.worldData.randomTickSpeed);
        tag.putBoolean("PassiveMobSpawningEnabled", world.worldData.passiveMobSpawningEnabled);
        tag.putBoolean("HostileMobSpawningEnabled", world.worldData.hostileMobSpawningEnabled);
        tag.putInt("PassiveSpawnIntervalTicks", world.worldData.passiveSpawnIntervalTicks);
        tag.putInt("HostileSpawnIntervalTicks", world.worldData.hostileSpawnIntervalTicks);
        tag.putInt("PassiveMobCap", world.worldData.passiveMobCap);
        tag.putInt("HostileMobCap", world.worldData.hostileMobCap);

        return tag;
    }

    /**
     * Reads summary world info used by the world-selection screen.
     */
    public static WorldInfo readWorldInfo(FileHandle levelFile, String folderName) {
        if (!levelFile.exists()) {
            return null;
        }

        try {
            NamedTag namedTag = NBTUtil.read(levelFile.file());
            CompoundTag tag = (CompoundTag) namedTag.getTag();

            String displayName = tag.getString("LevelName");
            if (displayName == null || displayName.isEmpty()) {
                displayName = folderName;
            }

            long lastPlayed = tag.getLong("LastPlayed");
            long seed = tag.getLong("Seed");
            long ticks = tag.getLong("GameTime");
            String lastVersion = tag.getString("LastVersion");
            if (lastVersion == null || lastVersion.isEmpty()) {
                lastVersion = "unknown";
            }
            long fileSize = WorldSaveManager.getWorldFolderSize(folderName);

            return new WorldInfo(folderName, displayName, lastPlayed, seed, ticks, lastVersion, fileSize);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Reads the raw world level tag from disk.
     */
    public static CompoundTag readWorldData(String worldFolderName) {
        FileHandle levelFile = WorldSaveManager.getLevelFile(worldFolderName);

        if (!levelFile.exists()) {
            return null;
        }

        try {
            NamedTag namedTag = NBTUtil.read(levelFile.file());
            return (CompoundTag) namedTag.getTag();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load world data: " + e.getMessage(), e);
        }
    }

    /**
     * Applies saved world settings to an in-memory world instance.
     */
    public static void deserialiseWorld(World world, CompoundTag tag) {
        world.seed = tag.getLong("Seed");
        world.gameTime = tag.getLong("GameTime");
        SpawnRulesManager.SpawnConfig passiveDefaults = SpawnRulesManager.getPassiveConfig();
        SpawnRulesManager.SpawnConfig hostileDefaults = SpawnRulesManager.getHostileConfig();
        world.worldData.randomTickSpeed = tag.containsKey("RandomTickSpeed")
            ? Math.max(0, tag.getInt("RandomTickSpeed"))
            : 3;
        world.worldData.passiveMobSpawningEnabled = !tag.containsKey("PassiveMobSpawningEnabled") || tag.getBoolean("PassiveMobSpawningEnabled");
        world.worldData.hostileMobSpawningEnabled = !tag.containsKey("HostileMobSpawningEnabled") || tag.getBoolean("HostileMobSpawningEnabled");
        world.worldData.passiveSpawnIntervalTicks = tag.containsKey("PassiveSpawnIntervalTicks")
            ? Math.max(1, tag.getInt("PassiveSpawnIntervalTicks"))
            : passiveDefaults.getSpawnIntervalTicks();
        world.worldData.hostileSpawnIntervalTicks = tag.containsKey("HostileSpawnIntervalTicks")
            ? Math.max(1, tag.getInt("HostileSpawnIntervalTicks"))
            : hostileDefaults.getSpawnIntervalTicks();
        world.worldData.passiveMobCap = tag.containsKey("PassiveMobCap")
            ? Math.max(0, tag.getInt("PassiveMobCap"))
            : passiveDefaults.getSpawnCap();
        world.worldData.hostileMobCap = tag.containsKey("HostileMobCap")
            ? Math.max(0, tag.getInt("HostileMobCap"))
            : hostileDefaults.getSpawnCap();
    }
}
