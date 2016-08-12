package data_handlers.monster_handler.ai_types;

import creature.Creature;
import creature.Npc;

public class RangedMaintained extends BaseAI {

  public RangedMaintained(Npc monster) {
    super(monster);
  }

  /**
   * Maintain distance with target
   */
  @Override
  public void doAggroBehaviour() {
    Creature target = me.getAggroTarget();
    int dX = target.getX() - me.getX();
    int dY = target.getY() - me.getY();

    double distToTarget = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));

    // CHECK IF TARGET IS TOO CLOSE, THEN STEP BACK
    if (Math.floor(distToTarget) < me.getAttackRange()) {
      moveAway(target);
    } else {
      chaseTarget(target, me.getAggroRange() * 2);
    }
  }
}
