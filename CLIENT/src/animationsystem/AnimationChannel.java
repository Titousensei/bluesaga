package animationsystem;

import java.util.*;

public class AnimationChannel {

  private List<PartAnimation> animations;
  private float waitItr = 0.0f;

  public AnimationChannel() {
    animations = new ArrayList<>(8);
  }

  /**
   * Constructor with time parameter
   * @param wait frames to wait till start
   */
  public AnimationChannel(float waitItr) {
    animations = new ArrayList<>(8);
    setWaitItr(waitItr);
  }

  public void add(PartAnimation animation) {
    animations.add(animation);
  }

  public PartAnimation get(int position) {
    return animations.get(position);
  }

  public void remove(int position) {
    animations.remove(position);
  }

  public int size() {
    return animations.size();
  }

  public boolean isActive() {
    if (waitItr > 0) {
      waitItr--;
      return false;
    }
    return true;
  }

  public void setWaitItr(float waitItr) {
    this.waitItr = waitItr;
  }
}
