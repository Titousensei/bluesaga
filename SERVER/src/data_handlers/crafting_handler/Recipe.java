package data_handlers.crafting_handler;

import java.util.*;

import data_handlers.item_handler.Item;

public class Recipe {
  private CraftingStation craftingStation;
  private List<Item> materials = new ArrayList<>();
  private Item product;

  public final int id;
  public final String name;
  private final String origin;

  public int[] abilities;
  public int skillLevel = 1;
  public String ingredientStr;

  public Recipe(int i, String n, String o) {
    id = i;
    name = n;
    origin = o;
  }

  public void addMaterial(Item material) {
    materials.add(material);
  }

  public void setSkillLevel(int val) {
    skillLevel = val;
  }

  public Item getProduct() {
    return product;
  }

  public void setProduct(Item val) {
    product = val;
  }

  public void setIngredientStr(String val) {
    ingredientStr = val;
  }

  public CraftingStation getCraftingStation() {
    return craftingStation;
  }

  public void setCraftingStation(CraftingStation craftingStation) {
    this.craftingStation = craftingStation;
  }

  public List<Item> getMaterials() {
    return materials;
  }

  @Override
  public String toString() {
    return name + " (" + id +")";
  }
}
