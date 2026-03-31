package dev.alexco.minecraft.world.level.levelgen.structure.structures;

import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.levelgen.structure.Structure;
import dev.alexco.minecraft.world.level.levelgen.structure.StructurePlacement;
import dev.alexco.minecraft.world.level.levelgen.structure.StructurePlacement.PlacementMode;

public class HouseStructure {
    public Structure house;

    public HouseStructure(){
        this.house = new Structure("minecraft:house", 8, 6);
        this.house.addBlock(0, 0, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(1, 0, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(0, 1, Blocks.OAK_DOOR.defaultBlockState().setValue(BlockStateProperties.TOP, false).setValue(BlockStateProperties.OPEN,true));
        this.house.addBlock(0, 2, Blocks.OAK_DOOR.defaultBlockState().setValue(BlockStateProperties.OPEN, true).setValue(BlockStateProperties.TOP, true));
        this.house.addBlock(2, 0, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(3, 0, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(4, 0, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(5, 0, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(6, 0, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(7, 0, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(7, 1, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(7, 2, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(7, 3, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(7, 4, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(7, 5, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(0, 3, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(0, 4, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(0, 5, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(-1, 6, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(0, 6, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(1, 6, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(2, 6, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(3, 6, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(4, 6, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(5, 6, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(6, 6, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(7, 6, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(8, 6, Blocks.OAK_PLANKS.defaultBlockState());

        this.house.addBlock(0, 7, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(1, 7, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(2, 7, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(3, 7, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(4, 7, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(5, 7, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(6, 7, Blocks.OAK_PLANKS.defaultBlockState());
        this.house.addBlock(7, 7, Blocks.OAK_PLANKS.defaultBlockState());
    }


    public static StructurePlacement house_placement = new StructurePlacement(new HouseStructure().house, 11, 5, 1f).setPlacementMode(PlacementMode.SURFACE).setNeedsSolidGround(true);
}
