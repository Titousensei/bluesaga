package data_handlers.item_handler;

import java.util.*;

import utils.RandomUtils;

public enum Modifier
{
  // .values() returns the values in the order they are declared
  Legendary (8,  6, 0.000001, 5.0, "247,66,234"),   // .000001  1.0570824524312898E-6
  Epic      (7,  5, 0.000011, 3.0, "249,81,81"),    // .00001   2.6449433426177612E-5
  Master    (6,  4, 0.000111, 2.0, "77,176,255"),   // .0001    1.0609035621405735E-4\
  Champion  (5,  3, 0.001111, 1.5, "81,251,94"),    // .001     3.827871976744301E-4
  Great     (4,  2, 0.011111, 1.2, "255,175,0"),    // .01      0.009053735103359212
  Reinforced(3,  1, 0.111111, 1.1, "255,234,116"),  // .10      0.11466120531007765
  Regular   (0,  0, 0.87,     1.0, "255,255,255"),  // .758889  0.718
  Weakened  (2, -1, 0.97,     .50, "200,200,200"),  // .10      0.11466120531007765
  Broken    (1, -2, 1.0,      .25, "100,100,100");  // .03      0.04310747020671842

  public final static Map<String, Float> ITEM_COEF = new HashMap<>();

  static {
    ITEM_COEF.put("Weapon",   .010f);
    ITEM_COEF.put("OffHand",  .016f);
    ITEM_COEF.put("Head",     .016f);
    ITEM_COEF.put("Amulet",   .020f);
    ITEM_COEF.put("Artifact", .020f);
  };

  public final int id;
  public final int bonus;
  public final double dropRate;
  public final String color;
  public final double priceCoef;

  private Modifier(int id, int bonus, double dropRate, double priceCoef, String color) {
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

  public static Modifier random(double bonus) {
    double chance = RandomUtils.getPercent() - bonus;
    for (Modifier mod : Modifier.values()) {
      if (mod.dropRate >= chance) {
        return mod;
      }
    }
    return Regular;
  }
}
