package data_handlers.battle_handler;

import utils.RandomUtils;
import creature.Creature;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.item_handler.Item;
import network.Server;
import utils.ServerGameInfo;

public class DamageCalculator {

  public static final Damage DAMAGE_EVADED = new Damage("evade",0);
  public static final Damage DAMAGE_MISSED = new Damage("miss",0);

  public enum HitResult { CRITICAL, NORMAL, EVADED, MISSED };

  private static boolean isCover(int x, int y, int z) {
    return Server.WORLD_MAP.getTile(x, y, z) == null
        || !Server.WORLD_MAP.getTile(x, y, z).getPassable();
  }

  private static int calculateCover(Creature ATTACKER, Creature TARGET) {

    int tx = TARGET.getX();
    int ty = TARGET.getY();
    int tz = TARGET.getZ();

    double angle = 180.0 * Math.atan2(ty - ATTACKER.getY(), ATTACKER.getX() - tx) / Math.PI;
    int dy = -1;
    if (angle<0) {
      angle = -angle;
      dy = 1;
    }

    if (angle>=0.0) {
      if (angle < 10.0) {
        if (isCover(tx+1, ty, tz)) {
          return 200;
        }
      }
      else if (angle <= 35.0) {
        int ret = 0;
        if (isCover(tx+1, ty, tz)) {
          ret += 100;
        }
        if (isCover(tx+1, ty+dy, tz)) {
          ret += 100;
        }
        return ret;
      }
      else if (angle < 55.0) {
        if (isCover(tx+1, ty+dy, tz)) {
          return 200;
        }
      }
      else if (angle <= 80.0) {
        int ret = 0;
        if (isCover(tx, ty+dy, tz)) {
          ret += 100;
        }
        if (isCover(tx+1, ty+dy, tz)) {
          ret += 100;
        }
        return ret;
      }
      else if (angle < 100.0) {
        if (isCover(tx, ty+dy, tz)) {
          return 200;
        }
      }
      else if (angle <= 125.0) {
        int ret = 0;
        if (isCover(tx, ty+dy, tz)) {
          ret += 100;
        }
        if (isCover(tx-1, ty+dy, tz)) {
          ret += 100;
        }
        return ret;
      }
      else if (angle < 145.0) {
        if (isCover(tx-1, ty+dy, tz)) {
          return 200;
        }
      }
      else if (angle <= 170.0) {
        int ret = 0;
        if (isCover(tx-1, ty, tz)) {
          ret += 100;
        }
        if (isCover(tx-1, ty+dy, tz)) {
          ret += 100;
        }
        return ret;
      }
      else {
        if (isCover(tx-1, ty, tz)) {
          return 200;
        }
      }
    }
    return 0;
  }

  // Check if miss, evade or hit
  public static HitResult criticalOrMiss(Creature ATTACKER, Creature TARGET) {

    // 10 -> .13   [50 -> .50]    100 -> .75   150 -> 0.875  200 -> 0.9375
    double accuracy = 1.0 - Math.exp(ATTACKER.getStat("ACCURACY") / -72.0);

    if (TARGET.isResting() || accuracy >= RandomUtils.getPercent()) {
      // 10 -> 0.10   50 -> 0.43   [100 -> .67]  150 -> .81   200 -> 0.89
      //        20 -> .20     63 -> .50
      int cover = calculateCover(ATTACKER, TARGET);
      int actualEvasion = TARGET.isResting() ? 0 : TARGET.getStat("EVASION");
      double evasion = 1.0 - Math.exp((actualEvasion + cover) / -91.0);
      if (evasion < RandomUtils.getPercent()) {

        // 10 -> 0.10   50 -> 0.43   [100 -> .67]  150 -> .81   200 -> 0.89
        double critical = 1.0 - Math.exp(ATTACKER.getStat("CRITICAL_HIT") / -91.0);

        if (cover == 0) {
          // Check if the attack is from behind
          double diffAngle = Math.abs(ATTACKER.getRotation() - TARGET.getRotation());
          if (TARGET.isResting()) {
            critical += 100.0;
          }
          else if (diffAngle < 10.0) {
            critical += 80.0;
          } else if (diffAngle < 60.0) { // Angles are m*45
            critical += 50.0;
          }
        }
        else if (TARGET.isResting()) { // resting behind cover
          critical += 20.0;
        }

        if (critical >= RandomUtils.getPercent()) {
          return HitResult.CRITICAL;
        } else {
          return HitResult.NORMAL;
        }
      } else {
        return HitResult.EVADED;
      }
    } else {
      return HitResult.MISSED;
    }
  }

  // Check if miss, evade or hit
  public static Damage calculateAttack(Creature ATTACKER, Creature TARGET) {

    HitResult hit = criticalOrMiss(ATTACKER, TARGET);

    if (hit == HitResult.MISSED) {
      return DAMAGE_MISSED;
    }

    if (hit == HitResult.EVADED) {
      return DAMAGE_EVADED;
    }

    int damage = calculateDamage(ATTACKER, TARGET);

    if (damage <= 0) {
      return DAMAGE_MISSED;
    }

    if (hit == HitResult.CRITICAL) {
      return new Damage("true", damage * 2);
    }

    return new Damage("false", damage);
  }

