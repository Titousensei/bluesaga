package data_handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.*;

import utils.ServerGameInfo;
import utils.ServerMessage;
import components.Quest;
import components.Ship;
import components.UserQuest;
import creature.Creature;
import creature.Npc;
import creature.Creature.CreatureType;
import data_handlers.ability_handler.Ability;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.item_handler.CoinConverter;
import data_handlers.item_handler.EquipHandler;
import data_handlers.item_handler.InventoryHandler;
import data_handlers.item_handler.Item;
import data_handlers.item_handler.Modifier;
import game.ServerSettings;
import map.Tile;
import network.Client;
import network.Server;

public class QuestHandler extends Handler {

  public final static Integer STATUS_TALK = Integer.valueOf(3);
  public final static Integer STATUS_NEWQUEST = Integer.valueOf(13);
  public final static Integer STATUS_COMPLETEQUEST = Integer.valueOf(14);

  public static void init() {
    DataHandlers.register("talknpc", m -> handleTalkNpc(m));
    DataHandlers.register("quest", m -> handleQuest(m));
    DataHandlers.register("questdescr", m -> handleQuestDescription(m));
    DataHandlers.register("myquests", m -> handleMyQuests(m));
    DataHandlers.register("checkin", m -> handleCheckIn(m));
    DataHandlers.register("learn_class", m -> handleLearnClass(m));
  }

  public static void handleTalkNpc(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    String talkInfo[] = m.message.split(";");

    int tileX = Integer.parseInt(talkInfo[0]);
    int tileY = Integer.parseInt(talkInfo[1]);
    int tileZ = client.playerCharacter.getZ();

    getNpcDialog(client, tileX, tileY, tileZ);
  }

  public static void handleQuest(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    String questInfo[] = m.message.split(";");

    String action = questInfo[0];
    int questId = Integer.parseInt(questInfo[1]);

    // ACTION, QUEST ID
    if (action.equals("info")) {
      getQuestInfo(client, questId);
    } else if (action.equals("add")) {
      addQuest(client, questId);
    }
  }

  public static void handleQuestDescription(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    int questId = Integer.parseInt(m.message);

    Quest questInfo = ServerGameInfo.questDef.get(questId);
    String questText = questInfo.getDescription();

    addOutGoingMessage(client, "questdesc", questText);
  }

