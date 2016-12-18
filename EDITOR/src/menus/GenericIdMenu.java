package menus;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.gui.TextField;

import game.BP_EDITOR;

public class GenericIdMenu extends BaseMenu {

  private TextField nrField;
  private String value = null;
  private String title = "ID";

  private int startX = 400;
  private int startY = 350;

  public GenericIdMenu(GameContainer app, String title) {
    nrField = new TextField(app, BP_EDITOR.FONTS.size12, startX + 18, startY + 45, 150, 20);
    nrField.setBackgroundColor(new Color(0, 0, 0, 80));
    nrField.setBorderColor(new Color(0, 0, 0, 0));
    nrField.setFocus(false);
    nrField.setCursorVisible(true);
    this.title = title;
  }

  @Override
  public void draw(Graphics g, GameContainer app, int mouseX, int mouseY) {
    g.setColor(new Color(238, 82, 65, 255));
    g.fillRect(startX, startY, 213, 150);

    g.setColor(new Color(255, 255, 255, 255));

    g.drawString(title, startX + 20, startY + 20);
    nrField.render(app, g);
  }

  @Override
  public void clear() {
    nrField.setText("");
    nrField.setFocus(true);
    value = null;
  }

  @Override
  public String getValue() { return value; }

  @Override
  public boolean keyLogic(Input INPUT) {
    if (INPUT.isKeyPressed(Input.KEY_ESCAPE)) {
      return false;
    }

    if (INPUT.isKeyPressed(Input.KEY_ENTER) || INPUT.isKeyPressed(Input.KEY_NUMPADENTER)) {
      value = nrField.getText();
System.out.println("+++ " + value);
      if ("".equals(value)) {
        value = null;
      }
      return false;
    }
    return true;
  }
}
