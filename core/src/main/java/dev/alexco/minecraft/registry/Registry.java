package dev.alexco.minecraft.registry;

import dev.alexco.minecraft.sound.Sound;
import dev.alexco.minecraft.sound.Sounds;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.biome.Biomes;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.Items;
import dev.alexco.minecraft.world.level.levelgen.noise.Noise;
import dev.alexco.minecraft.world.level.levelgen.noise.Noises;
public abstract class Registry {
   public static final  dev.alexco.registry.Registry<Block> BLOCK = dev.alexco.registry.Registry.registerDefaulted("block", "air", ()->Blocks.AIR);
   public static final  dev.alexco.registry.Registry<Item> ITEM = dev.alexco.registry.Registry.registerDefaulted("item", "air", ()->Items.AIR);
   public static final  dev.alexco.registry.Registry<Noise> NOISE = dev.alexco.registry.Registry.registerDefaulted("noise", "continentalness", ()->Noises.CONTINENTALNESS);
   public static final dev.alexco.registry.Registry<Biome> BIOME = dev.alexco.registry.Registry.registerDefaulted("biome", "plains", ()->Biomes.PLAINS);
   public static final dev.alexco.registry.Registry<Sound> SOUND = dev.alexco.registry.Registry.registerSimple("sound", ()->Sounds.EMPTY);
}
