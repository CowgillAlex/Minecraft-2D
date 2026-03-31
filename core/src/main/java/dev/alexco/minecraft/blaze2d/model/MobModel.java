package dev.alexco.minecraft.blaze2d.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import dev.alexco.minecraft.world.entity.Mob;

public interface MobModel<T extends Mob> {
    String getTexturePath();
    void loadTextures(TextureRegion texture);
    Class<T> getEntityClass();
    void render(SpriteBatch batch, T entity, float scale, float baseX, float baseY, boolean flip, float swing);
}
