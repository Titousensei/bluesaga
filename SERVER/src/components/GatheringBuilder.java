package components;

import java.util.*;

import data_handlers.item_handler.Item;
import utils.ServerGameInfo;

public class GatheringBuilder
extends Builder<Gathering>
{
  protected Gathering g = null;

  public void init(int id, String name, String origin) {
    g = new Gathering(id, name, origin);
  }

  public void sourceName(String val) {
    g.setSourceName(val);
  }

  public void skillLevel(String val) {
    g.setSkillLevel(parseInt(val));
  }

  public void skillId(String val) {
    g.setSkillId(parseInt(val));
  }

  public void resourceId(String val) {
    g.setResourceId(parseInt(val));
  }

  public Gathering build() {
    return g;
  }

  public static Map<String, Gathering> mapName(Map<Integer, Gathering> def) {
    Map<String, Gathering> ret = new HashMap<>();
    for (Gathering g : def.values()) {
      ret.put(g.getName(), g);
    }
    return ret;
  }

  public static void verify(Map<Integer, Item> items, Map<Integer, Gathering> def) {
    for (Gathering ga : def.values()) {
      if (!items.containsKey(ga.getResourceId())) {
        System.out.println("[GatheringBuilder] WARNING - ERROR - Unknow item for gathering "
            + ga + ": " + ga.getResourceId());
      }
    }
  }

  public static void main(String... args) {
    Map<Integer, Gathering> m = new HashMap<>();
    Builder.load(args[0], GatheringBuilder.class, m);
    if (args.length>1) {
      if ("-".equals(args[1])) {
        for (Gathering q : m.values()) {
          System.out.println(q);
        }
      }
      else {
        System.out.println(m.get(parseInt(args[1])));
      }
    }
  }
}
