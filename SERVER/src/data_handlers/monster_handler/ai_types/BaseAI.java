package data_handlers.monster_handler.ai_types;

import java.util.List;
import java.awt.Point;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;

import creature.Creature;
import creature.Creature.CreatureType;
import creature.Npc;
import creature.PathMover;
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

  protected void moveToward(int goalX, int goalY, int goalZ) {

    int monsterOldX = me.getX();
    int monsterOldY = me.getY();
    int monsterOldZ = me.getZ();

    // MAKE A PATHFINDING MAP
    WorldMap pathMap = new WorldMap();
    pathMap.createPathMap(monsterOldX, monsterOldY, monsterOldZ, goalX, goalY, goalZ);

    AStarPathFinder pathfinder =
        new AStarPathFinder(pathMap, pathMap.getPathMapSize(), MonsterHandler.diagonalWalk);
    PathMover mover = new PathMover(CreatureType.Monster);
    mover.setTarget(goalX, goalY, goalZ);
    Path foundPath = null;

    try {
      foundPath =
          pathfinder.findPath(
              mover,
              me.getX() - pathMap.getPathMapStartX(),
              me.getY() - pathMap.getPathMapStartY(),
              goalX - pathMap.getPathMapStartX(),
              goalY - pathMap.getPathMapStartY());
    } catch (ArrayIndexOutOfBoundsException e) {
      ServerMessage.println(true, "Can't find path between ",
          me.getX(), ",", me.getY(), " and ", goalX, ",", goalY);
      foundPath = null;
    }

    if (foundPath != null) {
      int stepX = foundPath.getX(1) + pathMap.getPathMapStartX();
      int stepY = foundPath.getY(1) + pathMap.getPathMapStartY();
      int stepZ = me.getZ();

      if (Server.WORLD_MAP.isPassableTileForMonster(me, stepX, stepY, stepZ)) {
        int diagonalMove = 0;

        me.walkTo(stepX, stepY, stepZ);

        if (stepX != me.getX()) {
          diagonalMove++;
        }
        if (stepY != me.getY()) {
          diagonalMove++;
        }

        me.startMoveTimer(diagonalMove > 1);
        hasMoved = true;
      }
    }

    // FREE / OCCUPY TILES
    if (hasMoved) {
      Server.WORLD_MAP
          .getTile(monsterOldX, monsterOldY, monsterOldZ)
          .setOccupant(CreatureType.None, null);

      Server.WORLD_MAP
          .getTile(me.getX(), me.getY(), me.getZ())
          .setOccupant(CreatureType.Monster, me);
    }
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
            moveToward(escapeX, escapeY, me.getZ());
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

      moveToward(target.getX(), target.getY(), target.getZ());
    }
  }

  public static BaseAI newAi(Npc me) {
    if (me.getAttackRange() > 2) {
      return new RangedShy(me);
    } else if (me.getCreatureId() == 63 || me.getCreatureId() == 77) {
      return new Shy(me);
    } else {
      return new Melee(me);
    }
  }
}
