package abilitysystem;

public class Skill {

  private int Id;
  private String Name;
  private String Type;

  private int Level;
  private int SP;
  private int SPnext;

  public Skill(int newId) {
    setId(newId);
  }

  public void addSP(int addedSP) {
    SP += addedSP;
    if (SP > SPnext) {
      SP = SPnext;
    }
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public String getType() {
    return Type;
  }

  public void setType(String type) {
    Type = type;
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

  public void setSPnext(int nextSP) {
    SPnext = nextSP;
  }

  public int getSPBarWidth(int max) {
    return Math.round(1.0f * SP * max / SPnext);
  }

  public int getId() {
    return Id;
  }

  public void setId(int id) {
    Id = id;
  }
}
