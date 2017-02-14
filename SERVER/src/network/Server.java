package network;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import java.lang.management.ManagementFactory;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import components.md5;
import map.WorldMap;
import utils.CrashLogger;
import utils.FileCopy;
import utils.GameInfo;
import utils.LanguageSupport;
import utils.ServerGameInfo;
import utils.PlayerFix;
import utils.ServerMessage;
import utils.TimeUtils;
import utils.WebHandler;
import utils.XPTables;
import data_handlers.ConnectHandler;
import data_handlers.DataHandlers;
import data_handlers.chat_handler.ChatHandler;
import data_handlers.Handler;
import game.Database;
import game.ServerSettings;

public abstract class Server {

  // Databases
  public static Database gameDB;
  public static Database mapDB;
  public static Database userDB;
  public static Database posDB;

  // World map
  public static WorldMap WORLD_MAP;

  // Timers
  public static Timer closeTimer;
  public static Timer restartTimer;

  public static md5 MD5 = new md5();

  public static boolean SERVER_RESTARTING = false;

  // Ticks per second
  private float ticksPerSecond = 20;
  protected float targetTPS;
  protected int actualTPS;

  // Nanoseconds per tick
  private float nsPerTick;

  // Milliseconds per tick
  private float msPerTick;

  // Global tick count
  protected static long tick = 0;

  // Running state
  public boolean running;

  // Connection Listener
  private ConnectionListener connectionListener;

  // Clients
  public static int clientIndex = 0;
  public static int newClientIndex = 0;
  public static ConcurrentHashMap<Integer, Client> clients;

  private Timers mb_timers;

  private static UpdateWebSiteStatus updateWebSiteStatus = null;
  public static UpdatePlayerPosition updatePlayerPosition = null;

  /**
   * Constructor
   * @param ticksPerSecond
   * @param port
   */
  public Server() {
    setTargetTPS(ticksPerSecond);
    init();
  }

  /**
   * Public Methods
   */

