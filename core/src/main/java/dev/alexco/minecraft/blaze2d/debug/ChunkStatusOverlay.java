package dev.alexco.minecraft.blaze2d.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;
import dev.alexco.minecraft.world.level.levelgen.structure.StructureManager.StructureStart;

import java.util.List;
import java.util.Map;

/**
 * Enhanced overlay showing chunk status, biome info, surface data, and generation timings
 */
public class ChunkStatusOverlay extends RenderableLifecycle {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont smallFont;

    // Colours for different chunk statuses
    private static final Color COLOR_EMPTY = new Color(0.5f, 0.5f, 0.5f, 0.8f);
    private static final Color COLOR_NOISE = new Color(0.6f, 0.4f, 0.2f, 0.8f);
    private static final Color COLOR_SURFACE = new Color(0.4f, 0.6f, 0.2f, 0.8f);
    private static final Color COLOR_STRUCTURES = new Color(0.2f, 0.5f, 0.8f, 0.8f);
    private static final Color COLOR_FEATURES = new Color(0.8f, 0.6f, 0.2f, 0.8f);
    private static final Color COLOR_FULL = new Color(0.2f, 0.8f, 0.2f, 0.8f);

    // Colours for surface info
    private static final Color COLOR_GRASS = new Color(0.4f, 0.8f, 0.2f, 1.0f);
    private static final Color COLOR_DIRT = new Color(0.6f, 0.4f, 0.2f, 1.0f);
    private static final Color COLOR_STONE = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    private static final Color COLOR_SAND = new Color(0.9f, 0.9f, 0.5f, 1.0f);
    private static final Color COLOR_WATER = new Color(0.2f, 0.4f, 0.9f, 1.0f);

    @Override
    public void create() {
        super.create("Chunk Status", Color.BLUE);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);

        smallFont = new BitmapFont();
        smallFont.setColor(Color.WHITE);
        smallFont.getData().setScale(0.8f);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void destroy() {
        super.destroy();
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        smallFont.dispose();
    }

    @Override
    public void render() {
        World world = Minecraft.getInstance().getWorld();
        float blockSize = world.worldData.blockSize;
        double camX = world.worldData.cameraX;
        double camY = world.worldData.cameraY;

        // Render status boxes at top of each chunk
        shapeRenderer.begin(ShapeType.Filled);
        for (Chunk chunk : world.worldChunks.values()) {
            renderChunkStatusBox(chunk, blockSize, camX, camY);
        }
        shapeRenderer.end();

        // Render text labels
        batch.begin();
        for (Chunk chunk : world.worldChunks.values()) {
            renderChunkStatusText(chunk, world, blockSize, camX, camY);
        }
        batch.end();
    }

    /**
     * Draws a coloured status strip above a chunk to reflect its generation stage.
     */
    private void renderChunkStatusBox(Chunk chunk, float blockSize, double camX, double camY) {
        int chunkX = chunk.getChunkPos().x;
        float worldX = chunkX * 16 * blockSize;
        float worldY = 380 * blockSize; // Near top of world
        float boxWidth = 16 * blockSize; // Full chunk width
        float boxHeight = 3 * blockSize;

        // Colour based on chunk status
        Color statusColor = getColorForStatus(chunk.getStatus());
        shapeRenderer.setColor(statusColor);

        shapeRenderer.rect(
            (float)(worldX - camX),
            (float)(worldY - camY),
            boxWidth,
            boxHeight
        );
    }

