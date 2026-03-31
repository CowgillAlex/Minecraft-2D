package dev.alexco.minecraft.sound;

import dev.alexco.registry.ResourceLocation;

/**
 * Placeholder class for sound registration.
 * Sounds are dynamically loaded from Minecraft's asset index during initialisation.
 */
public class Sounds {
    // This class will be populated with sound constants after dynamic loading
    public static final Sound EMPTY = new Sound(new ResourceLocation("minecraft", "empty"), "minecraft/sounds/empty.ogg");
}
