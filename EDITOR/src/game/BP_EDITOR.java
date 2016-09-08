package game;

import gui.Font;
import gui.Gui;
import gui.MouseCursor;
import utils.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import map.Coords;
import map.Tile;
import map.TileObject;
import map.WorldMap;
import menus.AreaEffectMenu;
import menus.BaseMenu;
import menus.MonsterMenu;
import menus.ObjectMenu;
import menus.TextureMenu;

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
      DELETE_TILE, DELETE_OBJECT, DELETE_MONSTER, MINI_MAP
  };

  private static AppGameContainer app;

  // INIT AND RESOLUTION
  private static int SCREEN_WIDTH = 1024; // 1024
  private static int SCREEN_HEIGHT = 640; // 640

  private static boolean FULL_SCREEN = false;
  private static final int FRAME_RATE = 60;
  public static int TILE_SIZE = 50;

  public static boolean SHOW_PASSABLE = true;

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

  // SCREEN DATA

  public static Tile SCREEN_TILES[][][];
  public static TileObject SCREEN_OBJECTS[][][];
  public static int TILE_HALF_W = 11;
  public static int TILE_HALF_H = 7;

  // GUI
  private static Gui GUI;
  private static MouseCursor Mouse;

  private static AreaEffectMenu AREA_EFFECT_MENU;

  private static TextureMenu TEXTURE_MENU;
  private static MonsterMenu MONSTER_MENU;
  private static ObjectMenu OBJECT_MENU;

  private static BaseMenu activeMenu = null;

  public static boolean Loading = true;
  public static Random randomGenerator = new Random();

  private int helpY;

  private static MapImporter loadPng = null;

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

    try (ResultSet editorOptions = mapDB.askDB("select X,Y,Z from editor_option")) {
      if (editorOptions.next()) {
        PLAYER_X = editorOptions.getInt("X");
        PLAYER_Y = editorOptions.getInt("Y");
        PLAYER_Z = editorOptions.getInt("Z");
      }
      editorOptions.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    GFX = new ImageResource();
    GFX.load();

    Mouse = new MouseCursor();

    FONTS = new Font();

    TEXTURE_MENU = new TextureMenu(600, 40);
    MONSTER_MENU = new MonsterMenu(500, 70);
    OBJECT_MENU  = new ObjectMenu(680, 70);
    AREA_EFFECT_MENU = new AreaEffectMenu(container);

    // GUI INIT
    GUI = new Gui();
    GUI.init();

    container.setMouseCursor(EditorSettings.clientImagePath + "gui/cursors/cursor_hidden.png", 5, 5);

    MouseTile = null;

    SCREEN_TILES = new Tile[TILE_HALF_W * 2][TILE_HALF_H * 2][MAX_Z];
    SCREEN_OBJECTS = new TileObject[TILE_HALF_W * 2][TILE_HALF_H * 2][MAX_Z];

    for (int i = 0; i < 22; i++) {
      for (int j = 0; j < 14; j++) {
        for (int k = 0; k < MAX_Z; k++) {
          SCREEN_TILES[i][j][k] = new Tile(i, j, k);
          SCREEN_OBJECTS[i][j][k] = null;
        }
      }
    }

    if (loadPng != null) {
      loadPng.load();
    }

    loadScreen();

    loading();
  }

  public void loadMiniMap() {
    MiniMap.clear();
    try (ResultSet mapInfo = mapDB.askDB(
            "select X, Y, Z, Type from area_tile where Z == "
                + PLAYER_Z
                + " AND X % 2 = 0 AND Y % 2 = 0")
    ) {
      while (mapInfo.next()) {
        Coords xyz = new Coords(mapInfo.getInt("X"), mapInfo.getInt("Y"), mapInfo.getInt("Z"));
        if (mapInfo.getString("Type").equals("water")) {
          MiniMap.put(xyz, EditColors.WATER);
        } else if (mapInfo.getString("Type").equals("shallow")) {
          MiniMap.put(xyz, EditColors.SHALLOW);
        } else if (mapInfo.getString("Type").equals("beach")) {
          MiniMap.put(xyz, EditColors.BEACH);
        } else if (mapInfo.getString("Type").equals("cliff")) {
          MiniMap.put(xyz, EditColors.CLIFF);
        } else {
          MiniMap.put(xyz, EditColors.MAP_OTHER);
        }

      }
      mapInfo.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void loadScreen() {

    for (int i = 0; i < 22; i++) {
      for (int j = 0; j < 14; j++) {
        for (int k = 0; k < MAX_Z; k++) {
          SCREEN_TILES[i][j][k].clear();
          SCREEN_OBJECTS[i][j][k] = null;
        }
      }
    }

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
        int tileZ = mapInfo.getInt("Z") - PLAYER_Z;
        Tile t = SCREEN_TILES[tileX][tileY][tileZ];
        t.setId(mapInfo.getString("Id"));

        boolean passable = false;
        if (mapInfo.getInt("Passable") == 1) {
          passable = true;
        }
        t.setType(mapInfo.getString("Type"), mapInfo.getString("Name"));
        t.setPassable(passable);
        t.setZ(mapInfo.getInt("Z"));
        t.setAreaEffectId(mapInfo.getString("AreaEffectId"));

        if (!mapInfo.getString("ObjectId").equals("None")) {
          TileObject newObject = new TileObject(mapInfo.getString("ObjectId"));
          newObject.setZ(mapInfo.getInt("Z"));
          SCREEN_OBJECTS[tileX][tileY][tileZ] = newObject;
        }

        String doorId = mapInfo.getString("DoorId");
        if (doorId!=null && !"0".equals(doorId)) {
          try (ResultSet doorInfo = mapDB.askDB(
                  "select GotoX, GotoY, GotoZ from door where Id = " + doorId);
          ) {
            if (doorInfo.next()) {
              Coords xyz = new Coords(
                  doorInfo.getInt("GotoX"),
                  doorInfo.getInt("GotoY"),
                  doorInfo.getInt("GotoZ"));
              t.setDestCoords(xyz);
              t.setDoorId(doorId);
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
        SCREEN_TILES[tileX][tileY][0].setDoorCoords(xyz);
        SCREEN_TILES[tileX][tileY][0].setDestId(destId);
      }
      doorInfo.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try (ResultSet containerInfo = mapDB.askDB(
            "select X,Y,Z,Type from area_container where X >= "
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
        SCREEN_OBJECTS[tileX][tileY][tileZ] = newObject;
      }
      containerInfo.getStatement().close();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    try (ResultSet monsterInfo = mapDB.askDB(
            "select SpawnX, SpawnY, SpawnZ, CreatureId from area_creature where SpawnX >= "
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

        SCREEN_TILES[tileX][tileY][tileZ].setOccupant(new Creature(creatureId, 0, 0));
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

        SCREEN_OBJECTS[tileX][tileY][tileZ].setTrapId(trapInfo.getString("Id"));
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

        SCREEN_TILES[tileX][tileY][tileZ].setTriggerId(triggerInfo.getString("Id"));
        SCREEN_TILES[tileX][tileY][tileZ].setTrapId(triggerInfo.getString("TrapId"));
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

  private void resetHelp(Graphics g) {
    g.setFont(FONTS.size8);
    helpY = 40;
  }

  private void renderHelp(Graphics g, String str) {
    g.setColor(EditColors.BLACK);
    g.drawString(str, 21, helpY+1);
    g.setColor(EditColors.WHITE);
    g.drawString(str, 20, helpY);
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
        SCREEN_TILES[i][j][0].draw(g, i * TILE_SIZE, j * TILE_SIZE);
      }
    }

    for (int j = 0; j < 14; j++) {
      for (int i = 0; i < 22; i++) {
        if (SCREEN_OBJECTS[i][j][0] != null) {
          SCREEN_OBJECTS[i][j][0].draw(g, i * TILE_SIZE, j * TILE_SIZE);
        }
      }
    }

    for (int i = 0; i < 22; i++) {
      for (int j = 0; j < 14; j++) {
        SCREEN_TILES[i][j][0].drawOverlay(g, i * TILE_SIZE, j * TILE_SIZE);
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
      for (Map.Entry<Coords, Color> pairs : MiniMap.entrySet()) {
        Coords coord = pairs.getKey();
        int x = (coord.x - EditorSettings.startX) / 2 + 100;
        int y = (coord.y - EditorSettings.startY) / 2 + 200;

        g.setColor(pairs.getValue());
        g.fillRect(x, y, 1, 1);
      }

      int playerx = (PLAYER_X - EditorSettings.startX) / 2 + 100 - 6;
      int playery = (PLAYER_Y - EditorSettings.startY) / 2 + 200 - 6;

      g.setColor(EditColors.RED);
      g.drawRect(playerx, playery, 10, 6);
    } else {
      resetHelp(g);
      renderHelp(g, "F12: Quit");
      renderHelp(g, "B: Background Tiles menu");
      renderHelp(g, "M: Monsters menu");
      renderHelp(g, "O: Objects menu");
      renderHelp(g, "F1: Hide/View Passable");
      renderHelp(g, "F2: Transparent Objects");
      renderHelp(g, "F4: Mini Map");
      renderHelp(g, "1/2: Brushsize -/+ " + BrushSize);
      renderHelp(g, "P: Toggle Passable");
      renderHelp(g, "R: Place Door");
      renderHelp(g, "E: Place Effect");
      renderHelp(g, "Del: Clear Area");

      String willEdges = (FixEdges
          && BrushSize>1
          && currentMode == Mode.BRUSH
          && MouseTile!=null
          && canFixEdges(MouseTile.getName()))
          ? "will do" : "won't do";

      renderHelp(g, "F: Fix edges = " + FixEdges + " (" + willEdges + ")");
      renderHelp(g, "N: Spawns = "
          + ((DAY_NIGHT_TIME==0) ? "night+day" : (DAY_NIGHT_TIME==1) ? "night" : "day"));
      renderHelp(g, "PgUp/PgDown: Z +/-");

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
      case PASSABLE:
        renderMode(g, "MODE: Toggle Passable - " + makePassable);
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

        g.setFont(FONTS.size8);
        g.setColor(EditColors.WHITE);
        g.drawString(tileX + "," + tileY + "," + PLAYER_Z, 105, 30);

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

        if (!Loading && activeMenu != null) {
          activeMenu.draw(g, container, INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
        }
      }
    }

    if (INPUT != null) {
      Mouse.draw(INPUT.getAbsoluteMouseX(), INPUT.getAbsoluteMouseY());
    }
  }

  /*
   *
   * 	KEYBOARD & MOUSE
   *
   */

  private void keyLogic(GameContainer GC) {
    INPUT = GC.getInput();
    if (INPUT.isKeyPressed(Input.KEY_F12)) {
      GC.exit();
    }

    if (activeMenu != null) {
      boolean keepOpen = activeMenu.keyLogic(INPUT);
      if (!keepOpen) {
        activeMenu = null;
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
      currentMode = Mode.BRUSH;
      MouseMonster = null;
      MouseObject = null;
      MouseTile = null;
      loadScreen();
    }

    if (INPUT.isKeyPressed(Input.KEY_N)) {
      DAY_NIGHT_TIME++;
      if (DAY_NIGHT_TIME > 2) {
        DAY_NIGHT_TIME = 0;
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

    if (INPUT.isKeyPressed(Input.KEY_PRIOR)) {
      PLAYER_Z++;
      if (currentMode == Mode.MINI_MAP) {
        loadMiniMap();
      }
      loadScreen();
    } else if (INPUT.isKeyPressed(Input.KEY_NEXT)) {
      PLAYER_Z--;
      if (currentMode == Mode.MINI_MAP) {
        loadMiniMap();
      }
      loadScreen();
    }

    if (INPUT.isKeyPressed(Input.KEY_F1)) {
      if (SHOW_PASSABLE) {
        SHOW_PASSABLE = false;
      } else {
        SHOW_PASSABLE = true;
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_F2)) {
      TileObject.transparent = !TileObject.transparent;
    }

    if (INPUT.isKeyPressed(Input.KEY_F)) {
      if (FixEdges) {
        FixEdges = false;
      } else {
        FixEdges = true;
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_1) && BrushSize>1) {
      BrushSize--;
    }
    if (INPUT.isKeyPressed(Input.KEY_2) && BrushSize<8) {
      BrushSize++;
    }

    if (INPUT.isKeyPressed(Input.KEY_F4)) {
      if (currentMode != Mode.MINI_MAP) {
        loadMiniMap();
        currentMode = Mode.MINI_MAP;
        activeMenu = null;
      } else {
        currentMode = Mode.BRUSH;
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
    if (INPUT.isKeyPressed(Input.KEY_E)) {
      activeMenu = AREA_EFFECT_MENU;
      activeMenu.clear();
    }
    if (INPUT.isKeyPressed(Input.KEY_R)) { // Door
      activeMenu = null;
      currentMode = Mode.DOOR;
    }

    if (INPUT.isKeyPressed(Input.KEY_DELETE)) {
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

    if (INPUT.isKeyPressed(Input.KEY_P)) {
      activeMenu = null;

      if (currentMode == Mode.PASSABLE) {
        makePassable = !makePassable;
      }
      else {
        currentMode = Mode.PASSABLE;
      }
    }

    if (INPUT.isMousePressed(0)) {

      int mouseX = INPUT.getAbsoluteMouseX();
      int mouseY = INPUT.getAbsoluteMouseY();

      int screenX = (int) Math.floor(mouseX / TILE_SIZE);
      int screenY = (int) Math.floor(mouseY / TILE_SIZE);

      int tileX = screenX + PLAYER_X - TILE_HALF_W;
      int tileY = screenY + PLAYER_Y - TILE_HALF_H;

      if (currentMode == Mode.MINI_MAP) {

        PLAYER_X = EditorSettings.startX - 200 + mouseX * 2 - 10;
        PLAYER_Y = EditorSettings.startY - 200 + mouseY * 2 - 6 - 200;
        loadScreen();
        INPUT.clearKeyPressedRecord();
        return;

      } else if (activeMenu == TEXTURE_MENU) {
        int clickButtonIndex = TEXTURE_MENU.click(mouseX, mouseY, mapDB);
        if (clickButtonIndex < 998) {
          currentMode = Mode.BRUSH;
          MouseTile = new Tile(0, 0, 0);
          MouseTile.setType(
              TEXTURE_MENU.getTile(clickButtonIndex).getType(),
              TEXTURE_MENU.getTile(clickButtonIndex).getName());
          INPUT.clearKeyPressedRecord();
          return;
        } else if (clickButtonIndex == 998) {
          currentMode = Mode.DELETE_TILE;
          MouseTile = null;
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
          INPUT.clearKeyPressedRecord();
          return;
        } else if (clickButtonIndex == 999) {
          currentMode = Mode.DELETE_MONSTER;
          MouseMonster = null;
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
          INPUT.clearKeyPressedRecord();
          return;
        } else if (clickButtonIndex == 999) {
          currentMode = Mode.DELETE_OBJECT;
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
                + SCREEN_TILES[screenX][screenY][0].getDestId());
        mapDB.updateDB(
            "delete from door where Id = "
                + SCREEN_TILES[screenX][screenY][0].getDoorId());
        mapDB.updateDB(
            "update area_tile set AreaEffectId = 0 where X = "
                + tileX
                + " and Y = "
                + tileY
                + " and Z = "
                + PLAYER_Z);
        mapDB.updateDB(
            "update area_tile set DoorId = 0 where DoorId = "
                + SCREEN_TILES[screenX][screenY][0].getDestId());
        mapDB.updateDB(
            "update area_tile set DoorId = 0 where DoorId = "
                + SCREEN_TILES[screenX][screenY][0].getDoorId());
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

        SCREEN_TILES[screenX][screenY][0].setAreaEffectId(AREA_EFFECT_ID);
        break;

      case DOOR:
        currentMode = Mode.DESTINATION;
        TILE_DOOR_ID = SCREEN_TILES[screenX][screenY][0].getId();
        TRIGGER_DOOR_ID = SCREEN_TILES[screenX][screenY][0].getDoorId();
        if (TRIGGER_DOOR_ID==null) {
          SCREEN_TILES[screenX][screenY][0].setDoorId("New");
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
        SCREEN_TILES[screenX][screenY][0].setPassable(makePassable);
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

              if (SCREEN_TILES[i][j][0].getDoorId() != null) {
                mapDB.updateDB(
                    "delete from door where Id = " + SCREEN_TILES[i][j][0].getDoorId());
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
          if (!SCREEN_TILES[screenX][screenY][0].getType().equals("None")) {

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
              SCREEN_OBJECTS[screenX][screenY][0] = newObject;
              SCREEN_TILES[screenX][screenY][0].setPassable(false);
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
              SCREEN_OBJECTS[screenX][screenY][0] = newObject;

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
                    //TileObject newObject = new TileObject(MouseObject.getName());
                    //newObject.setZ(PLAYER_Z);
                    //SCREEN_OBJECTS[screenX][screenY][1] = newObject;
                    //SCREEN_TILES[screenX][screenY][1].setPassable(false);

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

    if (INPUT.isMousePressed(1)) {
      int mouseX = INPUT.getAbsoluteMouseX();
      int mouseY = INPUT.getAbsoluteMouseY();

      int screenX = (int) Math.floor(mouseX / TILE_SIZE);
      int screenY = (int) Math.floor(mouseY / TILE_SIZE);

      if (SCREEN_TILES[screenX][screenY][0] != null) {
        if (SCREEN_TILES[screenX][screenY][0].getDoorCoords() != null) {
          Coords xyz = SCREEN_TILES[screenX][screenY][0].getDoorCoords();
          PLAYER_X = xyz.x;
          PLAYER_Y = xyz.y;
          PLAYER_Z = xyz.z;
          loadScreen();
        }
        else if (SCREEN_TILES[screenX][screenY][0].getDestCoords() != null) {
          Coords xyz = SCREEN_TILES[screenX][screenY][0].getDestCoords();
          PLAYER_X = xyz.x;
          PLAYER_Y = xyz.y;
          PLAYER_Z = xyz.z;
          loadScreen();
        }
        else if (SCREEN_TILES[screenX][screenY][0].getOccupant() != null) {
          int creatureId = SCREEN_TILES[screenX][screenY][0].getOccupant().getId();
          MouseMonster = new Monster(creatureId, 0, 0, "no");
          MouseObject = null;
          MouseTile = null;
          currentMode = Mode.BRUSH;
        }
        else if (SCREEN_TILES[screenX][screenY][0].getAreaEffectId() != null) {
          currentMode = Mode.EFFECT;
          AREA_EFFECT_ID = SCREEN_TILES[screenX][screenY][0].getAreaEffectId();
        }
        else if (SCREEN_TILES[screenX][screenY][0].getTrapId() != null) {
          currentMode = Mode.TRIGGER;
          TRIGGER_TRAP_ID = SCREEN_TILES[screenX][screenY][0].getTrapId();
        }
        else if (SCREEN_OBJECTS[screenX][screenY][0] != null) {
          if (SCREEN_OBJECTS[screenX][screenY][0].getTrapId() != null) {
            currentMode = Mode.TRIGGER;
            TRIGGER_TRAP_ID = SCREEN_OBJECTS[screenX][screenY][0].getTrapId();
          } else {
            MouseMonster = null;
            MouseObject = SCREEN_OBJECTS[screenX][screenY][0];
            MouseTile = null;
            currentMode = Mode.BRUSH;
          }
        }
        else {
          MouseTile = new Tile(0, 0, 0);
          MouseTile.setType(
              SCREEN_TILES[screenX][screenY][0].getType(),
              SCREEN_TILES[screenX][screenY][0].getName());
          currentMode = Mode.BRUSH;
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
    if (SCREEN_TILES[screenX][screenY][0].getName().contains("D")
        || SCREEN_TILES[screenX][screenY][0].getName().contains("U")
        || SCREEN_TILES[screenX][screenY][0].getName().contains("L")
        || SCREEN_TILES[screenX][screenY][0].getName().contains("R")) {
      return true;
    }
    return false;
  }

  public static boolean canFixEdges(String tileName) {
    return (tileName.contains("D")
        && !tileName.contains("Stairs")
        && !tileName.contains("Entrance")
        && !tileName.contains("Exit")
        && !tileName.contains("wall"));
  }

  public static void addTiles(int screenX, int screenY) {

    String tileName = MouseTile.getName();
    if (FixEdges && BrushSize>1 && canFixEdges(tileName)) {

      String tileType = MouseTile.getType();
      int lastChar = MouseTile.getName().length() - 1;

      while (lastChar>0 &&  Character.isUpperCase(tileName.charAt(lastChar-1))) {
        -- lastChar;
      }
      String otherType = tileName.substring(0, lastChar);
      //String saveTileType = MouseTile.getType();

      for (int i = screenX; i < screenX + BrushSize; i++) {
        for (int j = screenY; j < screenY + BrushSize; j++) {
          SCREEN_TILES[i][j][0].setType(tileType, "1");
        }
      }

      boolean passable = false;

      mapDB.updateDB("BEGIN TRANSACTION");
      try {
        for (int i = screenX; i < screenX + BrushSize; i++) {
          for (int j = screenY; j < screenY + BrushSize; j++) {
            if (i > 0 && i < 21 && j > 0 && j < 13) {
              if (SCREEN_TILES[i - 1][j][0].getType().equals(tileType)
                  && SCREEN_TILES[i][j - 1][0].getType().equals(tileType)
                  && !SCREEN_TILES[i - 1][j - 1][0].getType().equals(tileType)) {
                // IUL
                tileName = otherType + "IDR";
              } else if (SCREEN_TILES[i + 1][j][0].getType().equals(tileType)
                  && SCREEN_TILES[i][j - 1][0].getType().equals(tileType)
                  && !SCREEN_TILES[i + 1][j - 1][0].getType().equals(tileType)) {
                // IUR
                tileName = otherType + "IDL";
              } else if (SCREEN_TILES[i - 1][j][0].getType().equals(tileType)
                  && SCREEN_TILES[i][j + 1][0].getType().equals(tileType)
                  && !SCREEN_TILES[i - 1][j + 1][0].getType().equals(tileType)) {
                // IDL
                tileName = otherType + "IUR";
              } else if (SCREEN_TILES[i + 1][j][0].getType().equals(tileType)
                  && SCREEN_TILES[i][j + 1][0].getType().equals(tileType)
                  && !SCREEN_TILES[i + 1][j + 1][0].getType().equals(tileType)) {
                // IDR
                tileName = otherType + "IUL";
              } else if (!SCREEN_TILES[i - 1][j][0].getType().equals(tileType)
                  && !SCREEN_TILES[i][j - 1][0].getType().equals(tileType)) {
                // UL
                tileName = otherType + "UL";
              } else if (!SCREEN_TILES[i + 1][j][0].getType().equals(tileType)
                  && !SCREEN_TILES[i][j - 1][0].getType().equals(tileType)) {
                // UR
                tileName = otherType + "UR";
              } else if (!SCREEN_TILES[i + 1][j][0].getType().equals(tileType)
                  && !SCREEN_TILES[i][j + 1][0].getType().equals(tileType)) {
                // DR
                tileName = otherType + "DR";
              } else if (!SCREEN_TILES[i - 1][j][0].getType().equals(tileType)
                  && !SCREEN_TILES[i][j + 1][0].getType().equals(tileType)) {
                // DR
                tileName = otherType + "DL";
              } else if (!SCREEN_TILES[i - 1][j][0].getType().equals(tileType)) {
                // L
                tileName = otherType + "L";
              } else if (!SCREEN_TILES[i + 1][j][0].getType().equals(tileType)) {
                // R
                tileName = otherType + "R";
              } else if (!SCREEN_TILES[i][j + 1][0].getType().equals(tileType)) {
                // D
                tileName = otherType + "D";
              } else if (!SCREEN_TILES[i][j - 1][0].getType().equals(tileType)) {
                // U
                tileName = otherType + "U";
              } else {
                //tileName = SCREEN_TILES[i][j][1].getName();
                //tileType = SCREEN_TILES[i][j][1].getType();

                int randomTile = randomGenerator.nextInt(100) + 1;
                if (BP_EDITOR.GFX.getSprite("textures/" + tileType + "/" + randomTile) == null) {
                  randomTile = 1;
                }
                tileName = Integer.toString(randomTile);
              }

              Tile checkTile = new Tile(0, 0, PLAYER_Z);
              if (checkTile.setType(tileType, tileName)) {
                passable = checkTile.isTilePassable();
                SCREEN_TILES[i][j][0].setType(tileType, tileName);
                SCREEN_TILES[i][j][0].setPassable(passable);
                int passableInt = 0;
                if (passable) {
                  passableInt = 1;
                }

                int tileX = i + PLAYER_X - TILE_HALF_W;
                int tileY = j + PLAYER_Y - TILE_HALF_H;

                try {
                  mapDB.update(
                      "insert into area_tile (Type, Name, X, Y, Z, Passable) values ('"
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
                          + ")");
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
              boolean passable = false;

              passable = checkTile.isTilePassable();
              SCREEN_TILES[i][j][0].setType(tileType, saveName);
              SCREEN_TILES[i][j][0].setPassable(passable);
              int passableInt = 0;
              if (passable) {
                passableInt = 1;
              }

              int tileX = i + PLAYER_X - TILE_HALF_W;
              int tileY = j + PLAYER_Y - TILE_HALF_H;

              try {
                mapDB.update(
                    "insert into area_tile (Type, Name, X, Y, Z, Passable) values ('"
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
                        + ")");
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
      loadPng = new MapImporter(args[1]);
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
