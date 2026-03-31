package dev.alexco.minecraft.blaze2d;

import static dev.alexco.minecraft.SharedConstants.MAX_LIGHT_LEVEL;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.BlockItemEntity;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.entity.ItemEntity;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.item.BlockItem;
import dev.alexco.minecraft.world.level.item.Item;

public class ItemEntityRenderer extends RenderableLifecycle {

    private Texture atlasTexture;

    public void create() {
        super.create("ItemEntityRenderer", Color.ORANGE);
        Minecraft.getInstance().textureManager.forceLoadTexture("textures.png");
        atlasTexture = Minecraft.getInstance().textureManager.get("textures.png");
        atlasTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }


    /**
     * Renders dropped items and block-items using the shared atlas and entity lighting.
     */
    @Override
    public void render() {
        float blockSize = (float) Minecraft.getInstance().getWorld().worldData.blockSize;
        double camX = Minecraft.getInstance().getWorld().worldData.cameraX;
        double camY = Minecraft.getInstance().getWorld().worldData.cameraY;
        spriteBatch.begin();

        BlockTextureAtlas atlas = Minecraft.getInstance().atlas;

        for (Entity entity : Minecraft.getInstance().getWorld().entities) {
  if (entity.bb == null) {
                Logger.ERROR("Entity bb null for %s at %f, %f", entity.type, entity.x, entity.y);
                continue;
            }
            double x0 = entity.bb.x0 * blockSize;
            double y0 = (entity.bb.y0) * blockSize;
            double x1 = entity.bb.x1 * blockSize;
            double y1 = entity.bb.y1 * blockSize;

            if (y1 < y0) {
                double tmp = y0;
                y0 = y1;
                y1 = tmp;
            }

            if (entity instanceof ItemEntity itemEntity) {
                Item item = itemEntity.item;
                if (item == null) {
                    continue;
                }

                double width = x1 - x0;
                double height = y1 - y0;
                float renderX = (float) (x0 - camX);
                float renderY = (float) (y0 - camY) + (float) Math.sin(Minecraft.getInstance().getSession().getTimer().totalTicks / 20f) * 8f;

                float brightness = RenderableEntity.getLightBrightness(entity);
                spriteBatch.setColor(brightness, brightness, brightness, 1.0f);

                if (item instanceof BlockItem blockItem) {
                    BlockState blockState = blockItem.getBlockState();
                    Vector2 uv = atlas.getUV(blockState);
                    float uvBlockSize = atlas.getUVBlockSize();

                    float srcX = uv.x * BlockTextureAtlas.TEXTURE_SIZE;
                    float srcY = uv.y * BlockTextureAtlas.TEXTURE_SIZE;
                    float srcWidth = uvBlockSize * BlockTextureAtlas.TEXTURE_SIZE;
                    float srcHeight = uvBlockSize * BlockTextureAtlas.TEXTURE_SIZE;

                    spriteBatch.draw(
                        atlasTexture,
                        renderX,
                        renderY,
                        (float) width,
                        (float) height,
                        (int) srcX,
                        (int) srcY,
                        (int) srcWidth,
                        (int) srcHeight,
                        false,
                        false);
                } else {
                    Vector2 coords = item.getAtlasCoords();
                    if (coords == null) {
                        continue;
                    }

                    float u = coords.x / BlockTextureAtlas.TEXTURE_SIZE;
                    float v = coords.y / BlockTextureAtlas.TEXTURE_SIZE;
                    float uvBlockSize = 16f / BlockTextureAtlas.TEXTURE_SIZE;

                    spriteBatch.draw(
                        atlasTexture,
                        renderX,
                        renderY,
                        (float) width,
                        (float) height,
                        u,
                        v + uvBlockSize,
                        u + uvBlockSize,
                        v);
                }
            }

        }
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }
}
