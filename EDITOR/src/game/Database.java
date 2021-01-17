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

import java.text.SimpleDateFormat;
import java.util.Date;

import map.Container;
import map.Tile;

import static java.nio.file.StandardCopyOption.*;

public class Database {

  private Connection conn;
  public final String name;

  public final static SimpleDateFormat TIMESTAMP_FMT = new SimpleDateFormat("yyyy-dd-MM_HHmmss");

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
      Path destination = FileSystems.getDefault().getPath(EditorSettings.PATH, name + "."+ TIMESTAMP_FMT.format(new Date()) + ".bak");
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
    updateDB("CREATE INDEX IF NOT EXISTS tile_door on area_tile (DoorId);");
    updateDB("DROP INDEX IF EXISTS tile_pos;");
    updateDB("CREATE UNIQUE INDEX IF NOT EXISTS tile_pos_uniq ON area_tile (z,x,y);");
    updateDB("VACUUM;");

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
          + ", Locked = 0"
          + " where Id = " + doorId);
    }
  }

  public void createDoor(String tileId, String doorId, String locked) {
    if (doorId==null || "0".equals(doorId)) {
      updateDB("insert into door (GotoX, GotoY, GotoZ, Locked) values ("
          + "0,0,0," + locked + ")");
      doorId = getLastKey();

      updateDB("update area_tile set DoorId = " + doorId
          + " where id = " + tileId);
    }
    else {
      updateDB(
          "update door set GotoX = 0"
          + ", GotoY = 0"
          + ", GotoZ = 0"
          + ", Locked = " + locked
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
