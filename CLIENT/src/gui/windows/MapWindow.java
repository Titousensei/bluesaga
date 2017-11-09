package gui.windows;

import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;
import gui.Button;
import gui.MiniMapTile;
import screens.CharacterSelection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import utils.LanguageUtils;

public class MapWindow extends Window {

  public final static Color COL_TREE = new Color(126, 176, 97);
  public final static Color COL_GATHERING = new Color(255, 198, 0);
  public final static Color COL_ROCK = new Color(100, 100, 100);
  public final static Color COL_BLACK = new Color(0, 0, 0);
  public final static Color COL_DOOR = new Color(255, 255, 255);
  public final static Color COL_PATH = new Color(180, 180, 180);
  public final static Color COL_GRASS = new Color(166, 217, 124);
  public final static Color COL_TREE2 = new Color(76, 161, 82);
  public final static Color COL_WATER = new Color(65, 105, 160);
  public final static Color COL_SHALLOW = new Color(79, 128, 195);
  public final static Color COL_DIRTCAVE = new Color(150, 105, 57);
  public final static Color COL_CAVE = new Color(102, 125, 119);
  public final static Color COL_DUNGEON = new Color(154, 178, 141);
  public final static Color COL_GRASSPATCH = new Color(180, 229, 139);
  public final static Color COL_INDOORS = new Color(187, 136, 69);
  public final static Color COL_MOUNTAIN = new Color(218, 178, 99);
  public final static Color COL_MOUNTAINPATCH = new Color(203, 161, 90);
  public final static Color COL_BEACH = new Color(253, 252, 184);
  public final static Color COL_ARENA = new Color(173, 142, 82);
  public final static Color COL_CASTLE = new Color(211, 211, 211);
  public final static Color COL_CLIFF = new Color(205, 148, 73);
  public final static Color COL_SEWERS = new Color(103, 86, 56);
  public final static Color COL_ICE = new Color(170, 232, 255);
  public final static Color COL_SNOW = new Color(255, 255, 255);
  public final static Color COL_ICECAVE = new Color(170, 232, 255);
  public final static Color COL_WOODDUNGEON = new Color(244, 182, 93);
  public final static Color COL_OCEANTOWN = new Color(169, 122, 76);
  public final static Color COL_WATERCAVE1 = new Color(65, 105, 160);
  public final static Color COL_WATERCAVE2 = new Color(100, 100, 100);

  private HashMap<String, MiniMapTile> MiniMap;

  private boolean Loading = false;
  private boolean Saving = false;

  Timer closeTimer;
  Timer saveTimer;

  private Button expandButton;

  private boolean expanded = false;

  public MapWindow(int x, int y, int width, int height, boolean ShowCloseButton) {
    super("MapW", x, y, width, height, ShowCloseButton);
    // TODO Auto-generated constructor stub
    MiniMap = new HashMap<String, MiniMapTile>();
    MiniMap.clear();

    expandButton = new Button("", 135, 10, 24, 24, this);
    expandButton.setImage("gui/menu/expand_button");
    expandButton.getToolTip().setText("Expand Map");
  }

