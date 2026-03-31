package dev.alexco.minecraft.blaze2d.menu.button;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import dev.alexco.minecraft.blaze2d.menu.GridElement;

public class Button implements GridElement {
    public enum Alignment { LEFT, CENTRE, RIGHT }

    private Texture normal;
    private Texture highlighted;
    private Texture disabled;
    public String label;
    private final Runnable onClick;
    private float scale;
    private Alignment alignment;
    private float offsetX;
    private float relativeY;

    private boolean enabled = true;
    private Rectangle bounds = new Rectangle();

    private Float overrideX = null;
    private Float overrideY = null;
    private Float overrideWidth = null;
    private Float overrideHeight = null;

    public Button(Texture normal, Texture highlighted, Texture disabled, float relativeY, String label, Runnable onClick,
                  float scale, Alignment alignment, float offsetX) {
        this.normal = normal;
        this.highlighted = highlighted;
        this.disabled = disabled;
        this.label = label;
        this.onClick = onClick;
        this.scale = scale;
        this.alignment = alignment;
        this.offsetX = offsetX;
        this.relativeY = relativeY;
    }

    public Button(Texture normal, Texture highlighted, Texture disabled, float relativeY, String label, Runnable onClick) {
        this(normal, highlighted, disabled, relativeY, label, onClick, 1f, Alignment.CENTRE, 0f);
    }

    public void setTextures(Texture normal, Texture highlighted, Texture disabled) {
        this.normal = normal;
        this.highlighted = highlighted;
        this.disabled = disabled;
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
     * Recomputes button bounds from explicit overrides or alignment-based placement.
     */
    private void updateBounds() {
        float scaledWidth = overrideWidth != null ? overrideWidth : normal.getWidth() * scale;
        float scaledHeight = overrideHeight != null ? overrideHeight : normal.getHeight() * scale;

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

    private static GlyphLayout layout = new GlyphLayout();

    /**
     * Draws the button with hover/disabled state and centred label text.
     */
    public void render(SpriteBatch batch, BitmapFont font) {
        updateBounds();

        boolean isHovered = bounds.contains(new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));

        Texture toDraw;
        if (!enabled) {
            toDraw = disabled;
        } else if (isHovered) {
            toDraw = highlighted;
        } else {
            toDraw = normal;
        }

        batch.draw(toDraw, bounds.x, bounds.y, bounds.width, bounds.height);

        layout.setText(font, label);

        float textX = bounds.x + (bounds.width - layout.width) / 2f;
        float textY = bounds.y + (bounds.height + layout.height) / 2f;

        font.draw(batch, layout, textX, textY);
    }

    @Override
    public void render() {
    }

    /**
     * Handles click activation when the left mouse button is pressed over this button.
     */
    @Override
    public void update() {
        updateBounds();
        if (!enabled) return;

        if (Gdx.input.isButtonJustPressed(0)) {
            Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
            if (bounds.contains(mouse)) {
                onClick.run();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHovered() {
        if (!enabled) return false;
        updateBounds();
        return bounds.contains(new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));
    }

    public void setLabel(String label) {
        this.label = label;
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
