package dev.alexco.minecraft.blaze2d.menu.button;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.menu.GridElement;

public class TextField implements GridElement {
    public enum Alignment { LEFT, CENTRE, RIGHT }

    private Texture normal;
    private Texture highlighted;
    private float scale;
    private Alignment alignment;
    private float offsetX;
    private float relativeY;
    private float width;
    private float height;

    private StringBuilder text = new StringBuilder();
    private String tooltip = null;
    private String label = null;
    private boolean focused = false;

    private Rectangle bounds = new Rectangle();

    private Float overrideX = null;
    private Float overrideY = null;
    private Float overrideWidth = null;
    private Float overrideHeight = null;

    private float cursorBlinkTime = 0;
    private static final float CURSOR_BLINK_SPEED = 0.5f;

    private boolean numericOnly = false;
    private int maxLength = Integer.MAX_VALUE;

    public TextField(Texture normal, Texture highlighted, float relativeY, float width, float height,
                     float scale, Alignment alignment, float offsetX) {
        this.normal = normal;
        this.highlighted = highlighted;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.alignment = alignment;
        this.offsetX = offsetX;
        this.relativeY = relativeY;
    }

    public TextField(Texture normal, Texture highlighted, float relativeY, float width, float height) {
        this(normal, highlighted, relativeY, width, height, 1f, Alignment.CENTRE, 0f);
    }

    public void setTextures(Texture normal, Texture highlighted) {
        this.normal = normal;
        this.highlighted = highlighted;
    }

    @Override
    public void setPosition(float x, float y) {
        this.overrideX = x;
        this.overrideY = y;
    }

    @Override
    public void setSize(float width, float height) {
        this.overrideWidth = width;
        this.overrideHeight = height;
    }

    @Override
    public float getX() {
        updateBounds();
        return bounds.x;
    }

    @Override
    public float getY() {
        updateBounds();
        return bounds.y;
    }

    @Override
    public float getWidth() {
        updateBounds();
        return bounds.width;
    }

    @Override
    public float getHeight() {
        updateBounds();
        return bounds.height;
    }

    /**
     * Recomputes text-field bounds from explicit overrides or alignment-based placement.
     */
    private void updateBounds() {
        float scaledWidth = overrideWidth != null ? overrideWidth : width * scale;
        float scaledHeight = overrideHeight != null ? overrideHeight : height * scale;

        float x, y;

        if (overrideX != null && overrideY != null) {
            x = overrideX;
            y = overrideY;
        } else {
            switch (alignment) {
                case LEFT:
                    x = 20 + offsetX;
                    break;
                case RIGHT:
                    x = Gdx.graphics.getWidth() - scaledWidth - 20 + offsetX;
                    break;
                case CENTRE:
                default:
                    x = (Gdx.graphics.getWidth() - scaledWidth) / 2f + offsetX;
                    break;
            }
            y = relativeY;
        }

        bounds.set(x, y, scaledWidth, scaledHeight);
    }

    /**
     * Draws the text field, clipping visible text and animating a blinking cursor when focused.
     */
    public void render(SpriteBatch batch, BitmapFont font) {
        updateBounds();

        Texture toDraw = focused ? highlighted : normal;
        batch.draw(toDraw, bounds.x, bounds.y, bounds.width, bounds.height);

        String displayText = text.toString();
        if (focused && cursorBlinkTime < CURSOR_BLINK_SPEED) {
            displayText = text + "|";
        }

        float maxTextWidth = bounds.width - 20;
        GlyphLayout layout = new GlyphLayout(font, displayText);
        while (layout.width > maxTextWidth && displayText.length() > 1) {
            displayText = displayText.substring(1);
            layout.setText(font, displayText);
        }

        float textX = bounds.x + 10;
        float textY = bounds.y + (bounds.height + layout.height) / 2f;
        font.draw(batch, layout, textX, textY);
    }

    public void renderLabel(SpriteBatch batch, BitmapFont font) {
        if (label == null) return;
        updateBounds();
        font.draw(batch, label, bounds.x, bounds.y + bounds.height + 24);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void renderTooltip(SpriteBatch batch, BitmapFont font) {
        if (tooltip == null || !isHovered()) return;

        Minecraft.getInstance().tooltipRenderer.renderAtMouse(tooltip);
    }

    /**
     * Advances cursor blink state while this text field is focused.
     */
    public void update(float delta) {
        if (focused) {
            cursorBlinkTime += delta;
            if (cursorBlinkTime > CURSOR_BLINK_SPEED * 2) {
                cursorBlinkTime = 0;
            }
        }
    }

    @Override
    public void render() {
    }

    @Override
    public void update() {
    }

    /**
     * Sets focus based on mouse click position and resets cursor blink on focus.
     */
    public boolean handleClick() {
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        if (bounds.contains(mouse)) {
            focused = true;
            cursorBlinkTime = 0;
            return true;
        } else {
            focused = false;
            return false;
        }
    }

    /**
     * Applies keypress input to the text field respecting numeric-only and max-length constraints.
     */
    public void handleKeyboardInput() {
        if (!focused) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && text.length() > 0) {
            text.deleteCharAt(text.length() - 1);
        }

        for (int key = Input.Keys.A; key <= Input.Keys.Z; key++) {
            if (Gdx.input.isKeyJustPressed(key)) {
                if (text.length() >= maxLength) return;
                char c = (char) ('a' + (key - Input.Keys.A));
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                    c = Character.toUpperCase(c);
                }
                text.append(c);
            }
        }

        for (int key = Input.Keys.NUM_0; key <= Input.Keys.NUM_9; key++) {
            if (Gdx.input.isKeyJustPressed(key)) {
                if (text.length() >= maxLength) return;
                text.append((char) ('0' + (key - Input.Keys.NUM_0)));
            }
        }

        for (int key = Input.Keys.NUMPAD_0; key <= Input.Keys.NUMPAD_9; key++) {
            if (Gdx.input.isKeyJustPressed(key)) {
                if (text.length() >= maxLength) return;
                text.append((char) ('0' + (key - Input.Keys.NUMPAD_0)));
            }
        }

        if (!numericOnly && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (text.length() < maxLength) {
                text.append(' ');
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            if (text.length() < maxLength) {
                text.append('-');
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PLUS)) {
            if (text.length() < maxLength) {
                text.append('+');
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT)) {
            if (text.length() < maxLength) {
                text.append('.');
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS) &&
            (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
            if (text.length() < maxLength) {
                text.append('_');
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isHovered() {
        updateBounds();
        return bounds.contains(new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        if (focused) {
            cursorBlinkTime = 0;
        }
    }

    public String getText() {
        return text.toString();
    }

    public void setText(String text) {
        this.text = new StringBuilder(text);
    }

    public void clear() {
        this.text = new StringBuilder();
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setNumericOnly(boolean numericOnly) {
        this.numericOnly = numericOnly;
    }

    public boolean isNumericOnly() {
        return numericOnly;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public void setRelativeY(float relativeY) {
        this.relativeY = relativeY;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
