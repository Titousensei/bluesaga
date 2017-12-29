package game;

public class ServerSettings {

  public static String PATH = "./";

  public static String RESTART_COMMAND = "java -jar BPserver.jar";

  public static String CHATLOG_PATH = null;

  /**
   * Register a new server at http://www.bluesaga.org/newserver
   *
   * Copy the server id given on the site and paste it instead of the default value next to SERVER_ID
   */
  public static int SERVER_ID = 1;
  // IP1:userId1;IP2:userId2:...
  public static String auto_login = null;

  /**
   * If you made updates to the client, change the client version number,
   * be sure to set the same number in your server settings on the website
   * http://www.bluesaga.org/myservers
   */
  public static String CLIENT_VERSION = "807";

  /**
   * If you want to test the server locally while developing, use DEV_MODE = true
   * Set DEV_MODE = false when running it live.
   */
  public static boolean DEV_MODE = false;

  public static boolean TRACE_MODE = false;

  public static String geoip_cmd = null;

  // Network settings
  public static int PORT = 26342;

  // Game settings
  public static boolean PVP = true;
  public static int PK_WEAK_LEVEL = 5;

  public static int LEVEL_CAP = 50;
  public static int CLASS_LEVEL_CAP = 10;
  public static int JOB_LEVEL_CAP = 10;

  public static int NPC_SPECIAL_MIN_LEVEL = 6;

  public static boolean RANDOM_ARCHIPELAGO = true;
  public static boolean RANDOM_DUNGEON = true;

  public static int startX = 5005;
  public static int startY = 9984;
  public static int startZ = 2;

  public static int initialQuestId = 0;

  public static int initialItemIdWarrior = 0;
  public static int initialItemIdHunter = 0;
  public static int initialItemIdMage = 0;

  public static int initialAbilityIdWarrior = 0;
  public static int initialAbilityIdHunter = 0;
  public static int initialAbilityIdMage = 0;

  public static boolean startWithTutorial = true;
  public static boolean enableCutScenes = true;

  // Server restart time in milliseconds
  public static int restartTime = (4 * 60 * 60) * 1000; // 4 hours (+15 min for warning)

  // CLIENT SCREEN SIZE
  public static int TILE_HALF_W = 18;
  public static int TILE_HALF_H = 10;
  public static int TILE_SIZE = 50;
}
