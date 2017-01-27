package network;

/************************************
 * 									*
 *			CLIENT / CLIENT			*
 *									*
 ************************************/
import game.BlueSaga;
import game.ClientSettings;

import java.io.*;
import java.net.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import utils.Obfuscator;

public class Client {

  private String USER_MAIL = "";
  private int USER_ID = 0;

  private int keepAliveSec = 0;

  // decay: 90%
  public double pingMeter = 0.0;
  public long pingTimer = 0L;
  public long pingLast = 0L;

  public boolean connected = false;

  private int outputPacketId = 10000;

  private Socket requestSocket;
  public ObjectOutputStream out;
  public ObjectInputStream in_answer;
  ObjectInputStream in_new;

  String message;

  public Client() {}

  public String init() {
    resetPacketId();
    try {
      // REGULAR SOCKET
      requestSocket = new Socket(ClientSettings.SERVER_IP, ClientSettings.PORT);

      requestSocket.setSoTimeout(0);
      out = new ObjectOutputStream(requestSocket.getOutputStream());
      out.flush();

      in_answer = new ObjectInputStream(requestSocket.getInputStream());

      connected = true;
    } catch (UnknownHostException e) {
      BlueSaga.DEBUG.print("Connection to server failed to connect to " + ClientSettings.SERVER_IP);
      return "error";
    } catch (IOException e) {
      BlueSaga.DEBUG.print("Connection to server failed to reconnect");
      return "error";
    }
    return "";
  }

  public void sendMessage(String type, String msg) {
    keepAliveSec = 0;
    try {
      String outputPacketIdObfuscated = Obfuscator.obfuscate(outputPacketId);

      byte[] byteMsg = (outputPacketIdObfuscated + "<" + type + ">" + msg).getBytes();
      out.writeObject(byteMsg);
      out.flush();

      outputPacketId++;
      if (outputPacketId > 65000) {
        outputPacketId = 10000;
      }
    } catch (IOException ioException) {
      //ioException.printStackTrace();
    }
  }

  public void setUserMail(String newMail) {
    USER_MAIL = newMail;
  }

  public String getUserMail() {
    return USER_MAIL;
  }

  public void setUserId(int newUserId) {
    USER_ID = newUserId;
  }

  public int getUserId() {
    return USER_ID;
  }

  public void closeConnection() {
    sendMessage("connection", "disconnect");

    //4: Closing connection
    try {
      in_answer.close();
      out.close();
      requestSocket.close();

    } catch (IOException ioException) {
      //	ioException.printStackTrace();
    }
  }

  public void stopPingTimer() {
    if (pingTimer != 0L) {
      pingLast = System.currentTimeMillis() - pingTimer;
      pingMeter = (pingMeter == 0.0) ? pingLast : pingMeter * 0.9 + pingLast * 0.1;
      pingTimer = 0L;
    }
  }

  private String telemetry() {
    try {
      com.sun.management.OperatingSystemMXBean osMbean =
          (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

      return String.format("jvm_cpu=%1.5f; system_cpu=%1.5f",
                           osMbean.getProcessCpuLoad(),
                           osMbean.getSystemCpuLoad());
    } catch (Throwable ex) {
      return ex.toString();
    }
  }

  public void sendKeepAlive() {
    if (!BlueSaga.HAS_QUIT) {
      keepAliveSec++;
      if (this.keepAliveSec >= 4 && !BlueSaga.reciever.lostConnection) {
        pingTimer = System.currentTimeMillis();
        sendMessage("keepalive", "ping=" + pingLast + "; ping_avg=" + pingMeter
            + "; " + telemetry() + "; FPS=" + BlueSaga.getFPS());
      }
    }
  }

  public void resetPacketId() {
    outputPacketId = 10000;
  }
}
