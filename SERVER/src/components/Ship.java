package components;

public class Ship
{
  public final int id;
  private boolean show;

  public Ship(int newId) {
    id = newId;
    show = false;
  }

  public boolean isShow() { return show; }
  public void setShow(boolean val) { show = val; }

  public String getMessage() {
    switch (id) {
    case 0:
      return "";
    case 1:
      return "#messages.quest.travel_shallow_water";
    case 2:
      return "#messages.quest.travel_deep_water";
    default:
      return null;
    }
  }

  @Override
  public String toString() {
    switch (id) {
    case 0:
      return "None,0";
    case 1:
      return "raft,1";
    case 2:
      return "rowboat,2";
    default:
      return "rowboat," + id;
    }
  }
}
