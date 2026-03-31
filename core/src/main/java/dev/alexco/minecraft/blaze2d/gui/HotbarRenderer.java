package dev.alexco.minecraft.blaze2d.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.BlockTextureAtlas;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.inventory.Inventory;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.OakSlabBlock;
import dev.alexco.minecraft.world.level.block.OakStairsBlock;
import dev.alexco.minecraft.world.level.item.BlockItem;

public class HotbarRenderer extends RenderableLifecycle {

    Texture hotbar;
    Texture hotbarSelection;
    Texture durabilityPixel;
        public BitmapFont bitmapFont;

    float scale = 3f;

    @Override
    public void create() {
        super.create("Hotbar", Color.FOREST);
        String hotbarP = "textures/gui/";
                bitmapFont = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
 bitmapFont.getData().markupEnabled = true;

        bitmapFont.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        Minecraft.getInstance().textureManager.forceLoadTexture(hotbarP+"hotbar_selection.png");
        Minecraft.getInstance().textureManager.forceLoadTexture(hotbarP+"hotbar.png");
        hotbar = (Minecraft.getInstance().textureManager.get(hotbarP+"hotbar.png"));
        hotbarSelection = (Minecraft.getInstance().textureManager.get(hotbarP+"hotbar_selection.png"));
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        durabilityPixel = new Texture(pixmap);
        pixmap.dispose();
    }

    /**
     * Renders hotbar background, slot items, stack counts, durability bars, and selection cursor.
     */
    @Override
public void render() {
    spriteBatch.begin();
    spriteBatch.draw(hotbar, Gdx.graphics.getWidth() / 2f - (hotbar.getWidth() / 2f) * scale, 0,
            hotbar.getWidth() * scale, hotbar.getHeight() * scale);

    // get the inventory of the player in order to render it
    Inventory inventory = Minecraft.getInstance().getPlayer().inventory;
    float itemSize = 16f * (scale * 0.7f);

    for (int i = 0; i < 9; i++) {
        ItemStack stack = inventory.inventory[i];
        if (stack != null) {
            float x = Gdx.graphics.getWidth() / 2f - (hotbar.getWidth() / 2f) * scale + 5f * scale + ((i * 1.8f) * itemSize);
            float y = 5f * scale;

            if (stack.item instanceof BlockItem) {
                BlockItem blockItem = (BlockItem) stack.item;
                Block block = blockItem.getBlock();
                boolean needsGreenTint = BlockTags.GREEN_TINT.contains(block);

                if (needsGreenTint) {
                    spriteBatch.setColor(Color.GREEN);
                }

                // Get the blockstate uv
                float u = Minecraft.getInstance().atlas.getUV(blockItem.getBlockState()).x;
                float v = Minecraft.getInstance().atlas.getUV(blockItem.getBlockState()).y;
                float uvBlockSize = Minecraft.getInstance().atlas.getUVBlockSize();
                float drawX = x;
                float drawY = y;
                float drawW = itemSize;
                float drawH = itemSize;
                float minU = u;
                float minV = v;
                float maxU = u + uvBlockSize;
                float maxV = v + uvBlockSize;

                if (block instanceof OakSlabBlock) {
                    drawH = itemSize * 0.5f;
                    maxV = minV + (uvBlockSize * 0.5f);
                    spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), drawX, drawY, drawW,
                            drawH, minU, maxV, maxU, minV);
                } else if (block instanceof OakStairsBlock) {
                    // Bottom half
                    spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), drawX, drawY, drawW,
                            itemSize * 0.5f, minU, minV + uvBlockSize, maxU, minV + (uvBlockSize * 0.5f));
                    // Upper-right quarter
                    spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), drawX + (drawW * 0.5f),
                            drawY + (itemSize * 0.5f), drawW * 0.5f, itemSize * 0.5f,
                            minU + (uvBlockSize * 0.5f), minV + (uvBlockSize * 0.5f), maxU, minV);
                } else {
                    spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), drawX, drawY, drawW,
                            drawH, minU, maxV, maxU, minV);
                }

                if (needsGreenTint) {
                    spriteBatch.setColor(Color.WHITE);
                }
            } else {
                com.badlogic.gdx.math.Vector2 coords = stack.item.getAtlasCoords();
                if (coords != null) {
                    float u = coords.x / BlockTextureAtlas.TEXTURE_SIZE;
                    float v = coords.y / BlockTextureAtlas.TEXTURE_SIZE;
                    float uvBlockSize = 16f / BlockTextureAtlas.TEXTURE_SIZE;
                    spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), x, y, itemSize, itemSize,
                            u, v + uvBlockSize, u + uvBlockSize, v);
                }
            }
            if (stack.amount>1){
                bitmapFont.draw(spriteBatch, stack.amount+"", x+itemSize, y+itemSize/4f);
            }
            if (stack.item.canBeDepleted() && stack.item.getMaxDamage() > 0) {
                float durabilityFraction = 1.0f - ((float) stack.damage / (float) stack.item.getMaxDamage());
                durabilityFraction = Math.clamp(durabilityFraction, 0.0f, 1.0f);
                float barWidth = itemSize - 8f;
                float barHeight = 3f;
                float barX = x + 4f;
                float barY = y + 2f;

                spriteBatch.setColor(0f, 0f, 0f, 0.9f);
                spriteBatch.draw(durabilityPixel, barX, barY, barWidth, barHeight);

                float green = durabilityFraction;
                float red = 1.0f - durabilityFraction;
                spriteBatch.setColor(red, green, 0f, 1f);
                spriteBatch.draw(durabilityPixel, barX + 1f, barY + 1f, (barWidth - 2f) * durabilityFraction, 1f);
                spriteBatch.setColor(Color.WHITE);

            }
        }
    }

    // draw a selector overlay
    float selectionX = Gdx.graphics.getWidth() / 2f - (hotbar.getWidth() / 2f) * scale + (((Minecraft.getInstance().getPlayer().slotSelected - 1) * 1.8f) * itemSize);

    spriteBatch.draw(hotbarSelection, selectionX, 0,
            hotbarSelection.getWidth() * scale, hotbarSelection.getHeight() * scale);

    spriteBatch.end();
}

    @Override
    public void destroy() {
        super.destroy();
        bitmapFont.dispose();
        if (durabilityPixel != null) {
            durabilityPixel.dispose();
        }
        ;
    }
}
