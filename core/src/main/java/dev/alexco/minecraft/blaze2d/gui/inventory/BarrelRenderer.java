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
import dev.alexco.minecraft.world.level.block.BarrelBlock;
import dev.alexco.minecraft.world.level.block.entity.BarrelBlockEntity;

/**
 * Barrel screen renderer with 27-slot inventory (3 rows of 9).
 * The block entity is the source of truth - this is just a view.
 */
public class BarrelRenderer extends ContainerRenderer {
    private static final boolean DEBUG_SLOTS = true;

    private TextureManager texRef;
    private Texture barrelTexture;
    private boolean initialised = false;
    private float scaling = 4f;
    private String texturePath = "textures/gui/generic_27.png";

    private InventoryGrid mainInventoryGrid;
    private InventoryGrid barrelInventoryGrid;

    private int screenX;
    private int screenY;
    private ShapeRenderer shapeRenderer;


    // Player inventory uses 0-35, furnace uses 60-62, so we'll use 36-62 for barrel
    private static final int BARREL_SLOT_START = 36;

    protected Player getPlayerRef() {
        return Minecraft.getInstance().getPlayer();
    }

    private int barrelX;
    private int barrelY;

    public BarrelRenderer(int barrelX, int barrelY) {
        this.barrelX = barrelX;
        this.barrelY = barrelY;
    }

    public int getBarrelX() {
        return barrelX;
    }

    public int getBarrelY() {
        return barrelY;
    }

    @Override
    public void create() {
        texRef = Minecraft.getInstance().textureManager;
        barrelTexture = texRef.get(texturePath);
        setupContainer("Barrel", Color.GRAY);

        if (DEBUG_SLOTS) {
            shapeRenderer = new ShapeRenderer();
        }
    }

    @Override
    protected void onContainerCreate() {
        initBarrelGrid();
    }

    /**
     * Computes centred gui coordinates and initialises player/barrel slot grids.
     */
    private void initBarrelGrid() {
        if (initialised) return;

        int texWidth = barrelTexture.getWidth();
        int texHeight = barrelTexture.getHeight();

        screenX = Gdx.graphics.getWidth() / 2 - (texWidth * 2);
        screenY = Gdx.graphics.getHeight() / 2 - (texHeight * 2);

         mainInventoryGrid = new InventoryGrid(screenX + 40, screenY + 40);
        barrelInventoryGrid = new InventoryGrid(screenX + 40, screenY + 40);

        setupMainInventoryGrid();
        setupBarrelInventoryGrid();

        initialised = true;
    }

