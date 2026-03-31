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
import dev.alexco.minecraft.gui.ScreenState;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.level.item.BlockItem;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.block.BarrelBlock;
import dev.alexco.minecraft.world.level.block.FurnaceBlock;
import dev.alexco.minecraft.world.level.block.entity.BarrelBlockEntity;
import dev.alexco.minecraft.world.level.block.entity.FurnaceBlockEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Inventory screen renderer. Extends ContainerRenderer for drag-drop functionality
 */
public class InventoryRenderer extends ContainerRenderer {
    private static final boolean DEBUG_SLOTS = true;

    private TextureManager texRef;
    private Texture inventory;
    private boolean initialised = false;
    private float scaling = 4f;
    private String invPath = "textures/gui/inventory.png";
    private InventoryGrid mainGrid;
    private InventoryGrid armourGrid;
    private InventoryGrid offhandGrid;
    private InventoryGrid craftingGrid;
    private InventoryGrid craftingOutputGrid;
    private int inventoryScreenX;
    private int inventoryScreenY;
    private ShapeRenderer shapeRenderer;
    private Map<Integer, Texture> emptySlotTextures;

    @Override
    public void create() {
        texRef = Minecraft.getInstance().textureManager;
       // texRef.forceLoadTexture(invPath);
        inventory = texRef.get(invPath);
        setupContainer("Inventory", Color.CORAL);

        if (DEBUG_SLOTS) {
            shapeRenderer = new ShapeRenderer();
        }

        // Load empty slot textures for armour
        emptySlotTextures = new HashMap<>();
        String[] armorTextures = {"boots", "leggings", "chestplate", "helmet"};
        int[] armorSlots = {36, 37, 38, 39};

        for (int i = 0; i < armorTextures.length; i++) {
            String texPath = "textures/gui/slot/" + armorTextures[i] + ".png";
          //e  texRef.forceLoadTexture(texPath);
            emptySlotTextures.put(armorSlots[i], texRef.get(texPath));
        }
    }

    @Override
    protected void onContainerCreate() {
        initInventoryGrid();
    }

