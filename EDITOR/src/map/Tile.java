package map;

import game.BP_EDITOR;
import game.EditColors;
import gui.Font;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import components.Creature;
import graphics.Sprite;
import utils.Coords;

public class Tile {
  private String id;
  private int X;
  private int Y;
  private int Z;

  private String Type = "none";
  private String Name = "none";
  private boolean Passable;

  private boolean Animated;

  private Creature Occupant;

  private String AreaEffectId = null;
  private String doorId = null;
  private String destId = null;
  private String lockId = null;
  private String TriggerId = null;
  private String TrapId = null;

  private Coords doorCoords = null;
  private Coords destCoords = null;

  private boolean MENU = false;

  // CONTAINER
  private boolean LootBag = false;


  public Tile(int x, int y, int z) {
    X = x;
    Y = y;
    setZ(z);
  }

  public boolean setType(String type, String newName) {
    boolean changed = true;

    if (newName.contains("_")) {
      String fileName[] = newName.split("_");
      newName = fileName[0];
    }
    TriggerId = null;
    AreaEffectId = null;

    if (!type.equals("none")
        && BP_EDITOR.GFX.getSprite("textures/" + type + "/" + newName) == null) {
      changed = false;
    } else {
      if (Type.equals(type) && Name.equals(newName)) {
        changed = false;
      } else {
        Type = type;
        Name = newName;

        Occupant = null;

        //isDoor = false;

        Passable = isTilePassable();

        if (Type.equals("none")) {
          Animated = false;
        } else {
          if (BP_EDITOR.GFX.getSprite("textures/" + Type + "/" + Name).isAnimated()) {
            Animated = true;
          } else {
            Animated = false;
          }
        }
      }
    }

    setMENU(false);
    return changed;
  }

  public void restartAnimation() {
    if (BP_EDITOR.GFX.getSprite("textures/" + Type + "/" + Name).isAnimated()) {
      BP_EDITOR.GFX.getSprite("textures/" + Type + "/" + Name).getAnimation().restart();
    }
  }

  public void updateAnimation() {
    if (BP_EDITOR.GFX.getSprite("textures/" + Type + "/" + Name).isAnimated()) {
      BP_EDITOR.GFX.getSprite("textures/" + Type + "/" + Name).getAnimation().updateNoDraw();
    }
  }

  public boolean exists() {
    return BP_EDITOR
          .GFX
          .getSprite("textures/" + Type + "/" + Name) != null;
  }

  public void draw(Graphics g, int x, int y) {

    if (!Type.equals("none")) {
      Sprite t = BP_EDITOR.GFX.getSprite("textures/" + Type + "/" + Name);
      if (t!=null) {
        t.draw(x, y, EditColors.WHITE);
      }
      else {
        System.out.println("WARNING - MISSING textures/" + Type + "/" + Name);
      }
    }
  }

  public void drawOverlay(Graphics g, int x, int y) {

    if (!"none".equals(Type) && Z == BP_EDITOR.PLAYER_Z) {
      if (doorId != null) {
        if (doorCoords != null) {
          g.setColor(EditColors.DEST_BG);
        } else {
          g.setColor(EditColors.DOOR_BG);
        }
        g.fillRect(x, y, 50, 50);
        g.setFont(Font.size12);
        g.setColor(EditColors.BLACK);
        g.drawString(doorId, x + 3, y + 3);
        g.setColor(EditColors.DOOR_TXT);
        g.drawString(doorId, x + 2, y + 2);
        if (destId!=null) {
          String destStr = destId + ">";
          g.setColor(EditColors.BLACK);
          g.drawString(destStr, x + 3, y + 31);
          g.setColor(EditColors.DOOR_TXT);
          g.drawString(destStr, x + 2, y + 30);
        }
        else if (lockId!=null) {
          String destStr = "k " + lockId;
          g.setColor(EditColors.BLACK);
          g.drawString(destStr, x + 3, y + 31);
          g.setColor(EditColors.LOCK_TXT);
          g.drawString(destStr, x + 2, y + 30);
        }
      } else if (destId != null) {
        g.setColor(EditColors.DEST_BG);
        g.fillRect(x, y, 50, 50);
        g.setFont(Font.size12);
        String destStr = destId + ">";
        g.setColor(EditColors.BLACK);
        g.drawString(destStr, x + 3, y + 31);
        g.setColor(EditColors.DEST_TXT);
        g.drawString(destStr, x + 2, y + 30);
      }
    }

    if (Occupant != null && Z == BP_EDITOR.PLAYER_Z) {
      Occupant.draw(g, x, y);
    }

    if (BP_EDITOR.SHOW_PASSABLE==1 && !Passable && !Type.equals("none") && !MENU) {
      g.setColor(EditColors.IMPASSABLE);
      g.fillRect(x, y, 50, 50);
    }
    else if (BP_EDITOR.SHOW_PASSABLE==2 && !Passable && !Type.equals("none") && !MENU) {
      g.setColor(EditColors.IMPASSABLE2);
      g.fillRect(x, y, 50, 50);
    }

    if (BP_EDITOR.PLAYER_Z == Z && AreaEffectId != null) {
      g.setColor(EditColors.AREA_EFFECT);
      g.drawRect(x, y, 49, 49);
      g.setFont(Font.size12bold);
      g.setColor(EditColors.BLACK);
      g.drawString(AreaEffectId, x + 21, y + 3);
      g.setColor(EditColors.AREA_EFFECT);
      g.drawString(AreaEffectId, x + 20, y + 2);
    }

    if (TriggerId != null) {
      g.setColor(EditColors.TRIGGER);
      g.drawRect(x, y, 49, 49);
      g.setFont(Font.size12bold);
      g.setColor(EditColors.BLACK);
      g.drawString(">" + TrapId, x + 3, y + 3);
      g.setColor(EditColors.TRIGGER);
      g.drawString(">" + TrapId, x + 2, y + 2);
    }
  }

