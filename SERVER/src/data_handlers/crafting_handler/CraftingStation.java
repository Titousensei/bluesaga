package data_handlers.crafting_handler;

import java.util.Vector;

public enum CraftingStation
{
  Bonfire(100),
  Stela(103);

  public final int skillId;

  private CraftingStation(int skill)
  {
    skillId = skill;
  }
}
