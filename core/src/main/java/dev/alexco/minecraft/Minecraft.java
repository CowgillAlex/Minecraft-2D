package dev.alexco.minecraft;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.TimeUtils;
import dev.alexco.minecraft.blaze2d.*;
import dev.alexco.minecraft.blaze2d.debug.*;
import dev.alexco.minecraft.blaze2d.debug.DebugOverlay.Section;
import dev.alexco.minecraft.blaze2d.gui.HotbarRenderer;
import dev.alexco.minecraft.blaze2d.gui.inventory.CraftingTableRenderer;
import dev.alexco.minecraft.blaze2d.gui.inventory.InventoryRenderer;
import dev.alexco.minecraft.blaze2d.menus.*;
import dev.alexco.minecraft.blaze2d.tooltip.TooltipRenderer;
import dev.alexco.minecraft.crafting.FurnaceRecipeLoader;
import dev.alexco.minecraft.crafting.RecipeLoader;
import dev.alexco.minecraft.gui.ScreenState;
import dev.alexco.minecraft.input.InputHandler;
import dev.alexco.minecraft.loot.LootTableManager;
import dev.alexco.minecraft.phys.AABBPool;
import dev.alexco.minecraft.util.Crash;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.sound.SoundLoader;
import dev.alexco.minecraft.sound.SoundSystem;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.tag.BiomeTags;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.biome.Biome;
import dev.alexco.minecraft.world.biome.BiomeManager;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.entity.spawn.SpawnRulesManager;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.chunk.Chunk;
import dev.alexco.minecraft.world.level.chunk.ChunkPos;
import dev.alexco.minecraft.world.level.chunk.ChunkStatus;
import dev.alexco.minecraft.world.level.item.Items;
import dev.alexco.minecraft.world.level.levelgen.vanilla.VanillaNoiseStep;
import dev.alexco.minecraft.world.level.levelgen.noise.Noises;
import dev.alexco.minecraft.world.serialisation.WorldSaveManager;
import dev.alexco.minecraft.world.serialisation.WorldFile;
import dev.alexco.minecraft.world.serialisation.PlayerFile;
import net.querz.nbt.tag.CompoundTag;
/**
 * Contains the `ApplicationListener` which is the main entry point for the game.
 * It manages the game session, screens, and rendering loop.
 */
public class Minecraft implements ApplicationListener {
    private static Minecraft instance;
    //we only want one
    private GameSession session = new GameSession();
    //UI components
    //TODO organise them
    private TitleScreen titleScreen;
    private WorldSelectionScreen worldSelectionScreen;
    private WorldCreationScreen worldCreationScreen;
    private MoreWorldOptionsScreen moreWorldOptionsScreen;
    private PauseScreen pauseScreen;
    private SavingWorldScreen savingWorldScreen;
    private ChunkLoadingScreenRenderer chunkLoadingScreenRenderer;
    private SplashScreenRenderer splashScreenRenderer;
    public TextureManager textureManager;
    public ScreenState currentScreenState = ScreenState.TITLE;
    public DebugOverlay debugOverlay;
    private InputHandler inputHandler;
    public ExecutorService threads;
    public static TooltipRenderer tooltipRenderer;
    private Runtime runtime;
    //atomic because this is not particularly thread safe
    private final AtomicBoolean initComplete = new AtomicBoolean(false);
    private final AtomicBoolean assetsQueued = new AtomicBoolean(false);
    private volatile double bootStartTime = 0;
    public Section root;
    //one texture atlas instance
    public BlockTextureAtlas atlas;
    public Section tickSection;

    private HotbarRenderer hotbarRenderer;
    private InventoryRenderer inventoryRenderer;
    private CraftingTableRenderer craftingTableRenderer;

    public final LoadingState loadingState = new LoadingState();

    private Runnable pendingSaveTransition = null;

    public GameSession getSession() {
        return session;
    }

    public World getWorld() {
        return session.getWorld();
    }

