package dev.alexco.minecraft.blaze2d.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.blaze2d.menu.button.Button;
import dev.alexco.minecraft.gui.ScreenState;

import java.util.ArrayList;
import java.util.List;

public class PauseScreen extends RenderableLifecycle {
    private BitmapFont font;
    private final List<Button> buttons = new ArrayList<>();
    private Button resumeButton;
    private Button titleButton;

    private Texture buttonNormal;
    private Texture buttonHighlighted;
    private Texture buttonDisabled;

    public PauseScreen() {
    }

    @Override
    public void create() {
        super.create("Pause Screen", new Color(0, 0, 0, 0.5f));

        font = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        font.getData().markupEnabled = true;

        buttonNormal = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button.png");
        buttonHighlighted = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_highlighted.png");
        buttonDisabled = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_disabled.png");

        int screenHeight = Gdx.graphics.getHeight();

        resumeButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                screenHeight / 2f + 20, "Resume", () -> {
                    Minecraft.getInstance().currentScreenState = ScreenState.NONE;
                }, 2f, Button.Alignment.CENTRE, 0f);

        titleButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                screenHeight / 2f - 40, "Save & Quit to Title", () -> {
                    Minecraft.getInstance().saveAndQuitToWorldSelection();
                }, 2f, Button.Alignment.CENTRE, 0f);

        buttons.add(resumeButton);
        buttons.add(titleButton);
                dirtTexture = Minecraft.getInstance().textureManager.get("textures/block/dirt.png");

    }
        private Texture dirtTexture;

      /**
       * Draws the dimmed tiled dirt backdrop behind pause controls.
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
     * Renders pause title and actions, then handles resume hotkey and button updates.
     */
    @Override
    public void render() {
        spriteBatch.begin();
        renderDirtBackground();

        spriteBatch.setColor(Color.WHITE);

        GlyphLayout layout = new GlyphLayout(font, "Game Paused");
        float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float y = Gdx.graphics.getHeight() / 2f + 100;
        font.draw(spriteBatch, layout, x, y);

        for (Button button : buttons) {
            button.render(spriteBatch, font);
        }

        spriteBatch.end();

        for (Button button : buttons) {
            button.update();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Minecraft.getInstance().currentScreenState = ScreenState.NONE;
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        if (resumeButton != null) {
            resumeButton.setRelativeY(height / 2f + 20);
        }
        if (titleButton != null) {
            titleButton.setRelativeY(height / 2f - 40);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (font != null) {
            font.dispose();
        }
    }
}
