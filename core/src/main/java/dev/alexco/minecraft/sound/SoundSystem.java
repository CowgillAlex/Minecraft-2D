package dev.alexco.minecraft.sound;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.registry.ResourceLocation;

public class SoundSystem {
    public static void playSound(String soundId) {
        playSound(soundId, 1.0f);
    }

    /**
     * Resolves and plays a one-shot sound effect at the given volume.
     */
    public static void playSound(String soundId, float volume) {
        Sound sound = Registry.SOUND.get(new ResourceLocation(soundId));
        if (sound == null) {
            System.out.println("Sound not found: " + soundId);
            return;
        }
        if (!sound.isLoaded()) {
            sound.loadAsSound();
        }
        if (sound.getSoundEffect() != null) {
            sound.getSoundEffect().play(volume);
        }
    }

    public static void playMusic(String soundId) {
        playMusic(soundId, true);
    }

    /**
     * Resolves and starts a music track, optionally looping it.
     */
    public static void playMusic(String soundId, boolean loop) {
        Sound sound = Registry.SOUND.get(new ResourceLocation(soundId));
        if (sound == null) {
            System.out.println("Sound not found: " + soundId);
            return;
        }
        if (!sound.isLoaded()) {
            sound.loadAsMusic();
        }
        if (sound.getMusic() != null) {
            sound.getMusic().setLooping(loop);
            sound.getMusic().play();
        }
    }

    /**
     * Stops playback for one named music resource if it is active.
     */
    public static void stopMusic(String soundId) {
        Sound sound = Registry.SOUND.get(new ResourceLocation(soundId));
        if (sound != null && sound.getMusic() != null) {
            sound.getMusic().stop();
        }
    }

    /**
     * Stops every currently loaded music track in the sound registry.
     */
    public static void stopAllMusic() {
        for (ResourceLocation key : Registry.SOUND.keySet()) {
            Sound sound = Registry.SOUND.get(key);
            if (sound != null && sound.getMusic() != null) {
                sound.getMusic().stop();
            }
        }
    }

    /**
     * Disposes all registered sound resources.
     */
    public static void dispose() {
        for (ResourceLocation key : Registry.SOUND.keySet()) {
            Sound sound = Registry.SOUND.get(key);
            if (sound != null) {
                sound.dispose();
            }
        }
    }
}
