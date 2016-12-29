package components;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import data_handlers.item_handler.Item;
import utils.ServerGameInfo;

public class Gathering
{
  private final int id;
  private final String name;
  private final String origin;

  private int ParentQuestId;

  private String sourceName;
  private int skillLevel;
  private int skillId;
  private int resourceId;

  Gathering(int id, String name, String origin) {
    this.id = id;
    this.name = name;
    this.origin = origin;
  }

  /****************************************
   *                                      *
   *             GETTER/SETTER            *
   *                                      *
   *                                      *
   ****************************************/
  public int getId() { return id; }

  public String getName() { return name; }

  public String origin() { return origin; }

  public String getSourceName() { return sourceName; }
  public void setSourceName(String val) { sourceName = val; }

  public int getSkillLevel() { return skillLevel; }
  public void setSkillLevel(int val) { skillLevel = val; }

  public int getSkillId() { return skillId; }
  public void setSkillId(int val) { skillId = val; }

  public int getResourceId() { return resourceId; }
  public void setResourceId(int val) { resourceId = val; }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Gathering{").append(id)
      .append(" / ").append(name)
      .append(", sourceName: ").append(sourceName)
      .append(", skillLevel: ").append(skillLevel)
      .append(", skillId: ").append(skillId)
      .append(", resourceId: ").append(resourceId)
      .append('}');

    return sb.toString();
  }
}
