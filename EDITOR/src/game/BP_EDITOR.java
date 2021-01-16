package game;

import gui.Font;
import gui.Gui;
import gui.MouseCursor;
import utils.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import map.RandomDungeon;
import map.Tile;
import map.TileObject;
import map.WorldMap;
import menus.AreaEffectMenu;
import menus.BaseMenu;
import menus.DungeonMenu;
import menus.GenericIdMenu;
import menus.MonsterMenu;
import menus.ObjectMenu;
import menus.TextureMenu;
import utils.RandomUtils;
import utils.Coords;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.ScalableGame;
import components.Creature;
import components.Monster;
import graphics.ImageResource;

public class BP_EDITOR extends BasicGame {

  public enum Mode {
      CLEAR, EFFECT, TRAP, TRIGGER, DOOR, DESTINATION, PASSABLE, BRUSH,
      DELETE_TILE, DELETE_OBJECT, DELETE_MONSTER, MINI_MAP, AGGRO, KEY,
      MOVE
  };

  private static AppGameContainer app;

  // INIT AND RESOLUTION
  private static int SCREEN_WIDTH = 1024; // 1024
  private static int SCREEN_HEIGHT = 640; // 640

  private static int MINI_MAP_SIZE = 600;

  private static boolean FULL_SCREEN = false;
  private static final int FRAME_RATE = 60;
  public static int TILE_SIZE = 50;

  public static int SHOW_PASSABLE = 0;

  public static Font FONTS;

  public static Database mapDB;
  public static Database gameDB;

  private Map<Coords, Color> MiniMap = new HashMap<>();

  public static ImageResource GFX;

  // CONTROL
  private static Input INPUT;

  public static int PLAYER_X = 5000;
  public static int PLAYER_Y = 10000;
  public static int PLAYER_Z = 0;

  public static int ORIGIN_X = 0;
  public static int ORIGIN_Y = 0;

  private static int MAX_Z = 1;

  private static int DAY_NIGHT_TIME = 0; // 0 = both, 1 = night, 2 = day

  private static int SlowInputItr = 0;
  private static Timer loadingTimer;

  private static Tile MouseTile;
  private static Monster MouseMonster;
  private static TileObject MouseObject;

  public static boolean FixEdges = true;

  public static String AREA_EFFECT_ID = null;
  public static String TRIGGER_TRAP_ID = null;
  public static String TILE_DOOR_ID = null;
  public static String TRIGGER_DOOR_ID = null;

  public static Mode currentMode = Mode.BRUSH;

  private static int BrushSize = 1;

  private static boolean makePassable = true;

  private static int makeAggro = 2;

  // SCREEN DATA

  public static Tile SCREEN_TILES[][];
  public static TileObject SCREEN_OBJECTS[][];
  public static int TILE_HALF_W = 11;
  public static int TILE_HALF_H = 7;

  // GUI
  private static Gui GUI;
  private static MouseCursor Mouse;

  private static AreaEffectMenu AREA_EFFECT_MENU;
  private static GenericIdMenu KEY_ID_MENU;

  private static DungeonMenu DUNGEON_MENU;
  private static TextureMenu TEXTURE_MENU;
  private static MonsterMenu MONSTER_MENU;
  private static ObjectMenu OBJECT_MENU;

  private static BaseMenu activeMenu = null;
  private static RandomDungeon rDungeon = null;

  private static Image miniMap = null;
  private static int miniMapX;
  private static int miniMapY;

  public static boolean Loading = true;
  public static Random randomGenerator = new Random();

  private int helpY;

  private static String loadPng = null;

  public BP_EDITOR() {
    super("Blue Saga Map Editor");
  }

