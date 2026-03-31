package dev.alexco.minecraft.blaze2d.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.gui.ScreenState;

public class SavingWorldScreen extends RenderableLifecycle {
    private BitmapFont font;
    private Texture dirtTexture;

    public SavingWorldScreen() {
    }

    @Override
    public void create() {
        super.create("Saving World", Color.DARK_GRAY);
        font = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        font.getData().markupEnabled = true;
        dirtTexture = Minecraft.getInstance().textureManager.get("textures/block/dirt.png");
    }

    @Override
    public void render() {
        spriteBatch.begin();
        renderDirtBackground();
        renderText();
        spriteBatch.end();
    }

    /**
     * Draws the tiled dirt backdrop for the save-progress screen.
     */
    private void renderDirtBackground() {
        int bgX = (Gdx.graphics.getWidth() / 48) + 1;
        int bgY = (Gdx.graphics.getHeight() / 48) + 1;
        spriteBatch.setColor(Color.DARK_GRAY);
        for (int x = 0; x < bgX; x++) {
            for (int y = 0; y < bgY; y++) {
                spriteBatch.draw(dirtTexture, x * 48, y * 48, 48, 48);
            }
        }
        spriteBatch.setColor(Color.WHITE);
    }

    /**
     * Draws the centred "Saving World..." status message.
     */
    private void renderText() {
        GlyphLayout layout = new GlyphLayout(font, "Saving World...");
        float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float y = Gdx.graphics.getHeight() / 2f;
        font.draw(spriteBatch, layout, x, y);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (font != null) {
            font.dispose();
        }
    }
}
