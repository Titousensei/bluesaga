package map;

import java.awt.Point;
import java.util.*;

public class GeneratorBrush {

  private static Map<Point, Integer> caveBrush = null;
  private static Map<Point, Integer> heightBrush = null;
  private static Map<Point, Integer> islandBrush = null;
  private static Map<Point, Integer> waterBrush = null;
  private static Map<Point, Integer> shallowBrush = null;

  public static Map<Point, Integer> getCaveBrush() {
    if (caveBrush == null) {
      // 1 = deep water
      // 2 = shallow water
      // 3 = beach
      int[][] brush =
          new int[][] {
            {0, 2, 2, 2, 0},
            {2, 2, 1, 2, 2},
            {2, 1, 1, 1, 2},
            {2, 2, 1, 2, 2},
            {0, 2, 2, 2, 0}
          };

      caveBrush = new HashMap<>();

      int brushWidth = brush.length;
      int brushHeight = brush[0].length;

      for (int i = 0; i < brushWidth; i++) {
        for (int j = 0; j < brushHeight; j++) {
          if (brush[i][j] != 0) {
            caveBrush.put(new Point(i, j), Integer.valueOf(brush[i][j]));
          }
        }
      }
    }

    return caveBrush;
  }

  public static Map<Point, Integer> getHeightBrush() {
    if (heightBrush == null) {
      // 4 = grass
      // 5 = grass cliff
      // 6 = grass on top, to be replaced with 4 grass
      int[][] brush =
          new int[][] {
            {0, 4, 4, 4, 4, 4, 0},
            {4, 4, 5, 5, 5, 4, 4},
            {4, 5, 5, 6, 5, 5, 4},
            {4, 5, 6, 6, 6, 5, 4},
            {4, 5, 5, 6, 5, 5, 4},
            {4, 4, 5, 5, 5, 4, 4},
            {0, 4, 4, 4, 4, 4, 0},
          };

      heightBrush = new HashMap<>();

      int brushWidth = brush.length;
      int brushHeight = brush[0].length;

      for (int i = 0; i < brushWidth; i++) {
        for (int j = 0; j < brushHeight; j++) {
          heightBrush.put(new Point(i, j), Integer.valueOf(brush[i][j]));
        }
      }
    }

    return heightBrush;
  }

  public static Map<Point, Integer> getIslandBrush() {
    if (islandBrush==null) {
      // 1 = deep water
      // 2 = shallow water
      // 3 = beach
      // 4 = grass
      int[][] brush =
          new int[][] {
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1},
            {1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 4, 4, 3, 3, 2, 2, 2, 2, 1, 1, 1},
            {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 4, 4, 4, 3, 3, 2, 2, 2, 1, 1, 1},
            {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 4, 4, 4, 3, 3, 2, 2, 2, 1, 1, 1},
            {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 4, 4, 4, 3, 3, 2, 2, 2, 1, 1, 1},
            {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 4, 4, 4, 3, 3, 2, 2, 2, 1, 1, 1},
            {1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 4, 4, 3, 3, 2, 2, 2, 2, 1, 1, 1},
            {1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0}
          };

      islandBrush = new HashMap<>();

      int brushWidth = brush.length;
      int brushHeight = brush[0].length;

      for (int i = 0; i < brushWidth; i++) {
        for (int j = 0; j < brushHeight; j++) {
          if (brush[i][j] != 0) {
            islandBrush.put(new Point(i, j), Integer.valueOf(brush[i][j]));
          }
        }
      }
    }

    return islandBrush;
  }

  public static Map<Point, Integer> getWaterBrush() {
    if (waterBrush==null) {
      // 1 = deep water
      // 2 = shallow water
      // 3 = grass
      int[][] brush =
          new int[][] {
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0}
          };

      waterBrush = new HashMap<Point, Integer>();

      int brushWidth = brush.length;
      int brushHeight = brush[0].length;

      for (int i = 0; i < brushWidth; i++) {
        for (int j = 0; j < brushHeight; j++) {
          if (brush[i][j] != 0) {
            waterBrush.put(new Point(i, j), Integer.valueOf(brush[i][j]));
          }
        }
      }
    }

    return waterBrush;
  }

  public static Map<Point, Integer> getShallowBrush() {
    if (shallowBrush==null) {
      // 1 = deep water
      // 2 = shallow water
      // 3 = grass
      int[][] brush =
          new int[][] {
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 2, 2, 2, 0, 0},
            {0, 2, 2, 2, 2, 2, 0},
            {0, 2, 2, 2, 2, 2, 0},
            {0, 2, 2, 2, 2, 2, 0},
            {0, 0, 2, 2, 2, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
          };

      shallowBrush = new HashMap<Point, Integer>();

      int brushWidth = brush.length;
      int brushHeight = brush[0].length;

      for (int i = 0; i < brushWidth; i++) {
        for (int j = 0; j < brushHeight; j++) {
          if (brush[i][j] != 0) {
            shallowBrush.put(new Point(i, j), Integer.valueOf(brush[i][j]));
          }
        }
      }
    }

    return shallowBrush;
  }
}
