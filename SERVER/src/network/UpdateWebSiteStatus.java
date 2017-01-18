package network;

import game.ServerSettings;
import login.WebsiteServerStatus;
import utils.ServerMessage;

public class UpdateWebSiteStatus
extends Thread
{
  private Server server;

  UpdateWebSiteStatus(Server server) {
    this.server = server;
  }

  @Override
  public void run() {

    while (server.running) {

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
