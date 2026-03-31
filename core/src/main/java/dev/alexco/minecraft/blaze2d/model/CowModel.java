package dev.alexco.minecraft.blaze2d.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import dev.alexco.minecraft.world.entity.Cow;

public class CowModel implements MobModel<Cow> {
    private TextureRegion leg;
    private TextureRegion leg2;
    private TextureRegion head;
    private TextureRegion body;

    @Override
    public String getTexturePath() {
        return "textures/entity/cow.png";
    }

    @Override
    public void loadTextures(TextureRegion texture) {
        leg = new TextureRegion(texture.getTexture(), 12, 20, 4, 11);
        leg2 = new TextureRegion(texture.getTexture(), 0, 20, 4, 12);
        head = new TextureRegion(texture.getTexture(), 0, 6, 6, 8);
        body = new TextureRegion(texture.getTexture(), 40, 4, 12, 9);
    }

    @Override
    public Class<Cow> getEntityClass() {
        return Cow.class;
    }

    /**
     * Renders cow body parts with baby scaling and leg swing animation.
     */
    @Override
    public void render(SpriteBatch batch, Cow cow, float scale, float baseX, float baseY, boolean flip, float swing) {
        float ageScale = cow.isBaby() ? 0.6f : 1.0f;
        float finalScale = scale * ageScale;

        float bodyW = 12 * finalScale;
        float bodyH = 9 * finalScale;
        float legW = 4 * finalScale;
        float legH = 11 * finalScale;
        float headW = 6 * finalScale;
        float headH = 8 * finalScale;

        float bodyX = baseX - bodyW / 2f;
        float bodyY = baseY + 3f * finalScale;
        drawPart(batch, body, bodyX, bodyY, bodyW, bodyH, 0f, flip);

        drawPart(batch, leg, baseX - 5f * finalScale, baseY - 1f * finalScale, legW, legH, swing * 0.35f, flip);
        drawPart(batch, leg2, baseX + 1f * finalScale, baseY - 1f * finalScale, legW, legH, -swing * 0.35f, flip);

        float headX = flip ? bodyX - (headW - 1f * finalScale) : bodyX + bodyW - 1f * finalScale;
        float headY = bodyY + 0.5f * finalScale;
        drawPart(batch, head, headX, headY, headW, headH, 0f, flip);
    }

    /**
     * Draws one model part with optional mirroring and rotation.
     */
    private void drawPart(SpriteBatch batch, TextureRegion part, float x, float y, float w, float h, float rotation, boolean flipX) {
        batch.draw(part, x, y, w / 2f, h / 2f, w, h, flipX ? -1f : 1f, 1f, rotation);
    }
}
