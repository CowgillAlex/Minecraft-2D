package dev.alexco.minecraft.blaze2d.debug;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.blaze2d.special.SolidityAABB;
import dev.alexco.minecraft.phys.AABBPool;
import dev.alexco.minecraft.util.Mth;
import dev.alexco.minecraft.world.World;

public class AABBRenderer extends RenderableLifecycle {
    private ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        super.create("AABBRenderer", Color.CYAN);
    }

    /**
     * Draws collidable AABBs around the player and releases pooled boxes afterward.
     */
    @Override
    public void render() {
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);
        List<SolidityAABB> aabbs = Minecraft.getInstance().getWorld().getCubes(Minecraft.getInstance().getPlayer().bb);

        for (SolidityAABB aabb : aabbs) {
            shapeRenderer.setColor(aabb.solidity.debugColor);
            int chunkXo = World.getChunkX(Mth.floor(aabb.boundingbox.x0));
            int chunkx1 = World.getChunkX(Mth.floor(aabb.boundingbox.x1));

            double X0 = ((chunkXo * SharedConstants.CHUNK_WIDTH
                    + World.getLocalX((int) Math.floor(aabb.boundingbox.x0)))
                    * Minecraft.getInstance().getWorld().worldData.blockSize)
                    - Minecraft.getInstance().getWorld().worldData.cameraX;
            double Y0 = (((aabb.boundingbox.y0) * Minecraft.getInstance().getWorld().worldData.blockSize)
                    - Minecraft.getInstance().getWorld().worldData.cameraY);
            double X1 = ((chunkx1 * SharedConstants.CHUNK_WIDTH
                    + World.getLocalX((int) Math.floor(aabb.boundingbox.x1)))
                    * Minecraft.getInstance().getWorld().worldData.blockSize)
                    - Minecraft.getInstance().getWorld().worldData.cameraX;
            double Y1 = (((aabb.boundingbox.y1) * Minecraft.getInstance().getWorld().worldData.blockSize)
                    - Minecraft.getInstance().getWorld().worldData.cameraY);

            shapeRenderer.rect((float) X0, (float) Y0, (float) (X1 - X0), (float) (Y1 - Y0));
            AABBPool.AABBpool.release(aabb.boundingbox);
        }
        shapeRenderer.end();
    }

    @Override
    public void destroy() {
        super.destroy();
        shapeRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub
        super.resize(width, height);
        shapeRenderer.setProjectionMatrix(camera.combined); // this is a bit hacky but itll work

    }
}
