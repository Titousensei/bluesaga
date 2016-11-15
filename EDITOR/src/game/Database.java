package game;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import components.Monster;

import map.Container;
import map.Tile;

import static java.nio.file.StandardCopyOption.*;

public class Database {

  private Connection conn;
  public final String name;

  public Database(String name) throws ClassNotFoundException {
    Class.forName("org.sqlite.JDBC");
    this.name = name;

    try {
      conn = DriverManager.getConnection("jdbc:sqlite:" + EditorSettings.PATH + name + ".db");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void bootstrapMap()
  {
    try {
      Path source      = FileSystems.getDefault().getPath(EditorSettings.PATH, name + ".db");
      Path destination = FileSystems.getDefault().getPath(EditorSettings.PATH, name + ".bak");
      Files.copy(source, destination, REPLACE_EXISTING);
      System.out.println("Map backup: " + destination);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

    updateDB("CREATE TABLE IF NOT EXISTS area_creature (Id integer PRIMARY KEY  DEFAULT (NULL) ,AreaId integer,CreatureId integer,MobLevel integer DEFAULT (NULL) ,SpawnX integer DEFAULT (NULL) ,SpawnY integer DEFAULT (NULL) ,SpawnZ INTEGER DEFAULT (0) , Special VARCHAR DEFAULT None, AggroType INTEGER DEFAULT 0, Name VARCHAR, SpawnCriteria INTEGER DEFAULT 0, Equipment VARCHAR DEFAULT None);");
    updateDB("CREATE TABLE IF NOT EXISTS area_tile (Id INTEGER PRIMARY KEY  NOT NULL ,X INTEGER,Y INTEGER,Z INTEGER,Type VARCHAR,Name VARCHAR,Passable INTEGER,ObjectId VARCHAR DEFAULT ('None') , DoorId INTEGER DEFAULT 0, AreaEffectId INTEGER DEFAULT 0);");
    updateDB("CREATE TABLE IF NOT EXISTS door (Id integer PRIMARY KEY  DEFAULT (0) ,GotoX integer DEFAULT (0) ,GotoY INTEGER DEFAULT (0) ,GotoZ INTEGER DEFAULT (0) ,Locked INTEGER DEFAULT (0) , CreatureIds VARCHAR DEFAULT None, Premium INTEGER DEFAULT 0);");
    updateDB("CREATE TABLE IF NOT EXISTS checkpoint (Id INTEGER PRIMARY KEY  NOT NULL ,X INTEGER NOT NULL  DEFAULT (0) ,Y INTEGER NOT NULL  DEFAULT (0) ,Z INTEGER DEFAULT (0) , NpcId INTEGER DEFAULT 0);");
    updateDB("CREATE TABLE IF NOT EXISTS area_effect (Id INTEGER PRIMARY KEY  NOT NULL ,AreaName VARCHAR NOT NULL ,Tint INTEGER DEFAULT (0) ,Fog INTEGER DEFAULT (0) ,FogColor VARCHAR, TintColor VARCHAR, Song VARCHAR DEFAULT None, Ambient VARCHAR DEFAULT None, AreaItems VARCHAR DEFAULT None, AreaCopper INTEGER DEFAULT 0, Particles VARCHAR DEFAULT None, GuardLevel INTEGER DEFAULT 0);");
    updateDB("CREATE TABLE IF NOT EXISTS area_container (Id INTEGER PRIMARY KEY  NOT NULL ,X INTEGER DEFAULT (null) ,Y INTEGER DEFAULT (null) ,Z INTEGER DEFAULT (null) ,Type VARCHAR,Items VARCHAR,Fixed INTEGER DEFAULT (1) , Copper INTEGER DEFAULT 0);");
    updateDB("CREATE TABLE IF NOT EXISTS editor_option (X INTEGER, Y INTEGER, Z INTEGER);");
    updateDB("CREATE TABLE IF NOT EXISTS area_trap (Id INTEGER PRIMARY KEY  NOT NULL ,X INTEGER,Y INTEGER,Z INTEGER, TrapId INTEGER DEFAULT 0);");
    updateDB("CREATE TABLE IF NOT EXISTS trap (Id INTEGER PRIMARY KEY  NOT NULL  UNIQUE , Active INTEGER DEFAULT 0, Damage INTEGER DEFAULT 0, DamageType VARCHAR DEFAULT None, AbilityId INTEGER DEFAULT 0, Repeat INTEGER DEFAULT 1, Name VARCHAR, EffectSpan INTEGER DEFAULT 1);");
    updateDB("CREATE TABLE IF NOT EXISTS trigger (Id INTEGER PRIMARY KEY  NOT NULL  UNIQUE , X INTEGER DEFAULT 0, Y INTEGER DEFAULT 0, Z INTEGER DEFAULT 0, TrapId INTEGER DEFAULT 0, DoorId INTEGER DEFAULT 0, ActiveTime INTEGER DEFAULT 0);");
    updateDB("CREATE TABLE IF NOT EXISTS quest (Id INTEGER PRIMARY KEY, Name VARCHAR, RewardMessage VARCHAR, QuestMessage VARCHAR, Level INTEGER, Type VARCHAR, TargetNumber INTEGER, TargetType VARCHAR, TargetId INTEGER, NpcId INTEGER, OrderNr INTEGER, RewardXp INTEGER, RewardItemId INTEGER, RewardCopper INTEGER, ParentQuestId INTEGER, Description TEXT, EventId INTEGER, NextQuestId INTEGER, ReturnForReward INTEGER, QuestItems VARCHAR, RewardAbilityId INTEGER DEFAULT 0, LearnClassId INTEGER DEFAULT 0, QuestAbilityId INTEGER DEFAULT 0, CategoryId INTEGER DEFAULT 0);");
    updateDB("CREATE TABLE IF NOT EXISTS quest_category (Id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , Name VARCHAR);");
    updateDB("CREATE INDEX IF NOT EXISTS area_door on area_tile (DoorId);");
    updateDB("CREATE INDEX IF NOT EXISTS pos ON area_tile (z,x,y);");

    boolean isEmpty = true;
    try (ResultSet rs = askDB("SELECT count(*) FROM area_effect")) {
      if (rs.next()) {
        if (rs.getInt(1) > 0) {
          isEmpty = false;
        }
      }
      rs.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (isEmpty) {
      updateDB("PRAGMA foreign_keys=OFF;");
      updateDB("BEGIN TRANSACTION;");
      updateDB("INSERT INTO trap VALUES(1,1,20,'PIERCE',0,0,'trap/spikes',2);");
      updateDB("INSERT INTO trap VALUES(2,0,0,'None',37,1,'trap/drakeheadl',0);");
      updateDB("INSERT INTO trap VALUES(3,0,0,'None',37,1,'trap/drakeheadr',0);");
      updateDB("INSERT INTO trap VALUES(4,0,0,'None',37,1,'trap/drakeheadu',0);");
      updateDB("COMMIT;");

      updateDB("PRAGMA foreign_keys=OFF;");
      updateDB("BEGIN TRANSACTION;");
      updateDB("INSERT INTO area_effect VALUES(1,'The Old Cemetery',1,1,'206,255,198','100,100,150','dungeon','none','None',5,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(2,'Chompa Woods',0,0,'255,255,255','255,255,255','forest','forest','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(3,'The Chompa Crypt',0,1,'200,255,150','255,255,255','dungeon','none','None',6,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(4,'Scarab Cave',0,1,'253,151,241','255,255,255','dungeon','dungeon','None',4,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(5,'Chompa Inn',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(6,'Larva Lair',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(7,'???',0,1,'168,91,232','255,255,255','event','dungeon','None',0,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(8,'The Emerald Seas',0,0,'255,255,255','255,255,255','sea','title','None',6,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(9,'Green Leaf Village',0,0,'255,255,255','255,255,255','greenleaf','forest','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(10,'Minai''s Shop',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(11,'Landlubber Inn',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(12,'Elmk''s Weapon Shop',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(13,'???',1,1,'206,255,198','100,100,150','ghosts','None','None',0,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(14,'Shroom Forest',0,0,'255,255,255','255,255,255','forest','forest','None',6,'spore',1);");
      updateDB("INSERT INTO area_effect VALUES(15,'West Lockwoods',0,0,'255,255,255','255,255,255','forest','forest','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(16,'Goulda''s Cellar',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',12,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(17,'Goulda''s House',0,0,'255,255,255','255,255,255','indoors','indoors','None',4,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(18,'Bat Cave',0,1,'45,255,135','255,255,255','batcave','dungeon','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(19,'Spider Cave',0,1,'255,128,128','255,255,255','dungeon','dungeon','None',7,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(20,'Shroom King''s Lair',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(21,'Shroom''s Den',0,1,'141,255,114','255,255,255','dungeon','dungeon','3,5',14,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(22,'Bounty Hut',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(23,'Island of the Buried',0,1,'206,255,198','255,255,255','dungeon','none','None',5,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(24,'The Forgottens Abode',0,0,'255,255,255','255,255,255','ghosts','None','None',4,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(25,'The Hidden Archipelago',0,0,'255,255,255','255,255,255','sea','title','None',6,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(26,'East Lockwoods',0,1,'255,255,255','255,255,255','forest','forest','None',4,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(27,'The Botanical Maze',0,0,'255,255,255','255,255,255','forest','forest','None',6,'spore',1);");
      updateDB("INSERT INTO area_effect VALUES(28,'The Forest Temple',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',15,'spore',1);");
      updateDB("INSERT INTO area_effect VALUES(29,'The Forest Temple Exit',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',15,'spore',1);");
      updateDB("INSERT INTO area_effect VALUES(30,'Green Leaf Arena',0,0,'255,255,255','255,255,255','event','none','None',3,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(31,'Spider Queen Lair',0,1,'255,128,128','255,255,255','event','dungeon','None',4,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(32,'Luri & Guri''s Hut',0,0,'255,255,255','255,255,255','indoors','indoors','None',6,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(33,'Borgo''s Barber Shop',0,0,'255,255,255','255,255,255','indoors','indoors','None',6,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(34,'The Spring Hills',0,0,'255,255,255','255,255,255','forest','forest','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(35,'Oakstone Island',0,0,'255,255,255','255,255,255','forest','forest','None',8,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(36,'Carapace Island',0,1,'255,255,255','255,255,255','sea','title','None',4,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(37,'Cray Island',0,1,'255,255,255','255,255,255','sea','title','None',4,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(38,'Mount Morwyn',0,1,'255,255,255','255,255,255','mountain','title','None',4,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(39,'Oozemaw Fortress',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(40,'Oozemaw''s Hideout',0,1,'168,91,232','255,255,255','event','dungeon','None',20,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(41,'The Crimson Ocean',0,0,'255,255,255','255,255,255','sea','title','None',12,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(42,'Crab Den',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',8,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(43,'Morwyn''s Labyrinth',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(44,'Red Rock Mining Town',0,1,'255,255,255','255,255,255','mountain','title','None',4,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(45,'Gimhwick''s Shop',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(46,'Horgo''s Barber Shop',0,0,'255,255,255','255,255,255','indoors','indoors','None',6,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(47,'Borkh''s Weapon Shop',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(48,'Red Rock Inn',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(49,'Sorkorh''s Chambers',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',15,'spore',0);");
      updateDB("INSERT INTO area_effect VALUES(50,'The Dead Gates',1,1,'206,255,198','100,100,100','dungeon','none','None',5,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(51,'Oranga Haven',0,0,'255,255,255','255,255,255','forest','forest','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(52,'Oozemaw''s secret passage',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(53,'The Goblin Archives',0,0,'255,255,255','255,255,255','goblin','dungeon','3,5',10,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(54,'The Goblin Kings Throne Room',0,0,'255,255,255','255,255,255','event','dungeon','None',10,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(55,'Ollghar''s Island',0,0,'255,255,255','255,255,255','forest','forest','None',8,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(56,'Fur Island',0,0,'255,255,255','255,255,255','forest','forest','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(57,'The North',0,0,'255,255,255','255,255,255','snow','title','179',8,'snow',0);");
      updateDB("INSERT INTO area_effect VALUES(58,'Water Cave',0,1,'45,255,135','255,255,255','watercave','None','None',8,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(59,'Ollghar''s Hidden Cave',0,1,'255,128,128','255,255,255','dungeon','dungeon','None',7,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(60,'The Safehouse',0,0,'255,255,255','255,255,255','indoors','indoors','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(61,'Northern Passage',0,1,'45,255,135','255,255,255','icecave','None','179',8,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(62,'The Lockwoods River',0,0,'255,255,255','255,255,255','sea','title','None',6,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(63,'The Luna Passage',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(64,'Warrior Hut',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(65,'Hunter Hut',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(66,'Mage Hut',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(67,'The Frostvale',0,0,'255,255,255','255,255,255','snow','title','179',8,'snow',1);");
      updateDB("INSERT INTO area_effect VALUES(68,'Frostvale''s Tower',0,0,'255,255,255','255,255,255','indoors','indoors','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(69,'The Cold Luna Cave',0,1,'45,255,135','255,255,255','icecave','None','179',8,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(70,'Ollghar''s Lab',0,1,'168,91,232','255,255,255','event','dungeon','None',0,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(71,'The Lockwood Prison',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(72,'The Outlaw Harbor',0,0,'255,255,255','255,255,255','outlaw','None','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(73,'Foxer''s Weapon Shop',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(74,'Gulhrug''s Potion Shop',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(75,'Secret Chamber',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(76,'Secret Chamber',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(77,'Secret Chamber',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(78,'The Unreachable Spot',0,0,'255,255,255','255,255,255','greenleaf','forest','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(79,'A Hidden Path',0,0,'255,255,255','255,255,255','forest','forest','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(80,'A Secret Watercave',0,1,'45,255,135','255,255,255','watercave','None','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(81,'Secret Chamber',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(82,'The Dead Woods',0,1,'45,255,135','255,255,255','batcave','dungeon','None',8,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(83,'A Secret Chamber',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(84,'A Secret Chamber',0,0,'255,255,255','255,255,255','dungeon','dungeon','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(85,'Blood Skull''s Crew Hall',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(86,'Sea Bunnies Crew Hall',0,0,'255,255,255','255,255,255','indoors','indoors','None',2,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(87,'Squid Water Cave',0,1,'45,255,135','255,255,255','watercave','None','None',8,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(88,'The Hollow',0,1,'45,255,135','255,255,255','batcave','dungeon','None',10,'None',1);");
      updateDB("INSERT INTO area_effect VALUES(89,'Mystic Mines',0,1,'45,255,135','255,255,255','batcave','dungeon','None',20,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(90,'Crystal Caves',0,1,'45,255,135','255,255,255','icecave','dungeon','None',20,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(91,'The Lost Archipelago',0,0,'255,255,255','255,255,255','forest','forest','None',8,'None',0);");
      updateDB("INSERT INTO area_effect VALUES(92,'The Emerald Seas',0,0,'255,255,255','255,255,255','sea','title','None',6,'None',1);");
      updateDB("COMMIT;");
    }
  }

  public void saveTile(int AreaId, Tile newTile, int x, int y) {
    updateDB(
        "update area_tile set Type = '"
            + newTile.getType()
            + "', Number = "
            + newTile.getName()
            + " where X = "
            + x
            + " and Y = "
            + y
            + " and AreaId = "
            + AreaId);
  }

  public void saveTiles(int AreaId, Tile newTile, int x, int y, int brushSize) {

    updateDB(
        "update area_tile set Type = '"
            + newTile.getType()
            + "', Number = "
            + newTile.getName()
            + " where X >= "
            + x
            + " and X < "
            + (x + brushSize)
            + " and Y <= "
            + y
            + " and Y > "
            + (y - brushSize)
            + " and AreaId = "
            + AreaId);
  }

  public int addDoor(int areaId, int x, int y) {
    int doorId = 0;
    updateDB(
        "insert into door (AreaId, X, Y, GotoX, GotoY, GoToAreaId, Locked) values ("
            + areaId
            + ","
            + x
            + ","
            + y
            + ",0,0,0,0)");
    try (ResultSet rs = askDB("select Id from door order by Id desc limit 1")) {
      if (rs.next()) {
        doorId = rs.getInt("Id");
      }
      rs.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return doorId;
  }

  public int checkIfDoor(int areaId, int x, int y) {
    int ret = 0;
    try (ResultSet rs = askDB(
        "select Id from door where AreaId = " + areaId + " and X = " + x + " and Y = " + y)
    ) {
      if (rs.next()) {
        ret = rs.getInt("Id");
      }
      rs.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return ret;
  }

  public void deleteMap(int AreaId) {
    updateDB("delete from area_tile where AreaId = " + AreaId);
    updateDB("delete from area_creature where AreaId = " + AreaId);
    updateDB("delete from area_container where AreaId = " + AreaId);
  }

  public void saveTileObject(int AreaId, Container newContainer, int x, int y) {

    updateDB(
        "update container set Type = '"
            + newContainer.getType()
            + "', X = "
            + x
            + ", Y = '"
            + y
            + "' where X = "
            + x
            + " and Y = "
            + y
            + " and AreaId = "
            + AreaId);
  }
/*
  public void addMonster(int AreaId, Monster newMonster, int x, int y) {
    updateDB(
        "insert into area_creature (AreaId,CreatureId,MobLevel, SpawnX,SpawnY,Special,AggroType, NpcName) values ("
            + AreaId
            + ","
            + newMonster.getId()
            + ",1,"
            + x
            + ","
            + y
            + ",'"
            + newMonster.getSpecialType()
            + "',2,'None')");
  }
*/
  public void removeMonster(int AreaId, int x, int y) {
    updateDB(
        "delete from area_creature where AreaId = "
            + AreaId
            + " and SpawnX = "
            + x
            + " and SpawnY = "
            + y);
  }

  public void createDoor(String tileId, String doorId, int tileX, int tileY, int tileZ) {
    if (doorId==null || "0".equals(doorId)) {
      updateDB("insert into door (GotoX, GotoY, GotoZ, Locked) values ("
          + tileX + "," + tileY + "," + tileZ + ",0)");
      doorId = getLastKey();

      updateDB("update area_tile set DoorId = " + doorId
          + " where id = " + tileId);
    }
    else {
      updateDB(
          "update door set GotoX = " + tileX
          + ", GotoY = " + tileY
          + ", GotoZ = " + tileZ
          + " where Id = " + doorId);
    }
  }

  public void update(String sqlStatement) throws SQLException {
    Statement stat = conn.createStatement();
    stat.execute(sqlStatement);
    stat.close();
  }

  public void updateDB(String sqlStatement) {
    try (Statement stat = conn.createStatement()) {
      stat.execute(sqlStatement);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public ResultSet askDB(String sqlStatement) {
    try {
      Statement stat = conn.createStatement();

      if (stat.execute(sqlStatement)) {
        return stat.getResultSet();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getLastKey() {
    String ret = null;
    try (ResultSet rs = askDB("SELECT last_insert_rowid()")) {
      if (rs.next()) {
        ret = rs.getString(1);
      }
      rs.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return ret;
  }
}
