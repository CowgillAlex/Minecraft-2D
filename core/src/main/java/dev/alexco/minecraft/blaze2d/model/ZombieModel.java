package dev.alexco.minecraft.blaze2d.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import dev.alexco.minecraft.world.entity.Zombie;

public class ZombieModel implements MobModel<Zombie> {
    private TextureRegion head;
    private TextureRegion body;
    private TextureRegion rightArm;
    private TextureRegion rightLeg;

    @Override
    public String getTexturePath() {
        return "textures/entity/zombie.png";
    }

    @Override
    public void loadTextures(TextureRegion texture) {
        head = new TextureRegion(texture.getTexture(), 16, 8, 8, 8);
        body = new TextureRegion(texture.getTexture(), 28, 20, 4, 12);
        rightArm = new TextureRegion(texture.getTexture(), 48, 20, 4, 12);
        rightLeg = new TextureRegion(texture.getTexture(), 8, 20, 4, 12);
    }

    @Override
    public Class<Zombie> getEntityClass() {
        return Zombie.class;
    }

    /**
     * Renders zombie limbs, torso and head with baby scaling and swing animation.
     */
    @Override
    public void render(SpriteBatch batch, Zombie zombie, float scale, float baseX, float baseY, boolean flip, float swing) {
        float ageScale = zombie.isBaby() ? 0.55f : 1.0f;
        float finalScale = scale * ageScale;

        float headW = 8 * finalScale, headH = 8 * finalScale;
        float bodyW = 4 * finalScale, bodyH = 12 * finalScale;
        float limbW = 4 * finalScale, limbH = 12 * finalScale;

        batch.draw(rightLeg, baseX - limbW / 2f, baseY, limbW / 2f, limbH, limbW, limbH, flip ? -1f : 1f, 1f, flip ? swing : -swing);
        batch.draw(rightArm, baseX - limbW / 2f, baseY + limbH + 1 * finalScale, limbW / 2f, limbH, limbW, limbH, flip ? -1f : 1f, 1f, flip ? -swing : swing);
        batch.draw(body, baseX - bodyW / 2f, baseY + limbH, bodyW / 2f, bodyH / 2f, bodyW, bodyH, flip ? -1f : 1f, 1f, 0f);
        batch.draw(rightLeg, baseX - limbW / 2f, baseY, limbW / 2f, limbH, limbW, limbH, flip ? -1f : 1f, 1f, flip ? -swing : swing);
        batch.draw(rightArm, baseX - limbW / 2f, baseY + limbH + 1 * finalScale, limbW / 2f, limbH, limbW, limbH, flip ? -1f : 1f, 1f, flip ? swing : -swing);
        batch.draw(head, baseX - headW / 2f, baseY + limbH + bodyH, headW / 2f, headH / 2f, headW, headH, flip ? -1f : 1f, 1f, 0f);
    }
}
