package components;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import data_handlers.item_handler.Item;
import utils.ServerGameInfo;

public class Quest
{
  public enum QType {
    Instructions, GetItem, GoTo, Kill, LearnClass, Story, TalkTo, UseItem, GetRare
  };

  private final int Id;
  private final String Name;
  private final String origin;

  private int ParentQuestId;

  private String QuestMessage;
  private String RewardMessage;
  private String Description;

  private int Level;
  private QType Type;
  private int TargetNumber;
  private String TargetType;
  private String TargetSubType = null;
  private int TargetId;
  private int NextQuestId;
  private int RewardXp;
  private int RewardCopper;
  private int[] RewardItems;
  private int RewardShip = 0;
  private int RewardAbilityId;
  private Integer NpcId = null;
  private int EventId;

  private List<Item> questItems;
  private int questAbilityId;
  private int learnClassId;

  private boolean ReturnForReward = false;

  Quest(int id, String name, String origin) {
    this.Id = id;
    this.Name = name;
    this.origin = origin;
  }

  /****************************************
   *                                      *
   *             GETTER/SETTER            *
   *                                      *
   *                                      *
   ****************************************/
  public String getName() { return Name; }

  public int getId() { return Id; }

  public int getLevel() { return Level; }
  void setLevel(int val) { Level = val; }

  public QType getType() { return Type; }
  void setType(QType val) { Type = val; }

  public int getTargetNumber() { return TargetNumber; }
  void setTargetNumber(int val) { TargetNumber = val; }

  public String getTargetType() { return TargetType; }
  void setTargetType(String val) { TargetType = val; }

  public String getTargetSubType() { return TargetSubType; }
  void setTargetSubType(String val) { TargetSubType = val; }

  public int getTargetId() { return TargetId; }
  void setTargetId(int val) { TargetId = val; }

  public int getEventId() { return EventId; }
  void setEventId(int val) { EventId = val; }

  public String getQuestMessage() { return QuestMessage; }
  void setQuestMessage(String val) { QuestMessage = val; }

  public String getRewardMessage() { return RewardMessage; }
  void setRewardMessage(String val) { RewardMessage = val; }

  public String getDescription() { return Description; }
  void setDescription(String val) { Description = val; }

  public int getParentQuestId() { return ParentQuestId; }
  void setParentQuestId(int val) { ParentQuestId = val; }

  public int getNextQuestId() { return NextQuestId; }
  void setNextQuestId(int nextQuestId) { NextQuestId = nextQuestId; }

  public List<Item> getQuestItems() { return questItems; }
  void setQuestItems(List<Item> val) { questItems = val; }

  public boolean isReturnForReward() { return ReturnForReward; }
  void setReturnForReward(boolean returnForReward) { ReturnForReward = returnForReward;}

  public int getQuestAbilityId() { return questAbilityId; }
  void setQuestAbilityId(int val) { questAbilityId = val; }

  public int getLearnClassId() { return learnClassId; }
  void setLearnClassId(int val) { learnClassId = val; }

  public Integer getNpcId() { return NpcId; }
  void setNpcId(Integer val) { NpcId = val; }

  public int getRewardXp() { return RewardXp; }
  void setRewardXp(int val) { RewardXp = val; }

  public int getRewardCopper() { return RewardCopper; }
  void setRewardCopper(int val) { RewardCopper = val; }

  public int[] getRewardItems() { return RewardItems; }
  void setRewardItems(int[] val) { RewardItems = val; }

  public int getRewardShip() { return RewardShip; }
  void setRewardShip(int val) { RewardShip = val; }

  public int getRewardAbilityId() { return RewardAbilityId; }
  void setRewardAbilityId(int val) { RewardAbilityId = val; }

  public String origin() { return origin; }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Quest{").append(Id)
      .append(" / ").append(Name)
      .append(", Type=").append(Type);

    if (NpcId!=0) {
      sb.append(", NpcId=").append(NpcId);
    }

    if (ParentQuestId!=0) {
      sb.append(", ParentQuestId=").append(ParentQuestId);
    }

    if (TargetNumber!=0) {
      sb.append(", TargetNumber=").append(TargetNumber);
    }
    if (TargetType!=null) {
      sb.append(", TargetType=").append(TargetType);
    }
    if (TargetId!=0) {
      sb.append(", TargetId=").append(TargetId);
    }

    if (questItems!=null) {
      sb.append(", questItems=").append(questItems);
    }
    if (questAbilityId!=0) {
      sb.append(", questAbilityId=").append(questAbilityId);
    }
    if (learnClassId!=0) {
      sb.append(", learnClassId=").append(learnClassId);
    }
    if (EventId!=0) {
      sb.append(", EventId=").append(EventId);
    }

    sb.append(", Level=").append(Level);
    if (QuestMessage!=null) {
      sb.append(", QuestMessage=").append(QuestMessage.replace('\n','|'));
    }
    if (Description!=null) {
      sb.append(", Description=").append(Description.replace('\n','|'));
    }

    if (RewardMessage!=null) {
      sb.append(", RewardMessage=").append(RewardMessage.replace('\n','|'));
    }
    if (RewardXp>0) {
      sb.append(", RewardXp=").append(RewardXp);
    }
    if (RewardCopper>0) {
      sb.append(", RewardCopper=").append(RewardCopper);
    }
    if (RewardItems!=null) {
      sb.append(", RewardItems=").append(Arrays.toString(RewardItems));
    }
    if (RewardShip>0) {
      sb.append(", RewardShip=").append(RewardShip);
    }
    if (RewardAbilityId>0) {
      sb.append(", RewardAbilityId=").append(RewardAbilityId);
    }
    if (ReturnForReward) {
      sb.append(", ReturnForReward");
    }
    if (NextQuestId!=0) {
      sb.append(", NextQuestId=").append(NextQuestId);
    }

    sb.append(", Origin=").append(origin)
      .append('}');

    return sb.toString();
  }
}
