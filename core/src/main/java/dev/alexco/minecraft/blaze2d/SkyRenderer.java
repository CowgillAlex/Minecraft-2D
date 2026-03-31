package dev.alexco.minecraft.blaze2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.biome.Biomes;

public class SkyRenderer extends RenderableLifecycle {

    private ShapeRenderer shapeRenderer;
    private static final float HORIZON_BRIGHTNESS_MULTIPLIER = 1.2f; // Slightly brighter at horizon
    private static final float ZENITH_DARKNESS_MULTIPLIER = 0.8f;    // Slightly darker at top

    private static final Color tempHorizonColor = new Color();
    private static final Color tempZenithColor = new Color();
    /**
     * Derives horizon and zenith gradient colours from the current base sky colour.
     */
     private static void createGradientColors(Color baseSkyColor) {

        tempHorizonColor.set(
            Math.min(1.0f, baseSkyColor.r * HORIZON_BRIGHTNESS_MULTIPLIER),
            Math.min(1.0f, baseSkyColor.g * HORIZON_BRIGHTNESS_MULTIPLIER),
            Math.min(1.0f, baseSkyColor.b * HORIZON_BRIGHTNESS_MULTIPLIER),
            1.0f
        );


        tempZenithColor.set(
            baseSkyColor.r * ZENITH_DARKNESS_MULTIPLIER,
            baseSkyColor.g * ZENITH_DARKNESS_MULTIPLIER,
            baseSkyColor.b * ZENITH_DARKNESS_MULTIPLIER,
            1.0f
        );
    }
    @Override
    public void create() {
        super.create("Sky", Color.CYAN);
        shapeRenderer = new ShapeRenderer();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    /**
     * Renders the vertical sky gradient using biome tint and current world time.
     */
    @Override
    public void render() {
        shapeRenderer.begin(ShapeType.Filled);


        Color baseSkyColor = SkyColourManager.getSkyColor(1.0f,
         Minecraft.getInstance().getWorld().getChunk(World.getChunkX(Minecraft.getInstance().getSession().getPlayer().x)).getBiome(World.getLocalX((int)Math.floor(Minecraft.getInstance().getSession().getPlayer().x)))
        );

        // make a gradient
        createGradientColors(baseSkyColor);


        if (SelectorRenderer.worldMouseY < 63) {
            shapeRenderer.setColor(Color.BLACK);
        }
        shapeRenderer.rect(0, 0,
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
            tempHorizonColor, tempHorizonColor, tempZenithColor, tempZenithColor);
        shapeRenderer.end();

    }

    @Override
    public void destroy() {
        super.destroy();
        shapeRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }
}
