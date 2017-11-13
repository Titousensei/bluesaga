package data_handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import creature.Npc;
import creature.Creature.CreatureType;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.item_handler.ContainerHandler;
import map.Tile;
import map.TileData;
import network.Client;
import network.Server;
import game.ServerSettings;
import utils.ServerGameInfo;
import utils.ServerMessage;

public class MapHandler extends Handler {

  public static int dayNightTime = 2; // 2 = day, 1 = night

  public static int worldTimeItr = 0;
  public static int worldDayDuration = 60;
  public static int worldNightDuration = 60; // hours before it becomes night time
  //~ public static int worldDayDuration = 5 * 3600;
  //~ public static int worldNightDuration = 2 * 3600; // hours before it becomes night time

  public static void init() {
    DataHandlers.register("screen", m -> handleScreen(m));
    DataHandlers.register("cinfo", m -> handleInfo(m));
  }

  public static void handleScreen(Message m) {
    Client client = m.client;
    if (client.playerCharacter != null) {
      sendScreenData(client);

      // SEND NEW PLAYER TO NEARBY PLAYERS
      String playerData = client.playerCharacter.getSmallData();

      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client other = entry.getValue();

        if (other.Ready) {
          if (other.playerCharacter.getDBId() != client.playerCharacter.getDBId()) {
            if (isVisibleForPlayer(
                other.playerCharacter,
                client.playerCharacter.getX(),
                client.playerCharacter.getY(),
                client.playerCharacter.getZ())) {
              addOutGoingMessage(other, "new_creature", playerData);
            }
          }
        }
      }
    }
  }

  public static void handleInfo(Message m) {
    Client client = m.client;
    String cTypeId[] = m.message.split(",");
    CreatureType cType = CreatureType.valueOf(cTypeId[0]);
    int cDbId = Integer.parseInt(cTypeId[1]);

    sendCreatureInfo(client, cType, cDbId);
  }

  public static void sendCreatureInfo(Client client, CreatureType cType, int cDbId) {
    String cData = "";
    StringBuilder cSE = null;

    if (cType == CreatureType.Player) {
      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();
        if (s.playerCharacter != null) {
          if (s.playerCharacter.getDBId() == cDbId) {
            cData = s.playerCharacter.getFullData(client.playerCharacter);

            // get creature status effects
            int nrStatusEffects = s.playerCharacter.getStatusEffects().size();
            if (nrStatusEffects > 0) {
              cSE = new StringBuilder(1000);
              cSE.append(s.playerCharacter.getSmallData())
                 .append('/');
              for (Iterator<StatusEffect> iter =
                      s.playerCharacter.getStatusEffects().values().iterator();
                  iter.hasNext();
                  ) {
                StatusEffect se = iter.next();
                cSE.append(se.id)
                   .append(',')
                   .append(se.getGraphicsNr())
                   .append(';');
              }
            }

            break;
          }
        }
      }
    } else {
      Npc monster = Server.WORLD_MAP.getMonster(cDbId);
      if (monster != null) {
        cData = monster.getFullData(client.playerCharacter);

        // get creature status effects
        int nrStatusEffects = monster.getStatusEffects().size();
        if (nrStatusEffects > 0) {
          cSE = new StringBuilder(1000);
          cSE.append(monster.getSmallData())
             .append('/');
          for (Iterator<StatusEffect> iter = monster.getStatusEffects().values().iterator();
              iter.hasNext();
              ) {
            StatusEffect se = iter.next();
            cSE.append(se.id)
               .append(',')
               .append(se.getGraphicsNr())
               .append(';');
          }
        }
      }
    }

    if (!cData.equals("")) {
      addOutGoingMessage(client, "cinfo", cData);

      if (cSE != null) {
        addOutGoingMessage(client, "creature_seffects", cSE.toString());
      }
    }
  }

  public static void sendScreenData(Client client) {
    if (client.playerCharacter != null) {

      client.playerCharacter.setAggro(null);

      // Send "talk" tutorial
      TutorialHandler.updateTutorials(1, client);

      int z = client.playerCharacter.getZ();

      TileData tileData = new TileData();

      addOutGoingMessage(
          client,
          "canwalk",
          client.playerCharacter.getX()
              + ","
              + client.playerCharacter.getY()
              + ","
              + client.playerCharacter.getZ()
              + ","
              + client.playerCharacter.getStat("SPEED"));

      // Gather tile data
      StringBuilder screenData = new StringBuilder(40000);

      Tile lastTile = Tile.DUMMY;
      for (int i = client.playerCharacter.getX() - ServerSettings.TILE_HALF_W - 1;
          i < client.playerCharacter.getX() + ServerSettings.TILE_HALF_W + 2;
          i++) {
        for (int j = client.playerCharacter.getY() - ServerSettings.TILE_HALF_H - 1;
            j < client.playerCharacter.getY() + ServerSettings.TILE_HALF_H + 2;
            j++) {

          lastTile = getTileInfo(client, i, j, z, tileData, screenData, lastTile);
        }
      }

      // Send tile data
      addOutGoingMessage(client, "screen", screenData.toString());
      ServerMessage.println(true, "Screen - ", client.playerCharacter, ": ",
          client.playerCharacter.getX(), ",",
          client.playerCharacter.getY(), ",",
          client.playerCharacter.getZ());

      // Send info about soul if found
      if (tileData.foundSoul) {
        addOutGoingMessage(
            client, "soul", tileData.soulX + "," + tileData.soulY + "," + tileData.soulZ);
      }

      // Send info about monster lock if found
      if (tileData.foundMonsterLockedDoor) {
        addOutGoingMessage(
            client,
            "lock_door",
            tileData.monsterLockedX
                + ","
                + tileData.monsterLockedY
                + ","
                + tileData.monsterLockedZ);
      }
    }
  }

  public void playerChangeZ(Client client, int oldZ, int newZ) {}

  /**
   * Gets all data to send for a tile
   * @param client
   * @param tileX
   * @param tileY
   * @param tileZ
   * @param tileData - additional info about the whole screen of tiles
   * @return
   */
  public static Tile getTileInfo(
      Client client, int tileX, int tileY, int tileZ, TileData tileData,
      StringBuilder buf, Tile lastTile
  ) {

    Tile TILE = Server.WORLD_MAP.getTile(tileX, tileY, tileZ);

    String lastType = "none";
    String lastName = "none";
    int lastX = Integer.MIN_VALUE;
    int lastY = Integer.MIN_VALUE;
    int lastZ = Integer.MIN_VALUE;
    if (lastTile!=null) {
      lastType = lastTile.getType();
      lastName = lastTile.getName();
      lastX = lastTile.getX();
      lastY = lastTile.getY();
      lastZ = lastTile.getZ();
    }

    // Data to send
    String tileType = "none";
    String tileName = "none";
    boolean passable = false;
    String objectInfo = "";
    StringBuilder statusEffects = new StringBuilder(1000);
    String occupantInfo = null;

    // Check if tile exists
    if (TILE != null) {
      tileType = TILE.getType();
      tileName = TILE.getName();

      // Occupant info
      if (TILE.getOccupant() != null) {
        occupantInfo = TILE.getOccupant().getSmallData();
      }

      // Object info
      if (!TILE.getObjectId().equals("None")) {
        String objectId = TILE.getObjectId();
        if (objectId.contains("chest")) {
          // CHECK IF CHEST IS OPENED FOR PLAYER
          int chestInfo =
              Server.userDB.askInt(
                  "select ABS(ContainerId) from character_container where ABS(ContainerId) = "
                      + TILE.getContainerId()
                      + " and CharacterId = "
                      + client.playerCharacter.getDBId());
          if (chestInfo != 0) {
            objectId += "_open";
          }
        }
        objectInfo = objectId;
      }

      // Passable info
      if (TILE.isPassableType() || TILE.getDoorId() > 0) {
        passable = true;
      }

      // Status effects info
      if (TILE.getStatusEffects().size() > 0) {
        statusEffects.setLength(0);
        for (StatusEffect se : TILE.getStatusEffects()) {
          statusEffects.append(se.id)
                       .append('/')
                       .append(se.getGraphicsNr())
                       .append('-');
        }
        statusEffects.setLength(statusEffects.length() - 1);
      }

      // Check for soul
      if (client.playerCharacter.getDBId() == TILE.getSoulCharacterId()) {
        tileData.foundSoul = true;
        tileData.soulX = TILE.getX();
        tileData.soulY = TILE.getY();
        tileData.soulZ = TILE.getZ();
      }

      // Check for monster lock
      if (TILE.isMonsterLocked()) {
        tileData.foundMonsterLockedDoor = true;
        tileData.monsterLockedX = TILE.getX();
        tileData.monsterLockedY = TILE.getY();
        tileData.monsterLockedZ = TILE.getZ();
      }
    } else {
      TILE = new Tile(tileX, tileY, tileZ);
      TILE.setType(tileType, tileName, 0);
    }
    buf.append((tileX == lastX) ? "" : tileX)
       .append(',')
       .append((tileY == lastY) ? "" : tileY)
       .append(',')
       .append((tileZ == lastZ) ? "" : tileZ)
       .append(',')
       .append(tileType.equals(lastType) ? "" : tileType)
       .append(',')
       .append(tileName.equals(lastName) ? "" : tileName)
       .append(',')
       .append(passable ? "" : "0")
       .append(',')
       //.append(lootInfo)
       .append(',')
       .append((objectInfo!=null) ? objectInfo : "")
       .append(',')
       .append(statusEffects);

    if (occupantInfo != null) {
      buf.append(':')
         .append(occupantInfo);
    }

    buf.append(';');

    return TILE;
  }

  public static void checkIfDoorOpens(int MonsterDBId) {
    // CHECK IF OPENS DOOR
    int doorCheck =
        Server.mapDB.askInt("select Id from door where CreatureIds = '" + MonsterDBId + "'");
    try {
      if (doorCheck != 0) {
        ResultSet tileInfo =
            Server.mapDB.askDB(
                //      1 2 3
                "select X,Y,Z from area_tile where DoorId = " + doorCheck);
        if (tileInfo.next()) {
          Tile DoorTile =
              Server.WORLD_MAP.getTile(
                  tileInfo.getInt(1), tileInfo.getInt(2), tileInfo.getInt(3));
          if (DoorTile != null) {
            DoorTile.setMonsterLocked(false);
          }

          // SEND TO ALL PLAYERS IN AREA THAT DOOR IS OPENED
          for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
            Client s = entry.getValue();
            if (s.Ready) {
              if (isVisibleForPlayer(
                  s.playerCharacter, DoorTile.getX(), DoorTile.getY(), DoorTile.getZ())) {
                addOutGoingMessage(
                    s,
                    "unlock_door",
                    DoorTile.getX() + "," + DoorTile.getY() + "," + DoorTile.getZ());
              }
            }
          }
        }
        tileInfo.close();
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void checkIfDoorCloses(int MonsterDBId) {
    // CHECK IF CLOSES DOOR
    int doorCheck =
        Server.mapDB.askInt("select Id from door where CreatureIds = '" + MonsterDBId + "'");
    try {
      if (doorCheck != 0) {
        ResultSet tileInfo =
            Server.mapDB.askDB(
                //      1 2 3
                "select X,Y,Z from area_tile where DoorId = " + doorCheck);
        if (tileInfo.next()) {
          Tile DoorTile =
              Server.WORLD_MAP.getTile(
                  tileInfo.getInt(1), tileInfo.getInt(2), tileInfo.getInt(3));
          if (DoorTile != null) {
            DoorTile.setMonsterLocked(true);
          }

          // SEND TO ALL PLAYERS IN AREA THAT DOOR IS OPENED
          for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
            Client s = entry.getValue();
            if (s.Ready) {
              if (isVisibleForPlayer(
                  s.playerCharacter, DoorTile.getX(), DoorTile.getY(), DoorTile.getZ())) {
                addOutGoingMessage(
                    s,
                    "lock_door",
                    DoorTile.getX() + "," + DoorTile.getY() + "," + DoorTile.getZ());
              }
            }
          }
        }
        tileInfo.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void updateNightTime() {
    worldTimeItr++;

    boolean dayNightChange = false;

    if (dayNightTime == 1 && worldTimeItr >= worldNightDuration) {
      // NIGHT ENDS, MORNING COMES
      ServerMessage.println(false,
          "--- DAY Time ------------------------------------------------------------------");
      ContainerHandler.resetBlueChests();
      dayNightTime = 2;
      dayNightChange = true;
    } else if (dayNightTime == 2 && worldTimeItr >= worldDayDuration) {
      // NIGHT TIME
      ServerMessage.println(false,
          "--- NIGHT Time ----------------------------------------------------------------");

      dayNightTime = 1;
      dayNightChange = true;
    }

    if (dayNightChange) {
      worldTimeItr = 0;
      // Go through monsters, remove those who don't like the time of the day
      for (Npc m : Server.WORLD_MAP.getMonsters().values()) {
        if (m.getExistDayOrNight() != 0) {
          if (m.getExistDayOrNight() != dayNightTime) {
            // MONSTER DISSAPPEAR
            m.kill();
            Server.WORLD_MAP
                .getTile(m.getX(), m.getY(), m.getZ())
                .setOccupant(CreatureType.None, null);

            for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
              Client s = entry.getValue();
              if (s.Ready) {
                if (isVisibleForPlayer(
                    s.playerCharacter, m.getSpawnX(), m.getSpawnY(), m.getSpawnZ())) {
                  addOutGoingMessage(s, "monstergone", m.getSmallData());
                }
              }
            }
          }
        }
      }

      // Send time change to all players
      for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();
        if (s.Ready) {
          if (s.playerCharacter.getZ() >= 0) {
            if (dayNightTime == 2) {
              addOutGoingMessage(s, "night", "stop");
            } else {
              addOutGoingMessage(s, "night", "start");
            }
          }
        }
      }
    }
  }
}
