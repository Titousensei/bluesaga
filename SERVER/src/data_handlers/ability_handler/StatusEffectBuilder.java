package data_handlers.ability_handler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import components.Builder;
import components.Stats;

public class StatusEffectBuilder
extends Builder<StatusEffect>
{
  protected StatusEffect s = null;

  @Override
  public void init(int id, String name, String origin) {
    s = new StatusEffect(id, name, origin);
  }

  @Override
  protected boolean isDuplicateAllowed(String setter)
  { return "StatsModif".equals(setter); }

  public void statsModif(String val) {
    Stats mod = new Stats();
    String allStatsModifInfo[] = val.split(";");
    for (String statsModifInfo : allStatsModifInfo) {
      String statsModifSplit[] = statsModifInfo.split(":");
      String statsType = statsModifSplit[0];
      if (StatusEffect.VALID_STATS.contains(statsType)) {
        int statsEffect = parseInt(statsModifSplit[1]);
        mod.setValue(statsType, statsEffect);
      }
      else {
        System.out.println("[StatusEffectBuilder] ERROR - Invalid stat for " + s + ": " + statsType);
      }
    }
    s.setStatsModif(mod);
  }

  public void duration(String val) {
    s.setDuration(parseInt(val));
  }

  public void stackable() {
    s.setStackable(true);
  }

  public void repeatDamage(String val) {
    String[] parts = val.split(" +");
    s.setRepeatDamage(parseInt(parts[0]));
    if (AbilityBuilder.VALID_DMG_TYPES.contains(parts[1])) {
      s.setRepeatDamageType(parts[1]);
    }
    else {
      System.out.println("[StatusEffectBuilder] ERROR - Invalid damage type for " + s + ": " + parts[1]);
    }
  }

  public void color(String val) {
    s.setColor(val);
  }

  public void classId(String val) {
    s.setClassId(parseInt(val));
  }

  public void graphicsNr(String val) {
    s.setGraphicsNr(parseInt(val));
  }

  public void animationId(String val) {
    s.setAnimationId(parseInt(val));
  }

  public void sfx(String val) {
    s.setSfx(val);
  }

  public StatusEffect build() {
    return s;
  }

  public static void main(String... args) {
    Map<Integer, StatusEffect> m = new HashMap<>();
    Builder.load(args[0], StatusEffectBuilder.class, m);
    if (args.length>1) {
      if ("-".equals(args[1])) {
        for (StatusEffect q : m.values()) {
          System.out.println(q);
        }
      }
      else {
        System.out.println(m.get(parseInt(args[1])));
      }
    }
  }
}
