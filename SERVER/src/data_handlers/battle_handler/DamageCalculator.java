package data_handlers.battle_handler;

import utils.RandomUtils;
import creature.Creature;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.item_handler.Item;

public class DamageCalculator {

  // Check if miss, evade or hit
  public static String calculateAttack(Creature ATTACKER, Creature TARGET) {

    String damageInfo = "";

    // 10 -> .13   [50 -> .50]    100 -> .75   150 -> 0.875  200 -> 0.9375
    double accuracy = 1.0 - Math.exp(ATTACKER.getStat("ACCURACY") / -72.0);
    if (TARGET.isResting() || accuracy >= RandomUtils.getPercent()) {
      // 10 -> 0.10   50 -> 0.43   [100 -> .67]  150 -> .81   200 -> 0.89
      double evasion = 1.0 - Math.exp(TARGET.getStat("EVASION") / -91.0);
      if (TARGET.isResting() || evasion < RandomUtils.getPercent()) {

        int damage = calculateDamage(ATTACKER, TARGET);
        // 10 -> 0.10   50 -> 0.43   [100 -> .67]  150 -> .81   200 -> 0.89
        double critical = 1.0 - Math.exp(ATTACKER.getStat("CRITICAL_HIT") / -91.0);

        // Check if the attack is from behind
        double diffAngle = Math.abs(ATTACKER.getRotation() - TARGET.getRotation());
        if (diffAngle < 10.0) {
          critical += 80.0;
        } else if (diffAngle < 60.0) { // Angles are m*45
          critical += 50.0;
        }

        if (damage > 0 && (TARGET.isResting() || critical >= RandomUtils.getPercent())) {
          damageInfo = "true;" + (damage * 2);
        } else {
          damageInfo = "false;" + damage;
        }
      } else {
        damageInfo = "evade;0";
      }
    } else {
      damageInfo = "miss;0";
    }

    return damageInfo;
  }

  // CALCULATE DAMAGE
  public static int calculateDamage(Creature ATTACKER, Creature TARGET) {

    // GET ATTACK STAT
    String attackStat = "STRENGTH";

    int classId = 0;

    Item weapon = ATTACKER.getEquipment("Weapon");

    int weaponMinBaseDmg = 0;
    int weaponMaxBaseDmg = 0;

    if (weapon != null) {
      attackStat = weapon.getDamageStat();
      classId = weapon.getClassId();
      weaponMinBaseDmg = weapon.getStatValue("MinDamage");
      weaponMaxBaseDmg = weapon.getStatValue("MaxDamage");
    }

    int damageMin = 0;
    int damageMax = 0;

    double attackMinF = 0.0;
    double attackMaxF = 0.0;

    // Armor or resistance modifier
    double armorF = getDamageArmor(ATTACKER, TARGET);

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

      double ATKmin = (ATTACKER.getStat(attackStat) + 12.0) / 12.0;
      double ATKmax = (ATTACKER.getStat(attackStat) + 10.0) / 10.0;

      double classFac = 1.0;
      if (classId > 0) {
        if (playerAttacker.getClassById(classId) != null) {
          classId = playerAttacker.getClassById(classId).baseClassId;
          classFac = (playerAttacker.getClassById(classId).level + 12.0) / 13.0;
        }
      }

      double WeaponMin = weaponMinBaseDmg / 10.0;
      double WeaponMax = weaponMaxBaseDmg / 10.0;

      attackMinF = ATKmin * classFac * WeaponMin;
      attackMaxF = ATKmax * classFac * WeaponMax;

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

    if (damageMin < 0) {
      damageMin = 0;
    }
    if (damageMax < 0) {
      damageMax = 0;
    }

    return RandomUtils.getInt(damageMin, damageMax);
  }

  public static double getDamageArmor(Creature ATTACKER, Creature TARGET) {
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
        // ARMOR PIERCING STATUS EFFECT
        pierceFac *= 0.5f;
      }
      // 10 -> .90    50 -> .57   [100 -> .33]  150 -> .19   200 -> .11   419 -> .01
      ret = Math.exp(TARGET.getStat("ARMOR") * pierceFac / -91.);
    }
    if (magicType!=null) {
      // 10% MAGICAL DEFENSE APPLIES
      ret = 0.9 * ret  + 0.1 * Math.exp(TARGET.getStat(magicType + "_DEF") / -91.);
    }
    else {
      ret = Math.exp(TARGET.getStat(damageType + "_DEF") / -91.);
    }

    return ret;
  }
}
