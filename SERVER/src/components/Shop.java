package components;

import java.util.Collection;

import data_handlers.item_handler.Item;

public class Shop
{
  public final int npcId;
  public final String name;
  protected final String origin;

  protected Collection<Integer> itemIds;
  protected String itemsStr;
  protected Collection<Integer> abilityIds;
  protected String abilitiesStr;

  public Shop(int id, String n, String o) {
    npcId = id;
    name = n;
    origin = o;
    itemIds = null;
    itemsStr = "None";
    abilityIds = null;
    abilitiesStr = "None";
  }

  public void init() {}

  public boolean addItem(Item it, int characterId) { return true; }

  public boolean removeItem(Item it, int characterId) { return true; }

  public int getSkillId() { return 0; }

  void setItems(Collection<Integer> val) { itemIds = val; }
  public boolean containsItem(Item it) {
    return itemIds.contains(it.getId());
  }

  public String getItemsStr() { return itemsStr; }
  void setItemsStr(String val) { itemsStr = val; }

  void setAbilities(Collection<Integer> val) { abilityIds = val; }
  public boolean containsAbility(Integer val) { return abilityIds.contains(val); }

  public String getAbilitiesStr() { return abilitiesStr; }
  void setAbilitiesStr(String val) { abilitiesStr = val; }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(1000);
    sb.append("Shop{")
      .append(npcId);
    if (name!=null && !"".equals(name)) {
      sb.append(" / ")
        .append(name);
    }
    if (itemIds!=null) {
      sb.append(", items=")
        .append(itemIds);
    }
    if (abilityIds!=null) {
      sb.append(", abilities=")
        .append(abilityIds);
    }
    sb.append(", Origin=").append(origin)
      .append('}');
    return sb.toString();
  }
}
