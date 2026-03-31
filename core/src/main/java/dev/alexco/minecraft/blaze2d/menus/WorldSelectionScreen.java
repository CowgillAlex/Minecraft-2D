package dev.alexco.minecraft.blaze2d.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.blaze2d.menu.button.Button;
import dev.alexco.minecraft.gui.ScreenState;
import dev.alexco.minecraft.util.Formatter;
import dev.alexco.minecraft.world.serialisation.WorldSaveManager;
import dev.alexco.minecraft.world.serialisation.WorldSaveManager.WorldInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorldSelectionScreen extends RenderableLifecycle {
    private BitmapFont font;
    private final List<Button> buttons = new ArrayList<>();
    private Button playButton;
    private Button createButton;
    private Button deleteButton;
    private Button backButton;

    private Texture buttonNormal;
    private Texture buttonHighlighted;
    private Texture buttonDisabled;
    private Texture placeholderIcon;

    private List<WorldInfo> worlds = new ArrayList<>();
    private int selectedWorldIndex = -1;
    private float scrollOffset = 0;
    private static final float WORLD_ENTRY_HEIGHT = 90f;
    private static final float WORLD_LIST_WIDTH = 960f;
    private static float worldListStartY;
    private static float worldListHeight;

    private boolean showDeleteConfirm = false;
    private Button confirmDeleteButton;
    private Button cancelDeleteButton;

    private Rectangle worldListBounds = new Rectangle();

    private long lastClickTime = 0;
    private int lastClickedIndex = -1;
    private static final long DOUBLE_CLICK_THRESHOLD = 500;

    public WorldSelectionScreen() {
    }

    @Override
    public void create() {
        super.create("World Selection", Color.DARK_GRAY);

        font = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        font.getData().markupEnabled = true;

        buttonNormal = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button.png");
        buttonHighlighted = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_highlighted.png");
        buttonDisabled = Minecraft.getInstance().textureManager.get("textures/gui/sprites/button_disabled.png");
        placeholderIcon = Minecraft.getInstance().textureManager.get("textures/block/grass_block.png");

        playButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                80, "Play Selected World", () -> {
                    if (selectedWorldIndex >= 0 && selectedWorldIndex < worlds.size()) {
                        WorldInfo world = worlds.get(selectedWorldIndex);
                        Minecraft.getInstance().loadWorld(world.folderName, world.displayName);
                    }
                }, 2f, Button.Alignment.CENTRE, -200f);
        playButton.setEnabled(false);

        createButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                80, "Create New World", () -> {
                    Minecraft.getInstance().transitionToWorldCreation();
                }, 2f, Button.Alignment.CENTRE, 200f);

        deleteButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                40, "Delete selected world", () -> {
                    if (selectedWorldIndex >= 0 && selectedWorldIndex < worlds.size()) {
                        showDeleteConfirm = true;
                    }
                }, 2f, Button.Alignment.CENTRE, -200f);
        deleteButton.setEnabled(false);

        backButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                40, "Back", () -> {
                    Minecraft.getInstance().currentScreenState = ScreenState.TITLE;
                }, 2f, Button.Alignment.CENTRE, 200f);

        int screenHeight = Gdx.graphics.getHeight();
        confirmDeleteButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                screenHeight / 2f - 30, "Delete", () -> {
                    if (selectedWorldIndex >= 0 && selectedWorldIndex < worlds.size()) {
                        WorldSaveManager.deleteWorld(worlds.get(selectedWorldIndex).folderName);
                        refreshWorlds();
                        selectedWorldIndex = -1;
                        playButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                    }
                    showDeleteConfirm = false;
                }, 1.5f, Button.Alignment.CENTRE, -60f);

        cancelDeleteButton = new Button(buttonNormal, buttonHighlighted, buttonDisabled,
                screenHeight / 2f - 30, "Cancel", () -> {
                    showDeleteConfirm = false;
                }, 1.5f, Button.Alignment.CENTRE, 60f);

        buttons.add(playButton);
        buttons.add(createButton);
        buttons.add(deleteButton);
        buttons.add(backButton);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        refreshWorlds();
    }

    /**
     * Reloads the world list from disk and resets list scroll position.
     */
    public void refreshWorlds() {
        worlds = WorldSaveManager.getWorlds();
        scrollOffset = 0;
    }

    @Override
    public void render() {
        spriteBatch.begin();

        renderDirtBackground();

        renderTitle();

        renderWorldList();

        for (Button button : buttons) {
            button.render(spriteBatch, font);
        }

        if (showDeleteConfirm) {
            renderDeleteConfirm();
        }

        spriteBatch.end();

        for (Button button : buttons) {
            button.update();
        }

        if (showDeleteConfirm) {
            confirmDeleteButton.update();
            cancelDeleteButton.update();
        }

        handleInput();
    }

    /**
     * Draws a tiled dirt background behind the world selection UI.
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
     * Draws the screen title centred near the top of the window.
     */
    private void renderTitle() {
        GlyphLayout layout = new GlyphLayout(font, "Select World");
        float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float y = Gdx.graphics.getHeight() - 40f;
        font.draw(spriteBatch, layout, x, y);
    }

    /**
     * Draws the scrollable world list panel and all currently visible world entries.
     */
    private void renderWorldList() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        float listX = (screenWidth - WORLD_LIST_WIDTH) / 2f;
        float currentY = worldListStartY;

        worldListBounds.set(listX, worldListStartY - worldListHeight, WORLD_LIST_WIDTH, worldListHeight);

        spriteBatch.setColor(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        spriteBatch.draw(buttonDisabled, listX - 5, worldListStartY - worldListHeight - 5, WORLD_LIST_WIDTH + 10, worldListHeight + 10);
        spriteBatch.setColor(Color.WHITE);

        if (worlds.isEmpty()) {
            GlyphLayout layout = new GlyphLayout(font, "No worlds found. Create a new world!");
            float x = (screenWidth - layout.width) / 2f;
            float y = worldListStartY - worldListHeight / 2f;
            font.draw(spriteBatch, layout, x, y);
            return;
        }

        float contentHeight = worlds.size() * WORLD_ENTRY_HEIGHT;
        float maxScroll = Math.max(0, contentHeight - worldListHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        for (int i = 0; i < worlds.size(); i++) {
            float entryY = currentY - WORLD_ENTRY_HEIGHT - (i * WORLD_ENTRY_HEIGHT) + scrollOffset;

            if (entryY + WORLD_ENTRY_HEIGHT < worldListStartY - worldListHeight || entryY > worldListStartY) {
                continue;
            }

            WorldInfo world = worlds.get(i);
            boolean isSelected = i == selectedWorldIndex;

            renderWorldEntry(world, listX, entryY, isSelected);
        }
    }

    /**
     * Draws one world row including icon, metadata, hover state, and selection tint.
     */
    private void renderWorldEntry(WorldInfo world, float x, float y, boolean selected) {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        Color bgColor = selected ? new Color(0.3f, 0.3f, 0.7f, 0.8f) : new Color(0.2f, 0.2f, 0.2f, 0.8f);

        if (mouseX >= x && mouseX <= x + WORLD_LIST_WIDTH &&
                mouseY >= y && mouseY <= y + WORLD_ENTRY_HEIGHT && !selected) {
            bgColor = new Color(0.4f, 0.4f, 0.4f, 0.8f);
        }

        spriteBatch.setColor(bgColor);
        spriteBatch.draw(buttonNormal, x, y, WORLD_LIST_WIDTH, WORLD_ENTRY_HEIGHT);
        spriteBatch.setColor(Color.WHITE);

        spriteBatch.draw(placeholderIcon, x + 10, y + 10, 70, 70);

        float textX = x + 90;
        float textY = y + 65;

        font.setColor(selected ? Color.YELLOW : Color.WHITE);
        String displayName = world.displayName;
        if (displayName.length() > 30) {
            displayName = displayName.substring(0, 27) + "...";
        }
        font.draw(spriteBatch, displayName, textX, textY);

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.9f);
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(world.lastPlayed));
        font.draw(spriteBatch, "Last played: " + dateStr + "  |  Version: " + world.lastVersion, textX, textY - 20);
        font.draw(spriteBatch, "Time played: " + Formatter.formatTicksToTime(world.ticks) + "  |  Size: " + formatFileSize(world.fileSize), textX, textY - 40);
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }

    /**
     * Formats byte counts into human-readable units for list display.
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Draws the modal confirmation prompt used before deleting a world.
     */
    private void renderDeleteConfirm() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        float boxWidth = 400;
        float boxHeight = 150;
        float boxX = (screenWidth - boxWidth) / 2f;
        float boxY = (screenHeight - boxHeight) / 2f;

        spriteBatch.setColor(new Color(0, 0, 0, 0.8f));
        spriteBatch.draw(buttonNormal, boxX - 5, boxY - 5, boxWidth + 10, boxHeight + 10);
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.draw(buttonDisabled, boxX, boxY, boxWidth, boxHeight);

        GlyphLayout layout = new GlyphLayout(font, "Delete world?");
        font.draw(spriteBatch, layout, (screenWidth - layout.width) / 2f, boxY + boxHeight - 30);

        if (selectedWorldIndex >= 0 && selectedWorldIndex < worlds.size()) {
            String worldName = worlds.get(selectedWorldIndex).displayName;
            font.setColor(Color.RED);
            GlyphLayout nameLayout = new GlyphLayout(font, "\"" + worldName + "\"");
            font.draw(spriteBatch, nameLayout, (screenWidth - nameLayout.width) / 2f, boxY + boxHeight - 55);
            font.setColor(Color.WHITE);
        }

        confirmDeleteButton.render(spriteBatch, font);
        cancelDeleteButton.render(spriteBatch, font);
    }

    /**
     * Handles list clicks, double-click world launch, scroll input, and keyboard shortcuts.
     */
    private void handleInput() {
        if (showDeleteConfirm) return;

        if (Gdx.input.isButtonJustPressed(0)) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            float listX = (Gdx.graphics.getWidth() - WORLD_LIST_WIDTH) / 2f;

            if (mouseX >= listX && mouseX <= listX + WORLD_LIST_WIDTH &&
                    mouseY >= worldListStartY - worldListHeight && mouseY <= worldListStartY) {

                int visibleIndex = (int) ((worldListStartY - mouseY) / WORLD_ENTRY_HEIGHT);
                int clickedIndex = visibleIndex + (int) (scrollOffset / WORLD_ENTRY_HEIGHT);

                if (clickedIndex >= 0 && clickedIndex < worlds.size()) {
                    long currentTime = System.currentTimeMillis();

                    if (clickedIndex == lastClickedIndex && currentTime - lastClickTime < DOUBLE_CLICK_THRESHOLD) {
                        WorldInfo world = worlds.get(clickedIndex);
                        Minecraft.getInstance().loadWorld(world.folderName, world.displayName);
                        return;
                    }

                    selectedWorldIndex = clickedIndex;
                    playButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    lastClickedIndex = clickedIndex;
                    lastClickTime = currentTime;
                }
            }
        }

        float scrollAmount = dev.alexco.minecraft.input.InputHandler.getScrollDeltaY() * WORLD_ENTRY_HEIGHT;
        if (scrollAmount != 0) {
            scrollOffset -= scrollAmount;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Minecraft.getInstance().currentScreenState = ScreenState.TITLE;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && selectedWorldIndex >= 0) {
            WorldInfo world = worlds.get(selectedWorldIndex);
            Minecraft.getInstance().loadWorld(world.folderName, world.displayName);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        worldListStartY = height - 120;
        worldListHeight = height - 360;

        if (confirmDeleteButton != null) {
            confirmDeleteButton.setRelativeY(height / 2f - 30);
        }
        if (cancelDeleteButton != null) {
            cancelDeleteButton.setRelativeY(height / 2f - 30);
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
