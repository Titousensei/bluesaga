package map;

import org.newdawn.slick.Graphics;

import creature.Creature;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;

public class ScreenObject {
  private Creature myCreature = null;
  private TileObject myObject = null;

  public void setObjectCreature(Creature newCreature) {
    myCreature = newCreature;
    myObject = null;
  }

  public void setObjectCreature(TileObject newObject) {
    myObject = newObject;
    myCreature = null;
  }

  public int getX() {
    if (myObject != null) {
      return myObject.getX();
    } else {
      return myCreature.getX();
    }
  }

  public int getY() {
    if (myObject != null) {
      return myObject.getY();
    } else {
      return myCreature.getY();
    }
  }

  public int getZ() {
    if (myObject != null) {
      return myObject.getZ();
    } else {
      return myCreature.getZ();
    }
  }

  public boolean isObject() {
    return myObject != null;
  }

  public boolean isCreature() {
    return myCreature != null;
  }

  public Creature getCreature() {
    return myCreature;
  }

  public TileObject getObject() {
    return myObject;
  }

  public void clear() {
    myCreature = null;
    myObject = null;
  }

  public void draw(Graphics g, int x, int y) {
    if (myObject != null) {  // 20%
      myObject.draw(g, x, y);
    } else if (myCreature != null) {  // 5%
      myCreature.draw(g, x, y);
    }
  }
}
