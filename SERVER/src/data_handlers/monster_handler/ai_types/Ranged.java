package data_handlers.monster_handler.ai_types;

import creature.Creature;
import creature.Npc;

public class Ranged extends BaseAI {

  private boolean isFleeing = false;

  public Ranged(Npc monster) {
    super(monster);
  }

  @Override
  public void hitByAttack(int damage) {
    if (damage>0) {
      isFleeing = true;
    }
  }

  /**
   * If Attacked, run away and keep attacking
   */
  @Override
  public void doAggroBehaviour() {
    Creature target = me.getAggroTarget();
    int dX = target.getX() - me.getX();
    int dY = target.getY() - me.getY();
    double distToTarget = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));

    // Check if fleeing target, then step back
    if (isFleeing && Math.floor(distToTarget) < me.getAttackRange()) {
      moveAway(target);
    } else {
      // Target is too far away for attack, chase target if within chase range
      isFleeing = false;
      chaseTarget(target, me.getAggroRange() * 2);
    }
  }
}
