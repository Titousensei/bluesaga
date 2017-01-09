package utils;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import game.ServerSettings;

public class CrashLogger {

  public static void uncaughtException(Throwable e) {
    ServerMessage.println(false, "Server crashed!");

    Calendar cal = Calendar.getInstance();
    String filename = ServerSettings.PATH + "crashlogs/"
        + TimeUtils.FILENAME_DATETIME.get().format(cal.getTime())
        + ".txt";

    try {
      PrintStream writer = new PrintStream(filename, "UTF-8");
      e.printStackTrace(writer);
      ServerMessage.println(false, "Exception logged to file: " + filename);
    } catch (FileNotFoundException | UnsupportedEncodingException e1) {
      ServerMessage.println(false, "ERROR - Can't write exception to " + filename);
      e.printStackTrace();
    }
  }
}
