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

  private HashMap<String, Integer> stats_;

  public Stats() {
    stats_ = new HashMap<>();
    reset();
  }

  public Stats(Map<String, Integer> stats) {
    stats_ = new HashMap<>(stats);
  }

  @Deprecated
  public void setValues(ResultSet rs) {
    stats_.clear();
    try {
      // PRIMARY STATS
      stats_.put("STRENGTH", rs.getInt("STRENGTH"));
      stats_.put("INTELLIGENCE", rs.getInt("INTELLIGENCE"));
      stats_.put("AGILITY", rs.getInt("AGILITY"));
      stats_.put("SPEED", rs.getInt("SPEED"));

      // SECONDARY STATS
      stats_.put("CRITICAL_HIT", rs.getInt("CRITICAL_HIT"));
      stats_.put("EVASION", rs.getInt("EVASION"));
      stats_.put("ACCURACY", rs.getInt("ACCURACY"));
      stats_.put("ATTACKSPEED", rs.getInt("AttackSpeed"));

      // HEALTH AND MANA
      stats_.put("MAX_HEALTH", rs.getInt("MAX_HEALTH"));
      stats_.put("MAX_MANA", rs.getInt("MAX_MANA"));

      stats_.put("HEALTH_REGAIN", 0);
      stats_.put("MANA_REGAIN", 0);

      // RESISTANCE STATS
      stats_.put("FIRE_DEF", rs.getInt("FIRE_DEF"));
      stats_.put("COLD_DEF", rs.getInt("COLD_DEF"));
      stats_.put("SHOCK_DEF", rs.getInt("SHOCK_DEF"));
      stats_.put("CHEMS_DEF", rs.getInt("CHEMS_DEF"));
      stats_.put("MIND_DEF", rs.getInt("MIND_DEF"));
      stats_.put("MAGIC_DEF", rs.getInt("MAGIC_DEF"));

      stats_.put("ACCURACY", rs.getInt("ACCURACY"));

      // ATTACK STATS
      stats_.put("ARMOR", rs.getInt("ARMOR"));

    } catch (SQLException e1) {
      e1.printStackTrace();
    }
  }

  public void reset() {
    stats_.clear();

    // PRIMARY STATS
    stats_.put("STRENGTH", 0);
    stats_.put("INTELLIGENCE", 0);
    stats_.put("AGILITY", 0);
    stats_.put("SPEED", 0);

    // SECONDARY STATS
    stats_.put("CRITICAL_HIT", 0);
    stats_.put("EVASION", 0);
    stats_.put("ACCURACY", 0);
    stats_.put("ATTACKSPEED", 0);

    // HEALTH AND MANA
    stats_.put("MAX_HEALTH", 0);
    stats_.put("MAX_MANA", 0);

    stats_.put("HEALTH_REGAIN", 0);
    stats_.put("MANA_REGAIN", 0);

    // MAGIC STATS
    stats_.put("FIRE_DEF", 0);
    stats_.put("COLD_DEF", 0);
    stats_.put("SHOCK_DEF", 0);
    stats_.put("CHEMS_DEF", 0);
    stats_.put("MIND_DEF", 0);
    stats_.put("MAGIC_DEF", 0);

    // ATTACK STATS
    stats_.put("ARMOR", 0);
  }

  public void setValue(String StatType, int StatValue) {
    stats_.put(StatType, StatValue);
  }

  public int getValue(String StatType) {
    if (stats_.get(StatType) != null) {
      return stats_.get(StatType);
    }
    return 0;
  }

  public void addStats(Stats plusStats) {
    for (Map.Entry<String, Integer> entry : stats_.entrySet()) {
      String key = entry.getKey();
      Integer value = entry.getValue();
      stats_.put(key, value + plusStats.getValue(key));
    }
  }

  public void fraction(float ratio) {
    for (Map.Entry<String, Integer> entry : stats_.entrySet()) {
      String key = entry.getKey();
      Integer value = entry.getValue();
      stats_.put(key, Math.round(value * ratio));
    }
  }

  public void addValue(String statType, int StatValue) {
    if (stats_.containsKey(statType)) {
      int newvalue = stats_.get(statType) + StatValue;
      stats_.put(statType, newvalue);
    } else {
      stats_.put(statType, StatValue);
    }
  }

  public HashMap<String, Integer> getHashMap() {
    return stats_;
  }

  public void clear() {
    stats_.clear();
  }

  @Override
  public String toString() {
    return "Stats" + stats_;
  }
}
