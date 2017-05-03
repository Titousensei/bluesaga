package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import utils.Obfuscator;
import utils.ServerMessage;
import utils.TimeUtils;
import creature.PlayerCharacter;
import data_handlers.ConnectHandler;
import data_handlers.DataHandlers;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.battle_handler.PvpHandler;
import data_handlers.party_handler.PartyHandler;

public class Client implements Runnable {

  private Socket csocket;

  private int index;

  public String IP;

  public boolean sendingData = false;
  public ObjectOutputStream out;
  private ObjectInputStream in;

  public int inputPacketId = 10000;

  private String message;
  private Timer logoutTimer = new Timer();

  public int UserId;
  public String UserMail;
  public int loginSessionId = 0;

  public int chestSize;

  public PlayerCharacter playerCharacter;
  public PlayerCharacter lastCharacter;

  public String Muted = "No";

  public boolean ConfirmAccount = false;

  public boolean FirstTime = false;

  public int lostConnectionNr;

  public boolean Ready = false;
  public boolean RemoveMe = false;
  public boolean HasQuit = false;

  public boolean OpenContainer = false;

  public Client(Socket csocket) {
    this.csocket = csocket;
    this.UserId = 0;
    this.playerCharacter = null;
    this.lostConnectionNr = 0;
  }

  @Override
  public void run() {
    try {
      this.out = new ObjectOutputStream(this.csocket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.csocket.getInputStream());
      do {
        try {
          if (this.lostConnectionNr > 0) {
            ServerMessage.println(false,
                "Client ", this.UserMail, " lost connection: ", this.lostConnectionNr);
          }
          this.lostConnectionNr = 0;

          message = new String((byte[]) in.readObject());

          if (!Server.SERVER_RESTARTING) {
            int messageIndex = message.indexOf("<");

            String messageIdobf = message.substring(0, messageIndex);

            int messageId = Obfuscator.illuminate(messageIdobf);

            String messageInfo[] = message.substring(messageIndex).split(">", 2);
            String messageType = messageInfo[0].substring(1);
            String messageText = messageInfo[1];

            if (game.ServerSettings.TRACE_MODE) {
              if (message.length() < 100) {
                System.out.println("|-> " + messageId + message.substring(messageIndex));
              } else {
                System.out.println(
                    "|-> "
                    + messageId
                    + message.substring(messageIndex, 100)
                    + "...("
                    + message.length()
                    + ")");
              }
            }

            if (inputPacketId == messageId) {
              Message newMessage = new Message(this, messageType, messageText);

              DataHandlers.addIncomingMessage(newMessage);

              inputPacketId++;
              if (inputPacketId > 65000) {
                inputPacketId = 10000;
              }
            } else {
              ServerMessage.println(false, "ALERT - Wrong packet id: ", inputPacketId);
              // Disconnect client
              ConnectHandler.removeClient(this);
            }
          }
        } catch (NumberFormatException e) {
          if (playerCharacter != null) {
            ServerMessage.println(false,
                "Wrong packet sent by: ", playerCharacter);
          } else {
            ServerMessage.println(false,
                "Wrong packet sent by IP: ", this.csocket);
          }
        } catch (SocketTimeoutException te) {
          ServerMessage.println(false, "Client timed out... removed it");
          RemoveMe = true;
        } catch (ClassNotFoundException classnot) {
          ServerMessage.println(false, "ALERT - Data received in unknown format");
          Handler.addOutGoingMessage(this, "error", "fail");
        }
      } while (message != null && !message.contains("<connection>disconnect"));

      // KILLING THREAD, REMOVING PLAYER
      this.in.close();
      this.out.close();

      ServerMessage.println(false, "Client exited: ", this);

      ConnectHandler.removeClient(this);
    } catch (IOException e) {
      this.lostConnectionNr++;

      // NOT SURE ABOUT THIS ONE, JUST TRYING!!!
      this.Ready = false;
      // CAN CAUSE ERRORS!!!

      //e.printStackTrace();

      // RESPAWN AT CHECKPOINT IF DEAD
      if (this.playerCharacter != null) {
        if (this.playerCharacter.isDead()) {
          this.playerCharacter.revive();
          BattleHandler.respawnPlayer(this.playerCharacter);
        }
      }

      PartyHandler.leaveParty(this);

      if (!this.RemoveMe) {
        if (this.lostConnectionNr > 20) {
          this.Ready = false;
          this.startLogoutTimer();
        } else {
          run();
        }
      }
    }
  }

  public void startLogoutTimer() {
    ServerMessage.println(false, "Client closed improperly: started logout timer...");

    // Default logout time 20 sec
    int logoutTime = BattleHandler.playerHitTime;

    // Playerkiller logout time 2 hours
    if (playerCharacter != null) {
      if (playerCharacter.getPkMarker() > 0) {
        ServerMessage.println(false, playerCharacter,
            " is PK: longer logout time");

        if (playerCharacter.getPkMarker() == 1) {
          logoutTime = BattleHandler.playerHitTime;
        } else if (playerCharacter.getPkMarker() == 2) {
          logoutTime = PvpHandler.playerKillerTime;
        }
      }
    }

    logoutTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            if (playerCharacter != null) {
              if (playerCharacter.getPkMarker() > 0) {
                Server.userDB.updateDB(
                    "update user_character set PlayerKiller = 0 where Id = "
                        + playerCharacter.getDBId());
              }
            }
            quit();
          }
        },
        logoutTime * 1000);
  }

  public void quit() {
    ConnectHandler.removeClient(this);
  }

  public void closeSocket() {
    try {
      this.csocket.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public Socket getSocket() {
    return this.csocket;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  @Override
  public String toString() {
    if (UserMail != null) {
      return UserMail + " (" + UserId + ")";
    } else {
      return "[" + IP + "]";
    }
  }
}