  public void init(GameContainer container) throws SlickException {
    loadingTimer = new Timer();

    // CONNECT TO DB
    try {
      mapDB = new Database("mapDB");
      gameDB = new Database("gameDB");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    mapDB.bootstrapMap();

    GFX = new ImageResource();
    GFX.load();

    if (loadPng != null) {
      MapImporter imp = new MapImporter(loadPng);
      imp.load();
    }

    try (ResultSet editorOptions = mapDB.askDB("select X,Y,Z from editor_option")) {
      if (editorOptions.next()) {
        PLAYER_X = editorOptions.getInt("X");
        PLAYER_Y = editorOptions.getInt("Y");
        PLAYER_Z = editorOptions.getInt("Z");
      }
      else {
        mapDB.updateDB(
          "insert into editor_option (X, Y, Z) values (" + PLAYER_X + "," + PLAYER_Y + "," + PLAYER_Z+")");
      }
      editorOptions.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    Mouse = new MouseCursor();

    FONTS = new Font();

    DUNGEON_MENU = new DungeonMenu(700, 40);
    TEXTURE_MENU = new TextureMenu(600, 40);
    MONSTER_MENU = new MonsterMenu(500, 70);
    OBJECT_MENU  = new ObjectMenu(680, 70);
    AREA_EFFECT_MENU = new AreaEffectMenu(container);
    KEY_ID_MENU = new GenericIdMenu(container, "Enter Key ID");

    // GUI INIT
    GUI = new Gui();
    GUI.init();

    container.setMouseCursor(EditorSettings.clientImagePath + "gui/cursors/cursor_hidden.png", 5, 5);

    MouseTile = null;

    SCREEN_TILES = new Tile[TILE_HALF_W * 2][TILE_HALF_H * 2];
    SCREEN_OBJECTS = new TileObject[TILE_HALF_W * 2][TILE_HALF_H * 2];

    for (int i = 0; i < 22; i++) {
      for (int j = 0; j < 14; j++) {
        SCREEN_TILES[i][j] = new Tile(i, j, 0);
        SCREEN_OBJECTS[i][j] = null;
      }
    }

    loadScreen();

    loading();
  }

  public void loadMiniMap() {

    long t0 = System.currentTimeMillis();
    miniMapX = PLAYER_X;
    miniMapY = PLAYER_Y;
    try (ResultSet mapInfo = mapDB.askDB(
            //      1  2  3     4          5              6
            "select X, Y, Type, max(Name), max(ObjectID), min(ObjectID) from area_tile where Z = "
                + PLAYER_Z
                + " AND X>" + (PLAYER_X - MINI_MAP_SIZE)
                + " AND X<" + (PLAYER_X + MINI_MAP_SIZE)
                + " AND Y>" + (PLAYER_Y - MINI_MAP_SIZE)
                + " AND Y<" + (PLAYER_Y + MINI_MAP_SIZE)
                + " GROUP BY X/2,Y/2")
    ) {
      if (miniMap == null) {
        miniMap = new Image(MINI_MAP_SIZE, MINI_MAP_SIZE, Image.FILTER_NEAREST);
      }
      Graphics g = miniMap.getGraphics();
      g.setBackground(EditColors.CLEAR);
      g.clear();

      while (mapInfo.next()) {
        String obj1 = mapInfo.getString(5);
        String obj2 = mapInfo.getString(6);
        if(obj1.startsWith("tree") || obj2.startsWith("tree")) {
          g.setColor(EditColors.TREES);
        } else if(obj1.contains("rock") || obj2.contains("rock")) {
          g.setColor(EditColors.ROCKS);
        } else if(!"None".equals(obj1) || !"None".equals(obj2)) {
          g.setColor(EditColors.GATHERING);
        }
        else {
          String type = mapInfo.getString(3);
          switch (type) {
          case "water":
            g.setColor(EditColors.WATER);
            break;
          case "shallow":
            g.setColor(EditColors.SHALLOW);
            break;
          case "beach":
          case "sand":
            g.setColor(EditColors.BEACH);
            break;
          case "cliff":
            g.setColor(EditColors.CLIFF);
            break;
          default:
            String name = mapInfo.getString(4);
            if (name.contains("water") || name.contains("stream")) {
              g.setColor(EditColors.SHALLOW);
            }
            else {
              g.setColor(EditColors.MAP_OTHER);
            }
          }
        }
        int x = (mapInfo.getInt(1) - PLAYER_X + MINI_MAP_SIZE) / 2;
        int y = (mapInfo.getInt(2) - PLAYER_Y + MINI_MAP_SIZE) / 2;
        g.fillRect(x, y, 1, 1);
      }
      mapInfo.getStatement().close();
      g.flush();
    } catch (SlickException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    System.out.println("Minimap loaded in " + ((System.currentTimeMillis() - t0)/1000.) + " ms");

  }

  public static void loadScreen() {

    for (int i = 0; i < 22; i++) {
      for (int j = 0; j < 14; j++) {
        SCREEN_TILES[i][j].clear();
        SCREEN_OBJECTS[i][j] = null;
      }
    }

    String objectId;
    try (ResultSet mapInfo = mapDB.askDB(
            "select Id, X, Y, Z, Type, Name, AreaEffectId, Passable, ObjectId, DoorId from area_tile where X >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and X < "
                + (PLAYER_X + TILE_HALF_W)
                + " and Y >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and Y < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and Z = "
                + PLAYER_Z
                + " order by Y asc, X asc, Z asc");
    ) {
      while (mapInfo.next()) {
        int tileX = mapInfo.getInt("X") - (PLAYER_X - TILE_HALF_W);
        int tileY = mapInfo.getInt("Y") - (PLAYER_Y - TILE_HALF_H);
        Tile t = SCREEN_TILES[tileX][tileY];
        t.setId(mapInfo.getString("Id"));

        boolean passable = false;
        if (mapInfo.getInt("Passable") == 1) {
          passable = true;
        }
        t.setType(mapInfo.getString("Type"), mapInfo.getString("Name"));
        t.setPassable(passable);
        t.setZ(mapInfo.getInt("Z"));
        t.setAreaEffectId(mapInfo.getString("AreaEffectId"));

        if (mapInfo.getString("ObjectId") == null) {
          System.out.println("ERROR - Null objectid at "
              + mapInfo.getInt("X") + "," + mapInfo.getInt("Y") + "," + mapInfo.getInt("Z"));
        }
        else if (!mapInfo.getString("ObjectId").equals("None")) {
          TileObject newObject = new TileObject(mapInfo.getString("ObjectId"));
          newObject.setZ(mapInfo.getInt("Z"));
          SCREEN_OBJECTS[tileX][tileY] = newObject;
        }

        String doorId = mapInfo.getString("DoorId");
        if (doorId!=null && !"0".equals(doorId)) {
          try (ResultSet doorInfo = mapDB.askDB(
                  "select GotoX, GotoY, GotoZ, Locked from door where Id = " + doorId);
          ) {
            if (doorInfo.next()) {
              Coords xyz = new Coords(
                  doorInfo.getInt("GotoX"),
                  doorInfo.getInt("GotoY"),
                  doorInfo.getInt("GotoZ"));
              t.setDestCoords(xyz);
              t.setDoorId(doorId);
              String lockId = doorInfo.getString("Locked");
              t.setLockId(lockId);
            }
            doorInfo.getStatement().close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }
      mapInfo.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try (ResultSet doorInfo = mapDB.askDB(
            "select DoorId, X, Y, Z, GotoX, GotoY from door, area_tile where DoorId = door.id and door.GotoX >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and door.GotoX < "
                + (PLAYER_X + TILE_HALF_W)
                + " and door.GotoY >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and door.GotoY < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and door.GotoZ = "
                + PLAYER_Z)
    ) {
      while (doorInfo.next()) {
        String destId = doorInfo.getString("DoorId");
        int tileX = doorInfo.getInt("GotoX") - (PLAYER_X - TILE_HALF_W);
        int tileY = doorInfo.getInt("GotoY") - (PLAYER_Y - TILE_HALF_H);

        Coords xyz = new Coords(
            doorInfo.getInt("X"),
            doorInfo.getInt("Y"),
            doorInfo.getInt("Z"));
        SCREEN_TILES[tileX][tileY].setDoorCoords(xyz);
        SCREEN_TILES[tileX][tileY].setDestId(destId);
      }
      doorInfo.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try (ResultSet containerInfo = mapDB.askDB(
            "select X,Y,Z,Type, items,copper from area_container where X >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and X < "
                + (PLAYER_X + TILE_HALF_W)
                + " and Y >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and Y < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and Z = "
                + PLAYER_Z
                + " order by Y asc, X asc, Z asc")
    ) {
      while (containerInfo.next()) {
        int tileX = containerInfo.getInt("X") - (PLAYER_X - TILE_HALF_W);
        int tileY = containerInfo.getInt("Y") - (PLAYER_Y - TILE_HALF_H);
        int tileZ = containerInfo.getInt("Z") - (PLAYER_Z);
        String containerType = containerInfo.getString("Type");

        TileObject newObject = new TileObject(containerType);
        newObject.setZ(containerInfo.getInt("Z"));

        String items = containerInfo.getString("items");
        String copper = containerInfo.getString("copper");
        if (!"0".equals(copper) && !"".equals(items)) {
          newObject.setNotes(copper + "cc," + items);
        }
        else if (!"0".equals(copper)) {
          newObject.setNotes(copper + "cc");
        }
        else if (!"".equals(items)) {
          newObject.setNotes(items);
        }
        SCREEN_OBJECTS[tileX][tileY] = newObject;
      }
      containerInfo.getStatement().close();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    try (ResultSet monsterInfo = mapDB.askDB(
            "select SpawnX, SpawnY, SpawnZ, CreatureId, Id, AggroType from area_creature where SpawnX >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and SpawnX < "
                + (PLAYER_X + TILE_HALF_W)
                + " and SpawnY >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and SpawnY < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and SpawnZ = "
                + PLAYER_Z
                + " and SpawnCriteria = "
                + DAY_NIGHT_TIME)
    ) {
      while (monsterInfo.next()) {
        int tileX = monsterInfo.getInt("SpawnX") - (PLAYER_X - TILE_HALF_W);
        int tileY = monsterInfo.getInt("SpawnY") - (PLAYER_Y - TILE_HALF_H);
        int tileZ = 0;
        int creatureId = monsterInfo.getInt("CreatureId");
        int npcId = monsterInfo.getInt("Id");
        int aggroType = monsterInfo.getInt("AggroType");

        SCREEN_TILES[tileX][tileY].setOccupant(new Creature(creatureId, 0, 0, npcId, aggroType));
      }
      monsterInfo.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try (ResultSet trapInfo = mapDB.askDB(
            "select Id,TrapId,X,Y,Z from area_trap where X >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and X < "
                + (PLAYER_X + TILE_HALF_W)
                + " and Y >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and Y < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and Z = "
                + PLAYER_Z
                + " order by Y asc, X asc, Z asc")
    ) {
      while (trapInfo.next()) {
        int tileX = trapInfo.getInt("X") - (PLAYER_X - TILE_HALF_W);
        int tileY = trapInfo.getInt("Y") - (PLAYER_Y - TILE_HALF_H);
        int tileZ = 0;

        SCREEN_OBJECTS[tileX][tileY].setTrapId(trapInfo.getString("Id"));
      }
      trapInfo.getStatement().close();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    try (ResultSet triggerInfo = mapDB.askDB(
            "select Id,X,Y,Z,TrapId from trigger where X >= "
                + (PLAYER_X - TILE_HALF_W)
                + " and X < "
                + (PLAYER_X + TILE_HALF_W)
                + " and Y >= "
                + (PLAYER_Y - TILE_HALF_H)
                + " and Y < "
                + (PLAYER_Y + TILE_HALF_H)
                + " and Z = "
                + PLAYER_Z
                + " order by Y asc, X asc, Z asc")
    ) {
      while (triggerInfo.next()) {
        int tileX = triggerInfo.getInt("X") - (PLAYER_X - TILE_HALF_W);
        int tileY = triggerInfo.getInt("Y") - (PLAYER_Y - TILE_HALF_H);
        int tileZ = 0;

        SCREEN_TILES[tileX][tileY].setTriggerId(triggerInfo.getString("Id"));
        SCREEN_TILES[tileX][tileY].setTrapId(triggerInfo.getString("TrapId"));
      }
      triggerInfo.getStatement().close();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
  }

  public static void loading() {
    Loading = true;
    loadingTimer.schedule(
        new TimerTask() {
          public void run() {
            Loading = false;
          }
        },
        50);
  }

  public void update(GameContainer container, int delta) throws SlickException {
    // CHECK IF NEW INFO FROM SERVER HAS COME

    keyLogic(container);
  }

  private static boolean overlap(Image img, int x0, int y0, int w, int h) {
    for (int y = y0 ; y < y0 + h ; y++) {
      for (int x = x0 ; x < x0 + w ; x++) {
        if (!EditColors.CLEAR.equals(img.getColor(x, y))) {
          return true;
        }
      }
    }
    return false;
  }

  private void resetHelp(Graphics g) {
    g.setFont(FONTS.size8);
    helpY = 60;
  }

  private void renderHelp(Graphics g, String str) {
    g.setColor(EditColors.BLACK);
    g.drawString(str, 11, helpY+1);
    g.setColor(EditColors.WHITE);
    g.drawString(str, 10, helpY);
    helpY += 20;
  }

  private void renderMode(Graphics g, String str) {
    g.setFont(FONTS.size12bold);
    g.setColor(EditColors.BLACK);
    g.drawString(str, 106, 13);
    g.setColor(EditColors.WHITE);
    g.drawString(str, 105, 11);
  }

  public void render(GameContainer container, Graphics g) throws SlickException {

    FONTS.loadGlyphs();

    if (PLAYER_Z >= 10) {
      GFX.getSprite("effects/clouds").draw(0, 0);
    } else if (PLAYER_Z >= 0) {
      GFX.getSprite("effects/void").draw(0, 0);
    }

    for (int i = 0; i < 22; i++) {
      for (int j = 0; j < 14; j++) {
        SCREEN_TILES[i][j].draw(g, i * TILE_SIZE, j * TILE_SIZE);
      }
    }

    for (int j = 0; j < 14; j++) {
      for (int i = 0; i < 22; i++) {
        if (SCREEN_OBJECTS[i][j] != null) {
          SCREEN_OBJECTS[i][j].draw(g, i * TILE_SIZE, j * TILE_SIZE);
        }
      }
    }

    for (int i = 0; i < 22; i++) {
      for (int j = 0; j < 14; j++) {
        SCREEN_TILES[i][j].drawOverlay(g, i * TILE_SIZE, j * TILE_SIZE);
      }
    }

    if (DAY_NIGHT_TIME == 1) {
      g.setColor(EditColors.NIGHT);
      g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    } else if (DAY_NIGHT_TIME == 2) {
      g.setColor(EditColors.DAY);
      g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    if (currentMode == Mode.MINI_MAP) {
      g.setColor(EditColors.TRANSPARENT);
      miniMap.draw(20, 20);
      g.drawRect(20, 20, miniMap.getWidth(), miniMap.getHeight());

      resetHelp(g);
      renderHelp(g, "F4,4: Edit Mode");
      renderHelp(g, "F5,5: Small Random Dungeon");
      renderHelp(g, "F6,6: Medium Random Dungeon");
      renderHelp(g, "F7,7: Large Random Dungeon");
      renderHelp(g, "F12,^Q: Quit");

      int playerx = (PLAYER_X - miniMapX + MINI_MAP_SIZE) / 2 - 5 + 20;
      int playery = (PLAYER_Y - miniMapY + MINI_MAP_SIZE) / 2 - 3 + 20;

      if (rDungeon!=null) {
        if (overlap(miniMap, playerx-10-20, playery-7-20, rDungeon.width/2+20, rDungeon.height/2+14)) {
          g.setColor(EditColors.ORANGE);
          activeMenu.setReady(false);
          activeMenu.setTitle("Random Dungeon: Change Position");
        } else {
          g.setColor(EditColors.GREEN);
          activeMenu.setReady(true);
          activeMenu.setTitle("Random Dungeon: Select Type");
        }
        g.drawRect(playerx, playery, rDungeon.width/2, rDungeon.height/2);
        g.drawRect(playerx-10, playery-7, rDungeon.width/2+20, rDungeon.height/2+14);

        activeMenu.draw(g, container, INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
      }
      else {
        g.setColor(EditColors.RED);
        g.drawRect(playerx, playery, 9, 5);
      }
      if (INPUT != null) {
        Mouse.draw(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
      }
    } else {
      resetHelp(g);
      renderHelp(g, "F12,^Q: Quit");
      renderHelp(g, "B: Background Tiles menu");
      renderHelp(g, "F1,1: Hide/View Passable");
      renderHelp(g, "F2,2: Transparent Objects");
      renderHelp(g, "F4,4: Mini Map");
      renderHelp(g, "[/]: Brushsize -/+ " + BrushSize);
      renderHelp(g, "M: Monsters menu");
      renderHelp(g, "O: Objects menu");
      renderHelp(g, "P: Toggle Passable");
      renderHelp(g, "Y: Toggle AggroType");
      renderHelp(g, "R: Place Door");
      renderHelp(g, "T: Place Effect");
      renderHelp(g, "Del,Backspace: Clear Area");
      renderHelp(g, "Home,H: Go to start X,Y,Z");
      renderHelp(g, "LClick,Space: apply, paste");
      renderHelp(g, "Rclick/Return: copy, follow");

      String willEdges = (FixEdges
          && currentMode == Mode.BRUSH
          && MouseTile!=null
          && canFixEdges(MouseTile.getName()))
          ? "will do" : "can't do";

      renderHelp(g, "F: Fix edges = " + FixEdges + " (" + willEdges + ")");
      renderHelp(g, "N: Spawns = "
          + ((DAY_NIGHT_TIME==0) ? "night+day" : (DAY_NIGHT_TIME==1) ? "night" : "day"));
      renderHelp(g, "PgUp/PgDown,E/Q: Z +/-");

      switch (currentMode) {
      case CLEAR:
        renderMode(g, "MODE: Clear Area");
        break;
      case EFFECT:
        renderMode(g, "MODE: Place Effect " + AREA_EFFECT_ID);
        break;
      case TRAP:
        renderMode(g, "MODE: Place Trap " + TRIGGER_TRAP_ID);
        break;
      case TRIGGER:
        renderMode(g, "MODE: Place Trigger for Trap " + TRIGGER_TRAP_ID);
        break;
      case DOOR:
        renderMode(g, "MODE: Place Door");
        break;
      case DESTINATION:
        if (TRIGGER_DOOR_ID==null) {
          renderMode(g, "MODE: Place Destination for new Door");
        } else {
          renderMode(g, "MODE: Change Destination for Door " + TRIGGER_DOOR_ID);
        }
        break;
      case KEY:
        renderMode(g, "MODE: Enter Key ID");
        break;
      case PASSABLE:
        renderMode(g, "MODE: Toggle Passable - " + makePassable);
        break;
      case AGGRO:
        renderMode(g, "MODE: Toggle Aggro - " + Creature.getAggroTypeStr(makeAggro));
        break;
      case BRUSH:
        renderMode(g, "MODE: Brush");
        break;
      case DELETE_TILE:
        renderMode(g, "MODE: Delete Background Tile");
        break;
      case DELETE_OBJECT:
        renderMode(g, "MODE: Delete Object");
        break;
      case DELETE_MONSTER:
        renderMode(g, "MODE: Delete NPC");
        break;
      case MINI_MAP:
        renderMode(g, "MODE: Mini Map");
        break;
      default:
        renderMode(g, "MODE? " + currentMode);
      }

      if (INPUT != null) {

        int screenX = (int) Math.floor(INPUT.getAbsoluteMouseX() / TILE_SIZE);
        int screenY = (int) Math.floor(INPUT.getAbsoluteMouseY() / TILE_SIZE);

        int tileX = screenX + PLAYER_X - TILE_HALF_W;
        int tileY = screenY + PLAYER_Y - TILE_HALF_H;

        switch (currentMode) {
        case CLEAR:
        case DELETE_MONSTER:
          g.setColor(EditColors.WHITE);
          g.drawRect(screenX * 50, screenY * 50, 50, 50);
          g.drawLine(screenX * 50, screenY * 50, screenX * 50 + 50, screenY * 50 + 50);
          g.drawLine(screenX * 50 + 50, screenY * 50, screenX * 50, screenY * 50 + 50);
          break;
        case DELETE_TILE:
        case DELETE_OBJECT:
          g.setColor(EditColors.WHITE);
          g.drawRect(screenX * 50, screenY * 50, 50 * BrushSize, 50 * BrushSize);
          g.drawLine(screenX * 50, screenY * 50, screenX * 50 + 50 * BrushSize, screenY * 50 + 50 * BrushSize);
          g.drawLine(screenX * 50 + 50 * BrushSize, screenY * 50, screenX * 50, screenY * 50 + 50 * BrushSize);
          break;
        case EFFECT:
          g.setColor(EditColors.AREA_EFFECT);
          g.drawRect(screenX * 50, screenY * 50, 50, 50);
          g.setFont(FONTS.size12bold);
          g.drawString(AREA_EFFECT_ID, screenX * 50 + 12, screenY * 50 + 15);
          break;
        case TRAP:
          g.setColor(EditColors.RED);
          g.drawRect(screenX * 50, screenY * 50, 50, 50);
          break;
        case TRIGGER:
          g.setColor(EditColors.TRIGGER);
          g.drawRect(screenX * 50, screenY * 50, 50, 50);
          g.setFont(FONTS.size12bold);
          g.drawString(TRIGGER_TRAP_ID, screenX * 50 + 12, screenY * 50 + 15);
          break;
        case DOOR:
          g.setColor(EditColors.DOOR_BG);
          g.fillRect(screenX * 50, screenY * 50, 50, 50);
          break;
        case DESTINATION:
          g.setColor(EditColors.DEST_BG);
          g.fillRect(screenX * 50, screenY * 50, 50, 50);
          g.setFont(FONTS.size12bold);
          g.drawString((TRIGGER_DOOR_ID==null ? "New" : TRIGGER_DOOR_ID),
              screenX * 50 + 12, screenY * 50 + 15);
          break;
        case PASSABLE:
          if (makePassable) {
            g.setColor(EditColors.TRANSPARENT);
          } else {
            g.setColor(EditColors.IMPASSABLE);
          }
          g.fillRect(screenX * 50, screenY * 50, 50 * BrushSize, 50 * BrushSize);
          break;
        default: // Brush
          if (activeMenu == null || activeMenu.drawMouseTile()) {
            if (MouseTile != null) {
              MouseTile.draw(g, screenX * 50, screenY * 50);
            }
            if (MouseMonster != null) {
              MouseMonster.draw(g, screenX * 50, screenY * 50);
            }
            if (MouseObject != null) {
              MouseObject.draw(g, screenX * 50, screenY * 50);
            }
            g.setColor(EditColors.WHITE);
            g.drawRect(screenX * 50, screenY * 50, 50 * BrushSize, 50 * BrushSize);
          }
        }

        if (!Loading && activeMenu != null) {
          activeMenu.draw(g, container, INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
        }

        g.setFont(FONTS.size8);
        String coords = tileX + "," + tileY + "," + PLAYER_Z;
        String tileStr = SCREEN_TILES[screenX][screenY].getType() + "/" + SCREEN_TILES[screenX][screenY].getName();

        String occStr = null;
        if (SCREEN_TILES[screenX][screenY].getOccupant() != null) {
          occStr = "Npc: " + String.valueOf(SCREEN_TILES[screenX][screenY].getOccupant());
        }
        else if (SCREEN_OBJECTS[screenX][screenY] != null) {
          occStr = "Object: " + String.valueOf(SCREEN_OBJECTS[screenX][screenY]);
        }

        g.setColor(EditColors.BLACK);
        g.drawString(coords, 106, 31);
        g.drawString(tileStr, 106, 43);
        if (occStr!=null) {
          g.drawString(occStr,  106, 55);
        }

        g.setColor(EditColors.WHITE);
        g.drawString(coords, 105, 30);
        g.drawString(tileStr, 105, 42);
        if (occStr!=null) {
          g.drawString(occStr,  105, 54);
        }

        Mouse.draw(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
      }
    }
  }

  /*
   *
   *   KEYBOARD & MOUSE
   *
   */

  private void keyLogic(GameContainer GC) {
    INPUT = GC.getInput();
    boolean isQ = INPUT.isKeyPressed(Input.KEY_Q);
    if (INPUT.isKeyPressed(Input.KEY_F12)
    || (isQ && INPUT.isKeyDown(Input.KEY_LCONTROL))
    ) {
      GC.exit();
    }

    if (activeMenu != null) {
      boolean keepOpen = activeMenu.keyLogic(INPUT);
      if (!keepOpen) {
        activeMenu = null;
        rDungeon = null;

        if (currentMode == Mode.KEY) {
          String menuValue = KEY_ID_MENU.getValue();
          if (menuValue!=null) {
            mapDB.createDoor(TILE_DOOR_ID, TRIGGER_DOOR_ID, menuValue);
            TILE_DOOR_ID = null;
            TRIGGER_DOOR_ID = null;
            currentMode = Mode.BRUSH;
            loadScreen();
          }
        }
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_ESCAPE) || INPUT.isKeyPressed(Input.KEY_Z)) {
      currentMode = Mode.BRUSH;
      MouseMonster = null;
      MouseObject = null;
      MouseTile = null;
      loadScreen();
    }

    if (INPUT.isKeyPressed(Input.KEY_HOME) || INPUT.isKeyPressed(Input.KEY_H)) {
      System.out.println("KEY=Home z" + PLAYER_Z);
      PLAYER_X = EditorSettings.startX;
      PLAYER_Y = EditorSettings.startY;
      PLAYER_Z = EditorSettings.startZ;
      if (currentMode == Mode.MINI_MAP) {
        ORIGIN_X = PLAYER_X;
        ORIGIN_Y = PLAYER_Y;
        loadMiniMap();
      }
      loadScreen();
    }

    if (SlowInputItr == 3) {
      SlowInputItr = 0;

      if (INPUT.isKeyDown(Input.KEY_A) || INPUT.isKeyDown(Input.KEY_LEFT)) {
        PLAYER_X--;
        loadScreen();
      } else if (INPUT.isKeyDown(Input.KEY_D) || INPUT.isKeyDown(Input.KEY_RIGHT)) {
        PLAYER_X++;
        loadScreen();
      } else if (INPUT.isKeyDown(Input.KEY_W) || INPUT.isKeyDown(Input.KEY_UP)) {
        PLAYER_Y--;
        loadScreen();
      } else if (INPUT.isKeyDown(Input.KEY_S) || INPUT.isKeyDown(Input.KEY_DOWN)) {
        PLAYER_Y++;
        loadScreen();
      }
    }

    SlowInputItr++;

    if (INPUT.isKeyPressed(Input.KEY_PRIOR) || INPUT.isKeyPressed(Input.KEY_E)) {
      PLAYER_Z++;
      if (currentMode == Mode.MINI_MAP) {
        loadMiniMap();
      }
      loadScreen();
    } else if (INPUT.isKeyPressed(Input.KEY_NEXT) || isQ) {
      PLAYER_Z--;
      if (currentMode == Mode.MINI_MAP) {
        loadMiniMap();
      }
      loadScreen();
    }

    if (INPUT.isKeyPressed(Input.KEY_F1) || INPUT.isKeyPressed(Input.KEY_1)) {
      ++ SHOW_PASSABLE;
      if (SHOW_PASSABLE>2) {
        SHOW_PASSABLE = 0;
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_F2) || INPUT.isKeyPressed(Input.KEY_2)) {
      TileObject.transparent = !TileObject.transparent;
    }

    if (INPUT.isKeyPressed(Input.KEY_F4) || INPUT.isKeyPressed(Input.KEY_4)) {
      if (currentMode != Mode.MINI_MAP) {
        ORIGIN_X = PLAYER_X;
        ORIGIN_Y = PLAYER_Y;
        loadMiniMap();
        currentMode = Mode.MINI_MAP;
        activeMenu = null;
      } else {
        currentMode = Mode.BRUSH;
        rDungeon = null;
      }
    }
    if (PLAYER_Z<0 && currentMode == Mode.MINI_MAP) {
      int sz_min = 0;
      if (INPUT.isKeyPressed(Input.KEY_F5) || INPUT.isKeyPressed(Input.KEY_5)) {
        sz_min = 256;
      }
      else if (INPUT.isKeyPressed(Input.KEY_F6) || INPUT.isKeyPressed(Input.KEY_6)) {
        sz_min = 512;
      }
      else if (INPUT.isKeyPressed(Input.KEY_F7) || INPUT.isKeyPressed(Input.KEY_7)) {
        sz_min = 768;
      }
      if (sz_min>0) {
        rDungeon = RandomDungeon.generate(RandomUtils.getInt(sz_min, sz_min + 256));
        System.out.println("Positionning " + rDungeon);
        activeMenu = DUNGEON_MENU;
        activeMenu.clear();
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_LBRACKET)
    || INPUT.isKeyPressed(Input.KEY_MINUS)
    || INPUT.isKeyPressed(Input.KEY_UNDERLINE)) {
      if (BrushSize>1) {
        BrushSize--;
      }
    }
    if (INPUT.isKeyPressed(Input.KEY_RBRACKET)
    || INPUT.isKeyPressed(Input.KEY_EQUALS)
    || INPUT.isKeyPressed(Input.KEY_ADD)) {
      if (BrushSize<8) {
        BrushSize++;
      }
    }

    if (activeMenu == null) {
      if (INPUT.isKeyPressed(Input.KEY_F)) {
        if (FixEdges) {
          FixEdges = false;
        } else {
          FixEdges = true;
        }
      }

      if (INPUT.isKeyPressed(Input.KEY_O)) {
        activeMenu = OBJECT_MENU;
        activeMenu.clear();
      }
      if (INPUT.isKeyPressed(Input.KEY_M)) {
        activeMenu = MONSTER_MENU;
        BrushSize = 1;
        activeMenu.clear();
      }
      if (INPUT.isKeyPressed(Input.KEY_B)) {
        activeMenu = TEXTURE_MENU;
        activeMenu.clear();
      }
      if (INPUT.isKeyPressed(Input.KEY_T)) {
        activeMenu = AREA_EFFECT_MENU;
        activeMenu.clear();
      }
      if (INPUT.isKeyPressed(Input.KEY_K) && currentMode == Mode.DESTINATION) {
        currentMode = Mode.KEY;
        activeMenu = KEY_ID_MENU;
        activeMenu.clear();
      }
      if (INPUT.isKeyPressed(Input.KEY_R)) { // Door
        activeMenu = null;
        currentMode = Mode.DOOR;
      }

      if (INPUT.isKeyPressed(Input.KEY_P)) {
        activeMenu = null;

        if (currentMode == Mode.PASSABLE) {
          makePassable = !makePassable;
        }
        else {
          currentMode = Mode.PASSABLE;
        }
      }

      if (INPUT.isKeyPressed(Input.KEY_N)) {
        DAY_NIGHT_TIME++;
        if (DAY_NIGHT_TIME > 2) {
          DAY_NIGHT_TIME = 0;
        }
        loadScreen();
      }

      if (INPUT.isKeyPressed(Input.KEY_Y)) {
        activeMenu = null;

        if (currentMode == Mode.AGGRO) {
          ++ makeAggro;
          if (makeAggro == 8) {
            makeAggro = 0;
          }
        }
        else {
          currentMode = Mode.AGGRO;
        }
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_DELETE) || INPUT.isKeyPressed(Input.KEY_BACK)) {
      if (currentMode==Mode.DELETE_MONSTER
      ||  currentMode==Mode.DELETE_OBJECT
      ||  currentMode==Mode.DELETE_TILE
      ) {
        currentMode = Mode.BRUSH;
      }
      else if (MouseMonster != null) {
        currentMode = Mode.DELETE_MONSTER;
      }
      else if (MouseObject != null) {
        currentMode = Mode.DELETE_OBJECT;
      }
      else if (MouseTile != null) {
        currentMode = Mode.DELETE_TILE;
      }
      else {
        currentMode = Mode.CLEAR;
      }
    }

    if (INPUT.isMousePressed(Input.MOUSE_LEFT_BUTTON) || INPUT.isKeyPressed(Input.KEY_SPACE)) {

      int mouseX = INPUT.getAbsoluteMouseX();
      int mouseY = INPUT.getAbsoluteMouseY();

      int screenX = (int) Math.floor(mouseX / TILE_SIZE);
      int screenY = (int) Math.floor(mouseY / TILE_SIZE);

      int tileX = screenX + PLAYER_X - TILE_HALF_W;
      int tileY = screenY + PLAYER_Y - TILE_HALF_H;

      if (currentMode == Mode.MINI_MAP) {
        String selectedType = null;
        if (activeMenu == DUNGEON_MENU) {
          selectedType = DUNGEON_MENU.click(mouseX, mouseY);
        }
        if (selectedType!=null) {
          if (rDungeon !=null && activeMenu.isReady()) {
            int px = PLAYER_X - rDungeon.offset_x;
            int py = PLAYER_Y - rDungeon.offset_y;
            System.out.println("Saving RandomDungeon " + selectedType + ": "
                + px + "," + py + "," + PLAYER_Z);
            rDungeon.save(selectedType, px, py, PLAYER_Z);
            PLAYER_X = ORIGIN_X;
            PLAYER_Y = ORIGIN_Y;
            loadMiniMap();
            PLAYER_X = px + rDungeon.offset_x;
            PLAYER_Y = py + rDungeon.offset_y;
            activeMenu = null;
            rDungeon = null;
          }
        } else if (mouseX>30
        && mouseX<MINI_MAP_SIZE+25
        && mouseY>29
        && mouseY<MINI_MAP_SIZE+29
        ) {
          PLAYER_X = mouseX * 2 + miniMapX - MINI_MAP_SIZE - 60 + 9;
          PLAYER_Y = mouseY * 2 + miniMapY - MINI_MAP_SIZE - 60 + 5;
        }
        loadScreen();
        INPUT.clearKeyPressedRecord();
        return;

      } else if (activeMenu == TEXTURE_MENU) {
        int clickButtonIndex = TEXTURE_MENU.click(mouseX, mouseY, mapDB);
        if (clickButtonIndex < 998) {
          currentMode = Mode.BRUSH;
          MouseTile = new Tile(0, 0, 0);
          MouseMonster = null;
          MouseObject = null;
          MouseTile.setType(
              TEXTURE_MENU.getTile(clickButtonIndex).getType(),
              TEXTURE_MENU.getTile(clickButtonIndex).getName());
          INPUT.clearKeyPressedRecord();
          System.out.println("BRUSH="+MouseTile);
          return;
        } else if (clickButtonIndex == 998) {
          currentMode = Mode.DELETE_TILE;
          MouseTile = null;
          MouseMonster = null;
          MouseObject = null;
          INPUT.clearKeyPressedRecord();
          return;
        } else if (clickButtonIndex < 1000) {
          INPUT.clearKeyPressedRecord();
          return;
        }
      } else if (activeMenu == MONSTER_MENU) {
        int clickButtonIndex = MONSTER_MENU.click(mouseX, mouseY);
        if (clickButtonIndex < 999) {
          currentMode = Mode.BRUSH;
          MouseMonster =
              new Monster(MONSTER_MENU.getMonster(clickButtonIndex).getId(), 0, 0, "no");
          MouseTile = null;
          MouseObject = null;
          System.out.println("BRUSH="+MouseMonster);
          INPUT.clearKeyPressedRecord();
          return;
        } else if (clickButtonIndex == 999) {
          currentMode = Mode.DELETE_MONSTER;
          MouseTile = null;
          MouseMonster = null;
          MouseObject = null;
          INPUT.clearKeyPressedRecord();
          return;
        } else if (clickButtonIndex < 1000) {
          INPUT.clearKeyPressedRecord();
          return;
        }
      } else if (activeMenu == OBJECT_MENU) {
        int clickButtonIndex = OBJECT_MENU.click(mouseX, mouseY, mapDB);
        if (clickButtonIndex < 999) {
          currentMode = Mode.BRUSH;
          MouseObject = OBJECT_MENU.getClickedTileObject(mouseX, mouseY);
          MouseTile = null;
          MouseMonster = null;
          INPUT.clearKeyPressedRecord();
          System.out.println("BRUSH="+MouseObject);
          return;
        } else if (clickButtonIndex == 999) {
          currentMode = Mode.DELETE_OBJECT;
          MouseTile = null;
          MouseMonster = null;
          MouseObject = null;
          INPUT.clearKeyPressedRecord();
          return;
        } else if (clickButtonIndex < 1000) {
          INPUT.clearKeyPressedRecord();
          return;
        }
      }

      // PLACE TILE, MONSTER OR CONTAINER
      switch (currentMode) {

      case CLEAR:
        mapDB.updateDB("BEGIN TRANSACTION");
        mapDB.updateDB(
            "delete from trigger where X="
                + tileX
                + " and Y="
                + tileY
                + " and Z="
                + PLAYER_Z);
        mapDB.updateDB(
            "delete from door where Id = "
                + SCREEN_TILES[screenX][screenY].getDestId());
        mapDB.updateDB(
            "delete from door where Id = "
                + SCREEN_TILES[screenX][screenY].getDoorId());
        mapDB.updateDB(
            "update area_tile set AreaEffectId = 0 where X = "
                + tileX
                + " and Y = "
                + tileY
                + " and Z = "
                + PLAYER_Z);
        mapDB.updateDB(
            "update area_tile set DoorId = 0 where DoorId = "
                + SCREEN_TILES[screenX][screenY].getDestId());
        mapDB.updateDB(
            "update area_tile set DoorId = 0 where DoorId = "
                + SCREEN_TILES[screenX][screenY].getDoorId());
        mapDB.updateDB("END TRANSACTION");
        loadScreen();
        break;

      case EFFECT:
        mapDB.updateDB(
            "update area_tile set AreaEffectId = "
                + AREA_EFFECT_ID
                + " where X = "
                + tileX
                + " and Y = "
                + tileY
                + " and Z = "
                + PLAYER_Z);

        SCREEN_TILES[screenX][screenY].setAreaEffectId(AREA_EFFECT_ID);
        break;

      case DOOR:
        currentMode = Mode.DESTINATION;
        TILE_DOOR_ID = SCREEN_TILES[screenX][screenY].getId();
        TRIGGER_DOOR_ID = SCREEN_TILES[screenX][screenY].getDoorId();
        if (TRIGGER_DOOR_ID==null) {
          SCREEN_TILES[screenX][screenY].setDoorId("New");
        }
        break;

      case DESTINATION:
        mapDB.createDoor(TILE_DOOR_ID, TRIGGER_DOOR_ID, tileX, tileY, PLAYER_Z);
        TILE_DOOR_ID = null;
        TRIGGER_DOOR_ID = null;
        currentMode = Mode.BRUSH;
        loadScreen();
        break;

      case PASSABLE:
        SCREEN_TILES[screenX][screenY].setPassable(makePassable);
        mapDB.updateDB(
            "update area_tile set Passable = "
                + (makePassable ? 1 : 0)
                + " where X >= "
                + (screenX + PLAYER_X - TILE_HALF_W)
                + " and X < "
                + (screenX + PLAYER_X - TILE_HALF_W + BrushSize)
                + " and Y >= "
                + (screenY + PLAYER_Y - TILE_HALF_H)
                + " and Y < "
                + (screenY + PLAYER_Y - TILE_HALF_H + BrushSize)
                + " and Z = "
                + PLAYER_Z);
        loadScreen();
        break;

      case AGGRO:
        Creature c = SCREEN_TILES[screenX][screenY].getOccupant();
        if (c!=null) {
          c.setAggroType(makeAggro);
          mapDB.updateDB(
              "update area_creature set AggroType = "
                  + makeAggro
                  + " where SpawnX = "
                  + tileX
                  + " and SpawnY = "
                  + tileY
                  + " and SpawnZ = "
                  + PLAYER_Z);
        }
        break;

      case TRIGGER:
        mapDB.updateDB(
            "insert into trigger (X,Y,Z,TrapId,DoorId) values ("
                + tileX
                + ","
                + tileY
                + ","
                + PLAYER_Z
                + ","
                + TRIGGER_TRAP_ID
                + ",0)");
        loadScreen();
        break;

      case DELETE_MONSTER:
        mapDB.updateDB(
            "delete from area_creature where SpawnX = "
                + tileX
                + " and SpawnY = "
                + tileY
                + " and SpawnZ = "
                + PLAYER_Z);
        loadScreen();
        break;

      case DELETE_OBJECT:
        mapDB.updateDB("BEGIN TRANSACTION");
        try {
          for (int i = screenX; i < screenX + BrushSize; i++) {
            for (int j = screenY; j < screenY + BrushSize; j++) {
              tileX = i + PLAYER_X - TILE_HALF_W;
              tileY = j + PLAYER_Y - TILE_HALF_H;

              mapDB.updateDB(
                  "update area_tile set ObjectId = 'None', Passable = 1 where X = "
                      + tileX
                      + " and Y = "
                      + tileY
                      + " and Z = "
                      + PLAYER_Z);
              mapDB.updateDB(
                  "delete from area_container where X = "
                      + tileX
                      + " and Y = "
                      + tileY
                      + " and Z = "
                      + PLAYER_Z);
              mapDB.updateDB(
                  "delete from area_trap where X = "
                      + tileX
                      + " and Y = "
                      + tileY
                      + " and Z = "
                      + PLAYER_Z);
            }
          }
          mapDB.updateDB("END TRANSACTION");
        } catch (Exception ex) {
          mapDB.updateDB("ROLLBACK TRANSACTION");
        }
        loadScreen();
        break;

      case DELETE_TILE:
        mapDB.updateDB("BEGIN TRANSACTION");
        try {
          for (int i = screenX; i < screenX + BrushSize; i++) {
            for (int j = screenY; j < screenY + BrushSize; j++) {
              tileX = i + PLAYER_X - TILE_HALF_W;
              tileY = j + PLAYER_Y - TILE_HALF_H;

              if (SCREEN_TILES[i][j].getDoorId() != null) {
                mapDB.updateDB(
                    "delete from door where Id = " + SCREEN_TILES[i][j].getDoorId());
              }

              mapDB.updateDB(
                  "delete from area_tile where X = "
                      + tileX
                      + " and Y = "
                      + tileY
                      + " and Z = "
                      + PLAYER_Z);
            }
          }
          mapDB.updateDB("END TRANSACTION");
        } catch (Exception ex) {
          mapDB.updateDB("ROLLBACK TRANSACTION");
        }
        loadScreen();
        break;

      case BRUSH:
        if (MouseTile != null) {
          addTiles(screenX, screenY);
        } else if (MouseObject != null) {
          if (!SCREEN_TILES[screenX][screenY].getType().equals("None")) {

            // PLACE CONTAINER
            if (MouseObject.getName().contains("container")) {
              mapDB.updateDB(
                  "insert into area_container (X,Y,Z,Type,Items,Fixed) values ("
                      + tileX
                      + ","
                      + tileY
                      + ","
                      + PLAYER_Z
                      + ",'"
                      + MouseObject.getName()
                      + "','',1)");
              mapDB.updateDB(
                  "update area_tile set Passable = 0 where X = "
                      + tileX
                      + " and Y = "
                      + tileY
                      + " and Z = "
                      + PLAYER_Z);
              TileObject newObject = new TileObject(MouseObject.getName());
              newObject.setZ(PLAYER_Z);
              SCREEN_OBJECTS[screenX][screenY] = newObject;
              SCREEN_TILES[screenX][screenY].setPassable(false);
            } else if (MouseObject.getName().contains("moveable")) {
              mapDB.updateDB(
                  "update area_tile set ObjectId = '"
                      + MouseObject.getName()
                      + "' where X = "
                      + tileX
                      + " and Y = "
                      + tileY
                      + " and Z = "
                      + PLAYER_Z);
              TileObject newObject = new TileObject(MouseObject.getName());
              newObject.setZ(PLAYER_Z);
              SCREEN_OBJECTS[screenX][screenY] = newObject;

            } else {
              // PLACE OBJECT OVER AREA
              int passable = 0;
              if (MouseObject.getName().startsWith("gathering/skarrot")
              || MouseObject.getName().startsWith("gathering/gronion")
              || MouseObject.getName().startsWith("gathering/topato")
              || MouseObject.getName().startsWith("gathering/Tripfle")
              || MouseObject.getName().startsWith("gathering/Spongeroot")
              || MouseObject.getName().startsWith("gathering/herb")
              ) {
                passable = 1;
              }

              mapDB.updateDB("BEGIN TRANSACTION");
              try {
                for (int i = screenX; i < screenX + BrushSize; i += 2) {
                  for (int j = screenY; j < screenY + BrushSize; j += 2) {
                    tileX = i + PLAYER_X - TILE_HALF_W;
                    tileY = j + PLAYER_Y - TILE_HALF_H;

                    mapDB.updateDB(
                        "update area_tile set ObjectId = '"
                            + MouseObject.getName()
                            + "', Passable = "
                            + passable
                            + " where X = "
                            + tileX
                            + " and Y = "
                            + tileY
                            + " and Z = "
                            + PLAYER_Z);

                    if (BrushSize > 1) {
                      mapDB.updateDB(
                          "update area_tile set ObjectId = '"
                              + MouseObject.getName()
                              + "', Passable = 0 where X = "
                              + (tileX + 1)
                              + " and Y = "
                              + (tileY + 1)
                              + " and Z = "
                              + PLAYER_Z);
                    }

                    if (MouseObject.getName().contains("trap")) {
                      MouseObject.getName().split("_");
                      mapDB.updateDB(
                          "update area_tile set Passable = 1 where X = "
                              + tileX
                              + " and Y = "
                              + tileY
                              + " and Z = "
                              + PLAYER_Z);
                      mapDB.updateDB(
                          "insert into area_trap (TrapId,X,Y,Z) values (1,"
                              + tileX
                              + ","
                              + tileY
                              + ","
                              + PLAYER_Z
                              + ")");
                    }
                  }
                }
                mapDB.updateDB("END TRANSACTION");
              } catch (Exception ex) {
                mapDB.updateDB("ROLLBACK TRANSACTION");
              }

              if (BrushSize > 2) {
                // MAKE ALL TILES IN BETWEEN OBJECTS NOT PASSABLE
                tileX = screenX + PLAYER_X - TILE_HALF_W;
                tileY = screenY + PLAYER_Y - TILE_HALF_H;
                mapDB.updateDB(
                    "update area_tile set Passable = 0 where X > "
                        + tileX
                        + " and X < "
                        + (tileX + BrushSize)
                        + " and Y > "
                        + (tileY)
                        + " and Y < "
                        + (tileY + BrushSize)
                        + " and Z = "
                        + PLAYER_Z);
              }
              loadScreen();
            }
          }

        } else if (MouseMonster != null) {

          mapDB.updateDB(
              "delete from area_creature where SpawnX = "
                  + tileX
                  + " and SpawnY = "
                  + tileY
                  + " and SpawnZ = "
                  + PLAYER_Z);
          mapDB.updateDB(
              "insert into area_creature (AreaId, CreatureId, MobLevel, SpawnX, SpawnY, SpawnZ, AggroType, Name, SpawnCriteria) values (0,"
                  + MouseMonster.getId()
                  + ","
                  + MouseMonster.getMobLevel()
                  + ","
                  + tileX
                  + ","
                  + tileY
                  + ","
                  + PLAYER_Z
                  + ",2,'',"
                  + DAY_NIGHT_TIME
                  + ")");
          loadScreen();
        }
      }
    }

    if (INPUT.isMousePressed(Input.MOUSE_RIGHT_BUTTON)
    || INPUT.isKeyPressed(Input.KEY_ENTER)
    || INPUT.isKeyPressed(Input.KEY_NUMPADENTER)
    || INPUT.isKeyPressed(Input.KEY_RETURN)
    ) {
      int mouseX = INPUT.getAbsoluteMouseX();
      int mouseY = INPUT.getAbsoluteMouseY();

      int screenX = (int) Math.floor(mouseX / TILE_SIZE);
      int screenY = (int) Math.floor(mouseY / TILE_SIZE);

      if (SCREEN_TILES[screenX][screenY] != null) {
        if (SCREEN_TILES[screenX][screenY].getDoorCoords() != null) {
          Coords xyz = SCREEN_TILES[screenX][screenY].getDoorCoords();
          PLAYER_X = xyz.x;
          PLAYER_Y = xyz.y;
          PLAYER_Z = xyz.z;
          loadScreen();
        }
        else if (SCREEN_TILES[screenX][screenY].getDestCoords() != null) {
          Coords xyz = SCREEN_TILES[screenX][screenY].getDestCoords();
          PLAYER_X = xyz.x;
          PLAYER_Y = xyz.y;
          PLAYER_Z = xyz.z;
          loadScreen();
        }
        else if (SCREEN_TILES[screenX][screenY].getOccupant() != null) {
          int creatureId = SCREEN_TILES[screenX][screenY].getOccupant().getId();
          MouseMonster = new Monster(creatureId, 0, 0, "no");
          MouseObject = null;
          MouseTile = null;
          currentMode = Mode.BRUSH;
          System.out.println("BRUSH="+MouseMonster);
        }
        else if (SCREEN_TILES[screenX][screenY].getAreaEffectId() != null) {
          currentMode = Mode.EFFECT;
          AREA_EFFECT_ID = SCREEN_TILES[screenX][screenY].getAreaEffectId();
        }
        else if (SCREEN_TILES[screenX][screenY].getTrapId() != null) {
          currentMode = Mode.TRIGGER;
          TRIGGER_TRAP_ID = SCREEN_TILES[screenX][screenY].getTrapId();
        }
        else if (SCREEN_OBJECTS[screenX][screenY] != null) {
          if (SCREEN_OBJECTS[screenX][screenY].getTrapId() != null) {
            currentMode = Mode.TRIGGER;
            TRIGGER_TRAP_ID = SCREEN_OBJECTS[screenX][screenY].getTrapId();
          } else {
            MouseMonster = null;
            MouseObject = SCREEN_OBJECTS[screenX][screenY];
            MouseTile = null;
            currentMode = Mode.BRUSH;
          System.out.println("BRUSH="+MouseObject);
          }
        }
        else {
          MouseTile = new Tile(0, 0, 0);
          MouseTile.setType(
              SCREEN_TILES[screenX][screenY].getType(),
              SCREEN_TILES[screenX][screenY].getName());
          currentMode = Mode.BRUSH;
          System.out.println("BRUSH="+MouseTile);
        }
      }
      else {
        MouseMonster = null;
        MouseObject = null;
        MouseTile = null;
        currentMode = Mode.BRUSH;
      }
    }

    INPUT.clearKeyPressedRecord();
  }

  public static boolean isEdge(int screenX, int screenY, int screenZ) {
    if (SCREEN_TILES[screenX][screenY].getName().contains("D")
        || SCREEN_TILES[screenX][screenY].getName().contains("U")
        || SCREEN_TILES[screenX][screenY].getName().contains("L")
        || SCREEN_TILES[screenX][screenY].getName().contains("R")) {
      return true;
    }
    return false;
  }

  public static boolean canFixEdges(String tileName) {
    return ((tileName.contains("D") || tileName.contains("U") || tileName.contains("L") || tileName.contains("R"))
        && !tileName.contains("Stairs")
        && !tileName.contains("Entrance")
        && !tileName.contains("Exit")
        && !tileName.contains("wall"));
  }

  public static void addTiles(int screenX, int screenY) {

    String tileName = MouseTile.getName();

    if (FixEdges && BrushSize==1 && canFixEdges(tileName)
    && screenX>0 && screenY>0 && screenX<(TILE_HALF_W * 2) && screenY<(TILE_HALF_H * 2)
    ) {
      int lastChar = EdgeHelper.suffixPosition(tileName);
      String otherType = tileName.substring(0, lastChar);
      String suffix = tileName.substring(lastChar);
      Tile thisTile = SCREEN_TILES[screenX][screenY];
      if (thisTile != null) {
        String thisName = thisTile.getName();
        lastChar = EdgeHelper.suffixPosition(thisName);
        suffix = thisName.substring(lastChar);
      }

      String tileType = MouseTile.getType();

      String possible = EdgeHelper.complete(
          SCREEN_TILES[screenX-1][screenY],  // onLeft
          SCREEN_TILES[screenX+1][screenY],  // onRight
          SCREEN_TILES[screenX][screenY-1],  // onUp
          SCREEN_TILES[screenX][screenY+1]); // onDown
      String goodSuffix = EdgeHelper.nextPossible(possible, suffix);
      tileName = otherType + goodSuffix;
      MouseTile.setName(tileName);
      try {
        Tile checkTile = new Tile(0, 0, PLAYER_Z);
        if (checkTile.setType(tileType, tileName)) {
          boolean passable = checkTile.isTilePassable();
          if (!SCREEN_TILES[screenX][screenY].isPassable()) {
            passable = false;
          }
          SCREEN_TILES[screenX][screenY].setType(tileType, tileName);
          SCREEN_TILES[screenX][screenY].setPassable(passable);
          int passableInt = 0;
          if (passable) {
            passableInt = 1;
          }

          int tileX = screenX + PLAYER_X - TILE_HALF_W;
          int tileY = screenY + PLAYER_Y - TILE_HALF_H;
          try {
            mapDB.update(
                "insert into area_tile (Type, Name, X, Y, Z, Passable, ObjectId) values ('"
                    + tileType
                    + "', '"
                    + tileName
                    + "',"
                    + tileX
                    + ","
                    + tileY
                    + ","
                    + PLAYER_Z
                    + ","
                    + passableInt
                    + ", 'None')");
          } catch (SQLException ex) {
            mapDB.update(
                "update area_tile set Type = '"
                    + tileType
                    + "', Name = '"
                    + tileName
                    + "', Passable = "
                    + passableInt
                    + " where X = "
                      + tileX
                      + " and Y = "
                      + tileY
                      + " and Z = "
                      + PLAYER_Z);
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        mapDB.updateDB("ROLLBACK TRANSACTION");
      }
    }
    else if (FixEdges && canFixEdges(tileName)) {

      String tileType = MouseTile.getType();
      int lastChar = tileName.length() - 1;

      while (lastChar>0 &&  Character.isUpperCase(tileName.charAt(lastChar-1))) {
        -- lastChar;
      }
      String otherType = tileName.substring(0, lastChar);
      //String saveTileType = MouseTile.getType();

      for (int i = screenX; i < screenX + BrushSize; i++) {
        for (int j = screenY; j < screenY + BrushSize; j++) {
          SCREEN_TILES[i][j].setType(tileType, "1");
        }
      }

      boolean passable = false;

      mapDB.updateDB("BEGIN TRANSACTION");
      try {
        for (int i = screenX; i < screenX + BrushSize; i++) {
          for (int j = screenY; j < screenY + BrushSize; j++) {
            if (i > 0 && i < 21 && j > 0 && j < 13) {
              if (SCREEN_TILES[i - 1][j].getType().equals(tileType)
                  && SCREEN_TILES[i][j - 1].getType().equals(tileType)
                  && !SCREEN_TILES[i - 1][j - 1].getType().equals(tileType)) {
                // IUL
                tileName = otherType + "IDR";
              } else if (SCREEN_TILES[i + 1][j].getType().equals(tileType)
                  && SCREEN_TILES[i][j - 1].getType().equals(tileType)
                  && !SCREEN_TILES[i + 1][j - 1].getType().equals(tileType)) {
                // IUR
                tileName = otherType + "IDL";
              } else if (SCREEN_TILES[i - 1][j].getType().equals(tileType)
                  && SCREEN_TILES[i][j + 1].getType().equals(tileType)
                  && !SCREEN_TILES[i - 1][j + 1].getType().equals(tileType)) {
                // IDL
                tileName = otherType + "IUR";
              } else if (SCREEN_TILES[i + 1][j].getType().equals(tileType)
                  && SCREEN_TILES[i][j + 1].getType().equals(tileType)
                  && !SCREEN_TILES[i + 1][j + 1].getType().equals(tileType)) {
                // IDR
                tileName = otherType + "IUL";
              } else if (!SCREEN_TILES[i - 1][j].getType().equals(tileType)
                  && !SCREEN_TILES[i][j - 1].getType().equals(tileType)) {
                // UL
                tileName = otherType + "UL";
              } else if (!SCREEN_TILES[i + 1][j].getType().equals(tileType)
                  && !SCREEN_TILES[i][j - 1].getType().equals(tileType)) {
                // UR
                tileName = otherType + "UR";
              } else if (!SCREEN_TILES[i + 1][j].getType().equals(tileType)
                  && !SCREEN_TILES[i][j + 1].getType().equals(tileType)) {
                // DR
                tileName = otherType + "DR";
              } else if (!SCREEN_TILES[i - 1][j].getType().equals(tileType)
                  && !SCREEN_TILES[i][j + 1].getType().equals(tileType)) {
                // DR
                tileName = otherType + "DL";
              } else if (!SCREEN_TILES[i - 1][j].getType().equals(tileType)) {
                // L
                tileName = otherType + "L";
              } else if (!SCREEN_TILES[i + 1][j].getType().equals(tileType)) {
                // R
                tileName = otherType + "R";
              } else if (!SCREEN_TILES[i][j + 1].getType().equals(tileType)) {
                // D
                tileName = otherType + "D";
              } else if (!SCREEN_TILES[i][j - 1].getType().equals(tileType)) {
                // U
                tileName = otherType + "U";
              } else {
                int randomTile = randomGenerator.nextInt(100) + 1;
                if (BP_EDITOR.GFX.getSprite("textures/" + tileType + "/" + randomTile) == null) {
                  randomTile = 1;
                }
                tileName = Integer.toString(randomTile);
              }

              Tile checkTile = new Tile(0, 0, PLAYER_Z);
              if (checkTile.setType(tileType, tileName)) {
                passable = checkTile.isTilePassable();
                SCREEN_TILES[i][j].setType(tileType, tileName);
                SCREEN_TILES[i][j].setPassable(passable);
                int passableInt = 0;
                if (passable) {
                  passableInt = 1;
                }

                int tileX = i + PLAYER_X - TILE_HALF_W;
                int tileY = j + PLAYER_Y - TILE_HALF_H;

                try {
                  mapDB.update(
                      "insert into area_tile (Type, Name, X, Y, Z, Passable, ObjectId) values ('"
                          + tileType
                          + "', '"
                          + tileName
                          + "',"
                          + tileX
                          + ","
                          + tileY
                          + ","
                          + PLAYER_Z
                          + ","
                          + passableInt
                          + ", 'None')");
                } catch (SQLException ex) {
                  mapDB.update(
                      "update area_tile set Type = '"
                          + tileType
                          + "', Name = '"
                          + tileName
                          + "', Passable = "
                          + passableInt
                          + " where X = "
                            + tileX
                            + " and Y = "
                            + tileY
                            + " and Z = "
                            + PLAYER_Z);
                }
              }
            }
          }
        }
        mapDB.updateDB("END TRANSACTION");
      } catch (Exception ex) {
        mapDB.updateDB("ROLLBACK TRANSACTION");
      }
    } else {
      String tileType = MouseTile.getType();
      String saveName = tileName;

      mapDB.updateDB("BEGIN TRANSACTION");
      try {
        for (int i = screenX; i < screenX + BrushSize; i++) {
          for (int j = screenY; j < screenY + BrushSize; j++) {
            Tile checkTile = new Tile(0, 0, PLAYER_Z);

            if (tileName.equals("1")) {
              int randomTile = randomGenerator.nextInt(100) + 1;
              if (BP_EDITOR.GFX.getSprite("textures/" + tileType + "/" + randomTile) == null) {
                randomTile = 1;
              }
              saveName = Integer.toString(randomTile);
            }

            if (checkTile.setType(tileType, saveName)) {
              boolean passable = checkTile.isTilePassable();
              if (!SCREEN_TILES[i][j].isPassable()) {
                passable = false;
              }
              SCREEN_TILES[i][j].setType(tileType, saveName);
              SCREEN_TILES[i][j].setPassable(passable);
              int passableInt = 0;
              if (passable) {
                passableInt = 1;
              }

              int tileX = i + PLAYER_X - TILE_HALF_W;
              int tileY = j + PLAYER_Y - TILE_HALF_H;

              try {
                mapDB.update(
                    "insert into area_tile (Type, Name, X, Y, Z, Passable, ObjectId) values ('"
                        + tileType
                        + "', '"
                        + tileName
                        + "',"
                        + tileX
                        + ","
                        + tileY
                        + ","
                        + PLAYER_Z
                        + ","
                        + passableInt
                        + ", 'None')");
              } catch (SQLException ex) {
                mapDB.update(
                    "update area_tile set Type = '"
                        + tileType
                        + "', Name = '"
                        + tileName
                        + "', Passable = "
                        + passableInt
                        + " where X = "
                          + tileX
                          + " and Y = "
                          + tileY
                          + " and Z = "
                          + PLAYER_Z);
              }
            }
          }
        }
        mapDB.updateDB("END TRANSACTION");
      } catch (Exception ex) {
        mapDB.updateDB("ROLLBACK TRANSACTION");
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      Config.configure(EditorSettings.class, args[0]);
      PLAYER_X = EditorSettings.startX;
      PLAYER_Y = EditorSettings.startY;
    } else {
      System.err.println("ERROR - Please specify config directory");
      System.exit(1);
    }

    if (args.length > 1) {
      BP_EDITOR.loadPng = args[1];
    }

    app = new AppGameContainer(new BP_EDITOR());
    AppGameContainer app = new AppGameContainer(new ScalableGame(
        new BP_EDITOR(), SCREEN_WIDTH, SCREEN_HEIGHT));
    app.setDisplayMode(Math.round(SCREEN_WIDTH * EditorSettings.windowScale),
                       Math.round(SCREEN_HEIGHT * EditorSettings.windowScale),
                       FULL_SCREEN);
    app.setTargetFrameRate(FRAME_RATE);
    app.setShowFPS(true);
    app.setAlwaysRender(true);
    app.setVSync(true);
    app.setForceExit(false);

    app.start();

    System.out.println("Saving options");

    BP_EDITOR.mapDB.updateDB(
          "update editor_option set X = " + PLAYER_X + ", Y = " + PLAYER_Y + ", Z = " + PLAYER_Z);

    System.exit(0);
  }
}
