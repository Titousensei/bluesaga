package data_handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import utils.ServerGameInfo;
import utils.RandomUtils;
import data_handlers.item_handler.Container;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.Item;
import map.Tile;
import network.Client;
import network.Server;

public class GatheringHandler extends Handler {

  public static void init() {
    DataHandlers.register("gathering", m -> handleGathering(m));
  }

  public static void handleGathering(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    String gatheringInfo[] = m.message.split(",");

    int tileX = Integer.parseInt(gatheringInfo[0]);
    int tileY = Integer.parseInt(gatheringInfo[1]);
    int tileZ = Integer.parseInt(gatheringInfo[2]);

    // CHECK TILE
    Tile SourceTile = Server.WORLD_MAP.getTile(tileX, tileY, tileZ);

    if (SourceTile != null) {
      // CHECK IF TILE IS A RESOURCE
      if (SourceTile.getObjectId().contains("gathering")) {
        // CHECK THAT RESOURCE ISNT USED
        if (!SourceTile.getObjectId().contains("_open")) {
          // CHECK IF PLAYER IS NEXT TO IT
          if (client.playerCharacter.getZ() == tileZ) {
            int dx = tileX - client.playerCharacter.getX();
            int dy = tileY - client.playerCharacter.getY();
            if (dx * dx + dy * dy < 4) { // dist < 2
              generateHarvest(client, SourceTile.getObjectId(), tileX, tileY, tileZ);
            }
          }
        } else {
          //IF USED GET HARVEST THAT MAY BE LEFT
          if (ContainerHandler.CONTAINERS.get(tileX + "," + tileY + "," + tileZ) != null) {
            // GET CONTENT IN CONTAINER
            String content = ContainerHandler.getContainerContent(tileX, tileY, tileZ);
            addOutGoingMessage(client, "container_content", content);
          }
        }
      }
    }
  }

