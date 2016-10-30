package components;

public class Shop
{
  public final int npcId;
  public final String name;
  private final String origin;

  public String items;
  public int[] abilities;

  public Shop(int id, String n, String o) {
    npcId = id;
    name = n;
    origin = o;
  }

  public String getItems() { return items; }
  void setItems(String val) { items = val; }

  public int[] getAbilities() { return abilities; }
  void setAbilities(int[] val) { abilities = val; }

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
      sb.append(", items=[")
        .append(items)
        .append(']');
    }
    if (abilities!=null) {
      sb.append(", abilities=[")
        .append(abilities[0]);
      for (int i = 1 ; i<abilities.length ; i++) {
        sb.append(',')
          .append(abilities[i]);
      }
      sb.append(']');
    }
    sb.append(", Origin=").append(origin)
      .append('}');
    return sb.toString();
  }
}
