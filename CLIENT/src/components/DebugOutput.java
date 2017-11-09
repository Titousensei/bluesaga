package components;

public class DebugOutput {
  private boolean on_;

  public DebugOutput(boolean on) {
    on_ = on;
  }

  public boolean isON() {
    return on_;
  }

  public void setON(boolean on) {
    on_ = on;
  }

  public void print(String msg) {
    if (on_) {
      System.out.println(msg);
    }
  }
}
