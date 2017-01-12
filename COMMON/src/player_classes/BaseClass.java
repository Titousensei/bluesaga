package player_classes;

import java.util.Map.Entry;

import utils.GameInfo;
import components.Stats;

public class BaseClass {
  public int id;
  public String name;

  public int baseClassId = 0;

  public String bgColor = "0,0,0";
  public String textColor = "20,20,20";

  private Stats startStats = new Stats();
  private Stats levelStats = new Stats();

  private int xp = 0;
  public int nextXP = 100;

  public int level = 1;

  public boolean available = true;

  /**
   * Constructor
   * @param id
   */
  protected BaseClass(int id) {
    this.id = id;
  }

  public static BaseClass copy(BaseClass copy) {
    try {
      BaseClass ret = copy.getClass().newInstance();

      ret.name = copy.name;
      ret.baseClassId = copy.baseClassId;
      ret.bgColor = copy.bgColor;
      ret.textColor = copy.textColor;
      ret.nextXP = copy.nextXP;

      for (Entry<String, Integer> entry : copy.getStartStats().getHashMap().entrySet()) {
        String key = entry.getKey();
        int value = entry.getValue();

        ret.getStartStats().getHashMap().put(key, value);
      }

      for (Entry<String, Integer> entry : copy.getLevelStats().getHashMap().entrySet()) {
        String key = entry.getKey();
        int value = entry.getValue();

        ret.getLevelStats().getHashMap().put(key, value);
      }

      return ret;
    } catch (InstantiationException | IllegalAccessException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void resetStartStats() {
    startStats.reset();
    startStats.addValue("SPEED", 80);
    startStats.addValue("ATTACKSPEED", 75);
    startStats.addValue("ACCURACY", 64);
    startStats.addValue("EVASION", 5);
    startStats.addValue("CRITICAL_HIT", 0);
  }

  /**
   * @return level up
   */
  public boolean addXP(int addedXP) {
    xp += addedXP;
    if (xp >= nextXP) {
      xp = 0;
      level++;
      nextXP = GameInfo.classNextXP.get(level);
      return true;
    }

    return false;
  }

  public boolean gainsXP() { return true; }

  public int getXPBarWidth(int Max) {
    float fxp = xp;
    float fMaxXP = nextXP;
    float spBarWidth = (fxp / fMaxXP) * Max;
    return Math.round(spBarWidth);
  }

  /**
   * Getters and setters
   * @return
   */
  public Stats getStartStats() {
    return startStats;
  }

  public Stats getLevelStats() {
    return levelStats;
  }

  public int getXp() {
    return xp;
  }

  public void setXp(int xp) {
    this.xp = xp;
  }
}
