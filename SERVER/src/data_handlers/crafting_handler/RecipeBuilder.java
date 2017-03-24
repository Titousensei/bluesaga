package data_handlers.crafting_handler;

import java.util.*;

import components.Builder;
import data_handlers.item_handler.Item;
import utils.ServerGameInfo;
import utils.ServerMessage;

public class RecipeBuilder
extends Builder<Recipe>
{
  protected Recipe r = null;

  public void init(int id, String name, String origin) {
    r = new Recipe(id, name, origin);
  }

  public void productId(String val) {
    Item it = ServerGameInfo.itemDef.get(parseInt(val));
    r.setProduct(it);
  }

  public void material(String val) {
    String[] parts = val.split("\\*");
    Item material = ServerGameInfo.itemDef.get(parseInt(parts[0]));
    if (material != null) {
      if (parts.length > 1) {
        material.setStacked(parseInt(parts[1]));
      }
      r.addMaterial(material);
    }
    else {
      System.out.println("[RecipeBuilder] ERROR - Unknown material for " + r + ":" + val);
    }
  }

  public void craftingStation(String val) {
    r.setCraftingStation(Enum.valueOf(CraftingStation.class, val));
  }

  public void skillLevel(String val) {
    r.setSkillLevel(parseInt(val));
  }

  public Recipe build() {
    StringBuilder sb = null;
    for (Item ingredient : r.getMaterials()) {
      if (sb == null) {
        sb = new StringBuilder(1000);
      } else {
        sb.append(", ");
      }
      sb.append(ingredient.getStacked())
        .append(' ')
        .append(ingredient.getName());
    }
    r.setIngredientStr(sb.toString());

    return r;
  }

  public static void main(String... args) {
    Map<Integer, Recipe> m = new HashMap<>();
    Builder.load(args[0], RecipeBuilder.class, m);
    if (args.length>1) {
      if ("-".equals(args[1])) {
        for (Recipe r : m.values()) {
          System.out.println(r);
        }
      }
      else {
        System.out.println(m.get(parseInt(args[1])));
      }
    }
  }
}