    /**
     * Builds the 4x9 player inventory slots shown below the barrel rows.
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
     * Builds the 3x9 barrel inventory slots mapped to block-entity storage.
     */
    private void setupBarrelInventoryGrid() {
        int startY =  screenY + (int)Math.floor((48 * 6.5) - 8 + 40 + 48); // Above player inventory

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = BARREL_SLOT_START + (row * 9 + col);
                int x = screenX + 40 + (int)(48 * 1.5f * col);
                int y = startY + (int)(48 * 1.5f * row);

                barrelInventoryGrid.addSlot(new InventorySlot(slotIndex, x, y, 48, 48,
                    SlotType.REGULAR));
            }
        }
    }

    /**
     * Draws gui texture, player/barrel items, dragged stack, and optional debug slot outlines.
     */
    @Override
    protected void renderContainer() {
        BarrelBlockEntity blockEntity = getBarrelBlockEntity();

        spriteBatch.begin();

         spriteBatch.draw(barrelTexture, screenX, screenY,
            barrelTexture.getWidth() * scaling,
            barrelTexture.getHeight() * scaling);

         for (InventorySlot slot : mainInventoryGrid.getSlots()) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);
            if (item != null) {
                drawItemWithAmount(item, slot.x, slot.y, slot.width, slot.height);
            }
        }

        if (blockEntity != null) {
            for (InventorySlot slot : barrelInventoryGrid.getSlots()) {
                int barrelSlotIndex = slot.slotIndex - BARREL_SLOT_START; // Convert to 0-26
                ItemStack item = blockEntity.getStackInSlot(barrelSlotIndex);
                if (item != null) {
                    drawItemWithAmount(item, slot.x, slot.y, slot.width, slot.height);
                }
            }
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

    private void renderDebugSlots() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.GREEN);
        for (InventorySlot slot : mainInventoryGrid.getSlots()) {
            shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
        }

        shapeRenderer.setColor(Color.CYAN);
        for (InventorySlot slot : barrelInventoryGrid.getSlots()) {
            shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (shapeRenderer != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
        }
        initialised = false;
        initBarrelGrid();
    }

    /**
     * Picks up a full stack from either barrel storage or player inventory.
     */
    @Override
    protected void onLeftClickPickup(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot == null) return;

        BarrelBlockEntity blockEntity = getBarrelBlockEntity();

        if (isBarrelSlot(clickedSlot)) {
            int barrelSlotIndex = clickedSlot.slotIndex - BARREL_SLOT_START;
            if (blockEntity != null) {
                ItemStack item = blockEntity.getStackInSlot(barrelSlotIndex);
                if (item != null) {
                    ItemStack pickup = new ItemStack(item.item, item.amount, item.damage);
                    dragDropHandler.pickupItem(pickup, clickedSlot.slotIndex);
                    blockEntity.setStackInSlot(barrelSlotIndex, null);
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

    /**
     * Places or merges the held stack into the clicked slot, dropping to world when clicked outside.
     */
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

        BarrelBlockEntity blockEntity = getBarrelBlockEntity();

        if (isBarrelSlot(targetSlot)) {
            int barrelSlotIndex = targetSlot.slotIndex - BARREL_SLOT_START;

            if (blockEntity == null) return;

            ItemStack existing = blockEntity.getStackInSlot(barrelSlotIndex);

            if (existing == null) {
                blockEntity.setStackInSlot(barrelSlotIndex,
                    new ItemStack(heldItem.item, heldItem.amount, heldItem.damage));
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
                blockEntity.setStackInSlot(barrelSlotIndex,
                    new ItemStack(heldItem.item, heldItem.amount, heldItem.damage));
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

    /**
     * Takes half of the clicked stack into the drag cursor.
     */
    @Override
    protected void onRightClickTakeHalf(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot == null) return;

        BarrelBlockEntity blockEntity = getBarrelBlockEntity();

        if (isBarrelSlot(clickedSlot)) {
            int barrelSlotIndex = clickedSlot.slotIndex - BARREL_SLOT_START;
            if (blockEntity != null) {
                ItemStack item = blockEntity.getStackInSlot(barrelSlotIndex);
                if (item != null && item.amount > 0) {
                    int half = (item.amount + 1) / 2;
                    int remaining = item.amount - half;

                    ItemStack pickup = new ItemStack(item.item, half, item.damage);
                    dragDropHandler.pickupItem(pickup, clickedSlot.slotIndex);

                    if (remaining > 0) {
                        item.amount = remaining;
                    } else {
                        blockEntity.setStackInSlot(barrelSlotIndex, null);
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

    /**
     * Places one item from the held stack into the target slot if valid.
     */
    @Override
    protected void onRightClickPlaceOne(int mouseX, int mouseY) {
        InventorySlot targetSlot = getSlotAt(mouseX, mouseY);
        ItemStack heldItem = dragDropHandler.getHeldItem();

        if (heldItem == null || targetSlot == null) return;

        BarrelBlockEntity blockEntity = getBarrelBlockEntity();

        if (isBarrelSlot(targetSlot)) {
            int barrelSlotIndex = targetSlot.slotIndex - BARREL_SLOT_START;

            if (blockEntity == null) return;

            ItemStack existing = blockEntity.getStackInSlot(barrelSlotIndex);

            if (existing == null) {
                blockEntity.setStackInSlot(barrelSlotIndex,
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

    private BarrelBlockEntity getBarrelBlockEntity() {
        return BarrelBlock.getBlockEntity(barrelX, barrelY);
    }

    private boolean isBarrelSlot(InventorySlot slot) {
        return slot.slotIndex >= BARREL_SLOT_START &&
               slot.slotIndex < BARREL_SLOT_START + BarrelBlockEntity.INVENTORY_SIZE;
    }

    /**
     * Resolves the interactive slot at cursor position across barrel then player grids.
     */
    protected InventorySlot getSlotAt(int mouseX, int mouseY) {
        for (InventorySlot slot : barrelInventoryGrid.getSlots()) {
            if (slot.isMouseOver(mouseX, mouseY)) return slot;
        }
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

        if (isBarrelSlot(slot)) {
            BarrelBlockEntity blockEntity = getBarrelBlockEntity();
            if (blockEntity != null) {
                int barrelSlotIndex = slot.slotIndex - BARREL_SLOT_START;
                return blockEntity.getStackInSlot(barrelSlotIndex);
            }
            return null;
        }

        return getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);
    }

    /**
     * Handles quick-transfer rules between barrel storage and player inventory.
     */
    @Override
    protected void handleShiftClick(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot == null) return;

        int slotIndex = clickedSlot.slotIndex;

        // Shift-click from barrel -> inventory
        if (isBarrelSlotByIndex(slotIndex)) {
            BarrelBlockEntity blockEntity = getBarrelBlockEntity();
            if (blockEntity == null) return;
            int barrelSlotIndex = slotIndex - BARREL_SLOT_START;
            ItemStack item = blockEntity.getStackInSlot(barrelSlotIndex);
            if (item == null) return;
            boolean added = getPlayerRef().inventory.addItemToInventory(item);
            if (added) {
                blockEntity.setStackInSlot(barrelSlotIndex, null);
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

        // Shift-click inventory -> barrel
        if (isInventorySlot(slotIndex) || isHotbarSlot(slotIndex)) {
            item = getPlayerRef().inventory.getItemAtSlot(slotIndex);
            if (item == null) return;
            BarrelBlockEntity blockEntity = getBarrelBlockEntity();
            if (blockEntity == null) return;
            for (int i = 0; i < BarrelBlockEntity.INVENTORY_SIZE; i++) {
                if (blockEntity.getStackInSlot(i) == null) {
                    blockEntity.setStackInSlot(i, item);
                    getPlayerRef().inventory.setItemSlot(slotIndex, null);
                    updatePlayerHandItem();
                    return;
                }
            }
            for (int i = 0; i < BarrelBlockEntity.INVENTORY_SIZE; i++) {
                ItemStack existing = blockEntity.getStackInSlot(i);
                if (existing != null && existing.item == item.item && existing.amount < item.item.getMaxStackSize()) {
                    int space = item.item.getMaxStackSize() - existing.amount;
                    int toAdd = Math.min(item.amount, space);
                    existing.amount += toAdd;
                    item.amount -= toAdd;
                    if (item.amount <= 0) {
                        getPlayerRef().inventory.setItemSlot(slotIndex, null);
                    }
                    updatePlayerHandItem();
                    return;
                }
            }
        }
    }

    /**
     * Swaps hovered inventory slot contents with the offhand slot.
     */
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

    private boolean isBarrelSlotByIndex(int slotIndex) {
        return slotIndex >= BARREL_SLOT_START &&
               slotIndex < BARREL_SLOT_START + BarrelBlockEntity.INVENTORY_SIZE;
    }
}
