package dev.alexco.minecraft.world.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.SharedConstants;
import dev.alexco.minecraft.blaze2d.SelectorRenderer;
import dev.alexco.minecraft.input.InputHandler;
import dev.alexco.minecraft.inventory.Inventory;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.loot.ToolInfo;
import dev.alexco.minecraft.loot.LootTableManager;
import dev.alexco.minecraft.loot.ToolType;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.sound.SoundSystem;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.util.Mth;
import dev.alexco.minecraft.util.Direction;
import dev.alexco.minecraft.world.World;
import dev.alexco.minecraft.world.level.block.BarrelBlock;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.block.Blocks;
import dev.alexco.minecraft.world.level.block.DoorBlock;
import dev.alexco.minecraft.world.level.block.FurnaceBlock;
import dev.alexco.minecraft.world.level.block.OakSlabBlock;
import dev.alexco.minecraft.world.level.block.OakStairsBlock;
import dev.alexco.minecraft.world.level.block.state.properties.BlockStateProperties;
import dev.alexco.minecraft.world.level.chunk.ChunkPos;
import dev.alexco.minecraft.world.level.item.BlockItem;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.Items;
import dev.alexco.minecraft.world.level.item.ToolItem;
import dev.alexco.registry.ResourceLocation;

import static dev.alexco.minecraft.tag.BlockTags.CAN_PLACE_THROUGH;

import java.util.List;
import dev.alexco.minecraft.world.entity.Cow;
import dev.alexco.minecraft.world.entity.Zombie;

public class Player extends Entity {
    public Item blockInHand = Items.AIR;
    public static Player myself;
    public Inventory inventory;
    public int slotSelected; // this goes from 1-9
    private Direction facingDirection = Direction.RIGHT;


    private boolean isSprinting = false;
    private long lastAPressTick = -100;
    private long lastDPressTick = -100;
    private static final long DOUBLE_TAP_WINDOW = 10; // Ticks between double-tap
    private static final float SPRINT_MULTIPLIER = 1.5f;
    private long lastRightPlaceTick = -1;
    private long lastStepSoundTick = -100;
    private static final long STEP_SOUND_DELAY = 5;

    public Player() {
        this(true);
    }

    public Player(boolean starterInventory) {
        super(new ResourceLocation("player"));
        this.bbWidth = 0.8f;
        this.bbHeight = 1.8f;
        this.stepHeight = 0.6f;
        myself = this;
        heightOffset = 0.5f;
        this.inventory = new Inventory();

        if (starterInventory) {
            addStarterInventory();
        }

        this.slotSelected = 1;
        if (inventory.getItemAtSlot(slotSelected - 1) != null) {

            blockInHand = inventory.getItemAtSlot(slotSelected - 1).item;
        } else {
            blockInHand = Items.AIR;
        }
    }

