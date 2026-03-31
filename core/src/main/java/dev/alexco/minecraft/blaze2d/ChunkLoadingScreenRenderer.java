package dev.alexco.minecraft.blaze2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkPos;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;

public class ChunkLoadingScreenRenderer extends RenderableLifecycle {
    private BitmapFont titleFont;

    private static final Color TEXT_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color SHADOW_COLOR = new Color(0f, 0f, 0f, 0.5f);

    private static final int LOADING_RADIUS = 3;
    private static final GlyphLayout layout = new GlyphLayout(); // Only needs to be created once
    @Override
    public void create() {
        super.create("WorldLoadingScreen", Color.BLACK);

        titleFont  = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        titleFont.getData().markupEnabled = true;

        titleFont.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        titleFont.setColor(TEXT_COLOR);


    }

    /**
     * Draws the loading background and current chunk status summary near the player.
     */
    @Override
    public void render() {
        spriteBatch.begin();
        spriteBatch.setColor(Color.DARK_GRAY);
        int bgX = (Gdx.graphics.getWidth() / 48) + 1;
        int bgY = (Gdx.graphics.getHeight() / 48) + 1;
        spriteBatch.setColor(Color.DARK_GRAY);
        for (int x = 0; x < bgX; x++) {
            for (int y = 0; y < bgY; y++) {
                spriteBatch.draw(
                        Minecraft.getInstance().textureManager.get("textures/block/dirt.png"),
                        x * 48, y * 48, 48, 48);
            }
        }
        spriteBatch.setColor(Color.WHITE);

        spriteBatch.end();
        Minecraft minecraft = Minecraft.getInstance();
        String title = "Loading World\n";
        if (minecraft.loadingState != null) {
            title += minecraft.loadingState.getStep();
            if (!minecraft.loadingState.getDetail().isEmpty()) {
                title += "\n" + minecraft.loadingState.getDetail();
            }
        }
        title += "\n";

        if (minecraft.getWorld() != null && minecraft.getPlayer() != null) {
            int playerChunkX = (int) Math.floor(minecraft.getPlayer().x / 16.0);
            for (int dx = -LOADING_RADIUS; dx <= LOADING_RADIUS; dx++) {
                ChunkPos pos = new ChunkPos(playerChunkX + dx);
                Chunk chunk = minecraft.getWorld().getChunkIfExists(pos.x);
                title += "(" + pos.toString();
                if (chunk != null) {
                    ChunkStatus status = chunk.getStatus();
                    title += "= " + status;
                }
                title += ")\n";
            }
        }

        spriteBatch.begin();

        layout.setText(titleFont, title);
        float textX = (Gdx.graphics.getWidth() - layout.width) / 2;
        float textY = (Gdx.graphics.getHeight() + layout.height) / 2;
        titleFont.setColor(SHADOW_COLOR);
        titleFont.draw(spriteBatch, title, textX + 1, textY + 1);
        titleFont.setColor(TEXT_COLOR);
        titleFont.draw(spriteBatch, title, textX, textY);


        spriteBatch.end();
    }
@Override
public void resize(int width, int height) {
    super.resize(width, height);
}
    @Override
    public void destroy() {
        super.destroy();

        if (titleFont != null) {
            titleFont.dispose();
        }

    }
}
