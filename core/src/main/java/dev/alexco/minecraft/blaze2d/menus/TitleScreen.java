package dev.alexco.minecraft.blaze2d.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Matrix4;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.Version;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.blaze2d.menu.button.Button;
import dev.alexco.minecraft.blaze2d.menu.button.Button.Alignment;
import dev.alexco.minecraft.util.Formatter;

public class TitleScreen extends RenderableLifecycle {
    private BitmapFont titleFont;
    private final List<Button> buttons = new ArrayList<>();
    private Button playButton;
    private Button quitButton;

    private Texture buttonNormal;
    private Texture buttonHighlighted;
    private Texture buttonDisabled;

    private List<String> splashes = new ArrayList<>();
    private String currentSplash = "";
    private final Random random = new Random();

    private float logoX;
    private float logoY;
    private float logoWidth;
    private float logoHeight;
    private float elapsedTime = 0f;

    @Override
    public void create() {
        super.create("Title Screen", Color.DARK_GRAY);

        titleFont = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        titleFont.getData().markupEnabled = true;

        loadSplashes();

        buttonNormal = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button.png");
        buttonHighlighted = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_highlighted.png");
        buttonDisabled = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_disabled.png");

        int screenHeight = Gdx.graphics.getHeight();

        playButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                screenHeight / 2f, "Play", () -> {
                    Minecraft.getInstance().startGame();

                }, 2f, Alignment.CENTRE, 0f);

        quitButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                screenHeight / 2f - 60, "Quit", () -> {
                    Gdx.app.exit();
                }, 2f, Alignment.CENTRE, 0f);

        buttons.add(playButton);
        buttons.add(quitButton);
    }

    /**
     * Loads splash lines from disk and picks an initial random entry.
     */
    private void loadSplashes() {
        try {
            String text = Gdx.files.internal("splashes.txt").readString();
            String[] lines = text.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    splashes.add(trimmed);
                }
            }
            pickRandomSplash();
        } catch (Exception e) {
            currentSplash = "Missing splashes!";
        }
    }

    /**
     * Chooses one random splash string for title-screen display.
     */
    private void pickRandomSplash() {
        if (splashes.isEmpty()) {
            currentSplash = "No splashes!";
            return;
        }
        currentSplash = splashes.get(random.nextInt(splashes.size()));
    }

    @Override
    public void render() {
        elapsedTime += Gdx.graphics.getDeltaTime();

        spriteBatch.begin();

        renderDirtBackground();

        renderTitle();

        renderSplash();

        for (Button button : buttons) {
            button.render(spriteBatch, titleFont);
        }

        renderVersionInfo();

        spriteBatch.end();

        if (Gdx.input.isKeyJustPressed(Keys.R)) {//we do not sync to any world clock here
            pickRandomSplash();
        }

        for (Button button : buttons) {
            button.update();
        }
    }

    /**
     * Draws the tiled dirt backdrop used behind title-screen widgets.
     */
    private void renderDirtBackground() {
        int bgX = (Gdx.graphics.getWidth() / 48) + 1;
        int bgY = (Gdx.graphics.getHeight() / 48) + 1;
        spriteBatch.setColor(Color.DARK_GRAY);
        Texture dirtTexture = Minecraft.getInstance().textureManager.get("textures/block/dirt.png");
        for (int x = 0; x < bgX; x++) {
            for (int y = 0; y < bgY; y++) {
                spriteBatch.draw(dirtTexture, x * 48, y * 48, 48, 48);
            }
        }
        spriteBatch.setColor(Color.WHITE);
    }

    /**
     * Draws the game logo and edition stamp centred near the top of the screen.
     */
    private void renderTitle() {
        Texture logo = Minecraft.getInstance().textureManager.get("textures/gui/title/minecraft.png");
        Texture edition = Minecraft.getInstance().textureManager.get("textures/gui/title/nea_edition.png");

        float logoScale = 0.75f;
        logoWidth = logo.getWidth() * logoScale;
        logoHeight = logo.getHeight() * logoScale;

        float editionScale = 0.45f;
        float editionWidth = edition.getWidth() * editionScale;
        float editionHeight = edition.getHeight() * editionScale;

        logoX = (Gdx.graphics.getWidth() - logoWidth) / 2f;
        logoY = Gdx.graphics.getHeight() - logoHeight - 50;

        spriteBatch.draw(logo, logoX, logoY, logoWidth, logoHeight);

        float editionX = (Gdx.graphics.getWidth() - editionWidth) / 2f;
        float editionY = logoY + editionHeight-50;

        spriteBatch.draw(edition, editionX, editionY, editionWidth, editionHeight);
    }

    /**
     * Draws the animated rotating splash text with pulsating scale.
     */
    private void renderSplash() {
        float pulseScale = 1.0f + 0.1f * (float) Math.sin(elapsedTime * 5f);

        GlyphLayout splashLayout = new GlyphLayout(titleFont, Formatter.formatYellow(currentSplash));

        float splashX = logoX + logoWidth - splashLayout.width / 2f;
        float splashY = logoY + logoHeight / 2f;

        float originX = splashX + splashLayout.width / 2f;
        float originY = splashY - splashLayout.height / 2f;

        Matrix4 oldTransform = spriteBatch.getTransformMatrix().cpy();
        Matrix4 transform = new Matrix4();
        transform.setToTranslation(originX, originY, 0);
        transform.scale(pulseScale, pulseScale, 1);
        transform.rotate(0, 0, 1, 22.5f);
        transform.translate(-originX, -originY, 0);
        spriteBatch.setTransformMatrix(transform);

        titleFont.draw(spriteBatch, Formatter.formatYellow(currentSplash), splashX, splashY);

        spriteBatch.setTransformMatrix(oldTransform);
    }

    /**
     * Draws static version information in the bottom-left corner.
     */
    private void renderVersionInfo() {
        String versionText = "Paper Minecraft " + Version.VERSION_STRING;

        float x = 10;
        float y = 30;

        titleFont.setColor(Color.LIGHT_GRAY);
        titleFont.draw(spriteBatch, versionText, x, y);
        titleFont.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        if (playButton != null) {
            playButton.setRelativeY(height / 2f);
        }
        if (quitButton != null) {
            quitButton.setRelativeY(height / 2f - 60);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (titleFont != null) {
            titleFont.dispose();
        }
    }
}
