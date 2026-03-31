package dev.alexco.minecraft.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.registry.Registry;
import dev.alexco.minecraft.tag.ItemTags;
import dev.alexco.minecraft.tag.Tag;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.registry.ResourceLocation;


public class FurnaceRecipeLoader {
    private static final Map<ResourceLocation, FurnaceRecipe> RECIPE_REGISTRY = new HashMap<>();
    private static final List<FurnaceRecipe> RECIPES = new ArrayList<>();
    private static volatile boolean loaded = false;

    /**
     * Loads furnace recipes from listed asset recipe files once.
     */
    public static void loadRecipes() {
        if (loaded) {
            return;
        }
        synchronized (FurnaceRecipeLoader.class) {
            if (loaded) {
                return;
            }
            Logger.INFO("Loading furnace recipes from assets...");


        FileHandle assetHandle = Gdx.files.internal("assets.txt");
        if (!assetHandle.exists()) {
            Logger.ERROR("assets.txt not found! Cannot load furnace recipes.");
            return;
        }

        String text = assetHandle.readString();
        String[] lines = text.split("\n");

        int loadedCount = 0;
        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("recipes/") && line.endsWith(".json")) {
                try {
                    if (loadRecipe(line)) {
                        loadedCount++;
                    }
                } catch (Exception e) {
                    Logger.ERROR("Failed to load furnace recipe %s: %s", line, e.getMessage());
                }
            }
        }

        loaded = true;
        Logger.INFO("Loaded %d furnace recipes", loadedCount);
        }
    }

    /**
     * Parses one recipe file and registers it if it is a furnace recipe.
     */
    private static boolean loadRecipe(String path) {
        FileHandle fileHandle = Gdx.files.internal(path);
        if (!fileHandle.exists()) {
            return false;
        }

        String json = fileHandle.readString();
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        String type = jsonObject.get("type").getAsString();

        // Only process smelting and blasting recipes
        if (!type.equals("minecraft:smelting") && !type.equals("minecraft:blasting")) {
            return false;
        }

        String category = jsonObject.has("category") ? jsonObject.get("category").getAsString() : "misc";
        String group = jsonObject.has("group") ? jsonObject.get("group").getAsString() : "";
        int cookingTime = jsonObject.has("cookingtime") ? jsonObject.get("cookingtime").getAsInt() : 200;
        float experience = jsonObject.has("experience") ? jsonObject.get("experience").getAsFloat() : 0.0f;

        JsonObject resultObj = jsonObject.getAsJsonObject("result");
        String resultId = resultObj.get("id").getAsString();
        int resultCount = resultObj.has("count") ? resultObj.get("count").getAsInt() : 1;

        Item resultItem = Registry.ITEM.get(new ResourceLocation(resultId));
        if (resultItem == null) {
            Logger.ERROR("Unknown result item in furnace recipe %s: %s", path, resultId);
            return false;
        }
        ItemStack result = new ItemStack(resultItem, resultCount);

        Item ingredient = parseIngredient(jsonObject.get("ingredient"));
        if (ingredient == null) {
            Logger.ERROR("Failed to parse ingredient in furnace recipe %s", path);
            return false;
        }

        FurnaceRecipe recipe = new FurnaceRecipe(ingredient, result, cookingTime, experience, category, group);

        String recipeName = path.substring(path.lastIndexOf('/') + 1).replace(".json", "");
        ResourceLocation recipeId = new ResourceLocation("minecraft", recipeName);
        RECIPE_REGISTRY.put(recipeId, recipe);
        RECIPES.add(recipe);

        Logger.INFO("Loaded furnace recipe: %s", recipeName);
        return true;
    }

    /**
     * Resolves an ingredient item from primitive or object JSON forms.
     */
    private static Item parseIngredient(com.google.gson.JsonElement element) {
        if (element.isJsonPrimitive()) {
            String itemId = element.getAsString();
            return Registry.ITEM.get(new ResourceLocation(itemId));
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("item")) {
                String itemId = obj.get("item").getAsString();
                return Registry.ITEM.get(new ResourceLocation(itemId));
            } else if (obj.has("tag")) {
                // For tags, we'll use the first item in the tag as the ingredient
                String tagId = obj.get("tag").getAsString();
                Tag<Item> tag = ItemTags.getTag(new ResourceLocation(tagId));
                if (tag != null && !tag.values().isEmpty()) {
                    return tag.values().iterator().next();
                }
            }
        }
        return null;
    }

    /**
     * Finds the first furnace recipe matching an input item.
     */
    public static FurnaceRecipe findRecipe(Item input) {
        for (FurnaceRecipe recipe : RECIPES) {
            if (recipe.matches(input)) {
                return recipe;
            }
        }
        return null;
    }


    public static FurnaceRecipe findRecipe(ItemStack input) {
        if (input == null) return null;
        return findRecipe(input.item);
    }

    public static List<FurnaceRecipe> getAllRecipes() {
        return new ArrayList<>(RECIPES);
    }

    public static FurnaceRecipe getRecipe(ResourceLocation id) {
        return RECIPE_REGISTRY.get(id);
    }

    public static void clear() {
        RECIPE_REGISTRY.clear();
        RECIPES.clear();
        loaded = false;
    }
}
