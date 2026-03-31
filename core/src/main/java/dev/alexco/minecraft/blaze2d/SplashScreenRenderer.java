package dev.alexco.minecraft.blaze2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import dev.alexco.minecraft.Minecraft;

public class SplashScreenRenderer extends RenderableLifecycle {
    private BitmapFont font;
    private static final Color TEXT_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color SHADOW_COLOR = new Color(0f, 0f, 0f, 0.75f);
    private static final GlyphLayout layout = new GlyphLayout();

    @Override
    public void create() {
        super.create("Splash Screen", Color.GRAY);
        font = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        font.getData().markupEnabled = true;
        font.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        font.setColor(TEXT_COLOR);
    }

    /**
     * Draws the splash background and the latest loading step text.
     */
    @Override
    public void render() {
        spriteBatch.begin();
        spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures/splash.png"), 0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.end();

        Minecraft mc = Minecraft.getInstance();
        if (mc.loadingState != null) {
            String step = mc.loadingState.getStep();
            String detail = mc.loadingState.getDetail();

            String text = detail.isEmpty() ? step : step + "\n" + detail;

            layout.setText(font, text);
            float textX = (Gdx.graphics.getWidth() - layout.width) / 2;
            float textY = 80;

            spriteBatch.begin();
            font.setColor(SHADOW_COLOR);
            font.draw(spriteBatch, text, textX + 2, textY - 2);
            font.setColor(TEXT_COLOR);
            font.draw(spriteBatch, text, textX, textY);
            spriteBatch.end();
        }
    }

    /**
     * Releases splash font resources.
     */
    @Override
    public void destroy() {
        super.destroy();
        if (font != null) {
            font.dispose();
        }
    }
}
