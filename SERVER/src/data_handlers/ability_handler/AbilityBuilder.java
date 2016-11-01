package data_handlers.ability_handler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import components.Builder;

public class AbilityBuilder
extends Builder<Ability>
{
  public final static Pattern VALID_AOE = Pattern.compile("-?[0-9],-?[0-9](;-?[0-9],-?[0-9])*");

  public final static Set<String> VALID_TARGETS = new HashSet<>(Arrays.asList(
      Ability.TARGET_TILE, Ability.TARGET_CREATURE
  ));

  public final static Set<String> VALID_DMG_TYPES = new HashSet<>(Arrays.asList(
      "Healing", "SLASH", "STRIKE", "PIERCE", "CHEMS", "FIRE", "COLD", "SHOCK"
  ));

  protected Ability s = null;
  protected String colorStr = null;
  protected Vector<StatusEffect> se = null;

  @Override
  public void init(int id, String name, String origin) {
    s = new Ability(id, name, origin);
  }

  // ManaCost: 15
  public void manaCost(String val) {
    s.setManaCost(parseInt(val));
  }

  public void animationId(String val) {
    s.setAnimationId(parseInt(val));
  }

  // Cooldown: 20
  public void cooldown(String val) {
    s.setCooldown(parseInt(val));
  }

  // Range: 2
  public void range(String val) {
    String[] parts = val.split(" +");
    int r = parseInt(parts[0]);
    s.setRange(r);
    if (r == 0) {
      s.setTargetSelf(true);
    }
    if (VALID_TARGETS.contains(parts[1])) {
      s.setDamageType(parts[1]);
    }
    else {
      System.out.println("[AbilityBuilder] ERROR - Invalid target type for " + s + ": " + parts[1]);
    }
  }

  public void instant() {
    s.setInstant(true);
  }

  public void classId(String val) {
    s.setClassId(parseInt(val));
  }

  public void classLevel(String val) {
    s.setClassLevel(parseInt(val));
  }

  // -- Only fishing = 101
  public void jobSkillId(String val) {
    s.setJobSkillId(parseInt(val));
  }

  // -- Unused? Only 6 healing = 1
  public void familyId(String val) {
    s.setFamilyId(parseInt(val));
  }

  public void AoE(String val) {
    if ("square3".equals(val) || "3x3".equals(val)) {
      val = "0,0;-1,0;1,0;0,-1;-1,-1;1,-1;0,1;-1,1;1,1";
    }
    if ("frame3".equals(val) || "square3hollow".equals(val) || "3x3hollow".equals(val)) {
      val = "-1,0;1,0;0,-1;-1,-1;1,-1;0,1;-1,1;1,1";
    }
    else if ("cross3".equals(val) || "+3".equals(val)) {
      val = "0,0;-1,0;1,0;0,-1;0,1";
    }
    else if ("x3".equals(val)) {
      val = "0,0;-1,-1;1,-1;-1,1;1,1";
    }
    else if ("horizontal3".equals(val)) {
      val = "-1,0;0,0;1,0";
    }
    else if ("vertical3".equals(val)) {
      val = "0,-1;0,0;0,1";
    }
    else if ("cross5".equals(val) || "+5".equals(val)) {
      val = "0,0;-1,0;1,0;0,-1;0,1";
    }
    else if ("diamond5".equals(val)) {
      val = "0,0;-1,0;-2,0;1,0;2,0;0,-1;1,-1;-1,-1;0,1;1,1;-1,1;0,-2;0,2";
    }
    else if ("diamond5hollow".equals(val)) {
      val = "-1,0;-2,0;1,0;2,0;0,-1;1,-1;-1,-1;0,1;1,1;-1,1;0,-2;0,2";
    }
    else if ("7x3hollow".equals(val)) {
      val = "-1,0;-2,0;-3,0;1,0;2,0;3,0;-1,-1;-2,-1;-3,-1;0,-1;1,-1;2,-1;3,-1;-3,1;-2,1;-1,1;0,1;1,1;2,1;3,1";
    }
    else if ("7x3".equals(val)) {
      val = "0,0;-1,0;-2,0;-3,0;1,0;2,0;3,0;-1,-1;-2,-1;-3,-1;0,-1;1,-1;2,-1;3,-1;-3,1;-2,1;-1,1;0,1;1,1;2,1;3,1";
    }
    else if (!VALID_AOE.matcher(val).matches()) {
      System.out.println("[AbilityBuilder] ERROR - Invalid AoE for ability " + s + ": " + val);
    }

    s.setAoE(val);
  }

  public void delay(String val) {
    s.setDelay(parseInt(val));
  }

  // TODO: make reveal its own property
  public void reveal(String val) {
    s.setDamageType("Reveal " + val);
  }

  public void damage(String val) {
    String[] parts = val.split(" +");
    if (parts[0].charAt(0) == 'x' || parts[0].charAt(0) == '*') {
      s.setWeaponDamageFactor(parseFloat(parts[0].substring(1)));
    } else {
      s.setDamage(parseInt(parts[0]));
    }
    s.setDamageType(parts[1]);
    if (VALID_DMG_TYPES.contains(parts[1])) {
      s.setDamageType(parts[1]);
    }
    else {
      System.out.println("[AbilityBuilder] ERROR - Invalid damage type for " + s + ": " + parts[1]);
    }
  }

  public void projectileId(String val) {
    s.setProjectileId(parseInt(val));
  }

  public void color(String val) {
    colorStr = val;
  }

  public void price(String val) {
    s.setPrice(parseInt(val));
  }

  // StatusEffects: 9
  public void statusEffects(String val) {
    if (se == null) {
      se = new Vector<>();
    }
    String StatusEffectsId[] = val.split(";");

    for (String statusEffectInfo : StatusEffectsId) {
      int statusEffectId = parseInt(statusEffectInfo);
      StatusEffect newSE = new StatusEffect(statusEffectId);
      se.add(newSE);
    }
  }

  public void spawnIds(String val) {
    s.setSpawnIds(val);
  }

  // -- Only Hawk Eye, Focus shot = 1
  public void projectileEffectId(String val) {
    s.setProjectileEffectId(parseInt(val));
  }

  // EquipReq: Weapon:3
  public void equipReq(String val) {
    s.setEquipReq(val);
  }

  public void graphicsNr(String val) {
    s.setGraphicsNr(parseInt(val));
  }

  // Description: Boosts attack speed during short period of time. Enables shooting while running.
  public void description(String val) {
    s.setDescription(val);
  }

  // CastingSpeed: 100
  public void castingSpeed(String val) {
    s.setCastingSpeed(parseInt(val));
  }

  public void buff() {
    s.setBuffOrNot(true);
  }

  public Ability build() {
    if (colorStr != null) {
      s.setColor(colorStr);
    }
    if (se != null) {
      se.trimToSize();
      s.setStatusEffects(se);
    }
    return s;
  }

  public static void main(String... args) {
    Map<Integer, Ability> m = new HashMap<>();
    Builder.load(args[0], AbilityBuilder.class, m);
    if (args.length>1) {
      if ("-".equals(args[1])) {
        for (Ability q : m.values()) {
          System.out.println(q);
        }
      }
      else {
        System.out.println(m.get(parseInt(args[1])));
      }
    }
  }
}
