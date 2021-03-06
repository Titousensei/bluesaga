package data_handlers.monster_handler.ai_types;

import creature.Creature;
import creature.Npc;

public class Shy extends BaseAI {

  public Shy(Npc monster) {
    super(monster);
  }

  @Override
  public void doAggroBehaviour() {
    Creature target = me.getAggroTarget();
    int dX = target.getX() - me.getX();
    int dY = target.getY() - me.getY();

    double distToTarget = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));

    // CHECK IF TARGET IS TOO CLOSE, THEN STEP BACK
    if (Math.floor(distToTarget) < me.getAggroRange() + 5) {
      moveAway(target);
    }
  }
}
