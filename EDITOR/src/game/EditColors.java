package game;

import org.newdawn.slick.Color;

public final class EditColors
{
  public final static Color CLEAR = new Color(0, 0, 0, 0);
  public final static Color BLACK = new Color(0, 0, 0, 255);
  public final static Color WHITE = new Color(255, 255, 255);
  public final static Color TRANSPARENT = new Color(255, 255, 255, 100);
  public final static Color DIM = new Color(255, 255, 255, 160);

  public final static Color RED    = new Color(255, 0, 0);
  public final static Color ORANGE = new Color(255, 128, 0);
  public final static Color GREEN  = new Color(0, 255, 0);

  public final static Color TREES = new Color(64, 157, 31);
  public final static Color ROCKS = new Color(135, 127, 106);
  public final static Color WATER = new Color(65, 114, 187);
  public final static Color SHALLOW = new Color(144, 188, 255);
  public final static Color BEACH = new Color(255, 250, 94);
  public final static Color CLIFF = new Color(150, 108, 58);
  public final static Color MAP_OTHER = new Color(166, 217, 124);
  public final static Color GATHERING = new Color(255, 108, 58);

  public final static Color NIGHT = new Color(0, 0, 0, 150);
  public final static Color DAY = new Color(255, 255, 255, 150);

  public final static Color DOOR_BG = new Color(0, 0, 255, 100);
  public final static Color DEST_BG = new Color(0, 255, 255, 100);
  public final static Color DOOR_TXT = new Color(0, 0, 255);
  public final static Color DEST_TXT = new Color(128, 255, 255);

  public final static Color IMPASSABLE = new Color(255, 0, 0, 55);
  public final static Color AREA_EFFECT = new Color(255, 104, 235);
  public final static Color TRIGGER = new Color(255, 190, 30);

  public final static Color MENU = new Color(238, 82, 65, 255);

  private EditColors() { throw new AssertionError(); }
}
