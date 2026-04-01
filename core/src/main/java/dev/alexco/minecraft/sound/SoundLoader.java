package dev.alexco.minecraft.sound;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.registry.ResourceLocation;

public class SoundLoader {
    private static final List<String> soundPaths = new ArrayList<>();
    private static final ResourceLocation FALLBACK_SOUND_ID = new ResourceLocation("minecraft", "empty");

    /**
     * Ensures the sound registry always has at least one entry so registry
     * validation does not fail when external assets are unavailable.
     */
    private static void ensureFallbackRegistered() {
        if (Registry.SOUND.get(FALLBACK_SOUND_ID) == null) {
            Registry.SOUND.register(Registry.SOUND, FALLBACK_SOUND_ID.toString(), Sounds.EMPTY);
            System.out.println("Registered fallback sound: " + FALLBACK_SOUND_ID);
        }
    }

    /**
     * Loads and registers all Minecraft ogg assets from the newest local asset index.
     */
    public static void loadSounds() {
        ensureFallbackRegistered();

        File indexesFolder = new File(System.getProperty("user.home") + "/AppData/roaming/.minecraft/assets/indexes");
        System.out.println("Looking for sound indexes in: " + indexesFolder.getAbsolutePath());
        File[] indexFiles = indexesFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (indexFiles != null) {
            for (File indexFile : indexFiles) {
                System.out.println("Found sound index: " + indexFile.getName());
            }
        } else {
            System.out.println("No sound indexes found!");
            return;
        }
        int largestIndex = -1;
        if (indexFiles != null) {
            for (File indexFile : indexFiles) {
                String name = indexFile.getName();
                try {
                    int index = Integer.parseInt(name.substring(0, name.length() - 5));
                    if (index > largestIndex) {
                        largestIndex = index;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid sound index file name: " + name);
                }
            }
        }
        System.out.println("Largest sound index: " + largestIndex);
        File indexFile = new File(indexesFolder, largestIndex + ".json");
        System.out.println("Loading sound index from: " + indexFile.getAbsolutePath());
        String indexFileContent = null;
        try {
            indexFileContent = new String(Files.readAllBytes(indexFile.toPath()));
        } catch (Exception e) {
            System.out.println("Failed to read sound index file: " + e.getMessage());
            return;
        }
        try {
            Gson gson = new Gson();
            JsonObject soundIndex = gson.fromJson(indexFileContent, JsonObject.class);
            JsonObject objects = soundIndex.getAsJsonObject("objects");

            if (objects == null) {
                System.out.println("No 'objects' field found in sound index!");
                return;
            }

            System.out.println("Loaded sound index with " + objects.size() + " entries");

            List<Sound> registeredSounds = new ArrayList<>();
            String assetsPath = System.getProperty("user.home") + "/AppData/roaming/.minecraft/assets";
            String minecraftSoundsPrefix = "minecraft/sounds/";

            for (String key : objects.keySet()) {
                if (key.startsWith(minecraftSoundsPrefix) && key.endsWith(".ogg")) {
                    JsonObject fileInfo = objects.getAsJsonObject(key);
                    String hash = fileInfo.get("hash").getAsString();

                    String soundName = key.substring(minecraftSoundsPrefix.length());
                    soundName = soundName.substring(0, soundName.length() - 4);
                    soundName = soundName.replace("/", ".");

                    String first2Chars = hash.substring(0, 2);
                    String objectPath = assetsPath + "/objects/" + first2Chars + "/" + hash;

                    ResourceLocation location = new ResourceLocation("minecraft", soundName);
                    Sound sound = new Sound(location, objectPath);
                    Registry.SOUND.register(Registry.SOUND, location.toString(), sound);

                    soundPaths.add(objectPath);
                    registeredSounds.add(sound);
               //     System.out.println("Registered sound: " + location);
                }
            }

            System.out.println("\nRegistered " + registeredSounds.size() + " sound events");
        } catch (JsonSyntaxException e) {
            System.out.println("Failed to parse sound index: " + e.getMessage());
        }
    }

    /**
     * Creates a LibGDX sound effect handle for an ogg file on disk.
     */
    public static com.badlogic.gdx.audio.Sound loadGdxSound(String filePath) {
        return Gdx.audio.newSound(new OggFileHandle(Gdx.files.absolute(filePath)));
    }

    /**
     * Creates a LibGDX streamed music handle for an ogg file on disk.
     */
    public static Music loadMusic(String filePath) {
        return Gdx.audio.newMusic(new OggFileHandle(Gdx.files.absolute(filePath)));
    }

    private static class OggFileHandle extends FileHandle {
        private final FileHandle wrapped;

        OggFileHandle(FileHandle handle) {
            this.wrapped = handle;
        }

        @Override
        public String path() {
            return wrapped.path();
        }

        @Override
        public String name() {
            return wrapped.name() + ".ogg";
        }

        @Override
        public String extension() {
            return "ogg";
        }

        @Override
        public InputStream read() {
            return wrapped.read();
        }

        @Override
        public long length() {
            return wrapped.length();
        }

        @Override
        public boolean exists() {
            return wrapped.exists();
        }
    }
}
