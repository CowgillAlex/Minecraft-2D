package dev.alexco.minecraft.blaze2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.entity.Cow;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.entity.LivingEntity;

public class CowRenderer extends RenderableLifecycle {
    private Texture cowTexture;
    private TextureRegion leg;
    private TextureRegion leg_2;
    private TextureRegion head;
    private TextureRegion body;

    @Override
    public void create() {
        super.create("CowRenderer", Color.WHITE);
        Minecraft.getInstance().textureManager.forceLoadTexture("textures/entity/cow.png");
        cowTexture = Minecraft.getInstance().textureManager.get("textures/entity/cow.png");
        leg = new TextureRegion(cowTexture, 12, 20, 4, 11);
        leg_2 = new TextureRegion(cowTexture, 0, 20, 4, 12);
        head = new TextureRegion(cowTexture, 0, 6, 6, 8);
        body = new TextureRegion(cowTexture, 40, 4, 12, 9);
    }

    @Override
    public void render() {
        float blockSize = (float) Minecraft.getInstance().getWorld().worldData.blockSize;
        float camX = (float) Minecraft.getInstance().getWorld().worldData.cameraX;
        float camY = (float) Minecraft.getInstance().getWorld().worldData.cameraY;
        long ticks = Minecraft.getInstance().getTotalTicks();

        spriteBatch.begin();

        for (Entity entity : Minecraft.getInstance().getWorld().entities) {
            if (!(entity instanceof Cow cow)) continue;

            float brightness = RenderableEntity.getLightBrightness(entity);
            if (entity instanceof LivingEntity living && living.getHurtTicks() > 0) {
                spriteBatch.setColor(brightness, brightness * 0.35f, brightness * 0.35f, 1.0f);
            } else {
                spriteBatch.setColor(brightness, brightness, brightness, 1.0f);
            }

            float ageScale = cow.isBaby() ? 0.6f : 1.0f;
            float scale = (blockSize / 16f) * ageScale;
            float centerX = (float) (((entity.bb.x0 + entity.bb.x1) / 2.0) * blockSize - camX);
            float baseY = (float) (entity.bb.y0 * blockSize - camY);

            boolean flip = entity.xd < -0.01f;
            float swing = (float) (Math.sin(ticks * 0.25f) * (Math.abs(entity.xd) * 120f));
            float bodyW = 12 * scale;
            float bodyH = 9 * scale;
            float legW = 4 * scale;
            float legH = 11 * scale;
            float headW = 6 * scale;
            float headH = 8 * scale;

            float bodyX = centerX - bodyW / 2f;
            float bodyY = baseY + 3f * scale;
            drawPart(body, bodyX, bodyY, bodyW, bodyH, 0f, flip);

            drawPart(leg, centerX - 5f * scale, baseY - 1f * scale, legW, legH, swing * 0.35f, flip);
            drawPart(leg_2, centerX + 1f * scale, baseY - 1f * scale, legW, legH, -swing * 0.35f, flip);

            float headX = flip ? bodyX - (headW - 1f * scale) : bodyX + bodyW - 1f * scale;
            float headY = bodyY + 0.5f * scale;
            drawPart(head, headX, headY, headW, headH, 0f, flip);
        }

        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }

    /**
     * Draws a body part with optional mirroring and simple rotation animation.
     */
    private void drawPart(TextureRegion part, float x, float y, float w, float h, float rotation, boolean flipX) {
        spriteBatch.draw(part, x, y, w / 2f, h / 2f, w, h, flipX ? -1f : 1f, 1f, rotation);
    }
}
