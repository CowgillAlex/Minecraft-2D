package dev.alexco.minecraft.blaze2d.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.alexco.minecraft.world.entity.Pig;

public class PigModel implements MobModel<Pig> {

    private TextureRegion backLegL;
    private TextureRegion backLegR;
    private TextureRegion body;
    private TextureRegion frontLegL;
    private TextureRegion frontLegR;
    private TextureRegion head;

    @Override
    public String getTexturePath() {
        return "textures/entity/pig.png";
    }

    @Override
    public void loadTextures(TextureRegion texture) {
        backLegL = new TextureRegion(texture.getTexture(), 0, 20, 4, 12);
        backLegR = new TextureRegion(texture.getTexture(), 0, 20, 4, 12);
        body = new TextureRegion(texture.getTexture(), 40, 4, 12, 9);
        frontLegL = new TextureRegion(texture.getTexture(), 12, 20, 4, 11);
        frontLegR = new TextureRegion(texture.getTexture(), 12, 20, 4, 11);
        head = new TextureRegion(texture.getTexture(), 0, 6, 6, 8);
    }

    @Override
    public Class<Pig> getEntityClass() {
        return Pig.class;
    }

    /**
     * Renders pig body parts with baby scaling and mirrored walk animation.
     */
    @Override
    public void render(SpriteBatch batch, Pig pig, float scale, float baseX, float baseY, boolean flip, float swing) {
        float ageScale = pig.isBaby() ? 0.8f : 1.0f;
        float finalScale = scale * ageScale;

        float bodyW = 12 * finalScale;
        float bodyH = 10 * finalScale;
        float legW = 4 * finalScale;
        float legH = 14 * finalScale;
        float headW = 6 * finalScale;
        float headH = 8 * finalScale;

        float bodyX = baseX - bodyW / 2f;
        float bodyY = (baseY - 1f * finalScale)+2f ;
        batch.draw(body, bodyX, bodyY, bodyW / 2f, bodyH / 2f, bodyW, bodyH, flip ? 1f : -1f, -1f, 0f);

        drawPart(batch, backLegL, baseX - 5f * finalScale, baseY - 2f * finalScale, legW, legH, swing * 0.6f, flip);
        drawPart(batch, backLegR, baseX + 1f * finalScale, baseY - 2f * finalScale, legW, legH, -swing * 0.6f, flip);

        float headX = flip ? bodyX - (headW - 1f * finalScale) : bodyX + bodyW - 1f * finalScale;
        float headY = baseY + 3.5f * finalScale;
        drawPart(batch, head, headX, headY, headW, headH, 0f, flip);
    }

    /**
     * Draws one model part with optional mirroring and rotation.
     */
    private void drawPart(SpriteBatch batch, TextureRegion part, float x, float y, float w, float h, float rotation, boolean flipX) {
        batch.draw(part, x, y, w / 2f, h / 2f, w, h, flipX ? -1f : 1f, -1f, rotation);
    }
}
