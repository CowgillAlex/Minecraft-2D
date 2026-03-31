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
import dev.alexco.minecraft.world.entity.spawn.SpawnRulesManager;

import java.util.Random;

public class MoreWorldOptionsScreen extends RenderableLifecycle {
    private BitmapFont font;
    private Texture buttonNormal;
    private Texture buttonHighlighted;
    private Texture buttonDisabled;
    private Texture textFieldNormal;
    private Texture textFieldHighlighted;

    private GridLayout grid;
    private TextField seedField;
    private TextField randomTickSpeedField;
    private TextField passiveSpawnIntervalField;
    private TextField hostileSpawnIntervalField;
    private TextField passiveMobCapField;
    private TextField hostileMobCapField;
    private Button passiveSpawningButton;
    private Button hostileSpawningButton;
    private Button generateStructuresButton;
    private Button worldTypeButton;
    private Button startInvButton;
    private Button doneButton;
    private Button cancelButton;

    private long seed;
    private int randomTickSpeed = 3;
    private boolean passiveMobSpawningEnabled = true;
    private boolean hostileMobSpawningEnabled = true;
    private boolean starterInventoryEnabled = true;
    private int passiveSpawnIntervalTicks = 120;
    private int hostileSpawnIntervalTicks = 8;
    private int passiveMobCap = 6;
    private int hostileMobCap = 18;

    public MoreWorldOptionsScreen() {
    }

    @Override
    public void create() {
        super.create("More World Options", Color.DARK_GRAY);

        font = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        font.getData().markupEnabled = true;

        buttonNormal = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button.png");
        buttonHighlighted = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_highlighted.png");
        buttonDisabled = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_disabled.png");
        textFieldNormal = Minecraft.getInstance().textureManager.get("textures/gui/sprites/text_field.png");
        textFieldHighlighted = Minecraft.getInstance().textureManager.get("textures/gui/sprites/text_field_highlighted.png");

        grid = new GridLayout(2, 200, 20, 20, 20, 180, 2.0f);

        seedField = new TextField(textFieldNormal, textFieldHighlighted, 0, 200, 40, 2f, TextField.Alignment.LEFT, 0);
        seedField.setLabel("Seed");
        seedField.setTooltip("Leave empty for a random seed");
        seedField.setMaxLength(64);
        grid.addTextField(seedField);

        randomTickSpeedField = new TextField(textFieldNormal, textFieldHighlighted, 0, 200, 40, 2f, TextField.Alignment.LEFT, 0);
        randomTickSpeedField.setLabel("Random Tick Speed");
        randomTickSpeedField.setTooltip("Random block updates per chunk tick");
        randomTickSpeedField.setNumericOnly(true);
        randomTickSpeedField.setText("3");
        randomTickSpeedField.setMaxLength(3);
        grid.addTextField(randomTickSpeedField);

        passiveSpawningButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "", this::togglePassiveSpawning, 2f, Button.Alignment.LEFT, 0);
        grid.addButton(passiveSpawningButton);

        hostileSpawningButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "", this::toggleHostileSpawning, 2f, Button.Alignment.LEFT, 0);
        grid.addButton(hostileSpawningButton);

        passiveSpawnIntervalField = new TextField(textFieldNormal, textFieldHighlighted, 0, 200, 40, 2f, TextField.Alignment.LEFT, 0);
        passiveSpawnIntervalField.setLabel("Passive Spawn Interval");
        passiveSpawnIntervalField.setTooltip("Ticks between passive spawn attempts");
        passiveSpawnIntervalField.setNumericOnly(true);
        passiveSpawnIntervalField.setMaxLength(4);
        grid.addTextField(passiveSpawnIntervalField);

        hostileSpawnIntervalField = new TextField(textFieldNormal, textFieldHighlighted, 0, 200, 40, 2f, TextField.Alignment.LEFT, 0);
        hostileSpawnIntervalField.setLabel("Hostile Spawn Interval");
        hostileSpawnIntervalField.setTooltip("Ticks between hostile spawn attempts");
        hostileSpawnIntervalField.setNumericOnly(true);
        hostileSpawnIntervalField.setMaxLength(4);
        grid.addTextField(hostileSpawnIntervalField);

        passiveMobCapField = new TextField(textFieldNormal, textFieldHighlighted, 0, 200, 40, 2f, TextField.Alignment.LEFT, 0);
        passiveMobCapField.setLabel("Passive Mob Cap");
        passiveMobCapField.setTooltip("Local passive mob cap around the player");
        passiveMobCapField.setNumericOnly(true);
        passiveMobCapField.setMaxLength(4);
        grid.addTextField(passiveMobCapField);

        hostileMobCapField = new TextField(textFieldNormal, textFieldHighlighted, 0, 200, 40, 2f, TextField.Alignment.LEFT, 0);
        hostileMobCapField.setLabel("Hostile Mob Cap");
        hostileMobCapField.setTooltip("Local natural hostile mob cap around the player");
        hostileMobCapField.setNumericOnly(true);
        hostileMobCapField.setMaxLength(4);
        grid.addTextField(hostileMobCapField);

        generateStructuresButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Generate Structures: OFF", () -> {}, 2f, Button.Alignment.LEFT, 0);
        generateStructuresButton.setEnabled(false);
        grid.addButton(generateStructuresButton);

        worldTypeButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "World Type: Default", () -> {}, 2f, Button.Alignment.LEFT, 0);
        worldTypeButton.setEnabled(false);
        grid.addButton(worldTypeButton);

        startInvButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Starter Inventory: ON", this::toggleStarterInventory, 2f, Button.Alignment.LEFT, 0);
        grid.addButton(startInvButton);

        doneButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Done", () -> {
                    parseOptions();
                    Minecraft.getInstance().transitionToWorldCreation();
                }, 2f, Button.Alignment.LEFT, 0);
        grid.addButton(doneButton);

        cancelButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                0, "Cancel", () -> {
                    Minecraft.getInstance().currentScreenState = ScreenState.WORLD_CREATION;
                }, 2f, Button.Alignment.LEFT, 0);
        grid.addButton(cancelButton);
    }

    public void reset() {
        SpawnRulesManager.SpawnConfig passiveDefaults = SpawnRulesManager.getPassiveConfig();
        SpawnRulesManager.SpawnConfig hostileDefaults = SpawnRulesManager.getHostileConfig();
        seedField.clear();
        randomTickSpeedField.setText("3");
        passiveMobSpawningEnabled = true;
        hostileMobSpawningEnabled = true;
        passiveSpawnIntervalTicks = passiveDefaults.getSpawnIntervalTicks();
        hostileSpawnIntervalTicks = hostileDefaults.getSpawnIntervalTicks();
        passiveMobCap = passiveDefaults.getSpawnCap();
        hostileMobCap = hostileDefaults.getSpawnCap();
        passiveSpawnIntervalField.setText(Integer.toString(passiveSpawnIntervalTicks));
        hostileSpawnIntervalField.setText(Integer.toString(hostileSpawnIntervalTicks));
        passiveMobCapField.setText(Integer.toString(passiveMobCap));
        hostileMobCapField.setText(Integer.toString(hostileMobCap));
        refreshSpawnToggleLabels();
        seed = new Random().nextLong();
        randomTickSpeed = 3;
    }

    /**
     * Parses and clamps all user-entered world options from the form controls.
     */
    private void parseOptions() {
        String seedText = seedField.getText().trim();
        if (seedText.isEmpty()) {
            seed = new Random().nextLong();
        } else {
            try {
                seed = Long.parseLong(seedText);
            } catch (NumberFormatException e) {
                seed = seedText.hashCode();
            }
        }

        String randomTickText = randomTickSpeedField.getText().trim();
        if (randomTickText.isEmpty()) {
            randomTickSpeed = 3;
        } else {
            try {
                randomTickSpeed = Math.max(0, Math.min(128, Integer.parseInt(randomTickText)));
            } catch (NumberFormatException e) {
                randomTickSpeed = 3;
            }
        }

        passiveSpawnIntervalTicks = parseIntInRange(passiveSpawnIntervalField.getText(), 1, 12000, passiveSpawnIntervalTicks);
        hostileSpawnIntervalTicks = parseIntInRange(hostileSpawnIntervalField.getText(), 1, 12000, hostileSpawnIntervalTicks);
        passiveMobCap = parseIntInRange(passiveMobCapField.getText(), 0, 512, passiveMobCap);
        hostileMobCap = parseIntInRange(hostileMobCapField.getText(), 0, 512, hostileMobCap);
    }

    public long getSeed() {
        return seed;
    }

    public int getRandomTickSpeed() {
        parseOptions();
        return randomTickSpeed;
    }

    public boolean isPassiveMobSpawningEnabled() {
        parseOptions();
        return passiveMobSpawningEnabled;
    }

    public boolean isHostileMobSpawningEnabled() {
        parseOptions();
        return hostileMobSpawningEnabled;
    }

    public int getPassiveSpawnIntervalTicks() {
        parseOptions();
        return passiveSpawnIntervalTicks;
    }

    public int getHostileSpawnIntervalTicks() {
        parseOptions();
        return hostileSpawnIntervalTicks;
    }

    public int getPassiveMobCap() {
        parseOptions();
        return passiveMobCap;
    }

    public int getHostileMobCap() {
        parseOptions();
        return hostileMobCap;
    }

    public boolean isStarterInventoryEnabled() {
        return starterInventoryEnabled;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        grid.updateTextFields(delta);

        spriteBatch.begin();

        renderDirtBackground();
        renderTitle();
        grid.render(spriteBatch, font);
        grid.renderLabels(spriteBatch, font);
        grid.renderTooltips(spriteBatch, font);

        spriteBatch.end();

        grid.update();
        handleInput();
    }

    /**
     * Draws a tiled dirt background behind the options controls.
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
     * Draws the more-world-options title centred near the top.
     */
    private void renderTitle() {
        GlyphLayout layout = new GlyphLayout(font, "More World Options");
        float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float y = Gdx.graphics.getHeight() - 50f;
        font.draw(spriteBatch, layout, x, y);
    }

    /**
     * Routes click/focus and keyboard input for options text fields.
     */
    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(0)) {
            for (TextField field : grid.getTextFields()) {
                field.handleClick();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Minecraft.getInstance().currentScreenState = ScreenState.WORLD_CREATION;
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

    private void togglePassiveSpawning() {
        passiveMobSpawningEnabled = !passiveMobSpawningEnabled;
        refreshSpawnToggleLabels();
    }

    private void toggleHostileSpawning() {
        hostileMobSpawningEnabled = !hostileMobSpawningEnabled;
        refreshSpawnToggleLabels();
    }

    private void toggleStarterInventory() {
        starterInventoryEnabled = !starterInventoryEnabled;
        refreshSpawnToggleLabels();
    }

    /**
     * Syncs dynamic toggle button labels with current option values.
     */
    private void refreshSpawnToggleLabels() {
        if (passiveSpawningButton != null) {
            passiveSpawningButton.setLabel("Passive Mobs: " + (passiveMobSpawningEnabled ? "ON" : "OFF"));
        }
        if (hostileSpawningButton != null) {
            hostileSpawningButton.setLabel("Hostile Mobs: " + (hostileMobSpawningEnabled ? "ON" : "OFF"));
        }
        if (startInvButton != null) {
            startInvButton.setLabel("Starter Inventory: " + (starterInventoryEnabled ? "ON" : "OFF"));
        }
    }

    /**
     * Parses an integer and clamps it to a configured range with fallback.
     */
    private int parseIntInRange(String text, int min, int max, int fallback) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            return fallback;
        }
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(trimmed)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
