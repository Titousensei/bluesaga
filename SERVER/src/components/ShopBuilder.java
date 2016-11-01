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
  protected Collection<Integer> items = null;
  protected Collection<Integer> abilities = null;
  protected int skillId = 0;

  protected int id = 0;
  protected String name = null;
  protected String origin = null;

  @Override
  public void init(int id, String name, String origin) {
    this.id = id;
    this.name = name;
    this.origin = origin;
  }

  @Override
  protected boolean isDuplicateAllowed(String setter)
  { return "Item".equals(setter) || "Ability".equals(setter); }

  public void skill(String val) {
    skillId = parseInt(val);
  }

  public void items(String val) {
    String ids[] = val.split(",");
    for (int i = 0 ; i<ids.length ; i++) {
      item(ids[i]);
    }
  }

  public void item(String val) {
    if (items==null) {
      items = new ArrayList<>(10);
    }
    int id = parseInt(val);
    if (id!=0) {
      items.add(id);
    }
  }

  public void abilities(String val) {
    String ids[] = val.split(",");
    for (int i = 0 ; i<ids.length ; i++) {
      abilities(ids[i]);
    }
  }

  public void ability(String val) {
    if (abilities==null) {
      abilities = new ArrayList<>(10);
    }
    int id = parseInt(val);
    if (id!=0) {
      abilities.add(id);
    }
  }

  public Shop build() {
    Shop s = (skillId == 0)
             ? new Shop(id, name, origin)
             : new ExplorerShop(id, name, origin, skillId);

    if (items!=null) {
      s.setItems(new HashSet<>(items));
      StringBuilder sb = null;
      for (Integer id : items) {
        if (sb == null) {
          sb = new StringBuilder(1000);
        } else {
          sb.append(',');
        }
        sb.append(id);
      }
      s.setItemsStr(sb.toString());
    }
    if (abilities!=null) {
      s.setAbilities(new HashSet<>(abilities));
      StringBuilder sb = new StringBuilder();
      for (Integer id : abilities) {
        try (ResultSet abilityInfo =
                                //      1        2
            Server.gameDB.askDB("select ClassId, GraphicsNr from ability where Id = " + id);
        ) {
          if (abilityInfo.next()) {
            String color = "0,0,0";
            if (abilityInfo.getInt(1) > 0) {
              color = ServerGameInfo.classDef.get(abilityInfo.getInt(1)).bgColor;
            }
            sb.append(id)
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

    s.init();

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
