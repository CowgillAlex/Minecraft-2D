package dev.alexco.minecraft.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.block.BlockState;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.registry.ResourceLocation;

public class LootTableManager {
    private static final Map<ResourceLocation, LootTable> LOOT_TABLES = new HashMap<>();
    private static volatile boolean loaded = false;

    /**
     * Loads every loot table declared in assets.txt exactly once.
     */
    public static void loadLootTables() {
        if (loaded) {
            return;
        }
        synchronized (LootTableManager.class) {
            if (loaded) {
                return;
            }
            Logger.INFO("Loading loot tables from assets...");
        FileHandle assetHandle = Gdx.files.internal("assets.txt");
        if (!assetHandle.exists()) {
            Logger.ERROR("assets.txt not found! Cannot load loot tables.");
            return;
        }

        String text = assetHandle.readString();
        String[] lines = text.split("\n");

        int loadedCount = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("loot_tables/") && line.endsWith(".json")) {
                try {
                    loadLootTable(line);
                    loadedCount++;
                } catch (Exception e) {
                    Logger.ERROR("Failed to load loot table %s: %s", line, e.getMessage());
                }
            }
        }

        loaded = true;
        Logger.INFO("Loaded %d loot tables", loadedCount);
        }
    }

    /**
     * Reads one loot table file and registers it by its resource location.
     */
    private static void loadLootTable(String path) {
        FileHandle fileHandle = Gdx.files.internal(path);
        if (!fileHandle.exists()) {
            Logger.ERROR("Loot table file not found: %s", path);
            return;
        }

        String json = fileHandle.readString();
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        ResourceLocation lootTableId = toLootTableId(path);
        List<LootPool> pools = parsePools(jsonObject, path);
        LootTable table = new LootTable(lootTableId, pools);
        LOOT_TABLES.put(lootTableId, table);

        Logger.INFO("Loaded loot table: %s", lootTableId);
    }

    /**
     * Parses pool definitions from a loot table JSON object.
     */
    private static List<LootPool> parsePools(JsonObject jsonObject, String sourcePath) {
        if (!jsonObject.has("pools") || !jsonObject.get("pools").isJsonArray()) {
            return Collections.emptyList();
        }

        List<LootPool> pools = new ArrayList<>();
        JsonArray poolArray = jsonObject.getAsJsonArray("pools");
        for (JsonElement poolElement : poolArray) {
            if (!poolElement.isJsonObject()) {
                continue;
            }
            JsonObject poolObject = poolElement.getAsJsonObject();

            List<LootCondition> conditions = parseConditions(poolObject);
            List<LootEntry> entries = parseEntries(poolObject, sourcePath);
            boolean selectOne = poolObject.has("select_one") && poolObject.get("select_one").getAsBoolean();
            pools.add(new LootPool(conditions, entries, selectOne));
        }
        return pools;
    }

    /**
     * Builds runtime loot conditions from one pool definition.
     */
    private static List<LootCondition> parseConditions(JsonObject poolObject) {
        if (!poolObject.has("conditions") || !poolObject.get("conditions").isJsonArray()) {
            return Collections.emptyList();
        }

        List<LootCondition> conditions = new ArrayList<>();
        JsonArray conditionArray = poolObject.getAsJsonArray("conditions");
        for (JsonElement conditionElement : conditionArray) {
            if (!conditionElement.isJsonObject()) {
                continue;
            }
            JsonObject conditionObject = conditionElement.getAsJsonObject();
            String conditionType = conditionObject.has("condition")
                ? conditionObject.get("condition").getAsString()
                : "minecraft:always_true";

            LootCondition condition = switch (conditionType) {
                case "minecraft:tool_tier_at_least" -> {
                    ToolType toolType = ToolType.fromString(
                        conditionObject.has("tool") ? conditionObject.get("tool").getAsString() : null
                    );
                    ToolTier minTier = ToolTier.fromString(
                        conditionObject.has("tier") ? conditionObject.get("tier").getAsString() : null
                    );
                    yield new ToolTierAtLeastCondition(toolType, minTier);
                }
                case "minecraft:random_chance" -> new RandomChanceCondition(
                    conditionObject.has("chance") ? conditionObject.get("chance").getAsFloat() : 0f
                );
                case "minecraft:block_state" -> new BlockStateCondition(parseStateConditionMap(conditionObject));
                case "minecraft:block_state_not" -> new BlockStateCondition(parseStateConditionMap(conditionObject), false);
                case "minecraft:always_true" -> new AlwaysTrueCondition();
                default -> {
                    Logger.INFO("Unknown loot condition %s, defaulting to false", conditionType);
                    yield context -> false;
                }
            };
            conditions.add(condition);
        }
        return conditions;
    }

    /**
     * Converts a block-state condition object into property name/value pairs.
     */
    private static Map<String, String> parseStateConditionMap(JsonObject conditionObject) {
        if (!conditionObject.has("state") || !conditionObject.get("state").isJsonObject()) {
            return Collections.emptyMap();
        }

        Map<String, String> stateConditions = new HashMap<>();
        JsonObject stateObject = conditionObject.getAsJsonObject("state");
        for (Map.Entry<String, JsonElement> stateEntry : stateObject.entrySet()) {
            JsonElement valueElement = stateEntry.getValue();
            if (valueElement == null || valueElement.isJsonNull()) {
                continue;
            }
            if (valueElement.isJsonPrimitive()) {
                stateConditions.put(stateEntry.getKey(), valueElement.getAsString());
            } else {
                stateConditions.put(stateEntry.getKey(), valueElement.toString());
            }
        }
        return stateConditions;
    }

    /**
     * Parses item entries for a pool and resolves them through the item registry.
     */
    private static List<LootEntry> parseEntries(JsonObject poolObject, String sourcePath) {
        if (!poolObject.has("entries") || !poolObject.get("entries").isJsonArray()) {
            return Collections.emptyList();
        }

        List<LootEntry> entries = new ArrayList<>();
        JsonArray entryArray = poolObject.getAsJsonArray("entries");
        for (JsonElement entryElement : entryArray) {
            if (!entryElement.isJsonObject()) {
                continue;
            }
            JsonObject entryObject = entryElement.getAsJsonObject();
            if (!entryObject.has("item")) {
                continue;
            }

            String itemId = entryObject.get("item").getAsString();
            int count = entryObject.has("count") ? entryObject.get("count").getAsInt() : 1;
            int weight = entryObject.has("weight") ? entryObject.get("weight").getAsInt() : 1;
            Item item = Registry.ITEM.get(new ResourceLocation(itemId));
            if (item == null) {
                Logger.ERROR("Unknown item '%s' in loot table %s", itemId, sourcePath);
                continue;
            }
            entries.add(new LootEntry(item, count, weight));
        }
        return entries;
    }

    /**
     * Maps a loot table file path to its runtime resource location id.
     */
    private static ResourceLocation toLootTableId(String path) {
        String tablePath = path.substring("loot_tables/".length(), path.length() - ".json".length());
        return new ResourceLocation("minecraft", tablePath);
    }

    public static List<ItemStack> getDrops(BlockState blockState, Item toolItem) {
        ResourceLocation lootTableId = blockState.getBlock().getLootTable();
        return getDropsFromTable(lootTableId, blockState, toolItem);
    }

    /**
     * Generates drops from the selected table, with block-item fallback when missing.
     */
    public static List<ItemStack> getDropsFromTable(ResourceLocation lootTableId, BlockState blockState, Item toolItem) {
        LootTable lootTable = LOOT_TABLES.get(lootTableId);
        if (lootTable == null) {
            if (blockState == null) {
                return Collections.emptyList();
            }
            Item fallback = Item.BY_BLOCK.get(blockState.getBlock());
            if (fallback == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(new ItemStack(fallback));
        }

        LootContext context = new LootContext(blockState, toolItem);
        return lootTable.generateDrops(context);
    }

    public static void clear() {
        LOOT_TABLES.clear();
        loaded = false;
    }
}
