package dev.alexco.minecraft.blaze2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.entity.LivingEntity;
import dev.alexco.minecraft.world.entity.Zombie;

public class ZombieRenderer extends RenderableLifecycle {
    private Texture zombieTexture;
    private TextureRegion head, body, rightArm, rightLeg;

    @Override
    public void create() {
        super.create("ZombieRenderer", Color.FOREST);
        Minecraft.getInstance().textureManager.forceLoadTexture("textures/entity/zombie.png");
        zombieTexture = Minecraft.getInstance().textureManager.get("textures/entity/zombie.png");
        head = new TextureRegion(zombieTexture, 16, 8, 8, 8);
        body = new TextureRegion(zombieTexture, 28, 20, 4, 12);
        rightArm = new TextureRegion(zombieTexture, 48, 20, 4, 12);
        rightLeg = new TextureRegion(zombieTexture, 8, 20, 4, 12);

    }

    /**
     * Renders all zombie entities with mirrored limb swing animation and hurt tint.
     */
    @Override
    public void render() {
        float blockSize = (float) Minecraft.getInstance().getWorld().worldData.blockSize;
        float camX = (float) Minecraft.getInstance().getWorld().worldData.cameraX;
        float camY = (float) Minecraft.getInstance().getWorld().worldData.cameraY;
        spriteBatch.begin();

        for (Entity entity : Minecraft.getInstance().getWorld().entities) {
            if (!(entity instanceof Zombie zombie)) {
                continue;
            }

            float brightness = RenderableEntity.getLightBrightness(entity);
            if (entity instanceof LivingEntity living && living.getHurtTicks() > 0) {
                spriteBatch.setColor(brightness, brightness * 0.35f, brightness * 0.35f, 1.0f);
            } else {
                spriteBatch.setColor(brightness, brightness, brightness, 1.0f);
            }

            float scale = (blockSize / 16f) * (zombie.isBaby() ? 0.55f : 1.0f);

            float baseX = (float) (entity.x * Minecraft.getInstance().getWorld().worldData.blockSize
                    - Minecraft.getInstance().getWorld().worldData.cameraX);
            float baseY = (float) ((entity.y - 0.5) * Minecraft.getInstance().getWorld().worldData.blockSize
                    - Minecraft.getInstance().getWorld().worldData.cameraY);
            boolean flip = entity.xd < -0.01f;
            float time = (System.currentTimeMillis() % 1000) / 1000f;

            float swing = (float) ((float) Math.sin(time * Math.PI * 2) * ((Math.abs(entity.xd) * 90f)));
            float headW = 8 * scale, headH = 8 * scale;
            float bodyW = 4 * scale, bodyH = 12 * scale;
            float limbW = 4 * scale, limbH = 12 * scale;

            drawPart(rightLeg, baseX - limbW / 2f + 1 * scale, baseY, limbW, limbH, swing, flip);
            drawPart(rightLeg, baseX - limbW / 2f - 1 * scale, baseY, limbW, limbH, -swing, flip);

            drawPart(rightArm, baseX - limbW / 2f + 2 * scale, baseY + limbH + 1 * scale, limbW, limbH, -swing, flip);
            drawPart(rightArm, baseX - limbW / 2f - 2 * scale, baseY + limbH + 1 * scale, limbW, limbH, swing, flip);

            drawPart(body, baseX - bodyW / 2f, baseY + limbH, bodyW, bodyH, 0f, flip);
            drawPart(head, baseX - headW / 2f, baseY + limbH + bodyH, headW, headH, 0f, flip);
        }

        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }

    /**
     * Draws one zombie body part with optional mirroring and rotation.
     */
    private void drawPart(TextureRegion part, float x, float y, float w, float h, float rotation, boolean flipX) {
        spriteBatch.draw(part, x, y, w / 2f, h / 2f, w, h, flipX ? -1f : 1f, 1f, rotation);
    }
}
