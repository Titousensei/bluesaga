package player_classes;

public class MageClass extends BaseClass {

  public MageClass() {
    super(2);

    name = "Mage";

    baseClassId = 2;

    bgColor = "238,138,234";

    resetStartStats();

    getStartStats().setValue("MAX_HEALTH", 100);
    getStartStats().setValue("MAX_MANA", 50);

    getStartStats().setValue("STRENGTH", 5);
    getStartStats().setValue("INTELLIGENCE", 8);
    getStartStats().setValue("AGILITY", 5);

    getLevelStats().reset();

    getLevelStats().setValue("MAX_HEALTH", 6);
    getLevelStats().setValue("MAX_MANA", 8);

    getLevelStats().setValue("STRENGTH", 1);
    getLevelStats().setValue("INTELLIGENCE", 2);
    getLevelStats().setValue("AGILITY", 1);

    getLevelStats().setValue("SPEED", 1);
  }

  @Override
  public boolean gainsXP() { return false; }
}
