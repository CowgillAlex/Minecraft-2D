package dev.alexco.minecraft.blaze2d.debug;

import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.TimeUtils;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.Version;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.blaze2d.SelectorRenderer;
import dev.alexco.minecraft.blaze2d.special.SolidityAABB;
import dev.alexco.minecraft.phys.AABBPool;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.util.Formatter;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.state.properties.Property;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.levelgen.vanilla.VanillaNoiseStep;
import dev.alexco.minecraft.world.level.levelgen.vanilla.VanillaWorldgenConfig;

public class DebugScreenRenderer extends RenderableLifecycle {
    public BitmapFont bitmapFont;
    private ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        bitmapFont = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        bitmapFont.getData().markupEnabled = true;
        bitmapFont.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        shapeRenderer = new ShapeRenderer();
        super.create("Debug Screen", Color.CORAL);
    }

    @Override
    public void render() {
        spriteBatch.begin();

        int mouseX = SelectorRenderer.worldMouseX;
        int mouseY = SelectorRenderer.worldMouseY;
        int playerX = (int) Math.floor(Minecraft.getInstance().getPlayer().x);
        int playerY = (int) Math.floor(Minecraft.getInstance().getPlayer().y);

        drawTextWithBackground(String.format("Paper Minecraft: §6%s", Version.VERSION_STRING), 0, false);
        drawTextWithBackground(String.format(
                "FPS: §6%d§f, TPS: §6%d§f, ticks: §6%d§f, state: §6%s",
                Gdx.graphics.getFramesPerSecond(),
                Minecraft.getInstance().getSession().getTimer().getTicksPerSecond(),
                Minecraft.getInstance().getSession().getTimer().totalTicks,
                Minecraft.getInstance().getSession().getTimer().isPaused() ? "paused" : "running"),
                38,
                false);
        drawTextWithBackground(String.format("Mouse X: §6%d§f, Y: §6%d", mouseX, mouseY), 76 + 38, false);
        drawTextWithBackground(String.format("Player X: §6%f§f, Y: §6%f ",
                Minecraft.getInstance().getPlayer().xo, Minecraft.getInstance().getPlayer().yo), 76 + 38 + 38, false);

        drawTextWithBackground(getMemFromRuntime(Minecraft.getInstance().getRuntime()), 0, true);
        drawTextWithBackground(String.format("Zoom: §6%f", Minecraft.getInstance().getWorld().worldData.blockSize),
                76 + (38 * 4),
                false);

        drawTextWithBackground(String.format("Sky: §6%d§f Block:§6%d",
                getSkyLightAt(mouseX, mouseY), getBlockLightAt(mouseX, mouseY)),//fix
                76 + (38 * 5),
                false);

        drawTextWithBackground(String.format("AABB Pool: §6%d§f/§650000 §7(§fAcq:§6%d§7, Rel:§6%d§7, Net:§6%d§7)",
                AABBPool.AABBpool.getPoolSize(),
                AABBPool.getTotalAcquired(),
                AABBPool.getTotalReleased(),
                AABBPool.getNetChange()),
                76 + (38 * 6),
                false);
        int solidAabbPoolSize = SolidityAABB.getPoolSize();
        if (solidAabbPoolSize >= 0) {
            drawTextWithBackground(String.format("SolidAABB Pool: §6%d§f/§64000",
                    solidAabbPoolSize),
                    76 + (38 * 7),
                    false);
        } else {
            drawTextWithBackground("SolidAABB: §6no pooling§f (fresh instances)",
                    76 + (38 * 7),
                    false);
        }


        drawTextWithBackground(getBiomeDebugInfo(playerX), 76 + (38 * 3), true);
        drawTextWithBackground(getNoiseDebugInfo(playerX), 76 + (38 * 4), true);
        drawTextWithBackground(getTerrainDebugInfo(playerX, playerY), 76 + (38 * 5), true);

        spriteBatch.end();
    }

    @Override
    public void destroy() {
        super.destroy();
        bitmapFont.dispose();
        shapeRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    private static GlyphLayout layout = new GlyphLayout();

    /**
     * Draws one debug text line with a translucent backdrop on either screen edge.
     */
    private void drawTextWithBackground(String text, float yPos, boolean alignRight) {
        String text2 = text.replaceAll("§.", "");
        layout.setText(bitmapFont, text2);
        float width = layout.width + 20;
        float height = layout.height + 10;
        float xPos = alignRight ? Gdx.graphics.getWidth() - width - 10 : 10;
        float adjustedY = Gdx.graphics.getHeight() - yPos - height - 20;

        spriteBatch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.7f);
        shapeRenderer.rect(xPos - 2f, adjustedY, width + 2f, height + 5f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        spriteBatch.begin();
        bitmapFont.draw(spriteBatch, Formatter.formatBg(text), xPos + 0.5f, adjustedY + height - 0.5f);
        bitmapFont.draw(spriteBatch, Formatter.format(text), xPos, adjustedY + height);
    }

    /**
     * Returns a compact summary of current JVM memory usage.
     */
    private static String getMemFromRuntime(Runtime runtime) {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        return String.format("Mem: §6%s§fMB / §6%s§fMB, Free: §6%s§fMB",
                (int) Math.floor(usedMemory / (1024.0 * 1024.0)),
                (int) Math.floor(totalMemory / (1024.0 * 1024.0)),
                (int) Math.floor(freeMemory / (1024.0 * 1024.0)));
    }

    /**
     * Reads sky-light at world coordinates with bounds and chunk checks.
     */
    private static int getSkyLightAt(int worldX, int worldY) {
        if (worldY < 0 || worldY >= SharedConstants.CHUNK_HEIGHT) return -1;
        World world = Minecraft.getInstance().getWorld();
        Chunk chunk = world.getChunkIfExists(World.getChunkX(worldX));
        if (chunk == null) return -1;
        return chunk.getSkyLightAt(World.getLocalX(worldX), worldY) & 0xFF;
    }

    /**
     * Reads block-light at world coordinates with bounds and chunk checks.
     */
    private static int getBlockLightAt(int worldX, int worldY) {
        if (worldY < 0 || worldY >= SharedConstants.CHUNK_HEIGHT) return -1;
        World world = Minecraft.getInstance().getWorld();
        Chunk chunk = world.getChunkIfExists(World.getChunkX(worldX));
        if (chunk == null) return -1;
        return chunk.getBlockLightAt(World.getLocalX(worldX), worldY) & 0xFF;
    }

    /**
     * Formats foreground block id and properties for debug display.
     */
    @SuppressWarnings("unchecked")
    private static String getBlockAt(int x, int y) {
        try {
            BlockState state = Minecraft.getInstance().getWorld().getBlock(x, y);
            String blockKey = Registry.BLOCK.getKey(state.getBlock()).toString();

            StringBuilder sb = new StringBuilder(blockKey);
            if (state != null && !state.getValues().isEmpty()) {
                sb.append("§2[");
                sb.append(state.getValues().entrySet().stream()
                        .map(entry -> {
                            @SuppressWarnings("rawtypes")
                            Property property = entry.getKey();
                            @SuppressWarnings("rawtypes")
                            Comparable value = entry.getValue();
                            return property.getName() + "=" + property.getName(value);
                        })
                        .collect(Collectors.joining(",")));
                sb.append("]");
            }
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Formats background block id and properties for debug display.
     */
    @SuppressWarnings("unchecked")
    private static String getBackgroundBlockAt(int x, int y) {
        try {
            BlockState state = Minecraft.getInstance().getWorld().getBackgroundBlock(x, y);
            String blockKey = Registry.BLOCK.getKey(state.getBlock()).toString();

            StringBuilder sb = new StringBuilder(blockKey);
            if (state != null && !state.getValues().isEmpty()) {
                sb.append("§2[");
                sb.append(state.getValues().entrySet().stream()
                        .map(entry -> {
                            @SuppressWarnings("rawtypes")
                            Property property = entry.getKey();
                            @SuppressWarnings("rawtypes")
                            Comparable value = entry.getValue();
                            return property.getName() + "=" + property.getName(value);
                        })
                        .collect(Collectors.joining(",")));
                sb.append("]");
            }
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static long terrainDebugLastUpdateMs = 0L;
    private static int terrainDebugLastX = Integer.MIN_VALUE;
    private static int terrainDebugLastY = Integer.MIN_VALUE;
    private static String terrainDebugCached = "Terrain: pending";

    /**
     * Returns the current biome at the player's x coordinate.
     */
    private static String getBiomeDebugInfo(int playerX) {
        try {
            Biome biome = Minecraft.getInstance().getWorld().getChunk(World.getChunkX(playerX)).getBiome(World.getLocalX(playerX));
            if (biome == null) return String.format("Biome: §6%s", "Unknown");
            String biomeName = biome.getName().toString();
            return String.format("Biome: §6%s", biomeName);
        } catch (Exception e) {
            return "Biome: §cError§f" + e.getMessage();
        }
    }

    /**
     * Returns sampled climate noise channels for the player's x coordinate.
     */
    private static String getNoiseDebugInfo(int playerX) {
        try {
            VanillaNoiseStep noiseStep = VanillaNoiseStep.get(Minecraft.getInstance().getWorld().seed);
            int blockX = playerX;
            double continentalness = noiseStep.sampleContinentalness(blockX, 0, 0);
            double temperature = noiseStep.sampleTemperature(blockX, 0, 0);
            double humidity = noiseStep.sampleHumidity(blockX, 0, 0);
            double weirdness = noiseStep.sampleWeirdness(blockX, 0, 0);
            double erosion = noiseStep.sampleErosion(blockX, 0, 0);

            return String.format("C:§6%.2f§f T:§6%.2f§f H:§6%.2f§f W:§6%.2f§f E:§6%.2f",
                    continentalness, temperature, humidity, weirdness, erosion);
        } catch (Exception e) {
            return "Noise: §cError";
        }
    }

    /**
     * Returns cached terrain diagnostics including density and inferred surface height.
     */
    private static String getTerrainDebugInfo(int playerX, int playerY) {
        try {
            long now = TimeUtils.millis();
            if (Math.abs(playerX - terrainDebugLastX) <= 1
                    && Math.abs(playerY - terrainDebugLastY) <= 1
                    && now - terrainDebugLastUpdateMs < 750) {
                return terrainDebugCached;
            }

            long seed = Minecraft.getInstance().getWorld().seed;
            VanillaWorldgenConfig worldgen = VanillaWorldgenConfig.get(seed);
            VanillaNoiseStep noiseStep = VanillaNoiseStep.get(seed);

            int blockX = playerX;
            int vanillaY = worldgen.getMinY() + playerY;
            double density = noiseStep.sampleFinalDensity2D(blockX, vanillaY);

            double continentalness = noiseStep.sampleContinentalness(blockX, 0, 0);
            double erosion = noiseStep.sampleErosion(blockX, 0, 0);

            int groundLevel = findSurfaceY(blockX, noiseStep, worldgen);

            double depthBelowGround = groundLevel - playerY;
            String depthStr;
            if (depthBelowGround > 5) {
                depthStr = String.format("§6%.0f§f blocks underground", depthBelowGround);
            } else if (depthBelowGround < -5) {
                depthStr = String.format("§6%.0f§f blocks above ground", -depthBelowGround);
            } else {
                depthStr = "at surface";
            }

            terrainDebugCached = String.format("Dens: §6%.3f§f C:§6%.2f§f E:§6%.2f§f Surf: §6%d",
                    density, continentalness, erosion, groundLevel);
            terrainDebugLastX = playerX;
            terrainDebugLastY = playerY;
            terrainDebugLastUpdateMs = now;
            return terrainDebugCached;
        } catch (Exception e) {
            return "Terrain: §cError";
        }
    }

    /**
     * Finds surface y from loaded chunk data, with density-based fallback.
     */
    private static int findSurfaceY(int worldX, VanillaNoiseStep noiseStep, VanillaWorldgenConfig worldgen) {
        int chunkX = dev.alexco.minecraft.world.World.getChunkX(worldX);
        Chunk chunk = Minecraft.getInstance().getWorld().getChunkIfExists(chunkX);
        int localX = dev.alexco.minecraft.world.World.getLocalX(worldX);

        if (chunk != null && chunk.getStatus().isAtLeast(dev.alexco.minecraft.world.level.chunk.ChunkStatus.NOISE)) {
            for (int y = SharedConstants.CHUNK_HEIGHT - 2; y >= 1; y--) {
                BlockState here = chunk.getBlockAt(localX, y);
                BlockState above = chunk.getBlockAt(localX, y + 1);
                if (!here.getBlock().isAir() && (above.getBlock().isAir() || BlockTags.FLUID.contains(above.getBlock()))) {
                    return y;
                }
            }
        }

        int minY = worldgen.getMinY();
        for (int y = SharedConstants.CHUNK_HEIGHT - 2; y >= 1; y--) {
            int vanillaY = minY + y;
            double here = noiseStep.sampleFinalDensity2D(worldX, vanillaY);
            double above = noiseStep.sampleFinalDensity2D(worldX, vanillaY + 1);
            if (here > 0.0 && above <= 0.0) {
                return y;
            }
        }
        return worldgen.getSeaLevelInternal();
    }
}
