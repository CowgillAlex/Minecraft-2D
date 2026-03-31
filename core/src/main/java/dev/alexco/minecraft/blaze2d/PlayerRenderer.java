package dev.alexco.minecraft.blaze2d;

import static dev.alexco.minecraft.SharedConstants.MAX_LIGHT_LEVEL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.level.chunk.Chunk;

public class PlayerRenderer extends RenderableLifecycle {
    private TextureRegion head, body, rightArm, rightLeg;
    private TextureRegion headFlipped;
    private Texture playerTexture;
    private boolean facingLeft = false;

    @Override
    public void create() {
        Minecraft.getInstance().textureManager.forceLoadTexture("steve.png");
        this.playerTexture = Minecraft.getInstance().textureManager.get("steve.png");
        head = new TextureRegion(playerTexture, 16, 8, 8, 8);
        body = new TextureRegion(playerTexture, 28, 20, 4, 12);
        rightArm = new TextureRegion(playerTexture, 48, 20, 4, 12);
        rightLeg = new TextureRegion(playerTexture, 8, 20, 4, 12);

        headFlipped = new TextureRegion(head);
        headFlipped.flip(true, false);
        super.create();
    }


    /**
     * Renders the in-world player with light tint and current animation frame.
     */
    @Override
    public void render() {
        Entity entity = Minecraft.getInstance().getPlayer();
        spriteBatch.begin();
        //needs try catch because light might be not loaded
        float brightness = 0;
        try {
            brightness = RenderableEntity.getLightBrightness(entity);
            spriteBatch.setColor(brightness, brightness, brightness, 1.0f);
        } catch (Exception e) {
            spriteBatch.setColor(1, 1, 1, 1);
        }

        float heightScale = (float) (entity.bbHeight / 2.0f);
        float baseScale = (float) (Minecraft.getInstance().getWorld().worldData.blockSize / 16f);
        float scale = baseScale * heightScale;

        float baseX = (float) (entity.x * Minecraft.getInstance().getWorld().worldData.blockSize
                - Minecraft.getInstance().getWorld().worldData.cameraX);
        float baseY = (float) ((entity.y - 0.5) * Minecraft.getInstance().getWorld().worldData.blockSize
                - Minecraft.getInstance().getWorld().worldData.cameraY);

        renderPlayerAt(baseX, baseY, scale, true);
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }

    /**
     * Renders the player model at an arbitrary screen position (used by menus).
     */
    public void renderAtScreenPosition(float screenX, float screenY, float scale) {
        spriteBatch.begin();
        spriteBatch.setColor(Color.WHITE);
        renderPlayerAt(screenX, screenY, scale, false);
        spriteBatch.end();
    }

    /**
     * Draws all player body parts and rotates the head toward the mouse cursor.
     */
    private void renderPlayerAt(float baseX, float baseY, float scale, boolean inWorld) {
        Entity entity = Minecraft.getInstance().getPlayer();

        float headW = 8 * scale, headH = 8 * scale;
        float bodyW = 4 * scale, bodyH = 12 * scale;
        float limbW = 4 * scale, limbH = 12 * scale;

        float time = (System.currentTimeMillis() % 1000) / 1000f;
        float swing = (float) ((float) Math.sin(time * Math.PI * 2) * ((Math.abs(entity.xd) * 90f)));

        float armW = limbW;
        float armH = limbH;

        TextureRegion currentBody = body;
        TextureRegion currentArm = rightArm;
        TextureRegion currentLeg = rightLeg;

        boolean flipX = facingLeft;
        float legOffset = 0;
        spriteBatch.draw(
                currentLeg,
                baseX - limbW / 2f + legOffset, baseY,
                limbW / 2f, limbH,
                limbW, limbH,
                flipX ? -1f : 1f, 1f,
                flipX ? swing : -swing);

        spriteBatch.draw(
                currentArm,
                baseX - armW / 2f,
                baseY + limbH + 1 * scale,
                armW / 2f, armH,
                armW, armH,
                flipX ? -1f : 1f, 1f,
                flipX ? swing : -swing);

        spriteBatch.draw(
                currentBody,
                baseX - bodyW / 2f, baseY + limbH,
                bodyW / 2f, bodyH / 2f,
                bodyW, bodyH,
                flipX ? -1f : 1f, 1f,
                0f);

        spriteBatch.draw(
                currentLeg,
                baseX - limbW / 2f - legOffset, baseY,
                limbW / 2f, limbH,
                limbW, limbH,
                flipX ? -1f : 1f, 1f,
                flipX ? -swing : swing);

        spriteBatch.draw(
                currentArm,
                baseX - armW / 2f,
                baseY + limbH + 1 * scale,
                armW / 2f, armH,
                armW, armH,
                flipX ? -1f : 1f, 1f,
                flipX ? -swing : swing);

        float mouseScreenX = Gdx.input.getX();
        float mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float headScreenX = baseX;
        float headScreenY = baseY + limbH + bodyH + headH / 2f;

        // math to turn screen space into angle
        float dx = mouseScreenX - headScreenX;
        float dy = mouseScreenY - headScreenY;
        float angleRad = (float) Math.atan2(dy, dx);
        float angleDeg = (float) Math.toDegrees(angleRad);

        boolean flipHead = false;

        // flip when upside down
        if (angleDeg > 90) {
            angleDeg = 180 - angleDeg;
            flipHead = true;
        } else if (angleDeg < -90) {
            angleDeg = -180 - angleDeg;
            flipHead = true;
        }
        if (flipHead) {
            angleDeg = -angleDeg;
            facingLeft = true;
        } else {
            facingLeft = false;
        }

        spriteBatch.draw(
                headFlipped,
                baseX - headW / 2f, baseY + limbH + bodyH,
                headW / 2f, headH / 2f,
                headW, headH,
                flipHead ? -1f : 1f, 1f,
                angleDeg);
    }

}
