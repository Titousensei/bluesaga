package game;

import graphics.Font;
import graphics.ImageResource;
import gui.Gui;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

import map.ScreenObject;
import map.WorldMap;
import network.Client;
import network.ClientReceiverThread;
import screens.LoginScreen;
import screens.ScreenHandler;
import screens.ScreenHandler.ScreenType;
import data_handlers.*;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.util.Log;

import abilitysystem.Ability;
import abilitysystem.StatusEffect;
import sound.BgMusic;
import sound.Sfx;
import utils.Config;
import utils.GameInfo;
import utils.LanguageUtils;
import utils.json.JSONObject;
import components.DebugOutput;
import creature.PlayerCharacter;

public class BlueSaga extends BasicGame {

  public static DebugOutput DEBUG = new DebugOutput(ClientSettings.DEV_MODE);

  // GAME STATES
  public static boolean HAS_QUIT = false;

  public static long updateTimeItr = 0;
  public static int logoutTime = 20;
  public static int playerKillerLogoutTime = 60 * 5;
  public static int logoutTimeItr = 0;

  public static Database gameDB;

  // CONTROL
  public static Input INPUT;

  // SCREEN DATA
  public static WorldMap WORLD_MAP;

  // PLAYER
  public static PlayerCharacter playerCharacter;
  public static int lastPlayedDbId = 0;

  // GUI
  public static Gui GUI;
  public static boolean actionServerWait = false;
  public static boolean LoginSuccess = false;

  // NETWORK
  public static Client client;
  private String serverData;
  public static ClientReceiverThread reciever;
  public static boolean ServerCheck = false;

  // MUSIC & SFX
  public static BgMusic BG_MUSIC;

  public static AppGameContainer app;
  private static Timer closeTimer;

  public BlueSaga() {
    super("Blue Saga v0." + ClientSettings.VERSION_NR);
  }

