package network;

import game.ServerSettings;
import login.WebsiteServerStatus;
import utils.ServerMessage;

public class UpdateWebSiteStatus
extends Thread
{
  private Server server;
  private boolean running = false;

  UpdateWebSiteStatus(Server server) {
    this.server = server;
  }

  public void exit() {
    try {
      running = false;
      join();
    }
    catch (InterruptedException ex) {
      return;
    }
  }

  @Override
  public void run() {
    running = true;

    while (running) {

      try {
        int nrPlayers = Server.clients.size();
        WebsiteServerStatus.UpdateServerStatus(ServerSettings.SERVER_ID, nrPlayers);

        // Every minute
        sleep(60000L);
      }
      catch (InterruptedException ex) {
        return;
      }
      catch (Exception ex) {
        ServerMessage.println(false, "WARNING - WebsiteServerStatus failed: ", ex.toString());
      }
    }
  }
}
