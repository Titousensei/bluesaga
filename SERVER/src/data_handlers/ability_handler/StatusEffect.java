package data_handlers.ability_handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.newdawn.slick.Color;

import components.Stats;

import creature.Creature;
import network.Server;

public class StatusEffect {

  private int Id;
  private String Name;
  private Stats StatsModif;
  private int Duration; // Duration in seconds
  private int RepeatDamage;
  private String RepeatDamageType;

  private int GraphicsNr;
  private int AnimationId;

  private String Sfx;

  private Ability ability;

  private Creature Caster;

  private Color SEColor;
  private int classId;

  private int ActiveTimeEnd;

/*
CREATE TABLE "ability_statuseffect" (
  "Id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,
  "Name" VARCHAR,
  "StatsModif" VARCHAR DEFAULT None,
  "Duration" INTEGER DEFAULT 0,
  "RepeatDamage" INTEGER DEFAULT 0,
  "RepeatDamageType" VARCHAR DEFAULT None,
  "Color" VARCHAR DEFAULT '0,0,0',
  "ClassId" INTEGER DEFAULT 0,
  "GraphicsNr" INTEGER DEFAULT 0,
  "AnimationId" INTEGER DEFAULT 0,
  "Sfx" VARCHAR DEFAULT None
);

               |Duration                  |ClassId
                  |RepeatDamage             |GraphicsNr
                     |RepeatDamageType         |AnimationId
Id|Name                       |Color             |Sfx        |StatsModif
 1|Burning     |4 |2 |FIRE    |246,71,53  |0|1 |0|fire       |None
 2|Acid        |6 |1 |CHEMS   |65,245,65  |0|2 |0|acid       |None
 3|Charmed     |15|0 |None    |255,124,223|0|3 |0|None       |STRENGTH:-2
 4|Poisoned    |4 |2 |CHEMS   |230,128,250|0|4 |0|gas        |None
 5|Poisoned    |8 |2 |CHEMS   |230,128,250|0|4 |0|gas        |None
 6|Frozen      |4 |2 |COLD    |164,201,255|0|6 |0|frozen     |SPEED:-25
 7|Shocked     |4 |2 |SHOCK   |255,250,111|0|7 |0|electric   |SPEED:-20
 8|Rooted      |3 |0 |STRIKE  |168,149,94 |0|8 |0|plants     |SPEED:-200
 9|Attack Speed Boost|6|0|None|168,149,94 |0|9 |0|time1      |ATTACKSPEED:200
10|Haste       |15|0 |None    |195,90,234 |0|10|0|time1      |SPEED:200
11|Block       |10|0 |None    |195,90,234 |0|11|0|lock       |ARMOR:100
12|Faded       |20|0 |None    |93,94,91   |0|0 |0|None       |EVASION:40
13|Invisibility|60|0 |None    |93,94,91   |0|0 |0|None       |None
14|Play Music  |1 |0 |None    |0,0,0      |0|17|0|None       |None
15|Fishing     |2 |0 |None    |255,255,255|0|18|0|None       |None
16|Spikes      |4 |20|PIERCE  |114,195,135|0|20|0|None       |None
20|Frozen      |4 |5 |COLD    |164,201,255|0|6 |0|frozen     |SPEED:-35
21|Frozen      |4 |4 |COLD    |164,201,255|0|6 |0|frozen     |SPEED:-60
22|Burning     |4 |4 |FIRE    |246,71,53  |0|1 |0|fire       |None
23|Shocked     |2 |7 |SHOCK   |255,250,111|0|7 |0|electric   |SPEED:-30
24|Shocked     |2 |7 |SHOCK   |255,250,111|0|7 |0|electric   |SPEED:-30
25|Mana Shield |90|0 |None    |126,181,255|0|12|0|None       |None
26|Ink Splatter|18|0 |None    |183,228,104|0|13|0|slime      |None
27|Dizzy       |20|0 |None    |183,228,104|0|14|0|stopdizzy  |None
28|Fishing     |2 |0 |None    |255,255,255|0|18|0|drop       |None
29|Snow        |8 |0 |None    |255,255,255|0|19|0|slime      |None
30|Explosion   |2 |0 |None    |246,71,53  |0|21|0|explosion  |None
31|Necro Fire  |8 |6 |CHEMS   |59,158,134 |0|5 |0|fire       |None
32|Pierce Armor|10|0 |None    |129,172,79 |0|22|0|lock       |None
33|Fire Shield |20|0 |None    |230,82,62  |0|23|0|fire       |ARMOR:50
34|Ice Shield  |20|0 |None    |164,201,255|0|24|0|frozen     |ARMOR:80
35|Rage        |6 |0 |None    |200,0,0    |0|0 |9|rage       |ATTACKSPEED:50;STRENGTH:40
36|Rage Oozemaw|6 |0 |None    |200,0,0    |0|0 |9|rage       |ARMOR:200;STRENGTH:300
37|Oozemaw Powerstrike|2|0|Non|246,71,53  |0|26|0|powerstrike|None
38|Protector   |20|0 |None    |0,0,0      |0|27|0|None       |None
39|Lockdown    |20|0 |None    |195,90,234 |0|11|0|lock       |ARMOR:1000;SPEED:-999;ATTACKSPEED:-50
40|Nature Blend|60|0 |None    |201,255,182|0|0 |0|None       |None
41|Wooden Stub |20|0 |None    |201,255,182|0|28|0|plants     |None
42|Flash Step  |1 |0 |None    |255,250,111|0|0 |0|None       |None
43|Ice Spikes  |4 |30|PIERCE  |114,195,135|0|25|0|icespikes  |None

100|Nimt       |60|0 |None    |195,234,90 |0|17|0|time1      |STRENGTH:+2;MAX_HEALTH:-20

*/

