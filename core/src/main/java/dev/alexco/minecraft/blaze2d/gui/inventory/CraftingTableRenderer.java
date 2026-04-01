package dev.alexco.minecraft.blaze2d.gui.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.TextureManager;
import dev.alexco.minecraft.blaze2d.gui.container.ContainerRenderer;
import dev.alexco.minecraft.blaze2d.gui.inventory.InventorySlot.SlotType;
import dev.alexco.minecraft.crafting.CraftingInput;
import dev.alexco.minecraft.crafting.CraftingManager;
import dev.alexco.minecraft.crafting.Recipe;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Crafting Table screen renderer with 3x3 crafting grid.
 * Extends ContainerRenderer for drag-drop functionality.
 */
public class CraftingTableRenderer extends ContainerRenderer {
    private static final boolean DEBUG_SLOTS = true;

    private TextureManager texRef;
    private Texture craftingTableTexture;
    private boolean initialised = false;
    private float scaling = 4f;
    private String texturePath = "textures/gui/crafting_table.png";

    // Slot grids
    private InventoryGrid mainInventoryGrid;    // Main 36 inventory slots
    private InventoryGrid craftingInputGrid;    // 3x3 crafting input (slots 50-58)
    private InventorySlot craftingOutputSlot;   // Output slot (slot 45)

    // Position tracking
    private int screenX;
    private int screenY;
    private ShapeRenderer shapeRenderer;

    // Slot index mappings for 3x3 crafting grid
    private static final int[] CRAFTING_INPUT_SLOTS = {50, 51, 52, 53, 54, 55, 56, 57, 58};
    private static final int CRAFTING_OUTPUT_SLOT = 45;

    @Override
    public void create() {
        texRef = Minecraft.getInstance().textureManager;
        craftingTableTexture = texRef.get(texturePath);
        setupContainer("CraftingTable", Color.ORANGE);

        if (DEBUG_SLOTS) {
            shapeRenderer = new ShapeRenderer();
        }
    }

    @Override
    protected void onContainerCreate() {
        initCraftingTableGrid();
    }

    /**
     * Initialises all screen slot grids and computes centred gui placement.
     */
    private void initCraftingTableGrid() {
        if (initialised) return;

        int texWidth = craftingTableTexture.getWidth();
        int texHeight = craftingTableTexture.getHeight();

        // Calculate centred position
        screenX = Gdx.graphics.getWidth() / 2 - (texWidth * 2);
        screenY = Gdx.graphics.getHeight() / 2 - (texHeight * 2);

        // Create main inventory grid (4 rows x 9 columns)
        // Positioned at the bottom portion of the screen
        mainInventoryGrid = new InventoryGrid(screenX + 40, screenY + 40);
        setupMainInventoryGrid();

        // Create 3x3 crafting input grid
        craftingInputGrid = new InventoryGrid(0, 0);
        setupCraftingInputGrid();

        // Create crafting output slot
        setupCraftingOutputSlot();

        initialised = true;
    }

