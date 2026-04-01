package dev.alexco.minecraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;

import dev.alexco.minecraft.blaze2d.*;
import dev.alexco.minecraft.blaze2d.debug.*;
import dev.alexco.minecraft.blaze2d.gui.inventory.BarrelRenderer;
import dev.alexco.minecraft.blaze2d.gui.inventory.FurnaceRenderer;
import dev.alexco.minecraft.blaze2d.model.CowModel;
import dev.alexco.minecraft.blaze2d.model.PigModel;
import dev.alexco.minecraft.blaze2d.model.ZombieModel;
import dev.alexco.minecraft.gui.ScreenState;
import dev.alexco.minecraft.input.InputHandler;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.util.Timer;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.level.chunk.Chunk;
/**
 * Represents a single play session in the game, managing the world, player, and rendering.
 * These are isolated to ensure that they do not bleed into eachother creating problems.
 *
 */
public class GameSession {
    private World world;
    private Player player;
    private Timer timer;
    private String worldName;
    private String folderName;
    private boolean active = false;

    private MeshChunkRenderer meshChunkRenderer;
    private SkyRenderer skyRenderer;
    private SelectorRenderer selectorRenderer;
    private ItemEntityRenderer itemEntityRenderer;
    private MobRenderer cowRenderer;
    private MobRenderer zombieRenderer;
    private MobRenderer pigRenderer;
    private PlayerRenderer playerRenderer;
    private AABBRenderer aabbRenderer;
    private HitboxRenderer hitboxRenderer;
    private DebugScreenRenderer debugScreenRenderer;
    private StructureDebugRenderer structureDebugRenderer;
    private ChunkStatusOverlay chunkStatusOverlay;

    private FurnaceRenderer furnaceRenderer;
    private BarrelRenderer barrelRenderer;

    /**
     * Starts a new runtime session and creates world renderers.
     */
    public void start(World world, Player player, String worldName, String folderName) {
        this.world = world;
        this.player = player;
        this.worldName = worldName;
        this.folderName = folderName;
        this.timer = new Timer(20);
        this.active = true;

        initializeWorldRenderers();
        Logger.INFO("Game session started for world: %s", worldName);
    }

    /**
     * Creates all renderers used while a world is active.
     */
    private void initializeWorldRenderers() {
        meshChunkRenderer = new MeshChunkRenderer();
        meshChunkRenderer.create();

        skyRenderer = new SkyRenderer();
        skyRenderer.create();

        selectorRenderer = new SelectorRenderer();
        selectorRenderer.create();

        itemEntityRenderer = new ItemEntityRenderer();
        itemEntityRenderer.create();

        cowRenderer = new MobRenderer<>(new CowModel(), "CowRenderer", Color.WHITE);
        cowRenderer.create();

        zombieRenderer = new MobRenderer<>(new ZombieModel(), "ZombieRenderer", Color.FOREST);
        zombieRenderer.create();
        pigRenderer = new MobRenderer<>(new PigModel(), "PigRenderer", Color.PINK);
        pigRenderer.create();

        playerRenderer = new PlayerRenderer();
        playerRenderer.create();

        aabbRenderer = new AABBRenderer();
        aabbRenderer.create();

        hitboxRenderer = new HitboxRenderer();
        hitboxRenderer.create();

        debugScreenRenderer = new DebugScreenRenderer();
        debugScreenRenderer.create();

        structureDebugRenderer = new StructureDebugRenderer();
        structureDebugRenderer.create();

        chunkStatusOverlay = new ChunkStatusOverlay();
        chunkStatusOverlay.create();
    }

    /**
     * Runs fixed-step world updates and camera movement.
     */
    public void tick() {
        if (!active || world == null) return;

        timer.advanceTime();
        if (InputHandler.isKeyDown(Keys.EQUALS)){ //debug override
            //add time
            world.gameTime += 100;
            timer.totalTicks += 100;
        }
        else if (InputHandler.isKeyDown(Keys.MINUS)){ //debug override
            //subtract time
            world.gameTime = Math.max(0, world.gameTime - 100);
            timer.totalTicks = Math.max(0, timer.totalTicks - 100);
        }
        for (int i = 0; i < timer.elapsedTicks; i++) { //this will allow to catch up to the 20tps
            world.calculateLoadedChunks();
            InputHandler.update();
            player.tick(i);
            world.tick();
        }
        world.moveCamera();
    }

    /**
     * Advances chunk streaming while the loading screen is visible.
     */
    public void tickChunkLoading() {
        if (!active || world == null) return;

        timer.advanceTime();
        for (int i = 0; i < timer.elapsedTicks; i++) {
            // Keep chunk loading centred on the tentative player X,
            // but do not run player input interaction while spawning.
            world.moveCamera();
            world.calculateLoadedChunks();
        }
    }

    /**
     * Renders world geometry, entities and overlays.
     */
    public void renderWorld() { //draw the world separately
        if (!active || world == null) return;

        Minecraft mc = Minecraft.getInstance();

        skyRenderer.draw();
        meshChunkRenderer.draw();

        if (mc.currentScreenState.isPauseScreen()) {
            return;
        }

        selectorRenderer.draw();
        debugScreenRenderer.drawVersionLine();

        if (mc.isDebugHudVisible()) {
            aabbRenderer.draw();
            debugScreenRenderer.draw();
            hitboxRenderer.draw();
        }

        cowRenderer.draw();
        zombieRenderer.draw();
        pigRenderer.draw();
        itemEntityRenderer.draw();
    }

    public void renderPlayer() {
        if (!active || player == null) return;
        playerRenderer.draw();
    }

