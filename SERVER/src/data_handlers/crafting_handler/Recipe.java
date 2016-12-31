package data_handlers.crafting_handler;

import java.util.*;

import data_handlers.item_handler.Item;

public class Recipe {
  private CraftingStation craftingStation;
  private List<Item> materials = new ArrayList<>();
  private Item product;

  public final int npcId;
  public final String name;
  private final String origin;

  public String items;
  public int[] abilities;

  public Recipe(int id, String n, String o) {
    npcId = id;
    name = n;
    origin = o;
  }

  //public Recipe(int id, Item product) {
  //  this.product = product;
 // }

  public void addMaterial(Item material) {
    materials.add(material);
  }

  public Item getProduct() {
    return product;
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
}
