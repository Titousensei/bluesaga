package map;

import game.BP_EDITOR;
import game.EditColors;
import graphics.Sprite;
import gui.Font;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class TileObject {

  private String name;

  private Sprite graphics;
  private int width = 1;
  private int height = 1;

  private int Z;
  private boolean MENU = false;

  private String TrapId = null;

  public static boolean transparent = false;

  public TileObject(String newName) {
    name = newName;
    graphics = BP_EDITOR.GFX.getSprite("objects/" + name);

    if (graphics.isAnimated()) {
      width = graphics.getAnimation().getWidth() / 50;
      height = graphics.getAnimation().getHeight() / 50;
    } else {
      width = graphics.getImage().getWidth() / 50;
      height = graphics.getImage().getHeight() / 50;
    }

    //ServerMessage.printMessage("TILE OBJECT W,H: "+width+","+height);
  }

  public void draw(Graphics g, int x, int y) {
    Color alpha = transparent ? EditColors.TRANSPARENT : EditColors.WHITE;

    if (MENU || BP_EDITOR.PLAYER_Z == Z) {
      graphics.draw(x - (width - 1) * 25, y - ((height - 1) * 50), alpha);
    }

    if (TrapId != null) {
      g.setColor(EditColors.RED);
      g.drawRect(x, y, 49, 49);
      g.setFont(Font.size12bold);
      g.setColor(EditColors.BLACK);
      g.drawString(TrapId, x + 21, y + 21);
      g.setColor(EditColors.RED);
      g.drawString(TrapId, x + 20, y + 20);
    }
  }

  public Sprite getSprite() {
    return graphics;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public String getName() {
    return name;
  }

  public int getZ() {
    return Z;
  }

  public void setZ(int z) {
    Z = z;
  }

  public void setMENU(boolean newValue) {
    MENU = newValue;
  }

  public boolean isMENU() {
    return MENU;
  }

  public String getTrapId() {
    return TrapId;
  }

  public void setTrapId(String trapId) {
    TrapId = trapId;
  }
}