    /**
     * Initialises inventory, armour, offhand, and crafting slot geometry.
     */
    private void initInventoryGrid() {
        if (initialised)
            return;
        int invWidth = texRef.get(invPath).getWidth();
        int invHeight = texRef.get(invPath).getHeight();
        inventoryScreenX = Gdx.graphics.getWidth() / 2 - invWidth * 2;
        inventoryScreenY = Gdx.graphics.getHeight() / 2 - invHeight * 2;
        mainGrid = new InventoryGrid(inventoryScreenX + 32, inventoryScreenY + 32);
        float[] rowOffsets = {
            inventoryScreenY,
            Gdx.graphics.getHeight() / 2 - (48 * 2) - 4,
            Gdx.graphics.getHeight() / 2 - (48 * 3.5f) - 4,
            Gdx.graphics.getHeight() / 2 - (48 * 5) - 4
        };

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = row * 9 + col;
                int x = inventoryScreenX + 32 + 8 + (int) (48 * 1.5f * col);
                float y = rowOffsets[row] + 32 + 8;

                mainGrid.addSlot(new InventorySlot(slotIndex, x, (int) Math.floor(y), 48, 48,
                InventorySlot.SlotType.REGULAR));

            }
        }
        mainGrid.addSlot(new InventorySlot(40, inventoryScreenX+ 28 + (int)(48 * 1.5f * 4), (int)(Gdx.graphics.getHeight() / 2 - (48 * 0.5f) - 4) + 32 + 24, 48, 48, SlotType.OFFHAND));
        armourGrid = new InventoryGrid(0, 0);
        int[] armorSlots = {36, 37, 38, 39};
        String[] armorTextures = {"boots", "leggings", "chestplate", "helmet"};
        for (int i = 0; i < 4; i++) {
            int x = inventoryScreenX + 32 + 8;
            int y = (int)(Gdx.graphics.getHeight() / 2 - (48 * (0.5f - i * 1.5f)) - 4) + 32 + 24;

            InventorySlot slot = new InventorySlot(armorSlots[i], x, y, 48, 48,
                InventorySlot.SlotType.ARMOUR);
                slot.setEmptyTextureName(armorTextures[i]);
                armourGrid.addSlot(slot);
            }
            craftingGrid = new InventoryGrid(0, 0);
         int[] craftingSlots = {41, 42, 43, 44};
        int slotIndex = 0;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                int x = inventoryScreenX + 32 + 8 + (int)(48 * 1.5f * (5 + col));
                int y = 290+ inventoryScreenY + 64 + (int)(48 * 1.5f * (row + 1.5f));

                craftingGrid.addSlot(new InventorySlot(craftingSlots[slotIndex], x, y, 48, 48,
                    InventorySlot.SlotType.CRAFTING_INPUT));
                slotIndex++;
            }
        }
        int outputX = inventoryScreenX + 32 + 16 + (int)(48 * 1.5f * 8);
        int outputY = inventoryScreenY + 64 + (int)(48 * 1.5f * 2);
        craftingGrid.addSlot(new InventorySlot(45, outputX, 290+outputY, 48, 48, SlotType.CRAFTING_OUTPUT));
        initialised = true;
    }

    @Override
    protected void renderContainer() {

        spriteBatch.begin();
        // draw background
        spriteBatch.draw(inventory, Gdx.graphics.getWidth() / 2f - (inventory.getWidth() / 2f) * scaling,
                Gdx.graphics.getHeight() / 2f - (inventory.getHeight() / 2f) * scaling, inventory.getWidth() * scaling,
                inventory.getHeight() * scaling);

        // draw slot items
        for (InventorySlot slot : mainGrid.getSlots()) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);
            if (item == null)
                continue;
            drawItemWithAmount(item, slot.x, slot.y, slot.width, slot.height);
        }

        // draw armour slots with empty textures
        for (InventorySlot slot : armourGrid.getSlots()) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);

            // Draw empty slot texture if slot is empty
            if (item == null && emptySlotTextures.containsKey(slot.slotIndex)) {
                Texture emptyTex = emptySlotTextures.get(slot.slotIndex);
                spriteBatch.draw(emptyTex, slot.x, slot.y, slot.width, slot.height);
            } else if (item != null) {
                drawItemWithAmount(item, slot.x, slot.y, slot.width, slot.height);
            }
        }
        for (InventorySlot slot : craftingGrid.getSlots()) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(slot.slotIndex);

            // Draw empty slot texture if slot is empty
             if (item == null)
                continue;
            drawItemWithAmount(item, slot.x, slot.y, slot.width, slot.height);
        }

        // draw dragged item
        if (dragDropHandler.isDragging()) {
            ItemStack draggedItem = dragDropHandler.getHeldItem();
            if (draggedItem != null) {
                float dragX = dragDropHandler.getDragX() - (ITEM_RENDER_SIZE / 2f);
                float dragY = dragDropHandler.getDragY() - (ITEM_RENDER_SIZE / 2f);
                drawItemWithAmount(draggedItem, dragX, dragY, ITEM_RENDER_SIZE, ITEM_RENDER_SIZE);
            }
        }
        spriteBatch.end();


        if (DEBUG_SLOTS) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.GREEN);

            for (InventorySlot slot : mainGrid.getSlots()) {
                shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
            }

            shapeRenderer.setColor(Color.CYAN);
            for (InventorySlot slot : armourGrid.getSlots()) {
                shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
            }
            shapeRenderer.setColor(Color.YELLOW);
            for (InventorySlot slot : craftingGrid.getSlots()) {
                shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
            }

            shapeRenderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        shapeRenderer.setProjectionMatrix(camera.combined);
        initialised = false;
        initInventoryGrid();
    }

    @Override
    protected void onLeftClickPickup(int mouseX, int mouseY) {
        InventorySlot clickedSlot = getSlotAt(mouseX, mouseY);
        if (clickedSlot != null) {
            ItemStack item = getPlayerRef().inventory.getItemAtSlot(clickedSlot.slotIndex);
            if (item != null) {
                if (clickedSlot.slotIndex == 45) {
                    dragDropHandler.pickupItem(item, clickedSlot.slotIndex);
                    //clear the slot
                    getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);
                    // the items have immediately been used on click!!
                    consumeCraftingIngredients();
                    updatePlayerHandItem();
                    return;
                }

                dragDropHandler.pickupItem(item, clickedSlot.slotIndex);
                getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);

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
            //you are not allowed to place items into crafting output.
            if (targetSlot.slotIndex == 45) {
                return;
            }


            ItemStack existingItem = getPlayerRef().inventory.getItemAtSlot(targetSlot.slotIndex);

            if (existingItem == null) {
                int maxStackSize = heldItem.item.getMaxStackSize();
                int amountToPlace = Math.min(heldItem.amount, maxStackSize);

                ItemStack slotStack = new ItemStack(heldItem.item, amountToPlace, heldItem.damage);
                getPlayerRef().inventory.setItemSlot(targetSlot.slotIndex, slotStack);
                heldItem.amount -= amountToPlace;
                if (heldItem.amount <= 0) {
                    dragDropHandler.dropItem();
                }
                if (targetSlot.type == SlotType.CRAFTING_INPUT) {
                    updateCraftingOutput();
                }
                updatePlayerHandItem();
            } else if (existingItem.item == heldItem.item) {
                //of the same item
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
                //if theres no space we cannot delete it
            } else {
                //swap
                ItemStack heldCopy = new ItemStack(heldItem.item, heldItem.amount, heldItem.damage);
                dragDropHandler.pickupItem(existingItem, targetSlot.slotIndex);
                getPlayerRef().inventory.setItemSlot(targetSlot.slotIndex, heldCopy);

                if (targetSlot.type == SlotType.CRAFTING_INPUT) {
                    updateCraftingOutput();
                }
                updatePlayerHandItem();
            }
        } else if (targetSlot == null && heldItem != null) {
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
                //crafting output is treated as left click, we cannot consume "half" items
                if (clickedSlot.slotIndex == 45) {
                    dragDropHandler.pickupItem(item, clickedSlot.slotIndex);
                    getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);
                    consumeCraftingIngredients();
                    updateCraftingOutput();
                    updatePlayerHandItem();
                    return;
                }

                int halfAmount = (item.amount + 1) / 2;
                int remainingAmount = item.amount - halfAmount;

                ItemStack handStack = new ItemStack(item.item, halfAmount, item.damage);
                dragDropHandler.pickupItem(handStack, clickedSlot.slotIndex);

                if (remainingAmount > 0) {
                    item.amount = remainingAmount;
                } else {
                    getPlayerRef().inventory.setItemSlot(clickedSlot.slotIndex, null);
                }
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
            //we cannot place into empty
            if (targetSlot.slotIndex == 45) {
                return;
            }

            ItemStack existingItem = getPlayerRef().inventory.getItemAtSlot(targetSlot.slotIndex);

            if (existingItem == null) {
                ItemStack singleItem = new ItemStack(heldItem.item, 1, heldItem.damage);
                getPlayerRef().inventory.setItemSlot(targetSlot.slotIndex, singleItem);

                heldItem.amount--;
                if (heldItem.amount <= 0) {
                    dragDropHandler.dropItem();
                }

                if (targetSlot.type == SlotType.CRAFTING_INPUT) {
                    updateCraftingOutput();
                }
                updatePlayerHandItem();
            } else if (existingItem.item == heldItem.item) {
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
           //if the slot has something else, we can do nothing, the hand will continue to hold
        }
    }

   /**
     * Get the item from any grid
     */
        protected InventorySlot getSlotAt(int mouseX, int mouseY) {
        InventorySlot slot = mainGrid.getSlotAt(mouseX, mouseY);
        if (slot == null) {
            slot = armourGrid.getSlotAt(mouseX, mouseY);
        }
        if (slot == null) {
            slot = craftingGrid.getSlotAt(mouseX, mouseY);
        }
        return slot;
    }

    /**
     * Updates the crafting output slot based on current input
     */
        private void updateCraftingOutput() {
        // Build crafting input from slots 41-44
        CraftingInput input = new CraftingInput(CraftingInput.SIZE_2X2);

        // mapping: 41=0,1  42=1,1  43=0,0  44=1,0
        ItemStack slot41 = getPlayerRef().inventory.getItemAtSlot(41);
        ItemStack slot42 = getPlayerRef().inventory.getItemAtSlot(42);
        ItemStack slot43 = getPlayerRef().inventory.getItemAtSlot(43);
        ItemStack slot44 = getPlayerRef().inventory.getItemAtSlot(44);

        if (slot41 != null) input.setItem(0, 1, slot41);
        if (slot42 != null) input.setItem(1, 1, slot42);
        if (slot43 != null) input.setItem(0, 0, slot43);
        if (slot44 != null) input.setItem(1, 0, slot44);

        //match
        Recipe recipe = CraftingManager.findRecipe(input);
        if (recipe != null) {
            ItemStack result = CraftingManager.craft(input);
            getPlayerRef().inventory.setItemSlot(45, result);
        } else {
            getPlayerRef().inventory.setItemSlot(45, null);
        }
    }

    /**
     * Consumes ingredients from the crafting grid when output is taken
     */
        private void consumeCraftingIngredients() {
        // Build crafting input from slots 41-44
        CraftingInput input = new CraftingInput(CraftingInput.SIZE_2X2);

        ItemStack slot41 = getPlayerRef().inventory.getItemAtSlot(41);
        ItemStack slot42 = getPlayerRef().inventory.getItemAtSlot(42);
        ItemStack slot43 = getPlayerRef().inventory.getItemAtSlot(43);
        ItemStack slot44 = getPlayerRef().inventory.getItemAtSlot(44);

        // Store references to decrement later
        ItemStack[] inputStacks = new ItemStack[4];
        inputStacks[0] = slot41;
        inputStacks[1] = slot42;
        inputStacks[2] = slot43;
        inputStacks[3] = slot44;

        if (slot41 != null) input.setItem(0, 1, slot41);
        if (slot42 != null) input.setItem(1, 1, slot42);
        if (slot43 != null) input.setItem(0, 0, slot43);
        if (slot44 != null) input.setItem(1, 0, slot44);

        Recipe recipe = CraftingManager.findRecipe(input);
        if (recipe != null) {
          //consume an item from each
            for (int i = 0; i < 4; i++) {
                int slotId = 41 + i;
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
        int[] craftingSlots = {41, 42, 43, 44};
        for (int slotId : craftingSlots) {
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

        ItemStack outputStack = getPlayerRef().inventory.getItemAtSlot(45);
        if (outputStack != null) {
            // Output is discarded when closing - do not drop to world
            getPlayerRef().inventory.setItemSlot(45, null);
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
        inventory.dispose();
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
        ItemStack item = getPlayerRef().inventory.getItemAtSlot(slotIndex);
        if (item == null) return;

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

        // Shift-click from armour slot -> main inventory
        if (isArmorSlot(slotIndex)) {
            ItemStack armorItem = getPlayerRef().inventory.getItemAtSlot(slotIndex);
            if (armorItem == null) return;

            boolean added = getPlayerRef().inventory.addItemToInventory(armorItem);
            if (added) {
                getPlayerRef().inventory.setItemSlot(slotIndex, null);
                playArmorEquipSound();
                updatePlayerHandItem();
            }
            return;
        }

        // Shift-click armour item -> armour slot (only in inventory screen)
        ScreenState screen = getCurrentContainerScreen();
        if (screen == ScreenState.INVENTORY && isArmorItem(item.item)) {
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

        // Shift-click inventory/hotbar -> container or crafting grid
        if (isInventorySlot(slotIndex) || isHotbarSlot(slotIndex)) {
            // If barrel open -> move to barrel
            if (screen == ScreenState.BARREL) {
                BarrelBlockEntity barrel = BarrelBlock.getBlockEntity(
                    Minecraft.getInstance().getSession().getBarrelRenderer().getBarrelX(),
                    Minecraft.getInstance().getSession().getBarrelRenderer().getBarrelY());
                if (barrel == null) return;
                for (int i = 0; i < BarrelBlockEntity.INVENTORY_SIZE; i++) {
                    if (barrel.getStackInSlot(i) == null) {
                        barrel.setStackInSlot(i, item);
                        getPlayerRef().inventory.setItemSlot(slotIndex, null);
                        updatePlayerHandItem();
                        return;
                    }
                }
                for (int i = 0; i < BarrelBlockEntity.INVENTORY_SIZE; i++) {
                    ItemStack existing = barrel.getStackInSlot(i);
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
                return;
            }

            // If furnace open -> move to furnace (fuel to fuel slot, others to input)
            if (screen == ScreenState.FURNACE) {
                FurnaceBlockEntity furnace = FurnaceBlock.getBlockEntity(
                    Minecraft.getInstance().getSession().getFurnaceRenderer().getFurnaceX(),
                    Minecraft.getInstance().getSession().getFurnaceRenderer().getFurnaceY());
                if (furnace == null) return;

                if (isFurnaceFuel(item.item)) {
                    ItemStack fuelSlot = furnace.getStackInSlot(FurnaceBlockEntity.SLOT_FUEL);
                    if (fuelSlot == null) {
                        furnace.setStackInSlot(FurnaceBlockEntity.SLOT_FUEL, item);
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
                    ItemStack inputSlot = furnace.getStackInSlot(FurnaceBlockEntity.SLOT_INPUT);
                    if (inputSlot == null) {
                        furnace.setStackInSlot(FurnaceBlockEntity.SLOT_INPUT, item);
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
                return;
            }

            // If crafting table open -> 3x3 (50-58)
            if (screen == ScreenState.CRAFTING_TABLE) {
                for (int i = 50; i <= 58; i++) {
                    if (getPlayerRef().inventory.getItemAtSlot(i) == null) {
                        getPlayerRef().inventory.setItemSlot(i, item);
                        getPlayerRef().inventory.setItemSlot(slotIndex, null);
                        updateCraftingOutput();
                        updatePlayerHandItem();
                        return;
                    }
                }
                return;
            }

            // Just inventory open -> 2x2 (41-44)
            for (int i = 41; i <= 44; i++) {
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