  public void load() {
    if (!isSaving()) {
      MiniMap.clear();

      setLoading(true);

      File f = new File(ClientSettings.PATH + "local.data");

      if (f.exists()) {
        FileReader fr;
        try {
          fr = new FileReader(f);
          BufferedReader br = new BufferedReader(fr);
          String mapData = br.readLine();

          if (mapData != null) {
            if (!mapData.equals("")) {
              String mapTiles[] = mapData.split(";");

              for (String mapTile : mapTiles) {
                try {
                  String tileInfo[] = mapTile.split(",");
                  String x = tileInfo[0];
                  String y = tileInfo[1];
                  String z = tileInfo[2];

                  int zInt = Integer.parseInt(z);

                  if (zInt > -100) {
                    int colorR = Integer.parseInt(tileInfo[3]);
                    int colorG = Integer.parseInt(tileInfo[4]);
                    int colorB = Integer.parseInt(tileInfo[5]);
                    MiniMapTile newMiniMapTile = new MiniMapTile(new Color(colorR, colorG, colorB));
                    MiniMap.put(x + "," + y + "," + z, newMiniMapTile);
                  }
                } catch (ArrayIndexOutOfBoundsException e) {
                } catch (NumberFormatException e) {
                }
              }
            }
          }

        } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      setLoading(false);
      BlueSaga.client.sendMessage("screen", "info");
    }
  }

  @Override
  public void draw(GameContainer app, Graphics g, int mouseX, int mouseY) {
    if (isVisible()) {
      super.draw(app, g, mouseX, mouseY);

      int z = BlueSaga.playerCharacter.getZ();

      int miniMapSquareSize = 3;
      int miniMapSize = 30;

      if (expanded) {
        miniMapSize = 60;
      }

      if (isFullyOpened()) {
        g.setFont(Font.size12);
        g.setColor(BlueSagaColors.WHITE);
        g.drawString(
            LanguageUtils.getString("ui.mini_map.mini_map"), X + 15 + moveX, Y + 15 + moveY);

        if (isLoading()) {
          g.drawString(
              LanguageUtils.getString("ui.status.loading"), X + 10 - 50 + moveX, Y + 40 + moveY);
        } else {
          for (int y = 0; y < miniMapSize * 2; y++) {
            for (int x = 0; x < miniMapSize * 2; x++) {
              int tileX = BlueSaga.playerCharacter.getX() - miniMapSize + x;
              int tileY = BlueSaga.playerCharacter.getY() - miniMapSize + y;

              if (MiniMap.containsKey(tileX + "," + tileY + "," + z)) {
                g.setColor(MiniMap.get(tileX + "," + tileY + "," + z).getColor());
              } else {
                g.setColor(BlueSagaColors.BLACK);
              }
              g.fillRect(
                  X + 10 + x * miniMapSquareSize + moveX,
                  Y + 40 + y * miniMapSquareSize + moveY,
                  miniMapSquareSize,
                  miniMapSquareSize);
            }
          }
          g.setColor(BlueSagaColors.RED);
          g.fillRect(
              X + 10 + miniMapSize * miniMapSquareSize + moveX - 1,
              Y + 40 + miniMapSize * miniMapSquareSize + moveY - 1,
              miniMapSquareSize,
              miniMapSquareSize);

          expandButton.draw(g, mouseX, mouseY);
        }
      }
    }
  }

  @Override
  public void keyLogic(Input INPUT) {
    if (INPUT.isKeyPressed(Input.KEY_M) && !BlueSaga.GUI.Chat_Window.isActive()) {
      toggle();
    }
  }

  @Override
  public void leftMouseClick(Input INPUT) {
    super.leftMouseClick(INPUT);

    int mouseX = INPUT.getAbsoluteMouseX();
    int mouseY = INPUT.getAbsoluteMouseY();

    if (isVisible()) {
      if (clickedOn(mouseX, mouseY)) {
        if (expandButton.isClicked(mouseX, mouseY)) {
          if (expanded) {
            expanded = false;
            expandButton.setImage("gui/menu/expand_button");
            expandButton.getToolTip().setText("Expand Map");
            expandButton.setX(135);

            setWidth(200);
            setHeight(230);
            aniWidth = 200;
            aniHeight = 230;

            setX(800);
            setY(90);
            moveX = 0;
            moveY = 0;

          } else {
            expanded = true;
            expandButton.setImage("gui/menu/contract_button");
            expandButton.getToolTip().setText("Contract Map");
            expandButton.setX(315);

            setWidth(380);
            setHeight(410);

            aniWidth = 380;
            aniHeight = 410;

            setX(620);
            setY(90);
            moveX = 0;
            moveY = 0;
          }
        }
      }
    }
  }

  public boolean updateTileObject(int x, int y, int z, String tileObject) {

    boolean updateTile = false;

    MiniMapTile t = null;

    if (tileObject.contains("tree") || tileObject.contains("bush")) {
      t = new MiniMapTile(COL_TREE);
    } else if (tileObject.contains("gathering")) {
      t = new MiniMapTile(COL_GATHERING);
    } else if (tileObject.contains("rock")) {
      t = new MiniMapTile(COL_ROCK);
    } else if (tileObject.contains("moveable")) {
      t = new MiniMapTile(COL_ROCK);
    } else if (tileObject.contains("cellardoor")) {
      t = new MiniMapTile(COL_DOOR);
    }

    if (t != null) {
      updateTile = true;

      // Check if tile should be updated or added
      if (MiniMap.containsKey(x + "," + y + "," + z)) {
        if (t.getColor().getRed() != MiniMap.get(x + "," + y + "," + z).getColor().getRed()
            || t.getColor().getGreen() != MiniMap.get(x + "," + y + "," + z).getColor().getGreen()
            || t.getColor().getBlue() != MiniMap.get(x + "," + y + "," + z).getColor().getBlue()) {
          MiniMap.put(x + "," + y + "," + z, t);
        }
      } else {
        MiniMap.put(x + "," + y + "," + z, t);
      }
    }
    return updateTile;
  }

  public void updateTile(int x, int y, int z, String type, String name) {

    Color newColor = COL_BLACK;
    if (name.toLowerCase().contains("exit")
        || name.toLowerCase().contains("entrance")
        || name.toLowerCase().contains("hole")
        || name.toLowerCase().contains("stairs")) {
      newColor = COL_DOOR;
    } else if (name.contains("path")) {
      newColor = COL_PATH;
    } else if (type.equals("grass")) {
      newColor = COL_GRASS;
    } else if (type.equals("tree")) {
      newColor = COL_TREE2;
    } else if (type.equals("water")) {
      newColor = COL_WATER;
    } else if (type.equals("shallow")) {
      newColor = COL_SHALLOW;
    } else if (type.equals("dirtcave")) {
      newColor = COL_DIRTCAVE;
    } else if (type.equals("cave")) {
      newColor = COL_CAVE;
    } else if (type.equals("dungeon")) {
      newColor = COL_DUNGEON;
    } else if (type.equals("grasspatch")) {
      newColor = COL_GRASSPATCH;
    } else if (type.equals("indoors")) {
      newColor = COL_INDOORS;
    } else if (type.equals("mountain")) {
      newColor = COL_MOUNTAIN;
    } else if (type.equals("mountainpatch")) {
      newColor = COL_MOUNTAINPATCH;
    } else if (type.equals("beach") || type.equals("sand")) {
      newColor = COL_BEACH;
    } else if (type.equals("arena")) {
      newColor = COL_ARENA;
    } else if (type.equals("castle")) {
      newColor = COL_CASTLE;
    } else if (type.equals("cliff") || type.equals("grasscliff")) {
      newColor = COL_CLIFF;
    } else if (type.equals("sewers")) {
      newColor = COL_SEWERS;
    } else if (type.equals("ice")) {
      newColor = COL_ICE;
    } else if (type.equals("snow")) {
      newColor = COL_SNOW;
    } else if (type.equals("icecave")) {
      newColor = COL_ICECAVE;
    } else if (type.equals("wooddungeon")) {
      newColor = COL_WOODDUNGEON;
    } else if (type.equals("oceantown")) {
      newColor = COL_OCEANTOWN;
    } else if (type.equals("watercave")) {
      if (name.equals("1")) {
        newColor = COL_WATERCAVE1;
      } else {
        newColor = COL_WATERCAVE2;
      }
    }

    MiniMapTile t = new MiniMapTile(newColor);

    // Check if tile should be updated or added
    if (MiniMap.containsKey(x + "," + y + "," + z)) {
      if (newColor.getRed() != MiniMap.get(x + "," + y + "," + z).getColor().getRed()
          || newColor.getGreen() != MiniMap.get(x + "," + y + "," + z).getColor().getGreen()
          || newColor.getBlue() != MiniMap.get(x + "," + y + "," + z).getColor().getBlue()) {
        t.setUpdate(true);
        MiniMap.put(x + "," + y + "," + z, t);
      }
    } else {
      MiniMap.put(x + "," + y + "," + z, t);
    }
  }

  @Override
  public void close() {
    super.close();
  }

  // doAfterSave
  // 0: nothing
  // 1: quit game
  // 2: back to character select
  public void saveMiniMap(int doAfterSave) {
    setSaving(true);
    File yourFile = new File(ClientSettings.PATH + "local.data");
    if (!yourFile.exists()) {
      try {
        yourFile.createNewFile();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    PrintWriter writer = null;
    try {
      writer = new PrintWriter(yourFile);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (writer != null) {
      for (Iterator<Entry<String, MiniMapTile>> iter = MiniMap.entrySet().iterator();
          iter.hasNext();
          ) {
        Entry<String, MiniMapTile> entry = iter.next();
        writer.print(
            entry.getKey()
                + ","
                + entry.getValue().getColor().getRed()
                + ","
                + entry.getValue().getColor().getGreen()
                + ","
                + entry.getValue().getColor().getBlue()
                + ";");
      }
    }

    writer.close();

    setSaving(false);

    if (doAfterSave == 1) {
      closeTimer = new Timer();
      closeTimer.schedule(
          new TimerTask() {
            @Override
            public void run() {
              BlueSaga.client.closeConnection();
              System.exit(0);
            }
          },
          200);
    } else if (doAfterSave == 2) {
      CharacterSelection.show();
    }
  }

  public boolean isLoading() {
    return Loading;
  }

  public void setLoading(boolean loading) {
    Loading = loading;
  }

  public boolean isSaving() {
    return Saving;
  }

  public void setSaving(boolean saving) {
    Saving = saving;
  }
}
