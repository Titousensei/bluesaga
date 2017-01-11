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
    Item material = ServerGameInfo.itemDef.get(parseInt(val));
    if (material != null) {
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
//    s.setSkillLevel(parseInt(val));
  }

  public Recipe build() {
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
