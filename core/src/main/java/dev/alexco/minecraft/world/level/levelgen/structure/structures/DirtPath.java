package dev.alexco.minecraft.world.level.levelgen.structure.structures;

import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.levelgen.structure.StructurePlacement;
import dev.alexco.minecraft.world.level.levelgen.structure.StructurePlacement.PlacementMode;
import dev.alexco.minecraft.world.level.levelgen.structure.TerrainFollowingStructure;

public class DirtPath {
    public static TerrainFollowingStructure dirtPath() {
        TerrainFollowingStructure path = new TerrainFollowingStructure("path", 16, 1);

        TerrainFollowingStructure.TerrainLayer pathLayer =
            new TerrainFollowingStructure.TerrainLayer(true);


        pathLayer.addBlocks(0, 15, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
        path.addTerrainLayer(0, pathLayer);
        return path;
    }

    public static StructurePlacement dirt_path = new StructurePlacement(dirtPath(), 10, 3, 1f)
        .setPlacementMode(PlacementMode.TERRAIN_FOLLOWING)
        .setNeedsAir(false)
        .setNeedsSolidGround(false);
}
