package data_handlers.ability_handler;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import components.Stats;

import creature.Creature;
import network.Server;

public class StatusEffect
{
  public final static Set<String> VALID_STATS = new HashSet<>(Arrays.asList(
     "ACCURACY", "AGILITY" ,"ARMOR", "ATTACKSPEED", "CRITICAL_HIT", "EVASION",
     "INTELLIGENCE", "SPEED", "STRENGTH",
     "CHEMS_DEF", "COLD_DEF", "FIRE_DEF", "MAGIC_DEF", "MIND_DEF", "SHOCK_DEF",
     "MAX_HEALTH", "MAX_MANA"
  ));

  public final int id;
  public final String name;
  public final String origin;

  private Stats statsModif;
  private int Duration; // Duration in seconds
  private int RepeatDamage;
  private String RepeatDamageType;
  private boolean isStackable_ = false;

  private int GraphicsNr;
  private int AnimationId;

  private String Sfx;

  private Ability ability;

  private Creature Caster;

  private String SEColor;
  private int classId;

  private int ActiveTimeEnd;

  StatusEffect(int id, String name, String origin) {
    this.id = id;
    this.name = name;
    this.origin = origin;
  }

  public StatusEffect(StatusEffect copy) {
    id = copy.id;
    name = copy.name;
    origin = copy.origin;

    ability = null;
    Caster = null;

    if (copy.getStatsModif() != null) {
      setStatsModif(new Stats(copy.getStatsModif().getHashMap()));
    }
    setGraphicsNr(copy.getGraphicsNr());
    setAnimationId(copy.getAnimationId());

    setDuration(copy.getDuration());
    setRepeatDamage(copy.getRepeatDamage());
    setStackable(copy.isStackable());
    setRepeatDamageType(copy.getRepeatDamageType());
    SEColor = copy.getColor();
    setClassId(copy.getClassId());
    setSfx(copy.getSfx());

    ActiveTimeEnd = Duration;
  }

  public void start() {
    ActiveTimeEnd = Duration;
  }

  public int getDuration() {
    return Duration;
  }

  public void setDuration(int duration) {
    Duration = duration;
  }

  public int getRepeatDamage() {
    return RepeatDamage;
  }

  public void setRepeatDamage(int repeatDamage) {
    RepeatDamage = repeatDamage;
  }

  public String getRepeatDamageType() {
    return RepeatDamageType;
  }

  public void setRepeatDamageType(String repeatDamageType) {
    RepeatDamageType = repeatDamageType;
  }

  public Stats getStatsModif() {
    return statsModif;
  }

  void setStatsModif(Stats s) {
    statsModif = s;
  }

  public boolean isActive() {
    if (ActiveTimeEnd > 0) {
      -- ActiveTimeEnd;
      return true;
    }
    return false;
  }

  public boolean isStackable() { return isStackable_; }
  public void setStackable(boolean value) { isStackable_ = value; }

  public void deactivate() {
    ActiveTimeEnd = 0;
  }

  public Creature getCaster() {
    return Caster;
  }

  public void setCaster(Creature caster) {
    Caster = caster;
  }

  public String getColor() {
    return SEColor;
  }

  void setColor(String col) {
    SEColor = col;
  }

  public int getClassId() {
    return classId;
  }

  public void setClassId(int classId) {
    this.classId = classId;
  }

  public int getGraphicsNr() {
    return GraphicsNr;
  }

  public void setGraphicsNr(int graphicsNr) {
    GraphicsNr = graphicsNr;
  }

  public int getAnimationId() {
    return AnimationId;
  }

  public void setAnimationId(int animationId) {
    AnimationId = animationId;
  }

  public String getSfx() {
    return Sfx;
  }

  public void setSfx(String sfx) {
    Sfx = sfx;
  }

  public Ability getAbility() {
    return ability;
  }

  public void setAbility(Ability ability) {
    this.ability = ability;
  }

  @Override
  public String toString() {
    if (statsModif!=null) {
      return "StatusEffect[" + name + "]->" + statsModif;
    }
    else {
      return "StatusEffect[" + name + "]";
    }
  }
}
