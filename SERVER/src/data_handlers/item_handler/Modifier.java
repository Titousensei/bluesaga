package data_handlers.item_handler;

import java.util.*;

import utils.RandomUtils;

public enum Modifier
{
  // .values() returns the values in the order they are declared
  Legendary (8,  6, 5.0, "247,66,234",  new double[] { 0.000032, 0.0000384, 0.0000448, 0.0000512, 0.0000576, 0.000064,  0.0000704,  0.0000768 }),
  Epic      (7,  5, 3.0, "249,81,81",   new double[] { 0.000192, 0.0002304, 0.0002688, 0.0003072, 0.0003456, 0.000384,  0.0004224,  0.0004608 }),
  Master    (6,  4, 2.0, "77,176,255",  new double[] { 0.000992, 0.0011904, 0.0013888, 0.0015872, 0.0017856, 0.001984,  0.0021824,  0.0023808 }),
  Champion  (5,  3, 1.5, "81,251,94",   new double[] { 0.004992, 0.0059904, 0.0069888, 0.0079872, 0.0089856, 0.009984,  0.0109824,  0.0119808 }),
  Great     (4,  2, 1.2, "255,175,0",   new double[] { 0.024992, 0.0299904, 0.0349888, 0.0399872, 0.0449856, 0.049984,  0.0549824,  0.0599808 }),
  Reinforced(3,  1, 1.1, "255,234,116", new double[] { 0.124992, 0.1499904, 0.1749888, 0.1999872, 0.2249856, 0.249984,  0.2749824,  0.2999808 }),
  Regular   (0,  0, 1.0, "255,255,255", new double[] { 0.87    , 0.8885714, 0.9025   , 0.9133333, 0.922    , 0.929091,  0.935    ,  0.94 }     ),
  Weakened  (2, -1, .50, "200,200,200", new double[] { 0.97    , 0.9742857, 0.9775   , 0.98     , 0.982    , 0.983636,  0.985    ,  0.9861538 }),
  Broken    (1, -2, .25, "100,100,100", new double[] { 1.0     , 1.0      , 1.0      , 1.0      , 1.0      , 1.0     ,  1.0      ,  1.0 }      );

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
