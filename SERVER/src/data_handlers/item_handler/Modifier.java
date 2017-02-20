package data_handlers.item_handler;

import java.util.*;

import utils.RandomUtils;

public enum Modifier
{
  // .values() returns the values in the order they are declared
                                                     //  1         2          3          4          5          6         7          8
  Legendary (8,  6, 5.0, "247,66,234",  new double[] { 0.000032, 0.0000384, 0.0000448, 0.0000512, 0.0000576, 0.000064, 0.0000704, 0.0000768 }),
  Epic      (7,  5, 3.0, "249,81,81",   new double[] { 0.000192, 0.0002304, 0.0002761, 0.0003313, 0.0003976, 0.000477, 0.0005725, 0.0006871 }),
  Master    (6,  4, 2.0, "77,176,255",  new double[] { 0.000992, 0.0011904, 0.0014281, 0.0017137, 0.0020566, 0.002468, 0.0029615, 0.0035538 }),
  Champion  (5,  3, 1.5, "81,251,94",   new double[] { 0.004992, 0.0059904, 0.0071881, 0.0086257, 0.010351,  0.012421, 0.0149054, 0.0178865 }),
  Great     (4,  2, 1.2, "255,175,0",   new double[] { 0.024992, 0.0299904, 0.0359881, 0.0431857, 0.051823,  0.062188, 0.0746251, 0.0895501 }),
  Reinforced(3,  1, 1.1, "255,234,116", new double[] { 0.124992, 0.1499904, 0.1799881, 0.2159857, 0.259183,  0.311020, 0.3732235, 0.4478682 }),
  Regular   (0,  0, 1.0, "255,255,255", new double[] { 0.87,     0.8885714, 0.905286,  0.919492,  0.93157,   0.941835, 0.9505593, 0.9579754 } ),
  Weakened  (2, -1, .50, "200,200,200", new double[] { 0.97,     0.9742857, 0.978143,  0.981421,  0.98421,   0.986579, 0.9885917, 0.9903030 }),
  Broken    (1, -2, .25, "100,100,100", new double[] { 1.0,      1.0,       1.0,       1.0,       1.0,       1.0,      1.0,       1.0 }      );

  public final static Map<String, Float> ITEM_COEF = new HashMap<>();

  static {
    ITEM_COEF.put("Weapon",   0.10f);
    ITEM_COEF.put("OffHand",  0.16f);
    ITEM_COEF.put("Head",     0.16f);
    ITEM_COEF.put("Amulet",   0.20f);
    ITEM_COEF.put("Artifact", 0.20f);
  };

  public final int id;
  public final int bonus;
  public final double[] dropRate;
  public final String color;
  public final double priceCoef;

  private Modifier(int id, int bonus, double priceCoef, String color, double[] dropRate) {
    this.id = id;
    this.bonus = bonus;
    this.dropRate = dropRate;
    this.color = color;
    this.priceCoef = priceCoef;
  }

  public static Modifier get(int id) {
    if (id!=0) {
      for (Modifier mod : Modifier.values()) {
        if (mod.id == id) {
          return mod;
        }
      }
    }
    return Regular;
  }

  public static Modifier random(int level) {
    if (level >= Regular.dropRate.length) {
      level = Regular.dropRate.length - 1;
    }
    double chance = RandomUtils.getPercent();
    for (Modifier mod : Modifier.values()) {
      if (mod.dropRate[level] >= chance) {
        return mod;
      }
    }
    return Regular;
  }
}