  /****************************************
   *                                      *
   *         GETTER / SETTER              *
   *                                      *
   *                                      *
   ****************************************/
  public void setLootBag(boolean state) {
    LootBag = state;
  }

  public boolean getLootBag() {
    return LootBag;
  }

  public boolean isAnimated() {
    return Animated;
  }

  public void setOccupant(Creature m) {
    Occupant = m;
  }

  public Creature getOccupant() {
    return Occupant;
  }

  public String getName() {
    return Name;
  }

  public void setName(String newName) {
    Name = newName;
  }

  public String getType() {
    return Type;
  }

  public String getDoorId() {
    return doorId;
  }

  public String getDestId() {
    return destId;
  }

  public Coords getDoorCoords() {
    return doorCoords;
  }

  public Coords getDestCoords() {
    return destCoords;
  }

  public void setDoorCoords(Coords value) {
    doorCoords = value;
  }

  public void setDestCoords(Coords value) {
    if (value !=null && value.x != 0 && value.y != 0 && value.z != 0) {
      destCoords = value;
    }
    else {
      destCoords = null;
    }
  }

  public String getAreaEffectId() {
    return AreaEffectId;
  }

  public String getTrapId() {
    return TrapId;
  }

  public boolean isPassable() {
    return Passable;
  }

  public void setPassable(boolean newPassable) {
    Passable = newPassable;
  }

  public int getX() {
    return X;
  }

  public int getY() {
    return Y;
  }

  public void setDoorId(String id) {
    if (id == null || "0".equals(id)) {
      doorId = null;
    }
    else {
      doorId = id;
    }
  }

  public void setDestId(String id) {
    if (id == null || "0".equals(id)) {
      destId = null;
    }
    else {
      destId = id;
    }
  }

  public void setLockId(String id) {
    if (id == null || "0".equals(id)) {
      lockId = null;
    }
    else {
      lockId = id;
    }
  }

  public int getZ() {
    return Z;
  }

  public void setZ(int z) {
    Z = z;
  }

  public void setId(String value) {
    id = value;
  }

  public String getId() {
    return id;
  }

  public boolean isMENU() {
    return MENU;
  }

  public void setMENU(boolean value) {
    MENU = value;
  }

  public void setAreaEffectId(String id) {
    if (id == null || "0".equals(id)) {
      AreaEffectId = null;
    }
    else {
      AreaEffectId = id;
    }
  }

  public void setTrapId(String id) {
    TrapId = id;
  }

  public void setTriggerId(String id) {
    if (id == null || "0".equals(id)) {
      TriggerId = null;
    }
    else {
      TriggerId = id;
    }
  }

  public void clear() {
    setType("none", "none");
    setPassable(false);
    setOccupant(null);
    clearIds();
  }

  public void clearIds() {
    setTrapId(null);
    setTriggerId(null);

    setAreaEffectId(null);
    setDestCoords(null);
    setDestId(null);
    setLockId(null);
    setDoorCoords(null);
    setDoorId(null);
  }

  public static boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    }
    // only got here if we didn't return false
    return true;
  }

  public boolean isTilePassable() {

    if (Type.equals("none")) {
      return false;
    }

    if (Type.equals("beach")) {
      return true;
    }

    if (Name.contains("Stairs")) {
      return true;
    }

    if (Type.contains("patch")) {
      return true;
    }

    if (Name.contains("bridge")) {
      return true;
    }

    if (Type.equals("shallow")) {
      return true;
    }

    if (Type.equals("grass")
    && (Name.startsWith("sand")
      || Name.startsWith("stream")
    )
    ) {
      return true;
    }

    if (Type.equals("sand") && Name.startsWith("shallow")) {
      return true;
    }

    if (!isInteger(Name)) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return "textures/" + Type + "/" + Name;
  }
}
