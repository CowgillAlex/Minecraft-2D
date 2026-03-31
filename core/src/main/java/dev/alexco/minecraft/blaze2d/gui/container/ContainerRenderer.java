package dev.alexco.minecraft.blaze2d.gui.container;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.blaze2d.BlockTextureAtlas;
import dev.alexco.minecraft.blaze2d.RenderableLifecycle;
import dev.alexco.minecraft.blaze2d.gui.inventory.DragDropHandler;
import dev.alexco.minecraft.blaze2d.gui.inventory.InventorySlot;
import dev.alexco.minecraft.gui.ScreenState;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.sound.SoundSystem;
import dev.alexco.minecraft.tag.BlockTags;
import dev.alexco.minecraft.tag.ItemTags;
import dev.alexco.minecraft.world.entity.BlockItemEntity;
import dev.alexco.minecraft.world.entity.ItemEntity;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.level.block.Block;
import dev.alexco.minecraft.world.level.block.OakSlabBlock;
import dev.alexco.minecraft.world.level.block.OakStairsBlock;
import dev.alexco.minecraft.world.level.item.BlockItem;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.minecraft.world.level.item.Items;
import dev.alexco.minecraft.world.level.item.ToolItem;
import java.util.Locale;

/**
 * @author claude
 * Base class for all container renderers
 * Handles common drag-drop functionality and rendering patterns
 */
public abstract class ContainerRenderer extends RenderableLifecycle {
    protected DragDropHandler dragDropHandler;
    protected BitmapFont bitmapFont;
    protected boolean isOpen = true;
    protected static final int ITEM_RENDER_SIZE = 48;
    protected Texture durabilityPixel;

    protected void setupContainer(String name, Color color) {
        super.create(name, color);
        dragDropHandler = new DragDropHandler();
        bitmapFont = new BitmapFont(Gdx.files.internal("fonts/minecraftseven.fnt"));
        bitmapFont.getData().markupEnabled = true;
        bitmapFont.getRegion().getTexture().setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest,
                com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        durabilityPixel = new Texture(pixmap);
        pixmap.dispose();
        onContainerCreate();
    }

    /**
     * Called after common setup. Subclasses should initialise their specific UI
     */
    protected abstract void onContainerCreate();

    @Override
    public void render() {
        updateMousePosition();
        handleMouseInput();
        renderContainer();
        renderTooltip();
    }

    protected abstract InventorySlot getHoveredSlot();

    protected abstract ItemStack getItemStackAtSlot(InventorySlot slot);

    /**
     * Builds tooltip text for a stack, including tool attribute details when present.
     */
    protected String getTooltipText(ItemStack stack) {
        StringBuilder builder = new StringBuilder(Registry.ITEM.getKey(stack.item).toString());
        if (stack.item instanceof ToolItem toolItem) {
            for (ToolItem.AttributeValue attribute : toolItem.getAttributes().values()) {
                builder.append('\n')
                    .append(attribute.getDisplayName())
                    .append(": ")
                    .append(String.format(Locale.ROOT, "%.1f", attribute.getTotal()));
            }
        }
        return builder.toString();
    }

    /**
     * Renders tooltip text for the currently hovered slot item.
     */
    private void renderTooltip() {
        InventorySlot hoveredSlot = getHoveredSlot();
        if (hoveredSlot == null) return;

        ItemStack stack = getItemStackAtSlot(hoveredSlot);
        if (stack == null || stack.item == null) return;

        String tooltipText = getTooltipText(stack);
        if (tooltipText.indexOf('\n') >= 0) {
            Minecraft.tooltipRenderer.renderMultilineAtMouse(tooltipText);
        } else {
            Minecraft.tooltipRenderer.renderAtMouse(tooltipText);
        }
    }