  public static void generateHarvest(
      Client client,
      String objectId,
      int tileX,
      int tileY,
      int tileZ) {

    String SourceName = null;
    int skillLvl = 0;
    int skillId = 0;
    int resourceId = 0;

    ResultSet itemInfo =
        Server.gameDB.askDB(
            "select SourceName, SkillLevel, SkillId, ResourceId from item_gathering where ItemName = '"
                + objectId + "'");
    try {
      if (itemInfo.next()) {
        SourceName = itemInfo.getString("SourceName");
        skillLvl   = itemInfo.getInt("SkillLevel");
        skillId    = itemInfo.getInt("SkillId");
        resourceId = itemInfo.getInt("ResourceId");
        System.out.println("INFO - Found item_gathering: " + SourceName + " " + skillLvl + " " + resourceId);
      }
      else {
        System.out.println("WARNING - No such item_gathering: " + objectId);
        return;
      }
      itemInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    int charLvl = client.playerCharacter.getSkill(skillId).getLevel();

    boolean canGather = true;
    int nrGathered = 0;
    if (skillLvl>0) {
      //                1        2        3           4              5              6              7              8
      // 1 oranga       1:50/10  1:80/20  1:30/60/10  1:10/20/60/10  2:10/20/60/10  3:10/20/60/10  4:10/20/60/10  5:10/20/60/10
      // 2 piccoberries 1:20     1:50/10  1:80/20     1:30/60/10     1:10/20/60/10  2:10/20/60/10  3:10/20/60/10  4:10/20/60/10
      // 3 matchanuts            1:20     1:50/10     1:80/20        1:30/60/10     1:10/20/60/10  2:10/20/60/10  3:10/20/60/10
      // 4 soulbush                       1:20        1:50/10        1:80/20        1:30/60/10     1:10/20/60/10  2:10/20/60/10
      // 5 ?                                          1:20           1:50/10        1:80/20        1:30/60/10     1:10/20/60/10
      // 6 ?                                                         1:20           1:50/10        1:80/20        1:30/60/10
      // 7 ?                                                                        1:20           1:50/10        1:80/20
      // 8 ?                                                                                       1:20           1:50/10
      int chance = RandomUtils.getInt(0, 99);
      int eqLvl = charLvl - skillLvl;

      if (eqLvl >= 3) { // 1:10/20/60/10
        if (chance < 10) {
          nrGathered = 1;
        } else if (chance < 30) {
          nrGathered = 2;
        } else if (chance < 90) {
          nrGathered = 3;
        } else {
          nrGathered = 4;
        }
        nrGathered += eqLvl - 3;
      } else if (eqLvl == 2) { // 1:30/60/10
        if (chance < 30) {
          nrGathered = 1;
        } else if (chance < 90) {
          nrGathered = 2;
        } else {
          nrGathered = 3;
        }
      } else if (eqLvl == 1) { // 1:80/20
        if (chance < 80) {
          nrGathered = 1;
        } else {
          nrGathered = 2;
        }
      } else if (eqLvl == 0) { // 1:50/10
        if (chance < 50) {
          nrGathered = 1;
        } else if (chance < 70) {
          nrGathered = 2;
        }
      } else if (eqLvl == -1) { // 1:20
        if (chance < 20) {
          nrGathered = 1;
        }
      } else {
        canGather = false;
      }
    } else if (skillLvl<0) {
      //                1     2     3      4      5      6      7      8
      // -1 ?           1:50  1:80  1:100  1:100  1:100  1:100  1:100  1:100
      // -2 *herbs?     1:20  1:50  1:80   1:100  1:100  1:100  1:100  1:100
      // -3 ?                 1:20  1:50   1:80   1:100  1:100  1:100  1:100
      // -4 ?                       1:20   1:50   1:80   1:100  1:100  1:100
      // -5 flowerluna                     1:20   1:50   1:80   1:100  1:100
      // -6 ?                                     1:20   1:50   1:80   1:100
      // -7 ?                                            1:20   1:50   1:80
      // -8 ?                                                   1:20   1:50
      int eqLvl = charLvl + skillLvl;
      int chance = RandomUtils.getInt(0, 99);
      if (eqLvl>=2) { // 100%
        nrGathered = 1;
      } else if (eqLvl==1) { // 80%
        if (chance < 80) {
          nrGathered = 1;
        }
      } else if (eqLvl==0) { // 50%
        if (chance < 50) {
          nrGathered = 1;
        }
      } else if (eqLvl==-1) { // 20%
        if (chance < 20) {
          nrGathered = 1;
        }
      } else {
        canGather = false;
      }
    }

    if (canGather) {
      Tile TILE = Server.WORLD_MAP.getTile(tileX, tileY, tileZ);

      TILE.setObjectId(objectId + "_open");

      // CHECK IF CONTAINER ALREADY IN MEMORY
      String tileCoord = TILE.getX() + "," + TILE.getY() + "," + TILE.getZ();
      if (ContainerHandler.CONTAINERS.get(tileCoord)
          == null) {
        // IF NOT GENERATE HARVEST
        SkillHandler.gainSP(client, skillId, (nrGathered==0));

        Container newContainer = new Container("harvest");

        for (int i = 0; i < nrGathered; i++) {
          Item rsc = ServerGameInfo.itemDef.get(resourceId);
          if (rsc!=null) {
            Item it = new Item(rsc);
            newContainer.addItem(it);
          }
          else {
            System.out.println("WARNING - No resourceId: "+resourceId);
            return;
          }
        }

        newContainer.setName(SourceName);

        ContainerHandler.CONTAINERS.put(tileCoord, newContainer);
      }

      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client other = entry.getValue();

        if (other.Ready) {
          if (isVisibleForPlayer(other.playerCharacter, TILE.getX(), TILE.getY(), TILE.getZ())) {
            addOutGoingMessage(
                other,
                "open_container",
                client.playerCharacter.getSmallData()
                    + ";"
                    + objectId
                    + ","
                    + tileCoord
                    + ","
                    + client.playerCharacter.getAttackSpeed());
          }
        }
      }

      // GET CONTENT IN CONTAINER
      String content = ContainerHandler.getContainerContent(TILE.getX(), TILE.getY(), TILE.getZ());
      addOutGoingMessage(client, "container_content", content);
    } else {
      addOutGoingMessage(client, "message", "#messages.gathering.need_higher_level");
    }
  }
}
