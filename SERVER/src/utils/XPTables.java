package utils;

import game.ServerSettings;

public class XPTables {
  // LEVEL AND SKILL INFO
  private static int[] nextLevelXP;
  private static int[] totalLevelXP;

  private static int[] nextLevelSP;
  private static int[] totalLevelSP;

  public static void init() {
    // LEVEL UP XP INFO
    nextLevelXP = new int[ServerSettings.LEVEL_CAP + 1];
    totalLevelXP = new int[ServerSettings.LEVEL_CAP + 1];

    int oldxp = 0;
    int totalxp = 0;

    totalLevelXP[1] = totalxp;

    for (int lvl = 1; lvl < ServerSettings.LEVEL_CAP; lvl++) {
      if (lvl == 2) {
        totalxp += 150;
        nextLevelXP[lvl + 1] = 150;
      } else {
        totalxp += (int) (50 * Math.pow(lvl, 2) - 150 * lvl + 200);
        nextLevelXP[lvl + 1] = totalxp - oldxp;
      }
      totalLevelXP[lvl + 1] = totalxp;
      oldxp = totalxp;
    }

    nextLevelSP = new int[ServerSettings.JOB_LEVEL_CAP + 1];
    totalLevelSP = new int[ServerSettings.JOB_LEVEL_CAP + 1];

    //600 fibonacci
    nextLevelSP[0] =    0;
    nextLevelSP[1] =    0;
    nextLevelSP[2] =  400;
    nextLevelSP[3] =  600;
    nextLevelSP[4] = 1000;
    nextLevelSP[5] = 1600;
    nextLevelSP[6] = 2600;
    nextLevelSP[7] = 4200;
    nextLevelSP[8] = 6800;

    int total = 0;
    for (int i = 1 ; i <= ServerSettings.JOB_LEVEL_CAP ; i++) {
      total += nextLevelSP[i];
      totalLevelSP[i] = total;
    }

    //System.out.println("*** nextLevelXP "  + java.util.Arrays.toString(nextLevelXP));
    //System.out.println("*** totalLevelXP " + java.util.Arrays.toString(totalLevelXP));
    //System.out.println("*** nextLevelSP "  + java.util.Arrays.toString(nextLevelSP));
    //System.out.println("*** totalLevelSP " + java.util.Arrays.toString(totalLevelSP));
  }

  /*** Characters XP ***/

  public static int getTotalLevelXP(int lvl) {
    if (lvl > 1 && lvl <= ServerSettings.LEVEL_CAP) {
      return totalLevelXP[lvl];
    }
    return Integer.MAX_VALUE;
  }

  public static int getNextLevelXP(int lvl) {
    if (lvl > 1 && lvl <= ServerSettings.LEVEL_CAP) {
      return nextLevelXP[lvl];
    }
    return Integer.MAX_VALUE;
  }

  public static int getLevelByXP(int XP) {
    for (int lvl = 2; lvl < 100; lvl++) {
      if (totalLevelXP[lvl] > XP) {
        return lvl - 1;
      }
    }
    return 1;
  }

  /*** Skills and Classes SP ***/

  public static int getTotalLevelSP(int lvl) {
    if (lvl > 1 && lvl <= ServerSettings.JOB_LEVEL_CAP) {
      return totalLevelSP[lvl];
    }
    return Integer.MAX_VALUE;
  }

  public static int getNextLevelSP(int lvl) {
    if (lvl > 1 && lvl <= ServerSettings.JOB_LEVEL_CAP) {
      return nextLevelSP[lvl];
    }
    return Integer.MAX_VALUE;
  }

  public static int getLevelBySP(int SP) {
    for (int lvl = 2; lvl < 100; lvl++) {
      if (totalLevelSP[lvl] > SP) {
        return lvl - 1;
      }
    }
    return 1;
  }
}
