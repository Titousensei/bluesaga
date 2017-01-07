package data_handlers;

import network.Client;
import network.Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import components.Shop;
import utils.ServerGameInfo;
import data_handlers.ability_handler.Ability;
import data_handlers.item_handler.CoinConverter;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.Item;
import player_classes.BaseClass;

public class ShopHandler extends Handler {

  public static void init() {
    DataHandlers.register("shop", m -> handleShop(m));
    DataHandlers.register("buy", m -> handleBuy(m));
    DataHandlers.register("sell", m -> handleSell(m));
  }

  public static void handleShop(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    int shopId = Integer.parseInt(m.message);
    Shop shop = ServerGameInfo.shopDef.get(shopId);
    if (shop != null) {
      addOutGoingMessage(
          client, "shop", shop.name + ";"
          + shop.getItemsStr() + ";"
          + shop.getAbilitiesStr());
      InventoryHandler.sendInventoryInfo(client);
    }
  }

  public static void handleBuy(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    String buyInfo[] = m.message.split(";");
    String buyType = buyInfo[0];
    int itemId = Integer.parseInt(buyInfo[1]);

    client.playerCharacter.loadInventory();
    if (buyType.equals("Item")) {
      Item it = ServerGameInfo.itemDef.get(itemId);
      if (it!=null) {
        if (it.getType().equals("Customization")) {
          // CHECK IF PLAYER CAN AFFORD IT
          if (client.playerCharacter.hasCopper(it.getValue())) {
            // CHECK IF USER IS PREMIUM
            InventoryHandler.removeCopperFromInventory(client, it.getValue());

            if (it.getSubType().equals("Mouth Feature")) {
              client.playerCharacter.getCustomization().setMouthFeatureId(itemId);
              Server.userDB.updateDB(
                  "update user_character set MouthFeatureId = "
                      + itemId
                      + " where Id = "
                      + client.playerCharacter.getDBId());
            } else if (it.getSubType().equals("Accessories")) {
              client.playerCharacter.getCustomization().setAccessoriesId(itemId);
              Server.userDB.updateDB(
                  "update user_character set AccessoriesId = "
                      + itemId
                      + " where Id = "
                      + client.playerCharacter.getDBId());
            } else if (it.getSubType().equals("Skin Feature")) {
              client.playerCharacter.getCustomization().setSkinFeatureId(itemId);
              Server.userDB.updateDB(
                  "update user_character set SkinFeatureId = "
                      + itemId
                      + " where Id = "
                      + client.playerCharacter.getDBId());
            } else if (it.getSubType().equals("Remove")) {
              client.playerCharacter.getCustomization().setSkinFeatureId(0);
              client.playerCharacter.getCustomization().setAccessoriesId(0);
              client.playerCharacter.getCustomization().setMouthFeatureId(0);
              Server.userDB.updateDB(
                  "update user_character set SkinFeatureId = 0, AccessoriesId = 0, MouthFeatureId = 0 where Id = "
                      + client.playerCharacter.getDBId());
            }

            for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
              Client other = entry.getValue();

              if (other.Ready) {
                if (isVisibleForPlayer(
                    other.playerCharacter,
                    client.playerCharacter.getX(),
                    client.playerCharacter.getY(),
                    client.playerCharacter.getZ())) {
                  addOutGoingMessage(
                      other,
                      "newcustomize",
                      client.playerCharacter.getSmallData()
                          + ";"
                          + it.getSubType()
                          + ","
                          + itemId);
                }
              }
            }
          } else {
            addOutGoingMessage(client, "shoperror", "nogold");
          }
        } else if (it.getType().equals("Money")) {
          // EXCHANGE MONEY VALUES
          if (itemId == 34) {
            // GOLD
            // REMOVE 100 SILVER
            if (InventoryHandler.countPlayerItem(client, 35) >= 100) {
              InventoryHandler.removeNumberOfItems(client, 35, 100);
              InventoryHandler.addItemToInventory(
                  client, ServerGameInfo.newItem(34));
            } else {
              addOutGoingMessage(client, "message", "#messages.shop.not_enough_silver");
            }
          } else if (itemId == 35) {
            // SILVER
            // REMOVE 100 COPPER
            if (InventoryHandler.countPlayerItem(client, 36) >= 100) {
              InventoryHandler.removeNumberOfItems(client, 36, 100);
              InventoryHandler.addItemToInventory(
                  client, ServerGameInfo.newItem(35));

            } else {
              addOutGoingMessage(client, "message", "#messages.shop.not_enough_copper");
            }

          } else if (itemId == 36) {
            // COPPER
            if (InventoryHandler.countPlayerItem(client, 35) >= 1) {
              // REMOVE ONE SILVER
              InventoryHandler.removeNumberOfItems(client, 35, 1);
              Item copperCoins = ServerGameInfo.newItem(36);
              copperCoins.setStacked(100);
              InventoryHandler.addItemToInventory(client, copperCoins);
            } else if (InventoryHandler.countPlayerItem(client, 34) >= 1) {
              // REMOVE ONE GOLD
              InventoryHandler.removeNumberOfItems(client, 34, 1);
              Item silverCoins = ServerGameInfo.newItem(35);
              silverCoins.setStacked(99);
              InventoryHandler.addItemToInventory(client, silverCoins);
              Item copperCoins = ServerGameInfo.newItem(36);
              silverCoins.setStacked(100);
              InventoryHandler.addItemToInventory(client, copperCoins);
            } else {
              addOutGoingMessage(client, "message", "#messages.shop.no_money");
            }
          }

        } else {

          // CHECK IF PLAYER CAN AFFORD IT
          if (client.playerCharacter.hasCopper(it.getValue())) {
            // CHECK IF INVENTORY ISN'T FULL
            if (!client.playerCharacter.isInventoryFull(
                ServerGameInfo.newItem(itemId))) {
              // ADD ITEM AND REMOVE GOLD TO PLAYER
              InventoryHandler.removeCopperFromInventory(client, it.getValue());
              InventoryHandler.addItemToInventory(
                  client, ServerGameInfo.newItem(itemId));

              addOutGoingMessage(
                  client, "buy", "item/" + it.getName() + "/" + it.getValue());
            } else {
              addOutGoingMessage(client, "shoperror", "inventoryfull");
            }
          } else {
            addOutGoingMessage(client, "shoperror", "nogold");
          }
        }
      } else {
        addOutGoingMessage(client, "shoperror", "noitem");
      }
    } else if (buyType.equals("Ability")) {
      int abilityId = itemId;
      Ability shopAbility = new Ability(ServerGameInfo.abilityDef.get(abilityId));

      // CHECK IF PLAYER CAN AFFORD IT
      if (client.playerCharacter.hasCopper(shopAbility.getPrice())) {
        // CHECK IF PLAYER HAS THE ABILITY ALREADY
        if (client.playerCharacter.hasAbility(shopAbility.getName()) == 9999) {

          // CHECK IF PLAYER HAS ABILITY REQUIREMENTS
          boolean hasReq = true;

          if (shopAbility.getClassId() > 0) {
            if (!client.playerCharacter.hasClass(shopAbility.getClassId())) {
              hasReq = false;
            } else {
              // Check class level
              BaseClass cl = client.playerCharacter.getClassById(shopAbility.getClassId());
              if (cl != null) {
                int lvl = cl.gainsXP() ? cl.level : client.playerCharacter.getLevel();
                if (lvl < shopAbility.getClassLevel()) {
                  hasReq = false;
                }
              } else {
                hasReq = false;
              }
            }
          }

          if (shopAbility.getFamilyId() > 0) {
            if (shopAbility.getFamilyId() != client.playerCharacter.getFamilyId()) {
              hasReq = false;
            }
          }

          if (hasReq) {
            // ADD ABILITY AND REMOVE GOLD TO PLAYER
            Server.userDB.updateDB(
                "insert into character_ability (CharacterId, AbilityId, CooldownLeft) values ("
                    + client.playerCharacter.getDBId()
                    + ","
                    + abilityId
                    + ",0)");
            InventoryHandler.removeCopperFromInventory(client, shopAbility.getPrice());
            client.playerCharacter.loadInventory();
            client.playerCharacter.loadAbilities();

            Ability A = client.playerCharacter.getAbilityById(abilityId);
            if (A != null) {
              String msg =
                  "ability/"
                      + A.getName()
                      + '/'
                      + shopAbility.getPrice()
                      + '/'
                      + A.getAbilityId()
                      + '='
                      + A.getName()
                      + '='
                      + A.getClassId()
                      + '='
                      + A.getColor().getRed()
                      + '='
                      + A.getColor().getGreen()
                      + '='
                      + A.getColor().getBlue()
                      + '='
                      + A.getManaCost()
                      + '='
                      + A.getCooldown()
                      + '='
                      + A.getCooldownLeft()
                      + '='
                      + A.getRange()
                      + '='
                      + A.getPrice()
                      + '='
                      + A.isTargetSelf()
                      + '='
                      + A.isInstant()
                      + '='
                      + A.getEquipReq()
                      + '='
                      + A.getGraphicsNr()
                      + '='
                      + A.getAoE();
              addOutGoingMessage(client, "buy", msg);
            }
          } else {
            addOutGoingMessage(client, "shoperror", "noreq");
          }
        } else {
          addOutGoingMessage(client, "shoperror", "haveability");
        }
      } else {
        addOutGoingMessage(client, "shoperror", "nogold");
      }
    } else {
      addOutGoingMessage(client, "shoperror", "noability");
    }
    InventoryHandler.sendInventoryInfo(client);
  }

  public static void handleSell(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    int itemId = 0;
    Item soldItem = null;

    boolean sellFromMouse = false;
    String invPos = "";

    if (m.message.contains("mouse")) {
      String itemInfo[] = m.message.split(",");
      itemId = Integer.parseInt(itemInfo[1]);
      soldItem = client.playerCharacter.getMouseItem();
      sellFromMouse = true;
    } else {
      invPos = m.message;

      // CHECK IF PLAYER HAS ITEM
      ResultSet rs =
          Server.userDB.askDB(
              //      1       2           3        4
              "select ItemId, ModifierId, MagicId, Nr from character_item where InventoryPos = '"
                  + invPos
                  + "' and CharacterId = "
                  + client.playerCharacter.getDBId());

      try {
        if (rs.next()) {
          itemId = rs.getInt(1);
          soldItem = ServerGameInfo.newItem(itemId);
          soldItem.setModifierId(rs.getInt(2));
          soldItem.setMagicId(rs.getInt(3));
          soldItem.setStacked(rs.getInt(4));
        }
        rs.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    if (soldItem != null) {
      int value = soldItem.getSoldValue();

      // CHECK IF ITEM IS SELLABLE
      if (soldItem.isSellable()) {
        // CHECK IF PLAYER IS AT LEAST LVL 2
        if (client.playerCharacter.getLevel() > 1) {

          // REMOVE ITEM
          if (sellFromMouse) {
            Server.userDB.updateDB(
                "delete from character_item where InventoryPos = 'Mouse' and CharacterId = "
                    + client.playerCharacter.getDBId());
            value *= client.playerCharacter.getMouseItem().getStacked();
            client.playerCharacter.setMouseItem(null);
            addOutGoingMessage(client, "clearmouse", "");
          } else if (!invPos.equals("")) {
            if (soldItem.getStacked() > 1) {
              Server.userDB.updateDB(
                  "update character_item set Nr = Nr - 1 where InventoryPos = '"
                      + invPos
                      + "' and CharacterId = "
                      + client.playerCharacter.getDBId());
            } else {
              Server.userDB.updateDB(
                  "delete from character_item where InventoryPos = '"
                      + invPos
                      + "' and CharacterId = "
                      + client.playerCharacter.getDBId());
            }
          }

          // ADD GOLD TO PLAYER
          CoinConverter cc = new CoinConverter(value);

          if (cc.getGold() > 0) {
            Item GoldItem = ServerGameInfo.newItem(34);
            GoldItem.setStacked(cc.getGold());
            InventoryHandler.addItemToInventory(client, GoldItem);
          }
          if (cc.getSilver() > 0) {
            Item SilverItem = ServerGameInfo.newItem(35);
            SilverItem.setStacked(cc.getSilver());
            InventoryHandler.addItemToInventory(client, SilverItem);
          }
          if (cc.getCopper() > 0) {
            Item CopperItem = ServerGameInfo.newItem(36);
            CopperItem.setStacked(cc.getCopper());
            InventoryHandler.addItemToInventory(client, CopperItem);
          }

        } else {
          addOutGoingMessage(client, "shoperror", "lowlevel");
        }
      } else {
        addOutGoingMessage(client, "shoperror", "notwanted");
      }
    }
  }
}