  public static void handleMyQuests(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    ResultSet myQuestInfo =
        Server.userDB.askDB(
            //      1        2
            "select QuestId, Status from character_quest where CharacterId = "
                + client.playerCharacter.getDBId()
                + " and Status != 0 and Status < 3");

    StringBuilder questData = new StringBuilder(1000);

    try {
      while (myQuestInfo.next()) {
        Quest questInfo = ServerGameInfo.questDef.get(myQuestInfo.getInt(1));
        int status = myQuestInfo.getInt(2);
        if (status < 0 ) {
          status = 1;
        }

        questData
            .append(questInfo.getId())
            .append(',')
            .append(questInfo.getName())
            .append(',')
            .append(questInfo.getType())
            .append(',')
            .append(status)
            .append(',')
            .append(questInfo.getLevel())
            .append(';');
      }
      myQuestInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (questData.length() == 0) {
      questData.append("None");
    }
    addOutGoingMessage(client, "myquests", questData.toString());
  }

  public static void handleCheckIn(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    int checkInId = Integer.parseInt(m.message);

    Server.userDB.updateDB(
        "update user_character set CheckpointId = "
            + checkInId
            + " where Id = "
            + client.playerCharacter.getDBId());
    addOutGoingMessage(client, "message", "#messages.quest.checked_in");

    ServerMessage.println(false, "Checkin - ", m.client.playerCharacter,
        ": ", checkInId);
  }

  public static void handleLearnClass(Message m) {
    if (m.client.playerCharacter == null) return;
    Client client = m.client;
    String classInfo[] = m.message.split(",");
    int questId = Integer.parseInt(classInfo[0]);
    int classType = Integer.parseInt(classInfo[1]);

    // Get class that is learnt from quest
    Quest questInfo = ServerGameInfo.questDef.get(questId);
    int classId = questInfo.getLearnClassId();
    if (ClassHandler.learnClass(client, classId, classType)) {
      EquipHandler.checkRequirements(client);
      rewardQuest(client, questId);
    }
  }

  public static Map<Integer, Integer> getQuestNpcsForPlayer(int charId) {

    Set<Quest> qComplete = new HashSet<>(100);
    Set<Quest> qReward   = new HashSet<>(100);
    Set<Quest> qAccepted = new HashSet<>(100);
    Set<Quest> qNew      = new HashSet<>(100);

    ResultSet myQuestInfo =
        Server.userDB.askDB(
            //      1        2
            "select QuestId, Status from character_quest where CharacterId = "
                + charId);

    try {
      while (myQuestInfo.next()) {
        Quest q = ServerGameInfo.questDef.get(myQuestInfo.getInt(1));
        switch (myQuestInfo.getInt(2)) {
        case 1:  // accepted
          qAccepted.add(q);
          break;
        case 2:  // get reward
          qReward.add(q);
          break;
        case 3:  // completed
          qComplete.add(q);
          break;
        }
      }
      myQuestInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    for (Map.Entry<Integer, Quest> entry : ServerGameInfo.questDef.entrySet()) {
      Quest q = entry.getValue();
      if (q.getType() == Quest.QType.Instructions
      ||  q.getType() == Quest.QType.LearnClass
      ) {
        continue;
      }
      if (qComplete.contains(q)) {
        continue;
      }
      if (qAccepted.contains(q)) {
        continue;
      }
      if (qReward.contains(q)) {
        continue;
      }
      int parentId = q.getParentQuestId();
      Quest parent = ServerGameInfo.questDef.get(parentId);
      if (parent==null || qComplete.contains(parent)) {
        qNew.add(q);
      }
    }

    Map<Integer, Integer> npcStatus = new HashMap<>(qReward.size() + qNew.size());
    for (Quest q : qNew) {
      npcStatus.put(q.getNpcId(), STATUS_NEWQUEST);
    }
    for (Quest q : qReward) {
      npcStatus.put(q.getNpcId(), STATUS_COMPLETEQUEST);
    }

    return npcStatus;
  }

  public static void sendNpcBubbleUpdate(Client client, Quest q) {
    Map<Integer, Integer> oldQuestStatus = client.playerCharacter.getNpcQuestStatusMap();
    client.playerCharacter.loadQuests();
    Map<Integer, Integer> newQuestStatus = client.playerCharacter.getNpcQuestStatusMap();

    // send the updates for the NPCs with changed status
    for (Map.Entry<Integer, Integer> entry : newQuestStatus.entrySet()) {
      Integer oldStatus = oldQuestStatus.remove(entry.getKey());
      if (oldStatus==null || !oldStatus.equals(entry.getValue())) {
        MapHandler.sendCreatureInfo(client, CreatureType.Monster, entry.getKey());
      }
    }

    for (Map.Entry<Integer, Integer> entry : oldQuestStatus.entrySet()) {
      MapHandler.sendCreatureInfo(client, CreatureType.Monster, entry.getKey());
    }
  }

  public static void getNpcDialog(Client client, int tileX, int tileY, int tileZ) {
    // CHECK IF THERE IS NPC ON TILE
    Tile TILE = Server.WORLD_MAP.getTile(tileX, tileY, tileZ);

    if (TILE.getOccupantType() == CreatureType.Monster) {
      Npc NPC = Server.WORLD_MAP.getMonster(TILE.getOccupant().getDBId());

      // NPC IS FRIENDLY
      if (NPC.getAggroType() == 3) {
        // SEND TALK TUTORIAL
        TutorialHandler.updateTutorials(2, client);

        updateTalkToQuests(client, NPC.getDBId());

        // NpcName / QuestId, QuestName, QuestStatus; QuestId2, QuestName2, QuestStatus2 / ShopOrNot

        StringBuilder npcInfo = new StringBuilder(1000);
        npcInfo
            .append(NPC.getDBId())
            .append(';')
            .append(NPC.getX())
            .append(';')
            .append(NPC.getY())
            .append(';')
            .append(NPC.getName())
            .append('/');

        // GET QUESTS FROM NPC
        int nrQuests = 0;
        if (ServerGameInfo.questNpc.get(NPC.getDBId()) != null) {
          for (Quest questInfo : ServerGameInfo.questNpc.get(NPC.getDBId())) {
            boolean showOk = true;

            // IF QUEST HAS PARENT QUEST
            if (questInfo.getParentQuestId() > 0) {
              showOk = false;
              // CHECK IF PARENT QUEST IS COMPLETED
              int questParentStatus =
                  Server.userDB.askInt(
                      "select Status from character_quest where QuestId = "
                          + questInfo.getParentQuestId()
                          + " and CharacterId = "
                          + client.playerCharacter.getDBId());
              if (questParentStatus == 3) {
                showOk = true;
              }
            }

            // NOT SHOW COMPLETED QUESTS
            // 0 = new, 1 = accepted, 2 = get reward, 3 = completed
            // negative status (-1) is used to store the initial count of some quests, like kill
            int questStatus = Server.userDB.askInt(
                "select Status from character_quest where QuestId = "
                    + questInfo.getId()
                    + " and CharacterId = "
                    + client.playerCharacter.getDBId());

            if (questStatus == 3) {
              showOk = false;
            } else if (questStatus < 0) {
              questStatus = 1;
            }

            if (showOk) {
              // SEND QUEST IF NOT COMPLETED

              npcInfo
                  .append(questInfo.getId())
                  .append(',')
                  .append(questInfo.getName())
                  .append(',')
                  .append(questInfo.getType())
                  .append(',')
                  .append(questInfo.getLevel())
                  .append(',')
                  .append(questStatus)
                  .append(';');

              nrQuests++;
            }
          }
        }

        if (nrQuests == 0) {
          npcInfo.append("None");
        }

        // GET SHOP FROM NPC
        if (ServerGameInfo.shopDef.containsKey(NPC.getDBId())) {
          npcInfo.append('/').append(NPC.getDBId());
        }
        else {
          npcInfo.append("/0");
        }

        // GET CHECKIN FROM NPC
        if (ServerGameInfo.checkpointDef.containsKey(NPC.getDBId())) {
          npcInfo.append('/').append(NPC.getDBId());
        }
        else {
          npcInfo.append("/0");
        }

        // GET BOUNTY MERCHANT

        int bountyId = 0;
        /*
                  rs = Server.gameDB.askDB("select Id from bountyhut where NpcId = "+NPC.getDBId());

                  if(rs.next()){
                    bountyId = rs.getInt("Id");
                  }
                  rs.close();
        */
        npcInfo.append('/').append(bountyId);

        addOutGoingMessage(client, "talknpc", npcInfo.toString());
      }
    }
  }

  public static void addQuest(Client client, int questId) {
    // CHECK IF PLAYER ALREADY HAVE QUEST
    // AND IF HAVE REQUIREMENTS

    Quest checkQuest = ServerGameInfo.questDef.get(questId);
    boolean addOk = true;

    if (checkQuest!=null) {
      if (Quest.QType.Instructions.equals(checkQuest.getType())
      || Quest.QType.Story.equals(checkQuest.getType())) {
        addOk = false;
      } else {
        // CHECK IF COMPLETED PARENT QUEST
        if (checkQuest.getParentQuestId() > 0) {
          int parentQuestStatus = Server.userDB.askInt(
                  "select Status from character_quest where QuestId = "
                      + checkQuest.getParentQuestId()
                      + " and CharacterId = "
                      + client.playerCharacter.getDBId());

          if (parentQuestStatus < 3) {
            addOk = false;
          }
        }
        // CHECK IF ALREADY HAVE QUEST
        int haveQuest = Server.userDB.askInt(
                "select Status from character_quest where QuestId = "
                    + questId
                    + " and CharacterId = "
                    + client.playerCharacter.getDBId());
        if (haveQuest!=0) {
          addOk = false;
        }
      }
      // IF ALL IS FINE, ADD QUEST
      if (addOk) {
        checkQuestItem(client, checkQuest);
        checkQuestAbility(client, checkQuest);

        ServerMessage.println(false, "AcceptQuest - ", client.playerCharacter,
            questId, ":", checkQuest.getName());

        addOutGoingMessage(client, "quest", "add;" + checkQuest.getName());

        if (Quest.QType.Kill.equals(checkQuest.getType())) {
          int nrKills = Server.userDB.askInt(
                  "select Kills from character_kills where CreatureId = "
                      + checkQuest.getTargetId()
                      + " and CharacterId = "
                      + client.playerCharacter.getDBId());
          Server.userDB.updateDB(
              "insert into character_quest (QuestId, CharacterId, Status) values("
                  + questId
                  + ","
                  + client.playerCharacter.getDBId()
                  + ","
                  + (- nrKills - 1)
                  + ")");
        }
        else {
          Server.userDB.updateDB(
              "insert into character_quest (QuestId, CharacterId, Status) values("
                  + questId
                  + ","
                  + client.playerCharacter.getDBId()
                  + ",1)");
        }
        sendNpcBubbleUpdate(client, checkQuest);
      }
    }
  }

  private static void checkQuestAbility(Client client, Quest addedQuest) {

    // Check if quest gives ability
    if (addedQuest.getQuestAbilityId() != 0) {
      // Check if player has ability already
      if (client.playerCharacter != null) {
        if (client.playerCharacter.getAbilityById(addedQuest.getQuestAbilityId()) == null) {
          client.playerCharacter.addAbility(
              new Ability(ServerGameInfo.abilityDef.get(addedQuest.getQuestAbilityId())));
          Server.userDB.updateDB(
              "insert into character_ability (CharacterId, AbilityId, CooldownLeft) values ("
                  + client.playerCharacter.getDBId()
                  + ","
                  + addedQuest.getQuestAbilityId()
                  + ",0)");
          addOutGoingMessage(
              client, "abilitydata", "0/" + client.playerCharacter.getAbilitiesAsString());
          addOutGoingMessage(
              client,
              "message",
              "#messages.quest.gained"
                  + " '"
                  + ServerGameInfo.abilityDef.get(addedQuest.getQuestAbilityId()).name
                  + "' #messages.quest.ability");
        }
      }
    }
  }

  // Check if there is a quest item to be given before doing the quest
  private static void checkQuestItem(Client client, Quest addedQuest) {
    // CHECK IF THERE IS A QUEST ITEM
    if (addedQuest!=null && addedQuest.getQuestItems()!=null) {
      for (Item questItem : addedQuest.getQuestItems()) {
        InventoryHandler.addItemToInventory(client, questItem);
      }
    }
  }

  private static void getQuestInfo(Client client, int questId) {

    Quest questInfo = ServerGameInfo.questDef.get(questId);
    if (questInfo != null) {

      // GET QUEST STATUS
      int questStatus = Server.userDB.askInt(
              "select Status from character_quest where QuestId = "
                  + questId
                  + " and CharacterId = "
                  + client.playerCharacter.getDBId());

      // COMPLETE QUESTS OF "STORY" TYPE DIRECTLY
      if (Quest.QType.Story.equals(questInfo.getType())) {
        // GIVE QUEST ITEM
        checkQuestItem(client, questInfo);
        Server.userDB.updateDB(
            "insert into character_quest (QuestId, CharacterId, Status) values ("
                + questId
                + ","
                + client.playerCharacter.getDBId()
                + ",3)");

        ServerMessage.println(false, "Story - ", client.playerCharacter,
            questId, ": ", questInfo.getName());

        // CHECK IF QUEST TRIGGERS NEW QUEST
        if (questInfo.getNextQuestId() > 0) {
          addQuest(client, questInfo.getNextQuestId());
        }
        sendNpcBubbleUpdate(client, questInfo);
      }

      // IF COMPLETED, FINISH QUEST, GIVE REWARD
      boolean completedQuest = false;

      if (questStatus == 2) {
        TutorialHandler.updateTutorials(6, client);
        completedQuest = rewardQuest(client, questId);

        ServerMessage.println(false, "QuestReward - ", client.playerCharacter,
            questId, ": ", questInfo.getName());

        questStatus = 3;
      } else if (questStatus < 0) {
        questStatus = 1;

        ServerMessage.println(false, "TalkNpc - ", client.playerCharacter,
            questId, ": ", questInfo.getName());
      }

      String questType = questInfo.getType().toString();
      String questMessage = (completedQuest)
                            ? questInfo.getRewardMessage()
                            : questInfo.getQuestMessage();

      addOutGoingMessage(
          client,
          "questinfo",
          questId + ";" + questType + ";" + questMessage + ";" + questStatus);
    }
  }

  /*
   *
   *  DIFFERENT QUEST TYPES
   *
   */

  // USE X ITEM X
  public static void updateUseItemQuests(Client client, int itemId) {
    for (UserQuest q : client.playerCharacter.getQuests()) {
      if (Quest.QType.UseItem.equals(q.questRef.getType())
      && q.getStatus() == 1
      && q.questRef.getTargetId() == itemId
      ) {
        // COMPLETE QUEST
        q.setStatus(2);
        Server.userDB.updateDB(
            "update character_quest set Status = 2 where QuestId = "
                + q.questRef.getId()
                + " and CharacterId = "
                + client.playerCharacter.getDBId());
        addOutGoingMessage(client, "quest", "complete;" + q.questRef.getName());
        ServerMessage.println(false, "CompleteQuest Use - ", client.playerCharacter,
            q.questRef.getId(), ":", q.questRef.getName());
        if (!q.questRef.isReturnForReward()) {
          rewardQuest(client, q.questRef.getId());
        }
        sendNpcBubbleUpdate(client, q.questRef);
      }
    }
  }

  // KILL X CREATURE X
  public static void updateKills(Client client, Creature victim) {

    // CHECK IF KILLED CREATURE BEFORE
    int nrKills = Server.userDB.askInt(
            "select Kills from character_kills where CreatureId = "
                + victim.getCreatureId()
                + " and CharacterId = "
                + client.playerCharacter.getDBId());
    if (nrKills>0) {
      Server.userDB.updateDB(
          "update character_kills set Kills = Kills + 1  where CreatureId = "
              + victim.getCreatureId()
              + " and CharacterId = "
              + client.playerCharacter.getDBId());
    } else {
      nrKills = 1;
      Server.userDB.updateDB(
          "insert into character_kills (CharacterId, CreatureId, Kills) values ("
              + client.playerCharacter.getDBId()
              + ","
              + victim.getCreatureId()
              + ",1)");
    }

    List<UserQuest> playerQuests = client.playerCharacter.getQuests();
    for (UserQuest q : playerQuests) {
      if (q != null) {
        // CHECK IF COMPLETED ANY "KILL X CREATURE X" - QUESTS
        if (Quest.QType.Kill.equals(q.questRef.getType())
        && (q.getStatus() == 1 || q.getStatus() < 0)
        && q.questRef.getTargetId() == victim.getCreatureId()
        ) {
          int actualKills = nrKills;
          if (q.getStatus() < 0) {
            actualKills += q.getStatus() + 2; // +1 for encoding negative zero, +1 for this kill
          }

          addOutGoingMessage(
              client,
              "quest",
              "update;" + q.questRef.getName() + ";" + actualKills + " / " + q.questRef.getTargetNumber() + " killed");

          if (actualKills >= q.questRef.getTargetNumber()) {
            q.setStatus(2);

            // COMPLETED QUEST
            Server.userDB.updateDB(
                "update character_quest set Status = 2 where QuestId = "
                    + q.questRef.getId()
                    + " and CharacterId = "
                    + client.playerCharacter.getDBId());
            addOutGoingMessage(client, "quest", "complete;" + q.questRef.getName());
            ServerMessage.println(false, "CompleteQuest Kill - ", client.playerCharacter,
                q.questRef.getId(), ":", q.questRef.getName());

            // CHECK KILL PRACTICE TARGET TUTORIAL
            if (q.questRef.getId() == 36) {
              TutorialHandler.updateTutorials(5, client);
            }

            if (!q.questRef.isReturnForReward()) {
              rewardQuest(client, q.questRef.getId());
            }
            checkQuestEvents(client, q.questRef);
            sendNpcBubbleUpdate(client, q.questRef);
          }
        }
      }
    }
  }

  // "FIND AREA X" - QUESTS
  public static void updateFindAreaQuests(Client client, int areaEffectId) {
    for (UserQuest q : client.playerCharacter.getQuests()) {
      if (Quest.QType.GoTo.equals(q.questRef.getType())
      && q.getStatus() == 1
      && q.questRef.getTargetId() == areaEffectId
      ) {
        q.setStatus(2);
        // COMPLETED QUEST
        Server.userDB.updateDB(
            "update character_quest set Status = 3 where QuestId = "
                + q.questRef.getId()
                + " and CharacterId = "
                + client.playerCharacter.getDBId());
        addOutGoingMessage(client, "quest", "complete;" + q.questRef.getName());
        ServerMessage.println(false, "CompleteQuest Area - ", client.playerCharacter,
            q.questRef.getId(), ":", q.questRef.getName());
        if (!q.questRef.isReturnForReward()) {
          rewardQuest(client, q.questRef.getId());
        }
        checkQuestEvents(client, q.questRef);
        sendNpcBubbleUpdate(client, q.questRef);
      }
    }
  }

  // "TALK TO CREATURE X" - QUESTS
  public static void updateTalkToQuests(Client client, int npcId) {
    for (UserQuest q : client.playerCharacter.getQuests()) {
      if (Quest.QType.TalkTo.equals(q.questRef.getType())
      && q.getStatus() == 1
      && q.questRef.getTargetId() == npcId
      ) {
        // GIVE SECOND SHIP
        checkQuestShip(client, q.questRef);
        q.setStatus(2);
        if (!q.questRef.isReturnForReward()) {
          rewardQuest(client, q.questRef.getId());
        }
        addOutGoingMessage(client, "quest", "complete;" + q.questRef.getName());
        ServerMessage.println(false, "CompleteQuest Talk - ", client.playerCharacter,
            q.questRef.getId(), ":", q.questRef.getName());
      }
    }
  }

  // "GET X ITEMS X" - QUESTS
  public static void updateItemQuests(Client client, int itemId) {
    Item itemToCheck = ServerGameInfo.itemDef.get(itemId);
    for (UserQuest q : client.playerCharacter.getQuests()) {
      if (q.getStatus() != 1) {
        continue;
      }
      boolean questCompleted = false;

      Quest.QType qt = q.questRef.getType();
      if (Quest.QType.GetRare.equals(qt)) {
        if (itemToCheck.isOfType(q.questRef.getTargetType(), q.questRef.getTargetSubType())) {
          int modifierId = Server.userDB.askInt(
                  "select ModifierId from character_item where InventoryPos <> 'Mouse' and Equipped = 0 and ItemId = "
                      + itemId
                      + " and CharacterId = "
                      + client.playerCharacter.getDBId());
          questCompleted = (modifierId >= Modifier.Good.id);
        }
      }
      else if (Quest.QType.GetItem.equals(qt)
      && q.questRef.getTargetId() == itemId
      ) {
        if (itemToCheck.getType().equals("Key")) {
          // If item is a key, check keychain
          int keyInfo = Server.userDB.askInt(
                  "select Id from character_key where KeyId = "
                      + itemId
                      + " and CharacterId = "
                      + client.playerCharacter.getDBId());
          if (keyInfo>0) {
            questCompleted = true;
          }
        } else {
          // CHECK IF RIGHT NR
          int nrItems = Server.userDB.askInt(
                  "select sum(Nr) from character_item where InventoryPos <> 'Mouse' and Equipped = 0 and ItemId = "
                      + itemId
                      + " and CharacterId = "
                      + client.playerCharacter.getDBId());
          if (nrItems >= q.questRef.getTargetNumber()) {
            questCompleted = true;
          } else {
            Item questItem = ServerGameInfo.itemDef.get(itemId);
            addOutGoingMessage(
                client,
                "quest",
                "update;"
                    + q.questRef.getName()
                    + ";"
                    + nrItems
                    + " / "
                    + q.questRef.getTargetNumber()
                    + " "
                    + questItem.getName()
                    + " collected");
          }
        }
      }

      if (questCompleted) {
        // COMPLETE QUEST
        q.setStatus(2);
        Server.userDB.updateDB(
            "update character_quest set Status = 2 where QuestId = "
                + q.questRef.getId()
                + " and CharacterId = "
                + client.playerCharacter.getDBId());
        addOutGoingMessage(client, "quest", "complete;" + q.questRef.getName());
        ServerMessage.println(false, "CompleteQuest Get - ", client.playerCharacter,
            q.questRef.getId(), ":", q.questRef.getName());
        if (!q.questRef.isReturnForReward()) {
          rewardQuest(client, q.questRef.getId());
        }
        sendNpcBubbleUpdate(client, q.questRef);
      }
    }
  }

  /**
   *
   * QUEST COMPLETION/REWARD
   *
   **/
  public static boolean rewardQuest(Client client, int questId) {
    boolean rewardOk = true;

    Quest questInfo = ServerGameInfo.questDef.get(questId);

    // GIVE REWARD FOR COMPLETING QUEST
    if (questInfo!=null) {

      // CHECK IF ITEM QUEST
      if (Quest.QType.GetItem.equals(questInfo.getType())) {
        // CHECK THAT PLAYER HAS ITEMS IN HIS INVENTORY

        int checkItemId = questInfo.getTargetId();
        int checkItemNr = questInfo.getTargetNumber();

        Item itemToCheck = ServerGameInfo.itemDef.get(checkItemId);
        if (!itemToCheck.getType().equals("Key")) {
          int nrItems = InventoryHandler.countPlayerItem(client, checkItemId);

          if (nrItems >= checkItemNr) {
            // HAS ITEMS, REMOVE THEM FROM INVENTORY
            InventoryHandler.removeNumberOfItems(client, checkItemId, checkItemNr);
          } else {
            addOutGoingMessage(client, "message", "#messages.quest.missing_items");
            rewardOk = false;
          }
        }
      } else if (Quest.QType.GetRare.equals(questInfo.getType())) {
        rewardOk = InventoryHandler.removeRareItem(client,
                      questInfo.getTargetType(),
                      questInfo.getTargetSubType(),
                      Modifier.Good);
      }

      if (rewardOk) {

        Server.userDB.updateDB(
            "update character_quest set Status = 3 where QuestId = "
                + questId
                + " and CharacterId = "
                + client.playerCharacter.getDBId());

        int rewardCopper = questInfo.getRewardCopper();
        int rewardXp = questInfo.getRewardXp();
        int[] rewardItems = questInfo.getRewardItems();
        int rewardAbilityId = questInfo.getRewardAbilityId();

        if (rewardItems != null) {
          for (int i=0 ; i<rewardItems.length ; i++) {
            InventoryHandler.addItemToInventory(
                client, ServerGameInfo.newItem(rewardItems[i]));
          }
        }

        if (rewardCopper > 0) {
          CoinConverter cc = new CoinConverter(rewardCopper);
          InventoryHandler.addItemToInventory(client, cc.getGoldItem());
          InventoryHandler.addItemToInventory(client, cc.getSilverItem());
          InventoryHandler.addItemToInventory(client, cc.getCopperItem());
        }

        if (rewardXp > 0) {
          BattleHandler.addXP(client, rewardXp);
        }

        if (rewardAbilityId > 0) {
          // Check if player has ability
          Ability checkAbility = client.playerCharacter.getAbilityById(rewardAbilityId);
          if (checkAbility == null) {
            // Add ability
            Ability newAbility = new Ability(ServerGameInfo.abilityDef.get(rewardAbilityId));
            Server.userDB.updateDB(
                "insert into character_ability (CharacterId, AbilityId, CooldownLeft) values ("
                    + client.playerCharacter.getDBId()
                    + ","
                    + rewardAbilityId
                    + ",0)");
            client.playerCharacter.addAbility(newAbility);
            addOutGoingMessage(
                client,
                "message",
                "#messages.quest.gained# '" + newAbility.name + "' #messages.quest.ability");
            addOutGoingMessage(
                client, "abilitydata", "0/" + client.playerCharacter.getAbilitiesAsString());
          } else {
            addOutGoingMessage(
                client,
                "message",
                "#messages.quest.already_have# '"
                    + checkAbility.name
                    + "' #messages.quest.ability");
          }
        }

        // SPECIAL OCCASION
        // GET THE RAFT
        checkQuestShip(client, questInfo);
        UserQuest myQuest = client.playerCharacter.getQuestById(questId);
        if (myQuest != null) {
          myQuest.setStatus(3);

          // CHECK IF QUEST TRIGGERS NEW QUEST
          if (myQuest.questRef.getNextQuestId() > 0) {
            addQuest(client, myQuest.questRef.getNextQuestId());
          }
        }
        sendNpcBubbleUpdate(client, questInfo);
      }
    }
    return rewardOk;
  }

  public static void checkQuestEvents(Client client, Quest questInfo) {
    if (questInfo.getEventId() > 0 && ServerSettings.enableCutScenes) {
      addOutGoingMessage(client, "cutscene", "" + questInfo.getEventId());
    }
  }

  public static void checkQuestShip(Client client, Quest questInfo) {
    if (questInfo.getRewardShip()>0) {
      Ship sh = new Ship(questInfo.getRewardShip());
      Server.userDB.updateDB(
          "update user_character set ShipId = "
              + sh.id
              + " where Id = "
              + client.playerCharacter.getDBId());
      client.playerCharacter.setShip(sh);
      addOutGoingMessage(client, "getboat", sh.toString());
      addOutGoingMessage(client, "message", sh.getMessage());
    }
  }
}
