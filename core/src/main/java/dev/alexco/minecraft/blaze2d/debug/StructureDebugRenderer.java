package dev.alexco.minecraft.blaze2d.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkPos;
import dev.alexco.minecraft.world.level.levelgen.structure.StructureManager.StructureStart;
import dev.alexco.minecraft.world.level.levelgen.structure.TerrainFollowingStructure;

import java.util.List;

public class StructureDebugRenderer extends RenderableLifecycle {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    // Colours for different states
    private static final Color COLOR_CALCULATED = new Color(1f, 1f, 0f, 0.7f); // Yellow - structure position calculated
    private static final Color COLOR_PLACED = new Color(0f, 1f, 0f, 0.7f);     // Green - structure blocks placed
    private static final Color COLOR_PARTIAL = new Color(1f, 0.5f, 0f, 0.7f);  // Orange - partially placed
    private static final Color COLOR_ERROR = new Color(1f, 0f, 0f, 0.7f);      // Red - error/not placed
    private static final Color COLOR_TEXT = Color.WHITE;
    private static final Color COLOR_CHUNK_BORDER = new Color(0.5f, 0.5f, 0.5f, 0.3f); // Grey chunk borders

    @Override
    public void create() {
        super.create("Structure Debug", Color.CYAN);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(COLOR_TEXT);
        font.getData().setScale(1.5f); // Make text larger
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
    }

    @Override
    public void render() {
        World world = Minecraft.getInstance().getWorld();
        float blockSize = world.worldData.blockSize;
        double camX = world.worldData.cameraX;
        double camY = world.worldData.cameraY;

        // Render chunk borders first
        shapeRenderer.begin(ShapeType.Line);
        renderChunkBorders(world, blockSize, camX, camY);
        shapeRenderer.end();

        // Render structure boxes
        shapeRenderer.begin(ShapeType.Filled);
        for (Chunk chunk : world.worldChunks.values()) {
            renderStructuresInChunk(chunk, world, blockSize, camX, camY);
        }
        shapeRenderer.end();

        // Render structure outlines
        shapeRenderer.begin(ShapeType.Line);
        for (Chunk chunk : world.worldChunks.values()) {
            renderStructureOutlines(chunk, world, blockSize, camX, camY);
        }
        shapeRenderer.end();

        // Render text labels
        batch.begin();
        for (Chunk chunk : world.worldChunks.values()) {
            renderStructureLabels(chunk, world, blockSize, camX, camY);
        }
        batch.end();
    }

    /**
     * Draws vertical chunk boundary guides for structure placement context.
     */
    private void renderChunkBorders(World world, float blockSize, double camX, double camY) {
        shapeRenderer.setColor(COLOR_CHUNK_BORDER);

        for (Chunk chunk : world.worldChunks.values()) {
            int chunkX = chunk.getChunkPos().x;
            float worldX = chunkX * 16 * blockSize; // 16 blocks per chunk
            float worldY = 0;
            float chunkWidth = 16 * blockSize;
            float chunkHeight = 384 * blockSize; // Full world height

            // Render vertical line at chunk boundary
            shapeRenderer.line(
                (float)(worldX - camX), (float)(worldY - camY),
                (float)(worldX - camX), (float)(worldY + chunkHeight - camY)
            );
        }
    }

    /**
     * Renders filled debug boxes for structures near the current chunk.
     */
    private void renderStructuresInChunk(Chunk chunk, World world, float blockSize, double camX, double camY) {
        ChunkPos pos = chunk.getChunkPos();

        // Check nearby chunks for structures (structures can span chunks)
        for (int dx = -2; dx <= 2; dx++) {
            ChunkPos neighbourPos = new ChunkPos(pos.x + dx);
            List<StructureStart> starts = world.structureManager.structureStarts.get(neighbourPos);

            if (starts == null || starts.isEmpty()) continue;

            for (StructureStart start : starts) {
                renderStructureBox(start, chunk, world, blockSize, camX, camY, false);
            }
        }
    }

    /**
     * Renders outline boxes and origin markers for nearby structures.
     */
    private void renderStructureOutlines(Chunk chunk, World world, float blockSize, double camX, double camY) {
        ChunkPos pos = chunk.getChunkPos();

        for (int dx = -2; dx <= 2; dx++) {
            ChunkPos neighbourPos = new ChunkPos(pos.x + dx);
            List<StructureStart> starts = world.structureManager.structureStarts.get(neighbourPos);

            if (starts == null || starts.isEmpty()) continue;

            for (StructureStart start : starts) {
                renderStructureBox(start, chunk, world, blockSize, camX, camY, true);
            }
        }
    }

