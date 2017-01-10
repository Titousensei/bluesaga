package components;

import java.util.*;

import network.Server;
import java.sql.ResultSet;
import java.sql.SQLException;

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
      s.setItemsStr(items.toString());
    }
    if (abilities!=null) {
      int[] ab = new int[abilities.size()];
      int i = 0;
      for (Integer val : abilities) {
        ab[i] = val;
        ++ i;
      }
      s.setAbilities(ab);

      StringBuilder sb = new StringBuilder();
      for (int aId : ab) {
        try (ResultSet abilityInfo =
                                //      1        2
            Server.gameDB.askDB("select ClassId, GraphicsNr from ability where Id = " + aId);
        ) {
          if (abilityInfo.next()) {
            String color = "0,0,0";
            if (abilityInfo.getInt(1) > 0) {
              color = ServerGameInfo.classDef.get(abilityInfo.getInt(1)).bgColor;
            }
            sb.append(aId)
              .append(',')
              .append(color)
              .append(',')
              .append(abilityInfo.getInt(2))
              .append(':');
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      s.setAbilitiesStr(sb.toString());
    }

    return s;
  }

  public static void verify(Map<Integer, Item> items, Map<Integer, Shop> shops) {
    for (Shop sh : shops.values()) {
      String sales = sh.getItemsStr();
      if (sales!=null && !"None".equals(sales)) {
        for (String it : sales.split(",")) {
          if (!items.containsKey(parseInt(it))) {
            System.out.println("[ShopBuilder] ERROR - Unknow item for shop " + sh + ": " + it);
          }
        }
      }
    }
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
