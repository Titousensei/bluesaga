package data_handlers.item_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.newdawn.slick.Color;

import utils.ItemMagic;
import utils.ServerGameInfo;
import utils.ServerMessage;
import utils.RandomUtils;
import utils.TextFormater;
import creature.Creature;
import creature.Npc;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.ability_handler.Ability;
import data_handlers.ability_handler.AbilityHandler;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.ability_handler.StatusEffectHandler;
import data_handlers.card_handler.CardHandler;
import map.Tile;
import network.Client;
import network.Server;

public class ItemHandler extends Handler {

  public final static int MAX_LOOT_BAG_SIZE = 6 * 4;

  public static void init() {
    DataHandlers.register("drop_item", m -> handleDropItem(m));
    DataHandlers.register("item_info", m -> handleItemInfo(m));
    DataHandlers.register("use_scroll", m -> handleUseScroll(m));
    DataHandlers.register("useitem", m -> handleUseItem(m));
  }

  public static void handleDropItem(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    int dropInfo =
        Server.userDB.askInt(
            "select Nr from character_item where InventoryPos = 'Mouse' and CharacterId = "
                + client.playerCharacter.getDBId());
    if (dropInfo != 0) {
      Server.userDB.updateDB(
          "delete from character_item where InventoryPos = 'Mouse' and CharacterId = "
              + client.playerCharacter.getDBId());

      if (client.playerCharacter.getMouseItem() != null) {
        loseItemOnGround(client, client.playerCharacter.getMouseItem());
      }
      client.playerCharacter.setMouseItem(null);
    }
  }

