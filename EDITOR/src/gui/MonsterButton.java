package gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import components.Monster;
import game.EditColors;

public class MonsterButton {

  private int X = 0;
  private int Y = 0;
  private int width = 40;
  private int height = 32;

  private Monster MONSTER;

  public MonsterButton(int x, int y, Monster newMonster) {
    X = x;
    Y = y;

    MONSTER = newMonster;

    if (MONSTER == null) {
      width = 50;
      height = 50;
    }
  }

  public boolean clicked(int mouseX, int mouseY) {
    if (mouseX > X && mouseX < X + width && mouseY > Y && mouseY < Y + height) {
      return true;
    }
    return false;
  }

  public void draw(Graphics g, int mouseX, int mouseY) {
    if (MONSTER != null) {
      g.setColor(EditColors.WHITE);
      MONSTER.drawSmall(g, X, Y);
    }

    if (X < mouseX && X + width > mouseX && Y < mouseY && Y + height > mouseY) {
      g.setColor(EditColors.TRANSPARENT);
      g.fillRect(X, Y, width, height);
    }
  }

  public Monster getMonster() {
    return MONSTER;
  }
}
