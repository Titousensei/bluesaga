package data_handlers;

import java.util.Map;

import data_handlers.ability_handler.StatusEffect;
import network.Client;
import network.Server;
import utils.ServerGameInfo;

public class MusicHandler extends Handler {

  public static void init() {
    DataHandlers.register("playnote", m -> handlePlayNote(m));
  }

  public static void handlePlayNote(Message m) {
    Client client = m.client;
    String note = m.message;

    // CHECK IF PLAYER HAS INSTRUMENT EQUIPPED
    if (client.playerCharacter.getWeapon() != null) {
      if (client.playerCharacter.getWeapon().getSubType().equals("Instrument")) {

        int instrumentId = client.playerCharacter.getWeapon().getId();

        client.playerCharacter.addStatusEffect(ServerGameInfo.newStatusEffect(14));

        // SEND NOTE TO ALL PLAYERS IN VICINITY
        for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
          Client s = entry.getValue();

          if (s.Ready) {
            if (s.playerCharacter != null) {
              if (isVisibleForPlayer(
                  s.playerCharacter,
                  client.playerCharacter.getX(),
                  client.playerCharacter.getY(),
                  client.playerCharacter.getZ())) {
                addOutGoingMessage(
                    s, "statuseffect_add", client.playerCharacter.getSmallData() + "/14,17");
                addOutGoingMessage(
                    s,
                    "playnote",
                    client.playerCharacter.getSmallData() + ";" + instrumentId + "," + note);
              }
            }
          }
        }
      }
    }
  }
}
