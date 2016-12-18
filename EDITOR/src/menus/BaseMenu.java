package menus;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.gui.TextField;

import game.BP_EDITOR;

public abstract class BaseMenu {

  protected boolean is_ready = true;

  public abstract void draw(Graphics g, GameContainer app, int mouseX, int mouseY);

  // return keepOpen
  public boolean keyLogic(Input INPUT) {
    return !INPUT.isKeyPressed(Input.KEY_ESCAPE);
  }

  public void setReady(boolean val) { is_ready = val; }
  public boolean isReady() { return is_ready; }

  public void clear() {}

  public void setTitle(String val) {}

  public String getValue() { return null; }

  public boolean drawMouseTile() { return true; }
}