  // CALCULATE DAMAGE
  public static int calculateDamage(Creature ATTACKER, Creature TARGET) {

    // GET ATTACK STAT
    String attackStat = "STRENGTH";

    int skillId = 0;

    Item weapon = ATTACKER.getEquipment("Weapon");

    int weaponMinBaseDmg = 0;
    int weaponMaxBaseDmg = 0;

    if (weapon != null) {
      attackStat = weapon.getDamageStat();
      weaponMinBaseDmg = weapon.getStatValue("MinDamage");
      weaponMaxBaseDmg = weapon.getStatValue("MaxDamage");
      skillId = ServerGameInfo.getSkillId(weapon.getSubType());
    }

    int damageMin = 0;
    int damageMax = 0;

    double attackMinF = 0.0;
    double attackMaxF = 0.0;

    // Armor or resistance modifier
    double armorF = getArmorFactor(ATTACKER, TARGET);

    if (ATTACKER.getCreatureType() == CreatureType.Monster) {

      // MONSTER DAMAGE FORMULA
      attackMinF = (ATTACKER.getStat(attackStat) + weaponMinBaseDmg / 3.0) * 0.7;
      attackMaxF = ATTACKER.getStat(attackStat) + weaponMaxBaseDmg / 3.0;

      damageMin = (int) Math.floor(attackMinF * armorF * 0.7);
      damageMax = (int) Math.floor(attackMaxF * armorF * 0.7 + 1.0);

    } else if (ATTACKER.getCreatureType() == CreatureType.Player) {
      PlayerCharacter playerAttacker = (PlayerCharacter) ATTACKER;

      // PLAYER DAMAGE FORMULA

      // (ATK+12)/12 * (SkillLevel+16)/16 * WeaponMin/10 * (60/(60+Armor))

      double ATKmin = (ATTACKER.getStat(attackStat) + 108.0) / 108.0;
      double ATKmax = (ATTACKER.getStat(attackStat) + 80.0) / 80.0;

      double classFac = 0.6;
      if (skillId != 0
      && playerAttacker.getSkill(skillId) != null
      ) {
        int weaponLevel = playerAttacker.getSkill(skillId).getLevel();
        classFac = 0.8 + (weaponLevel / 10.0);
      }

      attackMinF = ATKmin * classFac * weaponMinBaseDmg;
      attackMaxF = ATKmax * classFac * weaponMaxBaseDmg;

      damageMin = (int) Math.floor(attackMinF * armorF);
      damageMax = (int) Math.floor(attackMaxF * armorF + 1.0f);
    }

    // HALF DAMAGE IF PVP AND NOT IN LOST ARCHIPELAGO
    if (ATTACKER.getCreatureType() == CreatureType.Player
        && TARGET.getCreatureType() == CreatureType.Player
        && TARGET.getZ() > -200) {
      damageMin = (int) Math.floor(damageMin / 2.0);
      damageMax = (int) Math.floor(damageMax / 2.0);
    }

    if (damageMin < 1) {
      damageMin = 1;
    }
    if (damageMax < 1) {
      damageMax = 1;
    }

    return RandomUtils.getInt(damageMin, damageMax);
  }

  public static double getArmorFactor(Creature ATTACKER, Creature TARGET) {
    double ret = 0.0;

    String damageType = ATTACKER.getAttackType();
    boolean physicalDmd = ("STRIKE".equals(damageType)
                        || "SLASH".equals(damageType)
                        || "PIERCE".equals(damageType)
                        );
    String magicType = ATTACKER.getAttackWithMagic();
    if (physicalDmd) {
      // PHYSICAL DAMAGE, USE ARMOR
      float pierceFac = 1.0f;
      if ("PIERCE".equals(damageType)) {
        pierceFac *= 0.8f;
      }
      else if ("STRIKE".equals(damageType)) {
        pierceFac *= 1.25f;
      }
      if (ATTACKER.hasStatusEffect(32)) {
        // ARMOR PIERCING STATUS EFFECT; only for PIERCE?
        pierceFac *= 0.5f;
      }
      // 10 -> .90    50 -> .57   [100 -> .33]  150 -> .19   200 -> .11   419 -> .01
      ret = Math.exp(TARGET.getStat("ARMOR") * pierceFac / -91.);

      if (magicType!=null) {
        // 10% MAGICAL DEFENSE APPLIES
        double mag = Math.exp(TARGET.getStat(magicType + "_DEF") / -91.);
        ret = 0.9 * ret  + 0.1 * mag;
      }
    }
    else {
      ret = Math.exp(TARGET.getStat(damageType + "_DEF") / -91.);
    }
    return ret;
  }

  public static class Damage
  {
    public final String criticalOrMiss;
    public final int damage;

    private Damage(String criticalOrMiss, int damage) {
      this.criticalOrMiss = criticalOrMiss;
      this.damage = damage;
    }
  }
}
