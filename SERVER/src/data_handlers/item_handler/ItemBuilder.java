package data_handlers.item_handler;

import java.util.*;
import java.io.File;

import components.Builder;
import components.Stats;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.item_handler.Item;

public class ItemBuilder
extends Builder<Item>
{
  public final static Set<String> ATTACK_TYPES = new HashSet(Arrays.asList(
      "MAGIC", "COLD", "FIRE", "SHOCK",
      "PIERCE", "SLASH", "STRIKE"));

  protected Item it = null;
  protected Map<String, Integer> info = null;

  public void init(int id, String name, String origin) {
    it = new Item(id, name, origin);
    // Defaults
    it.setStackable(1);
    it.setStacked(1);
    it.setSubType("");
    it.setTwoHands(false);
    it.setEquipable(false);
    it.setSellable(true);
    it.setValue(0);
    it.setRange(1);
    info = new LinkedHashMap<>();
  }

  @Override
  protected  boolean set(String name, String value)
  {
    if (Stats.NAMES.contains(name)) {
      it.setStats(name, parseInt(value));
      return true;
    }

    return false;
  }

  public void reqLevel(String val) {
    // ReqLevel: 11
    it.setReqLevel(parseInt(val));
  }

  public void reqStrength(String val) {
    it.setReqStrength(parseInt(val));
  }

  public void reqIntelligence(String val) {
    it.setReqIntelligence(parseInt(val));
  }

  public void reqAgility(String val) {
    it.setReqAgility(parseInt(val));
  }

  public void type(String val) {
    // Type: Weapon / Hammer
    String[] parts = val.split("/");
    String t = parts[0].trim();
    it.setType(t);
    if (parts.length>1) {
      it.setSubType(parts[1].trim());
    }

    if(t.equals("Head")
    || t.equals("Weapon")
    || t.equals("OffHand")
    || t.equals("Amulet")
    || t.equals("Artifact")
    ) {
      it.setEquipable(true);
    }
  }

  public void classId(String val) {
    // ClassId: 1
    it.setClassId(parseInt(val));
  }

  public void material(String val) {
    // Material: Iron
    it.setMaterial(val);
  }

  public void projectileId(String val) {
    it.setProjectileId(parseInt(val));
  }

  public void range(String val) {
    // Range: 1
    it.setRange(parseInt(val));
  }

  public void value(String val) {
    // Value: 110
    // Value: 10000 not sellable
    // Value: 0
    String[] parts = val.split(" ", 2);
    it.setValue(parseInt(parts[0]));
    if (parts.length>1) {
      if ("not sellable".equals(parts[1])) {
        it.setSellable(false);
      }
      else {
        System.err.println("WARNING - Unknown Value parameter: " + parts[1]);
      }
    }
  }

  public void stackable(String val) {
    it.setStackable(parseInt(val));
  }

  public void damage(String val) {
    // Damage: 1207 - 1810 SHOCK STRENGTH
    int d1 = 0;
    int d2 = 0;
    for (String part : val.split(" ")) {
      if (ATTACK_TYPES.contains(part)) {
        it.setAttackType(part);
      }
      else if (Stats.NAMES.contains(part)) {
        it.setDamageStat(part);
      }
      else {
        int v = parseInt(part);
        if (v==0) {
          continue;
        }
        else if (d1==0) {
          d1 = v;
        }
        else {
          d2 = v;
        }
      }
    }
    if (d2!=0) {
      it.setStats("MinDamage", (d1<d2) ? d1 : d2);
      it.setStats("MaxDamage", (d1>d2) ? d1 : d2);
    }
    else {
      System.err.println("WARNING - No damage for " + it);
    }
  }

  public void attackSpeed(String val) {
    it.setStats("ATTACKSPEED", parseInt(val));
  }

  public void twoHands() {
    it.setTwoHands(true);
  }

  public void scrollUseId(String val) {
    it.setScrollUseId(parseInt(val));
  }

  public void description(String val) {
    it.setDescription(val);
  }

  public void containerId(String val) {
    it.setContainerId(parseInt(val));
  }

  public void StatusEffects(String val) {
    String statusEffectIds[] = val.split(",");
    for (String statusEffectId : statusEffectIds) {
      int statusEffectIdInt = Integer.parseInt(statusEffectId);
      it.addStatusEffects(new StatusEffect(statusEffectIdInt));
    }
  }

  public void info(String val) {
    String parts[] = val.split(":");
    info.put(parts[0] + ": ", parseInt(parts[1]));
  }

  public Item build() {
    if (info.size() > 0) {
      it.setInfo(info);
    }
    return it;
  }

  public static void main(String... args) {
    Map<Integer, Item> m = new HashMap<>();
    Builder.load(args[0], ItemBuilder.class, m);
    if (args.length>1) {
      if ("-".equals(args[1])) {
        for (Item q : m.values()) {
          System.out.println(q.getId() + ": " + q.getName());
        }
      }
      else if ("+".equals(args[1])) {
        for (Item q : m.values()) {
          File f = new File("../CLIENT/bin/images/items/item" + q.getId() + ".png");
          if (!f.exists()) {
            System.out.println("WARNING - Missing image for item " + q.getId() + ":" + q.getName());
          }
        }
      }
      else {
        int itemid = parseInt(args[1]);
        for (Modifier mod : Modifier.values()) {
          Item it = new Item(m.get(itemid));
          it.setModifier(mod);
          System.out.println("--- " + it);
          System.out.println(it.getStats().getHashMap());
        }
      }
    }
  }
}
