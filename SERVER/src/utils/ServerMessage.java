package utils;

import game.ServerSettings;

public class ServerMessage {

  public static void println(boolean debugOnly, Object... message) {
    if (!debugOnly || ServerSettings.DEV_MODE) {
      System.out.print(TimeUtils.now());
      System.out.print(" - ");
      if (debugOnly) {
        System.out.print("DEBUG ");
      }
      if (message!=null) {
        for (int i = 0 ; i< message.length ; i++) {
          System.out.print(message[i]);
        }
        System.out.println();
      }
      else {
        System.out.println("NULL");
        Thread.dumpStack();
      }
    }
  }
}