  @Override
  public void init(GameContainer container) throws SlickException {

    INPUT = container.getInput();

    // Load translation
    File jarFile =
        new File(BlueSaga.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    String translation_path = jarFile.getAbsolutePath() + "/../assets/languages/game_text.txt";
    JSONObject translationJSON = ClientSettings.loadTranslationLanguageFile(translation_path);
    if (translationJSON != null) {
      LanguageUtils.loadJson(translationJSON, false);
    }

    LanguageUtils._debug = ClientSettings.DEV_MODE;

    // Load original english text
    JSONObject originalJSON = ClientSettings.loadOriginalLanguageFile();
    LanguageUtils.loadJson(originalJSON, true);

    loading(container);
  }

  public void loading(GameContainer container) throws SlickException {
    // Connect to DB
    try {
      gameDB = new Database();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    Font.load();

    GameInfo.load();

    ImageResource.load();

    DataHandlers.init();

    ScreenHandler.init(container);

    // LOAD SOUNDS
    BG_MUSIC = new BgMusic();
    Sfx.load(gameDB);

    // NETWORK INIT
    client = new Client();

    // LOAD OPTIONS
    gameDB.loadOptions();

    ServerCheck = false;

    // GUI INIT
    GUI = new Gui();
    GUI.init(container);

    WORLD_MAP = new WorldMap();

    container.setMouseCursor("images/gui/cursors/cursor_hidden.png", 5, 5);

    if (ClientSettings.DEV_MODE) {
      ClientSettings.MUSIC_ON = false;
      ClientSettings.SFX_ON = false;
    }

    BG_MUSIC.changeSong("title", "title");
    if (ClientSettings.auto_login && ClientSettings.DEV_MODE) {
      System.out.println("Attempting auto login " + ClientSettings.SERVER_IP + ":" + ClientSettings.PORT);
      chooseServer(null);
    }
    ScreenHandler.setActiveScreen(ScreenType.LOGIN);
  }

  public static int getFPS() {
    return app.getFPS();
  }

  public static void stopClient() {
    HAS_QUIT = true;
    reciever.interrupt();
    ServerCheck = false;
  }

  private static String shell(String... command) {
    try {
      Process p = Runtime.getRuntime().exec(command);
      p.waitFor();

      BufferedReader reader =
           new BufferedReader(new InputStreamReader(p.getInputStream()));

      StringBuilder sb = new StringBuilder(4000);
      String line = "";
      while ((line = reader.readLine())!= null) {
        sb.append(line).append('\n');
      }

      return sb.toString();
    }
    catch (Throwable ex) {
      return ex.toString();
    }
  }

  private static String getSystemInfo() {

    String osName = System.getProperty("os.name").toLowerCase();

    String cpuInfo = "(no info)";
    String gpuInfo = "(no info)";
    String osInfo  = "(no info)";

    if (osName.contains("win")) {
      cpuInfo = shell("cmd /c wmic cpu get name");
      gpuInfo = shell("cmd /c wmic path win32_VideoController get name driverVersion");
      osInfo  = shell("cmd /c ver");
    } else if (osName.contains("nix") || osName.contains("nux")) {
      cpuInfo = shell("sh", "-c", "cat /proc/cpuinfo | grep 'model name' | uniq -c");
      gpuInfo = shell("sh", "-c", "lspci | grep VGA");
      osInfo  = shell("sh", "-c", "uname -a");
    } else if (osName.contains("mac")) {
      cpuInfo = shell("sh", "-c", "sysctl machdep.cpu.brand_string");
      gpuInfo = shell("sh", "-c", "system_profiler SPDisplaysDataType | tr -s '\n ' ', '");
      osInfo  = shell("sh", "-c", "uname -a");
    }

    Runtime rt = Runtime.getRuntime();

    return "os.name=" + osName
        + "; os.version=" + System.getProperty("os.version")
        + "; os.arch=" + System.getProperty("os.arch")
        + "; cpu.num=" + rt.availableProcessors()
        + "; mem.free=" + rt.freeMemory()
        + "; java.version=" + System.getProperty("java.version")
        + "; java.vendor=" + System.getProperty("java.vendor")
        + "; java.runtime.name=" + System.getProperty("java.runtime.name")
        + "; java.runtime.version=" + System.getProperty("java.runtime.version")
        + "; java.class.version=" + System.getProperty("java.class.version")
        + "; java.vm.version=" + System.getProperty("java.vm.version")
        + "; OS: " + osInfo.trim()
        + "; CPU: " + cpuInfo.trim()
        + "; GPU: " + gpuInfo.trim();
  }

  public static void chooseServer(String ServerName) {
    LoginScreen.clickedLogin = true;

    if (client.init().equals("error")) {
      ScreenHandler.setActiveScreen(ScreenType.ERROR);
      ScreenHandler.setLoadingStatus("Can't connect to server!");
      LoginScreen.clickedLogin = false;
    } else {
      BlueSaga.HAS_QUIT = false;

      reciever = new ClientReceiverThread(client.in_answer);
      reciever.start();

      ServerCheck = true;
      client.resetPacketId();
      client.sendMessage("connection", "hello;" + getSystemInfo());
    }
  }

  public static void reconnect() {

    if (client.init().equals("error")) {
      reciever.lostConnection = true;
      ScreenHandler.LoadingStatus = "Failed to reconnect with server!";
      reciever.startReconnectCountdown();
    } else {
      reciever = new ClientReceiverThread(client.in_answer);
      reciever.start();

      ServerCheck = true;

      client.sendMessage("connection", "hello-again;" + getSystemInfo());
    }
  }

  public void serverCheck() {
    if (ServerCheck) {
      serverData = reciever.getInfo();

      if (!"".equals(serverData)) {
        DataHandlers.handleData(serverData);
      }
    }
  }

  @Override
  public void update(GameContainer container, int delta) throws SlickException {

    float elapsedTime = (delta) / 1000.0f;

    updateTimeItr++;

    if (updateTimeItr % 60 == 0) {
      // Every 1000 ms

      if (client.connected) {
        // Send keep alive packet
        client.sendKeepAlive();
      }
      // UPDATE LOGOUT TIMER
      if (logoutTimeItr > 0) {
        logoutTimeItr--;
        if (logoutTimeItr == 0) {
          Gui.getActionBar().updateSoulstone();
        }
      }

      // UPDATE STATUSEFFECTS
      if (playerCharacter != null) {
        for (Iterator<StatusEffect> iter = playerCharacter.getStatusEffects().iterator();
            iter.hasNext();
            ) {
          StatusEffect s = iter.next();
          if (!s.increaseDurationItr(1)) {
            iter.remove();
            playerCharacter.updateBonusStats();
          }
        }
      }
    }

    if (updateTimeItr % 12 == 0) {
      // Every 200 ms

      if (playerCharacter != null) {
        // UPDATE ABILITY COOLDOWN

        for (Iterator<Ability> iter = playerCharacter.getAbilities().iterator(); iter.hasNext(); ) {
          Ability a = iter.next();
          if (a != null) {
            a.cooldown();
          }
        }
      }
    }

    if (updateTimeItr % 6 == 0) {
      // Every 100 ms

      AbilityHandler.updateAbilityCooldown();

      // update monster rotation
      for (ScreenObject c : ScreenHandler.SCREEN_OBJECTS_DRAW) {
        if (c != null) {
          if (c.getType().equals("Creature")) {
            c.getCreature().updateRotation();
          }
        }
      }
    }

    // CHECK IF NEW INFO FROM SERVER HAS COME
    serverCheck();

    ScreenHandler.update(elapsedTime);

    if (ScreenHandler.getActiveScreen() == ScreenType.WORLD) {
      WalkHandler.updatePlayerWalk(playerCharacter);
    }

    keyLogic(container, elapsedTime);
  }

  @Override
  public void render(GameContainer container, Graphics g) throws SlickException {

    Font.loadGlyphs();

    ScreenHandler.draw(g, container);
    GUI.draw(g, container);
  }

  public static void main(String[] args) {

    if (args.length > 0) {
      Config.configure(ClientSettings.class, args[0]);
    }

    // CRASH REPORTS
    Thread.setDefaultUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread t, Throwable e) {
            String msg = BlueSagaLogSystem.logException(e);
            if (!ClientSettings.DEV_MODE) {
              Calendar cal = Calendar.getInstance();
              SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

              String filename = "libs/crashlogs/crashlog_" + sdf.format(cal.getTime()) + ".txt";

              try {
                PrintStream writer = new PrintStream(filename, "UTF-8");
                writer.println(msg);
                writer.close();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
          }
        });
    if (!ClientSettings.DEV_MODE) {
      System.setProperty("org.lwjgl.librarypath", new File("libs/").getAbsolutePath());
    }

    Log.setLogSystem(new BlueSagaLogSystem());

    try {

      app = new AppGameContainer(new BlueSaga());

      app.setDisplayMode(
          ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT, ClientSettings.FULL_SCREEN);
      app.setTargetFrameRate(ClientSettings.FRAME_RATE);
      app.setShowFPS(ClientSettings.DEV_MODE);
      app.setAlwaysRender(true);
      app.setVSync(false);

      app.setIcons(
          new String[] {
            "images/icons/16x16.png",
            "images/icons/24x24.png",
            "images/icons/32x32.png",
            "images/icons/64x64.png",
            "images/icons/128x128.png"
          });

      app.start();
    } catch (SlickException e) {
      e.printStackTrace();
    }
  }

  /*
   *
   * 	KEYBOARD & MOUSE
   *
   */

  private void keyLogic(GameContainer GC, float aElapsedTime) {
    INPUT = GC.getInput();

    if (INPUT.isKeyPressed(Input.KEY_F1)) {
      toggleFullscreen(app);
    }

    ScreenHandler.keyLogic(INPUT);
  }

  public static void restartLogoutTimer(int logoutTime) {
    if (BlueSaga.playerCharacter.isResting()) {
      BlueSaga.playerCharacter.setResting(false);
      BlueSaga.client.sendMessage("rest", "stop");
    }

    logoutTimeItr = logoutTime;

    Gui.getActionBar().updateSoulstone();
  }

  public static void logoutTimerOk() {
    logoutTimeItr = 0;
  }

  public static void toggleFullscreen(AppGameContainer app) {
    ClientSettings.toggleFullScreen();
    try {
      app.setDisplayMode(
          ClientSettings.SCREEN_WIDTH, ClientSettings.SCREEN_HEIGHT, ClientSettings.FULL_SCREEN);
    } catch (SlickException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void close() {
    BlueSaga.client.closeConnection();
    gameDB.closeDB();

    closeTimer = new Timer();

    closeTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            System.exit(0);
          }
        },
        1000);
  }
}
