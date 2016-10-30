package data_handlers;

import network.Client;
import network.Server;

import java.util.*;

import utils.ServerGameInfo;
import utils.RandomUtils;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.Item;

public class FishingHandler extends Handler {

  private static Collection<Item> Fishes;
  private static HashMap<Integer, Item> Catches; // CharacterId, Fish

  public static void init() {
    Catches = new HashMap<Integer, Item>();

    Fishes = new ArrayList<>(10);
    for (Item it : ServerGameInfo.itemDef.values()) {
      if ("Fish".equals(it.getSubType())) {
        Fishes.add(it);
      }
    }

    DataHandlers.register("fishgamecatch", m -> handleFishCatch(m));
  }

  public static void handleFishCatch(Message m) {
    Client client = m.client;
    // CHECK IF THERE IS A CATCH
    if (Catches.containsKey(client.playerCharacter.getDBId())) {
      // ADD FISH TO INVENTORY
      InventoryHandler.addItemToInventory(client, Catches.get(client.playerCharacter.getDBId()));

      Catches.remove(client.playerCharacter.getDBId());
    }
  }

  public static void generateCatch(Client client, int tileX, int tileY, int tileZ) {
    Item caughtFish = null;

    List<Item> myFishes = new ArrayList<>(Fishes);
    Collections.shuffle(myFishes);

    int catchChance =
        RandomUtils.getInt(0, 10000) - (client.playerCharacter.getSkill(101).getLevel() - 1) * 200;

    int fishingRodLevel = client.playerCharacter.getEquipment("Weapon").getRequirement("ReqLevel");

    for (Item fish : myFishes) {
      if (fish.getRequirement("ReqLevel") <= fishingRodLevel) {
        int fishChance = 3000 - fish.getRequirement("ReqLevel") * 500;

        if (fish.getRequirement("ReqLevel") > 9) {
          fishChance = 100 - fish.getRequirement("ReqLevel") * 10;
        } else if (fish.getRequirement("ReqLevel") > 4) {
          fishChance = 1000 - fish.getRequirement("ReqLevel") * 100;
        }

        if (catchChance < fishChance) {
          caughtFish = new Item(ServerGameInfo.itemDef.get(fish.getId()));

          // CHECK IF PLAYER HAS RIGHT FISHING ROD
          if (client.playerCharacter.getWeapon() != null) {
            if (client.playerCharacter.getWeapon().getRequirement("ReqLevel")
                >= caughtFish.getRequirement("ReqLevel")) {
              Catches.remove(client.playerCharacter.getDBId());

              Catches.put(client.playerCharacter.getDBId(), caughtFish);

              // SEND FISH CATCH TO CLIENT
              if (client.Ready) {
                addOutGoingMessage(
                    client,
                    "fishgame",
                    caughtFish.getId() + "," + tileX + "," + tileY + "," + tileZ);
              }
              break;
            }
          }
        }
      }
    }
  }
}
