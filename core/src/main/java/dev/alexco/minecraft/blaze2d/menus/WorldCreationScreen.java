package dev.alexco.minecraft.blaze2d.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.blaze2d.menu.GridLayout;
import dev.alexco.minecraft.blaze2d.menu.button.Button;
import dev.alexco.minecraft.blaze2d.menu.button.TextField;
import dev.alexco.minecraft.gui.ScreenState;
import dev.alexco.minecraft.world.serialisation.WorldSaveManager;

import java.util.Random;

public class WorldCreationScreen extends RenderableLifecycle {
    private BitmapFont font;
    private Texture buttonNormal;
    private Texture buttonHighlighted;
    private Texture buttonDisabled;
    private Texture textFieldNormal;
    private Texture textFieldHighlighted;

    private GridLayout grid;
    private TextField worldNameField;
    private Button gamemodeButton;
    private Button difficultyButton;
    private Button cheatsButton;
    private Button experimentsButton;
    private Button gameRulesButton;
    private Button moreOptionsButton;
    private Button createButton;
    private Button cancelButton;

    private String errorMessage = null;
    private float errorDisplayTime = 0;

    public WorldCreationScreen() {
    }

    @Override
    public void create() {
        super.create("World Creation", Color.DARK_GRAY);

        font = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        font.getData().markupEnabled = true;

        buttonNormal = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button.png");
        buttonHighlighted = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_highlighted.png");
        buttonDisabled = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_disabled.png");
        textFieldNormal = Minecraft.getInstance().textureManager.get("textures/gui/sprites/text_field.png");
        textFieldHighlighted = Minecraft.getInstance().textureManager.get("textures/gui/sprites/text_field_highlighted.png");

        grid = new GridLayout(2, 200, 20, 20, 20, 180, 2.0f);

        worldNameField = new TextField(textFieldNormal, textFieldHighlighted, 0, 200, 40, 2f, TextField.Alignment.LEFT, 0);
        worldNameField.setText("New World");
        worldNameField.setLabel("World Name");
        worldNameField.setTooltip("Enter a name for your world");
        worldNameField.setMaxLength(50);
        grid.addTextField(worldNameField);

        gamemodeButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Game mode: Survival", () -> {}, 2f, Button.Alignment.LEFT, 0);
        gamemodeButton.setEnabled(false);
        grid.addButton(gamemodeButton);

        difficultyButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Difficulty: Normal", () -> {}, 2f, Button.Alignment.LEFT, 0);
        difficultyButton.setEnabled(false);
        grid.addButton(difficultyButton);

        cheatsButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Commands: ON", () -> {}, 2f, Button.Alignment.LEFT, 0);
        cheatsButton.setEnabled(false);
        grid.addButton(cheatsButton);

        experimentsButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Experiments", () -> {}, 2f, Button.Alignment.LEFT, 0);
        experimentsButton.setEnabled(false);
        grid.addButton(experimentsButton);

        gameRulesButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Game rules", () -> {}, 2f, Button.Alignment.LEFT, 0);
        gameRulesButton.setEnabled(false);
        grid.addButton(gameRulesButton);

        moreOptionsButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "More World Options", () -> {
                    Minecraft.getInstance().transitionToMoreWorldOptions();
                }, 2f, Button.Alignment.LEFT, 0);
        grid.addButton(moreOptionsButton);

        createButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Create World", () -> {
                    createWorld();
                }, 2f, Button.Alignment.LEFT, 0);
        grid.addButton(createButton);

        cancelButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Cancel", () -> {
                    Minecraft.getInstance().transitionToWorldSelection();
                }, 2f, Button.Alignment.LEFT, 0);
        grid.addButton(cancelButton);
    }

    public void reset() {
        worldNameField.setText("New World");
        errorMessage = null;
        errorDisplayTime = 0;
    }

    /**
     * Validates world name/options and starts world creation with resolved seed.
     */
    private void createWorld() {
        String worldName = worldNameField.getText().trim();
        if (worldName.isEmpty()) {
            showError("World name cannot be empty!");
            return;
        }

        if (worldName.length() > 50) {
            showError("World name is too long!");
            return;
        }

        String folderName = WorldSaveManager.sanitizeFolderName(worldName);
        if (WorldSaveManager.worldFolderExists(folderName)) {
            showError("A world with this name already exists!");
            return;
        }

        long seed;
        MoreWorldOptionsScreen moreOptions = Minecraft.getInstance().getMoreWorldOptionsScreen();
        if (moreOptions != null) {
            seed = moreOptions.getSeed();
        } else {
            seed = new Random().nextLong();
        }

        Minecraft.getInstance().createWorld(worldName, seed);
    }

    /**
     * Displays a temporary validation error on screen.
     */
    private void showError(String message) {
        errorMessage = message;
        errorDisplayTime = 3f;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        grid.updateTextFields(delta);

        if (errorDisplayTime > 0) {
            errorDisplayTime -= delta;
            if (errorDisplayTime <= 0) {
                errorMessage = null;
            }
        }

        spriteBatch.begin();

        renderDirtBackground();
        renderTitle();
        grid.render(spriteBatch, font);
        grid.renderLabels(spriteBatch, font);
        grid.renderTooltips(spriteBatch, font);

        if (errorMessage != null) {
            renderError();
        }

        spriteBatch.end();

        grid.update();
        handleInput();
    }

    /**
     * Draws the tiled dirt background used behind world creation controls.
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
     * Draws the create-world title centred at the top of the screen.
     */
    private void renderTitle() {
        GlyphLayout layout = new GlyphLayout(font, "Create New World");
        float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float y = Gdx.graphics.getHeight() - 50f;
        font.draw(spriteBatch, layout, x, y);
    }

    /**
     * Draws the current error message in red near the lower section of the screen.
     */
    private void renderError() {
        font.setColor(Color.RED);
        GlyphLayout layout = new GlyphLayout(font, errorMessage);
        float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float y = 150;
        font.draw(spriteBatch, layout, x, y);
        font.setColor(Color.WHITE);
    }

    /**
     * Handles focus changes, keyboard shortcuts, and text field input.
     */
    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(0)) {
            for (TextField field : grid.getTextFields()) {
                field.handleClick();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            var textFields = grid.getTextFields();
            for (int i = 0; i < textFields.size(); i++) {
                if (textFields.get(i).isFocused()) {
                    textFields.get(i).setFocused(false);
                    int nextIndex = (i + 1) % textFields.size();
                    textFields.get(nextIndex).setFocused(true);
                    break;
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            createWorld();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Minecraft.getInstance().transitionToWorldSelection();
        }

        for (TextField field : grid.getTextFields()) {
            field.handleKeyboardInput();
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
