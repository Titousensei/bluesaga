package utils;

public enum ItemMagic {
    //    agicType SEID  color
    Burning("FIRE",  1, "230,82,62",  "FIRE_DEF"),  // 1
    Frozen( "COLD",  6, "0,150,255",  "COLD_DEF"),  // 2
    Shock(  "SHOCK", 7, "233,255,43", "SHOCK_DEF"), // 3
    Mighty( null,    0, "255,138,0",  "STRENGTH"),  // 4
    Toxic(  "CHEMS", 4, "247,36,250", "CHEMS_DEF"), // 5
    Haste(  null,    0, "186,255,96", "SPEED", "ATTACKSPEED");    // 6

    /*
      sqlite> select * from item_magic;
      Id|Name|ExtraBonus|StatusEffectId|Color
      1|Burning|FIRE_DEF,2|0|230,82,62
      2|Frozen|COLD_DEF,2|0|0,150,255
      3|Shock|SHOCK_DEF,2|0|233,255,43
      4|Mighty|STRENGTH,2|0|255,138,0
      5|Toxic|CHEMS_DEF,2|0|247,36,250
      6|Haste|ATTACKSPEED,2|0|186,255,96
     */

  private final static ItemMagic[] byId_ = new ItemMagic[] {null,
    Burning, Frozen, Shock, Mighty, Toxic, Haste
  };

  public final String type;
  public final int statusEffectId;
  public final String color;
  public final String[] bonus;

  ItemMagic(String type, int statusEffectId, String color, String... bonus) {
    this.type = type;
    this.statusEffectId = statusEffectId;
    this.color = color;
    this.bonus = bonus;
  }

  public static ItemMagic getById(int id) {
    if (id >= 1 && id < byId_.length) {
      return byId_[id];
    }
    return null;
  }
}
