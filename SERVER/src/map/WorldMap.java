package map;

import instances.DungeonGenerator;
import instances.PirateIslandGenerator;
import network.Client;
import network.Server;

import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import utils.ServerGameInfo;
import utils.ServerMessage;
import utils.Spiral;
import creature.Creature;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.Handler;
import data_handlers.MapHandler;
import data_handlers.TrapHandler;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.Item;
import data_handlers.monster_handler.MonsterHandler;
import game.ServerSettings;

public class WorldMap
{
  private HashMap<String, Tile> MapTiles;

  public HashMap<Integer, Vector<Npc>> monstersByZ;
  public HashMap<Integer, Vector<PlayerCharacter>> playersByZ;
  public Vector<Integer> zLevels;

  private int entranceX;
  private int entranceY;

  private int NrPlayers;

  private Map<Integer, Npc> Monsters;
  public List<Npc> dialogNpcs = new ArrayList<>(100);

  private int newMonsterId = 0;

  public WorldMap() {}

  public void loadMap() {
    Monsters = new ConcurrentHashMap<>();
    MapTiles = new HashMap<>();

    NrPlayers = 0;

    int minX = 100000;
    int minY = 100000;
    int maxX = 0;
    int maxY = 0;

    monstersByZ = new HashMap<>();
    playersByZ = new HashMap<>();
    zLevels = new Vector<>();

    // LOAD MAP FROM DB
    System.out.print("[WorldMap] INFO - Loading map");

    // LOAD MAP FROM DB                             1 2 3 4    5    6        7        8      9
    ResultSet tileInfo = Server.mapDB.askDB("select X,Y,Z,Type,Name,Passable,ObjectId,DoorId,AreaEffectId from area_tile");

    try {
      int count = 0;
      while (tileInfo.next()) {
        ++ count;
        if (count % 10000 == 0) {
          System.out.print(".");
          System.out.flush();
        }
        int tileX = tileInfo.getInt(1);
        int tileY = tileInfo.getInt(2);
        int tileZ = tileInfo.getInt(3);
        String xyz = tileX + "," + tileY + "," + tileZ;
        Tile newTile = new Tile(tileX, tileY, tileZ);
        newTile.setZ(tileZ);
        String tileName = tileInfo.getString(5).intern();
        int tilePassable = tileInfo.getInt(6);
        if (tilePassable == 0 && tileName.contains("Stairs")) {
          System.out.println("[WorldMap] WARNING - Impassable Stairs @" + xyz);
        }
        newTile.setType(tileInfo.getString(4).intern(), tileName, tilePassable);
        newTile.setObjectId(tileInfo.getString(7).intern());
        newTile.setDoorId(tileInfo.getInt(8));
        newTile.setAreaEffectId(tileInfo.getInt(9));

        if (newTile.getObjectId().contains("moveable")) {
          ContainerHandler.MOVEABLES.put(
              newTile.getX() + "," + newTile.getY() + "," + newTile.getZ(),
              new Moveable(newTile.getObjectId(), newTile.getX(), newTile.getY(), newTile.getZ()));
        }

        if (MapTiles.containsKey(xyz)) {
          System.out.println("[WorldMap] WARNING - Duplicate tile @" + xyz);
        }
        MapTiles.put(xyz, newTile);

        if (tileX > maxX) {
          maxX = tileX;
        }

        if (tileY > maxY) {
          maxY = tileY;
        }

        if (tileX < minX) {
          minX = tileX;
        }

        if (tileY < minY) {
          minY = tileY;
        }

        if (newTile.getDoorId() > 0) {
          // LOAD DOORINFO
          int doorInfo =
              Server.mapDB.askInt(
                  "select Id from door where Id = "
                      + newTile.getDoorId()
                      + " and CreatureIds != 'None'");
          if (doorInfo != 0) {
            newTile.setMonsterLocked(true);
          }
        }

        if (!monstersByZ.containsKey(tileZ)) {
          monstersByZ.put(tileZ, new Vector<Npc>());
        }
        if (!playersByZ.containsKey(tileZ)) {
          playersByZ.put(tileZ, new Vector<PlayerCharacter>());
        }
        if (!zLevels.contains(tileZ)) {
          zLevels.add(tileZ);
        }
      }
      System.out.println(" " + count);
      tileInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    System.out.println("[WorldMap] INFO - Loading traps...");

    // LOAD TRAPS AND TRIGGERS
    ResultSet areatrapInfo = Server.mapDB.askDB("select * from area_trap");

    try {
      while (areatrapInfo.next()) {
        ResultSet trapInfo =
            Server.mapDB.askDB("select * from trap where Id = " + areatrapInfo.getInt("TrapId"));
        if (trapInfo.next()) {
          Trap newTrap = new Trap();
          newTrap.load(trapInfo);
          newTrap.setId(areatrapInfo.getInt("Id"));
          newTrap.setX(areatrapInfo.getInt("X"));
          newTrap.setY(areatrapInfo.getInt("Y"));
          newTrap.setZ(areatrapInfo.getInt("Z"));

          TrapHandler.addTrap(newTrap);

          MapTiles.get(
                  areatrapInfo.getInt("X")
                      + ","
                      + areatrapInfo.getInt("Y")
                      + ","
                      + areatrapInfo.getInt("Z"))
              .setTrapId(newTrap.getId());
        }
        trapInfo.close();
      }
      areatrapInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    System.out.println("[WorldMap] INFO - Loading triggers...");

    ResultSet triggerInfo = Server.mapDB.askDB("select * from trigger");

    try {
      while (triggerInfo.next()) {
        Trigger newTrigger = new Trigger();
        newTrigger.load(triggerInfo);

        MapTiles.get(
                triggerInfo.getInt("X")
                    + ","
                    + triggerInfo.getInt("Y")
                    + ","
                    + triggerInfo.getInt("Z"))
            .setTrigger(newTrigger);
      }
      triggerInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    System.out.println("[WorldMap] INFO - Loading containers...");

    // LOAD CONTAINERS
    ResultSet containerInfo =
        //                         1   2     3  4  5  6
        Server.mapDB.askDB("select Id, Type, X, Y, Z, Items from area_container order by Y asc, X asc");
    StringBuilder chestinstanceSb = null;
    try {
      while (containerInfo.next()) {
        String coordStr =
            containerInfo.getInt(3)
                + ","
                + containerInfo.getInt(4)
                + ","
                + containerInfo.getInt(5);
        if (MapTiles.get(coordStr) != null) {
          String objectId = containerInfo.getString(2).intern();
          int containerId = containerInfo.getInt(1);
          if (objectId.contains("chestinstance")) {
            if (chestinstanceSb == null) {
              chestinstanceSb = new StringBuilder();
            } else {
              chestinstanceSb.append(',');
            }
            chestinstanceSb.append(containerId);
          }
          MapTiles.get(coordStr).setObjectId(objectId);
          MapTiles.get(coordStr).setContainerId(containerId);
        }
        String items = containerInfo.getString(6);
        if (items!=null && !"".equals(items)) {
          for (String itemid : items.split(","))
            if (ServerGameInfo.itemDef.get(Integer.parseInt(itemid)) == null) {
              System.out.println("[WorldMap] ERROR - Unknown itemid for container #"
                  + containerInfo.getInt(1) + ": " + itemid);
           }
        }

      }
      containerInfo.close();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
    if (chestinstanceSb != null) {
      ContainerHandler.initBlueChest(chestinstanceSb.toString());
    }

    System.out.println("[WorldMap] INFO - Loading souls...");

    // LOAD SOULS                                    1   2 3 4 5
    ResultSet soulInfo = Server.userDB.askDB("select Id, X,Y,Z,CharacterId from character_soul");
    try {
      while (soulInfo.next()) {
        String coords = soulInfo.getInt(2) + "," + soulInfo.getInt(3) + "," + soulInfo.getInt(4);
        if (MapTiles.containsKey(coords)) {
          MapTiles.get(coords)
              .setSoulCharacterId(soulInfo.getInt(5));
        } else {
          Server.userDB.updateDB("delete from character_soul where Id = " + soulInfo.getInt("Id"));
        }
      }
      soulInfo.close();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    System.out.println("[WorldMap] INFO - Loading monsters...");

    // LOAD NPCs FROM DATABASE
    ResultSet creatureInfo;

    creatureInfo = Server.mapDB.askDB("select * from area_creature where AggroType <> 5");

    Npc tempNpc;

    try {
      while (creatureInfo.next()) {
        tempNpc =
            new Npc(
                ServerGameInfo.creatureDef.get(creatureInfo.getInt("CreatureId")),
                creatureInfo.getInt("SpawnX"),
                creatureInfo.getInt("SpawnY"),
                creatureInfo.getInt("SpawnZ"));
        tempNpc.setDBId(creatureInfo.getInt("Id"));

        if (!creatureInfo.getString("Name").isEmpty()) {
          tempNpc.setName(creatureInfo.getString("Name").intern());
        }

        tempNpc.setCreatureType(CreatureType.Monster);

        // Sets eventual equipment
        String equipmentInfo = creatureInfo.getString("Equipment").intern();
        if (!equipmentInfo.equals("None")) {
          String equipment[] = equipmentInfo.split(",");

          for (String itemId_str : equipment) {
            try {
              int itemId = Integer.parseInt(itemId_str);
              if (ServerGameInfo.itemDef.containsKey(itemId)) {
                tempNpc.equipItem(ServerGameInfo.newItem(itemId));
              }
            } catch (NumberFormatException e) {
            }
          }
        }

        // CHANCE OF MAKING MONSTER SPECIAL
        tempNpc.turnSpecial(0);
        int aggrotype = creatureInfo.getInt("AggroType");
        if (aggrotype == 6) {
          aggrotype = 2;
          tempNpc.turnElite();
        }
        else if (aggrotype == 7) {
          aggrotype = 2;
          tempNpc.turnTitan(true);
        }
        else if (aggrotype == 3) {
          dialogNpcs.add(tempNpc);
        }
        tempNpc.setAggroType(aggrotype);
        tempNpc.setOriginalAggroType(aggrotype);

        // SET DAY NIGHT SPAWN TIME
        tempNpc.setExistDayOrNight(creatureInfo.getInt("SpawnCriteria"));

        if (tempNpc.getExistDayOrNight() == 1) {
          tempNpc.die();
          tempNpc.setRespawnTimerReady();
        }

        if (MapTiles.get(
                creatureInfo.getInt("SpawnX")
                    + ","
                    + creatureInfo.getInt("SpawnY")
                    + ","
                    + creatureInfo.getInt("SpawnZ"))
            == null) {
          ServerMessage.println(false,
              "WARNING - Monster on null tile: ",
              creatureInfo.getInt("Id"),
              " (",
              creatureInfo.getInt("SpawnX"),
              ",",
              creatureInfo.getInt("SpawnY"),
              ",",
              creatureInfo.getInt("SpawnZ"),
              ") -> ",
              creatureInfo.getInt("CreatureId"));
        } else {
          Monsters.put(creatureInfo.getInt("Id"), tempNpc);
          if (!tempNpc.isDead()) {
            MapTiles.get(
                    creatureInfo.getInt("SpawnX")
                        + ","
                        + creatureInfo.getInt("SpawnY")
                        + ","
                        + creatureInfo.getInt("SpawnZ"))
                .setOccupant(CreatureType.Monster, tempNpc);
          }
        }

        if (creatureInfo.getInt("Id") > newMonsterId) {
          newMonsterId = creatureInfo.getInt("Id");
        }

        // Add monster to z-index maps
        addMonsterToZ(tempNpc, tempNpc.getZ());
      }
      creatureInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (ServerSettings.RANDOM_ARCHIPELAGO) {
      System.out.println("[WorldMap] INFO - Generating random archipelago...");

      // Pirate island
      zLevels.add(-200);
      playersByZ.put(-200, new Vector<PlayerCharacter>());
      monstersByZ.put(-200, new Vector<Npc>());

      PirateIslandGenerator.generate(MapTiles);
    }


    if (ServerSettings.RANDOM_DUNGEON) {
      System.out.println("[WorldMap] INFO - Generating random dungeons...");

      // Generate instances
      DungeonGenerator dGenerator = new DungeonGenerator();
      dGenerator.generate(MapTiles, 10);

      dGenerator.generate(MapTiles, 20);

      dGenerator.generate(MapTiles, 30);
    }

    System.out.println("[WorldMap] INFO - Loading finished");
  }

  public void addMonsterToZ(Npc npc, int z) {
    if (monstersByZ.containsKey(z)) {
      monstersByZ.get(z).add(npc);
    }
  }

  public void addPlayerToZ(PlayerCharacter player, int z) {
    if (playersByZ.containsKey(z)) {
      playersByZ.get(z).add(player);
    }
  }

  public void removePlayerFromZ(PlayerCharacter player, int z) {
    if (playersByZ.containsKey(z)) {
      playersByZ.get(z).remove(player);
    }
  }

  public void removeMonsterFromZ(Npc monster, int z) {
    if (monstersByZ.containsKey(z)) {
      monstersByZ.get(z).remove(monster);
    }
  }

  /*
   *
   * MONSTER MOVEMENT
   *
   */

  public Vector<Npc> moveNonAggroMonsters() {
    Vector<Npc> movedMonsters = new Vector<Npc>();

    for (int z : zLevels) {
      if (playersByZ.get(z).size() > 0) {
        for (Iterator<Npc> iter = monstersByZ.get(z).iterator(); iter.hasNext(); ) {
          Npc m = iter.next();
          if (!m.isAggro() && !m.isDead() && m.isReadyToMove()) {

            // Respawn guardian if not at spawn point
            if (m.getOriginalAggroType() == 5) {
              if (m.getX() != m.getSpawnX() || m.getY() != m.getSpawnY()) {
                // Send dissappearence to players
                // MONSTER DISSAPPEAR
                Server.WORLD_MAP
                    .getTile(m.getX(), m.getY(), m.getZ())
                    .setOccupant(CreatureType.None, null);

                for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
                  Client s = entry.getValue();
                  if (s.Ready) {
                    if (Handler.isVisibleForPlayer(
                        s.playerCharacter, m.getX(), m.getY(), m.getZ())) {
                      Handler.addOutGoingMessage(s, "monstergone", m.getSmallData());
                    }
                  }
                }

                m.respawn();
                Server.WORLD_MAP
                    .getTile(m.getX(), m.getY(), m.getZ())
                    .setOccupant(m.getCreatureType(), m);

                // MONSTER APPEAR
                for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
                  Client s = entry.getValue();
                  if (s.Ready) {
                    if (Handler.isVisibleForPlayer(
                        s.playerCharacter, m.getSpawnX(), m.getSpawnY(), m.getSpawnZ())) {
                      Handler.addOutGoingMessage(s, "respawnmonster", m.getSmallData());
                    }
                  }
                }
              }
            } else if (MonsterHandler.movingMonsterTypes.contains(m.getAggroType())) {
              boolean monsterMoved = false;
              if (m.distanceFromSpawn() > 8.0) {
                monsterMoved = m.moveTowardSpawn();
              }
              if (!monsterMoved) {
                if (m.getHealth() < m.getStat("MAX_HEALTH") && !m.isResting()) {
                  MonsterHandler.changeMonsterSleepState(m, true);
                } else if (!m.isResting()) {
                  // MOVE RANDOM DIRECTION
                  int randomDir = ThreadLocalRandom.current().nextInt(8);

                  monsterMoved = true;

                  if (randomDir > 3) {
                    int gotoX = m.getX();
                    int gotoY = m.getY();

                    if (m.getGotoRotation() == 0.0f) {
                      gotoY--;
                    } else if (m.getGotoRotation() == 90.0f) {
                      gotoX++;
                    } else if (m.getGotoRotation() == 180.0f) {
                      gotoY++;
                    } else if (m.getGotoRotation() == 270.0f) {
                      gotoX--;
                    }

                    if (isPassableTileForMonster(m, gotoX, gotoY, m.getZ())) {
                      // Remove monster from tile
                      MapTiles.get(m.getX() + "," + m.getY() + "," + m.getZ())
                          .setOccupant(CreatureType.None, null);

                      m.walkTo(gotoX, gotoY, m.getZ());

                      // Occupy tile with monster
                      MapTiles.get(m.getX() + "," + m.getY() + "," + m.getZ())
                          .setOccupant(CreatureType.Monster, m);
                    }
                  } else if (randomDir == 0) {
                    m.setGotoRotation(0.0f);
                  } else if (randomDir == 1) {
                    m.setGotoRotation(90.0f);
                  } else if (randomDir == 2) {
                    m.setGotoRotation(180.0f);
                  } else if (randomDir == 3) {
                    m.setGotoRotation(270.0f);
                  }

                  m.startMoveTimer(false);

                  if (monsterMoved) {
                    // CHECK IF TILE HAS TRIGGER
                    if (MapTiles.get(m.getX() + "," + m.getY() + "," + m.getZ()).getTrigger()
                        != null) {
                      Trigger T =
                          MapTiles.get(m.getX() + "," + m.getY() + "," + m.getZ()).getTrigger();

                      T.setTriggered(true);

                      // IF TRIGGERS TRAP, TRAP EFFECT
                      if (T.getTrapId() > 0) {
                        TrapHandler.triggerTrap(T.getTrapId(), m.getX(), m.getY(), m.getZ());
                      }
                    }
                  }
                }
              }
              if (monsterMoved) {
                movedMonsters.add(m);
              }
            }
          } else if (!m.isDead()) {
            MapTiles.get(m.getX() + "," + m.getY() + "," + m.getZ())
                .setOccupant(CreatureType.Monster, m);
          }
        }
      }
    }

    return movedMonsters;
  }

  public List<Npc> checkRespawns() {

    List<Npc> respawnInfo = new ArrayList<>(Monsters.size());

    for (Iterator<Npc> iter = Monsters.values().iterator(); iter.hasNext(); ) {
      Npc m = iter.next();

      if (m.isDead() && !m.isSpawned()) {
        boolean okToRespawn = true;

        if (m.getExistDayOrNight() > 0 && m.getExistDayOrNight() != MapHandler.dayNightTime) {
          okToRespawn = false;
          m.setRespawnTimerReady();
        } else {
          if (m.getCreatureId() != 1) {
            // CHECK IF VISIBLE BY PLAYER
            for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
              Client client = entry.getValue();

              boolean visibleByPlayer = false;

              if (client.Ready) {
                if (client.playerCharacter.getZ() == m.getSpawnZ()
                    && Math.abs(client.playerCharacter.getX() - m.getSpawnX()) < 11
                    && Math.abs(client.playerCharacter.getY() - m.getSpawnY()) < 6) {
                  visibleByPlayer = true;
                }
              }

              if (visibleByPlayer) {
                okToRespawn = false;
                break;
              }
            }
          }
        }

        if (okToRespawn) {
          if (m.checkRespawn()) {

            /*
            // Chance of spawning more monsters if not npc
            if(m.getOriginalAggroType() != 3 && !m.isBoss() && m.getCreatureId() != 1){
              //int nrSpawns = RandomUtils.getInt(0, 2);
              int nrSpawns = 0;
              for(int i = 0; i < nrSpawns; i++){
                Npc newSpawn = new Npc(ServerGameInfo.creatureDef.get(m.getCreatureId()), m.getSpawnX(),m.getSpawnY(),m.getSpawnZ());
                newSpawn.setExistDayOrNight(m.getExistDayOrNight());
                newSpawn.setAggroType(m.getOriginalAggroType());
                Server.WORLD_MAP.addMonsterSpawn(newSpawn, m.getSpawnX(),m.getSpawnY(),m.getSpawnZ());
              }
            }
            */

            MapTiles.get(m.getX() + "," + m.getY() + "," + m.getZ())
                .setOccupant(CreatureType.Monster, m);
            respawnInfo.add(m);
          }
        }
      }

      if (m.isDead() && m.isSpawned()) {
        iter.remove();
      }
    }

    return respawnInfo;
  }

  public String getAggroInfo() {
    StringBuilder aggroInfo = new StringBuilder(1000);

    for (Iterator<Npc> iter = Monsters.values().iterator(); iter.hasNext(); ) {
      Npc m = iter.next();

      int aggroStatus = 0;

      if (!m.isDead() && m.isAggro()) {
        aggroStatus = 1;
      }

      aggroInfo.append(aggroStatus).append(',').append(m.getDBId());

      if (aggroStatus == 1) {
        aggroInfo.append(',')
                 .append(m.getStat("MAX_HEALTH"))
                 .append(',')
                 .append(m.getHealth())
                 .append(';');
      } else {
        aggroInfo.append(';');
      }
    }

    return aggroInfo.toString();
  }

  /*
   *
   *
   * 	PROJECTILES
   *
   *
   */

  public String checkProjectileObstacles(
      int startX, int startY, int startZ, int goalX, int goalY, int goalZ) {
    String goalTile = goalX + "," + goalY;

    float startXf = startX;
    float startYf = startY;

    float goalXf = goalX;
    float goalYf = goalY;

    float dX = (goalXf - startXf) * 50.0f;
    float dY = (goalYf - startYf) * 50.0f;

    float gotoItr = 0.0f;

    if (Math.abs(dX) > Math.abs(dY)) {
      gotoItr = Math.abs(dX);
      dY = dY / Math.abs(dX);
      dX = dX / Math.abs(dX);
    } else {
      gotoItr = Math.abs(dY);
      dX = dX / Math.abs(dY);
      dY = dY / Math.abs(dY);
    }

    startXf *= 50;
    startYf *= 50;

    for (float itr = 0.0f; itr < gotoItr; itr++) {
      startXf += dX;
      startYf += dY;

      if (!MapTiles.get(Math.round(startXf / 50) + "," + Math.round(startYf / 50) + "," + startZ)
          .isPassableType()) {
        startXf -= dX;
        startYf -= dY;

        goalTile = Math.round(startXf / 50) + "," + Math.round(startYf / 50);
        break;
      }
    }
    return goalTile;
  }

  public void addMonster(Npc m) {
    newMonsterId++;

    m.setDBId(newMonsterId);
    m.setCreatureType(CreatureType.Monster);
    m.setReadyToMove(true);
    m.loadEquipment();
    Monsters.put(newMonsterId, m);
    monstersByZ.get(m.getZ()).add(m);
  }

  public synchronized void addMonsterSpawn(Npc m, int posX, int posY, int posZ) {
    newMonsterId++;

    m.setDBId(newMonsterId);
    m.setSpawned(true);
    m.setJustSpawned(true);
    m.setCreatureType(CreatureType.Monster);
    m.setReadyToMove(true);

    m.loadEquipment();

    for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client s = entry.getValue();

      if (s.Ready && Handler.isVisibleForPlayer(s.playerCharacter, posX, posY, posZ)) {
        Handler.addOutGoingMessage(s, "respawnmonster", m.getSmallData());
      }
    }

    monstersByZ.get(m.getZ()).add(m);

    Monsters.put(newMonsterId, m);
  }

  /*
   *
   * GETTER / SETTER
   */

  public HashMap<String, Tile> getMap() {
    return MapTiles;
  }

  public Map<Integer, Npc> getMonsters() {
    return Monsters;
  }

  public Tile getTile(int x, int y, int z) {
    return MapTiles.get(x + "," + y + "," + z);
  }

  public Npc getMonster(Integer monsterId) {
    return Monsters.get(monsterId);
  }

  public int getEntranceX() {
    return entranceX;
  }

  public int getEntranceY() {
    return entranceY;
  }

  public int getNrPlayers() {
    return NrPlayers;
  }

  public int getNrMonsters() {
    return Monsters.size();
  }

  public String getMonstersPosAsString() {
    StringBuilder mobinfo = new StringBuilder(1000);

    for (Iterator<Npc> iter = Monsters.values().iterator(); iter.hasNext(); ) {
      Npc m = iter.next();

      // SEND dbId, newX, newY
      mobinfo.append(m.getDBId())
             .append(',')
             .append(m.getX())
             .append(',')
             .append(m.getY())
             .append(',')
             .append(m.getStat("SPEED"))
             .append(';');
    }
    return mobinfo.toString();
  }

  public HashMap<String, Tile> getMapTiles() {
    return MapTiles;
  }

  public boolean isPassableTile(int X, int Y, int Z) {
    if (MapTiles.get(X + "," + Y + "," + Z) == null) {
      return false;
    }
    return MapTiles.get(X + "," + Y + "," + Z).isPassable();
  }

  public boolean isPassableTileForMonster(Creature c, int X, int Y, int Z) {
    Tile gotoTile = MapTiles.get(X + "," + Y + "," + Z);

    if (gotoTile == null) {
      return false;
    }

    if (gotoTile.getAreaEffectId() > 0) {
      AreaEffect ae = ServerGameInfo.areaEffectsDef.get(gotoTile.getAreaEffectId());
      if (ae.isImpassableForMonsters()) {
        return false;
      }
    }

    if (gotoTile.getTrapId() > 0) {
      return false;
    }

    if (gotoTile.getDoorId() > 0 || gotoTile.getGeneratedDoor() != null) {
      return false;
    }

    /*
    if(gotoTile.getStatusEffects().size() > 0){
      if(c.isAggro()){
        for(StatusEffect se: gotoTile.getStatusEffects()){
          if(!c.hasStatusEffect(se.getId())){
            return false;
          }
        }
      }else{
        return false;
      }
    }
     */

    if (!c.getFamily().equals("Ocean Monsters") && gotoTile.isWater()) {
      if (gotoTile.getObjectId().contains("bridge")) {
        return true;
      }
      return false;
    } else if (c.getFamily().equals("Ocean Monsters") && !gotoTile.isWater()) {
      return false;
    }

    return gotoTile.isPassableNonAggro();
  }

  public boolean isPassableTileForPlayer(PlayerCharacter player, int X, int Y, int Z) {

    Tile gotoTile = MapTiles.get(X + "," + Y + "," + Z);
    return (gotoTile!=null) && gotoTile.isPassableForPlayer(player);
  }

  public boolean isPassableTile(Creature cr, int x, int y, int z) {
    if (cr.getCreatureType() == Creature.CreatureType.Player) {
      return isPassableTileForPlayer((PlayerCharacter) cr, x, y, z);
    } else {
      return isPassableTileForMonster(cr, x, y, z);
    }
  }

  public Point findClosestFreeTile(int startX, int startY, int startZ) {
    Spiral s = new Spiral(8, 8);
    List<Point> l = s.spiral();

    for (Point p : l) {
      if (p.getX() != 0 || p.getY() != 0) {
        int targetX = (int) (startX + p.getX());
        int targetY = (int) (startY + p.getY());

        if (getTile(targetX, targetY, startZ) != null) {
          if (getTile(targetX, targetY, startZ).isPassableNonAggro()) {
            return new Point(targetX, targetY);
          }
        }
      }
    }
    return null;
  }

  public Tile findClosestEmptyTile(int startX, int startY, int startZ) {
    Spiral s = new Spiral(8, 8);
    List<Point> l = s.spiral();

    for (Point p : l) {
      int targetX = (int) (startX + p.getX());
      int targetY = (int) (startY + p.getY());

      Tile ret = getTile(targetX, targetY, startZ);
      if (ret != null
      && !ret.hasLoot()
      && !"None".equals(ret.getObjectId())
      ) {
        return ret;
      }
    }
    return getTile(startX, startY, startZ);
  }

  public boolean isType(String type, int x, int y, int z) {
    Tile tile = getTile(x, y, z);
    return (tile != null && type.equals(tile.getType()));
  }
}
