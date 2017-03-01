package components;

import java.util.Set;

public class Shop
{
  public final int npcId;
  public final String name;
  private final String origin;

  private Set<Integer> items;
  private String itemsStr;
  private Set<Integer> abilities;
  private String abilitiesStr;

  public Shop(int id, String n, String o) {
    npcId = id;
    name = n;
    origin = o;
    itemsStr = "None";
    abilities = null;
    abilitiesStr = "None";
  }

  void setItems(Set<Integer> val) { items = val; }
  public boolean containsItem(Integer val) { return items.contains(val); }

  public String getItemsStr() { return itemsStr; }
  void setItemsStr(String val) { itemsStr = val; }

  void setAbilities(Set<Integer> val) { abilities = val; }
  public boolean containsAbility(Integer val) { return abilities.contains(val); }

  public String getAbilitiesStr() { return abilitiesStr; }
  void setAbilitiesStr(String val) { abilitiesStr = val; }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(1000);
    sb.append("Show{")
      .append(npcId);
    if (name!=null && !"".equals(name)) {
      sb.append(" / ")
        .append(name);
    }
    if (items!=null) {
      sb.append(", items=")
        .append(items);
    }
    if (abilities!=null) {
      sb.append(", abilities=")
        .append(abilities);
    }
    sb.append(", Origin=").append(origin)
      .append('}');
    return sb.toString();
  }
}
