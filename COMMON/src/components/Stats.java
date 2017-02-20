package components;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Stats {

  public final static Set<String> NAMES = new HashSet(Arrays.asList(
        "STRENGTH", "INTELLIGENCE", "AGILITY",
        "SPEED", "CRITICAL_HIT", "EVASION", "ACCURACY", "ATTACKSPEED",
        "MAX_HEALTH", "MAX_MANA", "HEALTH_REGAIN", "MANA_REGAIN",
        "FIRE_DEF", "COLD_DEF", "SHOCK_DEF", "CHEMS_DEF", "MIND_DEF", "MAGIC_DEF",
        "ACCURACY", "ARMOR"));

  private HashMap<String, Integer> Stats;

  public Stats() {
    Stats = new HashMap<>();
    reset();
  }

  public void setValues(ResultSet rs) {
    Stats.clear();
    try {
      // PRIMARY STATS
      Stats.put("STRENGTH", rs.getInt("STRENGTH"));
      Stats.put("INTELLIGENCE", rs.getInt("INTELLIGENCE"));
      Stats.put("AGILITY", rs.getInt("AGILITY"));
      Stats.put("SPEED", rs.getInt("SPEED"));

      // SECONDARY STATS
      Stats.put("CRITICAL_HIT", rs.getInt("CRITICAL_HIT"));
      Stats.put("EVASION", rs.getInt("EVASION"));
      Stats.put("ACCURACY", rs.getInt("ACCURACY"));
      Stats.put("ATTACKSPEED", rs.getInt("AttackSpeed"));

      // HEALTH AND MANA
      Stats.put("MAX_HEALTH", rs.getInt("MAX_HEALTH"));
      Stats.put("MAX_MANA", rs.getInt("MAX_MANA"));

      Stats.put("HEALTH_REGAIN", 0);
      Stats.put("MANA_REGAIN", 0);

      // RESISTANCE STATS
      Stats.put("FIRE_DEF", rs.getInt("FIRE_DEF"));
      Stats.put("COLD_DEF", rs.getInt("COLD_DEF"));
      Stats.put("SHOCK_DEF", rs.getInt("SHOCK_DEF"));
      Stats.put("CHEMS_DEF", rs.getInt("CHEMS_DEF"));
      Stats.put("MIND_DEF", rs.getInt("MIND_DEF"));
      Stats.put("MAGIC_DEF", rs.getInt("MAGIC_DEF"));

      Stats.put("ACCURACY", rs.getInt("ACCURACY"));

      // ATTACK STATS
      Stats.put("ARMOR", rs.getInt("ARMOR"));

    } catch (SQLException e1) {
      e1.printStackTrace();
    }
  }

  public void reset() {
    Stats.clear();

    // PRIMARY STATS
    Stats.put("STRENGTH", 0);
    Stats.put("INTELLIGENCE", 0);
    Stats.put("AGILITY", 0);
    Stats.put("SPEED", 0);

    // SECONDARY STATS
    Stats.put("CRITICAL_HIT", 0);
    Stats.put("EVASION", 0);
    Stats.put("ACCURACY", 0);
    Stats.put("ATTACKSPEED", 0);

    // HEALTH AND MANA
    Stats.put("MAX_HEALTH", 0);
    Stats.put("MAX_MANA", 0);

    Stats.put("HEALTH_REGAIN", 0);
    Stats.put("MANA_REGAIN", 0);

    // MAGIC STATS
    Stats.put("FIRE_DEF", 0);
    Stats.put("COLD_DEF", 0);
    Stats.put("SHOCK_DEF", 0);
    Stats.put("CHEMS_DEF", 0);
    Stats.put("MIND_DEF", 0);
    Stats.put("MAGIC_DEF", 0);

    // ATTACK STATS
    Stats.put("ARMOR", 0);
  }

  public void setValue(String StatType, int StatValue) {
    Stats.put(StatType, StatValue);
  }

  public int getValue(String StatType) {
    if (Stats.get(StatType) != null) {
      return Stats.get(StatType);
    }
    return 0;
  }

  public void addStats(Stats plusStats) {
    for (Map.Entry<String, Integer> entry : Stats.entrySet()) {
      String key = entry.getKey();
      Integer value = entry.getValue();
      Stats.put(key, value + plusStats.getValue(key));
    }
  }

  public void fraction(float ratio) {
    for (Map.Entry<String, Integer> entry : Stats.entrySet()) {
      String key = entry.getKey();
      Integer value = entry.getValue();
      Stats.put(key, Math.round(value * ratio));
    }
  }

  public void addValue(String statType, int StatValue) {
    if (Stats.containsKey(statType)) {
      int newvalue = Stats.get(statType) + StatValue;
      if (newvalue>0) {
        Stats.put(statType, newvalue);
      }
      else {
        Stats.put(statType, Integer.valueOf(0));
      }
    } else {
      Stats.put(statType, StatValue);
    }
  }

  public HashMap<String, Integer> getHashMap() {
    return Stats;
  }

  public void clear() {
    Stats.clear();
  }
}
