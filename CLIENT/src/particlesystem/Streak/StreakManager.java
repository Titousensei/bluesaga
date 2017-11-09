package particlesystem.Streak;

import java.util.*;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import screens.Camera;
import screens.ScreenHandler;

public class StreakManager {

  private List<Streak> myStreaks;

  public StreakManager() {
    myStreaks = new ArrayList<>(10);
  }

  public void Update(float aElapsedTime) {

    for (Streak Streak : myStreaks) {
      Streak.Update(aElapsedTime);
    }

    // Remove Streaks
    Iterator<Streak> it = myStreaks.iterator();
    while(it.hasNext()) {
      Streak st = it.next();
      if (st.ShouldBeRemoved()) {
        it.remove();
      }
    }
  }

  public void Draw(Graphics g, Camera aCamera) {

    for (Streak Streak : myStreaks) {
      Streak.Render(g, aCamera);
    }
  }

  public Streak SpawnStreak(Vector2f aPosition, int aStreakId) {

    StreakType streakType = ScreenHandler.myStreakContainer.GetStreakByID(aStreakId);

    Streak newStreak = new Streak(aPosition, streakType);
    myStreaks.add(newStreak);

    return newStreak;
  }

  public Streak SpawnStreak(Vector2f aPosition, StreakType aStreakType) {

    Streak newStreak = new Streak(aPosition, aStreakType);
    myStreaks.add(newStreak);

    return newStreak;
  }

  public void RemoveAllStreaks() {
    myStreaks.clear();
  }
}
