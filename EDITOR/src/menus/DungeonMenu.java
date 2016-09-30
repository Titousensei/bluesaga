package menus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.UnicodeFont;
import game.BP_EDITOR;
import game.EditColors;
import gui.TextButton;

public class DungeonMenu extends BaseMenu {

  private final static UnicodeFont FONT = BP_EDITOR.FONTS.size12;
  private final static int ORIGIN_X = 20;
  private final static int ORIGIN_Y = 20;
  private final static int SPACING_Y = 5;

  private List<TextButton> buttons_;

  private final int posx;
  private final int posy;
  private final int height;

  private String title = "Random Dungeon";

  public DungeonMenu(int px, int py) {
    posx = px;
    posy = py;
    buttons_ = new ArrayList<>();

    int y = posy + ORIGIN_Y+ORIGIN_X;
    y += addButton("wooddungeon", posx, y) + SPACING_Y;
    y += addButton("dirtcave",    posx, y) + SPACING_Y;
    y += addButton("cave",        posx, y) + SPACING_Y;
    y += addButton("catacombs",   posx, y) + SPACING_Y;
    y += addButton("crystalcave", posx, y) + SPACING_Y;
    y += addButton("icecave",     posx, y) + SPACING_Y;
    y += addButton("sewers",      posx, y) + SPACING_Y;
    y += addButton("watercave",   posx, y) + SPACING_Y;
    height = y + SPACING_Y;
  }

  private int addButton(String text, int x, int y) {
    TextButton t = new TextButton(FONT, text, x + ORIGIN_X, y);
    buttons_.add(t);
    return t.height;
  }

  @Override
  public void setTitle(String t) { title = t; }

  @Override
  public boolean drawMouseTile() { return false; }

  @Override
  public void draw(Graphics g, GameContainer app, int mouseX, int mouseY) {
    g.setColor(EditColors.MENU);
    g.fillRect(posx, posy, 300, height);

    g.setColor(EditColors.WHITE);
    g.setFont(FONT);
    g.drawString(title, posx + SPACING_Y, posy + SPACING_Y);
    for (TextButton button : buttons_) {
      button.draw(g, mouseX, mouseY, is_ready);
    }
  }

  public String click(int mouseX, int mouseY) {
    for (TextButton button : buttons_) {
      if (button.clicked(mouseX, mouseY)) {
        return button.text;
      }
    }
    return null;
  }
}
