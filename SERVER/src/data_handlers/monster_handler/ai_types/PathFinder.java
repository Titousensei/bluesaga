package data_handlers.monster_handler.ai_types;

import java.util.*;

import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import creature.Creature;
import creature.PlayerCharacter;
import map.WorldMap;
import map.Tile;
import network.Server;
import utils.MathUtils;

public class PathFinder
implements TileBasedMap
{
  public final int startX;
  public final int startY;
  public final int goalX;
  public final int goalY;
  public final int searchMax;
  public final int searchWidth;
  public final int searchHeight;
  public final int offsetX;
  public final int offsetY;
  public final int z;

  public PathFinder(int sx, int sy, int gx, int gy, int sz)
  {
    offsetX = Math.min(sx, gx) - 10;
    offsetY = Math.min(sy, gy) - 10;
    startX = sx - offsetX;
    startY = sy - offsetY;
    goalX = gx - offsetX;
    goalY = gy - offsetY;
    z = sz;
    searchWidth  = Math.abs(sx - gx) + 20;
    searchHeight = Math.abs(sy - gy) + 20;
    searchMax = (int) Math.round(2 * MathUtils.distance(sx - gx, sy - gy)) + 20;
  }

  @Override
  public boolean blocked(PathFindingContext pathMover, int x, int y)
  {
    if (x == goalX && y == goalY) {
      return false;
    }

    Creature mover = (Creature) pathMover.getMover();
    return !Server.WORLD_MAP.isPassableTile(mover, x + offsetX, y + offsetY, z);
  }

  @Override
  public float getCost(PathFindingContext arg0, int arg1, int arg2) {
    return 1;
  }

  @Override
  public int getHeightInTiles() {
    return searchHeight;
  }

  @Override
  public int getWidthInTiles() {
    return searchWidth;
  }

  @Override
  public void pathFinderVisited(int x, int y) {
  }
}
