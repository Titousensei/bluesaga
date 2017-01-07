package game;

/************************************
 * 									*
 *			SERVER / DATABASE		*
 *									*
 ************************************/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.ServerMessage;
import utils.TimeUtils;
import utils.WebHandler;
import data_handlers.Handler;
import login.WebsiteLogin;
import network.Client;
import network.Server;

public class Database {

  private Connection conn;

  public Database(String name) throws ClassNotFoundException {
    Class.forName("org.sqlite.JDBC");

    try {
      conn = DriverManager.getConnection("jdbc:sqlite:" + name + "DB.db");
      conn.setAutoCommit(true);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void updateDB(String sqlStatement) {
    try {
      if (conn != null) {
        Statement stat = conn.createStatement();
        stat.execute(sqlStatement);

        stat.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public ResultSet askDB(String sqlStatement) {
    try {
      if (conn != null) {
        Statement stat = conn.createStatement();
        if (stat.execute(sqlStatement)) {
          ResultSet rs = stat.getResultSet();
          return rs;
        }

        stat.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public int askInt(String sqlStatement) {
    if (conn != null) {
      try (Statement stat = conn.createStatement()) {
        if (stat.execute(sqlStatement)) {
          ResultSet rs = stat.getResultSet();
          if (rs.next()) {
            return rs.getInt(1);
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return 0;
  }

  public String askString(String sqlStatement) {
    if (conn != null) {
      try (Statement stat = conn.createStatement()) {
        if (stat.execute(sqlStatement)) {
          ResultSet rs = stat.getResultSet();
          if (rs.next()) {
            return rs.getString(1);
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return "";
  }

  /*
   *
   * 	BUG REPORT
   *
   */

  public int checkFriend(String friendName, Client client) {

    int friendId = -1;

    try {
      // CHECK IF FRIEND IS A PLAYER
      PreparedStatement stat =  //      1   2
          conn.prepareStatement("select Id, AdminLevel from user_character where lower(Name) = ?");
      stat.setString(1, friendName.toLowerCase());
      ResultSet checkResult = stat.executeQuery();

      if (checkResult.next()) {
        friendId = checkResult.getInt(1);

        if (checkResult.getInt(2) > 2) {
          friendId = -3;
        } else if (friendId != client.playerCharacter.getDBId()) {
          // CHECK IF PLAYER IS ALREADY IN FRIEND LIST
          int friendCheck =
              askInt(
                  "select Id from user_friend where UserId = "
                      + client.UserId
                      + " and FriendCharacterId = "
                      + friendId);
          if (friendCheck != 0) {
            friendId = 0;
          } else {
            updateDB(
                "insert into user_friend (UserId,FriendCharacterId,Date) values ("
                    + client.UserId
                    + ","
                    + friendId
                    + ",'"
                    + TimeUtils.now()
                    + "')");
            client.playerCharacter.getFriendsList().add(friendId);
          }
        } else {
          friendId = -2;
        }
      }

      stat.close();
    } catch (SQLException e) {
      ServerMessage.printMessage("Error adding bug report", false);
      e.printStackTrace();
    }

    // -3 = player is admin
    // -2 = player is you
    // -1 = player does not exit
    // 0 = player is already in friend list
    return friendId;
  }
/*
  private ThreadLocal<PreparedStatement> STMT_PLAYER_POS =
          conn.prepareStatement(
              "update user_character set X = ?, Y = ?, Z = ? where Id = ?");

  public void savePlayerPosition(int id, int x, int y, int z) {
    try {
      PreparedStatement stat =
          conn.prepareStatement(
              "update user_character set X = ?, Y = ?, Z = ? where Id = ?");
      stat.setInt(1, x);
      stat.setInt(2, y);
      stat.setInt(3, z);
      stat.setInt(4, id);
      stat.executeUpdate();
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
*/
  /*******************************************
   *
   * 		CHAT LOG
   *
   ******************************************/
  public void addChatText(String type, int fromId, int toId, String message) {
    if (!Server.SERVER_RESTARTING) {
      try {
        PreparedStatement stat =
            conn.prepareStatement(
                "insert into chat (Type,FromId,ToId,Message,Date) values (?,?,?,?,?)");
        stat.setString(1, type);
        stat.setInt(2, fromId);
        stat.setInt(3, toId);
        stat.setString(4, message);
        stat.setString(5, TimeUtils.now());
        stat.executeUpdate();
        stat.close();
      } catch (SQLException e) {
        ServerMessage.printMessage("Error adding chat message", false);
        e.printStackTrace();
      }
    }
  }

  /*
   * 	KILL INFO
   *
   *  Update nr of kills of specific creature
   *
   */
  public void addKills(int PlayerId, int CreatureId) {

    int rowId = askInt(
            "select Id from user_kills where CreatureId = "
                + CreatureId
                + " and UserId = "
                + PlayerId);

    if (rowId != 0) {
      updateDB("update user_kills set Kills = Kills + 1 where Id = " + rowId);
    } else {
      updateDB(
          "insert into user_kills (UserId, CreatureId, Kills) values("
              + PlayerId
              + ","
              + CreatureId
              + ",1)");
    }
  }

  public int monsterKillCount(int userId, int monsterId) {
    int nrKills = askInt(
            "select Kills from user_kills where CreatureId = "
                + monsterId
                + " and UserId = "
                + userId);

    return nrKills;
  }

  /*
   *
   * 	EQUIP / INVENTORY
   *
   */

  public boolean checkCharacterOwnership(int characterId, int userId) {
    int own = askInt(
            "select Id from user_character where Id = " + characterId + " and UserId = " + userId);

    if (own == 0) {
      ServerMessage.printMessage(
          "Hack attempt? User " + userId + " does not own character " + characterId, false);
    }

    return (own != 0);
  }

  /*
   * 	GET COORDINATES
   *
   *  Get player coordinates
   *
   */
  public String getCoord(int playerId) {

    int playerX = 0;
    int playerY = 0;

    try {                  //      1  2
      ResultSet rs = askDB("select X, Y from user where Id = " + playerId);

      while (rs.next()) {
        playerX = rs.getInt(1);
        playerY = rs.getInt(2);
      }
      rs.close();
    } catch (SQLException e) {
      return "0";
    }
    return Integer.toString(playerX) + "-" + Integer.toString(playerY);
  }

  public void closeDB() {
    try {
      conn.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    conn = null;
  }

  public boolean checkMail(String mail) {
    Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
    Matcher m = p.matcher(mail);
    boolean matchFound = m.matches();

    if (matchFound) {
      return true;
    }
    return false;
  }
}
