package data_handlers.monster_handler.ai_types;

import java.util.List;
import java.awt.Point;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;

import creature.Creature;
import creature.Creature.CreatureType;
import creature.Npc;
import data_handlers.monster_handler.MonsterHandler;
import map.Tile;
import map.WorldMap;
import network.Server;
import utils.ServerMessage;
import utils.Spiral;

public class BaseAI {

  protected final Npc me;

  protected boolean hasMoved  = false;
  protected boolean loseAggro = false;

  protected BaseAI(Npc monster) {
    me = monster;
  }

  // return true if moved
  public void doAggroBehaviour() {}

  public void becomeAggro() {}

  public void hitByAttack(int damage) {}

  public boolean takeHasMoved() {
    if (hasMoved) {
      hasMoved  = false;
      return true;
    }
    return false;
  }

  public boolean takeLoseAggro() {
    if (loseAggro) {
      loseAggro  = false;
      return true;
    }
    return false;
  }

  public static boolean moveToward(Creature cr, int goalX, int goalY, int goalZ)
  {
    int oldX = cr.getX();
    int oldY = cr.getY();
    int oldZ = cr.getZ();

    if (goalZ != oldZ) {
      ServerMessage.println(true, "Can't moveToward across Z",
          cr.getX(), ",", cr.getY(), ",", oldZ,
          " and ", goalX, ",", goalY, ",", goalZ);
      return false;
    }

    // MAKE A PATHFINDING MAP
    PathFinder pathMap = new PathFinder(oldX, oldY, goalX, goalY, goalZ);
    AStarPathFinder pathfinder =
        new AStarPathFinder(pathMap, pathMap.searchMax, MonsterHandler.diagonalWalk);
    Path foundPath = null;

    try {
      foundPath = pathfinder.findPath(cr, pathMap.startX, pathMap.startY, pathMap.goalX, pathMap.goalY);
    } catch (ArrayIndexOutOfBoundsException e) {
      ServerMessage.println(true, "Can't find path between ",
          oldX, ",", oldY, " and ", goalX, ",", goalY);
    }

    if (foundPath != null) {
      int stepX = foundPath.getX(1) + pathMap.offsetX;
      int stepY = foundPath.getY(1) + pathMap.offsetY;

      if (Server.WORLD_MAP.isPassableTile(cr, stepX, stepY, goalZ)) {
        Server.WORLD_MAP.getTile(oldX, oldY, oldZ)
                        .setOccupant(CreatureType.None, null);
        cr.walkTo(stepX, stepY, goalZ);
        Server.WORLD_MAP.getTile(cr.getX(), cr.getY(), cr.getZ())
                        .setOccupant(CreatureType.Monster, cr);
        cr.startMoveTimer((stepX != cr.getX()) && (stepY != cr.getY()));
        return true;
      }
    }

    return false;
  }


  protected void moveAway(Creature target) {
    // Find free tile that is outside range
    Spiral s = new Spiral(10, 10);
    List<Point> l = s.spiral();

    Point foundTile = null;

    for (Point p : l) {
      if (p.getX() != 0 || p.getY() != 0) {
        int escapeX = (int) (me.getX() + p.getX());
        int escapeY = (int) (me.getY() + p.getY());

        double distToTarget =
            Math.sqrt(
                Math.pow(escapeX - target.getX(), 2) + Math.pow(escapeY - target.getY(), 2));
        if (distToTarget >= me.getAttackRange()) {
          Tile tryTile = Server.WORLD_MAP.getTile(escapeX, escapeY, me.getZ());
          if (tryTile!= null && tryTile.isPassableNonAggro()) {
            moveToward(me, escapeX, escapeY, me.getZ());
            return;
          }
        }
      }
    }
  }

  protected void chaseTarget(Creature target, int chaseRange) {
    // Target is too far away for attack, chase target if within chase range
    double distToTarget =
            Math.sqrt(
                Math.pow(me.getX() - target.getX(), 2) + Math.pow(me.getY() - target.getY(), 2));

            // IF INSIDE CHASE RANGE - AGGRORANGE * 3
    if (distToTarget < chaseRange
        && distToTarget > me.getAttackRange()
        && me.getZ() == target.getZ()) {

      hasMoved = moveToward(me, target.getX(), target.getY(), target.getZ());
    }
  }

  public static BaseAI newAi(Npc me) {
    if (me.getAttackRange() > 2) {
      return new Ranged(me);
    } else if (me.getCreatureId() == 63 || me.getCreatureId() == 77) {
      return new Shy(me);
    } else {
      return new Melee(me);
    }
  }
}
