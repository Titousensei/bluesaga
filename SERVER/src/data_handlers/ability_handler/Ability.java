package data_handlers.ability_handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.newdawn.slick.Color;

import utils.ServerGameInfo;
import creature.Creature;
import creature.Creature.CreatureType;

public class Ability {

  public static final Vector<StatusEffect> EMPTY_SE = new Vector<StatusEffect>(0);

  public static final String TARGET_TILE = "tile";
  public static final String TARGET_CREATURE = "creature";

  public final int id;
  public final String name;
  public final String origin;

  private int dbId; // Id in character_ability table
  private Color abilityColor;
  private int ManaCost;

  private int animationId = 0;

  private int GraphicsNr = 0;

  private int Cooldown = 0;
  private int CooldownLeft = 0;

  private int CastingSpeed = 100;

  private boolean Ready = true;
  private boolean ReadySent = true;

  private String Description = null;

  private int ClassId = 0;
  private int classLevel = 0;

  private int jobSkillId = 0;

  private int FamilyId = 0;

  private String AoE = "0,0";
  private int Damage = 0;
  private String DamageType = "None";
  private int Range = 0;
  private boolean Instant;
  private int Price = 0;
  private String TargetType;
  private boolean TargetSelf = false;
  private Vector<StatusEffect> StatusEffects = EMPTY_SE;
  private float WeaponDamageFactor = 0.0f;

  private int Delay = 0;

  private String EquipReq = null;

  private String SpawnIds = null;

  private int ProjectileId = 0;
  private int projectileEffectId = 0;

  private boolean buffOrNot = false;

  // INFO ABOUT THE CASTER OF THE ABILITY
  private Creature Caster;

  Ability(int id, String name, String origin) {
    this.id = id;
    this.name = name;
    this.origin = origin;
  }

  public Ability(Ability copy) {
    id = copy.id;
    name = copy.name;
    origin = copy.origin;

    abilityColor = copy.getColor();

    animationId = copy.getAnimationId();

    ManaCost = copy.getManaCost();

    Cooldown = copy.getCooldown();
    CooldownLeft = 0;
    Ready = true;
    ReadySent = true;

    AoE = copy.getAoE();
    Damage = copy.getDamage();
    DamageType = copy.getDamageType();
    Range = copy.getRange();

    setDelay(copy.getDelay());

    WeaponDamageFactor = copy.getWeaponDamageFactor();

    setProjectileId(copy.getProjectileId());

    setInstant(copy.isInstant());

    setClassId(copy.getClassId());
    setFamilyId(copy.getFamilyId());

    jobSkillId = copy.getJobSkillId();

    Price = copy.getPrice();
    TargetType = copy.getTargetType();

    TargetSelf = copy.isTargetSelf();

    StatusEffects = copy.getStatusEffects();

    EquipReq = copy.getEquipReq();

    setGraphicsNr(copy.getGraphicsNr());

    setSpawnIds(copy.getSpawnIds());

    setDescription(copy.getDescription());

    setCastingSpeed(copy.getCastingSpeed());

    setClassLevel(copy.getClassLevel());

    setProjectileEffectId(copy.getProjectileEffectId());

    setBuffOrNot(copy.isBuffOrNot());
  }

  /*
   *
   *
   *   COOLDOWN
   *
   *
   */

  public void used() {
    CooldownLeft = Cooldown * 5;
    Ready = false;
    ReadySent = false;
  }

  public int getCooldown() {
    return Cooldown;
  }

  public void setCooldown(int value) {
    Cooldown = value;
  }

  public boolean isReady() {
    return Ready;
  }

  public boolean checkReady() {
    if (CooldownLeft > 0) {
      CooldownLeft--;
    } else if (!Ready) {
      CooldownLeft = 0;
      Ready = true;
    }
    if (Ready && !ReadySent) {
      ReadySent = true;
      return true;
    }
    return false;
  }

  /*
   *
   *
   *    GETTERS AND SETTERS
   *
   *
   */

  public void setDbId(int newDbId) {
    dbId = newDbId;
  }

  public int getDbId() {
    return dbId;
  }

  public int getDamage() {
    return Damage;
  }

  public void setDamage(int value) {
    Damage = value;
  }

  public void setCaster(CreatureType newCasterType, Creature newCaster) {
    Caster = newCaster;

    for (Iterator<StatusEffect> iter = StatusEffects.iterator(); iter.hasNext(); ) {
      StatusEffect s = iter.next();
      s.setCaster(Caster);
    }
  }

  public Color getColor() {
    return abilityColor;
  }

