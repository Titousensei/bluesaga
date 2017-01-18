package data_handlers;

import game.ServerSettings;
import login.WebsiteServerStatus;
import network.Client;
import network.Server;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import data_handlers.ability_handler.AbilityHandler;
import data_handlers.ability_handler.StatusEffectHandler;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.battle_handler.PvpHandler;
import data_handlers.card_handler.CardHandler;
import data_handlers.chat_handler.ChatHandler;
import data_handlers.crafting_handler.CraftingHandler;
import data_handlers.item_handler.ContainerHandler;
import data_handlers.item_handler.ActionbarHandler;
import data_handlers.item_handler.EquipHandler;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.ItemHandler;
import data_handlers.monster_handler.MonsterHandler;
import data_handlers.party_handler.PartyHandler;
import utils.CrashLogger;
import utils.ServerMessage;

public class DataHandlers {
  private static Map<String, Consumer<Message>> dispatch = new HashMap<>();

  private static ConcurrentLinkedQueue<Message> incomingMessages;
  private static ConcurrentLinkedQueue<Message> outgoingMessages;

  public static void init() {
    incomingMessages = new ConcurrentLinkedQueue<Message>();
    outgoingMessages = new ConcurrentLinkedQueue<Message>();

    MonsterHandler.init();
    AbilityHandler.init();
    ContainerHandler.init();
    ChatHandler.init();
    FishingHandler.init();
    TrapHandler.init();
    CraftingHandler.init();
    PartyHandler.init();
    //CardHandler.init();

    ConnectHandler.init();
    LoginHandler.init();
    WalkHandler.init();
    SkillHandler.init();
    ItemHandler.init();
    MapHandler.init();
    BattleHandler.init();
    BountyHandler.init();
    FriendsHandler.init();
    QuestHandler.init();
    ShopHandler.init();
    MusicHandler.init();
    GatheringHandler.init();
    SkinHandler.init();
    TutorialHandler.init();

    InventoryHandler.init();
    EquipHandler.init();
    ActionbarHandler.init();
  }

  public static void register(String type, Consumer<Message> handle) {
    dispatch.put(type, handle);
  }

  public static void update(long tick) {


    MonsterHandler.update(tick);  //= 516
    BattleHandler.updateRangedCooldowns(); //= 16

    AbilityHandler.updateProjectiles();   //= 15
    AbilityHandler.updateAbilityEvents(); //= 1236
    AbilityHandler.updatePlayerCasting(); //= 14

    // Every 200 ms
    if (tick % 4 == 0) {
      BattleHandler.update();  //= 2854
      AbilityHandler.updateCooldowns();  //= 1597
    }

    // Every 1,000 ms
    if (tick % 20 == 0) {
      TrapHandler.update();  //= 83
      StatusEffectHandler.updateStatusEffects();  //= 96
      StatusEffectHandler.updateTileStatusEffects();  //= 8160
      PvpHandler.updatePKTimers();  //= 5
      MapHandler.updateNightTime();  //= 1
    }

    // Every 10,000 ms
    if (tick % 200 == 0) {
      ContainerHandler.checkContainerRespawn();  //= 3
    }

    // Every minute
    if (tick % 1200 == 0) {
      if (!ServerSettings.DEV_MODE) {
        int nrPlayers = Server.clients.size();
        try {
          WebsiteServerStatus.UpdateServerStatus(ServerSettings.SERVER_ID, nrPlayers);
        }
        catch (Exception ex) {
          ServerMessage.println(false, "WARNING - WebsiteServerStatus failed: ", ex.toString());
        }
      }
    }
  }

  public static void addIncomingMessage(Message message) {
    incomingMessages.add(message);
  }

  //public static void handleData(Client client, String message){

  public static void processIncomingData() {
    for (Iterator<Message> i = incomingMessages.iterator(); i.hasNext(); ) {
      Message m = i.next();
      Consumer<Message> handle = dispatch.get(m.type);
      if (handle != null) {
        try {
          handle.accept(m);
        } catch (Exception ex) {
          ServerMessage.println(false, "ERROR - Message Exception: " + m);
          ex.printStackTrace();
        }
      } else {
        ServerMessage.println(false, "WARNING - Unknown message type: " + m.type);
      }
      i.remove();
    }
  }

  public static void addOutgoingMessage(Message message) {
    outgoingMessages.add(message);
  }

  public static void processOutgoingData() {
    for (Iterator<Message> i = outgoingMessages.iterator(); i.hasNext(); ) {
      Message m = i.next();

      sendMessage(m);

      i.remove();
    }
  }

  private static boolean sendMessage(Message message) {
    boolean sendOk = true;
    Client client = message.client;

    try {
      try {
        String dataToSend = "<" + message.type + ">" + message.message;

        if (ServerSettings.TRACE_MODE) {
          if (dataToSend.length() < 100) {
            System.out.println("<-| " + dataToSend);
          } else {
            System.out.println(
                "<-| " + dataToSend.substring(0, 100) + "...(" + dataToSend.length() + ")");
          }
        }

        byte[] byteMsg = (dataToSend).getBytes();
        if (client.out != null) {
          client.out.writeObject(byteMsg);
          client.out.reset();
          client.out.flush();
        }

      } catch (SocketException e) {
        sendOk = false;
      }
    } catch (IOException ioException) {
      CrashLogger.uncaughtException(ioException);
      sendOk = false;
    }
    return sendOk;
  }
}
