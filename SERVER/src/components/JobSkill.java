package components;

import java.sql.ResultSet;
import java.sql.SQLException;
import utils.XPTables;

public class JobSkill {

  private final int Id;
  private final String Name;
  private final String origin;

  private String Type;

  private int Level;
  private int SP;

  JobSkill(int id, String name, String origin) {
    this.Id = id;
    this.Name = name;
    this.origin = origin;
  }

  public JobSkill(JobSkill copy) {
    Id  = copy.getId();
    Name = copy.getName();
    Type = copy.getType();
    origin = copy.getOrigin();
    SP = 0;
    Level = 1;
  }

  public boolean addSP(int addedSP) {
    SP += addedSP;
    boolean levelUp = false;

    while (SP >= XPTables.getNextLevelSP(Level + 1)) {
      SP = SP - XPTables.getNextLevelSP(Level + 1);
      Level++;
      levelUp = true;
    }

    return levelUp;
  }

  public int getId() {
    return Id;
  }

  public String getName() {
    return Name;
  }

  public String getOrigin() {
    return origin;
  }

  public int getLevel() {
    return Level;
  }

  public void setLevel(int level) {
    Level = level;
  }

  public int getSP() {
    return SP;
  }

  public void setSP(int sp) {
    SP = sp;
  }

  public String getType() {
    return Type;
  }

  public void setType(String type) {
    Type = type;
  }

  @Override
  public String toString() {
    return Name + " (" + Id +") Lvl" + Level;
  }
}
