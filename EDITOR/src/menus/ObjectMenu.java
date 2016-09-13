package menus;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import map.TileObject;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import game.BP_EDITOR;
import game.EditorSettings;
import game.Database;
import gui.ObjectButton;

public class ObjectMenu extends BaseMenu {

  private final static String ROOT_DIR = "objects/";

  private Vector<ObjectButton> Buttons;
  private int X;
  private int Y;
  private String ActivePath = ROOT_DIR;
  private Vector<String> PathHistory;

  private Image deleteIcon;

  public ObjectMenu(int x, int y) {
    X = x;
    Y = y;

    Buttons = new Vector<ObjectButton>();
    PathHistory = new Vector<String>();
    load();
  }

  private void load() {

    Buttons.clear();

    File[] files = new File(EditorSettings.clientImagePath + ActivePath).listFiles();
    Arrays.sort(files);
    int i = 0;

    Buttons.add(new ObjectButton(X + (i % 8) * 50, (int) (Y + Math.floor(i / 8) * 50), "..", true));
    i++;

    deleteIcon = BP_EDITOR.GFX.getSprite("gui/editor/deleteButton").getImage();

    Buttons.add(
        new ObjectButton(X + (i % 8) * 50, (int) (Y + Math.floor(i / 8) * 50), "Delete", false));
    i++;

    int nrPerRow = 6;

    for (File file : files) {
      if (file.isFile()) {
        int mid = file.getName().lastIndexOf(".");
        String name = file.getName().substring(0, mid);
        String ext = file.getName().substring(mid + 1, file.getName().length());
        int offset = 0;

        if (ext.equals("png")) {
          if (!name.contains("_1")
              && !name.contains("_2")
              && !name.contains("_3")
              && !name.contains("_4")
              && !name.contains("_5")
              && !name.contains("_6")
              && !name.contains("_7")
              && !name.contains("_8")
              && !name.contains("_open")
              && !name.contains("_on")) {
            if (name.contains("_0")) {
              offset =  - 2;
            }

            String parentFolder = file.getPath();
            int cut = parentFolder.indexOf(ROOT_DIR) + ROOT_DIR.length();
            String path = parentFolder.substring(cut, parentFolder.lastIndexOf(".") + offset);

            Buttons.add(
                new ObjectButton(
                    X + (i % nrPerRow) * 50,
                    (int) (Y + Math.floor(i / nrPerRow) * 50),
                    path,
                    false));
            i++;
          }
        }
      } else if (file.isDirectory()) {

        Buttons.add(
            new ObjectButton(
                X + (i % nrPerRow) * 50,
                (int) (Y + Math.floor(i / nrPerRow) * 50),
                file.getName(),
                true));
        i++;
      }
    }
  }

  @Override
  public void draw(Graphics g, GameContainer app, int mouseX, int mouseY) {
    g.setColor(new Color(238, 82, 65, 255));
    g.fillRect(X, Y, 300, 500);

    g.setColor(new Color(255, 255, 255, 255));

    for (ObjectButton button : Buttons) {
      button.draw(g, mouseX, mouseY);
    }
    g.setColor(new Color(255, 255, 255, 255));
    g.drawRect(X, Y, 300, 500);
  }

  public TileObject getClickedTileObject(int mouseX, int mouseY) {
    for (ObjectButton button : Buttons) {
      if (button.clicked(mouseX, mouseY)) {
        return button.getTileObject();
      }
    }
    return null;
  }

  public int click(int mouseX, int mouseY, Database gameDB) {
    int buttonIndex = 0;
    for (ObjectButton button : Buttons) {
      if (button.clicked(mouseX, mouseY)) {

        if (button.getType().equals("Folder")) {
          goForward(button.getName() + "/");
          BP_EDITOR.loading();
          load();
          return 999;
        } else if (button.getType().equals("Back")) {
          goBack();
          BP_EDITOR.loading();
          load();
          return 999;
        } else if (button.getType().equals("Delete")) {
          return 999;
        }
        return buttonIndex;
      }
      buttonIndex++;
    }
    return 1000;
  }

  public void goBack() {
    if (PathHistory.size() > 0) {
      ActivePath = PathHistory.lastElement();
      PathHistory.remove(PathHistory.size() - 1);
    }
  }

  public void goForward(String path) {
    PathHistory.add(ActivePath);

    ActivePath += path;
  }
}