  public static void handleItemInfo(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    String info[] = m.message.split(";");

    String infoType = info[0];

    Item infoItem = null;

    String infoToSend = "";

    int userItemId = 0;

    if (infoType.equals("shop")) {
      int itemId = Integer.parseInt(info[1]);
      infoItem = ServerGameInfo.newItem(itemId);
      if (info.length > 3) {
        infoItem.setModifierId(Integer.parseInt(info[2]));
        infoItem.setMagicId(Integer.parseInt(info[3]));
      }
    } else if (infoType.equals("closet")) {
      int itemId = Integer.parseInt(info[1]);
      if (itemId == 214) {
        infoToSend =
            "255,234,116;"
                + "#ui.web.get_more_skins"
                + "/"
                + "0,0,0; /"
                + "255,255,255;"
                + "#ui.web.wide_selection"
                + "/"
                + "255,255,255;"
                + "#ui.web.customize"
                + "/";
      } else {
        infoItem = ServerGameInfo.newItem(itemId);
      }
    } else if (infoType.equals("container")) {

      String containerXYZ = info[1];
      String itemXY = info[2];

      if (ContainerHandler.CONTAINERS.containsKey(containerXYZ)) {
        infoItem = ContainerHandler.CONTAINERS.get(containerXYZ).getItems().get(itemXY);
      }
    } else if (infoType.equals("personalchest")) {
      // PERSONAL CHEST ITEM
      userItemId = Integer.parseInt(info[1]);

      if (userItemId == 0) {
        infoToSend =
            "255,234,116;"
                + "#ui.web.get_more_space"
                + "/"
                + "0,0,0; /"
                + "255,255,255;"
                + "#ui.web.expand_chest"
                + "/"
                + "255,255,255;"
                + "#ui.web.keep_safe"
                + "/";
      } else {
        ResultSet userItemInfo =
            Server.userDB.askDB(
                //      1       2           3
                "select ItemId, ModifierId, MagicId from user_chest where Id = " + userItemId);

        try {
          if (userItemInfo.next()) {

            // CHECK IF PLAYER HAS ITEM
            infoItem = ServerGameInfo.newItem(userItemInfo.getInt(1));
            infoItem.setModifierId(userItemInfo.getInt(2));
            infoItem.setMagicId(userItemInfo.getInt(3));
          }
          userItemInfo.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

    } else {
      // INVENTORY ITEM
      userItemId = Integer.parseInt(info[1]);

      ResultSet userItemInfo =
          Server.userDB.askDB(
              //      1       2           3
              "select ItemId, ModifierId, MagicId from character_item where Id = " + userItemId);

      try {
        if (userItemInfo.next()) {

          // CHECK IF PLAYER HAS ITEM
          infoItem = ServerGameInfo.newItem(userItemInfo.getInt(1));
          infoItem.setModifierId(userItemInfo.getInt(2));
          infoItem.setMagicId(userItemInfo.getInt(3));
        }
        userItemInfo.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    StringBuilder sb = new StringBuilder(1000);
    sb.append(infoType).append('@');
    int sb_empty = sb.length();
    sb.append(infoToSend);

    if (infoItem != null) {
      if (infoItem.getMagicId() > 0) {
        ItemMagic item = ItemMagic.getById(infoItem.getMagicId());
        if (item != null) {
          sb.append(item.color)
            .append(';')
            .append(item.toString())
            .append('/');
        } else {
          sb.append("0;0/");
        }
      }

      sb.append(infoItem.getColor())
          .append(';')
          .append(infoItem.getName())
          .append('/')
          .append("0,0,0; /255,234,116;")
          .append(infoItem.getType())
          .append(" - ")
          .append(infoItem.getSubType())
          .append('/');
      if (infoItem.isTwoHands()) {
        TextFormater.formatInfo(sb, "Two Hands");
      }

      sb.append("0,0,0; /");

      if (infoItem.getStatValue("MinDamage") > 0 || infoItem.getStatValue("MaxDamage") > 0) {
        TextFormater.formatInfo(
            sb,
            "Base DMG: "
                + infoItem.getStatValue("MinDamage")
                + " - "
                + infoItem.getStatValue("MaxDamage"));
      }

      TextFormater.formatStatInfo(sb, "ARMOR: ", infoItem.getStatValue("ARMOR"));
      TextFormater.formatStatInfo(sb, "Fire DEF: ", infoItem.getStatValue("FIRE_DEF"));
      TextFormater.formatStatInfo(sb, "Cold DEF: ", infoItem.getStatValue("COLD_DEF"));
      TextFormater.formatStatInfo(sb, "Shock DEF: ", infoItem.getStatValue("SHOCK_DEF"));
      TextFormater.formatStatInfo(sb, "Chems DEF: ", infoItem.getStatValue("CHEMS_DEF"));
      TextFormater.formatStatInfo(sb, "Mind DEF: ", infoItem.getStatValue("MIND_DEF"));
      TextFormater.formatStatInfo(sb, "Magic DEF: ", infoItem.getStatValue("MAGIC_DEF"));

      Map<String, Integer> infoForItem = infoItem.getInfo();
      if (infoForItem != null) {
        for (Map.Entry<String, Integer> entry : infoForItem.entrySet()) {
          TextFormater.formatStatInfo(sb, entry.getKey(), entry.getValue());
        }
      }

      sb.append("0,0,0; /");

      TextFormater.formatBonusInfo(sb, "STR: ", infoItem.getStatValue("STRENGTH"));
      TextFormater.formatBonusInfo(sb, "INT: ", infoItem.getStatValue("INTELLIGENCE"));
      TextFormater.formatBonusInfo(sb, "AGI: ", infoItem.getStatValue("AGILITY"));
      TextFormater.formatBonusInfo(sb, "EVA: ", infoItem.getStatValue("EVASION"));

      TextFormater.formatBonusInfo(sb, "SPD: ", infoItem.getStatValue("SPEED"));
      TextFormater.formatBonusInfo(sb, "ATK SPD: ", infoItem.getStatValue("ATTACKSPEED"));
      TextFormater.formatBonusInfo(sb, "CRIT HIT: ", infoItem.getStatValue("CRITICAL_HIT"));
      TextFormater.formatBonusInfo(sb, "ACC: ", infoItem.getStatValue("ACCURACY"));

      if (infoItem.getType().equals("Potion")) {
        TextFormater.formatBonusInfo(sb, "Restores Health: ", infoItem.getStatValue("MAX_HEALTH"));
        TextFormater.formatBonusInfo(sb, "Restores Mana: ", infoItem.getStatValue("MAX_MANA"));
        TextFormater.formatInfo(sb, "Instant effect");
      } else if (infoItem.getSubType().equals("Herb")) {
        TextFormater.formatInfo(sb, "Temporary buff,");
        TextFormater.formatInfo(sb, "cumulative");
      } else if (infoItem.getType().equals("Eatable")) {
        TextFormater.formatBonusInfo(sb, "Regain health: ", infoItem.getStatValue("MAX_HEALTH"));
        TextFormater.formatBonusInfo(sb, "Regain mana: ", infoItem.getStatValue("MAX_MANA"));
        TextFormater.formatInfo(sb, "Regain over time,");
        TextFormater.formatInfo(sb, "sleep for faster effect");
      } else {
        TextFormater.formatBonusInfo(sb, "MAX HEALTH: ", infoItem.getStatValue("MAX_HEALTH"));
        TextFormater.formatBonusInfo(sb, "MAX MANA: ", infoItem.getStatValue("MAX_MANA"));
      }

      sb.append("0,0,0; /");

      TextFormater.formatReqInfo(
          sb,
          "Req STR: ",
          infoItem.getRequirement("ReqStrength"),
          client.playerCharacter.getStat("STRENGTH"));
      TextFormater.formatReqInfo(
          sb,
          "Req INT: ",
          infoItem.getRequirement("ReqIntelligence"),
          client.playerCharacter.getStat("INTELLIGENCE"));
      TextFormater.formatReqInfo(
          sb,
          "Req AGI: ",
          infoItem.getRequirement("ReqAgility"),
          client.playerCharacter.getStat("AGILITY"));
      TextFormater.formatReqInfo(
          sb, "Req LVL: ", infoItem.getRequirement("ReqLevel"), client.playerCharacter.getLevel());
      if (infoItem.getClassId() > 0) {
        TextFormater.formatConditionInfo(
            sb,
            "Req Class: " + ServerGameInfo.classDef.get(infoItem.getClassId()).name,
            client.playerCharacter.hasClass(infoItem.getClassId()));
      }

      if (infoType.equals("inv") && infoItem.getType().equals("Readable")) {
        sb.append("255,255,255;#ui.inventory.right_click_to_use/");
      }
      if (infoType.equals("inv") && infoItem.getType().equals("Eatable")) {
        sb.append("255,255,255;#ui.inventory.right_click_to_eat/");
      }
      if (infoType.equals("inv") && infoItem.getType().equals("Potion")) {
        sb.append("255,255,255;#ui.inventory.right_click_to_use/");
      }

      if (infoType.equals("shop")) {
        sb.append("0,0,0; /");
        int price = infoItem.getValue();
        boolean canAfford = client.playerCharacter.hasCopper(price);
        if (price > 0) {
          TextFormater.formatPriceInfo(sb, "Price: ", price, canAfford);
        } else {
          TextFormater.formatInfo(sb, "Free");
        }
      }

      if (infoType.equals("invshop")) {
        sb.append("0,0,0; /");
        int price = infoItem.getValue();

        if (!infoItem.getType().equals("Money")) {
          price = infoItem.getSoldValue();
        }
        if (!infoItem.isSellable()) {
          TextFormater.formatInfo(sb, "Can't be sold");
        } else {
          TextFormater.formatValueInfo(sb, "#ui.shop.sell_value#: ", price);
          TextFormater.formatInfo(sb, "#ui.shop.right_click_sell");
        }
      }
    }
    if (sb.length() != sb_empty) {
      addOutGoingMessage(client, "item_info", sb.toString());
    }
  }

  public static void handleUseScroll(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    String scrollInfo[] = m.message.split(",");
    int tileX = Integer.parseInt(scrollInfo[0]);
    int tileY = Integer.parseInt(scrollInfo[1]);

    int scrollUserItemId = Integer.parseInt(scrollInfo[2]);

    String scrollLocation = scrollInfo[3];

    boolean useScrollSuccess = false;
    Item scrollItem = null;

    // Get scroll from inventory
    ResultSet itemInfo;

    if (scrollLocation.equals("Inventory")) {
      // Specific inventory scroll
      itemInfo =
          Server.userDB.askDB(
              //      1       2
              "select ItemId, Nr from character_item where Id = "
                  + scrollUserItemId
                  + " and CharacterId = "
                  + client.playerCharacter.getDBId());
    } else {
      // Similar scroll in inventory as the one clicked in the actionbar
      itemInfo =
          Server.userDB.askDB(
              //      1       2
              "select ItemId, Nr from character_item where ItemId = "
                  + scrollUserItemId
                  + " and CharacterId = "
                  + client.playerCharacter.getDBId());
    }

    try {
      if (itemInfo.next()) {
        scrollItem = ServerGameInfo.itemDef.get(itemInfo.getInt(1));

        // CHECK THAT IT IS A SCROLL AND AN ABILITY
        useScrollSuccess = true;

        // REMOVE SCROLL
        if (itemInfo.getInt(2) > 1) {
          Server.userDB.updateDB(
              "update character_item set Nr = Nr - 1 where ItemId = "
                  + itemInfo.getInt(1)
                  + " and CharacterId = "
                  + client.playerCharacter.getDBId());
        } else {
          Server.userDB.updateDB(
              "delete from character_item where ItemId = "
                  + itemInfo.getInt(1)
                  + " and CharacterId = "
                  + client.playerCharacter.getDBId());
        }

        InventoryHandler.sendInventoryInfo(client);
      }
      itemInfo.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // If no scroll in inventory, then use the one in the actionbar
    if (scrollLocation.equals("Actionbar") && !useScrollSuccess) {
      int scrollItemId = scrollUserItemId;

      ResultSet actionbarItem =
          Server.userDB.askDB(
              //      1   2        3
              "select Id, OrderNr, ActionId from character_actionbar where CharacterId = "
                  + client.playerCharacter.getDBId()
                  + " and ActionType = 'Item' and ActionId = "
                  + scrollItemId);
      try {
        if (actionbarItem.next()) {
          scrollItem = ServerGameInfo.newItem(actionbarItem.getInt(3));
          Server.userDB.updateDB(
              "delete from character_actionbar where Id = " + actionbarItem.getInt(1));

          // Send remove item from Actionbar
          addOutGoingMessage(client, "remove_actionbar", String.valueOf(actionbarItem.getInt(2)));
          useScrollSuccess = true;
        }
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // Use Ability
    if (useScrollSuccess && scrollItem != null) {
      Ability scrollAbility =
          new Ability(ServerGameInfo.abilityDef.get(scrollItem.getScrollUseId()));
      scrollAbility.setCaster(client.playerCharacter);
      scrollAbility.setManaCost(0);
      AbilityHandler.playerUseAbility(
          client, scrollAbility, tileX, tileY, client.playerCharacter.getZ());
    }
  }

  public static void handleUseItem(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    String itemData = m.message;

    boolean useItemSuccess = false;
    Color useColor = new Color(255, 255, 255);

    Item usedItem = null;

    if (itemData.startsWith("actionbar")) {
      String itemInfo[] = itemData.split(";");
      int itemId = Integer.parseInt(itemInfo[1]);

      usedItem = ServerGameInfo.newItem(itemId);

      if (usedItem != null) {
        // Use item from Actionbar
        useItemSuccess = ActionbarHandler.useItem(client, usedItem);
      }
    } else {
      // USE ITEM FROM INVENTORY!!

      String itemInfo[] = itemData.split(";");
      int posX = Integer.parseInt(itemInfo[0]);
      int posY = Integer.parseInt(itemInfo[1]);
      int itemId = Integer.parseInt(itemInfo[2]);

      usedItem = ServerGameInfo.newItem(itemId);

      if (usedItem != null) {
        useItemSuccess = InventoryHandler.useItem(client, usedItem, posX, posY);
      }
    }

    if (useItemSuccess) {
      client.playerCharacter.saveInfo();

      // SEND STATSCHANGE
      if (usedItem.getType().equals("Eatable")) {
        if (usedItem.getSubType().equals("Herb")) {
          useColor = new Color(255, 255, 73);
          boolean hadEffect = true;
          for (StatusEffect se : usedItem.getStatusEffects()) {
            hadEffect &= StatusEffectHandler.addStatusEffect(client.playerCharacter, se);
          }
          if (!hadEffect) {
            addOutGoingMessage(client, "message", "You're too weak: you cough out the herb before it has any effect");
            return;
          }
        } else {
          useColor = new Color(202, 253, 161);
        }
        addOutGoingMessage(client, "stat", "Health;" + client.playerCharacter.getHealth());
        addOutGoingMessage(
            client,
            "stat",
            "HEALTH_REGAIN;"
                + client.playerCharacter.getStat("HEALTH_REGAIN")
                + ';'
                + client.playerCharacter.getSatisfied());
        addOutGoingMessage(client, "stat", "Mana;" + client.playerCharacter.getMana());
        addOutGoingMessage(
            client,
            "stat",
            "MANA_REGAIN;"
                + client.playerCharacter.getStat("MANA_REGAIN")
                + ';'
                + client.playerCharacter.getSatisfied());
      } else if (usedItem.getSubType().equals("HEALTH")) {
        useColor = new Color(255, 88, 88);
      } else if (usedItem.getSubType().equals("MANA")) {
        useColor = new Color(91, 176, 255);
      }
      // SEND USE ITEM TO OTHER CLIENTS ON SAME MAP

      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();

        if (s.Ready
            && isVisibleForPlayer(
                s.playerCharacter,
                client.playerCharacter.getX(),
                client.playerCharacter.getY(),
                client.playerCharacter.getZ())) {
          // UserType; UserId; R; G; B
          addOutGoingMessage(
              s,
              "useitem",
              client.playerCharacter.getSmallData()
                  + ';'
                  + useColor.getRed()
                  + ','
                  + useColor.getGreen()
                  + ','
                  + useColor.getBlue()
                  + ','
                  + client.playerCharacter.getHealthStatus()
                  + ';'
                  + usedItem.getType()
                  + ','
                  + usedItem.getSubType());
        }
      }
    } else {
      addOutGoingMessage(client, "useitem", "no");
    }
  }

  public static Vector<Item> dropLoot(Npc TARGET, int charLvl) {

    Vector<Item> droppedItems = new Vector<Item>();

    String lootItems =
        Server.gameDB.askString("select LootItems from creature where Id = " + TARGET.getCreatureId());

    int dropOrNot = 0;
    try {
      if (!"".equals(lootItems) && !"None".equals(lootItems)) {

        String allLoot[] = lootItems.split(";");

        for (String loot : allLoot) {
          String lootInfo[] = loot.split(",");
          int itemId = Integer.parseInt(lootInfo[0]);
          int dropChance = Integer.parseInt(lootInfo[1]);

          dropOrNot = RandomUtils.getInt(0, 1000);

          if (TARGET.getSpecialType() > 0) {
            dropChance *= 4;
          }

          if (TARGET.isElite()) {
            dropChance *= 3;
          } else if (TARGET.isTitan()) {
            dropChance *= 10;
          }

          if (dropOrNot < dropChance) {
            Item droppedItem = ServerGameInfo.newItem(itemId);

            if (droppedItem.getType().equals("Weapon")
                || droppedItem.getType().equals("OffHand")
                || droppedItem.getType().equals("Head")
                || droppedItem.getType().equals("Amulet")
                || droppedItem.getType().equals("Artifact")) {
              // CREATE RARE ITEMS

              if (TARGET.isElite()) {
                droppedItem.setModifier(Modifier.random(charLvl + 1));
              } else if (TARGET.isTitan()) {
                droppedItem.setModifier(Modifier.random(charLvl + 3));
              } else {
                droppedItem.setModifier(Modifier.random(charLvl));
              }

              if (TARGET.getSpecialType() > 0) {
                int magicChance = RandomUtils.getInt(0, 100);

                if (TARGET.isTitan()) {
                  magicChance -= 20;
                }

                if (magicChance < 20) {
                  int magicId = TARGET.getSpecialType();
                  ItemMagic item = ItemMagic.getById(magicId);
                  if (item != null) {
                    droppedItem.setMagicId(magicId);
                  }
                }
              }
            }

            // DROP ITEM
            droppedItems.add(droppedItem);
          }
        }
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }

    return droppedItems;
  }

  public static int dropMoney(Npc TARGET) {

    int droppedCopper = 0;

    int dropOrNot = RandomUtils.getInt(0, 100);

    if (TARGET.getSpecialType() > 0 || TARGET.isElite() || TARGET.isTitan()) {
      dropOrNot -= 50;
    }

    if (dropOrNot < 50) {
      int lootCopper =
          Server.gameDB.askInt(
              "select LootCopper from creature where Id = " + TARGET.getCreatureId());

      if (lootCopper > 0) {
        droppedCopper = RandomUtils.getInt(0, lootCopper);
      }
    }

    if (TARGET.getSpecialType() > 0) {
      droppedCopper *= 2;
    }

    if (TARGET.isElite()) {
      droppedCopper *= 2;
    }

    if (TARGET.isTitan()) {
      droppedCopper *= 20;
    }

    return droppedCopper;
  }

  public static void loseLootUponDeath(Client client) {

    List<Item> lostLoot = new ArrayList<>(MAX_LOOT_BAG_SIZE);

    Creature TARGET = client.playerCharacter;
    EquipHandler.checkRequirements(client);

    ResultSet lostItemsInfo =
        Server.userDB.askDB(
            //      1   2           3        4       5
            "select Id, ModifierId, MagicId, ItemId, Nr from character_item where InventoryPos <> 'None' and Equipped = 0 and CharacterId = "
                + client.playerCharacter.getDBId());

    try {
      while (lostItemsInfo.next()) {
        if (lostLoot.size() < MAX_LOOT_BAG_SIZE) {
          Item lostItem = ServerGameInfo.newItem(lostItemsInfo.getInt(4));
          if (!"Key".equals(lostItem.getType())
          && !"Part".equals(lostItem.getSubType())
          ) {
            lostItem.setStacked(lostItemsInfo.getInt(5));
            lostItem.setModifierId(lostItemsInfo.getInt(2));
            lostItem.setMagicId(lostItemsInfo.getInt(3));
            Server.userDB.updateDB(
                "delete from character_item where Id = " + lostItemsInfo.getInt(1));
            lostLoot.add(lostItem);
          }
        } else {
          break;
        }
      }
      lostItemsInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // LOSE RANDOM CARD WHEN DYING
    Item lostCard = CardHandler.playerDropCard(client);
    if (lostCard != null) {
      lostLoot.add(lostCard);
    }
    // SEND LOST ITEMS TO ALL CLIENTS ON SAME MAP
    if (lostLoot.size() > 0) {
      Tile bagTile = Server.WORLD_MAP.findClosestEmptyTile(TARGET.getX(), TARGET.getY(), TARGET.getZ());
      bagTile.setObjectId("container/bigbag");
      ServerMessage.println(false, "LostLoot - ", lostLoot, " @", bagTile);
      for (Item loot : lostLoot) {
        ContainerHandler.addItemToContainer(loot, bagTile.getX(), bagTile.getY(), bagTile.getZ());
      }
      // SEND LOOT INFO TO ALL CLIENTS IN AREA
      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();

        if (s.Ready
            && isVisibleForPlayer(s.playerCharacter, TARGET.getX(), TARGET.getY(), TARGET.getZ())) {
          addOutGoingMessage(
              s,
              "droploot",
              "container/bigbag," + TARGET.getX() + "," + TARGET.getY() + "," + TARGET.getZ());
        }
      }
    }
  }

  public static void loseItemOnGround(Client client, Item droppedItem) {
    int tileX = client.playerCharacter.getX();
    int tileY = client.playerCharacter.getY();
    int tileZ = client.playerCharacter.getZ();

    Server.WORLD_MAP.getTile(tileX, tileY, tileZ).setObjectId("container/smallbag");
    ContainerHandler.addItemToContainer(droppedItem, tileX, tileY, tileZ);

    // SEND LOOT INFO TO ALL CLIENTS IN AREA
    for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client s = entry.getValue();

      if (s.Ready && isVisibleForPlayer(s.playerCharacter, tileX, tileY, tileZ)) {
        addOutGoingMessage(
            s, "droploot", "container/smallbag," + tileX + "," + tileY + "," + tileZ);
      }
    }

    addOutGoingMessage(
        client, "message", droppedItem.getName() + " #messages.inventory.dropped_on_ground");
  }
}
