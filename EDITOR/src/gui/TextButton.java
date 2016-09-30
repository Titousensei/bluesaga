package gui;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.UnicodeFont;

import game.EditColors;

public class TextButton {

  public final static int PADDING_TOP    = 10;
  public final static int PADDING_BOTTOM = 10;
  public final static int PADDING_LEFT   = 20;
  public final static int PADDING_RIGHT  = 20;

  public final UnicodeFont font;
  public final String text;
  public final int posx;
  public final int posy;
  public final int width;
  public final int height;

  public TextButton(UnicodeFont f, String t, int x, int y) {
    font = f;
    text = t;
    posx = x;
    posy = y;
    width  = font.getWidth(text) + PADDING_LEFT + PADDING_RIGHT;
    height = font.getHeight(text) + PADDING_TOP + PADDING_BOTTOM;
  }

  public boolean clicked(int mouseX, int mouseY) {
    return (mouseX > posx) && (mouseX < posx + width) && (mouseY > posy) && (mouseY < posy + height);
  }

  public void draw(Graphics g, int mouseX, int mouseY, boolean active) {
    g.setFont(font);
    if (active) {
      g.setColor(EditColors.WHITE);
    } else {
      g.setColor(EditColors.TRANSPARENT);
    }
    g.drawString(text, posx + PADDING_LEFT, posy + PADDING_TOP);

    if (clicked(mouseX, mouseY)) {
      g.setColor(EditColors.TRANSPARENT);
      g.fillRect(posx, posy, width, height);
    }
  }
}
