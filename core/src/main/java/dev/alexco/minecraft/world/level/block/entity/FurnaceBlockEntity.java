package dev.alexco.minecraft.world.level.block.entity;

import dev.alexco.minecraft.Minecraft;
import dev.alexco.minecraft.crafting.FurnaceRecipe;
import dev.alexco.minecraft.crafting.FurnaceRecipeLoader;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.tag.ItemTags;
import dev.alexco.minecraft.world.entity.ItemEntity;
import dev.alexco.minecraft.world.level.block.FurnaceBlock;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.registry.ResourceLocation;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;


public class FurnaceBlockEntity extends BlockEntity {
    public static final String TYPE_ID = "minecraft:furnace";
    private static final ThreadLocal<Boolean> UPDATING_STATE = ThreadLocal.withInitial(() -> false);

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int INVENTORY_SIZE = 3;

    private int cookTime = 0;
    private int cookTimeTotal = 200;

    private int burnTime = 0;
    private int burnTimeTotal = 0;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];

    public FurnaceBlockEntity(int x, int y) {
        super(x, y);
    }

    /**
     * Ticks burn/cook progress and updates lit state.
     */
    @Override
    public void tick() {
        boolean wasBurning = isBurning();

        if (isBurning()) {
            burnTime--;
        }

        ItemStack input = inventory[SLOT_INPUT];
        ItemStack fuel = inventory[SLOT_FUEL];
        ItemStack output = inventory[SLOT_OUTPUT];

        FurnaceRecipe recipe = input != null ? FurnaceRecipeLoader.findRecipe(input) : null;
        boolean canWork = recipe != null && canAcceptOutput(recipe, output);

        if (canWork) {
            cookTimeTotal = recipe.getCookingTime();

            if (!isBurning() && hasFuel()) {
                startBurning();
            }

            if (isBurning()) {
                cookTime++;

                if (cookTime >= cookTimeTotal) {
                    completeCooking(recipe);
                    cookTime = 0;
                }
            } else {
                cookTime = 0;
            }
        } else {
            cookTime = 0;
        }

        boolean isBurningNow = isBurning();
        if (wasBurning != isBurningNow) {
            FurnaceBlock.updateLitState(x, y, isBurningNow);
        }
    }

    /**
     * Checks whether recipe output can fit in the output slot.
     */
    private boolean canAcceptOutput(FurnaceRecipe recipe, ItemStack output) {
        if (recipe == null) return false;
        ItemStack result = recipe.getResult();
        if (result == null) return false;

        if (output == null) return true;

        return output.item.equals(result.item) &&
               output.amount + result.amount <= result.item.getMaxStackSize();
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    public float getBurnProgress() {
        if (burnTimeTotal == 0) return 0.0f;
        return (float) burnTime / (float) burnTimeTotal;
    }

    public float getCookProgress() {
        if (cookTimeTotal == 0) return 0.0f;
        return (float) cookTime / (float) cookTimeTotal;
    }

    private boolean hasFuel() {
        ItemStack fuel = inventory[SLOT_FUEL];
        return fuel != null && fuel.amount > 0 && isFuel(fuel.item);
    }

    /**
     * Consumes one fuel item and starts a new burn cycle.
     */
    private void startBurning() {
        ItemStack fuel = inventory[SLOT_FUEL];
        if (fuel != null && fuel.amount > 0 && isFuel(fuel.item)) {
            int fuelTime = getFuelTime(fuel.item);
            burnTime = fuelTime;
            burnTimeTotal = fuelTime;

            fuel.amount--;
            if (fuel.amount <= 0) {
                inventory[SLOT_FUEL] = null;
            }
        }
    }

    /**
     * Consumes one input and writes the smelting result to output.
     */
    private void completeCooking(FurnaceRecipe recipe) {
        if (recipe == null) return;

        ItemStack result = recipe.getResult();
        if (result == null) return;

        ItemStack input = inventory[SLOT_INPUT];
        ItemStack output = inventory[SLOT_OUTPUT];

        if (!canAcceptOutput(recipe, output)) return;

        if (input != null) {
            input.amount--;
            if (input.amount <= 0) {
                inventory[SLOT_INPUT] = null;
            }
        }

        if (output == null) {
            inventory[SLOT_OUTPUT] = new ItemStack(result.item, result.amount, result.damage);
        } else {
            output.amount += result.amount;
        }
    }

    /**
     * Returns true when an item is valid furnace fuel.
     */
    public static boolean isFuel(Item item) {
        return ItemTags.FURNACE_FUELS.contains(item);
    }

    /**
     * Returns burn duration in ticks for a fuel item.
     */
    public static int getFuelTime(Item item) {
        if (item == dev.alexco.minecraft.world.level.item.Items.COAL) return 1600;
        if (item == dev.alexco.minecraft.world.level.item.Items.CHARCOAL) return 1600;
        if (item == dev.alexco.minecraft.world.level.item.Items.OAK_LOG) return 300;
        if (item == dev.alexco.minecraft.world.level.item.Items.OAK_PLANKS) return 300;
        if (item == dev.alexco.minecraft.world.level.item.Items.STICK) return 100;
        return 200;
    }

    @Override
    public CompoundTag saveToNBT(CompoundTag tag) {
        tag.putString("type", TYPE_ID);
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("cookTime", cookTime);
        tag.putInt("cookTimeTotal", cookTimeTotal);
        tag.putInt("burnTime", burnTime);
        tag.putInt("burnTimeTotal", burnTimeTotal);

        ListTag<CompoundTag> inventoryTag = new ListTag<>(CompoundTag.class);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventory[i] != null) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("slot", i);
                itemTag.putString("item", Registry.ITEM.getKey(inventory[i].item).toString());
                itemTag.putInt("count", inventory[i].amount);
                if (inventory[i].damage > 0) {
                    itemTag.putInt("damage", inventory[i].damage);
                }
                inventoryTag.add(itemTag);
            }
        }
        tag.put("inventory", inventoryTag);

        return tag;
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.cookTime = tag.getInt("cookTime");
        this.cookTimeTotal = tag.getInt("cookTimeTotal");
        this.burnTime = tag.getInt("burnTime");
        this.burnTimeTotal = tag.getInt("burnTimeTotal");

        ListTag<CompoundTag> inventoryTag = tag.getListTag("inventory").asCompoundTagList();
        for (CompoundTag itemTag : inventoryTag) {
            int slot = itemTag.getInt("slot");
            String itemId = itemTag.getString("item");
            int count = itemTag.getInt("count");
            int damage = itemTag.containsKey("damage") ? itemTag.getInt("damage") : 0;

            Item item = Registry.ITEM.get(new ResourceLocation(itemId));
            if (item != null && slot >= 0 && slot < INVENTORY_SIZE) {
                inventory[slot] = new ItemStack(item, count, damage);
            }
        }
    }

    @Override
    public void onBlockBroken() {
        if (UPDATING_STATE.get()) {
            // if we are updating the state we dont wanna do much with it
            return;
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack stack = inventory[i];
            if (stack != null && stack.amount > 0) {
                for (int j = 0; j < stack.amount; j++) {
                    Minecraft.getInstance().getWorld().entities.add(
                        new ItemEntity(stack.item, x + 0.5D, y)
                    );
                }
            }
        }
    }

    /**
     * Enables temporary suppression of drops during state swaps.
     */
    public static void beginStateUpdate() {
        UPDATING_STATE.set(true);
    }

    /**
     * Re-enables normal drop behaviour after state swaps.
     */
    public static void endStateUpdate() {
        UPDATING_STATE.set(false);
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }


    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= INVENTORY_SIZE) return null;
        return inventory[slot];
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INVENTORY_SIZE) return;
        inventory[slot] = stack;
    }

    public boolean canPlaceItemInSlot(int slot, ItemStack stack) {
        if (stack == null) return true;

        switch (slot) {
            case SLOT_INPUT:
                return true;
            case SLOT_FUEL:
                return isFuel(stack.item);
            case SLOT_OUTPUT:
                return false;
            default:
                return false;
        }
    }
}
