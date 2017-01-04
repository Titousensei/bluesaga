package player_classes;

public class HunterClass extends BaseClass {

  public HunterClass() {
    super(3);

    name = "Hunter";

    bgColor = "228,136,68";

    baseClassId = 3;

    resetStartStats();

    getStartStats().setValue("MAX_HEALTH", 100);
    getStartStats().setValue("MAX_MANA", 50);

    getStartStats().setValue("STRENGTH", 5);
    getStartStats().setValue("INTELLIGENCE", 5);
    getStartStats().setValue("AGILITY", 8);

    getLevelStats().reset();

    getLevelStats().setValue("MAX_HEALTH", 8);
    getLevelStats().setValue("MAX_MANA", 4);

    getLevelStats().setValue("STRENGTH", 1);
    getLevelStats().setValue("INTELLIGENCE", 1);
    getLevelStats().setValue("AGILITY", 2);

    getLevelStats().setValue("SPEED", 1);
  }

  @Override
  public boolean gainsXP() { return false; }
}
