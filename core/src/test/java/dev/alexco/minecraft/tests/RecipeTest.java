package dev.alexco.minecraft.tests;

import dev.alexco.minecraft.Assert;
import dev.alexco.minecraft.TestCase;
import dev.alexco.minecraft.crafting.CraftingInput;
import dev.alexco.minecraft.crafting.CraftingManager;
import dev.alexco.minecraft.crafting.Recipe;
import dev.alexco.minecraft.crafting.RecipeLoader;
import dev.alexco.minecraft.crafting.ShapedRecipe;
import dev.alexco.minecraft.crafting.ShapelessRecipe;
import dev.alexco.minecraft.inventory.ItemStack;
import dev.alexco.minecraft.world.level.item.Items;

public class RecipeTest extends TestCase {
    public RecipeTest() {
        super("Recipe");
    }

    @Override
    protected void test() throws Exception {
        // Test 3x3 crafting input
        CraftingInput input = new CraftingInput(CraftingInput.SIZE_3X3);
        Assert.assertNotNull(input, "CraftingInput not null");
        Assert.assertEquals(input.getSize(), 3, "Size is 3");
        Assert.assertTrue(input.isEmpty(), "Input is initially empty");

        // Test 2x2 input
        CraftingInput input2x2 = new CraftingInput(CraftingInput.SIZE_2X2);
        Assert.assertNotNull(input2x2, "2x2 CraftingInput not null");
        Assert.assertEquals(input2x2.getSize(), 2, "Size is 2");

        // Test shapeless recipe - oak planks from oak log
        input.setItem(0, 0, new ItemStack(Items.OAK_LOG, 1));
        
        // Note: RecipeLoader.loadRecipes() needs to be called before this works
        // Since we can't load Gdx.files in tests, we test the recipe classes directly
        
        // Test ShapedRecipe
        String[] pattern = {"##", "##"};
        java.util.Map<Character, ShapedRecipe.Ingredient> key = new java.util.HashMap<>();
        key.put('#', ShapedRecipe.Ingredient.ofItem(Items.OAK_PLANKS));
        ItemStack result = new ItemStack(Items.CRAFTING_TABLE, 1);
        
        ShapedRecipe shapedRecipe = new ShapedRecipe("misc", "", pattern, key, result);
        Assert.assertNotNull(shapedRecipe, "ShapedRecipe created");
        Assert.assertEquals(shapedRecipe.getWidth(), 2, "Pattern width is 2");
        Assert.assertEquals(shapedRecipe.getHeight(), 2, "Pattern height is 2");
        Assert.assertEquals(shapedRecipe.getResultItem(), Items.CRAFTING_TABLE, "Result is crafting table");
        Assert.assertEquals(shapedRecipe.getResultCount(), 1, "Result count is 1");
        
        // Test shapeless recipe
        java.util.List<ShapedRecipe.Ingredient> ingredients = new java.util.ArrayList<>();
        ingredients.add(ShapedRecipe.Ingredient.ofItem(Items.OAK_LOG));
        ItemStack shapelessResult = new ItemStack(Items.OAK_PLANKS, 4);
        
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe("building", "planks", ingredients, shapelessResult);
        Assert.assertNotNull(shapelessRecipe, "ShapelessRecipe created");
        Assert.assertEquals(shapelessRecipe.getIngredientCount(), 1, "Has 1 ingredient");
        Assert.assertEquals(shapelessRecipe.getResultItem(), Items.OAK_PLANKS, "Result is oak planks");
        Assert.assertEquals(shapelessRecipe.getResultCount(), 4, "Result count is 4");
        
        // Test ingredient matching
        Assert.assertTrue(ingredients.get(0).matches(Items.OAK_LOG), "Ingredient matches oak log");
        Assert.assertFalse(ingredients.get(0).matches(Items.STONE), "Ingredient does not match stone");
        
        // Test tag ingredient
        ShapedRecipe.Ingredient tagIngredient = ShapedRecipe.Ingredient.ofTag(dev.alexco.minecraft.tag.ItemTags.PLANKS);
        Assert.assertTrue(tagIngredient.isTag(), "Is a tag ingredient");
        Assert.assertTrue(tagIngredient.matches(Items.OAK_PLANKS), "Tag matches oak planks");
        Assert.assertTrue(tagIngredient.matches(Items.BAMBOO_PLANKS), "Tag matches bamboo planks");
        Assert.assertFalse(tagIngredient.matches(Items.STONE), "Tag does not match stone");
        
        System.out.println("Recipe system tests passed!");
    }
}
