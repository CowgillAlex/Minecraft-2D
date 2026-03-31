package dev.alexco.minecraft.blaze2d.gui.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.TextureManager;
import dev.alexco.minecraft.blaze2d.gui.container.ContainerRenderer;
import dev.alexco.minecraft.blaze2d.gui.inventory.InventorySlot.SlotType;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.level.block.FurnaceBlock;
import dev.alexco.minecraft.world.level.block.entity.FurnaceBlockEntity;

/**
 * The block entity is the source of truth - this is just a view.
 */
public class FurnaceRenderer extends ContainerRenderer {
    private static final boolean DEBUG_SLOTS = true;

    private TextureManager texRef;
    private Texture furnaceTexture;
    private Texture burnProgressTexture;
    private Texture litProgressTexture;
    private boolean initialised = false;
    private float scaling = 4f;
    private String texturePath = "textures/gui/furnace.png";
    private String burnProgressPath = "textures/gui/furnace/burn_progress.png";
    private String litProgressPath = "textures/gui/furnace/lit_progress.png";

    private InventoryGrid mainInventoryGrid;
    private InventorySlot inputSlot;
    private InventorySlot fuelSlot;
    private InventorySlot outputSlot;

    private int screenX;
    private int screenY;
    private ShapeRenderer shapeRenderer;

    private static final int FURNACE_INPUT_SLOT = 60;
    private static final int FURNACE_FUEL_SLOT = 61;
    private static final int FURNACE_OUTPUT_SLOT = 62;

    private int furnaceX;
    private int furnaceY;

    protected Player getPlayerRef() {
        return Minecraft.getInstance().getPlayer();
    }

    public FurnaceRenderer(int furnaceX, int furnaceY) {
        this.furnaceX = furnaceX;
        this.furnaceY = furnaceY;
    }

    public int getFurnaceX() {
        return furnaceX;
    }

    public int getFurnaceY() {
        return furnaceY;
    }

    @Override
    public void create() {
        texRef = Minecraft.getInstance().textureManager;
        furnaceTexture = texRef.get(texturePath);
        burnProgressTexture = texRef.get(burnProgressPath);
        litProgressTexture = texRef.get(litProgressPath);
        setupContainer("Furnace", Color.GRAY);

        if (DEBUG_SLOTS) {
            shapeRenderer = new ShapeRenderer();
        }
    }

    @Override
    protected void onContainerCreate() {
        initFurnaceGrid();
    }

    /**
     * Computes furnace gui layout positions and initialises player/furnace slots.
     */
    private void initFurnaceGrid() {
        if (initialised) return;

        int texWidth = furnaceTexture.getWidth();
        int texHeight = furnaceTexture.getHeight();

        screenX = Gdx.graphics.getWidth() / 2 - (texWidth * 2);
        screenY = Gdx.graphics.getHeight() / 2 - (texHeight * 2);

        mainInventoryGrid = new InventoryGrid(screenX + 40, screenY + 40);
        setupMainInventoryGrid();
        setupFurnaceSlots();

        initialised = true;
    }