    /**
     * Draws one structure box, colouring it by current placement completion state.
     */
    private void renderStructureBox(StructureStart start, Chunk chunk, World world,
                                    float blockSize, double camX, double camY, boolean outline) {
        // Calculate structure bounds in world coordinates
        float worldX = start.x * blockSize;
        float worldY = start.y * blockSize;
        float width = start.structure.getWidth() * blockSize;
        float height = start.structure.getHeight() * blockSize;

        // Check how many blocks are actually placed
        int totalBlocks = start.structure.getBlocks().size() + start.structure.getBackgroundBlocks().size();
        int placedBlocks = countPlacedBlocks(start, world);

        // Determine colour based on placement status
        Color color;
        if (placedBlocks == 0) {
            color = COLOR_CALCULATED; // Yellow - calculated but not placed
        } else if (placedBlocks < totalBlocks) {
            color = COLOR_PARTIAL; // Orange - partially placed
        } else {
            color = COLOR_PLACED; // Green - fully placed
        }

        // Add transparency for filled boxes
        if (!outline) {
            color = new Color(color.r, color.g, color.b, 0.3f);
        }

        shapeRenderer.setColor(color);

        if (outline) {
            // Thicker outline for visibility
            shapeRenderer.rectLine(
                (float)(worldX - camX), (float)(worldY - camY),
                (float)(worldX + width - camX), (float)(worldY - camY),
                2f
            );
            shapeRenderer.rectLine(
                (float)(worldX + width - camX), (float)(worldY - camY),
                (float)(worldX + width - camX), (float)(worldY + height - camY),
                2f
            );
            shapeRenderer.rectLine(
                (float)(worldX + width - camX), (float)(worldY + height - camY),
                (float)(worldX - camX), (float)(worldY + height - camY),
                2f
            );
            shapeRenderer.rectLine(
                (float)(worldX - camX), (float)(worldY + height - camY),
                (float)(worldX - camX), (float)(worldY - camY),
                2f
            );

            // Draw X to mark origin point
            float markSize = 4f * blockSize;
            shapeRenderer.rectLine(
                (float)(worldX - markSize - camX), (float)(worldY - markSize - camY),
                (float)(worldX + markSize - camX), (float)(worldY + markSize - camY),
                2f
            );
            shapeRenderer.rectLine(
                (float)(worldX - markSize - camX), (float)(worldY + markSize - camY),
                (float)(worldX + markSize - camX), (float)(worldY - markSize - camY),
                2f
            );
        } else {
            shapeRenderer.rect(
                (float)(worldX - camX),
                (float)(worldY - camY),
                width,
                height
            );
        }
    }

    /**
     * Draws text labels for nearby structures.
     */
    private void renderStructureLabels(Chunk chunk, World world, float blockSize, double camX, double camY) {
        ChunkPos pos = chunk.getChunkPos();

        for (int dx = -2; dx <= 2; dx++) {
            ChunkPos neighbourPos = new ChunkPos(pos.x + dx);
            List<StructureStart> starts = world.structureManager.structureStarts.get(neighbourPos);

            if (starts == null || starts.isEmpty()) continue;

            for (StructureStart start : starts) {
                renderStructureLabel(start, world, blockSize, camX, camY);
            }
        }
    }

