package dev.alexco.minecraft.crafting;

import java.util.ArrayList;
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
import dev.alexco.minecraft.tag.ItemTags;
import dev.alexco.minecraft.tag.Tag;
import dev.alexco.minecraft.util.Logger;
import dev.alexco.minecraft.world.level.item.Item;
import dev.alexco.registry.ResourceLocation;

public class RecipeLoader {
    private static final Map<ResourceLocation, Recipe> RECIPE_REGISTRY = new HashMap<>();
    private static final List<Recipe> SHAPED_RECIPES = new ArrayList<>();
    private static final List<Recipe> SHAPELESS_RECIPES = new ArrayList<>();
    private static volatile boolean loaded = false;

    /**
     * Loads crafting recipes from asset recipe files once.
     */
    public static void loadRecipes() {
        if (loaded) {
            return;
        }
        synchronized (RecipeLoader.class) {
            if (loaded) {
                return;
            }
            Logger.INFO("Loading recipes from assets...");

        // Read assets.txt to find recipe files
        FileHandle assetHandle = Gdx.files.internal("assets.txt");
        if (!assetHandle.exists()) {
            Logger.ERROR("assets.txt not found! Cannot load recipes.");
            return;
        }

        String text = assetHandle.readString();
        String[] lines = text.split("\n");

        int loadedCount = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("recipes/") && line.endsWith(".json")) {
                try {
                    loadRecipe(line);
                    loadedCount++;
                } catch (Exception e) {
                    Logger.ERROR("Failed to load recipe %s: %s", line, e.getMessage());
                }
            }
        }

