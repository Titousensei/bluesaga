package menus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import components.Monster;
import game.BP_EDITOR;
import game.EditColors;
import gui.MonsterButton;

public class MonsterMenu extends BaseMenu {

  private Vector<MonsterButton> Buttons;
  private int X;
  private int Y;
  private Vector<Monster> MONSTERS;

  private Image deleteIcon;

  public MonsterMenu(int x, int y) {
    X = x;
    Y = y;

    Buttons = new Vector<MonsterButton>();
    MONSTERS = new Vector<Monster>();
    load();
  }

  private void load() {

    deleteIcon = BP_EDITOR.GFX.getSprite("gui/editor/deleteButton").getImage();

    Buttons.clear();

    int x = 0;
    int y = 0;

    Buttons.add(new MonsterButton(X + x, Y + y, null));
    x += 15;

    try (ResultSet rs = BP_EDITOR.gameDB.askDB(
        "select Id from creature order by Id asc")
    ) {
      while (rs.next()) {
        Monster newMonster = new Monster(rs.getInt("Id"), 0, 0, "no");
        MonsterButton newButton = new MonsterButton(X + x, Y + 55 + y, newMonster);

        Buttons.add(newButton);

        y += 32;
        if (y > 425) {
          x += 40;
          y = 0;
        }
      }
      rs.getStatement().close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void draw(Graphics g, GameContainer app, int mouseX, int mouseY) {
    g.setColor(EditColors.MENU);
    g.fillRect(X, Y, 500, 515);

    g.setColor(EditColors.WHITE);

    g.setFont(BP_EDITOR.FONTS.size12);
    MonsterButton hover = null;
    for (MonsterButton button : Buttons) {
      if (button.getMonster() == null) {
        deleteIcon.draw(X, Y);
      }
      button.draw(g, mouseX, mouseY);
      if (button.clicked(mouseX, mouseY)) {
        hover = button;
      }
    }

    if (hover!=null && hover.getMonster()!=null) {
      g.setColor(EditColors.WHITE);
      g.drawString(hover.getMonster().getName(), X+70, Y+10);
    }
  }

  public int click(int mouseX, int mouseY) {
    int buttonIndex = 0;
    for (MonsterButton button : Buttons) {
      if (button.clicked(mouseX, mouseY)) {
        if (button.getMonster() == null) {
          return 999;
        }
        return buttonIndex;
      }
      buttonIndex++;
    }
    return 1000;
  }

  public Monster getMonster(int tileIndex) {
    return Buttons.get(tileIndex).getMonster();
  }
}
