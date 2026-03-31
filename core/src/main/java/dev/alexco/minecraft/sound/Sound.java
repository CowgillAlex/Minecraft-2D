package dev.alexco.minecraft.sound;

import com.badlogic.gdx.audio.Music;
import dev.alexco.registry.ResourceLocation;

public class Sound {
    public final ResourceLocation location;
    public final String filePath;
    private com.badlogic.gdx.audio.Sound soundEffect;
    private Music music;
    private boolean loaded = false;

    public Sound(ResourceLocation location, String filePath) {
        this.location = location;
        this.filePath = filePath;
    }

    /**
     * Lazily loads this asset as a short sound effect.
     */
    public void loadAsSound() {
        if (loaded) return;
        try {
            soundEffect = SoundLoader.loadGdxSound(filePath);
            loaded = true;
        } catch (Exception e) {
            System.out.println("Failed to load sound: " + location + " - " + e.getMessage());
        }
    }

    /**
     * Lazily loads this asset as streamed music.
     */
    public void loadAsMusic() {
        if (loaded) return;
        try {
            music = SoundLoader.loadMusic(filePath);
            loaded = true;
        } catch (Exception e) {
            System.out.println("Failed to load music: " + location + " - " + e.getMessage());
        }
    }

    public com.badlogic.gdx.audio.Sound getSoundEffect() {
        return soundEffect;
    }

    public Music getMusic() {
        return music;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Releases any loaded sound or music handles for this resource.
     */
    public void dispose() {
        if (soundEffect != null) {
            soundEffect.dispose();
            soundEffect = null;
        }
        if (music != null) {
            music.dispose();
            music = null;
        }
        loaded = false;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return location.toString();
    }
}
