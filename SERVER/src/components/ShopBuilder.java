package components;

import java.util.*;

import data_handlers.item_handler.Item;
import utils.ServerGameInfo;

public class ShopBuilder
extends Builder<Shop>
{
  protected Shop s = null;

  public void init(int id, String name, String origin) {
    s = new Shop(id, name, origin);
  }

  public void items(String val) {
    s.setItems(val);
  }

  public void abilities(String val) {
    String abilityIds[] = val.split(",");
    int[] ret = new int[abilityIds.length];
    for (int i = 0 ; i<abilityIds.length ; i++) {
      ret[i] = Integer.parseInt(abilityIds[i]);
    }
    s.setAbilities(ret);
  }

  public Shop build() {
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