    public Player getPlayer() {
        return session.getPlayer();
    }
    /**
     * Creates everything for the initial playthrough, loads all the assets and preps renderers
     */
    @Override
    public void create() {
        instance = this;
        runtime = Runtime.getRuntime();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Crash.showCrashScreen(throwable);
            throwable.printStackTrace();
            Gdx.app.exit();
        });

        loadingState.setStep("Initialising renderer", "Setting up graphics");

        this.textureManager = new TextureManager();
        textureManager.create();
        textureManager.forceLoadTexture("textures/splash.png");

        this.debugOverlay = new DebugOverlay();
        root = debugOverlay.addSection("Root", Color.RED);
        debugOverlay.create();

        this.splashScreenRenderer = new SplashScreenRenderer();
        splashScreenRenderer.create();
        //four threads because most computers have 4 cores. We catch the exceptions of
        // these backgroudn threads to stop silent failings .
        this.threads = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Minecraft-Worker");
                t.setUncaughtExceptionHandler((thread, throwable) -> {
                    Logger.ERROR("[THREAD] Uncaught exception in %s: %s", thread.getName(), throwable.getMessage());
                    throwable.printStackTrace();
                });
                return t;
            }
        });

        threads.submit(this::initBg);
        Logger.INFO("Finished pre-init");
    }

    /**
     * Loads core registries and assets on a worker thread so we dont block main.
     */
    private void initBg() {
        bootStartTime = TimeUtils.nanoTime();

        try {
            loadingState.setStep("Loading textures", "Building texture atlas");
            Logger.INFO("[BOOT] Step 1/7: Loading texture atlas");
            atlas = new BlockTextureAtlas();
            Logger.INFO("[BOOT] Step 1/7: Complete");
        } catch (Exception e) {
            logBootstrapError("texture atlas", e);
            return;
        }

        try {
            loadingState.setStep("Loading sounds", "Registering sound events");
            SoundLoader.loadSounds();
        } catch (Exception e) {
            logBootstrapError("sounds", e);
            return;
        }

        try {
            loadingState.setStep("Validating registries", "Blocks and items");
            System.out.println(Blocks.AIR.getDescriptionId());
            System.out.println(Items.AIR.getDescriptionId());
            dev.alexco.registry.Registry.validateRegistries();
        } catch (Exception e) {
            logBootstrapError("registry validation", e);
            return;
        }

        try {
            loadingState.setStep("Loading tags", "Block tags");
            BlockTags.loadTags();
        } catch (Exception e) {
            logBootstrapError("block tags", e);
            return;
        }

        try {
            loadingState.setStep("Loading recipes", "Crafting recipes");
            RecipeLoader.loadRecipes();
        } catch (Exception e) {
            logBootstrapError("crafting recipes", e);
            return;
        }

        try {
            loadingState.setStep("Loading recipes", "Furnace recipes");
            FurnaceRecipeLoader.loadRecipes();
        } catch (Exception e) {
            logBootstrapError("furnace recipes", e);
            return;
        }

        try {
            loadingState.setStep("Loading loot tables", "Block drops");
            LootTableManager.loadLootTables();
        } catch (Exception e) {
            logBootstrapError("loot tables", e);
            return;
        }

        try {
            loadingState.setStep("Loading spawn rules", "Mob spawn configuration");
            SpawnRulesManager.ensureLoaded();
        } catch (Exception e) {
            logBootstrapError("spawn rules", e);
            return;
        }

        try {
            loadingState.setStep("Initialising input", "Setting up controls");
            inputHandler = new InputHandler();
        } catch (Exception e) {
            logBootstrapError("input handler", e);
            return;
        }

        assetsQueued.set(true);
        loadingState.setStep("Loading assets", "Finishing asset loading");
        Logger.INFO("[BOOT] Background init complete, waiting for assets to load in render loop");
    }

    /**
     * Creates UI and gameplay renderers on the render thread.
     */
    private void createRenderers() {
        Gdx.graphics.setForegroundFPS(SharedConstants.FPS_CAP);

        this.titleScreen = new TitleScreen();
        this.worldSelectionScreen = new WorldSelectionScreen();
        this.worldCreationScreen = new WorldCreationScreen();
        this.moreWorldOptionsScreen = new MoreWorldOptionsScreen();
        this.pauseScreen = new PauseScreen();
        this.savingWorldScreen = new SavingWorldScreen();
        this.chunkLoadingScreenRenderer = new ChunkLoadingScreenRenderer();
        tooltipRenderer = new TooltipRenderer();

        hotbarRenderer = new HotbarRenderer();
        inventoryRenderer = new InventoryRenderer();
        craftingTableRenderer = new CraftingTableRenderer();

        debugOverlay.create();
        titleScreen.create();
        worldSelectionScreen.create();
        worldCreationScreen.create();
        moreWorldOptionsScreen.create();
        pauseScreen.create();
        savingWorldScreen.create();
        chunkLoadingScreenRenderer.create();
        tooltipRenderer.create();
        hotbarRenderer.create();
        inventoryRenderer.create();
        craftingTableRenderer.create();
        tickSection = root.getOrCreateChild("Tick", Color.YELLOW);
    }

    /**
     * Logs bootstrap failures and updates the loading screen state.
     */
    private void logBootstrapError(String step, Exception e) {
        Logger.ERROR("[BOOT] FAILED at step: %s", step);
        Logger.ERROR("[BOOT] Exception: %s: %s", e.getClass().getSimpleName(), e.getMessage());
        e.printStackTrace();
        loadingState.setStep("Error", step + ": " + e.getMessage());
    }

    /**
     * Resizes all active screens, overlays and session renderers.
     * Due to the isolation of cameras and spritebatches, we have to do
     * this manually for each.
     */
    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0)
            return;
        if (titleScreen != null) {
            titleScreen.resize(width, height);
        }
        if (worldSelectionScreen != null) {
            worldSelectionScreen.resize(width, height);
        }
        if (worldCreationScreen != null) {
            worldCreationScreen.resize(width, height);
        }
        if (moreWorldOptionsScreen != null) {
            moreWorldOptionsScreen.resize(width, height);
        }
        if (pauseScreen != null) {
            pauseScreen.resize(width, height);
        }
        if (savingWorldScreen != null) {
            savingWorldScreen.resize(width, height);
        }
        if (splashScreenRenderer != null) {
            splashScreenRenderer.resize(width, height);
        }
        if (chunkLoadingScreenRenderer != null) {
            chunkLoadingScreenRenderer.resize(width, height);
        }
        if (debugOverlay != null) {
            debugOverlay.resize(width, height);
        }
        if (tooltipRenderer != null) {
            tooltipRenderer.resize(width, height);
        }
        if (hotbarRenderer != null) {
            hotbarRenderer.resize(width, height);
        }
        if (inventoryRenderer != null) {
            inventoryRenderer.resize(width, height);
        }
        if (craftingTableRenderer != null) {
            craftingTableRenderer.resize(width, height);
        }

        if (session.isActive()) {
            session.resize(width, height);
        }
    }
    /**
     * Render everything. This is called 60 times per second.
     */
    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClear(GL20.GL_ALPHA_BITS);

        if (!initComplete.get()) {
            if (assetsQueued.get()) {
                boolean finished = textureManager.update();
                float progress = textureManager.getProgress();
                loadingState.setStep("Loading assets", String.format("Progress: %d%%", (int)(progress * 100)));

                if (finished) {
                    createRenderers();
                    initComplete.set(true);
                    loadingState.markComplete();
                    System.gc();

                    double elapsedTime = TimeUtils.nanoTime() - bootStartTime;
                    if (elapsedTime < 1_000_000.0) {
                        Logger.INFO("[BOOT] Bootstrap complete in: " + elapsedTime + " nanoseconds");
                    } else if (elapsedTime < 1_000_000_000.0) {
                        Logger.INFO("[BOOT] Bootstrap complete in: " + (elapsedTime / 1_000_000.0) + " milliseconds");
                    } else {
                        Logger.INFO("[BOOT] Bootstrap complete in: " + (elapsedTime / 1_000_000_000.0) + " seconds");
                    }
                }
            }
            splashScreenRenderer.render();
            return;
        }

        if (currentScreenState == ScreenState.SAVING_WORLD) {
            savingWorldScreen.render();
            if (pendingSaveTransition != null) {
                pendingSaveTransition.run();
                pendingSaveTransition = null;
            }
            return;
        }
        if (currentScreenState == ScreenState.WORLD_LOADING) {
            if (session.isActive()) {
                session.tickChunkLoading();
            }
            chunkLoadingScreenRenderer.render();
            return;
        }

        if (currentScreenState == ScreenState.TITLE) {
            titleScreen.draw();
            return;
        }

        if (currentScreenState == ScreenState.WORLD_SELECTION) {
            worldSelectionScreen.render();
            return;
        }

        if (currentScreenState == ScreenState.WORLD_CREATION) {
            worldCreationScreen.render();
            return;
        }

        if (currentScreenState == ScreenState.MORE_WORLD_OPTIONS) {
            moreWorldOptionsScreen.render();
            return;
        }

        if (currentScreenState.isPauseScreen()) {
            pauseScreen.render();
            return;
        }

        if (session.isActive()) {
            root.start();
            //tick (20)
            session.tick();
            session.renderWorld();

            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                if (currentScreenState.isScreenOpen()) {
                    closeCurrentScreen();
                } else if (currentScreenState.isPauseScreen()) {
                    currentScreenState = ScreenState.NONE;
                } else {
                    currentScreenState = ScreenState.PAUSE;
                }
            }

            tickSection.start();
            for (int i = 0; i < session.getTimer().elapsedTicks; i++) {
                if (InputHandler.isKeyJustPressed(Keys.E)) {
                    if (currentScreenState.isScreenOpen()) {
                        closeCurrentScreen();
                    } else if (!currentScreenState.isPauseScreen()) {
                        currentScreenState = ScreenState.INVENTORY;
                    }
                }
                if (Gdx.input.isKeyJustPressed(Keys.P)) {
                    SoundSystem.playSound("minecraft:block.amethyst.place2");
                }

            }
            tickSection.stop();

            hotbarRenderer.draw();

            if (currentScreenState.isScreenOpen()) {
                if (currentScreenState == ScreenState.INVENTORY) {
                    inventoryRenderer.draw();
                    session.renderPlayerInInventory();
                } else if (currentScreenState == ScreenState.CRAFTING_TABLE) {
                    craftingTableRenderer.draw();
                } else if (currentScreenState == ScreenState.FURNACE && session.getFurnaceRenderer() != null) {
                    session.getFurnaceRenderer().draw();
                } else if (currentScreenState == ScreenState.BARREL && session.getBarrelRenderer() != null) {
                    session.getBarrelRenderer().draw();
                }
            } else {
                session.renderPlayer();
            }

            root.stop();
            debugOverlay.render();
        }
    }
    //empty because we have to fulfil the contract, but we arent making an android game
    @Override
    public void pause() {
    }
    //empty because we have to fulfil the contract, but we arent making an android game

    @Override
    public void resume() {
    }
    //clean up the mess we've made in runtime.
    @Override
    public void dispose() {
        session.saveAndEnd();
        splashScreenRenderer.destroy();
        titleScreen.destroy();
        worldSelectionScreen.destroy();
        worldCreationScreen.destroy();
        moreWorldOptionsScreen.destroy();
        pauseScreen.destroy();
        savingWorldScreen.destroy();
        chunkLoadingScreenRenderer.destroy();
        textureManager.destroy();
        //tell the bg threads to close
        threads.shutdown();
        //force them to close if theyre being rude.
        threads.shutdownNow();
        debugOverlay.destroy();
        tooltipRenderer.destroy();
        hotbarRenderer.destroy();
        inventoryRenderer.destroy();
        craftingTableRenderer.destroy();
        Logger.INFO("Goodbye");
    }

    public void closeCurrentScreen() {
        if (currentScreenState == ScreenState.CRAFTING_TABLE) {
            craftingTableRenderer.onClose();
        } else if (currentScreenState == ScreenState.INVENTORY) {
            inventoryRenderer.onClose();
        }
        session.closeCurrentScreen();
    }

    public void openCraftingTable() {
        currentScreenState = ScreenState.CRAFTING_TABLE;
    }

    public void openFurnace(int x, int y) {
        session.openFurnace(x, y);
        currentScreenState = ScreenState.FURNACE;
    }

    public void openBarrel(int x, int y) {
        session.openBarrel(x, y);
        currentScreenState = ScreenState.BARREL;
    }

    public void openInventory() {
        currentScreenState = ScreenState.INVENTORY;
    }
    //begin
    public void startGame() {
        if (session.isActive()) {
            currentScreenState = ScreenState.NONE;
            return;
        }

        if (WorldSaveManager.hasWorlds()) {
            transitionToWorldSelection();
        } else {
            transitionToWorldCreation();
        }
    }

    public void transitionToTitle() {
        session.end();
        currentScreenState = ScreenState.TITLE;
    }

    public void transitionToWorldSelection() {
        session.end();
        currentScreenState = ScreenState.WORLD_SELECTION;
        worldSelectionScreen.refreshWorlds();
    }

    public void transitionToWorldCreation() {
        session.end();
        currentScreenState = ScreenState.WORLD_CREATION;
        worldCreationScreen.reset();
    }

    public void transitionToMoreWorldOptions() {
        currentScreenState = ScreenState.MORE_WORLD_OPTIONS;
        moreWorldOptionsScreen.reset();
    }

    public MoreWorldOptionsScreen getMoreWorldOptionsScreen() {
        return moreWorldOptionsScreen;
    }

    /**
     * Shows the saving screen, then runs a transition callback.
     */
    public void saveAndTransition(Runnable afterSave) {
        currentScreenState = ScreenState.SAVING_WORLD;
        pendingSaveTransition = () -> {
            session.saveAndEnd();
            afterSave.run();
        };
    }

    public void saveAndQuitToWorldSelection() {
        saveAndTransition(() -> {
            if (WorldSaveManager.hasWorlds()) {
                currentScreenState = ScreenState.WORLD_SELECTION;
                worldSelectionScreen.refreshWorlds();
            } else {
                currentScreenState = ScreenState.WORLD_CREATION;
                worldCreationScreen.reset();
            }
        });
    }

    /**
     * Starts asynchronous world creation and switches to loading screen.
     */
    public void createWorld(String worldName, long seed) {
        session.end();

        String folderName = WorldSaveManager.sanitizeFolderName(worldName);
        WorldSaveManager.createWorldFolder(folderName);

        session.setCreating(true);
        loadingState.reset();
        currentScreenState = ScreenState.WORLD_LOADING;

        threads.submit(() -> createWorldInternal(worldName, folderName, seed));
    }

    /**
     * Starts asynchronous world loading and switches to loading screen.
     */
    public void loadWorld(String folderName, String worldName) {
        session.end();

        session.setCreating(true);
        loadingState.reset();
        currentScreenState = ScreenState.WORLD_LOADING;

        threads.submit(() -> loadWorldInternal(folderName, worldName));
    }

    /**
     * Builds a new world, resolves spawn and starts a fresh session.
     */
    private void createWorldInternal(String worldName, String folderName, long seed) {
        World world;
        Player loadedPlayer;
        boolean starterInventory = moreWorldOptionsScreen != null && moreWorldOptionsScreen.isStarterInventoryEnabled();

        try {
            loadingState.setStep("Creating world", "Generating terrain");
            world = new World();
            world.seed = seed;
            world.setWorldInfo(worldName, folderName);
            world.setIsNewWorld(true);
            world.gameTime = 5000;
            if (moreWorldOptionsScreen != null) {
                world.worldData.randomTickSpeed = moreWorldOptionsScreen.getRandomTickSpeed();
                world.worldData.passiveMobSpawningEnabled = moreWorldOptionsScreen.isPassiveMobSpawningEnabled();
                world.worldData.hostileMobSpawningEnabled = moreWorldOptionsScreen.isHostileMobSpawningEnabled();
                world.worldData.passiveSpawnIntervalTicks = moreWorldOptionsScreen.getPassiveSpawnIntervalTicks();
                world.worldData.hostileSpawnIntervalTicks = moreWorldOptionsScreen.getHostileSpawnIntervalTicks();
                world.worldData.passiveMobCap = moreWorldOptionsScreen.getPassiveMobCap();
                world.worldData.hostileMobCap = moreWorldOptionsScreen.getHostileMobCap();
            }

            Noises.ensureInitialized();
        } catch (Exception e) {
            e.printStackTrace();
            loadingState.setStep("Error", "World creation failed");
            Gdx.app.postRunnable(() -> {
                session.end();
                currentScreenState = ScreenState.WORLD_SELECTION;
            });
            return;
        }

        try {
            loadingState.setStep("Spawning player", "Finding spawn point");
            SpawnCandidate candidate = findDeterministicSpawnCandidate(seed);
            Player tempPlayer = new Player(starterInventory);
            tempPlayer.x = candidate.x();
            tempPlayer.y = SharedConstants.CHUNK_HEIGHT - 4;
            tempPlayer.xo = tempPlayer.x;
            tempPlayer.yo = tempPlayer.y;
            tempPlayer.noPhysics = true;
            tempPlayer.bb = AABBPool.AABBpool.get(tempPlayer.x, tempPlayer.y, tempPlayer.x + 0.8, tempPlayer.y + 1.8);
            Logger.INFO("[WORLD] Spawn X candidate selected at x=%d after %d attempt(s)",
                candidate.x(), candidate.attempt());
            loadedPlayer = tempPlayer;
        } catch (Exception e) {
            Logger.ERROR("[WORLD] Failed to spawn player: %s", e.getMessage());
            e.printStackTrace();
            loadingState.setStep("Error", "Player spawn failed");
            Gdx.app.postRunnable(() -> {
                session.end();
                currentScreenState = ScreenState.WORLD_SELECTION;
            });
            return;
        }


        final World finalWorld = world;
        final Player finalPlayer = loadedPlayer;
        CountDownLatch sessionStarted = new CountDownLatch(1);
        Gdx.app.postRunnable(() -> {
            session.start(finalWorld, finalPlayer, worldName, folderName);
            session.getTimer().setStartTick(finalWorld.gameTime);
            currentScreenState = ScreenState.WORLD_LOADING;
            sessionStarted.countDown();
        });

        try {
            sessionStarted.await();
            loadingState.setStep("Spawning player", "Waiting for spawn chunk");
            int spawnY = waitForSpawnYFromGeneratedChunk(finalWorld, finalPlayer, 25_000L);
            CountDownLatch positioned = new CountDownLatch(1);
            Gdx.app.postRunnable(() -> {
                finalPlayer.y = spawnY;
                finalPlayer.yo = spawnY;
                finalPlayer.xo = finalPlayer.x;
                finalPlayer.yd = 0;
                finalPlayer.xd = 0;
                finalPlayer.noPhysics = false;
                finalPlayer.bb = AABBPool.AABBpool.get(finalPlayer.x, finalPlayer.y, finalPlayer.x + 0.8, finalPlayer.y + 1.8);
                Logger.INFO("[WORLD] Final spawn resolved at x=%d, y=%d", (int) finalPlayer.x, spawnY);
                currentScreenState = ScreenState.NONE;
                loadingState.markComplete();
                positioned.countDown();
            });
            positioned.await();
        } catch (Exception e) {
            Logger.ERROR("[WORLD] Failed while resolving spawn: %s", e.getMessage());
            e.printStackTrace();
            Gdx.app.postRunnable(() -> {
                currentScreenState = ScreenState.WORLD_SELECTION;
                loadingState.setStep("Error", "Player spawn failed");
            });
        }
    }

    /**
     * Loads saved world data and starts a playable session.
     */
    private void loadWorldInternal(String folderName, String worldName) {
        World loadedWorld;
        Player loadedPlayer;

        try {
            loadingState.setStep("Loading world", "Reading world data");
            Logger.INFO("Creating world...");
            loadedWorld = new World();

            Logger.INFO("Reading world data");
            CompoundTag worldData = WorldFile.readWorldData(folderName);
            if (worldData != null) {
                WorldFile.deserialiseWorld(loadedWorld, worldData);
            }
            loadedWorld.setWorldInfo(worldName, folderName);
            loadedWorld.setIsNewWorld(false);

            Noises.ensureInitialized();
        } catch (Exception e) {
            Logger.INFO("Something went wrong...");
            e.printStackTrace();
            loadingState.setStep("Error", "World loading failed");
            Gdx.app.postRunnable(() -> {
                session.end();
                currentScreenState = ScreenState.WORLD_SELECTION;
            });
            return;
        }

        try {
            loadingState.setStep("Spawning player", "Loading player data");
            Player tempPlayer = new Player(false);
            PlayerFile.readPlayerFromDisk(tempPlayer, folderName);
            loadedPlayer = tempPlayer;
        } catch (Exception e) {
            Logger.ERROR("[WORLD] Failed to load player: %s", e.getMessage());
            e.printStackTrace();
            Player tempPlayer = new Player(false);
            tempPlayer.x = 0;
            tempPlayer.y = 256;
            tempPlayer.xo = 0;
            tempPlayer.yo = 256;
            loadedPlayer = tempPlayer;
        }
        loadedPlayer.bb = AABBPool.AABBpool.get(loadedPlayer.x, loadedPlayer.y, loadedPlayer.x + 0.8, loadedPlayer.y + 1.8);

        final World finalWorld = loadedWorld;
        final Player finalPlayer = loadedPlayer;
        final long gameTick = loadedWorld.gameTime;
        Gdx.app.postRunnable(() -> {
            Logger.INFO("Starting session");
            session.start(finalWorld, finalPlayer, worldName, folderName);
            session.getTimer().setStartTick(gameTick);
            Logger.INFO("Session ready");
            currentScreenState = ScreenState.NONE;
            loadingState.markComplete();
        });
    }

    public boolean isScreenOpen() {
        return currentScreenState.isScreenOpen();
    }

    public boolean isInventoryOpen() {
        return currentScreenState.isInventoryOpen();
    }

    public static Minecraft getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Minecraft instance not initialised!");
        }
        return instance;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public int getTicksPerSecond() {
        return session.isActive() ? session.getTimer().getTicksPerSecond() : 0;
    }

    public long getTotalTicks() {
        return session.isActive() ? session.getTimer().totalTicks : 0;
    }

    /**
     * Finds a biome-safe X spawn candidate from deterministic sampling.
     */
    private SpawnCandidate findDeterministicSpawnCandidate(long seed) {
        VanillaNoiseStep noiseStep = VanillaNoiseStep.get(seed);
        int direction = (seed & 1L) == 0L ? 1 : -1;
        int step = 100;
        int maxAttempts = 10_000;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int worldX = attempt * step * direction;
            Biome biome = sampleBiomeAtX(noiseStep, worldX);
            if (!BiomeTags.HAS_TREES.contains(biome)) {
                continue;
            }

            return new SpawnCandidate(worldX, attempt);
        }

        return new SpawnCandidate(0, maxAttempts);
    }

    /**
     * Samples the biome at a world X position from climate noise.
     */
    private Biome sampleBiomeAtX(VanillaNoiseStep noiseStep, int worldX) {
        double continentalness = noiseStep.sampleContinentalnessAtBlock(worldX, 0, 0);
        double temperature = noiseStep.sampleTemperatureAtBlock(worldX, 0, 0);
        double humidity = noiseStep.sampleHumidityAtBlock(worldX, 0, 0);
        double weirdness = noiseStep.sampleWeirdnessAtBlock(worldX, 0, 0);
        double erosion = noiseStep.sampleErosionAtBlock(worldX, 0, 0);
        return BiomeManager.sampleBiome(continentalness, temperature, humidity, weirdness, erosion);
    }

    /**
     * Waits for a generated chunk and derives a safe spawn Y.
     */
    private int waitForSpawnYFromGeneratedChunk(World world, Player player, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        int worldX = (int) Math.floor(player.x);
        int chunkX = World.getChunkX(worldX);
        int localX = World.getLocalX(worldX);

        while (System.currentTimeMillis() < deadline) {
            Chunk chunk = world.getChunkIfExists(chunkX);
            if (chunk != null && chunk.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
                int scannedY = scanSpawnYFromChunk(chunk, localX);
                if (scannedY > 1) {
                    return scannedY;
                }
            }
            Thread.sleep(25L);
        }

        Chunk fallbackChunk = world.getChunkIfExists(chunkX);
        if (fallbackChunk != null) {
            int fallbackY = scanSpawnYFromChunk(fallbackChunk, localX);
            if (fallbackY > 1) {
                return fallbackY;
            }
        }
        return SharedConstants.CHUNK_HEIGHT - 4;
    }

    /**
     * Scans downward in a chunk for valid ground and headroom.
     */
    private int scanSpawnYFromChunk(Chunk chunk, int localX) {
        int y = SharedConstants.CHUNK_HEIGHT - 3;
        while (y > 2) {
            BlockState feet = chunk.getBlockAt(localX, y);
            BlockState head = chunk.getBlockAt(localX, y + 1);
            BlockState ground = chunk.getBlockAt(localX, y - 1);
            boolean feetClear = feet.getBlock().isAir() || BlockTags.FLUID.contains(feet.getBlock());
            boolean headClear = head.getBlock().isAir() || BlockTags.FLUID.contains(head.getBlock());
            boolean groundSolid = !ground.getBlock().isAir() && !BlockTags.FLUID.contains(ground.getBlock());
            if (groundSolid && feetClear && headClear) {
                return y;
            }
            y--;
        }
        return -1;
    }

    private record SpawnCandidate(int x, int attempt) {
    }
}