  public StatusEffect(int newId) {
    setId(newId);

    ability = null;

    Caster = null;
    // Only for testing with ItemBuilder
    if (Server.gameDB==null) { return; }
    ResultSet rs = Server.gameDB.askDB("select * from ability_statuseffect where Id = " + newId);

    try {
      if (rs.next()) {
        setName(rs.getString("Name").intern());
        StatsModif = new Stats();
        StatsModif.reset();
        if (!rs.getString("StatsModif").equals("None")) {
          String allStatsModifInfo[] = rs.getString("StatsModif").split(";");
          for (String statsModifInfo : allStatsModifInfo) {
            String statsModifSplit[] = statsModifInfo.split(":");
            String statsType = statsModifSplit[0];
            int statsEffect = Integer.parseInt(statsModifSplit[1]);
            StatsModif.setValue(statsType, statsEffect);
          }
        }

        setGraphicsNr(rs.getInt("GraphicsNr"));
        setAnimationId(rs.getInt("AnimationId"));

        setDuration(rs.getInt("Duration"));
        setRepeatDamage(rs.getInt("RepeatDamage"));
        setRepeatDamageType(rs.getString("RepeatDamageType").intern());
        String colorInfo[] = rs.getString("Color").split(",");
        SEColor =
            new Color(
                Integer.parseInt(colorInfo[0]),
                Integer.parseInt(colorInfo[1]),
                Integer.parseInt(colorInfo[2]));
        setClassId(rs.getInt("ClassId"));
        setSfx(rs.getString("Sfx"));
      }
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    ActiveTimeEnd = Duration;
  }

  public void start() {
    ActiveTimeEnd = Duration;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public int getDuration() {
    return Duration;
  }

  public void setDuration(int duration) {
    Duration = duration;
  }

  public int getRepeatDamage() {
    return RepeatDamage;
  }

  public void setRepeatDamage(int repeatDamage) {
    RepeatDamage = repeatDamage;
  }

  public String getRepeatDamageType() {
    return RepeatDamageType;
  }

  public void setRepeatDamageType(String repeatDamageType) {
    RepeatDamageType = repeatDamageType;
  }

  public Stats getStatsModif() {
    return StatsModif;
  }

  public int getId() {
    return Id;
  }

  public void setId(int id) {
    Id = id;
  }

  public boolean isActive() {
    if (ActiveTimeEnd > 0) {
      -- ActiveTimeEnd;
      return true;
    }
    return false;
  }

  public void deactivate() {
    ActiveTimeEnd = 0;
  }

  public Creature getCaster() {
    return Caster;
  }

  public void setCaster(Creature caster) {
    Caster = caster;
  }

  public Color getColor() {
    return SEColor;
  }

  public int getSkillId() {
    return classId;
  }

  public void setClassId(int classId) {
    this.classId = classId;
  }

  public int getGraphicsNr() {
    return GraphicsNr;
  }

  public void setGraphicsNr(int graphicsNr) {
    GraphicsNr = graphicsNr;
  }

  public int getAnimationId() {
    return AnimationId;
  }

  public void setAnimationId(int animationId) {
    AnimationId = animationId;
  }

  public String getSfx() {
    return Sfx;
  }

  public void setSfx(String sfx) {
    Sfx = sfx;
  }

  public Ability getAbility() {
    return ability;
  }

  public void setAbility(Ability ability) {
    this.ability = ability;
  }
}
