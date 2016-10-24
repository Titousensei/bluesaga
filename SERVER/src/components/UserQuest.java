package components;

public class UserQuest {

  public final Quest questRef;
  private int status; // 0 = new, 1 = accepted, 2 = get reward, 3 = completed

  public UserQuest(Quest quest) {
    questRef = quest;
  }

  public int getStatus() { return status; }
  public void setStatus(int val) { status = val; }
}
