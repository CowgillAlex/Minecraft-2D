package dev.alexco.minecraft.blaze2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.model.MobModel;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.entity.Entity;
import dev.alexco.minecraft.world.entity.LivingEntity;
import dev.alexco.minecraft.world.entity.Mob;

public class MobRenderer<T extends Mob> extends RenderableLifecycle {
    private final MobModel<T> model;
    private final String rendererName;
    private final Color debugColor;
    private Texture texture;

    public MobRenderer(MobModel<T> model, String rendererName, Color debugColor) {
        this.model = model;
        this.rendererName = rendererName;
        this.debugColor = debugColor;
    }

    @Override
    public void create() {
        super.create(rendererName, debugColor);
        Minecraft.getInstance().textureManager.forceLoadTexture(model.getTexturePath());
        texture = Minecraft.getInstance().textureManager.get(model.getTexturePath());
        model.loadTextures(new TextureRegion(texture));
    }

    /**
     * Renders all mobs of the model type with walk animation, flip, and hurt tint.
     */
    @Override
    public void render() {
        float blockSize = (float) Minecraft.getInstance().getWorld().worldData.blockSize;
        float camX = (float) Minecraft.getInstance().getWorld().worldData.cameraX;
        float camY = (float) Minecraft.getInstance().getWorld().worldData.cameraY;
        float scale = blockSize / 16f;

        spriteBatch.begin();

        for (Entity entity : Minecraft.getInstance().getWorld().entities) {
            if (entity.bb == null) {
                Logger.ERROR("Entity bb null for %s at %f, %f", entity.type, entity.x, entity.y);
                continue;
            }
            if (!model.getEntityClass().isInstance(entity)) continue;

            @SuppressWarnings("unchecked")
            T mob = (T) entity;

            float brightness = RenderableEntity.getLightBrightness(entity);
            if (entity instanceof LivingEntity living && living.getHurtTicks() > 0) {
                spriteBatch.setColor(brightness, brightness * 0.35f, brightness * 0.35f, 1.0f);
            } else {
                spriteBatch.setColor(brightness, brightness, brightness, 1.0f);
            }
 if (Gdx.input.isKeyPressed(Keys.G)){
                spriteBatch.setColor(1, 1, 1, 1);
        }
            float baseX = (float) (((entity.bb.x0 + entity.bb.x1) / 2.0) * blockSize - camX);
            float baseY = (float) (entity.bb.y0 * blockSize - camY);
            boolean flip = entity.xd < -0.01f;
            float time = (System.currentTimeMillis() % 1000) / 1000f;
            float swing = (float) (Math.sin(time * Math.PI * 2) * (Math.abs(entity.xd) * 90f));

            model.render(spriteBatch, mob, scale, baseX, baseY, flip, swing);
        }

        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }
}
