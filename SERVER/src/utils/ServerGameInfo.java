package utils;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;

import game.ServerSettings;
import map.AreaEffect;
import map.AreaEffectBuilder;
import network.Server;
import player_classes.*;
import components.Builder;
import components.Family;
import components.JobSkill;
import components.Quest;
import components.QuestBuilder;
import components.Shop;
import components.ShopBuilder;
import creature.Npc;
import data_handlers.ability_handler.Ability;
import data_handlers.ability_handler.StatusEffect;
import data_handlers.item_handler.Item;

public class ServerGameInfo {

  public static Map<Integer, Item> itemDef;

  public static Map<Integer, Shop> shopDef;

  public static Map<Integer, Ability> abilityDef;
  public static Map<Integer, JobSkill> skillDef;

  public static Map<Integer, Family> familyDef;
  public static Map<Integer, BaseClass> classDef;

  public static Map<Integer, AreaEffect> areaEffectsDef;

  public static Map<Integer, Quest> questDef;
  public static Map<Integer, List<Quest>> questNpc;

  public static Map<Integer, Npc> creatureDef;

  public static void load() {
    // LOAD ITEMS
    itemDef = new HashMap<Integer, Item>();

    ResultSet itemInfo = Server.gameDB.askDB("select * from item");

    try {
      while (itemInfo.next()) {
        Item newItem = new Item();
        newItem.load(itemInfo);

        itemDef.put(itemInfo.getInt("Id"), newItem);
      }
      itemInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // LOAD CLASSES INFO
    classDef = new HashMap<Integer, BaseClass>();

    classDef.put(1, new WarriorClass());
    classDef.put(2, new MageClass());
    classDef.put(3, new HunterClass());
    classDef.put(4, new KnightClass());
    classDef.put(5, new FireMageClass());
    classDef.put(6, new ArcherClass());
    classDef.put(7, new TankClass());
    classDef.put(8, new IceMageClass());
    classDef.put(9, new DruidClass());
    classDef.put(10, new BerserkerClass());
    classDef.put(11, new ShockMageClass());
    classDef.put(12, new AssassinClass());
    classDef.put(14, new WhiteMageClass());

    // LOAD ABILITIES
    abilityDef = new HashMap<Integer, Ability>();

    ResultSet abilityInfo = Server.gameDB.askDB("select * from ability");

    try {
      while (abilityInfo.next()) {
        Ability newAbility = new Ability();
        newAbility.load(abilityInfo);

        for (StatusEffect se : newAbility.getStatusEffects()) {
          se.setAbility(newAbility);
        }

        abilityDef.put(abilityInfo.getInt("Id"), newAbility);
      }
      abilityInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // LOAD SKILLS INFO
    skillDef = new HashMap<Integer, JobSkill>();

    ResultSet skillInfo = Server.gameDB.askDB("select * from skill");

    try {
      while (skillInfo.next()) {
        JobSkill newSkill = new JobSkill();
        newSkill.load(skillInfo);

        skillDef.put(skillInfo.getInt("Id"), newSkill);
      }
      skillInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // LOAD FAMILIES INFO
    familyDef = new HashMap<Integer, Family>();
    familyDef.put(1, new Family(1, "Forest Creatures", 3));
    familyDef.put(2, new Family(2, "Mountain Dwellers", 6));
    familyDef.put(3, new Family(3, "Undead", 1));
    familyDef.put(4, new Family(4, "Pit Spawns", 5));
    familyDef.put(5, new Family(5, "Snow Beasts", 4));
    familyDef.put(6, new Family(6, "Sky Roamers", 2));
    familyDef.put(7, new Family(7, "Ocean Monsters", 0));
    familyDef.put(8, new Family(8, "Practice Targets", 0));

    // LOAD AREAEFFECT INFO
    areaEffectsDef = new HashMap<>();
    for (File f : new File(ServerSettings.PATH).listFiles((dir, name) -> name.startsWith("areas_"))) {
      Builder.load(f.getPath(), AreaEffectBuilder.class, areaEffectsDef);
    }

    // LOAD QUEST INFO
    questDef = new HashMap<>();
    for (File f : new File(ServerSettings.PATH).listFiles((dir, name) -> name.startsWith("quests_"))) {
      Builder.load(f.getPath(), QuestBuilder.class, questDef);
    }
    questNpc = QuestBuilder.mapNpc(questDef);

    // LOAD SHOP INFO
    shopDef = new HashMap<>();
    for (File f : new File(ServerSettings.PATH).listFiles((dir, name) -> name.startsWith("shop"))) {
      Builder.load(f.getPath(), ShopBuilder.class, shopDef);
    }

    // LOAD MONSTERS INFO
    creatureDef = new HashMap<Integer, Npc>();

    ResultSet monsterInfo = Server.gameDB.askDB("select Id from creature");

    try {
      while (monsterInfo.next()) {
        Npc newMob = new Npc(monsterInfo.getInt("Id"), 0, 0, 0);
        creatureDef.put(monsterInfo.getInt("Id"), newMob);
      }
      monsterInfo.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
