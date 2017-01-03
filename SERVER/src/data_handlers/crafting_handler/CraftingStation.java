package data_handlers.crafting_handler;

import java.util.Vector;

public enum CraftingStation
{
  Bonfire("crafting/fire", 100),
  Stela("crafting/tablet", 103);

  public final String id;
  public final int skillId;

  private CraftingStation(String id, int skill)
  {
    this.id = id;
    this.skillId = skill;
  }

  public static CraftingStation find(String id) {
    for (CraftingStation cs : CraftingStation.values()) {
      if (cs.id.equals(id)) {
        return cs;
      }
    }
    return null;
  }
}
