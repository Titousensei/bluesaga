package data_handlers.crafting_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.io.File;

import components.Builder;
import game.ServerSettings;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.SkillHandler;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.Item;
import network.Client;
import network.Server;
import utils.ServerGameInfo;
import utils.ServerMessage;

public class CraftingHandler extends Handler {

  private static Map<Integer, Recipe> recipes;

  public static void init() {

    // LOAD RECIPES INFO
    recipes = new HashMap<>();
    for (File f : new File(ServerSettings.PATH).listFiles((dir, name) -> name.startsWith("recipes_"))) {
      Builder.load(f.getPath(), RecipeBuilder.class, recipes);
    }

    DataHandlers.register("getrecipe", m -> handleGetRecipe(m));
    DataHandlers.register("craftitem", m -> handleCraftItem(m));
  }

  public static void handleGetRecipe(Message m) {
    Client client = m.client;
    String[] craftingStationInfo = m.message.split(",");
    CraftingStation cs = CraftingStation.find(craftingStationInfo[0]);
    if (cs == null) {
      ServerMessage.println(false, "ERROR - No such CraftingStation: ", m.message);
      return;
    }
    String craftingStationName = cs.toString();

    List<Recipe> availableRecipes = new ArrayList<>();

    StringBuilder recipesToSend = new StringBuilder(1000);
    recipesToSend.append(craftingStationName).append('/');

    try (ResultSet recipesInfo =
        Server.userDB.askDB(
            "select RecipeId from character_recipe where CharacterId = "
                + client.playerCharacter.getDBId());
    ) {
      while (recipesInfo.next()) {
        Recipe recipe = recipes.get(recipesInfo.getInt(1));
        if (recipe.getCraftingStation() == cs) {
          recipesToSend
              .append(recipe.getProduct().getId())
              .append(',')
              .append(recipe.getProduct().getName())
              .append(';');
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    addOutGoingMessage(client, "recipes", recipesToSend.toString());
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
        int skillId = itemRecipe.getCraftingStation().skillId;

        SkillHandler.gainSP(client, skillId, false);

        addOutGoingMessage(client, "crafting_done", "");
        ServerMessage.println(false, "Crafting - ", client.playerCharacter, ": ", itemRecipe);
      } else {
        addOutGoingMessage(client, "nocraftitem", ingredients.toString());
      }
    }
  }
}
