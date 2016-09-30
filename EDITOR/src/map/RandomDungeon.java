package map;

import java.awt.Point;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import game.BP_EDITOR;
import game.MapImporter;
import game.MapImporter.Tile;
import utils.RandomUtils;

public class RandomDungeon {

  public final static int[] LVL10_LARVA_SCARABS = new int[] { 15, 9, 10 }; // Larvas, Scarabs, Ruby scarabs
  public final static int[] LVL10_SPIDER_BATS = new int[] { 6, 34, 4 }; // Spiders, Toxic Spider, Bat
  public final static int[] LVL10_SKELETON_RATS = new int[] { 28,29,8 }; // Bone piles, Skeletons, Ghoul Rat

  public final static int[] LVL20_SLIME_TOAD = new int[] { 7,5,76 }; // Green Slime, Rat, Rock Toad
  public final static int[] LVL20_BUNNY_FLOWER = new int[] { 43,41,42 }; // Bunny, Angry flower, Hungry hungry plant,
  public final static int[] LVL20_ELVES = new int[] { 44,45 }; // Spear elf, Dart Elf

  public final static int[] LVL30_SHROOMS = new int[] { 21,22,23 }; // Shroom, Toxic shroom, Giant Shroom
  public final static int[] LVL30_GOBLINS = new int[] { 14,11,59,48 }; // Goblin warrior, Goblin archer, Goblin bomber, Ogre
  public final static int[] LVL30_NECORS = new int[] { 73,74,71,75 }; // Necro goblin warrior, Necro goblin archer, Necromancer, Goblin King

  private final Map<String, Integer> map;
  public final int offset_x;
  public final int offset_y;
  public final int width;
  public final int height;

  public RandomDungeon(Map<String, Integer> m, int min_x, int max_x, int min_y, int max_y) {
    map = m;
    offset_x = min_x;
    offset_y = min_y;
    width = max_x - min_x;
    height = max_y - min_y;
  }

