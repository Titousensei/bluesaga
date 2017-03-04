package components;

import java.util.*;

import data_handlers.item_handler.Item;
import utils.ServerGameInfo;

public class QuestBuilder
extends Builder<Quest>
{
  public final static int WINDOW_SIZE = 42;

  protected Quest q = null;

  public void init(int id, String name, String origin) {
    q = new Quest(id, name, origin);
    q.setType(Quest.QType.Instructions);
  }

  public void rewardMessage(String val) {
    q.setRewardMessage(justifyLeft(WINDOW_SIZE, val.trim().replace('\n', '/')));
  }

  public void questMessage(String val) {
    q.setQuestMessage(justifyLeft(WINDOW_SIZE, val.trim().replace('\n', '/')));
  }

  public void description(String val) {
    q.setDescription(val.trim().replace('\n', ' '));
  }

  public void level(String val) {
    q.setLevel(parseInt(val));
  }

  public void npcId(String val) {
    q.setNpcId(Integer.valueOf(parseInt(val)));
  }

  public void eventId(String val) {
    q.setEventId(parseInt(val));
  }

  public void nextQuestId(String val) {
    q.setNextQuestId(parseInt(val));
  }

  public void goTo(String val) {
    q.setType(Quest.QType.GoTo);
    q.setTargetId(parseInt(val));
  }

  public void story() {
    q.setType(Quest.QType.Story);
  }

  public void getItem(String val) {
    q.setType(Quest.QType.GetItem);
    q.setTargetType("Item");
    if (val.contains("*")) {
      String[] parts = val.split("\\*");
      q.setTargetId(parseInt(parts[0].trim()));
      q.setTargetNumber(parseInt(parts[1].trim()));
    }
    else {
      q.setTargetId(parseInt(val));
      q.setTargetNumber(1);
    }
  }

  public void getRare(String val) {
    q.setType(Quest.QType.GetRare);
    String[] parts = val.split("/");
    q.setTargetType(parts[0].trim());
    q.setTargetSubType(parts[1].trim());
    q.setTargetNumber(1);
  }

  public void useItem(String val) {
    q.setType(Quest.QType.UseItem);
    q.setTargetType("Item");
    if (val.contains("*")) {
      String[] parts = val.split("\\*");
      q.setTargetId(parseInt(parts[0].trim()));
      q.setTargetNumber(parseInt(parts[1].trim()));
    }
    else {
      q.setTargetId(parseInt(val));
      q.setTargetType("Creature");
      q.setTargetNumber(1);
    }
  }

  public void kill(String val) {
    q.setType(Quest.QType.Kill);
    q.setTargetType("Creature");
    if (val.contains("*")) {
      String[] parts = val.split("\\*");
      q.setTargetId(parseInt(parts[0].trim()));
      q.setTargetNumber(parseInt(parts[1].trim()));
    }
    else {
      q.setTargetId(parseInt(val));
      q.setTargetNumber(1);
    }
  }

  public void talkTo(String val) {
    q.setType(Quest.QType.TalkTo);
    q.setTargetId(parseInt(val));
    q.setTargetType("Creature");
  }

  public void learnClass(String val) {
    q.setType(Quest.QType.LearnClass);
    q.setLearnClassId(parseInt(val));
  }

  public void reward(String val) {

    int num_items = 0;
    for(String r : val.split("\\s+")) {
      if (r.endsWith("xp")) {
        q.setRewardXp(parseInt(r.substring(0, r.length()-2)));
      }
      else if (r.endsWith("cc")) {
        q.setRewardCopper(parseInt(r.substring(0, r.length()-2)));
      }
      else if (r.endsWith("sc")) {
        q.setRewardCopper(parseInt(r.substring(0, r.length()-2)) * 100);
      }
      else if (r.endsWith("gc")) {
        q.setRewardCopper(parseInt(r.substring(0, r.length()-2)) * 10000);
      }
      else if (r.startsWith("i") || r.endsWith("it") || r.endsWith("item")) {
        ++ num_items;
      }
      else if (r.startsWith("ship")) {
        q.setRewardShip(parseInt(r.substring(4)));
      }
      else {
        throw new RuntimeException("Unknown reward: " + r);
      }
    }
    if (num_items>0) {
      int[] items = new int[num_items];
      int i = 0;
      for(String r : val.split("\\s+")) {
        if (r.startsWith("i")) {
          items[i] = parseInt(r);
          ++ i;
        }
        else if (r.endsWith("it")) {
          items[i] = parseInt(r.substring(0, r.length()-2));
          ++ i;
        }
        else if(r.endsWith("item")) {
          items[i] = parseInt(r.substring(0, r.length()-4));
          ++ i;
        }
      }
      q.setRewardItems(items);
    }
  }

  public void returnForReward() {
    q.setReturnForReward(true);
  }

  public void questItems(String val) {
    // Only null when called from main()
    if (ServerGameInfo.itemDef!=null) {
      String[] parts = val.split("\\s+");
      List<Item> ret = new ArrayList(parts.length);
      for (String id : parts) {
        int itemId = parseInt(id);
        Item it = ServerGameInfo.itemDef.get(itemId);
        ret.add(it);
      }
      q.setQuestItems(ret);
    }
  }

  public void questAbilityId(String val) {
    q.setQuestAbilityId(parseInt(val));
  }

  public void rewardAbilityId(String val) {
    q.setRewardAbilityId(parseInt(val));
  }

  public void parentQuestId(String val) {
    q.setParentQuestId(parseInt(val));
  }

  private String justifyLeft(int width, String st) {
    if (!"".equals(st)) {
      StringBuilder buf = new StringBuilder(st.length() + 20);
      buf.append(st);
      int lastspace = -1;
      int linestart = 0;
      int i = 0;
      int tempTextLines = 1;
      while (i < buf.length()) {
        if (buf.charAt(i) == ' ') lastspace = i;
        if (buf.charAt(i) == '/') {
          linestart = i + 1;
        }
        if (buf.charAt(i) == '\n') {
          lastspace = -1;
          linestart = i + 1;
          tempTextLines++;
        }
        if (i > linestart + width - 1) {
          if (lastspace != -1) {
            buf.setCharAt(lastspace, '\n');
            linestart = lastspace + 1;
            lastspace = -1;
            tempTextLines++;
          } else {
            buf.insert(i, '\n');
            linestart = i + 1;
            tempTextLines++;
          }
        }
        i++;
      }

      return buf.toString();
    }
    return st;
  }

  public Quest build() {
    //if (q.getNpcId()==0) {
    //  throw new RuntimeException("Missing NpcId: " + q);
    //}
    return q;
  }

  public static Map<Integer, List<Quest>> mapNpc(Map<Integer, Quest> questDef) {
    Map<Integer, List<Quest>> ret = new HashMap<>();
    for (Quest q : questDef.values()) {
      Integer npcid = q.getNpcId();
      List<Quest> l = ret.get(npcid);
      if (l==null) {
        l = new ArrayList<>(4);
        ret.put(npcid, l);
      }
      l.add(q);
    }
    return ret;
  }

  public static void verify(Map<Integer, Item> items, Map<Integer, Quest> questDef) {
    for (Quest q : questDef.values()) {
      int[] rit = q.getRewardItems();
      if (rit!=null) {
        for (int it : rit) {
          if (!items.containsKey(it)) {
            System.out.println("[QuestBuilder] ERROR - Unknow reward item for quest " + q + ": " + it);
          }
        }
      }

      List<Item> qit = q.getQuestItems();
      if (qit!=null) {
        for (Item it : qit) {
          if (!items.containsKey(it.getId())) {
            System.out.println("[QuestBuilder] ERROR - Unknow quest item for quest " + q + ": " + it);
          }
        }
      }
    }
  }

  public static void main(String... args) {
    Map<Integer, Quest> m = new HashMap<>();
    Builder.load(args[0], QuestBuilder.class, m);
    if (args.length>1) {
      if ("-".equals(args[1])) {
        for (Quest q : m.values()) {
          System.out.println(q);
        }
      }
      else {
        System.out.println(m.get(parseInt(args[1])));
      }
    }
  }
}
