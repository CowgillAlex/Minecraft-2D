package dev.alexco.minecraft.world.serialisation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import dev.alexco.minecraft.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldSaveManager {
    public static final String GAME_FOLDER = ".paper-minecraft";
    public static final String SAVES_FOLDER = "saves";
    public static final String LEVEL_FILE = "level.dat";
    public static final String PLAYER_FILE = "player.dat";
    public static final String CHUNK_PREFIX = "chunk_";
    public static final String CHUNK_SUFFIX = ".dat";

    /**
     * Resolves the root game data folder on the current platform.
     */
    private static FileHandle getGameFolder() {
        String appdata = System.getenv("APPDATA");
        if (appdata != null) {
            return Gdx.files.absolute(appdata + "/" + GAME_FOLDER);
        }
        String userHome = System.getProperty("user.home");
        return Gdx.files.absolute(userHome + "/" + GAME_FOLDER);
    }

    public static FileHandle getSavesFolder() {
        return getGameFolder().child(SAVES_FOLDER);
    }

    public static FileHandle getWorldFolder(String folderName) {
        return getSavesFolder().child(folderName);
    }

    public static FileHandle getLevelFile(String folderName) {
        return getWorldFolder(folderName).child(LEVEL_FILE);
    }

    public static FileHandle getPlayerFile(String folderName) {
        return getWorldFolder(folderName).child(PLAYER_FILE);
    }

    public static FileHandle getChunkFile(String folderName, int chunkX) {
        return getWorldFolder(folderName).child(CHUNK_PREFIX + chunkX + CHUNK_SUFFIX);
    }

    public static class WorldInfo {
        public String folderName;
        public String displayName;
        public long lastPlayed;
        public long seed;
        public long ticks;
        public String lastVersion;
        public long fileSize;

        public WorldInfo(String folderName, String displayName, long lastPlayed, long seed, long ticks, String lastVersion, long fileSize) {
            this.folderName = folderName;
            this.displayName = displayName;
            this.lastPlayed = lastPlayed;
            this.seed = seed;
            this.ticks = ticks;
            this.lastVersion = lastVersion;
            this.fileSize = fileSize;
        }
    }

    /**
     * Converts a display name into a safe unique world folder name.
     */
    public static String sanitizeFolderName(String displayName) {
        String sanitized = displayName.toLowerCase();
        sanitized = sanitized.replaceAll("[^a-z0-9_]", "_");
        sanitized = sanitized.replaceAll("_{2,}", "_");
        sanitized = sanitized.replaceAll("^_|_$", "");
        if (sanitized.isEmpty()) {
            sanitized = "world";
        }
        String randomSuffix = "_" + Integer.toHexString(new Random().nextInt(0xFFFFFF));
        return sanitized + randomSuffix;
    }

    public static boolean worldFolderExists(String folderName) {
        return getWorldFolder(folderName).exists();
    }

    /**
     * Scans save folders and returns world metadata sorted by last played.
     */
    public static List<WorldInfo> getWorlds() {
        List<WorldInfo> worlds = new ArrayList<>();
        FileHandle savesFolder = getSavesFolder();

        if (!savesFolder.exists()) {
            return worlds;
        }

        for (FileHandle folder : savesFolder.list()) {
            if (!folder.isDirectory()) continue;
            try {
                FileHandle levelFile = getLevelFile(folder.name());
                if (levelFile.exists()) {
                    WorldInfo info = WorldFile.readWorldInfo(levelFile, folder.name());
                    if (info != null) {
                        worlds.add(info);
                    }
                }
            } catch (Exception e) {
                Logger.ERROR("Failed to read world info for %s: %s", folder.name(), e.getMessage());
            }
        }

        worlds.sort((a, b) -> Long.compare(b.lastPlayed, a.lastPlayed));
        return worlds;
    }

    /**
     * Checks if at least one valid world save exists.
     */
    public static boolean hasWorlds() {
        FileHandle savesFolder = getSavesFolder();
        if (!savesFolder.exists()) {
            return false;
        }
        for (FileHandle folder : savesFolder.list()) {
            if (folder.isDirectory() && getLevelFile(folder.name()).exists()) {
                return true;
            }
        }
        return false;
    }

    public static void createWorldFolder(String folderName) {
        FileHandle worldFolder = getWorldFolder(folderName);
        worldFolder.mkdirs();
    }

    public static boolean deleteWorld(String folderName) {
        FileHandle worldFolder = getWorldFolder(folderName);
        if (worldFolder.exists()) {
            worldFolder.deleteDirectory();
            return true;
        }
        return false;
    }

    /**
     * Calculates total on-disk size of a world folder.
     */
    public static long getWorldFolderSize(String folderName) {
        FileHandle worldFolder = getWorldFolder(folderName);
        if (!worldFolder.exists()) {
            return 0;
        }
        return calculateFolderSize(worldFolder);
    }

    /**
     * Recursively sums file sizes under a folder.
     */
    private static long calculateFolderSize(FileHandle folder) {
        long size = 0;
        if (folder.isDirectory()) {
            for (FileHandle child : folder.list()) {
                size += calculateFolderSize(child);
            }
        } else {
            size += folder.length();
        }
        return size;
    }
}
