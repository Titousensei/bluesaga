package creature;
/************************************
 *                                  *
 *    SERVER / CREATURE             *
 *                                  *
 ************************************/
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.newdawn.slick.util.pathfinding.Mover;

import utils.ServerGameInfo;
import utils.MathUtils;
import utils.RandomUtils;
import utils.XPTables;
import components.JobSkill;
import components.Stats;
import creature.Creature.CreatureType;
import data_handlers.ability_handler.Ability;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.item_handler.Item;
import network.Server;

public class Creature implements Mover {

  private static final List<Ability> EMPTY_ABILITIES = new ArrayList(0);

  protected int dbId; // Id in area_creature table or in user_character
  protected int CreatureId;
  protected String Name;
  protected int FamilyId;
  protected String Family;
  protected int Level;

  public enum CreatureType {
    None,
    Monster,
    Player
  }

  protected CreatureType creatureType = CreatureType.Monster; // Monster or Player

  private boolean Spawned = false;
  private boolean JustSpawned = false;

  private int Satisfied = 0; // If creature can eat or not, 3 = full

  private boolean RegainMode;

  private int potionHealthRegain = 0;
  private int potionManaRegain = 0;

  // XP
  protected int XP;
  protected int nextXP;

  // MOVEMENT
  protected int X;
  protected int Y;
  protected int Z;
  protected int oldX;
  protected int oldY;
  protected int oldZ;

  protected float gotoRotation = 180.0f;
  protected float rotation = 180.0f;

  // TIMERS
  private int moveTimerEnd;
  private int moveTimerItr = 0;

  private boolean READY_TO_MOVE = true;

  private int attackTimerEnd;
  private int attackTimerItr = 0;

  private boolean ATTACK_READY = true;

  // REGAIN
  public int potionRegainHealthTick = 5;
  public int potionRegainManaTick = 5;

  private int regainItr = 0; // seconds * 5
  private int defaultRegainReady = 24 * 5;
  private int regainSleepAccumulatedItr = 0;
  private int regainReady = defaultRegainReady; // seconds * 5

  private boolean Resting = false;

  // BATTLE
  private Creature AggroTarget;

  protected String AttackType;
  private String WeaponAttackType = "";
  private String attackWithMagic = null;

  private int mostHitDamage;

  // ABILTIY VARIABLES
  public int isCastingSpellItr = 0;

  protected boolean Dead = false;

  // EQUIPMENT AND STATS

  protected Item HeadItem = null;
  protected Item WeaponItem = null;
  protected Item OffHandItem = null;
  protected Item AmuletItem = null;
  protected Item ArtifactItem = null;

  public HashMap<String, Integer> coords = new HashMap<String, Integer>();

  private Customization Customization;

  protected int SizeWidth;
  protected int SizeHeight;

  private boolean Boss;

  protected HashMap<Integer, JobSkill> jobSkills = new HashMap<Integer, JobSkill>();
  protected List<Ability> abilities = null;
  private int DeathAbilityId = 0;

  protected Stats Stats = new Stats();
  private ConcurrentHashMap<Integer, StatusEffect> StatusEffects =
      new ConcurrentHashMap<Integer, StatusEffect>();
  private Stats BonusStats = new Stats();

  protected int Health;
  protected int Mana;
  protected boolean changedHealth = false;
  protected boolean changedMana = false;