  // Color: 129,172,79
  void setColor(String val)
  {
    String[] colorRGB;
    if ("0,0,0".equals(val)
    && ServerGameInfo.classDef != null
    && ServerGameInfo.classDef.get(getClassId()) != null) {
      colorRGB = ServerGameInfo.classDef.get(getClassId()).bgColor.split(",");
    }
    else {
      colorRGB = val.split(",");
    }
    abilityColor =
        new Color(
            Integer.parseInt(colorRGB[0]),
            Integer.parseInt(colorRGB[1]),
            Integer.parseInt(colorRGB[2]),
            255);
  }

  public Color getColorAlpha(int alpha) {
    Color alphaColor =
        new Color(abilityColor.getRed(), abilityColor.getGreen(), abilityColor.getBlue(), alpha);
    return alphaColor;
  }

  public void setInstant(boolean instant) {
    Instant = instant;
  }

  public boolean isInstant() {
    return Instant;
  }

  public String getAoE() {
    return AoE;
  }

  public void setAoE(String value) {
    AoE = value;
  }

  public int getManaCost() {
    return ManaCost;
  }

  public void setManaCost(int manaCost) {
    this.ManaCost = manaCost;
  }

  public Vector<StatusEffect> getStatusEffects() {
    return StatusEffects;
  }

  public void setStatusEffects(Vector<StatusEffect> value) {
    StatusEffects = value;
  }

  public int getRange() {
    return Range;
  }

  public void setRange(int value) {
    Range = value;
  }

  public void setCooldownLeft(int newCooldownLeft) {
    CooldownLeft = newCooldownLeft;
    if (CooldownLeft > 0) {
      Ready = false;
      ReadySent = false;
    }
  }

  public int getCooldownLeft() {
    return CooldownLeft;
  }

  public String getTargetType() {
    return TargetType;
  }

  public void setTargetType(String value) {
    TargetType = value;
  }

  public boolean isTargetSelf() {
    return TargetSelf;
  }

  public void setTargetSelf(boolean value) {
    TargetSelf = value;
  }

  public Creature getCaster() {
    return Caster;
  }

  public void setCaster(Creature caster) {
    Caster = caster;
    for (Iterator<StatusEffect> iter = StatusEffects.iterator(); iter.hasNext(); ) {
      StatusEffect SE = iter.next();
      SE.setCaster(Caster);
    }
  }

  public String getDamageType() {
    return DamageType;
  }

  public void setDamageType(String damageType) {
    DamageType = damageType;
  }

  public int getPrice() {
    return Price;
  }

  public void setPrice(int value) {
    Price = value;
  }

  public float getWeaponDamageFactor() {
    return WeaponDamageFactor;
  }

  public void setWeaponDamageFactor(float weaponDamageFactor) {
    WeaponDamageFactor = weaponDamageFactor;
  }

  public String getEquipReq() {
    return EquipReq;
  }

  public void setEquipReq(String value) {
    EquipReq = value;
  }

  public int getProjectileId() {
    return ProjectileId;
  }

  public void setProjectileId(int projectileId) {
    ProjectileId = projectileId;
  }

  public int getGraphicsNr() {
    return GraphicsNr;
  }

  public void setGraphicsNr(int graphicsNr) {
    GraphicsNr = graphicsNr;
  }

  public String getSpawnIds() {
    return SpawnIds;
  }

  public void setSpawnIds(String spawnIds) {
    SpawnIds = spawnIds;
  }

  public int getDelay() {
    return Delay;
  }

  public void setDelay(int delay) {
    Delay = delay;
  }

  public int getClassId() {
    return ClassId;
  }

  public void setClassId(int classId) {
    ClassId = classId;
  }

  public int getFamilyId() {
    return FamilyId;
  }

  public void setFamilyId(int familyId) {
    FamilyId = familyId;
  }

  public int getClassLevel() {
    return classLevel;
  }

  public void setClassLevel(int classLevel) {
    this.classLevel = classLevel;
  }

  public String getDescription() {
    return Description;
  }

  public void setDescription(String description) {
    Description = description;
  }

  public int getAnimationId() {
    return animationId;
  }

  public void setAnimationId(int animationId) {
    this.animationId = animationId;
  }

  public int getCastingSpeed() {
    return CastingSpeed;
  }

  public void setCastingSpeed(int castingSpeed) {
    CastingSpeed = castingSpeed;
  }

  public int getJobSkillId() {
    return jobSkillId;
  }

  public void setJobSkillId(int value) {
    jobSkillId = value;
  }

  public int getProjectileEffectId() {
    return projectileEffectId;
  }

  public void setProjectileEffectId(int projectileEffectId) {
    this.projectileEffectId = projectileEffectId;
  }

  public boolean isBuffOrNot() {
    return buffOrNot;
  }

  public void setBuffOrNot(boolean buffOrNot) {
    this.buffOrNot = buffOrNot;
  }

  @Override
  public String toString() {
    return id + ":" + name;
  }
}
