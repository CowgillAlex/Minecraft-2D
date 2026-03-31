package dev.alexco.minecraft.world.level.item;

import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.loot.ToolTier;
import dev.alexco.minecraft.loot.ToolType;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.registry.ResourceLocation;
import java.util.HashSet;
import java.util.Set;

public class Items {

    public static final Set<Block> IGNORE_STATE_PROPERTIES = new HashSet<>();

    public static Item AIR = registerBlock(Blocks.AIR);
    public static Item STONE = registerBlock(Blocks.STONE);
    public static Item DIRT = registerBlock(Blocks.DIRT);
    public static Item GRASS_BLOCK = registerBlock(
            new BlockItem(Blocks.GRASS_BLOCK, new Item.Properties().stacksTo(16)));
    public static Item TEST = registerBlock(Blocks.TEST);
    public static Item BEDROCK = registerBlock(Blocks.BEDROCK);
    public static Item DEEPSLATE = registerBlock(Blocks.DEEPSLATE);
    public static Item WATER = registerBlock(Blocks.WATER);
    public static Item FLOWING_WATER = registerBlock(Blocks.FLOWING_WATER);

    public static Item OAK_LOG = registerBlock(Blocks.OAK_LOG);
    public static Item OAK_LEAVES = registerBlock(Blocks.OAK_LEAVES);
    public static Item COAL_ORE = registerBlock(Blocks.COAL_ORE);
    public static Item DEEPSLATE_COAL_ORE = registerBlock(Blocks.DEEPSLATE_COAL);
    public static Item IRON_ORE = registerBlock(Blocks.IRON_ORE);
    public static Item DEEPSLATE_IRON_ORE = registerBlock(Blocks.DEEPSLATE_IRON);
    public static Item GOLD_ORE = registerBlock(Blocks.GOLD_ORE);
    public static Item DEEPSLATE_GOLD_ORE = registerBlock(Blocks.DEEPSLATE_GOLD);
    public static Item DIAMOND_ORE = registerBlock(Blocks.DIAMOND_ORE);
    public static Item DEEPSLATE_DIAMOND_ORE = registerBlock(Blocks.DEEPSLATE_DIAMOND);
    public static Item REDSTONE_ORE = registerBlock(Blocks.REDSTONE_ORE);
    public static Item DEEPSLATE_REDSTONE_ORE = registerBlock(Blocks.DEEPSLATE_REDSTONE_ORE);
    public static Item COBBLESTONE = registerBlock(Blocks.COBBLESTONE);
    public static Item MOSSY_COBBLESTONE = registerBlock(Blocks.MOSSY_COBBLESTONE);
    public static Item STONE_BRICKS = registerBlock(Blocks.STONE_BRICKS);
    public static Item MOSSY_STONE_BRICKS = registerBlock(Blocks.MOSSY_STONE_BRICKS);
    public static Item TORCH = registerBlock(Blocks.TORCH);
    public static Item SPAWNER = registerBlock(Blocks.SPAWNER);
    public static Item OAK_PLANKS = registerBlock(Blocks.OAK_PLANKS);
    public static Item OAK_DOOR = registerBlock(Blocks.OAK_DOOR);
    public static Item OAK_SLAB = registerBlock(Blocks.OAK_SLAB);
    public static Item OAK_STAIRS = registerBlock(Blocks.OAK_STAIRS);
    public static Item CRAFTING_TABLE = registerBlock(Blocks.CRAFTING_TABLE);
    public static Item FURNACE = registerBlock(Blocks.FURNACE);
    public static Item TALL_GRASS = registerBlock(Blocks.TALL_GRASS);
    public static Item BAMBOO = registerBlock(Blocks.BAMBOO);
    public static Item BARREL = registerBlock(Blocks.BARREL);
    public static Item SANDSTONE = registerBlock(Blocks.SANDSTONE);
    public static Item BAMBOO_PLANKS = registerBlock(Blocks.BAMBOO_PLANKS);
    public static Item BAMBOO_BLOCK = registerBlock(Blocks.BAMBOO_BLOCK);
    public static Item BAMBOO_MOSAIC = registerBlock(Blocks.BAMBOO_MOSAIC);
    public static Item BAMBOO_DOOR = registerBlock(Blocks.BAMBOO_DOOR);
    public static Item GOLD_BLOCK = registerBlock(Blocks.GOLD_BLOCK);
    public static Item NETHERITE_BLOCK = registerBlock(Blocks.NETHERITE_BLOCK);
    public static Item ANCIENT_DEBRIS = registerBlock(Blocks.ANCIENT_DEBRIS);
    public static Item WHEAT = registerBlock(Blocks.WHEAT);
    public static Item LADDER = registerBlock(Blocks.LADDER);
    public static Item NETHERRACK = registerBlock(Blocks.NETHERRACK);
    public static Item IRON_BLOCK = registerBlock(Blocks.IRON_BLOCK);
    public static Item DIAMOND_BLOCK = registerBlock(Blocks.DIAMOND_BLOCK);
    public static Item RED_CONCRETE = registerBlock(Blocks.RED_CONCRETE);
    public static Item ORANGE_CONCRETE = registerBlock(Blocks.ORANGE_CONCRETE);
    public static Item YELLOW_CONCRETE = registerBlock(Blocks.YELLOW_CONCRETE);
    public static Item GREEN_CONCRETE = registerBlock(Blocks.GREEN_CONCRETE);
    public static Item LIME_CONCRETE = registerBlock(Blocks.LIME_CONCRETE);
    public static Item CYAN_CONCRETE = registerBlock(Blocks.CYAN_CONCRETE);
    public static Item BLUE_CONCRETE = registerBlock(Blocks.BLUE_CONCRETE);
    public static Item MAGENTA_CONCRETE = registerBlock(Blocks.MAGENTA_CONCRETE);
    public static Item PINK_CONCRETE = registerBlock(Blocks.PINK_CONCRETE);
    public static Item WHITE_CONCRETE = registerBlock(Blocks.WHITE_CONCRETE);
    public static Item LIGHT_BLUE_CONCRETE = registerBlock(Blocks.LIGHT_BLUE_CONCRETE);
    public static Item LIGHT_GREY_CONCRETE = registerBlock(Blocks.LIGHT_GREY_CONCRETE);
    public static Item BLACK_CONCRETE = registerBlock(Blocks.BLACK_CONCRETE);
    public static Item BROWN_CONCRETE = registerBlock(Blocks.BROWN_CONCRETE);
    public static Item GREY_CONCRETE = registerBlock(Blocks.GREY_CONCRETE);
    public static Item PURPLE_CONCRETE = registerBlock(Blocks.PURPLE_CONCRETE);
    public static Item RED_CONCRETE_POWDER = registerBlock(Blocks.RED_CONCRETE_POWDER);
    public static Item ORANGE_CONCRETE_POWDER = registerBlock(Blocks.ORANGE_CONCRETE_POWDER);
    public static Item YELLOW_CONCRETE_POWDER = registerBlock(Blocks.YELLOW_CONCRETE_POWDER);
    public static Item GREEN_CONCRETE_POWDER = registerBlock(Blocks.GREEN_CONCRETE_POWDER);
    public static Item LIME_CONCRETE_POWDER = registerBlock(Blocks.LIME_CONCRETE_POWDER);
    public static Item CYAN_CONCRETE_POWDER = registerBlock(Blocks.CYAN_CONCRETE_POWDER);
    public static Item BLUE_CONCRETE_POWDER = registerBlock(Blocks.BLUE_CONCRETE_POWDER);
    public static Item MAGENTA_CONCRETE_POWDER = registerBlock(Blocks.MAGENTA_CONCRETE_POWDER);
    public static Item PINK_CONCRETE_POWDER = registerBlock(Blocks.PINK_CONCRETE_POWDER);
    public static Item WHITE_CONCRETE_POWDER = registerBlock(Blocks.WHITE_CONCRETE_POWDER);
    public static Item LIGHT_BLUE_CONCRETE_POWDER = registerBlock(Blocks.LIGHT_BLUE_CONCRETE_POWDER);
    public static Item LIGHT_GREY_CONCRETE_POWDER = registerBlock(Blocks.LIGHT_GREY_CONCRETE_POWDER);
    public static Item BLACK_CONCRETE_POWDER = registerBlock(Blocks.BLACK_CONCRETE_POWDER);
    public static Item BROWN_CONCRETE_POWDER = registerBlock(Blocks.BROWN_CONCRETE_POWDER);
    public static Item GREY_CONCRETE_POWDER = registerBlock(Blocks.GREY_CONCRETE_POWDER);
    public static Item PURPLE_CONCRETE_POWDER = registerBlock(Blocks.PURPLE_CONCRETE_POWDER);
    public static Item RED_GLAZED_TERRACOTTA = registerBlock(Blocks.RED_GLAZED_TERRACOTTA);
    public static Item ORANGE_GLAZED_TERRACOTTA = registerBlock(Blocks.ORANGE_GLAZED_TERRACOTTA);
    public static Item YELLOW_GLAZED_TERRACOTTA = registerBlock(Blocks.YELLOW_GLAZED_TERRACOTTA);
    public static Item GREEN_GLAZED_TERRACOTTA = registerBlock(Blocks.GREEN_GLAZED_TERRACOTTA);
    public static Item LIME_GLAZED_TERRACOTTA = registerBlock(Blocks.LIME_GLAZED_TERRACOTTA);
    public static Item CYAN_GLAZED_TERRACOTTA = registerBlock(Blocks.CYAN_GLAZED_TERRACOTTA);
    public static Item BLUE_GLAZED_TERRACOTTA = registerBlock(Blocks.BLUE_GLAZED_TERRACOTTA);
    public static Item MAGENTA_GLAZED_TERRACOTTA = registerBlock(Blocks.MAGENTA_GLAZED_TERRACOTTA);
    public static Item PINK_GLAZED_TERRACOTTA = registerBlock(Blocks.PINK_GLAZED_TERRACOTTA);
    public static Item WHITE_GLAZED_TERRACOTTA = registerBlock(Blocks.WHITE_GLAZED_TERRACOTTA);
    public static Item LIGHT_BLUE_GLAZED_TERRACOTTA = registerBlock(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
    public static Item LIGHT_GREY_GLAZED_TERRACOTTA = registerBlock(Blocks.LIGHT_GREY_GLAZED_TERRACOTTA);
    public static Item BLACK_GLAZED_TERRACOTTA = registerBlock(Blocks.BLACK_GLAZED_TERRACOTTA);
    public static Item BROWN_GLAZED_TERRACOTTA = registerBlock(Blocks.BROWN_GLAZED_TERRACOTTA);
    public static Item GREY_GLAZED_TERRACOTTA = registerBlock(Blocks.GREY_GLAZED_TERRACOTTA);
    public static Item PURPLE_GLAZED_TERRACOTTA = registerBlock(Blocks.PURPLE_GLAZED_TERRACOTTA);
    public static Item RED_TERRACOTTA = registerBlock(Blocks.RED_TERRACOTTA);
    public static Item ORANGE_TERRACOTTA = registerBlock(Blocks.ORANGE_TERRACOTTA);
    public static Item YELLOW_TERRACOTTA = registerBlock(Blocks.YELLOW_TERRACOTTA);
    public static Item GREEN_TERRACOTTA = registerBlock(Blocks.GREEN_TERRACOTTA);
    public static Item LIME_TERRACOTTA = registerBlock(Blocks.LIME_TERRACOTTA);
    public static Item CYAN_TERRACOTTA = registerBlock(Blocks.CYAN_TERRACOTTA);
    public static Item BLUE_TERRACOTTA = registerBlock(Blocks.BLUE_TERRACOTTA);
    public static Item MAGENTA_TERRACOTTA = registerBlock(Blocks.MAGENTA_TERRACOTTA);
    public static Item PINK_TERRACOTTA = registerBlock(Blocks.PINK_TERRACOTTA);
    public static Item WHITE_TERRACOTTA = registerBlock(Blocks.WHITE_TERRACOTTA);
    public static Item LIGHT_BLUE_TERRACOTTA = registerBlock(Blocks.LIGHT_BLUE_TERRACOTTA);
    public static Item LIGHT_GREY_TERRACOTTA = registerBlock(Blocks.LIGHT_GREY_TERRACOTTA);
    public static Item BLACK_TERRACOTTA = registerBlock(Blocks.BLACK_TERRACOTTA);
    public static Item BROWN_TERRACOTTA = registerBlock(Blocks.BROWN_TERRACOTTA);
    public static Item GREY_TERRACOTTA = registerBlock(Blocks.GREY_TERRACOTTA);
    public static Item PURPLE_TERRACOTTA = registerBlock(Blocks.PURPLE_TERRACOTTA);
    public static Item RED_WOOL = registerBlock(Blocks.RED_WOOL);
    public static Item ORANGE_WOOL = registerBlock(Blocks.ORANGE_WOOL);
    public static Item YELLOW_WOOL = registerBlock(Blocks.YELLOW_WOOL);
    public static Item GREEN_WOOL = registerBlock(Blocks.GREEN_WOOL);
    public static Item LIME_WOOL = registerBlock(Blocks.LIME_WOOL);
    public static Item CYAN_WOOL = registerBlock(Blocks.CYAN_WOOL);
    public static Item BLUE_WOOL = registerBlock(Blocks.BLUE_WOOL);
    public static Item MAGENTA_WOOL = registerBlock(Blocks.MAGENTA_WOOL);
    public static Item PINK_WOOL = registerBlock(Blocks.PINK_WOOL);
    public static Item WHITE_WOOL = registerBlock(Blocks.WHITE_WOOL);
    public static Item LIGHT_BLUE_WOOL = registerBlock(Blocks.LIGHT_BLUE_WOOL);
    public static Item LIGHT_GREY_WOOL = registerBlock(Blocks.LIGHT_GREY_WOOL);
    public static Item BLACK_WOOL = registerBlock(Blocks.BLACK_WOOL);
    public static Item BROWN_WOOL = registerBlock(Blocks.BROWN_WOOL);
    public static Item GREY_WOOL = registerBlock(Blocks.GREY_WOOL);
    public static Item PURPLE_WOOL = registerBlock(Blocks.PURPLE_WOOL);
    public static Item COBBLED_DEEPSLATE = registerBlock(Blocks.COBBLED_DEEPSLATE);
    public static Item CLAY = registerBlock(Blocks.CLAY);
    public static Item ALLIUM = registerBlock(Blocks.ALLIUM);
    public static Item AZURE_BLUET = registerBlock(Blocks.AZURE_BLUET);
    public static Item CORNFLOWER = registerBlock(Blocks.CORNFLOWER);
    public static Item DANDILION = registerBlock(Blocks.DANDILION);
    public static Item LILY_OF_THE_VALLEY = registerBlock(Blocks.LILY_OF_THE_VALLEY);
    public static Item OXEYE_DAISY = registerBlock(Blocks.OXEYE_DAISY);
    public static Item PURPLE_TULIP = registerBlock(Blocks.PURPLE_TULIP);
    public static Item WHITE_TULIP = registerBlock(Blocks.WHITE_TULIP);
    public static Item RED_TULIP = registerBlock(Blocks.RED_TULIP);
    public static Item LILAC_TOP = registerBlock(Blocks.LILAC_TOP);
    public static Item PEONY = registerBlock(Blocks.PEONY);
    public static Item ORANGE_TULIP = registerBlock(Blocks.ORANGE_TULIP);
    public static Item ROSE = registerBlock(Blocks.ROSE);
    public static Item JUNGLE_SAPLING = registerBlock(Blocks.JUNGLE_SAPLING);
    public static Item JUNGLE_LOG = registerBlock(Blocks.JUNGLE_LOG);
    public static Item JUNGLE_DOOR = registerBlock(Blocks.JUNGLE_DOOR);
    public static Item JUNGLE_PLANKS = registerBlock(Blocks.JUNGLE_PLANKS);
    public static Item JUNGLE_LEAVES = registerBlock(Blocks.JUNGLE_LEAVES);
    public static Item COCOA_BEANS = registerBlock(Blocks.COCOA_BEANS);
    public static Item SAND = registerBlock(Blocks.SAND);
    public static Item SHORT_GRASS = registerBlock(Blocks.SHORT_GRASS);
    public static Item BRICKS = registerBlock(Blocks.BRICKS);
    public static Item TERRACOTTA = registerBlock(Blocks.TERRACOTTA);
    public static Item COARSE_DIRT = registerBlock(Blocks.COARSE_DIRT);
    public static Item GRAVEL = registerBlock(Blocks.GRAVEL);
    public static Item CALCITE = registerBlock(Blocks.CALCITE);
    public static Item MUD = registerBlock(Blocks.MUD);
    public static Item MYCELIUM = registerBlock(Blocks.MYCELIUM);
    public static Item PODZOL = registerBlock(Blocks.PODZOL);
    public static Item ICE = registerBlock(Blocks.ICE);
    public static Item PACKED_ICE = registerBlock(Blocks.PACKED_ICE);
    public static Item POWDER_SNOW = registerBlock(Blocks.POWDER_SNOW);
    public static Item SNOW = registerBlock(Blocks.SNOW);
    public static Item RED_SAND = registerBlock(Blocks.RED_SAND);
    public static Item RED_SANDSTONE = registerBlock(Blocks.RED_SANDSTONE);
    public static Item FARMLAND = registerBlock(Blocks.FARMLAND);
    public static Item HAY_BLOCK = registerBlock(Blocks.HAY_BLOCK);
    public static Item OAK_SAPLING = registerBlock(Blocks.OAK_SAPLING);
    public static Item STICK = registerItem("minecraft:stick", new Item(new Item.Properties().atlas(0, 240)));
    public static Item RED_DYE = registerItem("minecraft:red_dye", new Item(new Item.Properties().atlas(0, 144)));
    public static Item ORANGE_DYE = registerItem("minecraft:orange_dye", new Item(new Item.Properties().atlas(16, 144)));
    public static Item YELLOW_DYE = registerItem("minecraft:yellow_dye", new Item(new Item.Properties().atlas(32, 144)));
    public static Item GREEN_DYE = registerItem("minecraft:green_dye", new Item(new Item.Properties().atlas(48, 144)));
    public static Item LIME_DYE = registerItem("minecraft:lime_dye", new Item(new Item.Properties().atlas(64, 144)));
    public static Item CYAN_DYE = registerItem("minecraft:cyan_dye", new Item(new Item.Properties().atlas(80, 144)));
    public static Item BLUE_DYE = registerItem("minecraft:blue_dye", new Item(new Item.Properties().atlas(96, 144)));
    public static Item MAGENTA_DYE = registerItem("minecraft:magenta_dye", new Item(new Item.Properties().atlas(112, 144)));
    public static Item PINK_DYE = registerItem("minecraft:pink_dye", new Item(new Item.Properties().atlas(128, 144)));
    public static Item WHITE_DYE = registerItem("minecraft:white_dye", new Item(new Item.Properties().atlas(144, 144)));
    public static Item LIGHT_BLUE_DYE = registerItem("minecraft:light_blue_dye", new Item(new Item.Properties().atlas(160, 144)));
    public static Item BLACK_DYE = registerItem("minecraft:black_dye", new Item(new Item.Properties().atlas(192, 144)));
    public static Item BROWN_DYE = registerItem("minecraft:brown_dye", new Item(new Item.Properties().atlas(208, 144)));
    public static Item GREY_DYE = registerItem("minecraft:grey_dye", new Item(new Item.Properties().atlas(224, 144)));
    public static Item PURPLE_DYE = registerItem("minecraft:purple_dye", new Item(new Item.Properties().atlas(240, 144)));
    public static Item NETHERITE_INGOT = registerItem("minecraft:netherite_ingot", new Item(new Item.Properties().atlas(0, 160)));
    public static Item NETHERITE_SCRAPS = registerItem("minecraft:netherite_scraps", new Item(new Item.Properties().atlas(16, 160)));
    public static Item NETHERITE_AXE = registerItem("minecraft:netherite_axe", new ToolItem(ToolType.AXE, ToolTier.NETHERITE, new Item.Properties().durability(2031).atlas(32, 160)));
    public static Item NETHERITE_HOE = registerItem("minecraft:netherite_hoe", new ToolItem(ToolType.HOE, ToolTier.NETHERITE, new Item.Properties().durability(2031).atlas(48, 160)));
    public static Item NETHERITE_PICKAXE = registerItem("minecraft:netherite_pickaxe", new ToolItem(ToolType.PICKAXE, ToolTier.NETHERITE, new Item.Properties().durability(2031).atlas(64, 160)));
    public static Item NETHERITE_SHOVEL = registerItem("minecraft:netherite_shovel", new ToolItem(ToolType.SHOVEL, ToolTier.NETHERITE, new Item.Properties().durability(2031).atlas(80, 160)));
    public static Item NETHERITE_BOOTS = registerItem("minecraft:netherite_boots", new Item(new Item.Properties().atlas(112, 160)));
    public static Item NETHERITE_LEGGINGS = registerItem("minecraft:netherite_leggings", new Item(new Item.Properties().atlas(128, 160)));
    public static Item NETHERITE_CHESTPLATE = registerItem("minecraft:netherite_chestplate", new Item(new Item.Properties().atlas(144, 160)));
    public static Item NETHERITE_HELMET = registerItem("minecraft:netherite_helmet", new Item(new Item.Properties().atlas(160, 160)));
    public static Item DIAMOND = registerItem("minecraft:diamond", new Item(new Item.Properties().atlas(0, 176)));
    public static Item WHEAT_ITEM = registerItem("minecraft:wheat", new Item(new Item.Properties().atlas(16, 176)));
    public static Item DIAMOND_AXE = registerItem("minecraft:diamond_axe", new ToolItem(ToolType.AXE, ToolTier.DIAMOND, new Item.Properties().durability(1561).atlas(32, 176)));
    public static Item DIAMOND_HOE = registerItem("minecraft:diamond_hoe", new ToolItem(ToolType.HOE, ToolTier.DIAMOND, new Item.Properties().durability(1561).atlas(48, 176)));
    public static Item DIAMOND_PICKAXE = registerItem("minecraft:diamond_pickaxe", new ToolItem(ToolType.PICKAXE, ToolTier.DIAMOND, new Item.Properties().durability(1561).atlas(64, 176)));
    public static Item DIAMOND_SHOVEL = registerItem("minecraft:diamond_shovel", new ToolItem(ToolType.SHOVEL, ToolTier.DIAMOND, new Item.Properties().durability(1561).atlas(80, 176)));
    public static Item DIAMOND_SWORD = registerItem("minecraft:diamond_sword", new ToolItem(ToolType.SWORD, ToolTier.DIAMOND, new Item.Properties().durability(1561).atlas(96, 176)));
    public static Item DIAMOND_BOOTS = registerItem("minecraft:diamond_boots", new Item(new Item.Properties().atlas(112, 176)));
    public static Item DIAMOND_LEGGINGS = registerItem("minecraft:diamond_leggings", new Item(new Item.Properties().atlas(128, 176)));
    public static Item DIAMOND_CHESTPLATE = registerItem("minecraft:diamond_chestplate", new Item(new Item.Properties().atlas(144, 176)));
    public static Item DIAMOND_HELMET = registerItem("minecraft:diamond_helmet", new Item(new Item.Properties().atlas(160, 176)));
    public static Item IRON_INGOT = registerItem("minecraft:iron_ingot", new Item(new Item.Properties().atlas(0, 192)));
    public static Item IRON_NUGGET = registerItem("minecraft:iron_nugget", new Item(new Item.Properties().atlas(16, 192)));
    public static Item IRON_AXE = registerItem("minecraft:iron_axe", new ToolItem(ToolType.AXE, ToolTier.IRON, new Item.Properties().durability(250).atlas(32, 192)));
    public static Item IRON_HOE = registerItem("minecraft:iron_hoe", new ToolItem(ToolType.HOE, ToolTier.IRON, new Item.Properties().durability(250).atlas(48, 192)));
    public static Item IRON_PICKAXE = registerItem("minecraft:iron_pickaxe", new ToolItem(ToolType.PICKAXE, ToolTier.IRON, new Item.Properties().durability(250).atlas(64, 192)));
    public static Item IRON_SHOVEL = registerItem("minecraft:iron_shovel", new ToolItem(ToolType.SHOVEL, ToolTier.IRON, new Item.Properties().durability(250).atlas(80, 192)));
    public static Item IRON_SWORD = registerItem("minecraft:iron_sword", new ToolItem(ToolType.SWORD, ToolTier.IRON, new Item.Properties().durability(250).atlas(96, 192)));
    public static Item IRON_BOOTS = registerItem("minecraft:iron_boots", new Item(new Item.Properties().atlas(112, 192)));
    public static Item IRON_LEGGINGS = registerItem("minecraft:iron_leggings", new Item(new Item.Properties().atlas(128, 192)));
    public static Item IRON_CHESTPLATE = registerItem("minecraft:iron_chestplate", new Item(new Item.Properties().atlas(144, 192)));
    public static Item IRON_HELMET = registerItem("minecraft:iron_helmet", new Item(new Item.Properties().atlas(160, 192)));
    public static Item GOLD_INGOT = registerItem("minecraft:gold_ingot", new Item(new Item.Properties().atlas(0, 208)));
    public static Item GOLD_NUGGET = registerItem("minecraft:gold_nugget", new Item(new Item.Properties().atlas(16, 208)));
    public static Item GOLD_AXE = registerItem("minecraft:gold_axe", new ToolItem(ToolType.AXE, ToolTier.GOLD, new Item.Properties().durability(32).atlas(32, 208)));
    public static Item GOLD_HOE = registerItem("minecraft:gold_hoe", new ToolItem(ToolType.HOE, ToolTier.GOLD, new Item.Properties().durability(32).atlas(48, 208)));
    public static Item GOLD_PICKAXE = registerItem("minecraft:gold_pickaxe", new ToolItem(ToolType.PICKAXE, ToolTier.GOLD, new Item.Properties().durability(32).atlas(64, 208)));
    public static Item GOLD_SHOVEL = registerItem("minecraft:gold_shovel", new ToolItem(ToolType.SHOVEL, ToolTier.GOLD, new Item.Properties().durability(32).atlas(80, 208)));
    public static Item GOLD_BOOTS = registerItem("minecraft:gold_boots", new Item(new Item.Properties().atlas(112, 208)));
    public static Item GOLD_LEGGINGS = registerItem("minecraft:gold_leggings", new Item(new Item.Properties().atlas(128, 208)));
    public static Item GOLD_CHESTPLATE = registerItem("minecraft:gold_chestplate", new Item(new Item.Properties().atlas(144, 208)));
    public static Item GOLD_HELMET = registerItem("minecraft:gold_helmet", new Item(new Item.Properties().atlas(160, 208)));
    public static Item BREAD = registerItem("minecraft:bread", new Item(new Item.Properties().atlas(0, 224)));
    public static Item BAMBOO_ITEM = registerItem("minecraft:bamboo", new Item(new Item.Properties().atlas(16, 224)));
    public static Item STONE_AXE = registerItem("minecraft:stone_axe", new ToolItem(ToolType.AXE, ToolTier.STONE, new Item.Properties().durability(131).atlas(32, 224)));
    public static Item STONE_HOE = registerItem("minecraft:stone_hoe", new ToolItem(ToolType.HOE, ToolTier.STONE, new Item.Properties().durability(131).atlas(48, 224)));
    public static Item STONE_PICKAXE = registerItem("minecraft:stone_pickaxe", new ToolItem(ToolType.PICKAXE, ToolTier.STONE, new Item.Properties().durability(131).atlas(64, 224)));
    public static Item STONE_SHOVEL = registerItem("minecraft:stone_shovel", new ToolItem(ToolType.SHOVEL, ToolTier.STONE, new Item.Properties().durability(131).atlas(80, 224)));
    public static Item STONE_SWORD = registerItem("minecraft:stone_sword", new ToolItem(ToolType.SWORD, ToolTier.STONE, new Item.Properties().durability(131).atlas(96, 224)));
    public static Item EMPTY_BUCKET = registerItem("minecraft:empty_bucket", new Item(new Item.Properties().atlas(128, 224)));
    public static Item BONEMEAL = registerItem("minecraft:bonemeal", new Item(new Item.Properties().atlas(144, 224)));
    public static Item BRICK = registerItem("minecraft:brick", new Item(new Item.Properties().atlas(160, 224)));
    public static Item COCOA_BEANS_ITEM = registerItem("minecraft:cocoa_beans", new Item(new Item.Properties().atlas(176, 224)));
    public static Item OAK_DOOR_ITEM = registerItem("minecraft:oak_door", new Item(new Item.Properties().atlas(16, 240)));
    public static Item WOOD_AXE = registerItem("minecraft:wood_axe", new ToolItem(ToolType.AXE, ToolTier.WOOD, new Item.Properties().durability(59).atlas(32, 240)));
    public static Item WOOD_HOE = registerItem("minecraft:wood_hoe", new ToolItem(ToolType.HOE, ToolTier.WOOD, new Item.Properties().durability(59).atlas(48, 240)));
    public static Item WOOD_PICKAXE = registerItem("minecraft:wood_pickaxe", new ToolItem(ToolType.PICKAXE, ToolTier.WOOD, new Item.Properties().durability(59).atlas(64, 240)));
    public static Item WOOD_SHOVEL = registerItem("minecraft:wood_shovel", new ToolItem(ToolType.SHOVEL, ToolTier.WOOD, new Item.Properties().durability(59).atlas(80, 240)));
    public static Item WOOD_SWORD = registerItem("minecraft:wood_sword", new ToolItem(ToolType.SWORD, ToolTier.WOOD, new Item.Properties().durability(59).atlas(96, 240)));
    public static Item WATER_BUCKET = registerItem("minecraft:water_bucket", new Item(new Item.Properties().atlas(128, 240)));
    public static Item BONE = registerItem("minecraft:bone", new Item(new Item.Properties().atlas(144, 240)));
    public static Item CLAY_BALL = registerItem("minecraft:clay_ball", new Item(new Item.Properties().atlas(160, 240)));
    public static Item INK_SAC = registerItem("minecraft:ink_sac", new Item(new Item.Properties().atlas(176, 240)));
    public static Item COOKIE = registerItem("minecraft:cookie", new Item(new Item.Properties().atlas(192, 240)));
    public static Item COW_SPAWN_EGG = registerItem("minecraft:cow_spawn_egg", new Item(new Item.Properties().atlas(272, 144)));
    public static Item ZOMBIE_SPAWN_EGG = registerItem("minecraft:zombie_spawn_egg", new Item(new Item.Properties().atlas(272, 128)));
    public static Item PIG_SPAWN_EGG = registerItem("minecraft:pig_spawn_egg", new Item(new Item.Properties().atlas(272, 160)));
    public static Item REDSTONE_DUST = registerItem("minecraft:redstone_dust", new Item(new Item.Properties().atlas(256, 80)));
    public static Item RAW_GOLD = registerItem("minecraft:raw_gold", new Item(new Item.Properties().atlas(256, 16)));
    public static Item RAW_IRON = registerItem("minecraft:raw_iron", new Item(new Item.Properties().atlas(256, 32)));
    public static Item CHARCOAL = registerItem("minecraft:charcoal", new Item(new Item.Properties().atlas(256, 48)));
    public static Item COAL = registerItem("minecraft:coal", new Item(new Item.Properties().atlas(256, 64)));
    public static Item WHEAT_SEEDS = registerItem("minecraft:wheat_seeds", new Item(new Item.Properties().atlas(272, 80)));

    static {
        IGNORE_STATE_PROPERTIES.add(Blocks.DEEPSLATE);
    }

    /**
     * Registers a block as a default BlockItem with standard item properties.
     */
    private static Item registerBlock(Block block) {
        return Items.registerBlock(new BlockItem(block, new Item.Properties()));
    }

    /**
     * Registers a custom BlockItem instance and maps it back to its block.
     */
    private static Item registerBlock(BlockItem blockItem) {
        return Items.registerBlock(blockItem.getBlock(), blockItem);
    }

    /**
     * Registers an item using the registry key of its backing block.
     */
    protected static Item registerBlock(Block block, Item item) {
        return Items.registerItem(Registry.BLOCK.getKey(block), item);
    }

    /**
     * Registers an item from a namespaced id string.
     */
    private static Item registerItem(String string, Item item) {
        return Items.registerItem(new ResourceLocation(string), item);
    }

    /**
     * Registers an item and updates block-item reverse mappings when applicable.
     */
    private static Item registerItem(ResourceLocation resourceLocation, Item item) {
        if (item instanceof BlockItem) {
            ((BlockItem) item).registerBlocks(Item.BY_BLOCK, item);
        }
        return dev.alexco.registry.Registry.register(Registry.ITEM, resourceLocation, item);
    }
}
