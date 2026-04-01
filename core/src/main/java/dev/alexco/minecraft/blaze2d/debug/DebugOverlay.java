package dev.alexco.minecraft.blaze2d.debug;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;

import java.util.ArrayList;
import java.util.List;

public class DebugOverlay extends RenderableLifecycle {

 public static class Section {
    public String name;
    public Color color;
    public float lastTimeMs;
    public float rollingAvgMs;
    public int callCount;
    public boolean expanded = false;
    public final List<Section> children = new ArrayList<>();
    private static final float ALPHA = 1/(float)SharedConstants.FPS_CAP;

    // Timing helpers
    private long startTimeNs = 0;

    public Section(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    /** Begin timing this section */
    public void start() {
        startTimeNs = System.nanoTime();
    }

    /** Stop timing and record elapsed time in ms */
    public void stop() {
        long elapsedNs = System.nanoTime() - startTimeNs;
        float elapsedMs = elapsedNs / 1_000_000f;
        setTime(elapsedMs);
    }

    public void setTime(float timeMs) {
        lastTimeMs = timeMs;
        rollingAvgMs = rollingAvgMs * (1 - ALPHA) + timeMs * ALPHA;
        callCount++;
    }

    public Section getOrCreateChild(String name, Color color) {
        for (Section c : children)
            if (c.name.equals(name)) return c;
        Section s = new Section(name, color);
        children.add(s);
        return s;
    }
}


    private final List<Section> sections = new ArrayList<>();
    private final BitmapFont font = new BitmapFont();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private boolean visible = true;

    @Override
    public void create() {
        super.create("Debug Renderer", Color.GOLD);
        font.getData().setScale(1.0f);
    }

    public Section addSection(String name, Color color) {
        Section s = new Section(name, color);
        sections.add(s);
        return s;
    }

    public void toggleVisible() {
        visible = !visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void render() {
        if (!visible) return;

        float currentY =  15;

        for (Section s : sections) {
            currentY = drawSection(s, 10, currentY, 0);
        }
    }

    /**
     * Recursively draws one timing section line and all visible child timings.
     */
    private float drawSection(Section s, float x, float y, int indent) {
        // Skip sections that have never been called
        if (s.callCount == 0) {
            for (Section c : s.children) {
                y = drawSection(c, x, y, indent + 1);
            }
            return y;
        }

        spriteBatch.begin();
        font.setColor(s.color);
        font.draw(spriteBatch, String.format("%s%s: %.2f ms (avg %.2f) [%.2fFPS]", "  ".repeat(indent),
                s.name, s.lastTimeMs, s.rollingAvgMs, SharedConstants.FPS_CAP/s.rollingAvgMs), x, y);
        spriteBatch.end();

        y += 15;


            for (Section c : s.children) {
                y = drawSection(c, x, y, indent + 1);
            }


        return y;
    }
    /**
     * Draws a top-level timing pie chart using section rolling averages.
     */
public void drawPie(Section root, float radius) {
    float centerX = Gdx.graphics.getWidth() - radius - 10;
    float centerY = radius + 10;

    // Compute total of root's children using rolling averages
    float totalRootTime = 0f;
    for (Section s : root.children) totalRootTime += s.rollingAvgMs;

    float startAngle = 0f;
    for (Section s : root.children) {
        drawPieSectionRecursive(s, centerX, centerY, radius, startAngle, totalRootTime);
        startAngle += 360f * (s.rollingAvgMs / totalRootTime);
    }
}

/**
 * Draws one recursive pie slice and nested child slices scaled to parent time.
 */
private void drawPieSectionRecursive(Section s, float centerX, float centerY, float radius, float startAngle, float parentTotal) {
    // Use rollingAvgMs for smoothness
    float sliceAngle = 360f * (s.rollingAvgMs / parentTotal);
    if (sliceAngle < 1f) return;

    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(s.color);
    shapeRenderer.arc(centerX, centerY, radius, startAngle, sliceAngle);
    shapeRenderer.end();

    if (!s.children.isEmpty()) {
        float childStartAngle = startAngle;
        float innerRadius = radius * 0.6f; // inner layer scaling
        for (Section child : s.children) {
            float childAngle = sliceAngle * (child.rollingAvgMs / s.rollingAvgMs);
            drawPieSectionRecursive(child, centerX, centerY, innerRadius, childStartAngle, s.rollingAvgMs);
            childStartAngle += childAngle;
        }
    }
}


    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        shapeRenderer.setProjectionMatrix(camera.combined);
        font.getRegion().getTexture().setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
    }

    @Override
    public void destroy() {
        super.destroy();
        font.dispose();
        shapeRenderer.dispose();
    }
}
