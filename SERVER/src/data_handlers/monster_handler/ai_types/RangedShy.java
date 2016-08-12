package data_handlers.monster_handler.ai_types;

import creature.Creature;
import creature.Npc;

public class RangedShy extends BaseAI {

  private boolean isFleeing = false;

  public RangedShy(Npc monster) {
    super(monster);
  }

  @Override
  public void hitByAttack(int damage) {
    if (damage>0) {
      isFleeing = true;
    }
  }

  /**
   * If Attacked, run away and lose aggro
   */
  @Override
  public void doAggroBehaviour() {
    Creature target = me.getAggroTarget();
    int dX = target.getX() - me.getX();
    int dY = target.getY() - me.getY();
    double distToTarget = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));

    if (isFleeing) {
      if (Math.floor(distToTarget) < me.getAttackRange()) {
        moveAway(target);
      }
      else {
        loseAggro = true;
        isFleeing = false;
      }
    } else {
      // Target is too far away for attack, chase target if within chase range
      chaseTarget(target, me.getAggroRange() * 2);
    }
  }
}
