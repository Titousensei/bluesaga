package data_handlers.crafting_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import utils.ServerGameInfo;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.SkillHandler;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.Item;
import network.Client;
import network.Server;

public class CraftingHandler extends Handler {

  private static Map<Integer, Recipe> recipes;

  public static void init() {

    // LOAD SHOP INFO
    recipes = new HashMap<>();
    for (File f : new File(ServerSettings.PATH).listFiles((dir, name) -> name.startsWith("recipes_"))) {
      Builder.load(f.getPath(), ShopBuilder.class, shopDef);
    }


    recipes = new HashMap<Integer, Recipe>();

    try (
        ResultSet recipesInfo = Server.gameDB.askDB("select * from recipe")
    ) {
      while (recipesInfo.next()) {
        int recipeId = recipesInfo.getInt("RecipeId");
        int productId = recipesInfo.getInt("ProductId");

        Recipe newRecipe = new Recipe(recipeId, ServerGameInfo.itemDef.get(productId));

        CraftingStation bonfire = new CraftingStation(recipesInfo.getString("CraftingStation"));
        bonfire.setSkillId(9);
        newRecipe.setCraftingStation(bonfire);

        String[] ingredients = recipesInfo.getString("Materials").split(";");

        for (String ingredient : ingredients) {
          String ingredientInfo[] = ingredient.split(",");
          int ingredientId = Integer.parseInt(ingredientInfo[0]);
          int ingredientNr = Integer.parseInt(ingredientInfo[1]);

          Item ingredientItem = ServerGameInfo.itemDef.get(ingredientId);
          if (ingredientItem != null) {
            ingredientItem.setStacked(ingredientNr);
            newRecipe.addMaterial(ingredientItem);
          }
          else {
            System.err.println("[CraftingHandler] ERROR - Ingredient unknown in recipe "
                + recipeId + ": " + ingredientId);
          }
        }
        recipes.put(recipeId, newRecipe);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    DataHandlers.register("getrecipe", m -> handleGetRecipe(m));
    DataHandlers.register("craftitem", m -> handleCraftItem(m));
  }

  public static void handleGetRecipe(Message m) {
    Client client = m.client;
    String craftingStationInfo = m.message;

    String craftingStationId = "fire";
    String craftingStationName = "Bonfire";

    if (craftingStationInfo.contains("fire")) {
      craftingStationId = "fire";
      craftingStationName = "Bonfire";
    }

    List<Recipe> availableRecipes = new ArrayList<>();

    ResultSet recipesInfo =
        Server.userDB.askDB(
            "select RecipeId from character_recipe where CharacterId = "
                + client.playerCharacter.getDBId());

    StringBuilder recipesToSend = new StringBuilder(1000);
    recipesToSend.append(craftingStationName).append('/');

    try {
      while (recipesInfo.next()) {
        if (recipes
            .get(recipesInfo.getInt("RecipeId"))
            .getCraftingStation()
            .getName()
            .equals(craftingStationId)) {
          availableRecipes.add(recipes.get(recipesInfo.getInt("RecipeId")));
        }
      }
      recipesInfo.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    for (Recipe recipe : availableRecipes) {
      recipesToSend
          .append(recipe.getProduct().getId())
          .append(',')
          .append(recipe.getProduct().getName())
          .append(';');
    }

    if (recipesToSend.length() > 0) {
      addOutGoingMessage(client, "recipes", recipesToSend.toString());
    }
  }

  public static void handleCraftItem(Message m) {
    Client client = m.client;
    int itemId = Integer.parseInt(m.message);

    // Get recipe for item
    Recipe itemRecipe = null;
    for (Recipe recipe : recipes.values()) {
      if (recipe.getProduct().getId() == itemId) {
        itemRecipe = recipe;
        break;
      }
    }

    if (itemRecipe != null) {
      // Check if player has ingredients for recipe
      boolean hasIngredients = true;
      StringBuilder ingredients = new StringBuilder(1000);
      int nrIngredients = 0;
      for (Item ingredient : itemRecipe.getMaterials()) {
        if (nrIngredients > 0) {
          ingredients.append(", ");
        }
        ingredients.append(ingredient.getStacked()).append(' ').append(ingredient.getName());
        if (!client.playerCharacter.hasItem(ingredient.getId(), ingredient.getStacked())) {
          hasIngredients = false;
        }
        nrIngredients++;
      }

      if (hasIngredients) {
        // Remove ingredients from inventory
        for (Item ingredient : itemRecipe.getMaterials()) {
          InventoryHandler.removeNumberOfItems(client, ingredient.getId(), ingredient.getStacked());
        }
        // Add crafted product in inventory
        InventoryHandler.addItemToInventory(client, itemRecipe.getProduct());

        // Give sp to crafting skill
        int skillId = itemRecipe.getCraftingStation().getSkillId();

        SkillHandler.gainSP(client, skillId, false);

        addOutGoingMessage(client, "crafting_done", "");
      } else {
        addOutGoingMessage(client, "nocraftitem", ingredients.toString());
      }
    }
  }
}