    /**
     * Update drag position every frame (even when mouse not pressed)
     */
    private void updateMousePosition() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        dragDropHandler.updatePosition(mouseX, mouseY);
    }

    /**
     * Handle all mouse input (click-to-pick, click-to-drop mode)
     */
    private void handleMouseInput() {
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Handle F key for offhand swap
        if (Gdx.input.isKeyPressed(Keys.F)) {
            handleFKeyPress();
        }

        // Left click handling
        if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
            if (!dragDropHandler.isDragging()) {
                // Try to pick up item
                if (isShiftPressed()) {
                    handleShiftClick(mouseX, mouseY);
                } else {
                    onLeftClickPickup(mouseX, mouseY);
                }
            } else {
                // Try to drop item
                onLeftClickDrop(mouseX, mouseY);
            }
        }

        // Right click handling
        if (Gdx.input.isButtonJustPressed(Buttons.RIGHT)) {
            if (!dragDropHandler.isDragging()) {
                // No item in hand - try to take half
                if (isShiftPressed()) {
                    handleShiftClick(mouseX, mouseY);
                } else {
                    onRightClickTakeHalf(mouseX, mouseY);
                }
            } else {
                // Item in hand - try to place one
                onRightClickPlaceOne(mouseX, mouseY);
            }
        }
    }

    /**
     * Check if shift key is pressed
     */
    protected boolean isShiftPressed() {
        return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
    }

    /**
     * Handle F key press for offhand swap
     */
    private void handleFKeyPress() {
        InventorySlot hoveredSlot = getHoveredSlot();
        if (hoveredSlot != null) {
            handleOffhandSwap(hoveredSlot.slotIndex);
        }
    }

    /**
     * Handle shift-click - routes to appropriate action based on slot type.
     * Override in subclasses for custom behaviour.
     */
    protected void handleShiftClick(int mouseX, int mouseY) {
        // Default implementation does nothing - subclasses should override
    }

    /**
     * Get slot at mouse position - must be implemented by subclass
     */
    protected abstract InventorySlot getSlotAt(int mouseX, int mouseY);

    /**
     * Get the current container screen state
     */
    protected ScreenState getCurrentContainerScreen() {
        return Minecraft.getInstance().currentScreenState;
    }

    /**
     * Check if item is furnace fuel
     */
    protected boolean isFurnaceFuel(Item item) {
        return ItemTags.FURNACE_FUELS.contains(item);
    }

    /**
     * Handle F key to swap with offhand.
     * Override in subclasses for custom behaviour.
     */
    protected void handleOffhandSwap(int hoveredSlot) {
        // Default implementation does nothing - subclasses should override
    }

    /**
     * Check if item is an armour item
     */
    protected boolean isArmorItem(Item item) {
        return ItemTags.HELMETS.contains(item) ||
               ItemTags.CHESTPLATES.contains(item) ||
               ItemTags.LEGGINGS.contains(item) ||
               ItemTags.BOOTS.contains(item);
    }

    /**
     * Get armour slot for item type
     */
    protected int getArmorSlotForItem(Item item) {
        if (ItemTags.HELMETS.contains(item)) return 39; // helmet
        if (ItemTags.CHESTPLATES.contains(item)) return 38; // chestplate
        if (ItemTags.LEGGINGS.contains(item)) return 37; // leggings
        if (ItemTags.BOOTS.contains(item)) return 36; // boots
        return -1;
    }

    /**
     * Check if slot is an inventory slot (0-35)
     */
    protected boolean isInventorySlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex <= 35;
    }

    /**
     * Check if slot is a hotbar slot (0-8)
     */
    protected boolean isHotbarSlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex <= 8;
    }

    /**
     * Check if slot is an armour slot (36-39)
     */
    protected boolean isArmorSlot(int slotIndex) {
        return slotIndex >= 36 && slotIndex <= 39;
    }

    /**
     * Check if slot is offhand slot (40)
     */
    protected boolean isOffhandSlot(int slotIndex) {
        return slotIndex == 40;
    }

    /**
     * Check if slot is crafting input slot
     */
    protected boolean isCraftingInputSlot(int slotIndex) {
        return (slotIndex >= 41 && slotIndex <= 44) || (slotIndex >= 50 && slotIndex <= 58);
    }

    /**
     * Play armour equip sound
     */
    protected void playArmorEquipSound() {
        int rand = (int)(Math.random() * 6) + 1;
        SoundSystem.playSound("item.armor.equip_iron" + rand);
    }

    /**
     * Get player reference
     */
    protected Player getPlayerRef() {
        return Minecraft.getInstance().getPlayer();
    }

    /**
     * Called when left mouse is clicked while NOT holding an item
     */
    protected abstract void onLeftClickPickup(int mouseX, int mouseY);

    /**
     * Called when left mouse is clicked while holding an item
     */
    protected abstract void onLeftClickDrop(int mouseX, int mouseY);

    /**
     * Called when right mouse is clicked while NOT holding an item
     * Should take half the stack from the slot
     */
    protected abstract void onRightClickTakeHalf(int mouseX, int mouseY);

    /**
     * Called when right mouse is clicked while holding an item
     * Should place one item in an empty slot
     */
    protected abstract void onRightClickPlaceOne(int mouseX, int mouseY);

    /**
     * Render the container UI
     */
    protected abstract void renderContainer();

    /**
     * Drop an item stack into the world at player position
     */
    protected void dropItemToWorld(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        for (int i = 0; i < itemStack.amount; i++){
            double playerX = Minecraft.getInstance().getPlayer().x;
            double playerY = Minecraft.getInstance().getPlayer().y;
            if (itemStack.item instanceof BlockItem blockItem) {
                BlockItemEntity entity = new BlockItemEntity(blockItem, playerX, playerY);
                Minecraft.getInstance().getWorld().entities.add(entity);
            } else {
                ItemEntity entity = new ItemEntity(itemStack.item, playerX, playerY);
                Minecraft.getInstance().getWorld().entities.add(entity);
            }
        }
    }

    /**
     * Draws either block-style or regular atlas item icons.
     */
    protected void drawItem(Item item, float x, float y, float width, float height) {
        if (item instanceof BlockItem) {
            drawBlockItem((BlockItem) item, x, y, width, height);
        } else {
            drawRegularItem(item, x, y, width, height);
        }
    }


    /**
     * Draws block items with special handling for tinted blocks and shaped icons.
     */
    protected void drawBlockItem(BlockItem blockItem, float x, float y, float width, float height) {
        Block block = blockItem.getBlock();
        boolean needsGreenTint = BlockTags.GREEN_TINT.contains(block);

        if (needsGreenTint) {
            spriteBatch.setColor(Color.GREEN);
        }

        float u = Minecraft.getInstance().atlas.getUV(blockItem.getBlockState()).x;
        float v = Minecraft.getInstance().atlas.getUV(blockItem.getBlockState()).y;
        float uvBlockSize = Minecraft.getInstance().atlas.getUVBlockSize();
        float drawX = x;
        float drawY = y;
        float drawW = width;
        float drawH = height;
        float minU = u;
        float minV = v;
        float maxU = u + uvBlockSize;
        float maxV = v + uvBlockSize;

        if (block instanceof OakSlabBlock) {
            drawH = height * 0.5f;
            maxV = minV + (uvBlockSize * 0.5f);
            spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), drawX, drawY, drawW, drawH,
                    minU, maxV, maxU, minV);
        } else if (block instanceof OakStairsBlock) {
            // bottom half
            spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), drawX, drawY, drawW, height * 0.5f,
                    minU, minV + uvBlockSize, maxU, minV + (uvBlockSize * 0.5f));
            // top quarter to read as stairs in icons
            spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), drawX + (drawW * 0.5f), drawY + (height * 0.5f), drawW * 0.5f, height * 0.5f,
                    minU + (uvBlockSize * 0.5f), minV + (uvBlockSize * 0.5f), maxU, minV);
        } else {
            spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), drawX, drawY, drawW, drawH,
                    minU, maxV, maxU, minV);
        }

        if (needsGreenTint) {
            spriteBatch.setColor(Color.WHITE);
        }
    }


    /**
     * Draws regular item atlas sprites using 16x16 texture coordinates.
     */
    protected void drawRegularItem(Item item, float x, float y, float width, float height) {
        Vector2 coords = item.getAtlasCoords();
        if (coords != null) {
            float u = coords.x / BlockTextureAtlas.TEXTURE_SIZE;
            float v = coords.y / BlockTextureAtlas.TEXTURE_SIZE;
            float uvBlockSize = 16f / BlockTextureAtlas.TEXTURE_SIZE;
            spriteBatch.draw(Minecraft.getInstance().textureManager.get("textures.png"), x, y, width, height,
                    u, v + uvBlockSize, u + uvBlockSize, v);
        }
    }

    /**
     * Draws one stack icon plus amount and durability overlays.
     */
    protected void drawItemWithAmount(ItemStack stack, float x, float y, float width, float height) {
        if (stack == null || stack.item == null) {
            return;
        }
        drawItem(stack.item, x, y, width, height);
        if (stack.amount > 1) {
            bitmapFont.draw(spriteBatch, stack.amount + "", x + width - 16, y + 16);
        }
        drawDurabilityBar(stack, x, y, width, height);
    }

    /**
     * Draws a compact durability bar for damageable items.
     */
    protected void drawDurabilityBar(ItemStack stack, float x, float y, float width, float height) {
        if (!stack.item.canBeDepleted() || stack.item.getMaxDamage() <= 0) {
            return;
        }
        float durabilityFraction = 1.0f - ((float) stack.damage / (float) stack.item.getMaxDamage());
        durabilityFraction = Math.clamp(durabilityFraction, 0.0f, 1.0f);

        float barWidth = width - 8.0f;
        float barHeight = Math.max(3.0f, height * 0.08f);
        float barX = x + 4.0f;
        float barY = y + 2.0f;

        spriteBatch.setColor(0f, 0f, 0f, 0.9f);
        spriteBatch.draw(durabilityPixel, barX, barY, barWidth, barHeight);

        float green = durabilityFraction;
        float red = 1.0f - durabilityFraction;
        spriteBatch.setColor(red, green, 0f, 1f);
        spriteBatch.draw(durabilityPixel, barX + 1.0f, barY + 1.0f, (barWidth - 2.0f) * durabilityFraction, Math.max(1.0f, barHeight - 2.0f));
        spriteBatch.setColor(Color.WHITE);
    }

    @Override
    public void destroy() {
        bitmapFont.dispose();
        if (durabilityPixel != null) {
            durabilityPixel.dispose();
        }
        super.destroy();
    }


    protected void updatePlayerHandItem() {
        Player player = Minecraft.getInstance().getPlayer();
        int selectedSlot = player.slotSelected - 1; // Convert 1-9 to 0-8

        ItemStack stack = player.inventory.getItemAtSlot(selectedSlot);
        if (stack != null && stack.item != null) {
            player.blockInHand = stack.item;
        } else {
            player.blockInHand = Items.AIR;
        }
    }
}
