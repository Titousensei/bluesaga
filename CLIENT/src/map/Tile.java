package map;

import java.util.*;

import game.BlueSaga;
import game.ClientSettings;
import graphics.ImageResource;
import graphics.Sprite;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import abilitysystem.StatusEffect;
import creature.Creature.CreatureType;
import utils.RandomUtils;

public class Tile {
  private int X;
  private int Y;
  private int Z;

  private String Type;
  private String Name;
  private boolean Passable;
  private boolean OnlyD;
  private boolean OnlyU;
  private boolean OnlyL;
  private boolean OnlyR;

  private boolean Transparent;

  private Sprite graphics;

  private boolean DeadBody = false;
  private int DeadBodyNr = 1;

  private CreatureType OccupantType;
  private int OccupantId;

  private boolean Soul;

  private boolean MonsterLocked;

  // DOOR
  private int doorId;
  private int graphicsNr;
  private int areaId;
  private int entranceX;
  private int entranceY;

  // ABILITY EFFECT
  // STATUSEFFCT
  private List<StatusEffect> StatusEffects;

  private int hitByAbilityItr = 0;

  // SHOW WALK TO INDICATOR
  private Animation walkToIcon;
  private boolean ShowWalkTo;
  private int showWalkToTimer = 0;

  // SHOW EVADE FLASH
  private boolean ShowEvadeFlash;
  private int ShowEvadeFade = 0;
  private int ShowEvadeFadeDelay = 0;

  public Tile(int x, int y, int z) {
    X = x;
    Y = y;
    Z = z;
    graphics = null;
    Type = "None";
    Name = "None";
    MonsterLocked = false;
  }

  public void setType(String type, String name) {

    if (!Type.equals(type) || !Name.equals(name)) {
      Type = type;
      Name = name;
      graphics = ImageResource.getSprite("textures/" + Type + "/" + Name);
    }

    Soul = false;

    ShowWalkTo = false;

    OccupantType = CreatureType.None;
    OccupantId = 0;

    Passable = true;

    if (name.equals("UL")
        || name.equals("U")
        || name.equals("UR")
        || name.equals("R")
        || name.equals("DR")
        || name.equals("D")
        || name.equals("DL")
        || name.equals("L")
        || name.equals("IUR")
        || name.equals("IUL")
        || name.equals("IDR")
        || name.equals("IDL")) {
      setTransparent(true);
    } else {
      setTransparent(false);
    }

    if ("cliff".equals(Type)) {
      OnlyD = name.contains("Entranced") || name.contains("Exitu") || name.contains("Onlyd");
      OnlyU = name.contains("Entranceu") || name.contains("Exitd") || name.contains("Onlyu");
      OnlyL = name.contains("Entrancel") || name.contains("Exitr") || name.contains("Onlyl");
      OnlyR = name.contains("Entrancer") || name.contains("Exitl") || name.contains("Onlyr");
    } else {
      OnlyD = name.contains("Entranceu") || name.contains("Exitu") || name.contains("Onlyd");
      OnlyU = name.contains("Entranced") || name.contains("Exitd") || name.contains("Onlyu");
      OnlyL = name.contains("Entrancer") || name.contains("Exitr") || name.contains("Onlyl");
      OnlyR = name.contains("Entrancel") || name.contains("Exitl") || name.contains("Onlyr");
    }

    StatusEffects = new ArrayList<StatusEffect>(30);

    walkToIcon = ImageResource.getSprite("gui/world/walkToIcon").getAnimation();
  }

  public void restartAnimation() {
    if (graphics.isAnimated()) {
      graphics.getAnimation().restart();
    }
  }

  @SuppressWarnings("deprecation")
  public void updateAnimation() {
    if (graphics.isAnimated()) {
      graphics.getAnimation().updateNoDraw();
    }
  }

  public void showEvadeFlash() {
    ShowEvadeFlash = true;
    ShowEvadeFade = 0;
    ShowEvadeFadeDelay = 0;
  }

  public void draw(int x, int y, Graphics g) {
    if (!Type.equals("none")) {

      if (graphics != null) {
        graphics.draw(x, y, ScreenHandler.AREA_EFFECT.getTintColor());
      }

      if (DeadBody) {
        ImageResource.getSprite("creatures/m_dead" + DeadBodyNr).draw(x, y);
      }

      if (ShowWalkTo) {
        showWalkToTimer--;
        if (showWalkToTimer <= 0) {
          ShowWalkTo = false;
        }
        walkToIcon.draw(x, y, new Color(255, 255, 255, showWalkToTimer * 2 - 50));
      }

      for (StatusEffect s : StatusEffects) {
        s.draw(x + 25, y + 25);
      }

      if (hitByAbilityItr > 0) {
        g.setColor(new Color(255, 255, 255, hitByAbilityItr));
        g.fillRect(x, y, 50, 50);
        hitByAbilityItr -= 4;
      }

      if (ShowEvadeFlash) {
        if (ShowEvadeFadeDelay < 30) {
          ShowEvadeFadeDelay++;
        } else {
          if (ShowEvadeFade < 100) {
            ShowEvadeFade += 4;
          } else {
            ShowEvadeFade = 100;
            ShowEvadeFlash = false;
          }
        }
      } else {
        if (ShowEvadeFade > 0) {
          ShowEvadeFade -= 4;
        } else {
          ShowEvadeFade = 0;
        }
      }
      if (ShowEvadeFade > 0) {
        g.setColor(new Color(255, 255, 255, ShowEvadeFade));
        g.fillRect(x, y, 50, 50);
      }

      if (isMonsterLocked()) {
        ImageResource.getSprite("objects/special/locked").draw(x, y);
      }

      if (Soul) {
        ImageResource.getSprite("objects/special/soul").draw(x, y);
      }
    }
    //    if(dustEmitter != null && !dustEmitter.ShouldBeRemoved()){
    //      dustEmitter.SetPositionHard(x + 25, y + 40);
    //    }else if(dustEmitter != null && dustEmitter.ShouldBeRemoved()){
    //      dustEmitter = null;
    //    }

  }