  public static RandomDungeon generate(int levelSize) {

    System.out.println("Generate START size=" + levelSize);

    Map<String, Integer> map = new HashMap<>();

    int brushX = 0;
    int brushY = 0;
    int brushDirection = RandomUtils.getInt(0, 7);

    int levelSizeItr = levelSize;

    int dirItr = 0;
    int holdDirThreshold = 3;

    int max_x = 0;
    int max_y = 0;
    int min_x = 0;
    int min_y = 0;

    while (levelSizeItr > 0) {
      // Use a cave brush to draw
      Iterator<Entry<Point, Integer>> brush_it =
          GeneratorBrush.getCaveBrush().entrySet().iterator();

      while (brush_it.hasNext()) {
        Entry<Point, Integer> b = brush_it.next();

        int tileX = brushX + b.getKey().x - 2;
        if (max_x < tileX) { max_x = tileX; }
        if (min_x > tileX) { min_x = tileX; }
        int tileY = brushY + b.getKey().y - 2;
        if (max_y < tileY) { max_y = tileY; }
        if (min_y > tileY) { min_y = tileY; }

        int tileNr = b.getValue() - 1;

        if (tileNr > -1) {
          if (!map.containsKey(tileX + "," + tileY)) {
            map.put(tileX + "," + tileY, tileNr);
          } else if (tileNr < map.get(tileX + "," + tileY)) {
            map.put(tileX + "," + tileY, tileNr);
          }
        }
      }

      // Move the brush in a random direction
      if (levelSizeItr > 0) {
        int oldBrushX = brushX;
        int oldBrushY = brushY;

        switch (brushDirection) {
        case 0:
          brushY--;
          break;
        case 1:
          brushY--;
          brushX++;
          break;
        case 2:
          brushX++;
          break;
        case 3:
          brushY++;
          brushX++;
          break;
        case 4:
          brushY++;
          break;
        case 5:
          brushY++;
          brushX--;
          break;
        case 6:
          brushX--;
          break;
        case 7:
          brushY--;
          brushX--;
          break;
        }

        boolean placeTiles = true;
        // Check if there is something there
        while (brush_it.hasNext()) {
          Entry<Point, Integer> b = brush_it.next();

          int tileX = brushX + b.getKey().x - 2;
          int tileY = brushY + b.getKey().y - 2;

          if (b.getValue() > 0) {
            if (map.containsKey(tileX + "," + tileY)) {
              placeTiles = false;
              break;
            }
          }
        }

        if (!placeTiles) {
          brushX = oldBrushX;
          brushY = oldBrushY;
          dirItr = holdDirThreshold;
        } else {
          // Lower size left to draw
          levelSizeItr--;
        }

        if (dirItr > holdDirThreshold) {
          brushDirection = RandomUtils.getInt(0, 7);
          dirItr = 0;
        } else {
          dirItr++;
        }
      }
    }

    // Placing ground around last spot so that stairs can fit
    Iterator<Entry<Point, Integer>> brush_it =
        GeneratorBrush.getCaveBrush().entrySet().iterator();

    while (brush_it.hasNext()) {
      Entry<Point, Integer> b = brush_it.next();

      int tileX = brushX + b.getKey().x - 2;
      int tileY = brushY + b.getKey().y - 2;

      int tileNr = b.getValue() - 1;

      if (tileNr > -1) {
        if (!map.containsKey(tileX + "," + tileY)) {
          map.put(tileX + "," + tileY, tileNr);
        } else if (tileNr < map.get(tileX + "," + tileY)) {
          map.put(tileX + "," + tileY, tileNr);
        }
      }
    }

    boolean removedTile = true;
    while (removedTile) {
      Iterator<Map.Entry<String, Integer>> fix_it = map.entrySet().iterator();
      removedTile = false;
      while (fix_it.hasNext()) {
        Map.Entry<String, Integer> pairs = fix_it.next();

        String tileCoord[] = pairs.getKey().split(",");
        int keyX = Integer.parseInt(tileCoord[0]);
        int keyY = Integer.parseInt(tileCoord[1]);

        if (pairs.getValue() == 1) {
          boolean removeTile = false;

          if (equalTile(map, (keyX - 1) + "," + keyY, 0)
              && equalTile(map, (keyX + 1) + "," + keyY, 0)) {
            removeTile = true;
          } else if (equalTile(map, (keyX) + "," + (keyY + 1), 0)
              && equalTile(map, (keyX) + "," + (keyY - 1), 0)) {
            removeTile = true;
          }
          if (removeTile) {
            removedTile = true;
            pairs.setValue(0);
          }
        }
      }
    }

    return new RandomDungeon(map, min_x, max_x, min_y, max_y);
  }

