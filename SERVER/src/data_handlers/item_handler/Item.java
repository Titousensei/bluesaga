package data_handlers.item_handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import utils.ItemMagic;
import components.Stats;

import data_handlers.ability_handler.StatusEffect;
import utils.ServerGameInfo;
import network.Server;

public class Item {

  private int Id; // Id in item table
  private String Name;
  private String origin;
  private String Type;
  private String SubType;
  private String Material;
  private String Family;

  private String Description;

  private int dbId; // Id in user_item table
  private boolean Equipped;

  private boolean equipable;

  private boolean consumeable = false; // Potion, eatable, scroll, snowball etc

  private int Value;

  private int ClassId = 0;

  private String attackType;
  private String magicType;
  private String DamageStat;

  private int Range;

  private Stats Stats = new Stats();

  // SPECIAL ITEM STATS
  private Modifier modifier = Modifier.Regular;
  private int MagicId = 0;
  private int contentId = 0; // itemId where readable content is defined

  private HashMap<String, Integer> Requirements = new HashMap<String, Integer>();

  private int ProjectileId;

  private int Stackable;
  private int Stacked = 1;

  private int ContainerId = 0;

  private boolean Sellable;

  private boolean TwoHands;

  private int ScrollUseId = 0;

  private Vector<StatusEffect> statusEffects;

  private Map<String, Integer> info;

  public Item(int id, String name, String origin) {
    this.Id = id; // Id in item table
    this.Name = name;
    this.origin = origin;

    statusEffects = new Vector<StatusEffect>();
  }

  public Item(Item copy) {
    equipable = false;

    Stats.reset();
    statusEffects = new Vector<StatusEffect>();

    setId(copy.getRawId());
    setName(copy.getName());
    setType(copy.getType());
    setSubType(copy.getSubType());
    setMaterial(copy.getMaterial());

    setAttackType(copy.getAttackType());
    setMagicType(copy.getMagicType());

    for (Iterator<String> iter = copy.getStats().getHashMap().keySet().iterator();
        iter.hasNext();
        ) {
      String key = iter.next().toString();
      int value = copy.getStats().getHashMap().get(key);
      Stats.setValue(key, value);
    }

    setValue(copy.getValue());

    setDamageStat(copy.getDamageStat());

    setRange(copy.getRange());

    setContainerId(copy.getContainerId());
    setContentId(copy.getContentId());

    setProjectileId(copy.getProjectileId());

    setDescription(copy.getDescription());
    setInfo(copy.getInfo());
    setTwoHands(copy.isTwoHands());
    setClassId(copy.getClassId());

    setRequirements(copy.getRequirements());
    setEquipable(copy.isEquipable());

    setSellable(copy.isSellable());

    setStackable(copy.getStackable());

    setScrollUseId(copy.getScrollUseId());

    for (StatusEffect se : copy.getStatusEffects()) {
      statusEffects.add(ServerGameInfo.newStatusEffect(se.id));
    }

    if (getType().equals("Eatable")
        || getType().equals("Potion")
        || getType().equals("Scroll")
        || getType().equals("Material")) {
      setConsumeable(true);
    }
  }

  public void equip() {
    Equipped = true;
  }

  public void unEquip() {
    Equipped = false;
  }

  public int getDamage(String AttackType) {
    return Stats.getValue(AttackType);
  }

  /****************************************
   *                                      *
   *             GETTER/SETTER            *
   *                                      *
   *                                      *
   ****************************************/
  public Stats getStats() {
    return Stats;
  }

  public int getStatValue(String StatType) {
    int value = 0;
    if (Stats.getHashMap().containsKey(StatType)) {
      value = Stats.getValue(StatType);
    }
    return value;
  }

  public int getUserItemId() {
    return dbId;
  }

  public void setUserItemId(int newUserItemId) {
    dbId = newUserItemId;
  }