  // Start the server
  public synchronized void start() {
    mb_timers = new Timers(); // Timers implements TimersMBean
    mb_timers.setInitBeginTime(System.currentTimeMillis());

    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName name = new ObjectName("BlueSaga:type=Server,name=Timers");
      mbs.registerMBean(mb_timers, name);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    clients = new ConcurrentHashMap<Integer, Client>();

    if (ServerSettings.DEV_MODE) {
      ServerMessage.println(false, "DEV_MODE");
    }

    ServerMessage.println(false, "PVP: " + ServerSettings.PVP);

    ServerMessage.println(false, "Starting server v" + ServerSettings.CLIENT_VERSION);

    // Initialize all databases
    try {
      gameDB = new Database(ServerSettings.PATH + "game");
      mapDB  = new Database(ServerSettings.PATH + "map");
      userDB = new Database(ServerSettings.PATH + "users");
      posDB  = new Database(ServerSettings.PATH + "pos");
      updatePlayerPosition = new UpdatePlayerPosition(posDB);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    // Generate language json-file
    LanguageSupport.loadLanguageFile("");

    XPTables.init();

    ServerGameInfo.load();
    GameInfo.load();

    PlayerFix.dbFix(false);

    // Prepare datahandlers
    DataHandlers.init();

    // Load world map
    WORLD_MAP = new WorldMap();
    WORLD_MAP.loadMap();

    ServerMessage.println(false, "Starting server processes...");

    restartTimer = new Timer();

    restartTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            ServerMessage.println(false, "SERVER RESTART: -15 min");
            for (Map.Entry<Integer, Client> entry : clients.entrySet()) {
              Client s = entry.getValue();
              if (s.Ready) {
                Handler.addOutGoingMessage(s, "message", "#messages.server.restart_fifteen");
              }
            }
            sendRestartWarning();
          }
        },
        ServerSettings.restartTime);

    // Start server connection

    ServerMessage.println(false, "Initializing connection...");

    connectionListener = new ConnectionListener(this, ServerSettings.PORT);
    Thread th = new Thread(connectionListener);
    th.setName("ConnectionListener");
    th.start();

    ServerMessage.println(false, "Server is ready and waiting for clients!");

    updatePlayerPosition.start();

    running = true;
    if (!ServerSettings.DEV_MODE) {
      updateWebSiteStatus = new UpdateWebSiteStatus(this);
      updateWebSiteStatus.start();
    }

    // Begin the loop
    serverLoop();
  }

  /**
   * Implementation-based Methods
   */
  protected abstract void init();

  protected abstract void update(int delta);

  /**
   * Accessory Methods
   */
  public void setTargetTPS(float targetTPS) {
    this.targetTPS = targetTPS;
    nsPerTick = TimeUtils.NS_PER_SEC / targetTPS;
    msPerTick = nsPerTick / TimeUtils.NS_PER_MS;
  }

  public long getTick() {
    return tick;
  }

  private void serverLoop() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          Server.close();
          ServerMessage.println(false, "SERVER RESTART: now");
        }
    });

    // Initialize last tick/second time
    long lastTickTime = TimeUtils.nanos();
    long lastSecondTime = TimeUtils.nanos();

    // Initialize the live tick counter for TPS monitoring
    int ticksCount = 0;

    long t_incoming = 0L;
    long t_update = 0L;
    long t_outgoing = 0L;
    long t_cleanup = 0L;

    mb_timers.setRunningBeginTime(System.currentTimeMillis());
    while (running) {

      // Current time
      long nowTime = TimeUtils.nanos();

      // The delta time in ms since last tick
      int delta = (int) ((nowTime - lastTickTime) / TimeUtils.NS_PER_MS);

      // Once the proper tick interval has passed, perform an update
      if (delta >= msPerTick) {
        lastTickTime = nowTime;
        ticksCount++;
        tick++;

        if (!SERVER_RESTARTING) {
          long t0 = System.currentTimeMillis();

          // 1. Process queued requests
          DataHandlers.processIncomingData();
          long t1 = System.currentTimeMillis();
          t_incoming += t1 - t0;
          t0 = t1;

          // 2. Update the models
          DataHandlers.update(tick);
          t1 = System.currentTimeMillis();
          t_update += t1 - t0;
          t0 = t1;

          // 3. Notify clients of queued relevant changes
          DataHandlers.processOutgoingData();
          t1 = System.currentTimeMillis();
          t_outgoing += t1 - t0;
          t0 = t1;

          // 4. Remove deleted clients
          removeClients();
          t1 = System.currentTimeMillis();
          t_cleanup += t1 - t0;
        }
      }

      // Update TPS counter each second
      if ((nowTime - lastSecondTime) / TimeUtils.NS_PER_MS >= TimeUtils.MS_PER_SEC) {
        lastSecondTime = nowTime;
        actualTPS = ticksCount;
        ticksCount = 0;
        mb_timers.updateLoopTime(
            System.currentTimeMillis(), t_incoming, t_update, t_outgoing, t_cleanup);
        mb_timers.updateTicksPerSecond(actualTPS);
      }

      try {
        Thread.currentThread().sleep(10L);
      } catch (InterruptedException ex) {
        return;
      }
    }
  }

  public void removeClients() {
    boolean removedClient = false;
    Iterator<Map.Entry<Integer, Client>> iterator = clients.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Integer, Client> client = iterator.next();

      if (client.getValue().RemoveMe) {
        ConnectHandler.removeClient(client.getValue());
        iterator.remove();
        removedClient = true;
        ServerMessage.println(false, "Removed client: " + client);
      }
    }

    if (removedClient) {
      ServerMessage.println(false, "Players online: " + clients.size());
    }
  }

  public void sendRestartWarning() {
    restartTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            // Send server restart warning to all clients
            ServerMessage.println(false, "SERVER RESTART: -1 min");
            for (Map.Entry<Integer, Client> entry : clients.entrySet()) {
              Client s = entry.getValue();
              if (s.Ready) {
                Handler.addOutGoingMessage(s, "message", "#messages.server.restart_one");
              }
            }
            executeRestart();
          }
        },
        15 * 60 * 1000);
  }

  public void executeRestart() {
    restartTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            System.exit(0);
          }
        },
        60 * 1000);
  }

  public static void close() {
    ServerMessage.println(false, "Closing server...");

    SERVER_RESTARTING = true;

    ServerMessage.println(false, "... closing connected clients");
    for (Map.Entry<Integer, Client> entry : clients.entrySet()) {
      Client s = entry.getValue();
      ConnectHandler.removeClient(s);
    }

    ServerMessage.println(false, "... closing maintenance threads");
    if (!ServerSettings.DEV_MODE) {
      updateWebSiteStatus.exit();
    }
    updatePlayerPosition.exit();

    if (posDB != null) {
      ServerMessage.println(false, "... closing posDB");
      posDB.closeDB();
      posDB = null;
    }
    if (userDB != null) {
      ServerMessage.println(false, "... closing userDB");
      userDB.closeDB();
      userDB = null;
    }
    if (gameDB != null) {
      ServerMessage.println(false, "... closing gameDB");
      gameDB.closeDB();
      gameDB = null;
    }
    if (mapDB != null) {
      ServerMessage.println(false, "... closing mapDB");
      mapDB.closeDB();
      mapDB = null;
    }

    // BACKUP SERVER
    FileCopy.backupDB();
  }

  public void addClient(Client client) {
    clients.put(clientIndex, client);
    clientIndex++;
  }
}
