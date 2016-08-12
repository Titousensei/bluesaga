package data_handlers.monster_handler.ai_types;

import creature.Creature;
import creature.Npc;
import map.Tile;
import network.Server;
import utils.MathUtils;

public class Melee extends BaseAI {

  public Melee(Npc monster) {
    super(monster);
  }

  public void doAggroBehaviour() {
    Creature target = me.getAggroTarget();

    // If Guardian then check if target is on water, then drop aggro
    if (me.getOriginalAggroType() == 5) {
      Tile targetTile = Server.WORLD_MAP.getTile(target.getX(), target.getY(), target.getZ());
      if (targetTile != null) {
        if (targetTile.isWater()) {
          return;
        }
      }
    }

    int dX = target.getX() - me.getX();
    int dY = target.getY() - me.getY();

    double distToTarget = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
    boolean nearTarget = (Math.floor(distToTarget) <= me.getAttackRange());

    // CHECK IF TARGET IS CLOSE ENOUGH TO ATTACK
    if (nearTarget) {
      float angleNeeded = MathUtils.angleBetween(-dX, -dY);

      if (angleNeeded < 0) {
        angleNeeded = 360 + angleNeeded;
      }

      if (Math.abs(me.getGotoRotation() - angleNeeded) < .01  ) {
        me.setGotoRotation(angleNeeded);
        hasMoved = true;
      }
    }
    else {
    // IF FAR AWAY FROM PLAYER USE PATHFINDING
      chaseTarget(target, me.getAggroRange() * 2);
    }
  }
}