    /**
     * Draws one structure's metadata, placement progress, and chunk status.
     */
    private void renderStructureLabel(StructureStart start, World world, float blockSize, double camX, double camY) {
        float worldX = start.x * blockSize;
        float worldY = start.y * blockSize;

        // Count regular blocks + terrain layer blocks
        int totalBlocks = start.structure.getBlocks().size() + start.structure.getBackgroundBlocks().size();

        // Add terrain layer blocks if it's a terrain-following structure
        if (start.structure instanceof TerrainFollowingStructure) {
            TerrainFollowingStructure tfs = (TerrainFollowingStructure) start.structure;
            for (var layer : tfs.getTerrainLayers().values()) {
                totalBlocks += layer.getBlocks().size();
            }
        }

        int placedBlocks = countPlacedBlocks(start, world);

        // Position text above structure
        float textX = (float)(worldX - camX);
        float textY = (float)(worldY + (start.structure.getHeight() * blockSize) - camY + 20);

        // Draw structure name
        font.draw(batch, start.structure.getName(), textX, textY);

        // Draw position
        font.draw(batch, String.format("Pos: (%d, %d)", start.x, start.y), textX, textY - 20);

        // Draw size
        font.draw(batch, String.format("Size: %dx%d", start.structure.getWidth(), start.structure.getHeight()),
                 textX, textY - 40);

        // Draw placement status
        String status = String.format("Blocks: %d/%d", placedBlocks, totalBlocks);
        Color statusColor = placedBlocks == totalBlocks ? COLOR_PLACED :
                           placedBlocks == 0 ? COLOR_CALCULATED : COLOR_PARTIAL;
        font.setColor(statusColor);
        font.draw(batch, status, textX, textY - 60);
        font.setColor(COLOR_TEXT);

        // Draw chunk info
        int structureChunkX = World.getChunkX(start.x);
        Chunk structureChunk = world.getChunkIfExists(structureChunkX);
        if (structureChunk != null) {
            font.draw(batch, String.format("Chunk: %d (%s)", structureChunkX, structureChunk.getStatus()),
                     textX, textY - 80);
        } else {
            font.setColor(COLOR_ERROR);
            font.draw(batch, String.format("Chunk: %d (NOT LOADED)", structureChunkX), textX, textY - 80);
            font.setColor(COLOR_TEXT);
        }
    }

    /**
     * Count how many blocks from this structure are actually placed in the world
     */
    private int countPlacedBlocks(StructureStart start, World world) {
        int count = 0;

        // Check foreground blocks
        for (var block : start.structure.getBlocks()) {
            int globalX = start.x + block.relX;
            int globalY = start.y + block.relY;

            try {
                BlockState placedBlock = world.getBlock(globalX, globalY);
                if (placedBlock != null && !placedBlock.getBlock().isAir()) {
                    // Block exists - might be the structure block or terrain
                    // For more accurate counting, compare with expected block type
                    if (placedBlock.getBlock().equals(block.state.getBlock())) {
                        count++;
                    }
                }
            } catch (Exception e) {
                // Chunk not loaded or out of bounds
            }
        }

        // Check background blocks
        for (var block : start.structure.getBackgroundBlocks()) {
            int globalX = start.x + block.relX;
            int globalY = start.y + block.relY;

            try {
                BlockState placedBlock = world.getBackgroundBlock(globalX, globalY);
                if (placedBlock != null && !placedBlock.getBlock().isAir()) {
                    if (placedBlock.getBlock().equals(block.state.getBlock())) {
                        count++;
                    }
                }
            } catch (Exception e) {
                // Chunk not loaded
            }
        }

        // Check terrain layer blocks if it's a terrain-following structure
        if (start.structure instanceof TerrainFollowingStructure) {
            TerrainFollowingStructure tfs = (TerrainFollowingStructure) start.structure;

            for (var layerEntry : tfs.getTerrainLayers().entrySet()) {
                int yOffset = layerEntry.getKey();
                var layer = layerEntry.getValue();

                for (var blockEntry : layer.getBlocks().entrySet()) {
                    int xOffset = blockEntry.getKey();
                    BlockState expectedState = blockEntry.getValue();

                    int globalX = start.x + xOffset;

                    try {
                        // Find surface at this X
                        Integer surfaceY = findSurfaceYForDebug(globalX, world);
                        if (surfaceY != null) {
                            int placementY = surfaceY + yOffset;

                            BlockState placedBlock = world.getBlock(globalX, placementY);
                            if (placedBlock != null &&
                                placedBlock.getBlock().equals(expectedState.getBlock())) {
                                count++;
                            }
                        }
                    } catch (Exception e) {
                        // Chunk not loaded or error
                    }
                }
            }
        }

        return count;
    }

    /**
     * Find surface Y for debug visualization
     */
    private Integer findSurfaceYForDebug(int globalX, World world) {
        try {
            Chunk chunk = world.getChunkIfExists(World.getChunkX(globalX));
            if (chunk == null) return null;

            int localX = World.getLocalX(globalX);

            for (int y = 250; y > 10; y--) {
                BlockState current = chunk.getBlockAt(localX, y);
                BlockState below = chunk.getBlockAt(localX, y - 1);

                if (current != null && current.getBlock().isAir() &&
                    below != null && !below.getBlock().isAir()) {
                    return y;
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }

        return null;
    }
}
