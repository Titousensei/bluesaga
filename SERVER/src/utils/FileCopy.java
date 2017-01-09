package utils;

import game.ServerSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileCopy {

  private static void copyFile(File src, File dest) throws IOException {

    //if file, then copy it
    //Use bytes stream to support all file types
    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dest);

    byte[] buffer = new byte[1024];

    int length;
    //copy the file content in bytes
    while ((length = in.read(buffer)) > 0) {
      out.write(buffer, 0, length);
    }

    in.close();
    out.close();
  }

  public static void backupDB() {
    String dbPath = ServerSettings.PATH + "usersDB.db";
    String bakPath = ServerSettings.PATH + "db_backups/usersDB_"
        + TimeUtils.getDate("yyyyMMdd_HHmmss") + ".db";

    ServerMessage.println(false, "DB to backup: ", dbPath);
    ServerMessage.println(false, "Backup path: ", bakPath);

    if (!ServerSettings.DEV_MODE) {
      File originalDB = new File(dbPath);
      File copyDB = new File(bakPath);

      ServerMessage.println(false, "Making backup of usersDB.db...");

      try {
        Files.copy(originalDB.toPath(), copyDB.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ServerMessage.println(false, "Done saving backup: ", dbPath);
      } catch (IOException e) {
        ServerMessage.println(false, "Failed to backup db!");
        e.printStackTrace();
      }
    }
  }
}
