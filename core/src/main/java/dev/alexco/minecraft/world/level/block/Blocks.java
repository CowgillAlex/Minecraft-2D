package dev.alexco.minecraft.world.level.block;

import java.util.Set;

import com.badlogic.gdx.math.Vector2;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.material.Material;
import dev.alexco.minecraft.world.level.material.MaterialColor;

public class Blocks {
    public static final Block AIR = register("air", new AirBlock(Block.Properties.of(Material.AIR).noCollission().solidity(Solidity.AIR).filtersBy(0)), new Vector2(0, 0));
    public static final Block STONE = Blocks.register("stone", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5f, 6.0f)), new Vector2(16, 0));
    public static final Block DIRT = Blocks.register("dirt", new Block(Block.Properties.of(Material.DIRT, MaterialColor.DIRT).strength(0.5f)), new Vector2(32, 0));
    public static final Block GRASS_BLOCK = Blocks.register("grass_block", new Block(Block.Properties.of(Material.GRASS, MaterialColor.GRASS).strength(0.5f).backgroundBlockState(DIRT.defaultBlockState())), new Vector2(48, 0));
    public static final Block TEST = Blocks.register("minecraft:test_block", new TestBlock(Block.Properties.of(Material.DIRT, MaterialColor.GRASS).strength(0.5f).dontChangeBgStateOnBreak()), new Vector2(64, 0));
    public static final Block BEDROCK = Blocks.register("minecraft:bedrock", new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(0f)), new Vector2(80, 0));
    public static final Block DEEPSLATE = Blocks.register("minecraft:deepslate", new DeepslateBlock(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(3, 6)), new Vector2(96, 0));
    public static final Block WATER = Blocks.register("minecraft:water_still", new WaterBlock(Block.Properties.of(Material.WATER, MaterialColor.WATER).strength(0f, 1016).backgroundBlockState(AIR.defaultBlockState()).solidity(Solidity.WATER).filtersBy(1)), new Vector2(112, 0));
    public static final Block FLOWING_WATER = Blocks.register("minecraft:water_flow", new WaterBlock(Block.Properties.of(Material.WATER, MaterialColor.WATER).strength(0f, 1016).solidity(Solidity.WATER).filtersBy(1)), new Vector2(128, 0));
    public static final Block OAK_LOG = Blocks.register("minecraft:oak_log", new OakLogBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2).solidity(Solidity.AIR).noCollission().backgroundBlockState(AIR.defaultBlockState()).filtersBy(1)), new Vector2(144, 0));
    public static final Block OAK_LEAVES = Blocks.register("minecraft:oak_leaves", new OakLeafBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2).solidity(Solidity.AIR).noCollission().backgroundBlockState(AIR.defaultBlockState()).filtersBy(1)), new Vector2(160, 0));
    public static final Block COAL_ORE = Blocks.register("minecraft:coal_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(3f, 3f).backgroundBlockState(STONE.defaultBackgroundBlockState())), new Vector2(176, 0));
    public static final Block DEEPSLATE_COAL = Blocks.register("minecraft:deepslate_coal_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(4.5f, 3f).backgroundBlockState(DEEPSLATE.defaultBackgroundBlockState())), new Vector2(192, 0));
    public static final Block IRON_ORE = Blocks.register("minecraft:iron_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(4.5f, 3f).backgroundBlockState(STONE.defaultBackgroundBlockState())), new Vector2(208, 0));
    public static final Block DEEPSLATE_IRON = Blocks.register("minecraft:deepslate_iron_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(4.5f, 3f).backgroundBlockState(DEEPSLATE.defaultBackgroundBlockState())), new Vector2(224, 0));
    public static final Block GOLD_ORE = Blocks.register("minecraft:gold_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(4.5f, 3f).backgroundBlockState(STONE.defaultBackgroundBlockState())), new Vector2(240, 0));
    public static final Block DEEPSLATE_GOLD = Blocks.register("minecraft:deepslate_gold_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(4.5f, 3f).backgroundBlockState(DEEPSLATE.defaultBackgroundBlockState())), new Vector2(0, 16));
    public static final Block DIAMOND_ORE = Blocks.register("minecraft:diamond_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(4.5f, 3f).backgroundBlockState(STONE.defaultBackgroundBlockState())), new Vector2(16, 16));
    public static final Block DEEPSLATE_DIAMOND = Blocks.register("minecraft:deepslate_diamond_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(4.5f, 3f).backgroundBlockState(DEEPSLATE.defaultBackgroundBlockState())), new Vector2(32, 16));
    public static final Block REDSTONE_ORE = Blocks.register("minecraft:redstone_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(4.5f, 3f).backgroundBlockState(STONE.defaultBackgroundBlockState())), new Vector2(48, 16));
    public static final Block DEEPSLATE_REDSTONE_ORE = Blocks.register("minecraft:deepslate_redstone_ore", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(4.5f, 3f).backgroundBlockState(DEEPSLATE.defaultBackgroundBlockState())), new Vector2(80, 16));
    public static final Block COBBLESTONE = Blocks.register("minecraft:cobblestone", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2, 6f)), new Vector2(96, 16));
    public static final Block MOSSY_COBBLESTONE = Blocks.register("minecraft:mossy_cobblestone", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2, 6f)), new Vector2(112, 16));
    public static final Block STONE_BRICKS = Blocks.register("minecraft:stone_bricks", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2, 6f)), new Vector2(128, 16));
    public static final Block MOSSY_STONE_BRICKS = Blocks.register("minecraft:mossy_stone_bricks", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2, 6f)), new Vector2(144, 16));
    public static final Block TORCH = Blocks.register("minecraft:torch", new Block(Block.Properties.of(Material.DECORATION, MaterialColor.COLOR_YELLOW).strength(0f).lightEmission(14).dontChangeBgStateOnBreak().solidity(Solidity.AIR).noCollission().filtersBy(1)), new Vector2(160, 16));
    public static final Block SPAWNER = Blocks.register("minecraft:spawner", new Block(Block.Properties.of(Material.HEAVY_METAL, MaterialColor.COLOR_GRAY).strength(4.5f).dontChangeBgStateOnBreak()), new Vector2(176, 16));
    public static final Block OAK_PLANKS = Blocks.register("minecraft:oak_planks", new Block(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2f, 3f)), new Vector2(208, 16));
    public static final Block OAK_SLAB = Blocks.register("minecraft:oak_slab", new OakSlabBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2f, 3f).backgroundBlockState(Blocks.AIR.defaultBlockState())), new Vector2(208, 16));
    public static final Block OAK_STAIRS = Blocks.register("minecraft:oak_stairs", new OakStairsBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2f, 3f).backgroundBlockState(Blocks.AIR.defaultBackgroundBlockState())), new Vector2(208, 16));
    public static final Block OAK_DOOR = Blocks.register("minecraft:oak_door", new DoorBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(3f).dontChangeBgStateOnBreak()), new Vector2(192, 16));
    public static final Block CRAFTING_TABLE = Blocks.register("minecraft:crafting_table", new Block(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.5f).dontChangeBgStateOnBreak().solidity(Solidity.AIR).noCollission()), new Vector2(0, 32));
    public static final Block FURNACE = Blocks.register("minecraft:furnace", new FurnaceBlock(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(3.5f).dontChangeBgStateOnBreak().solidity(Solidity.AIR).noCollission()), new Vector2(16, 32));
    public static final Block TALL_GRASS = Blocks.register("minecraft:tall_grass", new Block(Block.Properties.of(Material.PLANT, MaterialColor.PLANT).strength(0)), new Vector2(32, 32));
    public static final Block BAMBOO = Blocks.register("minecraft:bamboo", new Block(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(1f)), new Vector2(48, 32));
    public static final Block BARREL = Blocks.register("minecraft:barrel", new BarrelBlock(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.5f).solidity(Solidity.AIR).noCollission()), new Vector2(112, 32));
    public static final Block SANDSTONE = Blocks.register("minecraft:sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(128, 32));
    public static final Block BAMBOO_PLANKS = Blocks.register("minecraft:bamboo_planks", new Block(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2, 3)), new Vector2(144, 32));
    public static final Block BAMBOO_BLOCK = Blocks.register("minecraft:bamboo_block", new Block(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2f, 3f)), new Vector2(160, 32));
    public static final Block BAMBOO_MOSAIC = Blocks.register("minecraft:bamboo_mosaic", new Block(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2, 3)), new Vector2(176, 32));
    public static final Block BAMBOO_DOOR = Blocks.register("minecraft:bamboo_door", new Block(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(3)), new Vector2(208, 32));
    public static final Block GOLD_BLOCK = Blocks.register("minecraft:gold_block", new Block(Block.Properties.of(Material.METAL, MaterialColor.METAL).strength(3, 6)), new Vector2(224, 32));
    public static final Block NETHERITE_BLOCK = Blocks.register("minecraft:netherite_block", new Block(Block.Properties.of(Material.METAL, MaterialColor.METAL).strength(50, 1200)), new Vector2(240, 32));
    public static final Block ANCIENT_DEBRIS = Blocks.register("minecraft:ancient_debris", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(30, 1200)), new Vector2(0, 48));
    public static final Block WHEAT = Blocks.register("minecraft:wheat_block", new WheatBlock(Block.Properties.of(Material.PLANT, MaterialColor.PLANT).strength(0).noCollission().solidity(Solidity.AIR).filtersBy(0).dontChangeBgStateOnBreak()), new Vector2(48, 48));
    public static final Block LADDER = Blocks.register("minecraft:ladder", new Block(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(0.4f)), new Vector2(176, 48));
    public static final Block NETHERRACK = Blocks.register("minecraft:netherrack", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.4f)), new Vector2(192, 48));
    public static final Block IRON_BLOCK = Blocks.register("minecraft:iron_block", new Block(Block.Properties.of(Material.METAL, MaterialColor.METAL).strength(5, 6)), new Vector2(224, 48));
    public static final Block DIAMOND_BLOCK = Blocks.register("minecraft:diamond_block", new Block(Block.Properties.of(Material.METAL, MaterialColor.METAL).strength(5, 6)), new Vector2(240, 48));
    public static final Block RED_CONCRETE = Blocks.register("minecraft:red_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(0, 64));
    public static final Block ORANGE_CONCRETE = Blocks.register("minecraft:orange_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(16, 64));
    public static final Block YELLOW_CONCRETE = Blocks.register("minecraft:yellow_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(32, 64));
    public static final Block GREEN_CONCRETE = Blocks.register("minecraft:green_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(48, 64));
    public static final Block LIME_CONCRETE = Blocks.register("minecraft:lime_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(64, 64));
    public static final Block CYAN_CONCRETE = Blocks.register("minecraft:cyan_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(80, 64));
    public static final Block BLUE_CONCRETE = Blocks.register("minecraft:blue_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(96, 64));
    public static final Block MAGENTA_CONCRETE = Blocks.register("minecraft:magenta_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(112, 64));
    public static final Block PINK_CONCRETE = Blocks.register("minecraft:pink_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(128, 64));
    public static final Block WHITE_CONCRETE = Blocks.register("minecraft:white_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(144, 64));
    public static final Block LIGHT_BLUE_CONCRETE = Blocks.register("minecraft:light_blue_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(160, 64));
    public static final Block LIGHT_GREY_CONCRETE = Blocks.register("minecraft:light_grey_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(176, 64));
    public static final Block BLACK_CONCRETE = Blocks.register("minecraft:black_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(192, 64));
    public static final Block BROWN_CONCRETE = Blocks.register("minecraft:brown_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(208, 64));
    public static final Block GREY_CONCRETE = Blocks.register("minecraft:grey_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(224, 64));
    public static final Block PURPLE_CONCRETE = Blocks.register("minecraft:purple_concrete", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.8f)), new Vector2(240, 64));
    public static final Block RED_CONCRETE_POWDER = Blocks.register("minecraft:red_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(0, 80));
    public static final Block ORANGE_CONCRETE_POWDER = Blocks.register("minecraft:orange_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(16, 80));
    public static final Block YELLOW_CONCRETE_POWDER = Blocks.register("minecraft:yellow_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(32, 80));
    public static final Block GREEN_CONCRETE_POWDER = Blocks.register("minecraft:green_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(48, 80));
    public static final Block LIME_CONCRETE_POWDER = Blocks.register("minecraft:lime_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(64, 80));
    public static final Block CYAN_CONCRETE_POWDER = Blocks.register("minecraft:cyan_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(80, 80));
    public static final Block BLUE_CONCRETE_POWDER = Blocks.register("minecraft:blue_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(96, 80));
    public static final Block MAGENTA_CONCRETE_POWDER = Blocks.register("minecraft:magenta_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(112, 80));
    public static final Block PINK_CONCRETE_POWDER = Blocks.register("minecraft:pink_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(128, 80));
    public static final Block WHITE_CONCRETE_POWDER = Blocks.register("minecraft:white_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(144, 80));
    public static final Block LIGHT_BLUE_CONCRETE_POWDER = Blocks.register("minecraft:light_blue_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(160, 80));
    public static final Block LIGHT_GREY_CONCRETE_POWDER = Blocks.register("minecraft:light_grey_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(176, 80));
    public static final Block BLACK_CONCRETE_POWDER = Blocks.register("minecraft:black_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(192, 80));
    public static final Block BROWN_CONCRETE_POWDER = Blocks.register("minecraft:brown_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(208, 80));
    public static final Block GREY_CONCRETE_POWDER = Blocks.register("minecraft:grey_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(224, 80));
    public static final Block PURPLE_CONCRETE_POWDER = Blocks.register("minecraft:purple_concrete_powder", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(240, 80));
    public static final Block RED_GLAZED_TERRACOTTA = Blocks.register("minecraft:red_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(0, 96));
    public static final Block ORANGE_GLAZED_TERRACOTTA = Blocks.register("minecraft:orange_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(16, 96));
    public static final Block YELLOW_GLAZED_TERRACOTTA = Blocks.register("minecraft:yellow_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(32, 96));
    public static final Block GREEN_GLAZED_TERRACOTTA = Blocks.register("minecraft:green_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(48, 96));
    public static final Block LIME_GLAZED_TERRACOTTA = Blocks.register("minecraft:lime_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(64, 96));
    public static final Block CYAN_GLAZED_TERRACOTTA = Blocks.register("minecraft:cyan_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(80, 96));
    public static final Block BLUE_GLAZED_TERRACOTTA = Blocks.register("minecraft:blue_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(96, 96));
    public static final Block MAGENTA_GLAZED_TERRACOTTA = Blocks.register("minecraft:magenta_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(112, 96));
    public static final Block PINK_GLAZED_TERRACOTTA = Blocks.register("minecraft:pink_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(128, 96));
    public static final Block WHITE_GLAZED_TERRACOTTA = Blocks.register("minecraft:white_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(144, 96));
    public static final Block LIGHT_BLUE_GLAZED_TERRACOTTA = Blocks.register("minecraft:light_blue_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(160, 96));
    public static final Block LIGHT_GREY_GLAZED_TERRACOTTA = Blocks.register("minecraft:light_grey_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(176, 96));
    public static final Block BLACK_GLAZED_TERRACOTTA = Blocks.register("minecraft:black_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(192, 96));
    public static final Block BROWN_GLAZED_TERRACOTTA = Blocks.register("minecraft:brown_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(208, 96));
    public static final Block GREY_GLAZED_TERRACOTTA = Blocks.register("minecraft:grey_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(224, 96));
    public static final Block PURPLE_GLAZED_TERRACOTTA = Blocks.register("minecraft:purple_glazed_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.4f)), new Vector2(240, 96));
    public static final Block RED_TERRACOTTA = Blocks.register("minecraft:red_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(0, 112));
    public static final Block ORANGE_TERRACOTTA = Blocks.register("minecraft:orange_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(16, 112));
    public static final Block YELLOW_TERRACOTTA = Blocks.register("minecraft:yellow_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(32, 112));
    public static final Block GREEN_TERRACOTTA = Blocks.register("minecraft:green_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(48, 112));
    public static final Block LIME_TERRACOTTA = Blocks.register("minecraft:lime_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(64, 112));
    public static final Block CYAN_TERRACOTTA = Blocks.register("minecraft:cyan_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(80, 112));
    public static final Block BLUE_TERRACOTTA = Blocks.register("minecraft:blue_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(96, 112));
    public static final Block MAGENTA_TERRACOTTA = Blocks.register("minecraft:magenta_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(112, 112));
    public static final Block PINK_TERRACOTTA = Blocks.register("minecraft:pink_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(128, 112));
    public static final Block WHITE_TERRACOTTA = Blocks.register("minecraft:white_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(144, 112));
    public static final Block LIGHT_BLUE_TERRACOTTA = Blocks.register("minecraft:light_blue_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(160, 112));
    public static final Block LIGHT_GREY_TERRACOTTA = Blocks.register("minecraft:light_grey_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(176, 112));
    public static final Block BLACK_TERRACOTTA = Blocks.register("minecraft:black_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(192, 112));
    public static final Block BROWN_TERRACOTTA = Blocks.register("minecraft:brown_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(208, 112));
    public static final Block GREY_TERRACOTTA = Blocks.register("minecraft:grey_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(224, 112));
    public static final Block PURPLE_TERRACOTTA = Blocks.register("minecraft:purple_terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(240, 112));
    public static final Block RED_WOOL = Blocks.register("minecraft:red_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(0, 128));
    public static final Block ORANGE_WOOL = Blocks.register("minecraft:orange_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(16, 128));
    public static final Block YELLOW_WOOL = Blocks.register("minecraft:yellow_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(32, 128));
    public static final Block GREEN_WOOL = Blocks.register("minecraft:green_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(48, 128));
    public static final Block LIME_WOOL = Blocks.register("minecraft:lime_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(64, 128));
    public static final Block CYAN_WOOL = Blocks.register("minecraft:cyan_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(80, 128));
    public static final Block BLUE_WOOL = Blocks.register("minecraft:blue_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(96, 128));
    public static final Block MAGENTA_WOOL = Blocks.register("minecraft:magenta_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(112, 128));
    public static final Block PINK_WOOL = Blocks.register("minecraft:pink_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(128, 128));
    public static final Block WHITE_WOOL = Blocks.register("minecraft:white_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(144, 128));
    public static final Block LIGHT_BLUE_WOOL = Blocks.register("minecraft:light_blue_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(160, 128));
    public static final Block LIGHT_GREY_WOOL = Blocks.register("minecraft:light_grey_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(176, 128));
    public static final Block BLACK_WOOL = Blocks.register("minecraft:black_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(192, 128));
    public static final Block BROWN_WOOL = Blocks.register("minecraft:brown_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(208, 128));
    public static final Block GREY_WOOL = Blocks.register("minecraft:grey_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(224, 128));
    public static final Block PURPLE_WOOL = Blocks.register("minecraft:purple_wool", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(240, 128));
    public static final Block COBBLED_DEEPSLATE = Blocks.register("minecraft:cobbled_deepslate", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(3.5f, 6)), new Vector2(240, 160));
    public static final Block CLAY = Blocks.register("minecraft:clay", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.6f)), new Vector2(224, 192));
    public static final Block ALLIUM = Blocks.register("minecraft:allium", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(176, 160));
    public static final Block AZURE_BLUET = Blocks.register("minecraft:azure_bluet", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(192, 160));
    public static final Block CORNFLOWER = Blocks.register("minecraft:cornflower", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(208, 160));
    public static final Block DANDILION = Blocks.register("minecraft:dandilion", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(224, 160));
    public static final Block LILY_OF_THE_VALLEY = Blocks.register("minecraft:lily_of_the_valley", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(176, 176));
    public static final Block OXEYE_DAISY = Blocks.register("minecraft:oxeye_daisy", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(192, 176));
    public static final Block PURPLE_TULIP = Blocks.register("minecraft:purple_tulip", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(208, 176));
    public static final Block WHITE_TULIP = Blocks.register("minecraft:white_tulip", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(224, 176));
    public static final Block RED_TULIP = Blocks.register("minecraft:red_tulip", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(240, 176));
    public static final Block LILAC_TOP = Blocks.register("minecraft:lilac_top", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(176, 192));
    public static final Block PEONY = Blocks.register("minecraft:peony", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(192, 192));
    public static final Block ORANGE_TULIP = Blocks.register("minecraft:orange_tulip", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(240, 192));
    public static final Block ROSE = Blocks.register("minecraft:rose", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(192, 224));
    public static final Block JUNGLE_SAPLING = Blocks.register("minecraft:jungle_sapling", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(208, 224));
    public static final Block JUNGLE_LOG = Blocks.register("minecraft:jungle_log", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2f)), new Vector2(224, 224));
    public static final Block JUNGLE_DOOR = Blocks.register("minecraft:jungle_door", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(3f)), new Vector2(240, 224));
    public static final Block JUNGLE_PLANKS = Blocks.register("minecraft:jungle_planks", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2f, 3f)), new Vector2(208, 240));
    public static final Block JUNGLE_LEAVES = Blocks.register("minecraft:jungle_leaves", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.2f).noCollission().backgroundBlockState(Blocks.AIR.defaultBlockState()).solidity(Solidity.AIR).filtersBy(1)), new Vector2(224, 240));
    public static final Block COCOA_BEANS = Blocks.register("minecraft:cocoa_beans", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.2f, 3)), new Vector2(240, 208));
    public static final Block SAND = Blocks.register("minecraft:sand", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(224, 16));
    public static final Block SHORT_GRASS = Blocks.register("minecraft:short_grass", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE)), new Vector2(240, 16));
    public static final Block BRICKS = Blocks.register("minecraft:bricks", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(3f, 6f)), new Vector2(256, 0));
    public static final Block TERRACOTTA = Blocks.register("minecraft:terracotta", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.25f, 4.2f)), new Vector2(256, 96));
    public static final Block COARSE_DIRT = Blocks.register("minecraft:coarse_dirt", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(256, 112));
    public static final Block GRAVEL = Blocks.register("minecraft:gravel", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.6f)), new Vector2(256, 128));
    public static final Block CALCITE = Blocks.register("minecraft:calcite", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.6f)), new Vector2(256, 144));
    public static final Block MUD = Blocks.register("minecraft:mud", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(256, 160));
    public static final Block MYCELIUM = Blocks.register("minecraft:mycelium", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.6f)), new Vector2(256, 176));
    public static final Block PODZOL = Blocks.register("minecraft:podzol", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(256, 192));
    public static final Block ICE = Blocks.register("minecraft:ice", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(256, 208));
    public static final Block PACKED_ICE = Blocks.register("minecraft:packed_ice", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(256, 224));
    public static final Block POWDER_SNOW = Blocks.register("minecraft:powder_snow", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.25f)), new Vector2(256, 240));
    public static final Block SNOW = Blocks.register("minecraft:snow", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.2f)), new Vector2(272, 0));
    public static final Block RED_SAND = Blocks.register("minecraft:red_sand", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.5f)), new Vector2(272, 16));
    public static final Block RED_SANDSTONE = Blocks.register("minecraft:red_sandstone", new Block(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(0.8f)), new Vector2(272, 32));
    public static final Block FARMLAND = Blocks.register("minecraft:farmland", new FarmlandBlock(Block.Properties.of(Material.DIRT, MaterialColor.DIRT).strength(0.6f)), new Vector2(272, 48));
    public static final Block HAY_BLOCK = Blocks.register("minecraft:hay_block", new FarmlandBlock(Block.Properties.of(Material.DIRT, MaterialColor.DIRT).strength(0.6f)), new Vector2(272, 96));
    public static final Block OAK_SAPLING = Blocks.register("minecraft:oak_sapling", new Block(Block.Properties.of(Material.PLANT, MaterialColor.PLANT).dontChangeBgStateOnBreak()), new Vector2(272, 112));

    /**
     * Registers a block and its atlas coordinates.
     */
    private static Block register(String string, Block block, Vector2 atlasCoord) {
        //Logger.INFO("Attempting to register block %s", string);
        Minecraft.getInstance().atlas.register(block, (float)(atlasCoord.x),(float) (atlasCoord.y));
        return dev.alexco.registry.Registry.register(Registry.BLOCK, string, block);
    }

    static {
        for (Block block : Registry.BLOCK) {
            for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
                blockState.initCache();
                Block.BLOCK_STATE_REGISTRY.add(blockState);
            }
            block.getLootTable();
        }
        Minecraft.getInstance().atlas.register(TEST, 64, 0);
        Minecraft.getInstance().atlas.registerBlockState(TEST.defaultBlockState().setValue(BlockStateProperties.TEST, true), 64, 16);
        Minecraft.getInstance().atlas.register(OAK_DOOR, 192, 16);
        Minecraft.getInstance().atlas.registerBlockState(
            OAK_DOOR.defaultBlockState().setValue(BlockStateProperties.TOP, true).setValue(BlockStateProperties.OPEN, false),
            192,
            32
        );
        Minecraft.getInstance().atlas.registerBlockState(
            OAK_DOOR.defaultBlockState().setValue(BlockStateProperties.TOP, true).setValue(BlockStateProperties.OPEN, true),
            192,
            32
        );
        Minecraft.getInstance().atlas.register(OAK_SLAB, 208, 16);
        Minecraft.getInstance().atlas.register(OAK_STAIRS, 208, 16);
        Minecraft.getInstance().atlas.register(FURNACE, 16, 32);
        Minecraft.getInstance().atlas.registerBlockState(FURNACE.defaultBlockState().setValue(BlockStateProperties.LIT, true), 16, 48);
        Minecraft.getInstance().atlas.register(FARMLAND, 272, 64);
        Minecraft.getInstance().atlas.registerBlockState(FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 0), 272, 64);
        for (int i = 1; i <= 7; i++) {
            Minecraft.getInstance().atlas.registerBlockState(FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, i), 272, 48);
        }

        Minecraft.getInstance().atlas.register(WHEAT, 48, 48);
        for (int i = 0; i <= 7; i++) {
            Minecraft.getInstance().atlas.registerBlockState(
                WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE, i),
                48 + (i * 16),
                48
            );
        }
    }

}