    /**
     * Builds the main 4x9 player inventory slots shown at the bottom.
     */
    private void setupMainInventoryGrid() {
        // Main inventory: 4 rows of 9 slots
        // Slot indices 0-35
        // Positioned at the bottom of the crafting table GUI
        // Based on crafting_table.png texture (176x166 at 1x, scaled by 4)
        int texHeightScaled = craftingTableTexture.getHeight() * 4;
        int baseY = screenY;  // Base position of the crafting table GUI

        // Row positions from bottom to top (relative to crafting table texture)
        // The inventory area starts around y=84 in the original texture
        float[] rowOffsets = {
            baseY,
            Gdx.graphics.getHeight() / 2 - (48 * 2) - 4,
            Gdx.graphics.getHeight() / 2 - (48 * 3.5f) - 4,
            Gdx.graphics.getHeight() / 2 - (48 * 5) - 4
        };

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = row * 9 + col;
                int x = screenX + 40 + (int)(48 * 1.5f * col);
                int y = (int)rowOffsets[row]+40;

                mainInventoryGrid.addSlot(new InventorySlot(slotIndex, x, y, 48, 48,
                    InventorySlot.SlotType.REGULAR));
            }
        }
    }

    /**
     * Builds the 3x3 crafting input grid mapped to slots 50-58.
     */
    private void setupCraftingInputGrid() {
        // 3x3 crafting input grid using slots 50-58
        // Positioned in the upper area of the crafting table GUI
        // Based on the provided CraftingScreen.java coordinates

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = 50 + (row * 3) + col;
                // Adjusted coordinates to match the crafting table texture layout
                int x = screenX + 32 + 24 + (int)(48 * 1.5f * (col + 1));
                int y = screenY + 330 + 32 + 32 + (int)(48 * 1.5f * row);

                craftingInputGrid.addSlot(new InventorySlot(slotIndex, x, y, 48, 48,
                    InventorySlot.SlotType.CRAFTING_INPUT));
            }
        }
    }

    /**
     * Positions the crafting output slot to the right of the input grid.
     */
    private void setupCraftingOutputSlot() {
        // Crafting output slot (index 45)
        // Positioned to the right of the 3x3 grid
        int outputX = screenX + 36 + (int)(48 * 1.5f * 6.5f);
        int outputY = screenY + 330 + 64 + (int)(48 * 1.5f);

        craftingOutputSlot = new InventorySlot(CRAFTING_OUTPUT_SLOT, outputX, outputY, 48, 48,
            InventorySlot.SlotType.CRAFTING_OUTPUT);
    }

    @Override
    protected void renderContainer() {
        spriteBatch.begin();

        // Draw background texture
        spriteBatch.draw(craftingTableTexture, screenX, screenY,
            craftingTableTexture.getWidth() * scaling,
            craftingTableTexture.getHeight() * scaling);

        // Draw main inventory items
        for (InventorySlot slot : mainInventoryGrid.getSlots()) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);
            if (item != null) {
                drawItemWithAmount(item, slot.x, slot.y, slot.width, slot.height);
            }
        }

        // Draw crafting input items
        for (InventorySlot slot : craftingInputGrid.getSlots()) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);
            if (item != null) {
                drawItemWithAmount(item, slot.x, slot.y, slot.width, slot.height);
            }
        }

        // Draw crafting output item
        ItemStack outputItem = getPlayerRef().inventory.getItemAtSlot(craftingOutputSlot.slotIndex);
        if (outputItem != null) {
            drawItemWithAmount(outputItem,
                craftingOutputSlot.x, craftingOutputSlot.y,
                craftingOutputSlot.width, craftingOutputSlot.height);
        }

        // Draw dragged item
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

        // Debug: Draw slot outlines
        if (DEBUG_SLOTS && shapeRenderer != null && Minecraft.getInstance().isDebugHudVisible()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            shapeRenderer.setColor(Color.GREEN);
            for (InventorySlot slot : mainInventoryGrid.getSlots()) {
                shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
            }

            shapeRenderer.setColor(Color.YELLOW);
            for (InventorySlot slot : craftingInputGrid.getSlots()) {
                shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
            }

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(craftingOutputSlot.x, craftingOutputSlot.y,
                craftingOutputSlot.width, craftingOutputSlot.height);

            shapeRenderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (shapeRenderer != null) {
            shapeRenderer.setProjectionMatrix(camera.combined);
        }
        initialised = false;
        initCraftingTableGrid();
    }

    @Override
    protected void onLeftClickPickup(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot != null) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(clickedSlot.slotIndex);
            if (item != null) {
                // Special handling for crafting output slot
                if (clickedSlot.slotIndex == CRAFTING_OUTPUT_SLOT) {
                    dragDropHandler.pickupItem(item, clickedSlot.slotIndex);
                    getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);
                    consumeCraftingIngredients();
                    updatePlayerHandItem();
                    return;
                }

                dragDropHandler.pickupItem(item, clickedSlot.slotIndex);
                getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);

                // Update crafting output if removed from crafting grid
                if (clickedSlot.type == SlotType.CRAFTING_INPUT) {
                    updateCraftingOutput();
                }
                updatePlayerHandItem();
            }
        }
    }

    @Override
    protected void onLeftClickDrop(int mouseX, int mouseY) {
        InventorySlot targetSlot = getSlotAt(mouseX, mouseY);
        ItemStack heldItem = dragDropHandler.getHeldItem();

        if (targetSlot != null && heldItem != null) {
            // Prevent placing items into crafting output slot
            if (targetSlot.slotIndex == CRAFTING_OUTPUT_SLOT) {
                return;
            }

            ItemStack existingItem = getPlayerRef().inventory.getItemAtSlot(targetSlot.slotIndex);

            if (existingItem == null) {
                // Empty slot - place as much as possible
                int maxStackSize = heldItem.item.getMaxStackSize();
                int amountToPlace = Math.min(heldItem.amount, maxStackSize);

                ItemStack slotStack = new ItemStack(heldItem.item, amountToPlace, heldItem.damage);
                getPlayerRef().inventory.setItemSlot(targetSlot.slotIndex, slotStack);

                heldItem.amount -= amountToPlace;
                if (heldItem.amount <= 0) {
                    dragDropHandler.dropItem();
                }

                // Update crafting output if placed in crafting grid
                if (targetSlot.type == SlotType.CRAFTING_INPUT) {
                    updateCraftingOutput();
                }
                updatePlayerHandItem();
            } else if (existingItem.item == heldItem.item) {
                // Same item type - try to stack
                int maxStackSize = existingItem.item.getMaxStackSize();
                int spaceAvailable = maxStackSize - existingItem.amount;

                if (spaceAvailable > 0) {
                    int amountToAdd = Math.min(heldItem.amount, spaceAvailable);
                    existingItem.amount += amountToAdd;
                    heldItem.amount -= amountToAdd;

                    if (heldItem.amount <= 0) {
                        dragDropHandler.dropItem();
                    }
                }
                updatePlayerHandItem();
            } else {
                // Different item type - swap
                ItemStack heldCopy = new ItemStack(heldItem.item, heldItem.amount, heldItem.damage);
                dragDropHandler.pickupItem(existingItem, targetSlot.slotIndex);
                getPlayerRef().inventory.setItemSlot(targetSlot.slotIndex, heldCopy);

                // Update crafting output if placed in crafting grid
                if (targetSlot.type == SlotType.CRAFTING_INPUT) {
                    updateCraftingOutput();
                }
                updatePlayerHandItem();
            }
        } else if (targetSlot == null && heldItem != null) {
            // Clicked outside - drop into world
            ItemStack droppedItem = dragDropHandler.dropItem();
            dropItemToWorld(droppedItem);
            updatePlayerHandItem();
        }
    }

    @Override
    protected void onRightClickTakeHalf(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot != null) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(clickedSlot.slotIndex);
            if (item != null && item.amount > 0) {
                // Special handling for crafting output slot - take full stack like left click
                if (clickedSlot.slotIndex == CRAFTING_OUTPUT_SLOT) {
                    dragDropHandler.pickupItem(item, clickedSlot.slotIndex);
                    getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);
                    consumeCraftingIngredients();
                    // Update crafting output to show next preview
                    updateCraftingOutput();
                    updatePlayerHandItem();
                    return;
                }

                // Calculate half (round up)
                int halfAmount = (item.amount + 1) / 2;
                int remainingAmount = item.amount - halfAmount;

                ItemStack handStack = new ItemStack(item.item, halfAmount, item.damage);
                dragDropHandler.pickupItem(handStack, clickedSlot.slotIndex);

                if (remainingAmount > 0) {
                    item.amount = remainingAmount;
                } else {
                    getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);
                }

                // Update crafting output if removed from crafting grid
                if (clickedSlot.type == SlotType.CRAFTING_INPUT) {
                    updateCraftingOutput();
                }
                updatePlayerHandItem();
            }
        }
    }

    @Override
    protected void onRightClickPlaceOne(int mouseX, int mouseY) {
        InventorySlot targetSlot = getSlotAt(mouseX, mouseY);
        ItemStack heldItem = dragDropHandler.getHeldItem();

        if (targetSlot != null && heldItem != null) {
            // Prevent placing items into crafting output slot
            if (targetSlot.slotIndex == CRAFTING_OUTPUT_SLOT) {
                return;
            }

            ItemStack existingItem = getPlayerRef().inventory.getItemAtSlot(targetSlot.slotIndex);

            if (existingItem == null) {
                // Empty slot - place one item
                ItemStack singleItem = new ItemStack(heldItem.item, 1, heldItem.damage);
                getPlayerRef().inventory.setItemSlot(targetSlot.slotIndex, singleItem);

                heldItem.amount--;
                if (heldItem.amount <= 0) {
                    dragDropHandler.dropItem();
                }

                // Update crafting output if placed in crafting grid
                if (targetSlot.type == SlotType.CRAFTING_INPUT) {
                    updateCraftingOutput();
                }
                updatePlayerHandItem();
            } else if (existingItem.item == heldItem.item) {
                // Same item type - try to add one
                int maxStackSize = existingItem.item.getMaxStackSize();
                if (existingItem.amount < maxStackSize) {
                    existingItem.amount++;
                    heldItem.amount--;
                    if (heldItem.amount <= 0) {
                        dragDropHandler.dropItem();
                    }
                }
                updatePlayerHandItem();
            }
        }
    }


    protected InventorySlot getSlotAt(int mouseX, int mouseY) {
        InventorySlot slot = craftingInputGrid.getSlotAt(mouseX, mouseY);
        if (slot != null) return slot;
        if (craftingOutputSlot.isMouseOver(mouseX, mouseY)) {
            return craftingOutputSlot;
        }
        slot = mainInventoryGrid.getSlotAt(mouseX, mouseY);
        if (slot != null) return slot;

        return null;
    }

    /**
     * Recomputes preview output in slot 45 from current 3x3 inputs.
     */
    private void updateCraftingOutput() {

        CraftingInput input = new CraftingInput(CraftingInput.SIZE_3X3);

        ItemStack[] inputSlots = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            inputSlots[i] = getPlayerRef().inventory.getItemAtSlot(50 + i);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = row * 3 + col;
                if (inputSlots[slotIndex] != null) {
                    //we flip because its upside down
                    input.setItem(col, 2 - row, inputSlots[slotIndex]);
                }
            }
        }
        Recipe recipe = CraftingManager.findRecipe(input);
        if (recipe != null) {
            ItemStack result = CraftingManager.craft(input);
            getPlayerRef().inventory.setItemSlot(CRAFTING_OUTPUT_SLOT, result);
        } else {
            getPlayerRef().inventory.setItemSlot(CRAFTING_OUTPUT_SLOT, null);
        }
    }


    /**
     * Consumes one item from each occupied input slot when crafting succeeds.
     */
    private void consumeCraftingIngredients() {
        CraftingInput input = new CraftingInput(CraftingInput.SIZE_3X3);

        ItemStack[] inputStacks = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            inputStacks[i] = getPlayerRef().inventory.getItemAtSlot(50 + i);
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = row * 3 + col;
                if (inputStacks[slotIndex] != null) {
                    //we flip because its upside down
                    input.setItem(col, 2 - row, inputStacks[slotIndex]);
                }
            }
        }

        Recipe recipe = CraftingManager.findRecipe(input);
        if (recipe != null) {
            for (int i = 0; i < 9; i++) {
                int slotId = 50 + i;
                ItemStack stack = inputStacks[i];
                if (stack != null) {
                    stack.amount--;
                    if (stack.amount <= 0) {
                        getPlayerRef().inventory.setItemSlot(slotId, null);
                    }
                }
            }
        }

        updateCraftingOutput();
    }

    public void onClose() {
        for (int i = 0; i < 9; i++) {
            int slotId = 50 + i;
            ItemStack stack = getPlayerRef().inventory.getItemAtSlot(slotId);
            if (stack != null) {
                boolean added = getPlayerRef().inventory.addItemToInventory(stack);
                if (added) {
                    getPlayerRef().inventory.setItemSlot(slotId, null);
                } else {
                    dropItemToWorld(stack);
                    getPlayerRef().inventory.setItemSlot(slotId, null);
                }
            }
        }

        ItemStack outputStack = getPlayerRef().inventory.getItemAtSlot(CRAFTING_OUTPUT_SLOT);
        if (outputStack != null) {
            // Output is discarded when closing - do not drop to world
            getPlayerRef().inventory.setItemSlot(CRAFTING_OUTPUT_SLOT, null);
        }

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
        return getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);
    }

    @Override
    protected void handleShiftClick(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot == null) return;

        int slotIndex = clickedSlot.slotIndex;

        // Shift-click from 3x3 crafting grid (50-58) -> inventory
        if (slotIndex >= 50 && slotIndex <= 58) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(slotIndex);
            if (item == null) return;
            boolean added = getPlayerRef().inventory.addItemToInventory(item);
            if (added) {
                getPlayerRef().inventory.setItemSlot(slotIndex, null);
                updateCraftingOutput();
                updatePlayerHandItem();
            }
            return;
        }

        // Shift-click crafting output (45) -> inventory
        if (slotIndex == 45) {
            // Get the output
            ItemStack outputStack = getPlayerRef().inventory.getItemAtSlot(45);
            if (outputStack == null) return;

            // Clear the slot BEFORE consuming ingredients (like left click does)
            getPlayerRef().inventory.setItemSlot(45, null);

            // Consume ingredients (this will calculate new output for remaining items)
            consumeCraftingIngredients();

            // Try to add to inventory
            boolean added = getPlayerRef().inventory.addItemToInventory(outputStack);
            if (added) {
                updatePlayerHandItem();
            }
            return;
        }

        // Shift-click inventory/hotbar -> 3x3
        if (isInventorySlot(slotIndex) || isHotbarSlot(slotIndex)) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(slotIndex);
            if (item == null) return;
            for (int i = 50; i <= 58; i++) {
                if (getPlayerRef().inventory.getItemAtSlot(i) == null) {
                    getPlayerRef().inventory.setItemSlot(i, item);
                    getPlayerRef().inventory.setItemSlot(slotIndex, null);
                    updateCraftingOutput();
                    updatePlayerHandItem();
                    return;
                }
            }
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