  public void move(int dirX, int dirY, int speed) {}

  /****************************************
   *                                      *
   *         STATUSEFFECTS        *
   *                                      *
   *                                      *
   ****************************************/
  public void addStatusEffect(StatusEffect newSE) {
    if (!StatusEffects.contains(newSE)) {
      StatusEffects.add(newSE);
    }
  }

  public void addStatusEffects(List<StatusEffect> newStatusEffects) {
    for (StatusEffect s : newStatusEffects) {
      if (!StatusEffects.contains(s)) {
        StatusEffects.add(s);
      }
    }
  }

  public void removeStatusEffect(int sId) {
    List<StatusEffect> newStatusEffects = new ArrayList<StatusEffect>(30);
    for (StatusEffect s : StatusEffects) {
      if (s.getId() == sId) {
        //REMOVE SE
      } else {
        newStatusEffects.add(s);
      }
    }
    StatusEffects.clear();
    StatusEffects.addAll(newStatusEffects);
  }

  /****************************************
   *                                      *
   *         OCCUPANT                     *
   *                                      *
   ****************************************/
  public int getOccupantId() {
    return OccupantId;
  }

  public CreatureType getOccupantType() {
    return OccupantType;
  }

  public void setOccupant(CreatureType oType, int oId) {
    OccupantType = oType;
    OccupantId = oId;
  }

  /****************************************
   *                                      *
   *         SHOW DUST                    *
   *                                      *
   ****************************************/
  public void showDust(int x, int y) {

    if (ScreenHandler.getActiveScreen() != ScreenType.LOADING) {
      String dustType = Type;
      if (Type.contains("water")) {
        dustType = "shallow";
      }

      if (dustType.equals("snow")
          || dustType.equals("grass")
          || dustType.equals("beach")
          || dustType.equals("shallow")) {
        ScreenHandler.myEmitterManager.SpawnEmitter(x, y, "dust_" + dustType);
      }
    }
  }

  public int getGraphicsNr() {
    return graphicsNr;
  }

  public int getAreaId() {
    return areaId;
  }

  public int getEntranceX() {
    return entranceX;
  }

  public int getEntranceY() {
    return entranceY;
  }

  /****************************************
   *                                      *
   *         GETTER / SETTER              *
   *                                      *
   ****************************************/
  public void hitByAbility() {
    hitByAbilityItr = 200;
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

  public int getDoorId() {
    return doorId;
  }

  public boolean getPassable() {
    return Passable;
  }

  public boolean isPassableFromDir(int dirX, int dirY) {
    if (OnlyD) {
      return (dirY < 0);
    }
    if (OnlyU) {
      return (dirY > 0);
    }
    if (OnlyL) {
      return (dirX > 0);
    }
    if (OnlyR) {
      return (dirX < 0);
    }
    return true;
  }

  public void showWalkTo() {
    if (!ShowWalkTo) {
      ShowWalkTo = true;
      showWalkToTimer = 100;
    }
  }

  public int getPixelX() {
    return X * ClientSettings.TILE_SIZE;
  }

  public int getPixelY() {
    return Y * ClientSettings.TILE_SIZE;
  }

  public boolean getSoul() {
    return Soul;
  }

  public void setSoul(boolean newSoul) {
    Soul = newSoul;
  }

  public boolean isDeadBody() {
    return DeadBody;
  }

  public void setDeadBody(boolean deadBody) {
    if (deadBody) {
      DeadBodyNr = RandomUtils.getInt(1, 3);
    }
    DeadBody = deadBody;
  }

  public int getX() {
    return X;
  }

  public int getY() {
    return Y;
  }

  public int getZ() {
    return Z;
  }

  public void setPassable(boolean newPassable) {
    Passable = newPassable;
  }

  public boolean isPassable() {
    if (Z != BlueSaga.playerCharacter.getZ()) {
      return false;
    }

    if (OccupantType != CreatureType.None) {
      if (OccupantType == CreatureType.Player && Type.equals("indoors")) {
        return true;
      }
      return false;
    }
    return Passable;
  }

  public boolean isPassableType() {
    return Passable;
  }

  public boolean isTransparent() {
    return Transparent;
  }

  public void setTransparent(boolean transparent) {
    Transparent = transparent;
  }

  public boolean isMonsterLocked() {
    return MonsterLocked;
  }

  public void setMonsterLocked(boolean monsterLocked) {
    MonsterLocked = monsterLocked;
  }

  public StatusEffect getStatusEffect(int sId) {
    for (StatusEffect se : StatusEffects) {
      if (se.getId() == sId) {
        return se;
      }
    }
    return null;
  }
}
