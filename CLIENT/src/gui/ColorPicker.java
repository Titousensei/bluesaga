package gui;

import graphics.BlueSagaColors;

import java.util.*;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ColorPicker {
  public final static Color COL_PICKER = new Color(255, 255, 255, 200);

  private int X;
  private int Y;

  private int Width;
  private int Height;

  private List<Color> Colors;

  public ColorPicker(int x, int y) {
    Colors = new ArrayList<>(100);
    X = x;
    Y = y;
    Width = 0;
    Height = 0;
  }

  public void addColor(Color newColor) {
    Colors.add(newColor);

    int nrColors = Colors.size();
    if (nrColors > 8) {
      nrColors = 8;
    }

    Width = nrColors * 40 + 10;
    Height = (int) Math.ceil((float) Colors.size() / 8.0f) * 50;
  }

  public void draw(Graphics g, int mouseX, int mouseY) {

    g.setColor(BlueSagaColors.RED.darker(0.2f));
    g.fillRoundRect(X, Y, Width, Height, 10);

    g.setColor(BlueSagaColors.RED);
    g.fillRoundRect(X + 5, Y + 5, Width - 10, Height - 10, 8);

    int i = 0;
    for (Color c : Colors) {
      int row = (int) Math.floor(((float) i / 8.0f));

      if (mouseX > X + 10 + i * 40
          && mouseX < X + 10 + i * 40 + 30
          && mouseY > Y + 10 + row * 50
          && mouseY < Y + 10 + row * 50 + 30) {
        g.setColor(COL_PICKER);
        g.fillRoundRect(X + 8 + i * 40, Y + 8 + row * 50, 34, 34, 10);
      }

      g.setColor(c);
      g.fillRoundRect(X + 10 + i * 40, Y + 10 + row * 50, 30, 30, 8);
      i++;
    }
  }
}