    /**
     * Builds the 4x9 player inventory grid used in the furnace screen.
     */
    private void setupMainInventoryGrid() {
        float[] rowOffsets = {
            screenY,
            Gdx.graphics.getHeight() / 2 - (48 * 2) - 4,
            Gdx.graphics.getHeight() / 2 - (48 * 3.5f) - 4,
            Gdx.graphics.getHeight() / 2 - (48 * 5) - 4
        };

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = row * 9 + col;
                int x = screenX + 40 + (int)(48 * 1.5f * col);
                int y = (int)rowOffsets[row] + 40;

                mainInventoryGrid.addSlot(new InventorySlot(slotIndex, x, y, 48, 48,
                    InventorySlot.SlotType.REGULAR));
            }
        }
    }

    /**
     * Positions furnace input, fuel, and output slots on the gui texture.
     */
    private void setupFurnaceSlots() {
        int inputX = screenX + 233;
        int inputY = screenY + 540;
        inputSlot = new InventorySlot(FURNACE_INPUT_SLOT, inputX, inputY, 48, 48,
            SlotType.CRAFTING_INPUT);

        int fuelX = screenX + 233;
        int fuelY = screenY + 395;
        fuelSlot = new InventorySlot(FURNACE_FUEL_SLOT, fuelX, fuelY, 48, 48,
            SlotType.CRAFTING_INPUT);

        int outputX = screenX + 473;
        int outputY = screenY + 470;
        outputSlot = new InventorySlot(FURNACE_OUTPUT_SLOT, outputX, outputY, 48, 48,
            SlotType.CRAFTING_OUTPUT);
    }

    @Override
    protected void renderContainer() {
        FurnaceBlockEntity blockEntity = getFurnaceBlockEntity();

        spriteBatch.begin();

        spriteBatch.draw(furnaceTexture, screenX, screenY,
            furnaceTexture.getWidth() * scaling,
            furnaceTexture.getHeight() * scaling);

        for (InventorySlot slot : mainInventoryGrid.getSlots()) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);
            if (item != null) {
                drawItemWithAmount(item, slot.x, slot.y, slot.width, slot.height);
            }
        }

        if (blockEntity != null) {
            ItemStack inputItem = blockEntity.getStackInSlot(FurnaceBlockEntity.SLOT_INPUT);
            if (inputItem != null) {
                drawItemWithAmount(inputItem, inputSlot.x, inputSlot.y, inputSlot.width, inputSlot.height);
            }

            ItemStack fuelItem = blockEntity.getStackInSlot(FurnaceBlockEntity.SLOT_FUEL);
            if (fuelItem != null) {
                drawItemWithAmount(fuelItem, fuelSlot.x, fuelSlot.y, fuelSlot.width, fuelSlot.height);
            }

            ItemStack outputItem = blockEntity.getStackInSlot(FurnaceBlockEntity.SLOT_OUTPUT);
            if (outputItem != null) {
                drawItemWithAmount(outputItem, outputSlot.x, outputSlot.y, outputSlot.width, outputSlot.height);
            }

            drawProgressBars(blockEntity);
        }

        if (dragDropHandler.isDragging()) {
            ItemStack draggedItem = dragDropHandler.getHeldItem();
            if (draggedItem != null) {
                float dragX = dragDropHandler.getDragX() - (ITEM_RENDER_SIZE / 2f);
                float dragY = dragDropHandler.getDragY() - (ITEM_RENDER_SIZE / 2f);
                drawItemWithAmount(draggedItem,
                    dragX, dragY, ITEM_RENDER_SIZE, ITEM_RENDER_SIZE);
            }
        }

        spriteBatch.end();

        if (DEBUG_SLOTS) {
            renderDebugSlots();
        }
    }

    /**
     * Draws coloured debug outlines for inventory and furnace-specific slots.
     */
    private void renderDebugSlots() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.GREEN);
        for (InventorySlot slot : mainInventoryGrid.getSlots()) {
            shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
        }

        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(inputSlot.x, inputSlot.y, inputSlot.width, inputSlot.height);
        shapeRenderer.rect(fuelSlot.x, fuelSlot.y, fuelSlot.width, fuelSlot.height);

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(outputSlot.x, outputSlot.y, outputSlot.width, outputSlot.height);

        shapeRenderer.end();
    }

    /**
     * Draws cook-progress arrow and burn-progress flame from furnace state.
     */
    private void drawProgressBars(FurnaceBlockEntity blockEntity) {
        float cookProgress = blockEntity.getCookProgress();
        if (cookProgress > 0 && burnProgressTexture != null) {
            int arrowX = screenX + 320;
            int arrowY = screenY + 460;
            int fullWidth = burnProgressTexture.getWidth();
            int height = burnProgressTexture.getHeight();
            int partialWidth = (int) (fullWidth * cookProgress);

            spriteBatch.draw(
                burnProgressTexture,
                arrowX, arrowY,
                partialWidth * 4f, height * 4f,
                0, 0,
                partialWidth, height,
                false, false
            );
        }

        float burnProgress = blockEntity.getBurnProgress();
        if (burnProgress > 0 && litProgressTexture != null) {
            int fireX = screenX + 223;
            int fireY = screenY + 460;
            int fullWidth = litProgressTexture.getWidth();
            int fullHeight = litProgressTexture.getHeight();
            int partialHeight = (int) (fullHeight * burnProgress);

            spriteBatch.draw(
                litProgressTexture,
                fireX, fireY,
                fullWidth * 4f, partialHeight * 4f,
                0, fullHeight - partialHeight,
                fullWidth, partialHeight,
                false, false
            );
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (shapeRenderer != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
        }
        initialised = false;
        initFurnaceGrid();
    }

    @Override
    protected void onLeftClickPickup(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot == null) return;

        FurnaceBlockEntity blockEntity = getFurnaceBlockEntity();

        if (isFurnaceSlot(clickedSlot)) {
            int beSlot = getBlockEntitySlot(clickedSlot);
            if (blockEntity != null) {
                ItemStack item = blockEntity.getStackInSlot(beSlot);
                if (item != null) {
                    ItemStack pickup = new ItemStack(item.item, item.amount, item.damage);
                    dragDropHandler.pickupItem(pickup, clickedSlot.slotIndex);
                    blockEntity.setStackInSlot(beSlot, null);
                }
            }
        } else {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(clickedSlot.slotIndex);
            if (item != null) {
                dragDropHandler.pickupItem(item, clickedSlot.slotIndex);
                getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);
                updatePlayerHandItem();
            }
        }
    }

    @Override
    protected void onLeftClickDrop(int mouseX, int mouseY) {
        InventorySlot targetSlot = getSlotAt(mouseX, mouseY);
        ItemStack heldItem = dragDropHandler.getHeldItem();

        if (heldItem == null) return;

        if (targetSlot == null) {
            ItemStack dropped = dragDropHandler.dropItem();
            if (dropped != null) {
                dropItemToWorld(dropped);
            }
            return;
        }

        FurnaceBlockEntity blockEntity = getFurnaceBlockEntity();

        if (isFurnaceSlot(targetSlot)) {
            int beSlot = getBlockEntitySlot(targetSlot);

            if (blockEntity == null || !blockEntity.canPlaceItemInSlot(beSlot, heldItem)) {
                return;
            }

            ItemStack existing = blockEntity.getStackInSlot(beSlot);

            if (existing == null) {
                blockEntity.setStackInSlot(beSlot, new ItemStack(heldItem.item, heldItem.amount, heldItem.damage));
                dragDropHandler.dropItem();
            } else if (existing.item == heldItem.item) {
                int maxStack = heldItem.item.getMaxStackSize();
                int space = maxStack - existing.amount;
                if (space > 0) {
                    int toAdd = Math.min(heldItem.amount, space);
                    existing.amount += toAdd;
                    heldItem.amount -= toAdd;
                    if (heldItem.amount <= 0) {
                        dragDropHandler.dropItem();
                    }
                }
            } else {
                ItemStack swap = new ItemStack(existing.item, existing.amount, existing.damage);
                blockEntity.setStackInSlot(beSlot, new ItemStack(heldItem.item, heldItem.amount, heldItem.damage));
                dragDropHandler.pickupItem(swap, targetSlot.slotIndex);
            }
        } else {
            ItemStack existingItem = getPlayerRef().inventory.getItemAtSlot(targetSlot.slotIndex);

            if (existingItem == null) {
                getPlayerRef().inventory.setItemSlot(targetSlot.slotIndex,
                    new ItemStack(heldItem.item, heldItem.amount, heldItem.damage));
                dragDropHandler.dropItem();
            } else if (existingItem.item == heldItem.item) {
                int maxStack = heldItem.item.getMaxStackSize();
                int space = maxStack - existingItem.amount;
                if (space > 0) {
                    int toAdd = Math.min(heldItem.amount, space);
                    existingItem.amount += toAdd;
                    heldItem.amount -= toAdd;
                    if (heldItem.amount <= 0) {
                        dragDropHandler.dropItem();
                    }
                }
            }
            updatePlayerHandItem();
        }
    }

    @Override
    protected void onRightClickTakeHalf(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot == null) return;

        FurnaceBlockEntity blockEntity = getFurnaceBlockEntity();

        if (isFurnaceSlot(clickedSlot)) {
            int beSlot = getBlockEntitySlot(clickedSlot);
            if (blockEntity != null) {
                ItemStack item = blockEntity.getStackInSlot(beSlot);
                if (item != null && item.amount > 0) {
                    int half = (item.amount + 1) / 2;
                    int remaining = item.amount - half;

                    ItemStack pickup = new ItemStack(item.item, half, item.damage);
                    dragDropHandler.pickupItem(pickup, clickedSlot.slotIndex);

                    if (remaining > 0) {
                        item.amount = remaining;
                    } else {
                        blockEntity.setStackInSlot(beSlot, null);
                    }
                }
            }
        } else {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(clickedSlot.slotIndex);
            if (item != null && item.amount > 0) {
                int half = (item.amount + 1) / 2;
                int remaining = item.amount - half;

                ItemStack pickup = new ItemStack(item.item, half, item.damage);
                dragDropHandler.pickupItem(pickup, clickedSlot.slotIndex);

                if (remaining > 0) {
                    item.amount = remaining;
                } else {
                    getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);
                }
                updatePlayerHandItem();
            }
        }
    }

    @Override
    protected void onRightClickPlaceOne(int mouseX, int mouseY) {
        InventorySlot targetSlot = getSlotAt(mouseX, mouseY);
        ItemStack heldItem = dragDropHandler.getHeldItem();

        if (heldItem == null || targetSlot == null) return;

        FurnaceBlockEntity blockEntity = getFurnaceBlockEntity();

        if (isFurnaceSlot(targetSlot)) {
            int beSlot = getBlockEntitySlot(targetSlot);

            if (blockEntity == null || !blockEntity.canPlaceItemInSlot(beSlot, heldItem)) {
                return;
            }

            ItemStack existing = blockEntity.getStackInSlot(beSlot);

            if (existing == null) {
                blockEntity.setStackInSlot(beSlot, new ItemStack(heldItem.item, 1, heldItem.damage));
                heldItem.amount--;
                if (heldItem.amount <= 0) {
                    dragDropHandler.dropItem();
                }
            } else if (existing.item == heldItem.item) {
                int maxStack = heldItem.item.getMaxStackSize();
                if (existing.amount < maxStack) {
                    existing.amount++;
                    heldItem.amount--;
                    if (heldItem.amount <= 0) {
                        dragDropHandler.dropItem();
                    }
                }
            }
        } else {
            ItemStack existing = getPlayerRef().inventory.getItemAtSlot(targetSlot.slotIndex);

            if (existing == null) {
                getPlayerRef().inventory.setItemSlot(targetSlot.slotIndex,
                    new ItemStack(heldItem.item, 1, heldItem.damage));
                heldItem.amount--;
                if (heldItem.amount <= 0) {
                    dragDropHandler.dropItem();
                }
            } else if (existing.item == heldItem.item) {
                int maxStack = heldItem.item.getMaxStackSize();
                if (existing.amount < maxStack) {
                    existing.amount++;
                    heldItem.amount--;
                    if (heldItem.amount <= 0) {
                        dragDropHandler.dropItem();
                    }
                }
            }
            updatePlayerHandItem();
        }
    }

    private FurnaceBlockEntity getFurnaceBlockEntity() {
        return FurnaceBlock.getBlockEntity(furnaceX, furnaceY);
    }

    /**
     * Returns true when the slot belongs to the furnace input/fuel/output set.
     */
    private boolean isFurnaceSlot(InventorySlot slot) {
        return slot == inputSlot || slot == fuelSlot || slot == outputSlot;
    }

    /**
     * Maps a renderer slot reference to its block-entity slot index.
     */
    private int getBlockEntitySlot(InventorySlot slot) {
        if (slot == inputSlot) return FurnaceBlockEntity.SLOT_INPUT;
        if (slot == fuelSlot) return FurnaceBlockEntity.SLOT_FUEL;
        if (slot == outputSlot) return FurnaceBlockEntity.SLOT_OUTPUT;
        return -1;
    }

    /**
     * Resolves the interactive slot under the mouse across furnace and inventory grids.
     */
    protected InventorySlot getSlotAt(int mouseX, int mouseY) {
        if (inputSlot.isMouseOver(mouseX, mouseY)) return inputSlot;
        if (fuelSlot.isMouseOver(mouseX, mouseY)) return fuelSlot;
        if (outputSlot.isMouseOver(mouseX, mouseY)) return outputSlot;
        return mainInventoryGrid.getSlotAt(mouseX, mouseY);
    }

    public void onClose() {
        if (dragDropHandler.isDragging()) {
            ItemStack heldItem = dragDropHandler.dropItem();
            if (heldItem != null) {
                dropItemToWorld(heldItem);
            }
        }
    }

    @Override
    public void destroy() {
        if (DEBUG_SLOTS && shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        super.destroy();
    }

    @Override
    protected InventorySlot getHoveredSlot() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        return getSlotAt(mouseX, mouseY);
    }

    @Override
    protected ItemStack getItemStackAtSlot(InventorySlot slot) {
        if (slot == null) return null;

        if (isFurnaceSlot(slot)) {
            FurnaceBlockEntity blockEntity = getFurnaceBlockEntity();
            if (blockEntity != null) {
                int beSlot = getBlockEntitySlot(slot);
                return blockEntity.getStackInSlot(beSlot);
            }
            return null;
        }

        return getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);
    }

    private boolean isFurnaceSlotByIndex(int slotIndex) {
        return slotIndex == FURNACE_INPUT_SLOT ||
               slotIndex == FURNACE_FUEL_SLOT ||
               slotIndex == FURNACE_OUTPUT_SLOT;
    }

    @Override
    protected void handleShiftClick(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot == null) return;

        int slotIndex = clickedSlot.slotIndex;

        // Shift-click from furnace slots -> inventory
        if (isFurnaceSlotByIndex(slotIndex)) {
            FurnaceBlockEntity blockEntity = getFurnaceBlockEntity();
            if (blockEntity == null) return;

            int beSlot = -1;
            if (slotIndex == FURNACE_INPUT_SLOT) {
                beSlot = FurnaceBlockEntity.SLOT_INPUT;
            } else if (slotIndex == FURNACE_FUEL_SLOT) {
                beSlot = FurnaceBlockEntity.SLOT_FUEL;
            } else if (slotIndex == FURNACE_OUTPUT_SLOT) {
                beSlot = FurnaceBlockEntity.SLOT_OUTPUT;
            }

            if (beSlot == -1) return;

            ItemStack item = blockEntity.getStackInSlot(beSlot);
            if (item == null) return;
            boolean added = getPlayerRef().inventory.addItemToInventory(item);
            if (added) {
                blockEntity.setStackInSlot(beSlot, null);
                updatePlayerHandItem();
            }
            return;
        }

        // Shift-click armour item -> armour slot
        ItemStack item = getPlayerRef().inventory.getItemAtSlot(slotIndex);
        if (item != null && isArmorItem(item.item)) {
            int targetArmorSlot = getArmorSlotForItem(item.item);
            if (targetArmorSlot == -1) return;
            ItemStack armorInSlot = getPlayerRef().inventory.getItemAtSlot(targetArmorSlot);
            getPlayerRef().inventory.setItemSlot(targetArmorSlot, item);
            if (armorInSlot != null) {
                getPlayerRef().inventory.setItemSlot(slotIndex, armorInSlot);
            } else {
                getPlayerRef().inventory.setItemSlot(slotIndex, null);
            }
            playArmorEquipSound();
            updatePlayerHandItem();
            return;
        }

        // Shift-click inventory -> furnace
        if (isInventorySlot(slotIndex) || isHotbarSlot(slotIndex)) {
            item = getPlayerRef().inventory.getItemAtSlot(slotIndex);
            if (item == null) return;
            FurnaceBlockEntity blockEntity = getFurnaceBlockEntity();
            if (blockEntity == null) return;

            if (isFurnaceFuel(item.item)) {
                ItemStack fuelSlot = blockEntity.getStackInSlot(FurnaceBlockEntity.SLOT_FUEL);
                if (fuelSlot == null) {
                    blockEntity.setStackInSlot(FurnaceBlockEntity.SLOT_FUEL, item);
                    getPlayerRef().inventory.setItemSlot(slotIndex, null);
                } else if (fuelSlot.item == item.item && fuelSlot.amount < item.item.getMaxStackSize()) {
                    int space = item.item.getMaxStackSize() - fuelSlot.amount;
                    int toAdd = Math.min(item.amount, space);
                    fuelSlot.amount += toAdd;
                    item.amount -= toAdd;
                    if (item.amount <= 0) {
                        getPlayerRef().inventory.setItemSlot(slotIndex, null);
                    }
                }
            } else {
                ItemStack inputSlot = blockEntity.getStackInSlot(FurnaceBlockEntity.SLOT_INPUT);
                if (inputSlot == null) {
                    blockEntity.setStackInSlot(FurnaceBlockEntity.SLOT_INPUT, item);
                    getPlayerRef().inventory.setItemSlot(slotIndex, null);
                } else if (inputSlot.item == item.item && inputSlot.amount < item.item.getMaxStackSize()) {
                    int space = item.item.getMaxStackSize() - inputSlot.amount;
                    int toAdd = Math.min(item.amount, space);
                    inputSlot.amount += toAdd;
                    item.amount -= toAdd;
                    if (item.amount <= 0) {
                        getPlayerRef().inventory.setItemSlot(slotIndex, null);
                    }
                }
            }
            updatePlayerHandItem();
        }
    }

    @Override
    protected void handleOffhandSwap(int hoveredSlot) {
        ItemStack hoveredItem = getPlayerRef().inventory.getItemAtSlot(hoveredSlot);
        ItemStack offhandItem = getPlayerRef().inventory.getItemAtSlot(40);

        getPlayerRef().inventory.setItemSlot(40, hoveredItem);
        getPlayerRef().inventory.setItemSlot(hoveredSlot, offhandItem);

        if (hoveredSlot >= 0 && hoveredSlot <= 8) {
            if (offhandItem != null && offhandItem.item != null) {
                getPlayerRef().blockInHand = offhandItem.item;
            } else {
                getPlayerRef().blockInHand = dev.alexco.minecraft.world.level.item.Items.AIR;
            }
        } else {
            updatePlayerHandItem();
        }
    }
}
