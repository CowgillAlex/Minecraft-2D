package dev.alexco.minecraft.world.serialisation;

import com.badlogic.gdx.files.FileHandle;
import dev.alexco.minecraft.Version;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.entity.Player;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.registry.ResourceLocation;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;

import java.io.IOException;

public class PlayerFile {
    /**
     * Saves the current player state to disk.
     */
    public static void writePlayerToDisk(Player player, String worldFolderName) {
        CompoundTag playerTag = serialisePlayer(player);
        FileHandle playerFile = WorldSaveManager.getPlayerFile(worldFolderName);

        try {
            playerFile.parent().mkdirs();
            NBTUtil.write(new NamedTag(null, playerTag), playerFile.file());
            Logger.INFO("Saved player data to %s", playerFile.path());
        } catch (IOException e) {
            Logger.ERROR("Failed to save player data: %s", e.getMessage());
            throw new RuntimeException("Failed to save player data: " + e.getMessage(), e);
        }
    }

    /**
     * Serialises player position, motion and inventory into NBT.
     */
    public static CompoundTag serialisePlayer(Player player) {
        CompoundTag tag = new CompoundTag();

        tag.putString("DataVersion", Version.DATA_VERSION);
        tag.putDouble("x", player.x);
        tag.putDouble("y", player.y);
        tag.putDouble("xd", player.xd);
        tag.putDouble("yd", player.yd);
        tag.putInt("slotSelected", player.slotSelected);
        tag.putString("uuid", player.uuid);

        ListTag<CompoundTag> inventoryTag = new ListTag<>(CompoundTag.class);
        for (int i = 0; i <= 40; i++) {
            ItemStack stack = player.inventory.getItemAtSlot(i);
            if (stack != null) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("slot", i);
                itemTag.putString("item", Registry.ITEM.getKey(stack.item).toString());
                itemTag.putInt("count", stack.amount);
                if (stack.damage > 0) {
                    itemTag.putInt("damage", stack.damage);
                }
                inventoryTag.add(itemTag);
            }
        }
        tag.put("inventory", inventoryTag);

        return tag;
    }

    /**
     * Reads player data from disk and applies it to the current player.
     */
    public static void readPlayerFromDisk(Player player, String worldFolderName) {
        FileHandle playerFile = WorldSaveManager.getPlayerFile(worldFolderName);

        if (!playerFile.exists()) {
            Logger.INFO("No player data found at %s, using fresh player", playerFile.path());
            return;
        }

        try {
            Logger.INFO("Loading player data from %s", playerFile.path());
            NamedTag namedTag = NBTUtil.read(playerFile.file());
            CompoundTag tag = (CompoundTag) namedTag.getTag();
            deserialisePlayer(player, tag);
        } catch (IOException e) {
            Logger.ERROR("Failed to load player data: %s", e.getMessage());
            throw new RuntimeException("Failed to load player data: " + e.getMessage(), e);
        }
    }

    /**
     * Applies saved player NBT values to a player instance.
     */
    public static void deserialisePlayer(Player player, CompoundTag tag) {
        player.x = tag.getDouble("x");
        player.y = tag.getDouble("y");
        player.xo = player.x;
        player.yo = player.y;
        player.xd = tag.getDouble("xd");
        player.yd = tag.getDouble("yd");
        player.slotSelected = tag.getInt("slotSelected");

        if (tag.containsKey("uuid")) {
            player.uuid = tag.getString("uuid");
        }

        player.setPos(player.x-0.4f, player.y);

        for (int i = 0; i <= 40; i++) {
            player.inventory.setItemSlot(i, null);
        }

        if (tag.containsKey("inventory")) {
            ListTag<CompoundTag> inventoryTag = tag.getListTag("inventory").asCompoundTagList();
            for (CompoundTag itemTag : inventoryTag) {
                int slot = itemTag.getInt("slot");
                String itemId = itemTag.getString("item");
                int count = itemTag.getInt("count");
                int damage = itemTag.containsKey("damage") ? itemTag.getInt("damage") : 0;

                Item item = Registry.ITEM.get(new ResourceLocation(itemId));
                if (item != null && slot >= 0 && slot <= 40) {
                    player.inventory.setItemSlot(slot, new ItemStack(item, count, damage));
                }
            }
        }

        if (player.inventory.getItemAtSlot(player.slotSelected - 1) != null) {
            player.blockInHand = player.inventory.getItemAtSlot(player.slotSelected - 1).item;
        } else {
            player.blockInHand = dev.alexco.minecraft.world.level.item.Items.AIR;
        }
    }
}