    /**
     * Draws per-chunk diagnostics including status, structures, surface and timings.
     */
    private void renderChunkStatusText(Chunk chunk, World world, float blockSize, double camX, double camY) {
        int chunkX = chunk.getChunkPos().x;
        float worldX = chunkX * 16 * blockSize + (8 * blockSize); // Centre of chunk
        float worldY = 381 * blockSize;

        float textX = (float)(worldX - camX);
        float textY = (float)(worldY - camY);

        // Chunk number
        font.draw(batch, String.format("Chunk %d", chunkX), textX - 30, textY + 60);

        // Status
        font.draw(batch, chunk.getStatus().toString(), textX - 30, textY + 40);

        // Structure count
        List<StructureStart> starts = world.structureManager.structureStarts.get(chunk.getChunkPos());
        int structureCount = starts != null ? starts.size() : 0;

        if (structureCount > 0) {
            font.setColor(Color.YELLOW);
            font.draw(batch, String.format("%d structure(s)", structureCount), textX - 30, textY + 20);
            font.setColor(Color.WHITE);
        } else {
            font.draw(batch, "No structures", textX - 30, textY + 20);
        }

        // Biome info - sample at centre of chunk
        int centerWorldX = chunkX * 16 + 8;


        // Surface analysis at centre column
        int centerLocalX = 8;
        renderSurfaceAnalysis(chunk, centerLocalX, textX, textY - 15);

        // Generation timing metrics
        long totalTime = chunk.getTotalGenerationTime();
        if (totalTime > 0) {
            font.setColor(Color.GOLD);
            font.draw(batch, String.format("Total: %dms", totalTime), textX - 30, textY - 70);

            // Show individual step timings
            int lineOffset = -90;
            for (ChunkStatus status : ChunkStatus.values()) {
                long timing = chunk.getGenerationTiming(status);
                if (timing > 0) {
                    smallFont.setColor(getColorForStatus(status));
                    smallFont.draw(batch, String.format("%s: %dms", status.name(), timing), textX - 30, textY + lineOffset);
                    lineOffset -= 12;
                }
            }
            smallFont.setColor(Color.WHITE);
            font.setColor(Color.WHITE);
        }

        // Generating indicator
        if (chunk.amiGenerating()) {
            font.setColor(Color.CYAN);
            font.draw(batch, "GENERATING...", textX - 30, textY - (totalTime > 0 ? 160 : 70));
            font.setColor(Color.WHITE);
        }
    }

    /**
     * Inspects one column and renders a short breakdown of surface and sub-surface blocks.
     */
    private void renderSurfaceAnalysis(Chunk chunk, int localX, float textX, float startY) {
        // Analyse the column - find surface and show top blocks
        int surfaceY = -1;
        BlockState surfaceBlock = null;
        BlockState blockBelow = null;
        BlockState block2Below = null;

        // Scan from top to find surface
        for (int y = 255; y >= 0; y--) {
            BlockState state = chunk.getBlockAt(localX, y);
            if (!state.getBlock().equals(Blocks.AIR)) {
                surfaceY = y;
                surfaceBlock = state;
                if (y > 0) blockBelow = chunk.getBlockAt(localX, y - 1);
                if (y > 1) block2Below = chunk.getBlockAt(localX, y - 2);
                break;
            }
        }

        if (surfaceY >= 0 && surfaceBlock != null) {
            Color blockColor = getColorForBlock(surfaceBlock);
            smallFont.setColor(blockColor);
            String blockName = surfaceBlock.getBlock().getDescriptionId();
            if (blockName.contains(":")) {
                blockName = blockName.substring(blockName.indexOf(":") + 1);
            }
            smallFont.draw(batch, String.format("S: %s@%d", blockName, surfaceY), textX - 40, startY);

            // Show blocks below
            if (blockBelow != null) {
                String belowName = blockBelow.getBlock().getDescriptionId();
                if (belowName.contains(":")) belowName = belowName.substring(belowName.indexOf(":") + 1);
                smallFont.setColor(getColorForBlock(blockBelow));
                smallFont.draw(batch, String.format("-1: %s", belowName), textX - 40, startY - 12);
            }
            if (block2Below != null) {
                String below2Name = block2Below.getBlock().getDescriptionId();
                if (below2Name.contains(":")) below2Name = below2Name.substring(below2Name.indexOf(":") + 1);
                smallFont.setColor(getColorForBlock(block2Below));
                smallFont.draw(batch, String.format("-2: %s", below2Name), textX - 40, startY - 24);
            }
            smallFont.setColor(Color.WHITE);
        } else {
            smallFont.setColor(Color.GRAY);
            smallFont.draw(batch, "No surface", textX - 40, startY);
            smallFont.setColor(Color.WHITE);
        }
    }


    /**
     * Maps chunk generation statuses to overlay colours.
     */
    private Color getColorForStatus(ChunkStatus status) {
        switch (status) {
            case EMPTY: return COLOR_EMPTY;
            case NOISE: return COLOR_NOISE;
            case SURFACE: return COLOR_SURFACE;
            case STRUCTURES: return COLOR_STRUCTURES;
            case FEATURES: return COLOR_FEATURES;
            case FULL: return COLOR_FULL;
            default: return Color.GRAY;
        }
    }

    /**
     * Maps common surface block names to debug text colours.
     */
    private Color getColorForBlock(BlockState state) {
        String name = state.getBlock().getDescriptionId().toLowerCase();
        if (name.contains("grass")) return COLOR_GRASS;
        if (name.contains("dirt")) return COLOR_DIRT;
        if (name.contains("sand")) return COLOR_SAND;
        if (name.contains("water")) return COLOR_WATER;
        if (name.contains("stone")) return COLOR_STONE;
        return Color.WHITE;
    }
}