  public void save(String dungeonType, int startX, int startY, int brushZ)
  {
    Tile[] row = new Tile[map.size()];
    int x = 0;
    for (Entry<String, Integer> pairs : map.entrySet()) {
      String tileCoord[] = pairs.getKey().split(",");
      int keyX = Integer.parseInt(tileCoord[0]);
      int keyY = Integer.parseInt(tileCoord[1]);

      int tileX = startX + keyX;
      int tileY = startY + keyY;
      int tileZ = brushZ;

      int tileNr = pairs.getValue();

      // If wall, decide wall type
      if (tileNr == 1) {

        String caveType = "";
        String tileName = "1";

        if (equalTile(map, (keyX - 1) + "," + (keyY), 0)
            && equalTile(map, (keyX) + "," + (keyY - 1), 0)
            && equalTile(map, (keyX - 1) + "," + (keyY - 1), 0)) {
          tileName = caveType + "IUL";
        } else if (equalTile(map, (keyX + 1) + "," + (keyY), 0)
            && equalTile(map, (keyX) + "," + (keyY - 1), 0)
            && equalTile(map, (keyX + 1) + "," + (keyY - 1), 0)) {
          tileName = caveType + "IUR";
        } else if (equalTile(map, (keyX - 1) + "," + (keyY), 0)
            && equalTile(map, (keyX) + "," + (keyY + 1), 0)
            && equalTile(map, (keyX - 1) + "," + (keyY + 1), 0)) {
          tileName = caveType + "IDL";
        } else if (equalTile(map, (keyX + 1) + "," + (keyY), 0)
            && equalTile(map, (keyX) + "," + (keyY + 1), 0)
            && equalTile(map, (keyX + 1) + "," + (keyY + 1), 0)) {
          tileName = caveType + "IDR";
        } else if (equalTile(map, (keyX - 1) + "," + (keyY - 1), 0)
            && equalTile(map, (keyX - 1) + "," + (keyY), 1)
            && equalTile(map, (keyX) + "," + (keyY - 1), 1)) {
          tileName = caveType + "DR";
        } else if (equalTile(map, (keyX + 1) + "," + (keyY - 1), 0)
            && equalTile(map, (keyX + 1) + "," + (keyY), 1)
            && equalTile(map, (keyX) + "," + (keyY - 1), 1)) {
          tileName = caveType + "DL";
        } else if (equalTile(map, (keyX + 1) + "," + (keyY + 1), 0)
            && equalTile(map, (keyX + 1) + "," + (keyY), 1)
            && equalTile(map, (keyX) + "," + (keyY + 1), 1)) {
          tileName = caveType + "UL";
        } else if (equalTile(map, (keyX - 1) + "," + (keyY + 1), 0)
            && equalTile(map, (keyX - 1) + "," + (keyY), 1)
            && equalTile(map, (keyX) + "," + (keyY + 1), 1)) {
          tileName = caveType + "UR";
        } else if (equalTile(map, (keyX - 1) + "," + (keyY), 0)) {
          tileName = caveType + "R";
        } else if (equalTile(map, (keyX + 1) + "," + (keyY), 0)) {
          tileName = caveType + "L";
        } else if (equalTile(map, (keyX) + "," + (keyY - 1), 0)) {
          tileName = caveType + "D";
        } else if (equalTile(map, (keyX) + "," + (keyY + 1), 0)) {
          tileName = caveType + "U";
        }

        if (!tileName.equals("1")) {
          Tile t = new Tile();
          t.tileType = dungeonType;
          t.tileName = tileName;
          t.objType = null;
          t.x = tileX;
          t.y = tileY;
          t.passable = 0;
          row[x++] = t;
        }
      } else if (tileNr == 0) {
        int floorNr = RandomUtils.getInt(1, 10);
        if (floorNr == 0) {
          floorNr = 1;
        } else if (floorNr == 3) {
          if (dungeonType.equals("wooddungeon")) {
            floorNr = 1;
          }
        } else if (floorNr > 2) {
          floorNr = 1;
        }

        Tile t = new Tile();
        t.tileType = dungeonType;
        t.tileName = String.valueOf(floorNr);
        t.objType = null;
        t.x = tileX;
        t.y = tileY;
        t.passable = 1;
        row[x++] = t;

        // Chance of spawning barrel
        // Need to be close to wall
        boolean nearWall = false;
        if (equalTile(map, (keyX - 1) + "," + (keyY), 1)
            && equalTile(map, (keyX) + "," + (keyY - 1), 1)) {
          nearWall = true;
        } else if (equalTile(map, (keyX + 1) + "," + (keyY), 1)
            && equalTile(map, (keyX) + "," + (keyY - 1), 1)) {
          nearWall = true;
        } else if (equalTile(map, (keyX - 1) + "," + (keyY), 1)
            && equalTile(map, (keyX) + "," + (keyY + 1), 1)) {
          nearWall = true;
        } else if (equalTile(map, (keyX + 1) + "," + (keyY), 1)
            && equalTile(map, (keyX) + "," + (keyY + 1), 1)) {
          nearWall = true;
        }

        if (nearWall) {
          int chanceOfBarrel = RandomUtils.getInt(0, 10);
          if (chanceOfBarrel == 0) {
            t.objType = "container/barrel";
            t.passable = 0;
          }
        }
      }
    }

    try {
      BP_EDITOR.mapDB.updateDB("BEGIN TRANSACTION");
      MapImporter.insertRow(row, brushZ);
      BP_EDITOR.mapDB.updateDB("END TRANSACTION");
    }
    catch (SQLException ex) {
      BP_EDITOR.mapDB.updateDB("ROLLBACK TRANSACTION");
      ex.printStackTrace();
    }
  }

  private static boolean equalTile(Map<String, Integer> map, String key, int checkValue) {
    if (map.containsKey(key)) {
      if (map.get(key) == checkValue) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "RandomDungeon{(" + offset_x + "," + offset_y + ") "
        + width + "x" + height + "}";
  }
}
