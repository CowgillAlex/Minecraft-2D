package dev.alexco.minecraft.blaze2d.tooltip;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;

public class TooltipRenderer extends RenderableLifecycle {
    private static NinePatch backgroundPatch;
    private static NinePatch framePatch;
    private static final int PADDING = 16;
    private static final int BORDER_SIZE = 16;
    private Texture background;
    private Texture frame;
    private BitmapFont font;

    @Override
    public void create() {
        super.create("TooltipRenderer", Color.TAN);
        Minecraft.getInstance().textureManager.forceLoadTexture("textures/gui/tooltip/background.png");
        Minecraft.getInstance().textureManager.forceLoadTexture("textures/gui/tooltip/frame.png");

        background = Minecraft.getInstance().textureManager.get("textures/gui/tooltip/background.png");
        frame = Minecraft.getInstance().textureManager.get("textures/gui/tooltip/frame.png");

        backgroundPatch = new NinePatch(background, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
        framePatch = new NinePatch(frame, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
        font = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
    }

    /**
     * Renders a single-line tooltip at screen coordinates with viewport clamping.
     */
    public void render(String text, int x, int y) {
        if (backgroundPatch == null || framePatch == null) {
            throw new IllegalStateException("Background or foreground patches are null. Did you initialise?");
        }

        GlyphLayout layout = new GlyphLayout(font, text);
        float textWidth = layout.width;
        float textHeight = layout.height;
        int tooltipWidth = (int) (textWidth + PADDING * 2);
        int tooltipHeight = (int) (textHeight + PADDING * 2);

        // Ensure tooltip doesn't go off-screen
        int finalX = x;
        int finalY = y;
        if (finalX + tooltipWidth > Gdx.graphics.getWidth()) {
            finalX = Gdx.graphics.getWidth() - tooltipWidth - 8;
        }
        if (finalY - tooltipHeight < 0) {
            finalY = tooltipHeight + 8;
        }
        if (finalX < 0) {
            finalX = 8;
        }
        if (finalY > Gdx.graphics.getHeight()) {
            finalY = Gdx.graphics.getHeight() - 8;
        }

        spriteBatch.begin();

        backgroundPatch.draw(spriteBatch,
                finalX, finalY - tooltipHeight,
                tooltipWidth, tooltipHeight);
        framePatch.draw(spriteBatch,
                finalX, finalY - tooltipHeight,
                tooltipWidth, tooltipHeight);

        // Draw text centred horizontally, PADDING from the top of the tooltip box
        float textX = finalX + (tooltipWidth - textWidth) / 2f;
        float textY = finalY - PADDING;

        font.draw(spriteBatch, layout, textX, textY);
        spriteBatch.end();
    }

    /**
     * Renders a single-line tooltip offset from the current mouse position.
     */
    public void renderAtMouse(String text) {
        int mouseX = Gdx.input.getX() + 16;
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY() + 16;
        render(text, mouseX, mouseY);
    }

    /**
     * Renders a multi-line tooltip with centred lines and viewport clamping.
     */
    public void renderMultiLine(String text, int x, int y) {
        if (backgroundPatch == null || framePatch == null) {
            throw new IllegalStateException("Background or foreground patches are null. Did you initialise?");
        }

        String[] lines = text.split("\n");
        float maxWidth = 0;
        float totalHeight = 0;
        GlyphLayout[] layouts = new GlyphLayout[lines.length];

        // Calculate dimensions for all lines
        for (int i = 0; i < lines.length; i++) {
            layouts[i] = new GlyphLayout(font, lines[i]);
            maxWidth = Math.max(maxWidth, layouts[i].width);
            totalHeight += layouts[i].height;
            if (i < lines.length - 1) {
                totalHeight += 4; // Line spacing
            }
        }

        int tooltipWidth = (int) (maxWidth + PADDING * 2);
        int tooltipHeight = (int) (totalHeight + PADDING * 2);

        // Screen boundary checks
        int finalX = x;
        int finalY = y;
        if (finalX + tooltipWidth > Gdx.graphics.getWidth()) {
            finalX = Gdx.graphics.getWidth() - tooltipWidth - 8;
        }
        if (finalY - tooltipHeight < 0) {
            finalY = tooltipHeight + 8;
        }
        if (finalX < 0) {
            finalX = 8;
        }
        if (finalY > Gdx.graphics.getHeight()) {
            finalY = Gdx.graphics.getHeight() - 8;
        }

        spriteBatch.begin();

        backgroundPatch.draw(spriteBatch,
                finalX, finalY - tooltipHeight,
                tooltipWidth, tooltipHeight);
        framePatch.draw(spriteBatch,
                finalX, finalY - tooltipHeight,
                tooltipWidth, tooltipHeight);

        // Draw each line â€” draw first, then advance Y downward
        float currentY = finalY - PADDING;
        for (int i = 0; i < layouts.length; i++) {
            float textX = finalX + (tooltipWidth - layouts[i].width) / 2f;
            font.draw(spriteBatch, layouts[i], textX, currentY);
            currentY -= layouts[i].height;
            if (i < layouts.length - 1) {
                currentY -= 4; // Line spacing
            }
        }

        spriteBatch.end();
    }

    /**
     * Renders a multi-line tooltip offset from the current mouse position.
     */
    public void renderMultilineAtMouse(String text) {
        int mouseX = Gdx.input.getX() + 16;
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY() + 16;
        renderMultiLine(text, mouseX, mouseY);
    }
}
