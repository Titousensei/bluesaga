package network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import creature.PlayerCharacter;
import game.Database;
import utils.Coords;

public class UpdatePlayerPosition
extends Thread
{
  private ConcurrentHashMap<PlayerCharacter, Coords> pos;
  private static Database posDB;
  private PreparedStatement stat;

  private boolean running = false;

  UpdatePlayerPosition(Database db) throws SQLException {
    posDB = db;

    posDB.updateDB(
        "CREATE TABLE IF NOT EXISTS character_position (id INTEGER PRIMARY KEY, x INTEGER, y INTEGER, z INTEGER)");
    stat = posDB.prepareStatement(
        "UPDATE character_position SET x = ?, y = ?, z = ? WHERE id = ?");

    pos = new ConcurrentHashMap<>();
  }

  public void savePosition(PlayerCharacter pc, int x, int y, int z) {
    pos.put(pc, new Coords(x,y,z));
  }

  public static void createCharacterPos(int charId, int x, int y, int z) {
    String posStatement =
        "INSERT INTO character_position (id, x, y, z) VALUES ("
        + charId + ',' + x + ',' + y + ',' + z + ')';
System.out.println(posStatement);
    Server.posDB.updateDB(posStatement);
  }

  public void exit() {
    try {
      running = false;
      join();
      updatePositions();
    }
    catch (SQLException ex) {
      ex.printStackTrace();
    }
    catch (InterruptedException ex) {
      return;
    }
  }

  private void updatePositions()
  throws SQLException
  {
    if (!pos.isEmpty()) {
      for (Map.Entry<PlayerCharacter, Coords> entry : pos.entrySet()) {
        PlayerCharacter pc = entry.getKey();
        Coords xyz = entry.getValue();
        stat.setInt(1, xyz.x);
        stat.setInt(2, xyz.y);
        stat.setInt(3, xyz.z);
        stat.setInt(4, pc.getDBId());
        stat.addBatch();
        pos.remove(pc, xyz);
      }
      stat.executeBatch();
      posDB.commit();
    }
  }

  @Override
  public void run() {
    running = true;

    while (running) {
      try {
        updatePositions();

        // Every 1s
        sleep(1000L);
      }
      catch (InterruptedException ex) {
        running = false;
      }
      catch (SQLException ex) {
        ex.printStackTrace();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
