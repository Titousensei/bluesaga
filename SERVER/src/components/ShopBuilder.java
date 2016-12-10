package components;

import java.util.*;

import data_handlers.item_handler.Item;
import utils.ServerGameInfo;

public class ShopBuilder
extends Builder<Shop>
{
  protected Shop s = null;
  protected StringBuilder items = null;
  protected List<Integer> abilities = null;

  public void init(int id, String name, String origin) {
    s = new Shop(id, name, origin);
  }

  public void items(String val) {
    if (items==null) {
      items = new StringBuilder();
    }
    else {
      items.append(',');
    }
    items.append(val);
  }

  public void item(String val) {
    int id = parseInt(val);
    if (id!=0) {
      if (items==null) {
        items = new StringBuilder(100);
      }
      else {
        items.append(',');
      }
      items.append(id);
    }
  }

  public void abilities(String val) {
    String abilityIds[] = val.split(",");
    abilities = new ArrayList<>(abilityIds.length);
    for (int i = 0 ; i<abilityIds.length ; i++) {
      abilities.add(parseInt(abilityIds[i]));
    }
  }

  public void ability(String val) {
    int id = parseInt(val);
    if (id!=0) {
      if (abilities==null) {
        abilities = new ArrayList<>(10);
      }
      abilities.add(id);
    }
  }

  public Shop build() {
    if (items!=null) {
      s.setItems(items.toString());
    }
    if (abilities!=null) {
      int[] ab = new int[abilities.size()];
      int i = 0;
      for (Integer val : abilities) {
        ab[i] = val;
        ++ i;
      }
      s.setAbilities(ab);
    }
    return s;
  }

  public static void main(String... args) {
    Map<Integer, Shop> m = new HashMap<>();
    Builder.load(args[0], ShopBuilder.class, m);
    if (args.length>1) {
      if ("-".equals(args[1])) {
        for (Shop q : m.values()) {
          System.out.println(q);
        }
      }
      else {
        System.out.println(m.get(parseInt(args[1])));
      }
    }
  }
}
