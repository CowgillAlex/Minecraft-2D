package dev.alexco.minecraft.blaze2d.debug;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.world.entity.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import java.util.Collection;

public class HitboxRenderer extends RenderableLifecycle {
    private ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        super.create("Hitbox", Color.ORANGE);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    @Override
    public void destroy() {
        super.destroy();
        shapeRenderer.dispose();
    }

    @Override
    public void render() {
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);

        renderEntity(Minecraft.getInstance().getPlayer());
        renderEntities(Minecraft.getInstance().getWorld().entities);
        shapeRenderer.end();
    }


    /**
     * Draws one entity's bounding box in screen space.
     */
    public void renderEntity(Entity entity) {
        if (entity == null || entity.bb == null) return;

        float blockSize = (float) Minecraft.getInstance().getWorld().worldData.blockSize;
        double camX = Minecraft.getInstance().getWorld().worldData.cameraX;
        double camY = Minecraft.getInstance().getWorld().worldData.cameraY;

        double x0 = entity.bb.x0 * blockSize;
        double y0 = entity.bb.y0 * blockSize;
        double x1 = entity.bb.x1 * blockSize;
        double y1 = entity.bb.y1 * blockSize;


        if (y1 < y0) {
            double tmp = y0;
            y0 = y1;
            y1 = tmp;
        }

        double width = x1 - x0;
        double height = y1 - y0;

        shapeRenderer.rect(
            (float)(x0 - camX),
            (float)(y0 - camY),
            (float)width,
            (float)height
        );
    }


    /**
     * Draws hitboxes for each entity in the given collection.
     */
    public void renderEntities(Collection<? extends Entity> entities) {
        if (entities == null) return;

        for (Entity entity : entities) {
            renderEntity(entity);
        }
    }


    /**
     * Draws hitboxes for the provided entity varargs.
     */
    public void renderEntities(Entity... entities) {
        if (entities == null) return;

        for (Entity entity : entities) {
            renderEntity(entity);
        }
    }
}
