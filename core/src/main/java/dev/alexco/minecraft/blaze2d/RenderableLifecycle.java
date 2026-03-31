package dev.alexco.minecraft.blaze2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.debug.DebugOverlay.Section;
import dev.alexco.minecraft.util.Lifecycle;

public abstract class RenderableLifecycle implements Lifecycle {
    protected SpriteBatch spriteBatch;
    protected OrthographicCamera camera;
    public Section renderSection;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderSection = Minecraft.getInstance().root.getOrCreateChild(this.getClass().getName(), Color.BLACK);
    }

    public void create(String name, Color color) {
        spriteBatch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        renderSection = Minecraft.getInstance().root.getOrCreateChild(name, color);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        spriteBatch.setProjectionMatrix(camera.combined);
    }

    /**
     * Wraps render execution with debug timing section start/stop hooks.
     */
    public void draw() {
        if (renderSection != null) {
            renderSection.start();
        }
        try {
            render();
        } finally {
            if (renderSection != null) {
                renderSection.stop();
            }
        }
    }

    @Override
    public void destroy() {
        spriteBatch.dispose();
    }

}