  public boolean isEquipped() {
    return Equipped;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public String getType() {
    return Type;
  }

  public void setType(String type) {
    Type = type;
  }

  public String getSubType() {
    return SubType;
  }

  public void setSubType(String subType) {
    SubType = subType;
  }

  public String getMaterial() {
    return Material;
  }

  public void setMaterial(String material) {
    Material = material;
  }

  public String getFamily() {
    return Family;
  }

  public void setFamily(String family) {
    Family = family;
  }

  public String getColor() {
    return modifier.color;
  }

  public void setId(int id) {
    Id = id;
  }

  public void setContentId(int id) {
    contentId = id;
  }

  public int getContentId() {
    return contentId;
  }

  public void setStoreBought(boolean yes) {
    Id = Math.abs(Id);
    if (yes) {
      Id = -Id;
    }
  }

  public int getId() {
    return Math.abs(Id);
  }

  public int getRawId() {
    return Id;
  }

  public int getValue() {
    int sp = (MagicId > 0) ? modifier.sp_magic : modifier.sp;
    return (sp > 0) ? Value * sp : Value;
  }

  public void setValue(int newvalue) {
    Value = newvalue;
  }

  public String getAttackType() {
    return attackType;
  }

  public String getMagicType() {
    return magicType;
  }

  public boolean isEquipable() {
    return equipable;
  }

  public int getRequirement(String Type) {
    Integer ret = Requirements.get(Type);

    if (ret == null) {
      return 0;
    }

    if ("ReqLevel".equals(Type)) {
      int lvl = ret.intValue();
      if (modifier.bonus > 0) {
        lvl += modifier.bonus;
      }
      else {
        lvl += modifier.bonus * 2;
        if (lvl<1) { lvl = 1; }
      }
      return lvl;
    }

    return ret.intValue();
  }

  private HashMap<String, Integer> getRequirements() {
    return Requirements;
  }

  public int getRange() {
    return Range;
  }

  public String getDamageStat() {
    return DamageStat;
  }

  public String getStatsAsString() {
    StringBuilder bonusStats = new StringBuilder(500);

    bonusStats
        .append(Stats.getValue("STRENGTH"))
        .append(',')
        .append(Stats.getValue("INTELLIGENCE"))
        .append(',')
        .append(Stats.getValue("AGILITY"))
        .append(',')
        .append(Stats.getValue("SPEED"))
        .append(',')
        .append(Stats.getValue("CRITICAL_HIT"))
        .append(',')
        .append(Stats.getValue("EVASION"))
        .append(',')
        .append(Stats.getValue("ACCURACY"))
        .append(',')
        .append(Stats.getValue("MAX_HEALTH"))
        .append(',')
        .append(Stats.getValue("MAX_MANA"))
        .append(',')
        .append(Stats.getValue("FIRE_DEF"))
        .append(',')
        .append(Stats.getValue("COLD_DEF"))
        .append(',')
        .append(Stats.getValue("SHOCK_DEF"))
        .append(',')
        .append(Stats.getValue("CHEMS_DEF"))
        .append(',')
        .append(Stats.getValue("MIND_DEF"))
        .append(',')
        .append(Stats.getValue("ARMOR"));

    return bonusStats.toString();
  }

  public int getProjectileId() {
    return ProjectileId;
  }

  public void setProjectileId(int projectileId) {
    ProjectileId = projectileId;
  }

  public int getStackable() {
    return Stackable;
  }

  public void setStackable(int stackable) {
    Stackable = stackable;
  }

  public int getStacked() {
    return Stacked;
  }

  public void setStacked(int stacked) {
    Stacked = stacked;
  }

  public int getContainerId() {
    return ContainerId;
  }

  public void setContainerId(int containerId) {
    ContainerId = containerId;
  }

  public boolean isSellable() {
    return Sellable;
  }

  public void setSellable(boolean sellable) {
    Sellable = sellable;
  }

  public boolean isOfType(String thatType, String thatSubType) {
    return thatType.equals(Type)
        && thatSubType.equals(SubType);
  }

  public boolean isTwoHands() {
    return TwoHands;
  }

  public void setTwoHands(boolean twoHands) {
    TwoHands = twoHands;
  }

  public int getModifierId() {
    return modifier.id;
  }

  public void setModifierId(int id) {
    Modifier mod = Modifier.get(id);
    setModifier(mod);
  }

  public void setModifier(Modifier mod) {

    modifier = mod;
    if (modifier != Modifier.Regular) {
      Name = modifier.toString() + ' ' + Name;

      for (Map.Entry<String, Integer> stat : Stats.getHashMap().entrySet()) {

        String statName = stat.getKey();
        int statValue = stat.getValue();

        if (statValue > 0) {
          float statBonus = modifier.bonus * Modifier.ITEM_COEF.get(getType());
          int newStat = Math.max(Math.round(statValue * (1.0f + statBonus)),
                                 statValue + modifier.bonus);
          if (newStat < 1) {
            newStat = 1;
          }
          Stats.setValue(statName, newStat);
        } else if (statValue < 0) {
          float statBonus = modifier.bonus * Modifier.ITEM_COEF.get(getType());
          int newStat = Math.min(Math.round(statValue * (1.0f - statBonus)),
                                 statValue + modifier.bonus);
          if (newStat > -1) {
            newStat = -1;
          }
          Stats.setValue(statName, newStat);
        }
      }
    }
  }

  public int getMagicId() {
    return MagicId;
  }

  public void setMagicId(int magicId) {
    MagicId = magicId;

    ItemMagic item = ItemMagic.getById(magicId);
    if (item != null) {
      int levelModif = 1;

      if (getRequirement("ReqLevel") > 0) {
        levelModif = getRequirement("ReqLevel");
      }

      float statBonus = levelModif * 0.25f;
      int statBonusInt = (int) Math.ceil(statBonus);

      if (getType().equals("Weapon") && item.statusEffectId > 0) {
        StatusEffect seEffect = ServerGameInfo.newStatusEffect(item.statusEffectId);
        seEffect.setRepeatDamage(statBonusInt);
        statusEffects.add(seEffect);
      } else {
        for(String bonus : item.bonus) {
          Stats.setValue(bonus, Stats.getValue(bonus) + statBonusInt);
        }
      }
      magicType = item.type;
    }
  }

  public int getScrollUseId() {
    return ScrollUseId;
  }

  public void setScrollUseId(int scrollUseId) {
    ScrollUseId = scrollUseId;
  }

  public int getSoldValue() {
    return (int) Math.floor(Value * modifier.priceCoef / 2.0f);
  }

  public String getDescription() {
    return Description;
  }

  public void setDescription(String description) {
    Description = description;
  }

  public void setInfo(Map<String, Integer> val) {
    info = val;
  }

  public Map<String, Integer> getInfo() {
    return info;
  }

  public int getClassId() {
    return ClassId;
  }

  public void setClassId(int classId) {
    ClassId = classId;
  }

  public void setEquipped(boolean equipped) {
    Equipped = equipped;
  }

  public void setEquipable(boolean equipable) {
    this.equipable = equipable;
  }

  public void setAttackType(String attackType) {
    this.attackType = attackType;
  }

  private void setMagicType(String magicType) {
    this.magicType = magicType;
  }

  public void setDamageStat(String damageStat) {
    DamageStat = damageStat;
  }

  public void setRange(int range) {
    Range = range;
  }

  public void setStats(String type, int val) {
    Stats.setValue(type, val);
  }

  public void setRequirements(HashMap<String, Integer> requirements) {
    Requirements = requirements;
  }

  public void setReqLevel(int val) {
    Requirements.put("ReqLevel", val);
  }

  public void setReqStrength(int val) {
    Requirements.put("ReqStrength", val);
  }

  public void setReqIntelligence(int val) {
    Requirements.put("ReqIntelligence", val);
  }

  public void setReqAgility(int val) {
    Requirements.put("ReqAgility", val);
  }

  public Vector<StatusEffect> getStatusEffects() {
    return statusEffects;
  }

  public void addStatusEffects(StatusEffect se) {
    statusEffects.add(se);
  }

  public boolean isConsumeable() {
    return consumeable;
  }

  public void setConsumeable(boolean consumeable) {
    this.consumeable = consumeable;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(100);
    sb.append(Name)
      .append(" (")
      .append(Id)
      .append(")");
    if (Stacked>1) {
      sb.append(" *")
        .append(Stacked);
    }
    if (MagicId != 0) {
      sb.append(" magic=")
        .append(MagicId);
    }
    if (info != null) {
      sb.append(" info=")
        .append(info.toString());
    }
    return sb.toString();
  }
}