  public Creature(int creatureId, int newX, int newY, int newZ) {

    CreatureId = creatureId;

    mostHitDamage = 0;

    RegainMode = true;

    setCustomization(new Customization());

    X = newX;
    Y = newY;
    Z = newZ;

    oldX = X;
    oldY = Y;
    oldZ = Z;

    getStatusEffects().clear();

    Level = 1;
    XP = 0;
    nextXP = XPTables.getNextLevelXP(Level + 1);

    AggroTarget = null;

    jobSkills.clear();

    try {
      ResultSet rs;
      rs = Server.gameDB.askDB("select * from creature where Id = " + creatureId);

      if (rs.next()) {

        Name = rs.getString("Name");
        WeaponAttackType = "";
        attackWithMagic = null;
        Family = ServerGameInfo.familyDef.get(rs.getInt("FamilyId")).getName();
        FamilyId = rs.getInt("FamilyId");

        Boss = false;
        if (rs.getInt("Boss") == 1) {
          Boss = true;
        }

        SizeWidth = rs.getInt("SizeW");
        SizeHeight = rs.getInt("SizeH");

        Level = rs.getInt("Level");

        AttackType = rs.getString("AttackType");

        Stats.setValues(rs);
        BonusStats = new Stats();

        Health = Stats.getValue("MAX_HEALTH");
        Mana = Stats.getValue("MAX_MANA");

        coords.put("HeadX", rs.getInt("HeadX"));
        coords.put("HeadY", rs.getInt("HeadY"));

        coords.put("WeaponX", rs.getInt("WeaponX"));
        coords.put("WeaponY", rs.getInt("WeaponY"));

        coords.put("OffHandX", rs.getInt("OffHandX"));
        coords.put("OffHandY", rs.getInt("OffHandY"));

        coords.put("AmuletX", rs.getInt("AmuletX"));
        coords.put("AmuletY", rs.getInt("AmuletY"));

        coords.put("ArtifactX", rs.getInt("ArtifactX"));
        coords.put("ArtifactY", rs.getInt("ArtifactY"));

        getCustomization().setAccessoriesX(rs.getInt("AccessoriesX"));
        getCustomization().setAccessoriesY(rs.getInt("AccessoriesY"));
        getCustomization().setSkinFeatureX(rs.getInt("SkinFeatureX"));
        getCustomization().setSkinFeatureY(rs.getInt("SkinFeatureY"));
        getCustomization().setMouthFeatureX(rs.getInt("MouthFeatureX"));
        getCustomization().setMouthFeatureY(rs.getInt("MouthFeatureY"));

        setDeathAbilityId(rs.getInt("DeathAbility"));

        if (rs.getInt("Ability1") > 0) {
          Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(rs.getInt("Ability1")));
          newAbility.setCaster(CreatureType.Monster, this);
          addAbility(newAbility);
        }
        if (rs.getInt("Ability2") > 0) {
          Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(rs.getInt("Ability2")));
          newAbility.setCaster(CreatureType.Monster, this);
          addAbility(newAbility);
        }
        if (rs.getInt("Ability3") > 0) {
          Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(rs.getInt("Ability3")));
          newAbility.setCaster(CreatureType.Monster, this);
          addAbility(newAbility);
        }
        if (rs.getInt("Ability4") > 0) {
          Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(rs.getInt("Ability4")));
          newAbility.setCaster(CreatureType.Monster, this);
          addAbility(newAbility);
        }
        if (rs.getInt("Ability5") > 0) {
          Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(rs.getInt("Ability5")));
          newAbility.setCaster(CreatureType.Monster, this);
          addAbility(newAbility);
        }
        if (rs.getInt("Ability6") > 0) {
          Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(rs.getInt("Ability6")));
          newAbility.setCaster(CreatureType.Monster, this);
          addAbility(newAbility);
        }

        moveTimerEnd = Stats.getValue("SPEED") * 10;

        if (moveTimerEnd > 0) {
          moveTimerItr = RandomUtils.getInt(0, moveTimerEnd);
        } else {
          moveTimerItr = 0;
        }

        attackTimerEnd = Math.round(2000.0f * (200.0f / (getAttackSpeed() + 100.0f)));
        attackTimerItr = attackTimerEnd;
        ATTACK_READY = true;
      }

      rs.close();

    } catch (SQLException e1) {
      e1.printStackTrace();
    }
  }

  public Creature(Creature copy, int creatureX, int creatureY, int creatureZ) {
    CreatureId = copy.getCreatureId();

    mostHitDamage = 0;

    RegainMode = true;

    setCustomization(new Customization());

    X = creatureX;
    Y = creatureY;
    Z = creatureZ;

    oldX = X;
    oldY = Y;
    oldZ = Z;

    getStatusEffects().clear();

    Level = 1;
    XP = 0;
    nextXP = XPTables.getNextLevelXP(Level + 1);

    AggroTarget = null;

    jobSkills.clear();

    Name = copy.getName();
    WeaponAttackType = "";
    attackWithMagic = null;
    Family = ServerGameInfo.familyDef.get(copy.getFamilyId()).getName();
    FamilyId = copy.getFamilyId();

    Boss = copy.isBoss();

    SizeWidth = copy.getSizeWidth();
    SizeHeight = copy.getSizeHeight();

    Level = copy.getLevel();

    AttackType = copy.getAttackType();

    // Add stats
    Stats = new Stats();
    for (Iterator<String> iter = copy.getStats().getHashMap().keySet().iterator();
        iter.hasNext();
        ) {
      String key = iter.next().toString();
      int value = copy.getStats().getHashMap().get(key);
      Stats.setValue(key, value);
    }

    // Add abilities
    if (copy.getAbilities() != null) {
      for (Ability ability : copy.getAbilities()) {
        Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(ability.id));
        newAbility.setCaster(CreatureType.Monster, this);
        addAbility(newAbility);
      }
    }

    Health = Stats.getValue("MAX_HEALTH");
    Mana = Stats.getValue("MAX_MANA");

    BonusStats = new Stats();

    coords = copy.getCoords();

    Customization = copy.getCustomization();

    setDeathAbilityId(copy.getDeathAbilityId());

    moveTimerEnd = Stats.getValue("SPEED") * 10;

    if (moveTimerEnd > 0) {
      moveTimerItr = RandomUtils.getInt(0, moveTimerEnd);
    } else {
      moveTimerItr = 0;
    }

    attackTimerEnd = Math.round(2000.0f * (200.0f / (getAttackSpeed() + 100.0f)));
    attackTimerItr = attackTimerEnd;
    ATTACK_READY = true;
  }

  public String getFullData(Creature observer) {

    String creatureData =
        getSmallData()
            + ','
            + getName()
            + ','
            + getEquipmentInfo()
            + ','
            + getHealthStatus()
            + ','
            + getStat("SPEED")
            + ','
            + getCustomization().getMouthFeatureId()
            + ','
            + getCustomization().getAccessoriesId()
            + ','
            + getCustomization().getSkinFeatureId()
            + (isResting() ? ",1" : ",0");

    return creatureData;
  }

  public String getSmallData() {
    String newCreatureData =
        getCreatureType().toString()
            + ','
            + getDBId()
            + ','
            + getCreatureId()
            + ','
            + getX()
            + ','
            + getY()
            + ','
            + getZ()
            + ','
            + getRotation();
    return newCreatureData;
  }

  public int getNpcQuestStatus(Npc npc) {
    return 0;
  }

  /****************************************
   *                                      *
   *          SKILLS                      *
   *                                      *
   ****************************************/
  public JobSkill getSkill(int skillId) {
    return jobSkills.get(skillId);
  }

  public HashMap<Integer, JobSkill> getSkills() {
    return jobSkills;
  }

  /****************************************
   *                                      *
   *         STATUS EFFECTS               *
   *                                      *
   *                                      *
   ****************************************/
  public void updateBonusStats() {
    Stats newStats = new Stats();

    // GO THROUGH EQUIPMENT AND UPDATE BONUS STATS
    if (WeaponItem != null) {
      newStats.addStats(WeaponItem.getStats());
    }
    if (HeadItem != null) {
      newStats.addStats(HeadItem.getStats());
    }
    if (OffHandItem != null) {
      newStats.addStats(OffHandItem.getStats());
    }
    if (AmuletItem != null) {
      newStats.addStats(AmuletItem.getStats());
    }
    if (ArtifactItem != null) {
      newStats.addStats(ArtifactItem.getStats());
    }

    // Adjust Secondary stats from primary stats
    // "ACCURACY"       64 + INT/3.5
    newStats.addValue("ACCURACY", getStatFraction("INTELLIGENCE", 3.5f));
    // "EVASION"         5 + AGI/3
    newStats.addValue("EVASION", getStatFraction("AGILITY", 3.0f));
    // "CRITICAL_HIT"    0 + INT/10
    newStats.addValue("CRITICAL_HIT", getStatFraction("INTELLIGENCE", 10.0f));
    // "ARMOR"           0 + STR/3
    newStats.addValue("ARMOR", getStatFraction("STRENGTH", 3.0f));
    // "AttackSpeed"    75 + AGI
    newStats.addValue("ATTACKSPEED", getStatFraction("AGILITY", 1.0f));
    // "SPEED"          80 + STR/2
    newStats.addValue("SPEED", getStatFraction("STRENGTH", 2.0f));

    // GO THROUGH STATUS EFFECTS AND CHANGES STATS ACCORDINGLY
    for (Iterator<StatusEffect> iter = getStatusEffects().values().iterator(); iter.hasNext(); ) {
      StatusEffect s = iter.next();
      newStats.addStats(s.getStatsModif());
    }

    BonusStats = newStats;
  }

  private int getStatFraction(String name, float div) {
    int st = Stats.getValue(name);
    int bst = Stats.getValue(name);
    return Math.round((st + bst)/div);
  }

  public String getBonusStatsAsString() {

    StringBuilder bonusStats = new StringBuilder(1000);

    bonusStats
        .append(BonusStats.getValue("STRENGTH"))
        .append(',')
        .append(BonusStats.getValue("INTELLIGENCE"))
        .append(',')
        .append(BonusStats.getValue("AGILITY"))
        .append(',')
        .append(BonusStats.getValue("SPEED"))
        .append(',')
        .append(BonusStats.getValue("CRITICAL_HIT"))
        .append(',')
        .append(BonusStats.getValue("EVASION"))
        .append(',')
        .append(BonusStats.getValue("ACCURACY"))
        .append(',')
        .append(BonusStats.getValue("MAX_HEALTH"))
        .append(',')
        .append(BonusStats.getValue("MAX_MANA"))
        .append(',')
        .append(BonusStats.getValue("FIRE_DEF"))
        .append(',')
        .append(BonusStats.getValue("COLD_DEF"))
        .append(',')
        .append(BonusStats.getValue("SHOCK_DEF"))
        .append(',')
        .append(BonusStats.getValue("CHEMS_DEF"))
        .append(',')
        .append(BonusStats.getValue("MIND_DEF"))
        .append(',')
        .append(BonusStats.getValue("ARMOR"))
        .append(',')
        .append(getAttackSpeed())
        .append(',')
        .append(BonusStats.getValue("HEALTH_REGAIN"))
        .append(',')
        .append(BonusStats.getValue("MANA_REGAIN"));

    return bonusStats.toString();
  }

  public synchronized int addStatusEffect(StatusEffect newStatusEffect) {

    int ret = 0;
    Stats newStats = newStatusEffect.getStatsModif();
    if (newStats != null) {
      if (newStats.getValue("MAX_HEALTH") < 0) {
        int newMaxHealth = Stats.getValue("MAX_HEALTH")
                           + BonusStats.getValue("MAX_HEALTH")
                           + newStats.getValue("MAX_HEALTH");
        if (newMaxHealth < 0) {
          return -1;
        } else if (Health > newMaxHealth) {
          Health = newMaxHealth;
          ret |= 1;
        }
        int delta = newMaxHealth - Health;
        if (getStat("HEALTH_REGAIN") > delta) {
          setStat("HEALTH_REGAIN", delta);
        }
      }
      if (newStats.getValue("MAX_MANA") < 0) {
        int newMaxMana = Stats.getValue("MAX_MANA")
                         + BonusStats.getValue("MAX_MANA")
                         + newStats.getValue("MAX_MANA");
        if (newMaxMana < 0) {
          return -1;
        } else if (Mana > newMaxMana) {
          Mana = newMaxMana;
          ret |= 2;
        }
        int delta = newMaxMana - Mana;
        if (getStat("MANA_REGAIN") > delta) {
          setStat("MANA_REGAIN", delta);
        }
      }
    }

    StatusEffect mySE = getStatusEffects().get(newStatusEffect.getId());

    if (mySE == null) {
      getStatusEffects().put(newStatusEffect.getId(), newStatusEffect);
      updateBonusStats();
    } else if ("stackable".equals(newStatusEffect.getRepeatDamageType())) {
      Stats myStats = mySE.getStatsModif();
      myStats.fraction(1.0f * mySE.getDuration() / newStatusEffect.getDuration());
      myStats.addStats(newStats);
      updateBonusStats();
      mySE.start();
      ret |= 4;
    } else {
      mySE.setCaster(newStatusEffect.getCaster());
      if (mySE.getAbility() != null) {
        mySE.setAbility(newStatusEffect.getAbility());
      }
      mySE.start();
    }

    return ret;
  }

  public synchronized void removeStatusEffect(int statusEffectId) {
    for (Iterator<StatusEffect> iter = StatusEffects.values().iterator(); iter.hasNext(); ) {
      StatusEffect s = iter.next();
      if (s.getId() == statusEffectId) {
        iter.remove();
        break;
      }
    }
    updateBonusStats();
  }

  public boolean hasStatusEffect(int statusEffectId) {
    if (getStatusEffects().get(statusEffectId) == null) {
      return false;
    }
    return true;
  }

  public ConcurrentHashMap<Integer, StatusEffect> getStatusEffects() {
    return StatusEffects;
  }

  public StatusEffect getStatusEffect(int sId) {
    return getStatusEffects().get(sId);
  }

  public boolean hasManaShield() {
    if (getStatusEffect(25) == null) {
      return false;
    }
    return true;
  }

  /****************************************
   *                                      *
   *         ABILITIES                    *
   *                                      *
   *                                      *
   ****************************************/
  public void useAbility(Ability ability) {
    loseMana(ability.getManaCost());  // ignore return value, cost already checked
    ability.used();
    isCastingSpellItr = ability.getCastingSpeed();
  }

  public int hasAbility(String abilityName) {
    int abilityId = 9999;
    if (abilities != null) {
      for (int i = 0; i < abilities.size(); i++) {
        if (abilities.get(i).name.equals(abilityName)) {
          abilityId = i;
          break;
        }
      }
    }
    return abilityId;
  }

  public List<Ability> getAbilities() {
    return (abilities != null) ? abilities : EMPTY_ABILITIES;
  }

  public int getNrAbilities() {
    return (abilities != null) ? abilities.size() : 0;
  }

  public Ability getAbility(int index) {
    return (abilities != null) ? abilities.get(index) : null;
  }

  public Ability getAbilityById(int Id) {
    if (abilities != null) {
      for (Ability ability : abilities) {
        if (ability.id == Id) {
          return ability;
        }
      }
    }
    return null;
  }

  public void addAbility(Ability newAbility) {
    if (abilities == null) {
      abilities = new ArrayList(1);
    }
    abilities.add(newAbility);
  }

  /****************************************
   *                                      *
   *             MOVEMENT                 *
   *                                      *
   *                                      *
   ****************************************/
  public void walkTo(int newX, int newY, int newZ) {
    oldX = X;
    oldY = Y;
    oldZ = Z;

    X = newX;
    Y = newY;
    Z = newZ;

    int dX = oldX - X;
    int dY = oldY - Y;

    float newAngle = MathUtils.angleBetween(dX, dY);

    if (newAngle < 0) {
      newAngle += 360;
    }

    setGotoRotation(newAngle);
  }

  public void startMoveTimer(boolean diagonalMove) {

    READY_TO_MOVE = false;

    float moveTime = 2000.0f;

    if (getCreatureType() == CreatureType.Player) {
      moveTime = 200.0f;
    } else {
      if (isAggro()) {
        moveTime = 360.0f;
      }
      moveTime *= (100.0f / (getStat("SPEED") + 1.0f));
    }

    if (diagonalMove) {
      moveTime *= 1.4f;
    }

    moveTime /= 100.0f;

    moveTimerItr = 0;
    moveTimerEnd = Math.round(moveTime);
  }

  public void restartMoveTimer() {
    startMoveTimer(false);
  }

  /**
   * Updates move timer
   */
  public void checkMoveTimer() {

    // Update rotation
    float diff = rotation - gotoRotation;

    if (creatureType == CreatureType.Player) {
      if (diff != 0) {
        rotation = gotoRotation;
      }
    } else {
      float rotationSpeed = 18.0f;

      if (Math.abs(diff) > rotationSpeed) {
        if (diff > 0) {
          rotationSpeed *= -1.0f;
        }
        if (Math.abs(diff) > 180.0f) {
          rotationSpeed *= -1.0f;
        }

        rotation += rotationSpeed;

        if (rotation < 0.0f) {
          rotation = 360.0f + rotation;
        }

        if (rotation >= 360.0f) {
          rotation = rotation - 360.0f;
        }
      } else {
        rotation = gotoRotation;
      }
    }

    if (moveTimerItr >= moveTimerEnd) {
      READY_TO_MOVE = true;
    } else {
      moveTimerItr++;
    }
  }

  public boolean isReadyToMove() {
    if (getStat("SPEED") <= 0) {
      return false;
    }
    return READY_TO_MOVE;
  }

  public void setReadyToMove(boolean value) {
    READY_TO_MOVE = value;
  }

  /****************************************
   *                                      *
   *             EQUIPMENT/INVENTORY      *
   *                                      *
   *                                      *
   ****************************************/
  public boolean equipItem(Item newItem) {
    boolean equipOk = false;
    if (newItem != null) {
      if (newItem.getType().equals("Weapon") || newItem.getSubType().equals("Weapon")) {
        WeaponItem = newItem;
        WeaponAttackType = newItem.getAttackType();
        if (WeaponAttackType == null) {
          WeaponAttackType = "";
        }
        attackWithMagic = newItem.getMagicType();
        equipOk = true;
      } else if (newItem.getType().equals("Head") || newItem.getSubType().equals("Head")) {
        HeadItem = newItem;
        equipOk = true;
      } else if (newItem.getType().equals("OffHand") || newItem.getSubType().equals("OffHand")) {
        OffHandItem = newItem;
        equipOk = true;
      } else if (newItem.getType().equals("Amulet") || newItem.getSubType().equals("Amulet")) {
        AmuletItem = newItem;
        equipOk = true;
      } else if (newItem.getType().equals("Artifact") || newItem.getSubType().equals("Artifact")) {
        ArtifactItem = newItem;
        equipOk = true;
      }
      if (equipOk) {
        updateBonusStats();
      }
    }
    return equipOk;
  }

  public void unequipItem(String equipType) {
    if (equipType.equals("Weapon")) {
      WeaponItem = null;
      WeaponAttackType = "";
      attackWithMagic = null;
    } else if (equipType.equals("Head")) {
      HeadItem = null;
    } else if (equipType.equals("Weapon")) {
      WeaponItem = null;
    } else if (equipType.equals("OffHand")) {
      OffHandItem = null;
    } else if (equipType.equals("Amulet")) {
      AmuletItem = null;
    } else if (equipType.equals("Artifact")) {
      ArtifactItem = null;
    }
    updateBonusStats();
  }

  public Item getEquipment(String equipType) {
    if (equipType.equals("Weapon")) {
      return WeaponItem;
    } else if (equipType.equals("Head")) {
      return HeadItem;
    } else if (equipType.equals("OffHand")) {
      return OffHandItem;
    } else if (equipType.equals("Amulet")) {
      return AmuletItem;
    } else if (equipType.equals("Artifact")) {
      return ArtifactItem;
    }

    return null;
  }

  public boolean canEquip(Item equipItem) {
    boolean equipOk = true;
    if (equipItem.getRequirement("ReqLevel") > getLevel()
        || equipItem.getRequirement("ReqStrength") > getStat("STRENGTH")
        || equipItem.getRequirement("ReqAgility") > getStat("AGILITY")
        || equipItem.getRequirement("ReqIntelligence") > getStat("INTELLIGENCE")) {
      equipOk = false;
    }

    return equipOk;
  }

  public boolean useItem(Item newItem) {

    boolean useSuccess = false;

    if (newItem != null) {
      if (newItem.getSubType().equals("HEALTH") && Health < getStat("MAX_HEALTH")) {
        potionRegainHealthTick = newItem.getStatValue("MAX_HEALTH") / 10;

        potionHealthRegain += newItem.getStatValue("MAX_HEALTH");

        if (Health + potionHealthRegain > getStat("MAX_HEALTH")) {
          potionHealthRegain = getStat("MAX_HEALTH") - Health;
        }

        useSuccess = true;
      }

      if (newItem.getSubType().equals("MANA") && Mana < getStat("MAX_MANA")) {
        potionRegainManaTick = newItem.getStatValue("MAX_MANA") / 10;

        potionManaRegain += newItem.getStatValue("MAX_MANA");

        if (Mana + potionManaRegain > getStat("MAX_MANA")) {
          potionManaRegain = getStat("MAX_MANA") - Mana;
        }
        useSuccess = true;
      }

      if (newItem.getType().equals("Eatable")) {

        int addedHealthRegain = newItem.getStatValue("MAX_HEALTH");
        int addedManaRegain = newItem.getStatValue("MAX_MANA");

        setStat("HEALTH_REGAIN", getStat("HEALTH_REGAIN") + addedHealthRegain);
        setStat("MANA_REGAIN", getStat("MANA_REGAIN") + addedManaRegain);

        useSuccess = true;
      }
    }

    return useSuccess;
  }

  public String getEquipmentInfo() {

    StringBuilder info = new StringBuilder();

    if (HeadItem != null) {
      info.append(HeadItem.getId()).append(',');
    } else {
      info.append("0,");
    }
    info.append(getCustomization().getHeadSkinId()).append(',');

    if (WeaponItem != null) {
      info.append(WeaponItem.getId()).append(',');
    } else {
      info.append("0,");
    }
    info.append(getCustomization().getWeaponSkinId()).append(',');

    if (OffHandItem != null) {
      info.append(OffHandItem.getId()).append(',');
    } else {
      info.append("0,");
    }
    info.append(getCustomization().getOffHandSkinId()).append(',');

    if (AmuletItem != null) {
      info.append(AmuletItem.getId()).append(',');
    } else {
      info.append("0,");
    }
    info.append(getCustomization().getAmuletSkinId()).append(',');

    if (ArtifactItem != null) {
      info.append(ArtifactItem.getId()).append(',');
    } else {
      info.append("0,");
    }
    info.append(getCustomization().getArtifactSkinId());

    return info.toString();
  }

  /****************************************
   *                                      *
   *         REGAIN HEALTH AND MANA       *
   *                                      *
   *                                      *
   ****************************************/

  /****************************************
   *                                      *
   *             BATTLE!                  *
   *                                      *
   *                                      *
   ****************************************/

  // TIMER
  public void startAttackTimer() {

    ATTACK_READY = false;
    float attackTurnTime = 2000.0f * (200.0f / (getAttackSpeed() + 100.0f));

    attackTurnTime /= 200.0f;

    attackTimerItr = 0;
    attackTimerEnd = Math.round(attackTurnTime);
  }

  public boolean checkAttackTimer() {

    if (attackTimerItr >= attackTimerEnd) {
      ATTACK_READY = true;
    } else {
      attackTimerItr++;
    }
    return ATTACK_READY;
  }

  public void kill() {
    Dead = true;
  }

  public void setAttackTimerReady() {
    attackTimerItr = attackTimerEnd;
    ATTACK_READY = true;
  }

  /****************************************
   *                                      *
   *             ATTACK/ANIMATION         *
   *                                      *
   *                                      *
   ****************************************/
  public void regainPotionHealth(int healthTick) {
    if (potionHealthRegain > 0) {
      int healthGain = 0;

      if (potionHealthRegain < healthTick) {
        healthGain = potionHealthRegain;
        potionHealthRegain = 0;
      } else {
        potionHealthRegain -= healthTick;
        healthGain = healthTick;
      }
      gainHealth(healthGain, healthGain / 2);
    }
  }

  public void regainPotionMana(int manaTick) {
    if (potionManaRegain > 0) {
      int manaGain = 0;

      if (potionManaRegain < manaTick) {
        manaGain = potionManaRegain;
        potionManaRegain = 0;
      } else {
        potionManaRegain -= manaTick;
        manaGain = manaTick;
      }
      gainMana(manaGain, manaGain / 2);
    }
  }

  public void regainHealth() {

    if (Health > getStat("MAX_HEALTH")) {
      gainHealth(0, 0);
    }
    else if (Health < getStat("MAX_HEALTH")) {
      int gain = 0;

      if (isResting()) {
        gain = Math.round(regainSleepAccumulatedItr / 8.0f);
      } else {
        gain = getStat("MAX_HEALTH") / 100;
        int bonus = getStat("MAX_HEALTH") % 100;
        if (bonus > RandomUtils.getInt(0, 100)) {
          ++ gain;
        }
      }
      if (gain > getStat("HEALTH_REGAIN")) {
        gain = getStat("HEALTH_REGAIN");
      }

      gainHealth(gain, gain);
    }
  }

  public void regainMana() {

    if (Mana > getStat("MAX_MANA")) {
      gainMana(0, 0);
    }
    else if (Mana < getStat("MAX_MANA")) {
      int gain = 0;

      if (isResting()) {
        gain = Math.round(regainSleepAccumulatedItr / 8.0f);
      } else {
        gain = getStat("MAX_MANA") / 100;
        int bonus = getStat("MAX_MANA") % 100;
        if (bonus > RandomUtils.getInt(0, 100)) {
          ++ gain;
        }
      }
      if (gain > getStat("MANA_REGAIN")) {
        gain = getStat("MANA_REGAIN");
      }

      gainMana(gain, gain);
    }
  }

  public void restartRegainTimer() {
    RegainMode = false;
    regainItr = 0;
  }

  public boolean regainItrUpdate() {
    if (isResting()) {
      regainItr += 16;
      regainSleepAccumulatedItr += 2;
    } else {
      regainItr += 4;
    }
    if (regainItr >= regainReady
        && (Health < getStat("MAX_HEALTH") || Mana < getStat("MAX_MANA"))) {
      return true;
    }
    return false;
  }

  // return true if dead
  public boolean hitByAttack(int damage) {
    if (damage > mostHitDamage) {
      mostHitDamage = damage;
    }

    if (damage > 0 && hasManaShield()) {
      damage -= loseMana(damage);
    }

    restartRegainTimer();
    return loseHealth(damage);
  }

  /*
   *
   *  DEATH / RESPAWN
   *
   */
  public void die() {
    getStatusEffects().clear();
    Dead = true;

    updateBonusStats();

    setAggro(null);
  }

  public boolean isDead() {
    return Dead;
  }

  public void revive() {
    getStatusEffects().clear();
    Dead = false;
    mostHitDamage = 0;
    AggroTarget = null;
    RegainMode = true;
    Health = getStat("MAX_HEALTH");
    Mana = getStat("MAX_MANA");
    ATTACK_READY = true;
    setRotation(180.0f);
    setGotoRotation(180.0f);
    updateBonusStats();
  }

  public void gainHealth(int change, int regain) {
    if (Dead) {
      return;
    }

    int before = Health;
    Health += change;

    int delta = getStat("MAX_HEALTH") - Health;
    if (delta < 0) {
      Health = getStat("MAX_HEALTH");
      setStat("HEALTH_REGAIN", 0);
    } else {
      int adjust = getStat("HEALTH_REGAIN") - regain;
      if (adjust > delta) {
        adjust = delta;
      }
      setStat("HEALTH_REGAIN", adjust);
    }
    changedHealth = (before != Health);
  }

  public boolean loseHealth(int change) {
    if (Dead) {
      return true;
    }

    Health -= change;
    changedHealth = true;

    if (Health <= 0) {
      Health = 0;
      setStat("HEALTH_REGAIN", 0);
      die();
      return true;
    } else {
      int regain = change / 2;
      int bonus = change % 2;
      if (bonus > 0 && 0.5 > RandomUtils.getPercent()) {
        ++ regain;
      }
      setStat("HEALTH_REGAIN", getStat("HEALTH_REGAIN") + regain);

      return false;
    }
  }

  public void gainMana(int change, int regain) {
    int before = Mana;
    Mana += change;

    changedMana = false;
    int delta = getStat("MAX_MANA") - Mana;
    if (delta < 0) {
      Mana = getStat("MAX_MANA");
      setStat("MANA_REGAIN", 0);
    } else {
      int adjust = getStat("MANA_REGAIN") - regain;
      if (adjust > delta) {
        adjust = delta;
      }
      setStat("MANA_REGAIN", adjust);
    }
    changedMana = (before != Mana);
  }

  // return how much mana was actually lost
  public int loseMana(int change) {
    int before = Mana;
    Mana -= change;

    if (Mana <= 0) {
      Mana = 0;
    } else {
      int regain = change / 2;
      int bonus = change % 2;
      if (bonus > 0 && 0.5 > RandomUtils.getPercent()) {
        ++ regain;
      }
      setStat("MANA_REGAIN", getStat("MANA_REGAIN") + regain);
    }
    if (before != Mana) {
      changedMana = true;
      return (before - Mana);
    }
    return 0;
  }

  public boolean pollChangedHealth() {
    if (changedHealth) {
      changedHealth = false;
      return true;
    }
    return false;
  }

  public boolean pollChangedMana() {
    if (changedMana) {
      changedMana = false;
      return true;
    }
    return false;
  }

  public int getHealthStatus() {
    // 4 = Healthy, 3 = Wounded, 2 = Heavily wounded, 1 = Dying
    int healthStatus = 4;

    healthStatus = 4 * Health / getStat("MAX_HEALTH");
    return healthStatus;
  }

  /****************************************
   *                                      *
   *             GETTER/SETTER            *
   *                                      *
   *                                      *
   ****************************************/
  public int getHealth() {
    return Health;
  }

  public int getMana() {
    return Mana;
  }

  public boolean isBoss() {
    return Boss;
  }

  public void setSizeWidth(int sizeW) {
    SizeWidth = sizeW;
  }

  public void setSizeHeight(int sizeH) {
    SizeHeight = sizeH;
  }

  public int getSizeWidth() {
    return SizeWidth;
  }

  public int getSizeHeight() {
    return SizeHeight;
  }

  public int getMostHitDamage() {
    return mostHitDamage;
  }

  public int getHeadId() {
    if (HeadItem != null) {
      return HeadItem.getId();
    }
    return 0;
  }

  public int getWeaponId() {
    if (WeaponItem != null) {
      return WeaponItem.getId();
    }
    return 0;
  }

  public int getOffHandId() {
    if (OffHandItem != null) {
      return OffHandItem.getId();
    }
    return 0;
  }

  public int getAmuletId() {
    if (AmuletItem != null) {
      return AmuletItem.getId();
    }
    return 0;
  }

  public int getArtifactId() {
    if (ArtifactItem != null) {
      return ArtifactItem.getId();
    }
    return 0;
  }

  public String getAttackType() {
    if (WeaponAttackType.equals("")) {
      return AttackType;
    }
    return WeaponAttackType;
  }

  public String getAttackWithMagic() {
    return attackWithMagic;
  }

  public int getId() {
    return CreatureId;
  }

  public void setName(String newName) {
    Name = newName;
  }

  public String getName() {
    return Name;
  }

  public String getFamily() {
    return Family;
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

  public String getHealthAsString() {
    return Health + " / " + getStat("MAX_HEALTH");
  }

  public int getHealthBarWidth(int Max) {
    float fHealth = Health;
    float fMaxHealth = getStat("MAX_HEALTH");
    float healthBarWidth = (fHealth / fMaxHealth) * Max;
    return Math.round(healthBarWidth);
  }

  public String getManaAsString() {
    return Mana + " / " + getStat("MAX_MANA");
  }

  public int getManaBarWidth(int Max) {
    float fMana = Mana;
    float fMaxMana = getStat("MAX_MANA");
    float manaBarWidth = (fMana / fMaxMana) * Max;
    return Math.round(manaBarWidth);
  }

  public void setStat(String StatType, int value) {
    Stats.setValue(StatType, value);
  }

  public int getRawStat(String StatType) {
    return Stats.getValue(StatType);
  }

  public Stats getStats() {
    return Stats;
  }

  public int getStat(String StatType) {
    int totalStat = Stats.getValue(StatType);
    totalStat += BonusStats.getValue(StatType);
    if (totalStat < 0) {
      totalStat = 0;
    }
    return totalStat;
  }

  public int getBonusStat(String StatType) {
    return BonusStats.getValue(StatType);
  }

  public int getCreatureId() {
    return CreatureId;
  }

  public void setCreatureId(int creatureId) {
    CreatureId = creatureId;
  }

  public int getLevel() {
    return Level;
  }

  public int getDBId() {
    return dbId;
  }

  public void setDBId(int newDBId) {
    dbId = newDBId;
  }

  public Item getWeapon() {
    return WeaponItem;
  }

  public int getAttackSpeed() {
    int attackSpeed = 0;

    attackSpeed = getStat("ATTACKSPEED");
    if (attackSpeed < 0) {
      attackSpeed = 0;
    }
    return getStat("ATTACKSPEED");
  }

  public int getAttackRange() {
    if (WeaponItem != null) {
      return WeaponItem.getRange();
    }
    return 1;
  }

  public void setAggro(Creature target) {
    setResting(false);

    AggroTarget = target;

    if (creatureType == CreatureType.Monster) {
      READY_TO_MOVE = false;
      moveTimerItr = RandomUtils.getInt(moveTimerEnd - 2, moveTimerEnd);
      if (moveTimerItr < 0) {
        moveTimerItr = 0;
      }
    } else {
      //  READY_TO_MOVE = true;
    }
  }

  public void turnAggroOff() {
    AggroTarget = null;
  }

  public boolean isAggro() {
    if (AggroTarget == null) {
      return false;
    }
    return true;
  }

  public Creature getAggroTarget() {
    return AggroTarget;
  }

  public boolean getRegainMode() {
    return RegainMode;
  }

  public int getXP() {
    return XP;
  }

  public int getOldX() {
    return oldX;
  }

  public int getOldY() {
    return oldY;
  }

  public int getOldZ() {
    return oldZ;
  }

  public CreatureType getCreatureType() {
    return creatureType;
  }

  public void setCreatureType(CreatureType newCreatureType) {
    creatureType = newCreatureType;
  }

  public void setRegainReady(int newValue) {
    regainReady = newValue;
    regainItr = 0;
  }

  public void restoreRegainReady() {
    regainReady = defaultRegainReady;
    regainItr = 0;
  }

  public boolean isResting() {
    return Resting;
  }

  public void setResting(boolean resting) {
    regainSleepAccumulatedItr = 0;
    this.Resting = resting;
  }

  public int getDeathAbilityId() {
    return DeathAbilityId;
  }

  public void setDeathAbilityId(int deathAbilityId) {
    DeathAbilityId = deathAbilityId;
  }

  public boolean isSpawned() {
    return Spawned;
  }

  public void setSpawned(boolean spawned) {
    Spawned = spawned;
  }

  public boolean isJustSpawned() {
    return JustSpawned;
  }

  public void setJustSpawned(boolean justSpawned) {
    JustSpawned = justSpawned;
  }

  public Customization getCustomization() {
    return Customization;
  }

  public void setCustomization(Customization customization) {
    Customization = customization;
  }

  public int getFamilyId() {
    return FamilyId;
  }

  public int getSatisfied() {
    return Satisfied;
  }

  public void setSatisfied(int satisfied) {
    Satisfied = satisfied;
  }

  public float getRotation() {
    return rotation;
  }

  public void setRotation(float rotation) {
    this.rotation = rotation;
  }

  public float getGotoRotation() {
    return gotoRotation;
  }

  public void setGotoRotation(float gotoRotation) {
    this.gotoRotation = gotoRotation;
  }

  public HashMap<String, Integer> getCoords() {
    return coords;
  }

  @Override
  public String toString() {
    return Name + " (" + dbId +")";
  }
}