        loaded = true;
        Logger.INFO("Loaded %d recipes (%d shaped, %d shapeless)",
            loadedCount, SHAPED_RECIPES.size(), SHAPELESS_RECIPES.size());
        }
    }

    /**
     * Parses a recipe file and registers the resulting recipe.
     */
    private static void loadRecipe(String path) {
        FileHandle fileHandle = Gdx.files.internal(path);
        if (!fileHandle.exists()) {
            Logger.ERROR("Recipe file not found: %s", path);
            return;
        }

        String json = fileHandle.readString();
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        String type = jsonObject.get("type").getAsString();
        String category = jsonObject.has("category") ? jsonObject.get("category").getAsString() : "misc";
        String group = jsonObject.has("group") ? jsonObject.get("group").getAsString() : "";

        // Parse result
        JsonObject resultObj = jsonObject.getAsJsonObject("result");
        String resultId = resultObj.get("id").getAsString();
        int resultCount = resultObj.has("count") ? resultObj.get("count").getAsInt() : 1;

        Item resultItem = Registry.ITEM.get(new ResourceLocation(resultId));
        if (resultItem == null) {
            Logger.ERROR("Unknown result item in recipe %s: %s", path, resultId);
            return;
        }
        ItemStack result = new ItemStack(resultItem, resultCount);

        Recipe recipe;
        if (type.equals("minecraft:crafting_shaped")) {
            recipe = parseShapedRecipe(jsonObject, category, group, result);
            if (recipe == null) {
                Logger.ERROR("Malformed crafting recipe: %s, skipping", path.substring(path.lastIndexOf('/') + 1));
                return;
            }
            SHAPED_RECIPES.add(recipe);
        } else if (type.equals("minecraft:crafting_shapeless")) {
            recipe = parseShapelessRecipe(jsonObject, category, group, result);
            SHAPELESS_RECIPES.add(recipe);
        } else {
            Logger.INFO("Unknown recipe type %s in %s", type, path);
            return;
        }

        // Extract recipe name from path
        String recipeName = path.substring(path.lastIndexOf('/') + 1).replace(".json", "");
        ResourceLocation recipeId = new ResourceLocation("minecraft", recipeName);
        RECIPE_REGISTRY.put(recipeId, recipe);

       // Logger.INFO("Loaded recipe: %s (%s)", recipeName, type);
    }

        /**
         * Parses a shaped crafting recipe definition.
         */
    private static ShapedRecipe parseShapedRecipe(JsonObject json, String category, String group, ItemStack result) {
        JsonArray patternArray = json.getAsJsonArray("pattern");
        String[] pattern = new String[patternArray.size()];
        for (int i = 0; i < patternArray.size(); i++) {
            pattern[i] = patternArray.get(i).getAsString();
        }

        if (pattern.length > 0) {
            int expectedWidth = pattern[0].length();
            for (int i = 1; i < pattern.length; i++) {
                if (pattern[i].length() != expectedWidth) {
                    return null;
                }
            }
        }

        Map<Character, ShapedRecipe.Ingredient> key = new HashMap<>();
        JsonObject keyObj = json.getAsJsonObject("key");
        for (Map.Entry<String, JsonElement> entry : keyObj.entrySet()) {
            char keyChar = entry.getKey().charAt(0);
            JsonElement valueElement = entry.getValue();

            ShapedRecipe.Ingredient ingredient = parseIngredient(valueElement);
            if (ingredient != null) {
                key.put(keyChar, ingredient);
            }
        }

        return new ShapedRecipe(category, group, pattern, key, result);
    }

    /**
     * Parses a shapeless crafting recipe definition.
     */
    private static ShapelessRecipe parseShapelessRecipe(JsonObject json, String category, String group, ItemStack result) {
        List<ShapedRecipe.Ingredient> ingredients = new ArrayList<>();
        JsonArray ingredientsArray = json.getAsJsonArray("ingredients");

        for (JsonElement element : ingredientsArray) {
            ShapedRecipe.Ingredient ingredient = parseIngredient(element);
            if (ingredient != null) {
                ingredients.add(ingredient);
            }
        }

        return new ShapelessRecipe(category, group, ingredients, result);
    }

    /**
     * Parses one ingredient entry into an item or tag matcher.
     */
    private static ShapedRecipe.Ingredient parseIngredient(JsonElement element) {
        String value;
        if (element.isJsonPrimitive()) {
            value = element.getAsString();
        } else if (element.isJsonObject()) {
            // Handle object format with "item" or "tag" field
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("item")) {
                value = obj.get("item").getAsString();
            } else if (obj.has("tag")) {
                value = "#" + obj.get("tag").getAsString();
            } else {
                Logger.ERROR("Ingredient object must have 'item' or 'tag' field");
                return null;
            }
        } else {
            Logger.ERROR("Invalid ingredient format");
            return null;
        }

        if (value.startsWith("#")) {
            // It's a tag
            String tagId = value.substring(1);
            Tag<Item> tag = ItemTags.getTag(new ResourceLocation(tagId));
            return ShapedRecipe.Ingredient.ofTag(tag);
        } else {
            // It's an item
            Item item = Registry.ITEM.get(new ResourceLocation(value));
            if (item == null) {
                Logger.ERROR("Unknown item in recipe: %s", value);
                return null;
            }
            return ShapedRecipe.Ingredient.ofItem(item);
        }
    }

    /**
     * Returns the first recipe that matches the current grid input.
     */
    public static Recipe findMatchingRecipe(CraftingInput input) {
        // Check shapeless recipes first
        for (Recipe recipe : SHAPELESS_RECIPES) {
            if (recipe.matches(input)) {
                return recipe;
            }
        }

        // Check shaped recipes
        for (Recipe recipe : SHAPED_RECIPES) {
            if (recipe.matches(input)) {
                return recipe;
            }
        }

        return null;
    }

    public static List<Recipe> getAllRecipes() {
        List<Recipe> all = new ArrayList<>();
        all.addAll(SHAPED_RECIPES);
        all.addAll(SHAPELESS_RECIPES);
        return all;
    }

    public static List<Recipe> getShapedRecipes() {
        return new ArrayList<>(SHAPED_RECIPES);
    }

    public static List<Recipe> getShapelessRecipes() {
        return new ArrayList<>(SHAPELESS_RECIPES);
    }

    public static Recipe getRecipe(ResourceLocation id) {
        return RECIPE_REGISTRY.get(id);
    }

    public static void clear() {
        RECIPE_REGISTRY.clear();
        SHAPED_RECIPES.clear();
        SHAPELESS_RECIPES.clear();
        loaded = false;
    }
}
