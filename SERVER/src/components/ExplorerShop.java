package components;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import data_handlers.item_handler.Item;
import data_handlers.item_handler.Modifier;
import network.Server;
import utils.ServerGameInfo;

public class ExplorerShop
extends Shop
{
  public static final Comparator<Item> ITEM_VALUE_DESCENDING = new ItemValueDescending();

  public final int jobId;

  protected Map<String, Item> items = new HashMap<>(50);

  public ExplorerShop(int id, String n, String o, int jobId) {
    super(id, n, o);
    this.jobId = jobId;
  }

  @Override
  public void init() {
    Server.userDB.updateDB(
        "CREATE TABLE IF NOT EXISTS items_" + jobId + " (ItemId INTEGER, ModifierId INTEGER, MagicId INTEGER, CharacterId INTEGER, BuyTime BIGINT)");

    ResultSet shopInfo =    //      1       2           3
        Server.userDB.askDB("SELECT ItemId, ModifierId, MagicId FROM items_" + jobId);

    items = new HashMap<>(50);
    try {
      while (shopInfo.next()) {
        int itemId = shopInfo.getInt(1);
        Item it = ServerGameInfo.newItem(itemId);
        if (it!=null) {
          it.setModifierId(shopInfo.getInt(2));
          it.setMagicId(shopInfo.getInt(3));
          String key = it.getId() + "," + it.getModifierId() + "," + it.getMagicId();
          items.put(key, it);
        }
      }
      shopInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public int getSkillId() { return jobId; }

  @Override
  public boolean addItem(Item it, int characterId) {
    Server.userDB.updateDB("INSERT INTO items_" + jobId
        + " (ItemId, ModifierId, MagicId, CharacterId, BuyTime) VALUES ("
        + it.getId() + "," + it.getModifierId() + "," + it.getMagicId() + ","
        + characterId + "," + System.currentTimeMillis() + ")");
    String key = it.getId() + "," + it.getModifierId() + "," + it.getMagicId();
    items.put(key, it);
    if (items.size() > 50) {
      List<Item> list_items = new ArrayList(items.values());
      list_items.sort(ITEM_VALUE_DESCENDING);
      for (int i = 50 ; i < list_items.size() ; i++) {
        Item it_remove = list_items.get(i);
        removeItem(it_remove, 0);
      }
    }
    return true;
  }

  @Override
  public boolean removeItem(Item it, int characterId) {
    Server.userDB.updateDB("DELETE FROM items_" + jobId
        + " WHERE ItemId = " + it.getId()
        + " AND ModifierId = " + it.getModifierId()
        + " AND MagicId = " + it.getMagicId());
    String key = it.getId() + "," + it.getModifierId() + "," + it.getMagicId();
    items.remove(key);
    return true;
  }

  @Override
  public boolean containsItem(Item it) {
    String key = it.getId() + "," + it.getModifierId() + "," + it.getMagicId();
    return items.containsKey(key);
  }

  @Override
  public String getItemsStr() {
    StringBuilder sb = null;
    List<Item> list_items = new ArrayList(items.values());
    list_items.sort(ITEM_VALUE_DESCENDING);
    int count = 6*6;
    for (Item it : list_items) {
      if (count <= 0) {
        break;
      }
      count --;
      if (sb == null) {
        sb = new StringBuilder(1000);
      } else {
        sb.append(',');
      }
      sb.append(it.getId())
        .append('_')
        .append(it.getModifierId())
        .append('_')
        .append(it.getMagicId());
    }
    if (sb != null) {
      return sb.toString();
    } else {
      return "None";
    }
  }

  public static class ItemValueDescending
  implements Comparator<Item>
  {
    public int compare(Item item1, Item item2) {
      int v1 = item1.getValue();
      int v2 = item2.getValue();
      if(v1==v2) {
        return 0;
      } else if (v1>v2) {
        return -1;
      } else {
        return 1;
      }
    }
  }
}