    /**
     * Populates debug starter items for new players.
     */
    private void addStarterInventory() {
        inventory.addItemToInventory(new BlockItem(
                Blocks.TEST.defaultBlockState().setValue(BlockStateProperties.TEST, true), new Item.Properties()));
       inventory.addItemToInventory(new BlockItem(Blocks.OAK_LOG.defaultBlockState(), new Item.Properties()));
       inventory.addItemToInventory(new BlockItem(Blocks.OAK_LEAVES.defaultBlockState(), new Item.Properties()));
       inventory.addItemToInventory(new BlockItem(Blocks.JUNGLE_LEAVES.defaultBlockState(), new Item.Properties()));
       inventory.addItemToInventory(new BlockItem(Blocks.OAK_SLAB.defaultBlockState(), new Item.Properties()));
       inventory.addItemToInventory(Items.BARREL);
       inventory.addItemToInventory(Items.FURNACE);
       inventory.addItemToInventory(Items.RAW_IRON);
       inventory.addItemToInventory(Items.RAW_IRON);
       inventory.addItemToInventory(Items.RAW_IRON);
       inventory.addItemToInventory(Items.COAL);
       inventory.addItemToInventory(Items.COAL);
       inventory.addItemToInventory(Items.COAL);
       inventory.addItemToInventory(Items.ZOMBIE_SPAWN_EGG);
       inventory.addItemToInventory(Items.ZOMBIE_SPAWN_EGG);
       inventory.addItemToInventory(Items.ZOMBIE_SPAWN_EGG);
       inventory.addItemToInventory(Items.ZOMBIE_SPAWN_EGG);
       inventory.addItemToInventory(Items.ZOMBIE_SPAWN_EGG);
       inventory.addItemToInventory(Items.ZOMBIE_SPAWN_EGG);
       inventory.addItemToInventory(Items.ZOMBIE_SPAWN_EGG);
       inventory.addItemToInventory(Items.ZOMBIE_SPAWN_EGG);
         inventory.addItemToInventory(Items.COW_SPAWN_EGG);
         inventory.addItemToInventory(Items.COW_SPAWN_EGG);
         inventory.addItemToInventory(Items.COW_SPAWN_EGG);
         inventory.addItemToInventory(Items.COW_SPAWN_EGG);
         inventory.addItemToInventory(Items.COW_SPAWN_EGG);
         inventory.addItemToInventory(Items.COW_SPAWN_EGG);
         inventory.addItemToInventory(Items.COW_SPAWN_EGG);
         inventory.addItemToInventory(Items.COW_SPAWN_EGG);
         inventory.addItemToInventory(Items.SPAWNER);
         inventory.addItemToInventory(Items.SPAWNER);
         inventory.addItemToInventory(Items.SPAWNER);
         inventory.addItemToInventory(Items.SPAWNER);
         inventory.addItemToInventory(Items.SPAWNER);
         inventory.addItemToInventory(Items.SPAWNER);
       inventory.addItemToInventory(Items.OAK_DOOR_ITEM);
       inventory.addItemToInventory(Items.STONE_PICKAXE);
       inventory.addItemToInventory(Items.IRON_AXE);
       inventory.addItemToInventory(Items.DIAMOND_AXE);
       inventory.addItemToInventory(Items.IRON_PICKAXE);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /**
     * Handles player input, movement, interaction and combat each tick.
     */
    @Override
    public void tick(float delta) {
           if (InputHandler.isKeyJustPressed(Keys.P)) {
                    SoundSystem.playSound("minecraft:block.amethyst.place" + (MathUtils.random.nextInt(3) + 1));
                }
      super.tick(delta);
        Minecraft.getInstance().getWorld().getChunk(World.getChunkX(this.x));
        Minecraft.getInstance().getWorld().getChunk(World.getChunkX(this.x) + 1);
        Minecraft.getInstance().getWorld().getChunk(World.getChunkX(this.x) - 1);

        float xa = 0.0F;
        float ya = 0.0F;
        if (!Minecraft.getInstance().isScreenOpen()) {
            for (int i = Keys.NUM_1; i < Keys.NUM_9+1; i++){

                if (InputHandler.isKeyJustPressed(i)){
                    slotSelected = i-7;
                     if (inventory.getItemAtSlot(slotSelected - 1) != null) {

                        blockInHand = inventory.getItemAtSlot(slotSelected - 1).item;
                    } else {
                        blockInHand = Items.AIR;
                    }
                }
            }


            // sprint
            if (InputHandler.isKeyJustPressed(Keys.A)) {
                long currentTick = Minecraft.getInstance().getSession().getTimer().totalTicks;
                if (currentTick - lastAPressTick <= DOUBLE_TAP_WINDOW) {
                    isSprinting = true;
                }
                lastAPressTick = currentTick;
            }
            if (InputHandler.isKeyJustPressed(Keys.D)) {
                long currentTick = Minecraft.getInstance().getSession().getTimer().totalTicks;
                if (currentTick - lastDPressTick <= DOUBLE_TAP_WINDOW) {
                    isSprinting = true;
                }
                lastDPressTick = currentTick;
            }

            if (isSprinting && (!InputHandler.isKeyDown(Keys.A) && !InputHandler.isKeyDown(Keys.D))) {
                isSprinting = false;
            }

            float moveSpeed = isSprinting ? 0.8f * SPRINT_MULTIPLIER : 0.8f;

            if (InputHandler.isKeyDown(Keys.A)) {
                xa -= moveSpeed;
                facingDirection = Direction.LEFT;
            }
            if (InputHandler.isKeyDown(Keys.D)) {
                xa += moveSpeed;
                facingDirection = Direction.RIGHT;
            }
            if (InputHandler.isKeyDown(Keys.S))
                ya -= moveSpeed;
            if (InputHandler.isKeyDown(Keys.W)) {
                if (amIinWater()) {

                    yd += 0.12f;
                } else if (onGround) {

                    yd += 0.8f;
                }
            }
            if (InputHandler.isKeyDown(Keys.I))

                yd += 10000.8f;
            if (InputHandler.isKeyDown(Keys.K))

                yd -= 10000.8f;
            if (InputHandler.isKeyDown(Keys.J))

                xd -= 10000.8f;
            if (InputHandler.isKeyDown(Keys.L))

                xd += 10000.8f;

            if (InputHandler.isKeyDown(Keys.LEFT)) {
                xd -= 0.8f;
            }
            if (InputHandler.isKeyDown(Keys.RIGHT)) {
                xd += 0.8f;
            }
            if (InputHandler.isKeyDown(Keys.DOWN))
                yd -= 0.8f;
            if (InputHandler.isKeyDown(Keys.UP))
                yd += 0.8f;
            double scroll = InputHandler.getScrollDeltaY();

            if (InputHandler.isKeyDown(Keys.CONTROL_LEFT)) {
                Minecraft.getInstance().getWorld().worldData.blockSize -= scroll * 16f;
                if (Minecraft.getInstance().getWorld().worldData.blockSize < 8) {
                    Minecraft.getInstance().getWorld().worldData.blockSize = 8f;
                }
            } else {
                if (scroll > 0) {
                    slotSelected++;
                    if (slotSelected == 10) {
                        slotSelected = 1;
                    }
                    if (inventory.getItemAtSlot(slotSelected - 1) != null) {

                        blockInHand = inventory.getItemAtSlot(slotSelected - 1).item;
                    } else {
                        blockInHand = Items.AIR;
                    }
                } else if (scroll < -0) {
                    slotSelected--;
                    if (slotSelected == 0) {
                        slotSelected = 9;
                    }
                    if (inventory.getItemAtSlot(slotSelected - 1) != null) {

                        blockInHand = inventory.getItemAtSlot(slotSelected - 1).item;
                    } else {
                        blockInHand = Items.AIR;
                    }
                }

            }

        }
        if (InputHandler.isKeyJustPressed(Keys.NUM_0)) {
            System.gc();
        }
        if (SelectorRenderer.worldMouseY > 0 && SelectorRenderer.worldMouseY < SharedConstants.CHUNK_HEIGHT - 1) {

            handleMouse();
        }

        boolean inWater = amIinWater();

        if (!Minecraft.getInstance().isScreenOpen()) {
            noPhysics = InputHandler.isKeyDown(Keys.C);

            // Handle F key for offhand swap when not in inventory
            if (InputHandler.isKeyJustPressed(Keys.F)) {
                int selectedSlot = slotSelected - 1; // Convert 1-9 to 0-8
                ItemStack hotbarItem = inventory.getItemAtSlot(selectedSlot);
                ItemStack offhandItem = inventory.getItemAtSlot(40);

                inventory.setItemSlot(40, hotbarItem);
                inventory.setItemSlot(selectedSlot, offhandItem);

                // Update hand item - should be what is NOW in the selected hotbar slot (the offhand item)
                if (offhandItem != null && offhandItem.item != null) {
                    blockInHand = offhandItem.item;
                } else {
                    blockInHand = dev.alexco.minecraft.world.level.item.Items.AIR;
                }
            }
        }
            if (this.onGround) {
                ya = 0;
            }

            float speedMultiplier = this.onGround ? 0.1f : 0.02f;
            if (isSprinting) {
                speedMultiplier *= 1.5f;
            }
            moveRelative(xa, ya, speedMultiplier);

              if (!noPhysics){//i hate gravity

                if (inWater) {
                    //fall slow in water
                    this.yd -= 0.0057f;
                } else {
                    this.yd -= 0.157f;
                }
            }
            this.yd *= 0.91F;

            move(this.xd, this.yd);
            this.xd *= 0.91F;
            if (this.onGround) {
                this.xd *= 0.7F;
            }
        if (InputHandler.isKeyJustPressed(Keys.RIGHT_BRACKET)) {
            xo = Mth.nearestPowerOfTwo((long) xo + 1, false); // go to next power of 2
            this.bb.set(xo, yo, xo + 0.8f, yo + 1.8f);

            Minecraft.getInstance().getWorld().getChunk(World.getChunkX(this.x));
            Minecraft.getInstance().getWorld().getChunk(World.getChunkX(this.x) + 1);
            Minecraft.getInstance().getWorld().getChunk(World.getChunkX(this.x) - 1);
        }
        if (InputHandler.isKeyJustPressed(Keys.LEFT_BRACKET)) {
            xo = Mth.nearestPowerOfTwo((long) xo - 1, true); // go to next power of 2
            this.bb.set(xo, yo, xo + 0.8f, yo + 1.8f);

            Minecraft.getInstance().getWorld().getChunk(World.getChunkX(this.x));
            Minecraft.getInstance().getWorld().getChunk(World.getChunkX(this.x) + 1);
            Minecraft.getInstance().getWorld().getChunk(World.getChunkX(this.x) - 1);
        }

    }

    // @Override
    // public void move(double xa, double ya) {
    // this.bb.move(0.0F, ya);

    // this.bb.move(xa, 0.0F);
    // this.x += xa;
    // this.y += ya;
    // }

    public int lastWorldMouseX = 0;
    public int lastWorldMouseY = 0;
    public static BlockState blockStateUnderPlayerHand = Blocks.AIR.defaultBlockState();

    /**
     * Resets hold-interaction state when target selection changes.
     */
    private void cancel() {
        InputHandler.mouseHeldDownFor = 0;
        lastWorldMouseX = SelectorRenderer.worldMouseX;
        lastWorldMouseY = SelectorRenderer.worldMouseY;
        blockStateUnderPlayerHand = Blocks.AIR.defaultBlockState();
    }

    @SuppressWarnings("null")
    /**
     * Processes mouse mining, placing and block/entity interactions.
     */
    private void handleMouse() throws IndexOutOfBoundsException {
        World world = Minecraft.getInstance().getWorld();
        blockStateUnderPlayerHand = world.getBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY);
        if (InputHandler.isKeyDown(Keys.CONTROL_LEFT) && InputHandler.isButtonDown(Buttons.LEFT)) {
            Minecraft.getInstance().getWorld().entities.clear();
        }

        if (SelectorRenderer.worldMouseY < 0 || SelectorRenderer.worldMouseY > SharedConstants.CHUNK_HEIGHT) {

            cancel();
            return;
        }
        // Keep mining behaviour stable (same block), but allow right-click drag placement.
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (lastWorldMouseX != SelectorRenderer.worldMouseX) {
                cancel();
                return;
            }
            if (lastWorldMouseY != SelectorRenderer.worldMouseY) {
                cancel();
                return;
            }
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (Minecraft.getInstance().isScreenOpen()) {
                InputHandler.mouseHeldDownFor = 0; // we need to reset it to stop players from using it when the
                                                   // inventory is open
                return;
            }
            if (InputHandler.isButtonJustPressed(Buttons.LEFT) && attackMobUnderCursor(world)) {
                InputHandler.mouseHeldDownFor = 0;
                SoundSystem.playSound("damage.hit"+ (MathUtils.random.nextInt(2) + 1));

                return;
            }
            // we now set the block
            Block blockUnderPlayerHand = blockStateUnderPlayerHand.getBlock();
            if (BlockTags.UNBREAKABLE.contains(blockUnderPlayerHand)) {
                InputHandler.mouseHeldDownFor = 0;
                return;
            }
            boolean canHarvest = ToolItem.canHarvest(blockInHand, blockUnderPlayerHand);
            float toolSpeed = ToolItem.getMiningSpeed(blockInHand, blockUnderPlayerHand);
            double miningTime = blockUnderPlayerHand.calculateMiningTime(
                    canHarvest,
                    toolSpeed,
                    0,
                    false,
                    0,
                    false,
                    0,
                    false,
                    false,
                    onGround);
            if (miningTime == -1) {
                InputHandler.mouseHeldDownFor = 0;
                return;
            }
            long currentTick = Minecraft.getInstance().getSession().getTimer().totalTicks;
            if (currentTick - lastStepSoundTick >= STEP_SOUND_DELAY) {
                SoundSystem.playSound("step.stone" + (MathUtils.random.nextInt(5) + 1));
                lastStepSoundTick = currentTick;
            }

            if (InputHandler.mouseHeldDownFor > miningTime) {
                // dev.alexco.minecraft.util.Logger.INFO("The block under me is %s",
                // blockUnderPlayerHand.getKey());
                // dev.alexco.minecraft.util.Logger.INFO("The item I should be is %s",
                // Registry.ITEM.get(blockUnderPlayerHand.getKey()));
                // if (!blockUnderPlayerHand.getKey().toString()
                // .equals(Registry.ITEM.get(blockUnderPlayerHand.getKey()).toString())) {
                // // Logger.ERROR("Mismatch between block and item for %s. Block form: %s, item
                // form: %s",
                // // blockUnderPlayerHand.getKey(), blockUnderPlayerHand.getKey(),
                // // Registry.ITEM.get(blockUnderPlayerHand.getKey()));
                // }
                if (blockUnderPlayerHand instanceof DoorBlock) {
                    breakDoorPair(world, SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY, blockStateUnderPlayerHand);
                } else {
                    //block entities have got extra data to be disposed of
                    if (blockUnderPlayerHand instanceof FurnaceBlock) {
                        ((FurnaceBlock) blockUnderPlayerHand).onRemove(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY);
                    }
                    if (blockUnderPlayerHand instanceof BarrelBlock) {
                        ((BarrelBlock) blockUnderPlayerHand).onRemove(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY);
                    }
                    List<ItemStack> drops = LootTableManager.getDrops(blockStateUnderPlayerHand, blockInHand);
                    double dropX = SelectorRenderer.worldMouseX + 0.5D;
                    double dropY = SelectorRenderer.worldMouseY;
                    for (ItemStack drop : drops) {
                        for (int i = 0; i < drop.amount; i++) {
                            if (drop.item instanceof BlockItem blockItem) {
                                Minecraft.getInstance().getWorld().entities.add(new BlockItemEntity(blockItem,
                                        dropX, dropY));
                            } else {
                                Minecraft.getInstance().getWorld().entities.add(new ItemEntity(drop.item,
                                        dropX, dropY));
                            }
                        }
                    }
                    if (blockStateUnderPlayerHand.getBlock().doIChangeStateForBg()) {
                        BlockState toSet = blockStateUnderPlayerHand.getBlock().defaultBackgroundBlockState();
                        world.setBackgroundBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY, toSet);
                    }
                    world.setBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY, Blocks.AIR.defaultBlockState());
                                        SoundSystem.playSound("dig.stone" + (MathUtils.random.nextInt(3) + 1));

                    world.getChunk(new ChunkPos(World.getChunkX(SelectorRenderer.worldMouseX))).build();
                }
                damageHeldTool();
                InputHandler.mouseHeldDownFor = 0;
            }

        }

        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            if (Minecraft.getInstance().isScreenOpen())
                return;

            if (InputHandler.isButtonJustPressed(Buttons.RIGHT)){
            if (handleSpawnEggInteraction(world)) {
                return;
            }
            if (interactCowUnderCursor(world)) {
                return;
            }
            // Check for crafting table interaction
            if (blockStateUnderPlayerHand.getBlock().equals(Blocks.CRAFTING_TABLE)){
                Minecraft.getInstance().openCraftingTable();
                return;
            }


            if (blockStateUnderPlayerHand.getBlock().equals(Blocks.FURNACE)){
                Minecraft.getInstance().openFurnace(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY);
                return;
            }
            if ((blockStateUnderPlayerHand.getBlock().equals(Blocks.WATER) || blockStateUnderPlayerHand.getBlock().equals(Blocks.FLOWING_WATER))
                    && blockInHand.equals(Items.EMPTY_BUCKET)) {
                ItemStack selectedStack = inventory.getItemAtSlot(slotSelected - 1);
                if (selectedStack != null && selectedStack.item.equals(Items.EMPTY_BUCKET)) {
                    world.setBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY, Blocks.AIR.defaultBlockState());
                    world.getChunk(new ChunkPos(World.getChunkX(SelectorRenderer.worldMouseX))).build();

                    if (selectedStack.amount <= 1) {
                        inventory.setItemSlot(slotSelected - 1, new ItemStack(Items.WATER_BUCKET, 1));
                        blockInHand = Items.WATER_BUCKET;
                    } else {
                        selectedStack.amount--;
                        inventory.setItemSlot(slotSelected - 1, selectedStack);
                        inventory.addItemToInventory(Items.WATER_BUCKET);
                        blockInHand = selectedStack.item;
                    }
                }
                return;
            }
            if (blockStateUnderPlayerHand.getBlock().equals(Blocks.BARREL)){
                Minecraft.getInstance().openBarrel(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY);
                return;
            }

                if (blockStateUnderPlayerHand.getBlock().equals(Blocks.OAK_DOOR)){
                    toggleDoorPair(world, SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY, blockStateUnderPlayerHand);
                    return;
                }

                if (blockInHand.equals(Items.WATER_BUCKET) && CAN_PLACE_THROUGH.contains(blockStateUnderPlayerHand.getBlock())) {
                    world.setBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY, Blocks.WATER.defaultBlockState());
                    world.getChunk(new ChunkPos(World.getChunkX(SelectorRenderer.worldMouseX))).build();
                    inventory.setItemSlot(slotSelected - 1, new ItemStack(Items.EMPTY_BUCKET, 1));
                    blockInHand = Items.EMPTY_BUCKET;
                    return;
                }

                ToolInfo toolInfo = ToolInfo.fromItem(blockInHand);
                if (toolInfo.getType() == ToolType.HOE && BlockTags.FARMABLE_BLOCKS.contains(blockStateUnderPlayerHand.getBlock())) {
                    BlockState aboveTarget = world.getBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY + 1);
                    if (!aboveTarget.getBlock().isAir()) {
                        return;
                    }
                    if (BlockTags.DROPS_GRASS_ON_HOE.contains(blockStateUnderPlayerHand.getBlock())) {
                        List<ItemStack> drops = LootTableManager.getDropsFromTable(
                            new ResourceLocation("minecraft", "gameplay/grass_on_hoe"),
                            blockStateUnderPlayerHand,
                            blockInHand
                        );
                        for (ItemStack drop : drops) {
                            for (int i = 0; i < drop.amount; i++) {
                                if (drop.item instanceof BlockItem blockItem) {
                                    world.entities.add(new BlockItemEntity(blockItem, SelectorRenderer.worldMouseX + 0.5D, SelectorRenderer.worldMouseY + 1));
                                } else {
                                    world.entities.add(new ItemEntity(drop.item, SelectorRenderer.worldMouseX + 0.5D, SelectorRenderer.worldMouseY + 1));
                                }
                            }
                        }
                    }
                    world.setBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY, Blocks.FARMLAND.defaultBlockState());
                    world.getChunk(new ChunkPos(World.getChunkX(SelectorRenderer.worldMouseX))).build();
                    return;
                }

                if (blockInHand.equals(Items.WHEAT_SEEDS)
                        && blockStateUnderPlayerHand.getBlock().isAir()
                        && SelectorRenderer.worldMouseY > 0
                        && world.getBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY - 1).getBlock().equals(Blocks.FARMLAND)) {
                    world.setBlock(
                        SelectorRenderer.worldMouseX,
                        SelectorRenderer.worldMouseY,
                        Blocks.WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE, 0)
                    );
                    ItemStack seedStack = inventory.getItemAtSlot(slotSelected - 1);
                    if (seedStack != null) {
                        seedStack.amount--;
                        if (seedStack.amount <= 0) {
                            inventory.setItemSlot(slotSelected - 1, null);
                            blockInHand = Items.AIR;
                        } else {
                            inventory.setItemSlot(slotSelected - 1, seedStack);
                            blockInHand = seedStack.item;
                        }
                    }
                    world.getChunk(new ChunkPos(World.getChunkX(SelectorRenderer.worldMouseX))).build();
                    return;
                }
            }
            if (!CAN_PLACE_THROUGH.contains(blockStateUnderPlayerHand.getBlock()))
                return;
            if (blockInHand.equals(Items.OAK_DOOR_ITEM)) {
                long currentTick = Minecraft.getInstance().getSession().getTimer().totalTicks;
                if (currentTick == lastRightPlaceTick) {
                    return;
                }
                lastRightPlaceTick = currentTick;
                placeDoorFromItem(world, SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY);
                return;
            }
            if (blockInHand instanceof BlockItem && !((BlockItem) blockInHand).getBlock().isAir()) {
                long currentTick = Minecraft.getInstance().getSession().getTimer().totalTicks;
                if (currentTick == lastRightPlaceTick) {
                    return;
                }

                BlockState placeState = ((BlockItem) blockInHand).getBlockState();
                if (placeState.getBlock() instanceof OakSlabBlock) {
                    double blockSize = world.worldData.blockSize;
                    double worldY = (Gdx.graphics.getHeight() - Gdx.input.getY()) + world.worldData.cameraY;
                    double localY = (worldY / blockSize) - Math.floor(worldY / blockSize);
                    boolean topHalf = localY > 0.5;
                    placeState = placeState.setValue(BlockStateProperties.TOP, topHalf);
                } else if (placeState.getBlock() instanceof OakStairsBlock) {
                    double blockSize = world.worldData.blockSize;
                    double worldY = (Gdx.graphics.getHeight() - Gdx.input.getY()) + world.worldData.cameraY;
                    double localY = (worldY / blockSize) - Math.floor(worldY / blockSize);
                    boolean topHalf = localY > 0.5;
                    Direction stairFacing = SelectorRenderer.worldMouseX < this.x ? Direction.LEFT : Direction.RIGHT;
                    placeState = placeState
                        .setValue(BlockStateProperties.TOP, topHalf)
                        .setValue(BlockStateProperties.FACING, stairFacing);
                }
                if (BlockTags.LEAVES.contains(placeState.getBlock())
                        && placeState.hasProperty(BlockStateProperties.PERSISTENT)) {
                    placeState = placeState.setValue(BlockStateProperties.PERSISTENT, true);
                }

                if (BlockTags.NEEDS_BASE_SUPPORT.contains(placeState.getBlock())) {
                    if (SelectorRenderer.worldMouseY <= 0) {
                        return;
                    }
                    BlockState below = world.getBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY - 1);
                    if (below.getBlock().isAir() || BlockTags.FLUID.contains(below.getBlock())) {
                        return;
                    }
                }

                world.setBlock(SelectorRenderer.worldMouseX, SelectorRenderer.worldMouseY,
                        placeState);
                lastRightPlaceTick = currentTick;
                ItemStack stack = inventory.getItemAtSlot(slotSelected - 1);
                stack.amount--;

                if (stack.amount <= 0) {
                    blockInHand = Items.AIR;
                    inventory.setItemSlot(slotSelected - 1, null); // null means that the slot is completely empty.
                } else {
                    inventory.setItemSlot(slotSelected - 1, stack);
                }
                world.getChunk(World.getChunkX(SelectorRenderer.worldMouseX)).setDirty(true);
                world.getChunk(new ChunkPos(World.getChunkX(SelectorRenderer.worldMouseX))).build();
            }
        }

        lastWorldMouseX = SelectorRenderer.worldMouseX;

        lastWorldMouseY = SelectorRenderer.worldMouseY;
    }

    public static Player getPlayer() {
        return myself;
    }

    /**
     * Applies durability loss to the selected tool and handles breakage.
     */
    private void damageHeldTool() {
        int selectedSlot = slotSelected - 1;
        ItemStack selectedStack = inventory.getItemAtSlot(selectedSlot);
        if (selectedStack == null || !selectedStack.item.canBeDepleted()) {
            return;
        }

        selectedStack.damage++;
        if (selectedStack.damage >= selectedStack.item.getMaxDamage()) {
            inventory.setItemSlot(selectedSlot, null);
            blockInHand = Items.AIR;
        } else {
            inventory.setItemSlot(selectedSlot, selectedStack);
            blockInHand = selectedStack.item;
        }
    }

    /**
     * Finds and damages the nearest valid mob under the cursor.
     */
    private boolean attackMobUnderCursor(World world) {
        LivingEntity best = null;
        double bestDistSq = Double.MAX_VALUE;
        double cursorX = SelectorRenderer.worldMouseX + 0.5D;
        double cursorY = SelectorRenderer.worldMouseY + 0.5D;

        for (Entity entity : world.entities) {
            if (!(entity instanceof LivingEntity living) || entity == this || living.removed || living.getHealth() <= 0f) {
                continue;
            }
            if (living.bb == null) {
                continue;
            }
            boolean cursorInBox = cursorX >= living.bb.x0 && cursorX <= living.bb.x1
                && cursorY >= living.bb.y0 && cursorY <= living.bb.y1;
            if (!cursorInBox) {
                continue;
            }
            double dx = living.x - this.x;
            double dy = living.y - this.y;
            double distSq = dx * dx + dy * dy;
            if (distSq > 16.0) {
                continue;
            }
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = living;
            }
        }

        if (best == null) {
            return false;
        }

        best.hurt(getAttackDamageFromHeldItem(), this);
        float knockback = (float) Math.signum(best.x - this.x) * 0.25f;
        if (knockback == 0.0f) {
            knockback = this.facingDirection == Direction.LEFT ? -0.25f : 0.25f;
        }
        best.xd += knockback;
        best.yd += 0.08f;
        damageHeldTool();
        return true;
    }

    /**
     * Computes base melee damage from the held item.
     */
    private float getAttackDamageFromHeldItem() {
        if (blockInHand instanceof ToolItem toolItem) {
            return Math.max(0.5f, toolItem.getAttackDamage());
        }
        return 1.0f;
    }

    /**
     * Handles wheat breeding interaction for cows under the cursor.
     */
    private boolean interactCowUnderCursor(World world) {
        if (!(blockInHand.equals(Items.WHEAT_ITEM) || blockInHand.equals(Items.WHEAT))) {
            return false;
        }

        Cow target = findCowUnderCursor(world);
        if (target == null || target.isBaby() || !target.canBreed()) {
            return false;
        }

        if (!consumeOneHeldItem()) {
            return false;
        }
        target.feedForBreeding();

        Cow mate = findBreedingMate(world, target);
        if (mate == null) {
            return true;
        }

        double babyX = (target.x + mate.x) * 0.5D;
        double babyY = Math.max(target.y, mate.y);
        world.entities.add(new Cow(babyX, babyY, true));
        target.onBreedComplete();
        mate.onBreedComplete();
        return true;
    }

    /**
     * Handles spawn egg usage on entities or empty placement targets.
     */
    private boolean handleSpawnEggInteraction(World world) {
        if (!(blockInHand.equals(Items.COW_SPAWN_EGG) || blockInHand.equals(Items.ZOMBIE_SPAWN_EGG) || blockInHand.equals(Items.PIG_SPAWN_EGG))) {
            return false;
        }

        LivingEntity target = findLivingEntityUnderCursor(world);
        if (target != null) {
            Entity baby = createBabyFromEggTarget(target);
            if (baby != null && consumeOneHeldItem()) {
                world.entities.add(baby);
                return true;
            }
            return false;
        }

        if (!CAN_PLACE_THROUGH.contains(blockStateUnderPlayerHand.getBlock())) {
            return false;
        }
        Entity spawned = createAdultFromEgg(blockInHand, SelectorRenderer.worldMouseX + 0.5D, SelectorRenderer.worldMouseY);
        if (spawned == null) {
            return false;
        }
        if (!consumeOneHeldItem()) {
            return false;
        }
        world.entities.add(spawned);
        return true;
    }

    /**
     * Finds the closest living entity under the cursor within range.
     */
    private LivingEntity findLivingEntityUnderCursor(World world) {
        LivingEntity best = null;
        double bestDistSq = Double.MAX_VALUE;
        double cursorX = SelectorRenderer.worldMouseX + 0.5D;
        double cursorY = SelectorRenderer.worldMouseY + 0.5D;

        for (Entity entity : world.entities) {
            if (!(entity instanceof LivingEntity living) || entity == this || living.removed || living.bb == null) {
                continue;
            }
            boolean cursorInBox = cursorX >= living.bb.x0 && cursorX <= living.bb.x1
                && cursorY >= living.bb.y0 && cursorY <= living.bb.y1;
            if (!cursorInBox) {
                continue;
            }
            double dx = living.x - this.x;
            double dy = living.y - this.y;
            double distSq = dx * dx + dy * dy;
            if (distSq > 64.0) {
                continue;
            }
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = living;
            }
        }

        return best;
    }

    /**
     * Creates an adult mob instance matching a spawn egg item.
     */
    private Entity createAdultFromEgg(Item egg, double x, double y) {
        if (egg.equals(Items.COW_SPAWN_EGG)) {
            return new Cow(x, y, false);
        }
        if (egg.equals(Items.ZOMBIE_SPAWN_EGG)) {
            return new Zombie(x, y, false, false);
        }
        if (egg.equals(Items.PIG_SPAWN_EGG)) {
            return new Pig(x, y, false);
        }
        return null;
    }

    /**
     * Creates a baby mob variant when using a matching egg on a parent.
     */
    private Entity createBabyFromEggTarget(LivingEntity target) {
        if (target instanceof Cow && blockInHand.equals(Items.COW_SPAWN_EGG)) {
            return new Cow(target.x + 0.2D, target.y, true);
        }
        if (target instanceof Zombie && blockInHand.equals(Items.ZOMBIE_SPAWN_EGG)) {
            return new Zombie(target.x + 0.2D, target.y, false, true);
        }
        if (target instanceof Pig && blockInHand.equals(Items.PIG_SPAWN_EGG)) {
            return new Pig(target.x + 0.2D, target.y, true);
        }
        return null;
    }

    /**
     * Finds the nearest cow under the cursor within interaction range.
     */
    private Cow findCowUnderCursor(World world) {
        Cow best = null;
        double bestDistSq = Double.MAX_VALUE;
        double cursorX = SelectorRenderer.worldMouseX + 0.5D;
        double cursorY = SelectorRenderer.worldMouseY + 0.5D;

        for (Entity entity : world.entities) {
            if (!(entity instanceof Cow cow) || cow.removed || cow.bb == null) {
                continue;
            }
            boolean cursorInBox = cursorX >= cow.bb.x0 && cursorX <= cow.bb.x1
                && cursorY >= cow.bb.y0 && cursorY <= cow.bb.y1;
            if (!cursorInBox) {
                continue;
            }
            double dx = cow.x - this.x;
            double dy = cow.y - this.y;
            double distSq = dx * dx + dy * dy;
            if (distSq > 64.0) {
                continue;
            }
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = cow;
            }
        }
        return best;
    }

    /**
     * Finds a nearby cow that is also in love mode for breeding.
     */
    private Cow findBreedingMate(World world, Cow source) {
        for (Entity entity : world.entities) {
            if (!(entity instanceof Cow other) || other == source || other.removed) {
                continue;
            }
            if (other.isBaby() || !other.canBreed() || other.getLoveTicks() <= 0) {
                continue;
            }
            double dx = other.x - source.x;
            double dy = other.y - source.y;
            if ((dx * dx + dy * dy) <= 6.0 * 6.0) {
                return other;
            }
        }
        return null;
    }

    /**
     * Consumes one item from the selected hotbar slot.
     */
    private boolean consumeOneHeldItem() {
        int selectedSlot = slotSelected - 1;
        ItemStack selectedStack = inventory.getItemAtSlot(selectedSlot);
        if (selectedStack == null || selectedStack.amount <= 0) {
            return false;
        }
        selectedStack.amount--;
        if (selectedStack.amount <= 0) {
            inventory.setItemSlot(selectedSlot, null);
            blockInHand = Items.AIR;
        } else {
            inventory.setItemSlot(selectedSlot, selectedStack);
            blockInHand = selectedStack.item;
        }
        return true;
    }

    /**
     * Toggles open state for both halves of a door.
     */
    private void toggleDoorPair(World world, int x, int y, BlockState doorState) {
        boolean newOpen = !doorState.getValue(BlockStateProperties.OPEN);
        boolean isTop = doorState.getValue(BlockStateProperties.TOP);
        int otherY = isTop ? y - 1 : y + 1;

        world.setBlock(x, y, doorState.setValue(BlockStateProperties.OPEN, newOpen));

        if (otherY >= 0 && otherY < SharedConstants.CHUNK_HEIGHT) {
            BlockState otherState = world.getBlock(x, otherY);
            if (otherState.getBlock().equals(Blocks.OAK_DOOR)) {
                world.setBlock(x, otherY, otherState.setValue(BlockStateProperties.OPEN, newOpen));
            }
        }

        world.getChunk(World.getChunkX(x)).setDirty(true);
        world.getChunk(new ChunkPos(World.getChunkX(x))).build();
    }

    /**
     * Breaks both halves of a door block pair.
     */
    private void breakDoorPair(World world, int x, int y, BlockState doorState) {
        int bottomY = doorState.getValue(BlockStateProperties.TOP) ? y - 1 : y;
        int topY = bottomY + 1;

        if (bottomY >= 0 && bottomY < SharedConstants.CHUNK_HEIGHT) {
            // world.setBackgroundBlock(x, bottomY, Blocks.AIR.defaultBlockState());
            world.setBlock(x, bottomY, Blocks.AIR.defaultBlockState());
        }
        if (topY >= 0 && topY < SharedConstants.CHUNK_HEIGHT) {
            // world.setBackgroundBlock(x, topY, Blocks.AIR.defaultBlockState());
            world.setBlock(x, topY, Blocks.AIR.defaultBlockState());
        }

        world.getChunk(World.getChunkX(x)).setDirty(true);
        world.getChunk(new ChunkPos(World.getChunkX(x))).build();
    }

    /**
     * Places a two-block door if target and support rules are valid.
     */
    private void placeDoorFromItem(World world, int x, int y) {
        if (y < 0 || y >= SharedConstants.CHUNK_HEIGHT - 1) {
            return;
        }
        if (y <= 0) {
            return;
        }
        BlockState target = world.getBlock(x, y);
        BlockState above = world.getBlock(x, y + 1);
        BlockState below = world.getBlock(x, y - 1);
        if (!CAN_PLACE_THROUGH.contains(target.getBlock()) || !CAN_PLACE_THROUGH.contains(above.getBlock())) {
            return;
        }
        if (below.getBlock().isAir() || BlockTags.FLUID.contains(below.getBlock())) {
            return;
        }

        BlockState bottom = Blocks.OAK_DOOR.defaultBlockState()
            .setValue(BlockStateProperties.TOP, false)
            .setValue(BlockStateProperties.OPEN, false);
        BlockState top = Blocks.OAK_DOOR.defaultBlockState()
            .setValue(BlockStateProperties.TOP, true)
            .setValue(BlockStateProperties.OPEN, false);

        world.setBlock(x, y, bottom);
        world.setBlock(x, y + 1, top);

        ItemStack stack = inventory.getItemAtSlot(slotSelected - 1);
        if (stack != null) {
            stack.amount--;
            if (stack.amount <= 0) {
                blockInHand = Items.AIR;
                inventory.setItemSlot(slotSelected - 1, null);
            } else {
                inventory.setItemSlot(slotSelected - 1, stack);
            }
        }

        world.getChunk(World.getChunkX(x)).setDirty(true);
        world.getChunk(new ChunkPos(World.getChunkX(x))).build();
    }
}
