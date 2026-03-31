package dev.alexco.minecraft.world.level.levelgen.structure.structures;

import java.util.function.Predicate;

import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.levelgen.structure.Structure;
import dev.alexco.minecraft.world.level.levelgen.structure.StructurePlacement;
import dev.alexco.minecraft.world.level.levelgen.structure.StructurePlacement.PlacementMode;

public class Dungeon {
    public Structure dungeon;

    public Dungeon() {
        Logger.INFO("Creating structure: minecraft:monster_room");
        this.dungeon = new Structure("minecraft:monster_room", 6, 4);
        Logger.INFO("Created structure: minecraft:monster_room");
        // Base ring
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                dungeon.addBlock(i, j, Blocks.AIR.defaultBlockState());
                dungeon.addBackgroundBlock(i, j, Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        dungeon.addBlock(1, 0, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        dungeon.addBlock(0, 0, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(0, 1, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        dungeon.addBlock(0, 2, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(0, 3, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(2, 0, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(3, 0, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(3, 1, Blocks.SPAWNER.defaultBlockState());
        dungeon.addBlock(4, 0, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(5, 0, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(6, 0, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        dungeon.addBlock(6, 1, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(6, 2, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        dungeon.addBlock(6, 3, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(6, 4, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(5, 4, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        dungeon.addBlock(4, 4, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(3, 4, Blocks.COBBLESTONE.defaultBlockState());
        dungeon.addBlock(2, 4, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        dungeon.addBlock(1, 4, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        dungeon.addBlock(0, 4, Blocks.COBBLESTONE.defaultBlockState());
        Logger.INFO("Finished adding blocks to monster room placement");

    }

    public static StructurePlacement dungeonPlacement = new StructurePlacement(new Dungeon().dungeon, 6, 10, 0.8f)
            .setYRange(0, 127)
            .setNeedsSolidGround(false)
            .setNeedsAir(false).addValidGroundBlock(Blocks.STONE).setPlacementMode(PlacementMode.UNDERGROUND);

}