    public void renderPlayerInInventory() {
        if (!active || player == null) return;
        float playerScreenX = Gdx.graphics.getWidth() / 2f - 150f;
        float playerScreenY = Gdx.graphics.getHeight() / 2f + 50f;
        playerRenderer.renderAtScreenPosition(playerScreenX, playerScreenY, 6f);
    }

    /**
     * Resizes all active renderer resources.
     */
    public void resize(int width, int height) {
        if (!active) return;

        meshChunkRenderer.resize(width, height);
        skyRenderer.resize(width, height);
        selectorRenderer.resize(width, height);
        itemEntityRenderer.resize(width, height);
        cowRenderer.resize(width, height);
        zombieRenderer.resize(width, height);
        pigRenderer.resize(width, height);
        playerRenderer.resize(width, height);
        aabbRenderer.resize(width, height);
        hitboxRenderer.resize(width, height);
        debugScreenRenderer.resize(width, height);
        structureDebugRenderer.resize(width, height);
        chunkStatusOverlay.resize(width, height);
        if (furnaceRenderer != null) furnaceRenderer.resize(width, height);
        if (barrelRenderer != null) barrelRenderer.resize(width, height);
    }
    /**
     * cleanup the world so it does not leak resources. Save the currently loaded chunks.
     */
    public void destroy() {
        if (world != null) {
            Logger.INFO("Saving world: %s", worldName);
            world.saveWorld();
            for (Chunk chunk : world.worldChunks.values()) {
                chunk.unload(world);
            }
            world.worldChunks.clear();
            world.getBlockEntityManager().clear();
            world.entities.clear();
        }

        disposeWorldRenderers();

        world = null;
        player = null;
        timer = null;
        worldName = null;
        folderName = null;
        active = false;
        Logger.INFO("World saved and session destroyed");
    }

    /**
     * Destroys and nulls all world renderer instances.
     */
    private void disposeWorldRenderers() {
        if (meshChunkRenderer != null) { meshChunkRenderer.destroy(); meshChunkRenderer = null; }
        if (skyRenderer != null) { skyRenderer.destroy(); skyRenderer = null; }
        if (selectorRenderer != null) { selectorRenderer.destroy(); selectorRenderer = null; }
        if (itemEntityRenderer != null) { itemEntityRenderer.destroy(); itemEntityRenderer = null; }
        if (cowRenderer != null) { cowRenderer.destroy(); cowRenderer = null; }
        if (zombieRenderer != null) { zombieRenderer.destroy(); zombieRenderer = null; }
        if (playerRenderer != null) { playerRenderer.destroy(); playerRenderer = null; }
        if (aabbRenderer != null) { aabbRenderer.destroy(); aabbRenderer = null; }
        if (hitboxRenderer != null) { hitboxRenderer.destroy(); hitboxRenderer = null; }
        if (debugScreenRenderer != null) { debugScreenRenderer.destroy(); debugScreenRenderer = null; }
        if (structureDebugRenderer != null) { structureDebugRenderer.destroy(); structureDebugRenderer = null; }
        if (chunkStatusOverlay != null) { chunkStatusOverlay.destroy(); chunkStatusOverlay = null; }
        if (furnaceRenderer != null) { furnaceRenderer.destroy(); furnaceRenderer = null; }
        if (barrelRenderer != null) { barrelRenderer.destroy(); barrelRenderer = null; }
    }

    public void end() {
        destroy();
    }

    public void saveAndEnd() {
        destroy();
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    public Timer getTimer() {
        return timer;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getFolderName() {
        return folderName;
    }

    public boolean isActive() {
        return active;
    }

    public void setCreating(boolean creating) {
    }

    public LoadingState getLoadingState() {
        return Minecraft.getInstance().loadingState;
    }

    public void openFurnace(int x, int y) {
        if (furnaceRenderer != null) {
            furnaceRenderer.destroy();
        }
        furnaceRenderer = new FurnaceRenderer(x, y);
        furnaceRenderer.create();
    }

    public void openBarrel(int x, int y) {
        if (barrelRenderer != null) {
            barrelRenderer.destroy();
        }
        barrelRenderer = new BarrelRenderer(x, y);
        barrelRenderer.create();
    }

    /**
     * Closes whichever container screen is currently open.
     */
    public void closeCurrentScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.currentScreenState == ScreenState.FURNACE) {
            if (furnaceRenderer != null) {
                furnaceRenderer.onClose();
            }
        } else if (mc.currentScreenState == ScreenState.BARREL) {
            if (barrelRenderer != null) {
                barrelRenderer.onClose();
            }
        }
        mc.currentScreenState = ScreenState.NONE;
    }

    public FurnaceRenderer getFurnaceRenderer() {
        return furnaceRenderer;
    }

    public BarrelRenderer getBarrelRenderer() {
        return barrelRenderer;
    }

    public MeshChunkRenderer getMeshChunkRenderer() {
        return meshChunkRenderer;
    }

    public SkyRenderer getSkyRenderer() {
        return skyRenderer;
    }

    public SelectorRenderer getSelectorRenderer() {
        return selectorRenderer;
    }

    public ItemEntityRenderer getItemEntityRenderer() {
        return itemEntityRenderer;
    }

    public PlayerRenderer getPlayerRenderer() {
        return playerRenderer;
    }

    public AABBRenderer getAabbRenderer() {
        return aabbRenderer;
    }

    public HitboxRenderer getHitboxRenderer() {
        return hitboxRenderer;
    }

    public DebugScreenRenderer getDebugScreenRenderer() {
        return debugScreenRenderer;
    }

    public StructureDebugRenderer getStructureDebugRenderer() {
        return structureDebugRenderer;
    }

    public ChunkStatusOverlay getChunkStatusOverlay() {
        return chunkStatusOverlay;
    }
}
