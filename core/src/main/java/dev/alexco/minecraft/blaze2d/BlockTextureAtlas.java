package dev.alexco.minecraft.blaze2d;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;

import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;

public class BlockTextureAtlas {
    public static final float TEXTURE_SIZE = 512f;
    private static final float BLOCK_SIZE = 16f;
    private Map<BlockState, Vector2> blockStateUVMap = new HashMap<>();
    private Map<Block, Vector2> blockDefaultUVMap = new HashMap<>();


    /**
     * Registers atlas uv coordinates for one concrete block state variant.
     */
    public void registerBlockState(BlockState blockState, float texX, float texY) {
        float u = texX / TEXTURE_SIZE;
        float v = (texY) / TEXTURE_SIZE;
        blockStateUVMap.put(blockState, new Vector2(u, v));
    }



    /**
     * Registers atlas uv coordinates used as the fallback for all states of a block.
     */
    public void register(Block block, float texX, float texY) {
        float u = texX / TEXTURE_SIZE;
        float v = (texY) / TEXTURE_SIZE;
        blockDefaultUVMap.put(block, new Vector2(u, v));
    }


    /**
     * Resolves uv coordinates for a state, falling back to the owning block mapping.
     */
    public Vector2 getUV(BlockState blockState) {
        Vector2 uv = blockStateUVMap.get(blockState);
        if (uv != null) {
            return uv;
        }
        return blockDefaultUVMap.getOrDefault(blockState.getBlock(), new Vector2(0, 0));
    }


    public float getUVBlockSize() {
        return BLOCK_SIZE / TEXTURE_SIZE;
    }
}
