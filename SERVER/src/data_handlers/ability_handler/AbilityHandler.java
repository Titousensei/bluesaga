package data_handlers.ability_handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import utils.GameInfo;
import utils.ServerGameInfo;
import utils.ServerMessage;
import utils.MathUtils;
import utils.RandomUtils;
import utils.TextFormater;
import components.Projectile;
import creature.Creature;
import creature.Npc;
import creature.PlayerCharacter;
import creature.Creature.CreatureType;
import data_handlers.ClassHandler;
import data_handlers.DataHandlers;
import data_handlers.FishingHandler;
import data_handlers.Handler;
import data_handlers.Message;
import data_handlers.WalkHandler;
import data_handlers.battle_handler.BattleHandler;
import data_handlers.battle_handler.DamageCalculator;
import data_handlers.battle_handler.DamageCalculator.HitResult;
import data_handlers.battle_handler.HitHandler;
import data_handlers.item_handler.Item;
import data_handlers.monster_handler.MonsterHandler;
import map.Tile;
import network.Client;
import network.Server;
import player_classes.BaseClass;

public class AbilityHandler extends Handler {

  private static ArrayList<Projectile> projectiles;

  private static Vector<AbilityEvent> AbilityEvents;

  public static void init() {
    projectiles = new ArrayList<Projectile>();
    AbilityEvents = new Vector<AbilityEvent>();

    DataHandlers.register("abilitydata", m -> handleAbilityData(m));
    DataHandlers.register("use_ability", m -> handleUseAbility(m));
    DataHandlers.register("ability_info", m -> handleAbilityInfo(m));
  }

  public static void handleAbilityData(Message m) {
    Client client = m.client;
    // 1/ means load more data when done
    if (client.playerCharacter != null) {
      addOutGoingMessage(
          client, "abilitydata", "1/" + client.playerCharacter.getAbilitiesAsString());
    }
  }

  public static void handleUseAbility(Message m) {
    Client client = m.client;
    String actionInfo[] = m.message.split(",");
    int goalX = Integer.parseInt(actionInfo[0]);
    int goalY = Integer.parseInt(actionInfo[1]);
    int goalZ = client.playerCharacter.getZ();

    int abilityId = Integer.parseInt(actionInfo[2]);

    Ability ABILITY = client.playerCharacter.getAbilityById(abilityId);
    if (ABILITY != null) {
      playerUseAbility(client, ABILITY, goalX, goalY, goalZ);
    }
  }

  public static void handleAbilityInfo(Message m) {
    Client client = m.client;
    String info[] = m.message.split(";");

    String infoType = info[0];
    int abilityId = Integer.parseInt(info[1]);

    getAbilityInfo(client, infoType, abilityId);
  }

  public static void updateAbilityEvents() {
    for (Iterator<AbilityEvent> iter = AbilityEvents.iterator(); iter.hasNext(); ) {
      AbilityEvent e = iter.next();

      if (e.checkReady()) {
        e.perform();
        iter.remove();
      }
    }
  }

  public static void updateProjectiles() {

    // UPDATE PROJECTILES
    synchronized (projectiles) {
      for (Iterator<Projectile> iter = projectiles.iterator(); iter.hasNext(); ) {
        Projectile p = iter.next();

        if (p.getActive()) {
          p.updatePos();
        }

        if (!p.getActive()) {

          int HitX = p.getTileX();
          int HitY = p.getTileY();
          int HitZ = p.getGoalZ();
          Ability ABILITY = p.getAbility();

          // SET ABILITY EFFECT ON TILES (AoE)
          abilityEffect(ABILITY, HitX, HitY, HitZ);

          iter.remove();
        }
      }
    }
  }

  public static void updatePlayerCasting() {
    for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client client = entry.getValue();
      if (client.Ready) {
        if (client.playerCharacter.isCastingSpellItr > 0) {
          client.playerCharacter.isCastingSpellItr -= 50;
          if (client.playerCharacter.isCastingSpellItr < 0) {
            client.playerCharacter.isCastingSpellItr = 0;
          }
        }
      }
    }
  }

  public static Ability getAbility(int abilityId) {
    if (ServerGameInfo.abilityDef.containsKey(abilityId)) {
      Ability ability = new Ability(ServerGameInfo.abilityDef.get(abilityId));
      return ability;
    }
    return null;
  }

  public static void getAbilityInfo(Client client, String infoType, int abilityId) {

    // CHECK IF PLAYER HAS ITEM

    if (ServerGameInfo.abilityDef.containsKey(abilityId)) {
      Ability abilityInfo = ServerGameInfo.abilityDef.get(abilityId);
      StringBuilder infoToSend = new StringBuilder(1000);
      infoToSend
          .append(infoType)
          .append("#255,234,116;")
          .append(abilityInfo.name)
          .append("/0,0,0; /255,255,255;")
          .append(abilityInfo.getDescription())
          .append("/0,0,0; /");

      if (abilityInfo.getDamage() > 0) {
        TextFormater.formatInfo(infoToSend, "Damage: " + abilityInfo.getDamage());
      }

      if (abilityInfo.getStatusEffects().size() > 0) {

        TextFormater.formatInfo(infoToSend, "Status Effects: ");
        for (StatusEffect se : abilityInfo.getStatusEffects()) {
          TextFormater.formatInfo(infoToSend, se.name);
        }
      }

      infoToSend.append("0,0,0; /");

      if (abilityInfo.isInstant()) {
        TextFormater.formatInfo(infoToSend, "Type: Instant");
      } else {
        TextFormater.formatInfo(infoToSend, "Type: Projectile");
      }

      infoToSend.append("0,0,0; /");

      if (abilityInfo.getClassId() > 0) {
        if (ServerGameInfo.classDef.containsKey(abilityInfo.getClassId())) {

          boolean hasClass = false;

          if (client.playerCharacter.hasClass(abilityInfo.getClassId())) {
            hasClass = true;
          }

          TextFormater.formatConditionInfo(
              infoToSend,
              "Req Class: " + ServerGameInfo.classDef.get(abilityInfo.getClassId()).name,
              hasClass);

          if (hasClass) {
            BaseClass cl = client.playerCharacter.getClassById(abilityInfo.getClassId());
            if (cl != null) {
              int lvl = cl.gainsXP() ? cl.level : client.playerCharacter.getLevel();
              TextFormater.formatReqInfo(
                  infoToSend,
                  "Req Class Lvl: ",
                  abilityInfo.getClassLevel(),
                  lvl);
            }
          }
        }
      }
      if (abilityInfo.getFamilyId() > 0) {
        if (ServerGameInfo.familyDef.containsKey(abilityInfo.getFamilyId())) {
          boolean hasFamily = false;
          if (abilityInfo.getFamilyId() == client.playerCharacter.getFamilyId()) {
            hasFamily = true;
          }
          TextFormater.formatConditionInfo(
              infoToSend,
              "Req Family: " + ServerGameInfo.familyDef.get(abilityInfo.getFamilyId()).getName(),
              hasFamily);
        }
      }

      infoToSend.append("0,0,0; /");

      TextFormater.formatInfo(infoToSend, "Mana cost: " + abilityInfo.getManaCost());
      TextFormater.formatInfo(infoToSend, "Cooldown: " + abilityInfo.getCooldown() + " sec");

      infoToSend.append("0,0,0; /");

      if (infoType.equals("shop")) {
        infoToSend.append("0,0,0; /");
        boolean canAfford = client.playerCharacter.hasCopper(abilityInfo.getPrice());
        TextFormater.formatPriceInfo(infoToSend, "Price: ", abilityInfo.getPrice(), canAfford);
      }

      addOutGoingMessage(client, "ability_info", infoToSend.toString());
    }
  }

  public static void playerUseAbility(
      Client client, Ability ABILITY, int goalX, int goalY, int goalZ) {

    // Check that player is not casting an ability already
    if (client.playerCharacter.isCastingSpellItr > 0) {
      return;
    }

    boolean canUse = true;
    ABILITY.setCaster(client.playerCharacter);
    if (ABILITY.getEquipReq() != null) {

      String equipRequired[] = ABILITY.getEquipReq().split(";");

      canUse = false;
      for (int i = 0; i < equipRequired.length; i++) {
        String equipInfo[] = equipRequired[i].split(":");
        String equipType = equipInfo[0];

        boolean onlyClass = false;
        int classId = 0;
        if (equipInfo[1].contains("only")) {
          onlyClass = true;
          equipInfo[1] = equipInfo[1].substring(4);
        }

        classId = Integer.parseInt(equipInfo[1]);

        if (client.playerCharacter.getEquipment(equipType) != null) {
          int weaponClassId = client.playerCharacter.getEquipment(equipType).getClassId();
          // TODO: Exception for fishing, make this better
          if (classId == 101
              && (client.playerCharacter.getEquipment(equipType).getId() == 173
                  || client.playerCharacter.getEquipment(equipType).getId() == 193)) {
            canUse = true;
          } else if (classId > 0) {
            int weaponBaseClassId = 0;
            if (GameInfo.classDef.get(weaponClassId) != null) {
              weaponBaseClassId = GameInfo.classDef.get(weaponClassId).baseClassId;
            }
            if (weaponClassId == 0
            || weaponClassId == classId
            || (!onlyClass && weaponBaseClassId == classId)
            ) {
              canUse = true;
            }
          }
        }
      }
    }

    if (!canUse) {
      return;
    }
    if (ABILITY.getManaCost() > client.playerCharacter.getMana()) {
      addOutGoingMessage(client, "message", "#messages.ability.not_enough_mana");
    } else if (!ABILITY.isReady()) {
        addOutGoingMessage(client, "message", "#messages.ability.ability_not_ready");
    } else {
      // ABILITY IS READY
      boolean haveTarget = true;

      if (ABILITY.isTargetSelf()) {
        //goalX = client.playerCharacter.getX();
        //goalY = client.playerCharacter.getY();

      } else if (ABILITY.isInstant()) {
        // GET AGGRO TARGET
        if (client.playerCharacter.getAggroTarget() != null) {
          goalX = client.playerCharacter.getAggroTarget().getX();
          goalY = client.playerCharacter.getAggroTarget().getY();
        } else {
          haveTarget = false;
        }
      }

      if (Server.WORLD_MAP.getTile(goalX, goalY, goalZ) != null) {
        boolean attackOk = true;

        attackOk =
            BattleHandler.checkAttackOk(
                client.playerCharacter.getX(),
                client.playerCharacter.getY(),
                client.playerCharacter.getZ(),
                goalX,
                goalY,
                goalZ,
                client.playerCharacter.getAttackRange() > 1); // is ranged attack

        // Flash step and wooden stub can't be cast on a tile that is not passable
        if (ABILITY.id == 84 || ABILITY.id == 83) {
          if (!Server.WORLD_MAP
              .getTile(goalX, goalY, goalZ)
              .isPassableForPlayer(client.playerCharacter)) {
            attackOk = false;
          }
        }

        // Fishing can only be done on water tiles
        if (ABILITY.id == 45) {
          if (Server.WORLD_MAP.getTile(goalX, goalY, goalZ).isWater()) {
            attackOk = true;
          } else {
            attackOk = false;
          }
        }
      if (attackOk && haveTarget) {

          int dX = goalX - client.playerCharacter.getX();
          int dY = goalY - client.playerCharacter.getY();

          double distToTarget = Math.floor(Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)));

          int abilityRange = ABILITY.getRange();

          if (ABILITY.getWeaponDamageFactor() > 0) {
            abilityRange = client.playerCharacter.getAttackRange();
          }

          if (distToTarget <= abilityRange || ABILITY.isTargetSelf()) {

            // Give Class XP
            int classId = ABILITY.getClassId();

            if (classId == 0) {
              classId = ABILITY.getJobSkillId();
            }

            ClassHandler.gainSubXP(client, classId, ABILITY.getManaCost());

            // SEND USE ABILITY TO ALL CLIENTS ON SAME MAP
            for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
              Client s = entry.getValue();

              if (s.Ready
              && isVisibleForPlayer(
                  s.playerCharacter,
                  client.playerCharacter.getX(),
                  client.playerCharacter.getY(),
                  client.playerCharacter.getZ())
              ) {
                addOutGoingMessage(
                    s,
                    "use_ability",
                    client.playerCharacter.getSmallData()
                        + ';'
                        + ABILITY.id
                        + ','
                        + ABILITY.getColor().getRed()
                        + ','
                        + ABILITY.getColor().getGreen()
                        + ','
                        + ABILITY.getColor().getBlue()
                        + ','
                        + ABILITY.getGraphicsNr()
                        + ','
                        + ABILITY.getAnimationId()
                        + ','
                        + client.playerCharacter.getAttackSpeed());
              }
            }

            boolean useMana = true;
            // Flash step
            if (ABILITY.id == 84) {
              StatusEffectHandler.addStatusEffect(
                  client.playerCharacter, ServerGameInfo.newStatusEffect(42));
            }
            else if (ABILITY.id == 29) { // Block
              Item offhand = client.playerCharacter.getEquipment("OffHand");
              int def = offhand.getStatValue("ARMOR");
              StatusEffect se = ServerGameInfo.newStatusEffect(11);
              se.setAbility(ABILITY);
              se.setCaster(client.playerCharacter);
              se.getStatsModif().setValue("ARMOR", def);
              StatusEffectHandler.addStatusEffect(
                  client.playerCharacter, se);
            }
            else if (ABILITY.id == 96) { // Mana Armor
              int def = (int) Math.round(
                    2 * Math.sqrt(
                          client.playerCharacter.getBaseClass().level));
              StatusEffect se = ServerGameInfo.newStatusEffect(47);
              se.setAbility(ABILITY);
              se.setCaster(client.playerCharacter);
              se.getStatsModif().setValue("ARMOR", def);
              se.getStatsModif().setValue("MAGIC_DEF", def);
              se.getStatsModif().setValue("SPEED", -5);
              StatusEffectHandler.addStatusEffect(
                  client.playerCharacter, se);
            }
            else if (ABILITY.id == 27) {
              // TAUNT ABILITY
              // MAKE ALL MONSTERS ATTACK PLAYER
              MonsterHandler.alertNearMonsters(
                  client.playerCharacter, goalX, goalY, goalZ, true);

            } else if (ABILITY.id == 31
                && client.playerCharacter.getPkMarker() == 0) {
              // SOUL STONE
              // TELEPORT PLAYER BACK TO INN

              Server.WORLD_MAP
                  .getTile(
                      client.playerCharacter.getX(),
                      client.playerCharacter.getY(),
                      client.playerCharacter.getZ())
                  .setOccupant(CreatureType.None, null);

              for (Entry<Integer, Client> entry : Server.clients.entrySet()) {
                Client other = entry.getValue();
                if (other.Ready
                    && other.playerCharacter.getDBId() != client.playerCharacter.getDBId()
                    && isVisibleForPlayer(
                        other.playerCharacter,
                        client.playerCharacter.getX(),
                        client.playerCharacter.getY(),
                        client.playerCharacter.getZ())) {
                  addOutGoingMessage(
                      other, "creature_remove", client.playerCharacter.getSmallData());
                }
              }

              BattleHandler.respawnPlayer(client.playerCharacter);
              if (client.playerCharacter.getShip() != null) {
                client.playerCharacter.getShip().setShow(false);
              }

              Server.WORLD_MAP
                  .getTile(
                      client.playerCharacter.getX(),
                      client.playerCharacter.getY(),
                      client.playerCharacter.getZ())
                  .setOccupant(CreatureType.Player, client.playerCharacter);

              addOutGoingMessage(
                  client,
                  "respawn",
                  client.playerCharacter.getX()
                      + ","
                      + client.playerCharacter.getY()
                      + ','
                      + client.playerCharacter.getZ());

            } else {
              useMana = false;
              int castingSpeed = ABILITY.getCastingSpeed();
              addAbilityEvent(
                  ABILITY, client.playerCharacter, goalX, goalY, goalZ, castingSpeed);
            }

            if (useMana) {
              client.playerCharacter.useAbility(ABILITY);
              addOutGoingMessage(client, "stat", "Mana;" + client.playerCharacter.getMana());
              ServerMessage.println(false, "UseAbility - ", client.playerCharacter,
                  ": ", ABILITY);
            }

            if (client.playerCharacter != null) {
              client.playerCharacter.saveInfo();
            }
          }
        } else {
          addOutGoingMessage(client, "message", "#messages.ability.cant_perform_action");
        }
      }
    }
  }

  public static boolean monsterUseAbility(Creature MONSTER, Ability ABILITY, Creature Target) {

    boolean useAbility = false;

    int goalX = Target.getX();
    int goalY = Target.getY();

    // ENOUGH MANA TO USE
    if (ABILITY.getManaCost() <= MONSTER.getMana()) {

      // ABILITY IS READY
      if (ABILITY.isReady()) {

        useAbility = true;

        ABILITY.setCaster(CreatureType.Monster, MONSTER);

        if (ABILITY.isTargetSelf()) {
          goalX = MONSTER.getX();
          goalY = MONSTER.getY();
        } else {
          // CHECK IF ABILITY WILL HIT CASTER
          String TileEffects[] = ABILITY.getAoE().split(";");

          for (String tileXY : TileEffects) {
            String tileCoord[] = tileXY.split(",");
            int dX = Integer.parseInt(tileCoord[0]);
            int dY = Integer.parseInt(tileCoord[1]);

            int tileX = goalX + dX;
            int tileY = goalY + dY;

            if (tileX == MONSTER.getX() && tileY == MONSTER.getY()) {
              useAbility = false;
              break;
            }
          }
        }

        if (useAbility) {
          int dX = goalX - MONSTER.getX();
          int dY = goalY - MONSTER.getY();

          double distToTarget = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));

          if (distToTarget <= ABILITY.getRange()) {

            MONSTER.useAbility(ABILITY);

            // SEND USE ABILITY TO ALL CLIENTS ON SAME MAP
            for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
              Client s = entry.getValue();

              if (s.Ready
                  && isVisibleForPlayer(
                      s.playerCharacter, MONSTER.getX(), MONSTER.getY(), MONSTER.getZ())) {
                addOutGoingMessage(
                    s,
                    "use_ability",
                    MONSTER.getSmallData()
                        + ';'
                        + ABILITY.id
                        + ','
                        + ABILITY.getColor().getRed()
                        + ','
                        + ABILITY.getColor().getGreen()
                        + ','
                        + ABILITY.getColor().getBlue()
                        + ','
                        + ABILITY.getGraphicsNr()
                        + ','
                        + ABILITY.getAnimationId()
                        + ','
                        + MONSTER.getAttackSpeed());
              }
            }

            int castingDuration = ABILITY.getCastingSpeed();

            addAbilityEvent(ABILITY, MONSTER, goalX, goalY, MONSTER.getZ(), castingDuration);
          }
        }
      }
    }

    return useAbility;
  }

  public static void addAbilityEvent(
      Ability ABILITY, Creature CASTER, int goalX, int goalY, int goalZ, int delay) {
    AbilityEvents.add(new AbilityEvent(ABILITY, CASTER, goalX, goalY, goalZ, delay));
  }

  public static void addProjectile(
      Ability ABILITY, int startX, int startY, int startZ, int goalX, int goalY, int goalZ) {
    // CHECK IF ANY NON PASSABLE TILES IN THE WAY OF THE PROJECTILE
    //String goal_info[] = Server.WORLD_MAP.checkProjectileObstacles(startX, startY,startZ, goalX,goalY, goalZ).split(",");

    //goalX = Integer.parseInt(goal_info[0]);
    //goalY = Integer.parseInt(goal_info[1]);

    Projectile newProjectile = new Projectile(ABILITY, startX, startY, startZ, goalX, goalY, goalZ);

    projectiles.add(newProjectile);

    // Arrow Storm
    if (ABILITY.id == 74) {

      int goal2X = goalX;
      int goal2Y = goalY;

      int goal3X = goalX;
      int goal3Y = goalY;

      if (ABILITY.getCaster().getGotoRotation() < 30) {
        goal2X = goalX - 1;
        goal3X = goalX + 1;
      } else if (ABILITY.getCaster().getGotoRotation() < 60) {
        goal2X = goalX - 1;
        goal3Y = goalY + 1;
      } else if (ABILITY.getCaster().getGotoRotation() < 105) {
        goal2Y = goalY - 1;
        goal3Y = goalY + 1;
      } else if (ABILITY.getCaster().getGotoRotation() < 150) {
        goal2Y = goalY - 1;
        goal3X = goalX - 1;
      } else if (ABILITY.getCaster().getGotoRotation() < 195) {
        goal2X = goalX - 1;
        goal3X = goalX + 1;
      } else if (ABILITY.getCaster().getGotoRotation() < 240) {
        goal2Y = goalY - 1;
        goal3X = goalX + 1;
      } else if (ABILITY.getCaster().getGotoRotation() < 285) {
        goal2Y = goalY - 1;
        goal3Y = goalY + 1;
      } else if (ABILITY.getCaster().getGotoRotation() >= 285) {
        goal2X = goalX + 1;
        goal3Y = goalY + 1;
      }

      Projectile newProjectile2 =
          new Projectile(ABILITY, startX, startY, startZ, goal2X, goal2Y, goalZ);
      Projectile newProjectile3 =
          new Projectile(ABILITY, startX, startY, startZ, goal3X, goal3Y, goalZ);

      projectiles.add(newProjectile2);
      projectiles.add(newProjectile3);

      for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
        Client s = entry.getValue();

        if (s.Ready && isVisibleForPlayer(s.playerCharacter, startX, startY, startZ)) {
          addOutGoingMessage(
              s,
              "projectile",
              ABILITY.getProjectileId()
                  + ","
                  + startX
                  + ','
                  + startY
                  + ','
                  + startZ
                  + ','
                  + newProjectile2.getGoalX()
                  + ','
                  + newProjectile2.getGoalY()
                  + ','
                  + ABILITY.getDelay()
                  + ",1,0");
          addOutGoingMessage(
              s,
              "projectile",
              ABILITY.getProjectileId()
                  + ","
                  + startX
                  + ','
                  + startY
                  + ','
                  + startZ
                  + ','
                  + newProjectile3.getGoalX()
                  + ','
                  + newProjectile3.getGoalY()
                  + ','
                  + ABILITY.getDelay()
                  + ",1,0");
        }
      }
    }

    goalX = newProjectile.getGoalX();
    goalY = newProjectile.getGoalY();

    // SEND USE ABILITY TO ALL PLAYERS IN THE AREA
    // CasterType; CasterId; GoalX;GoalY;AbilityId
    for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client s = entry.getValue();

      if (s.Ready && isVisibleForPlayer(s.playerCharacter, startX, startY, startZ)) {
        int projectileId = ABILITY.getProjectileId();
        float speed = 1.0f;

        if (ABILITY.getWeaponDamageFactor() > 0) {
          projectileId = 0;
          if (ABILITY.getCaster().getWeapon() != null) {
            projectileId = ABILITY.getCaster().getWeapon().getProjectileId();
          }
        }

        addOutGoingMessage(
            s,
            "projectile",
            projectileId
                + ","
                + startX
                + ','
                + startY
                + ','
                + startZ
                + ','
                + goalX
                + ','
                + goalY
                + ','
                + ABILITY.getDelay()
                + ','
                + speed
                + ','
                + ABILITY.getProjectileEffectId());
      }
    }
  }

  public static void abilityEffect(Ability ABILITY, int goalX, int goalY, int goalZ) {
    if (ABILITY != null) {
      String AoE = ABILITY.getAoE();
      String tileData = "None";
      boolean useMana = true;
      // FIND CASTER

      Creature CASTER = ABILITY.getCaster();
      boolean arena = false;

      if (CASTER != null) {
        if (Server.WORLD_MAP.getTile(CASTER.getX(), CASTER.getY(), CASTER.getZ()) != null) {
          if (Server.WORLD_MAP
              .getTile(CASTER.getX(), CASTER.getY(), CASTER.getZ())
              .getType()
              .equals("arena")) {
            arena = true;
          }
        }
      }

      if (AoE != null) {

        if (ABILITY.id == 75) {  // Slash
          if (CASTER.getGotoRotation() == 45) {
            AoE = "-1,0;0,0;0,1";
          } else if (CASTER.getGotoRotation() == 90) {
            AoE = "0,-1;0,0;0,1";
          } else if (CASTER.getGotoRotation() == 135) {
            AoE = "0,-1;0,0;-1,0";
          } else if (CASTER.getGotoRotation() == 225) {
            AoE = "0,-1;0,0;1,0";
          } else if (CASTER.getGotoRotation() == 270) {
            AoE = "0,-1;0,0;0,1";
          } else if (CASTER.getGotoRotation() == 315) {
            AoE = "0,1;0,0;1,0";
          }
        }

        else if (ABILITY.id == 76) {  // Thrust

          AoE = "";

          // Check if caster has target
          if (CASTER != null) {
            if (CASTER.getAggroTarget() != null) {
              Creature TARGET = CASTER.getAggroTarget();

              // Get direction of attack
              int dX = TARGET.getX() - CASTER.getX();
              int dY = TARGET.getY() - CASTER.getY();

              float angleNeeded = MathUtils.angleBetween(-dX, -dY);

              if (angleNeeded < 0) {
                angleNeeded = 360 + angleNeeded;
              }

              if (angleNeeded == 0) {
                dY = -1;
              } else if (angleNeeded == 45) {
                dX = 1;
                dY = -1;
              } else if (angleNeeded == 90) {
                dX = 1;
              } else if (angleNeeded == 135) {
                dX = 1;
                dY = 1;
              } else if (angleNeeded == 180) {
                dY = 1;
              } else if (angleNeeded == 225) {
                dX = -1;
                dY = 1;
              } else if (angleNeeded == 270) {
                dX = -1;
              } else if (angleNeeded == 315) {
                dX = -1;
                dY = -1;
              }

              goalX = CASTER.getX();
              goalY = CASTER.getY();

              // Loop through tiles in the direction of attacker
              // If tile has occupant go to next tile

              int moveX = dX;
              int moveY = dY;

              Tile checkTile = null;

              boolean thrustOk = true;
              int step = 1;
              boolean thrustDone = false;

              while (step < 10 && thrustOk && !thrustDone) {
                moveX = dX * step;
                moveY = dY * step;

                checkTile =
                    Server.WORLD_MAP.getTile(
                        CASTER.getX() + moveX, CASTER.getY() + moveY, CASTER.getZ());

                if (checkTile != null) {
                  if (!checkTile.isPassableType()) {
                    thrustOk = false;
                  } else {
                    if (checkTile.isPassable()) {
                      thrustDone = true;
                    }
                  }
                } else {
                  thrustOk = false;
                }
                if (thrustOk) {
                  AoE += dX * step + "," + dY * step + ";";
                }
                step++;
              }
              if (!thrustOk) {
                AoE = "";
              }

              if (!AoE.equals("")) {
                // SEND ANIMATION TO ALL CLIENT IN AREA

                float distance = (float) Math.sqrt(Math.pow(moveX, 2) + Math.pow(moveY, 2));
                int speed = (int) (distance * 1000);
                int gotoX = CASTER.getX() + moveX;
                int gotoY = CASTER.getY() + moveY;

                for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
                  Client s = entry.getValue();

                  if (s.Ready) {
                    if (isVisibleForPlayer(
                        s.playerCharacter, CASTER.getX(), CASTER.getY(), CASTER.getZ())) {
                      addOutGoingMessage(
                          s,
                          "creature_goto",
                          CASTER.getSmallData() + ';' + gotoX + ',' + gotoY + ',' + speed);
                    }
                  }
                }

                CASTER.setGotoRotation(angleNeeded);

                if (CASTER.getCreatureType() == CreatureType.Player) {
                  PlayerCharacter player = (PlayerCharacter) CASTER;
                  WalkHandler.movePlayer(player.client, gotoX, gotoY, player.getZ());
                } else {
                  Tile oldPosTile =
                      Server.WORLD_MAP.getTile(CASTER.getX(), CASTER.getY(), CASTER.getZ());

                  if (oldPosTile != null) {
                    oldPosTile.setOccupant(CreatureType.None, null);
                  }

                  CASTER.walkTo(gotoX, gotoY, CASTER.getZ());

                  Tile newPosTile =
                      Server.WORLD_MAP.getTile(CASTER.getX(), CASTER.getY(), CASTER.getZ());

                  if (newPosTile != null) {
                    newPosTile.setOccupant(CASTER.getCreatureType(), CASTER);
                  }
                }

              } else {
                // No free tile available for thrust
                // Just attack from standing spot
                goalX = TARGET.getX();
                goalY = TARGET.getY();
                AoE = "0,0";
              }
            }
          }

        } else if (ABILITY.id == 56     // DASH
        || ABILITY.id == 84             // FLASH STEP
        ) {
          boolean flashStep = (ABILITY.id == 84);

          AoE = "";

          float diffX = goalX - CASTER.getX();
          float diffY = goalY - CASTER.getY();

          int speed = Math.round((Math.abs(diffX) + Math.abs(diffY)) * 400 + 400) * 2;

          if (flashStep) {
            speed *= 2;
          }

          if (Math.abs(diffX) > Math.abs(diffY) && diffX != 0) {
            diffY = diffY / Math.abs(diffX);
            diffX = diffX / Math.abs(diffX);
          } else if (diffY != 0) {
            diffX = diffX / Math.abs(diffY);
            diffY = diffY / Math.abs(diffY);
          }

          float moveX = 0;
          float moveY = 0;

          Tile checkTile;

          int tileX = CASTER.getX();
          int tileY = CASTER.getY();

          int gotoX = tileX;
          int gotoY = tileY;

          for (int i = 1; i < 10; i++) {

            tileX = Math.round(CASTER.getX() + moveX);
            tileY = Math.round(CASTER.getY() + moveY);

            checkTile = Server.WORLD_MAP.getTile(tileX, tileY, CASTER.getZ());

            if (checkTile != null) {
              if (tileX == CASTER.getX() && tileY == CASTER.getY()) {

              } else if (!checkTile.isPassable()) {
                if (checkTile.isPassableType() && flashStep) {
                  // Can go through enemies with flashStep
                } else {
                  break;
                }
              }

            } else {
              break;
            }

            gotoX = tileX;
            gotoY = tileY;

            moveX = diffX * i;
            moveY = diffY * i;

            if (flashStep && tileX == goalX && tileY == goalY) {
              break;
            }
          }

          if (gotoX != CASTER.getX() || gotoY != CASTER.getY()) {
            for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
              Client s = entry.getValue();

              if (s.Ready
                  && isVisibleForPlayer(
                      s.playerCharacter, CASTER.getX(), CASTER.getY(), CASTER.getZ())) {
                addOutGoingMessage(
                    s,
                    "creature_goto",
                    CASTER.getSmallData() + ';' + gotoX + ',' + gotoY + ',' + speed);
              }
            }

            if (CASTER.getCreatureType() == CreatureType.Player) {
              PlayerCharacter player = (PlayerCharacter) CASTER;
              WalkHandler.movePlayer(player.client, gotoX, gotoY, player.getZ());
            } else {
              Tile oldPosTile =
                  Server.WORLD_MAP.getTile(CASTER.getX(), CASTER.getY(), CASTER.getZ());

              if (oldPosTile != null) {
                oldPosTile.setOccupant(CreatureType.None, null);
              }

              CASTER.walkTo(gotoX, gotoY, CASTER.getZ());

              Tile newPosTile =
                  Server.WORLD_MAP.getTile(CASTER.getX(), CASTER.getY(), CASTER.getZ());

              if (newPosTile != null) {
                newPosTile.setOccupant(CASTER.getCreatureType(), CASTER);
              }
            }
          }
        }

        if (Ability.TARGET_TILE.equals(ABILITY.getTargetType())) {
          tileData = ABILITY.id + ";";
        }

        // Set caster on ability and its status effects
        ABILITY.setCaster(CASTER);

        String TileEffects[] = AoE.split(";");
        if (AoE.contains(",")) {
          for (String tileXY : TileEffects) {
            String tileCoord[] = tileXY.split(",");
            int dX = Integer.parseInt(tileCoord[0]);
            int dY = Integer.parseInt(tileCoord[1]);

            int tileX = goalX + dX;
            int tileY = goalY + dY;
            int tileZ = goalZ;

            Tile TILE = Server.WORLD_MAP.getTile(tileX, tileY, tileZ);

            if (TILE != null && TILE.isPassableType()) {

              // SPAWN MONSTERS
              if (ABILITY.getSpawnIds() != null) {
                String spawnGroup = RandomUtils.getAny(ABILITY.getSpawnIds().split(";"));
                String spawnIds[] = spawnGroup.split(",");

                for (String spawnId : spawnIds) {
                  int sId = Integer.parseInt(spawnId);
                  if (sId == 0) {
                    sId = CASTER.getId();
                  }
                  // Find free spot to spawn
                  Point spawnTile = Server.WORLD_MAP.findClosestFreeTile(tileX, tileY, tileZ);
                  if (spawnTile != null) {
                    Npc m =
                        new Npc(
                            ServerGameInfo.creatureDef.get(sId), spawnTile.x, spawnTile.y, tileZ);
                    m.setAggroType(2);
                    m.setAggro(CASTER.getAggroTarget());

                    if (CASTER != null
                    && CASTER.getCreatureType() == CreatureType.Monster
                    ) {
                      Npc monsterCaster = (Npc) CASTER;
                      if (monsterCaster.isTitan() || monsterCaster.isElite()) {
                        m.setElite(true);
                      }
                    }

                    MonsterHandler.spawnMonster(m, spawnTile.x, spawnTile.y, tileZ);
                  }
                }
              } else {
                Creature TARGET = TILE.getOccupant();

                // ADD STATUSEFFECTS TO TILE
                if (Ability.TARGET_TILE.equals(ABILITY.getTargetType())) {
                  tileData += tileX + "," + tileY + "," + tileZ + ";";
                  TILE.addStatusEffects(ABILITY.getStatusEffects());
                }

                // ADD DAMAGE, ADD STATUSEFFECTS TO TARGET
                if (TARGET == null) {
                  if (ABILITY.getDamageType().equals("Healing")
                  || ABILITY.getDamageType().startsWith("Reveal")
                  ) {
                    useMana = false;
                  }
                } else {
                  // Caster on the same tile as target? Then force ability to caster.
                  if (CASTER.getX() == TARGET.getX()
                      && CASTER.getY() == TARGET.getY()
                      && CASTER.getZ() == TARGET.getZ()) {
                    TARGET = CASTER;
                  }

                  int damage = 0;

                  if (ABILITY.getDamageType().equals("Healing")) {
                    damage = ABILITY.getDamage();
                  } else if (ABILITY.getDamageType().startsWith("Reveal")) {
                    PlayerCharacter playerCaster = (PlayerCharacter) CASTER;
                    int baseClassId = GameInfo.classDef.get(ABILITY.getClassId()).baseClassId;
                    double att = CASTER.getStat("INTELLIGENCE") + playerCaster.getClassLevel(baseClassId);
                    double def = TARGET.getStat("INTELLIGENCE") + CASTER.getStat("MIND_DEF");
                    String msg = "@PUR Their Intelligence is too strong, your vision fades before you understand what it means...";
                    if (RandomUtils.sigmoidCheck(att, def)) {
                      int chance = RandomUtils.getInt(0, 75);
                      if (chance < 10) {
                        msg = "@PUR You see their Health: " + TARGET.getHealth();
                      } else if (chance < 20) {
                        msg = "@PUR You feel their Mana: " + TARGET.getMana();
                      } else if (chance < 30) {
                        msg = "@PUR You have a vision of their Armor: " + TARGET.getStat("ARMOR");
                      } else if (chance < 40) {
                        msg = "@PUR You foresee their Attack Speed: " + TARGET.getStat("ATTACKSPEED");
                      } else if (chance < 50) {
                        if (TARGET instanceof PlayerCharacter) {
                          msg = "@PUR You sense their Level: " + ((PlayerCharacter) TARGET).getLevel();
                        } else {
                          msg = "@PUR You sense the XP you can get from them: " + ((Npc) TARGET).getGiveXP();
                        }
                      } else if (chance < 60) {
                        Ability ab = RandomUtils.getAny(TARGET.getAbilities());
                        if (ab!= null) {
                          msg = "@PUR You dream of their Ability: " + ab.name;
                          if (chance < 64) {
                            msg += " (" + ab.getCooldown() + "s cooldown)";
                          } else if (chance < 68) {
                            msg += " (" + ab.getManaCost() + " MP)";
                          }
                        }
                      } else { // 70
                        msg = "@PUR You have a glimpse of the damage they can do to you: " + DamageCalculator.calculateDamage(TARGET, CASTER);
                      }
                    }
                    addOutGoingMessage(playerCaster.client, "message", msg);
                  } else if (ABILITY.getDamage() > 0 || ABILITY.getWeaponDamageFactor() > 0) {
                    damage = calculateAbilityDamage(ABILITY, TARGET);
                  }

                  // ABILITY HIT INFO

                  //String abilityHitInfo = TARGET.getSmallData()+";"+TARGET.getHealthStatus()+","+ABILITY.getAbilityId()+","+ABILITY.getDamageType()+","+damage+","+ABILITY.getColor().getRed()+","+ABILITY.getColor().getGreen()+","+ABILITY.getColor().getBlue()+","+ABILITY.getGraphicsNr();

                  if (TARGET.getCreatureType().equals(CASTER.getCreatureType())
                  && TARGET.getDBId() == CASTER.getDBId()
                  && ABILITY.isBuffOrNot()
                  ) {
                    if (ABILITY.getStatusEffects() != null) {
                      for (StatusEffect statusFX : ABILITY.getStatusEffects()) {
                        StatusEffect seFX = ServerGameInfo.newStatusEffect(statusFX.id);
                        seFX.setCaster(CASTER);
                        StatusEffectHandler.addStatusEffect(TARGET, seFX);
                      }
                    }
                  } else if (damage != 0) {
                    HitResult hit = DamageCalculator.criticalOrMiss(CASTER, TARGET);

                    if (hit == HitResult.MISSED) {
                      HitHandler.creatureGetHit(
                          TARGET, CASTER, 0, ABILITY.getDamageType(),
                          "miss", arena, null);
                    }
                    else if (hit == HitResult.EVADED) {
                      HitHandler.creatureGetHit(
                          TARGET, CASTER, 0, ABILITY.getDamageType(),
                          "evade", arena, null);
                    }
                    else if (hit == HitResult.CRITICAL) {
                      HitHandler.creatureGetHit(
                        TARGET, CASTER, damage, ABILITY.getDamageType(),
                        "true", arena, ABILITY.getStatusEffects());
                    }
                    else {
                      HitHandler.creatureGetHit(
                          TARGET, CASTER, damage, ABILITY.getDamageType(),
                          "false", arena, ABILITY.getStatusEffects());
                    }
                  }
                }
                if (TARGET != null) {
                  ServerMessage.println((CASTER.getCreatureType() != CreatureType.Player),
                      "AbilityEffect - ", CASTER, ": ", ABILITY, " on ", TARGET);
                }
              }

              // IF FISHING
              if (ABILITY.getJobSkillId() == 101
              && TILE.isWater()
              && ABILITY.getCaster().getCreatureType() == CreatureType.Player) {
                PlayerCharacter playerFisher = (PlayerCharacter) ABILITY.getCaster();

                Client playerClient = playerFisher.client;

                if (playerClient.Ready) {
                  FishingHandler.generateCatch(playerClient, tileX, tileY, tileZ);
                  ServerMessage.println(false, "AbilityFishing - ", playerFisher,
                      ": ", ABILITY);
                }
              }
            }
          }
        }
      }

      if (!tileData.equals("None")) {
        tileData += "/";
        StringBuilder seInfo = new StringBuilder();
        for (StatusEffect se : ABILITY.getStatusEffects()) {
          seInfo.append(se.id)
                .append(',')
                .append(se.getGraphicsNr())
                .append(',')
                .append(se.getSfx())
                .append(';');
        }

        if (seInfo.length() == 0) {
          tileData += "None";
        }
        else {
          tileData += seInfo;
        }

        // SEND TILE STATUSEFFECT HIT DATA
        for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
          Client s = entry.getValue();
          if (s.Ready) {
            addOutGoingMessage(s, "tile_effect", tileData);
          }
        }
        ServerMessage.println((CASTER.getCreatureType() != CreatureType.Player),
            "AbilityEffect - ", CASTER, ": ", ABILITY, " at ", goalX, ",", goalY, ",", goalZ);
      }

      if (useMana) {
        CASTER.useAbility(ABILITY);
        if (CASTER instanceof PlayerCharacter) {
          addOutGoingMessage(((PlayerCharacter) CASTER).client, "stat", "Mana;" + CASTER.getMana());
        }
      }

      // ALERT MONSTERS, EXCEPTIONS: SPAWN MAGIC, TRAPS, FISHING, REVEAL
      if (ABILITY.getCaster() != null
      && ABILITY.getCaster().getCreatureType() == CreatureType.Player
      && ABILITY.id != 45
      && ABILITY.getSpawnIds() == null
      && !ABILITY.getDamageType().startsWith("Reveal")
      ) {
        MonsterHandler.alertNearMonsters(CASTER, goalX, goalY, goalZ, false);
      }
    }
  }

  /**
   * Update ability cooldowns, health and mana regains for players and npcs
   */
  public static void updateCooldowns() {

    // Update ability cooldowns, health and mana regain for players
    for (Map.Entry<Integer, Client> entry : Server.clients.entrySet()) {
      Client s = entry.getValue();

      if (s.Ready
      && s.playerCharacter != null
      && !s.playerCharacter.isDead()) {

        // REGAIN HEALTH AND MANA
        int oldHealthStatus = s.playerCharacter.getHealthStatus();

        // FROM POTION
        s.playerCharacter.regainPotionHealth(s.playerCharacter.potionRegainHealthTick);
        s.playerCharacter.regainPotionMana(s.playerCharacter.potionRegainManaTick);

        // FROM EATING AND RESTING
        if (s.playerCharacter.regainItrUpdate()) {
          s.playerCharacter.restartRegainTimer();

          s.playerCharacter.regainHealth();
          s.playerCharacter.regainMana();
        }

        if (s.playerCharacter.pollChangedHealth()) {
          addOutGoingMessage(s, "stat", "Health;" + s.playerCharacter.getHealth());
          addOutGoingMessage(
              s,
              "stat",
              "HEALTH_REGAIN;"
                  + s.playerCharacter.getStat("HEALTH_REGAIN")
                  + ';'
                  + s.playerCharacter.getSatisfied());
        }

        if (s.playerCharacter.pollChangedMana()) {
          addOutGoingMessage(s, "stat", "Mana;" + s.playerCharacter.getMana());
          addOutGoingMessage(
              s,
              "stat",
              "MANA_REGAIN;"
                  + s.playerCharacter.getStat("MANA_REGAIN")
                  + ';'
                  + s.playerCharacter.getSatisfied());
        }

        if (s.playerCharacter.getHealthStatus() != oldHealthStatus) {
          for (Map.Entry<Integer, Client> entry2 : Server.clients.entrySet()) {
            Client other = entry2.getValue();
            if (other.Ready
            && isVisibleForPlayer(
                  other.playerCharacter,
                  s.playerCharacter.getX(),
                  s.playerCharacter.getY(),
                  s.playerCharacter.getZ())
            ) {
              addOutGoingMessage(
                  other,
                  "changehealthstatus",
                  s.playerCharacter.getSmallData()
                      + ';'
                      + s.playerCharacter.getHealthStatus());
            }
          }
        }

        // UPDATE COOLDOWNS
        for (Ability a : s.playerCharacter.getAbilities()) {
          if (a.checkReady()) {
            addOutGoingMessage(s, "ability_ready", String.valueOf(a.id));
          }
        }
      }
    }

    // Update ability cooldowns, health and mana regain for npcs
    for (Iterator<Npc> iter2 = Server.WORLD_MAP.getMonsters().values().iterator();
        iter2.hasNext();
        ) {
      Npc m = iter2.next();

      // Update Health and Mana regain
      if (m.regainItrUpdate()) {
        m.restartRegainTimer();

        m.regainMana();
        m.regainHealth();

        if (m.isResting() && m.getHealth() == m.getStat("MAX_HEALTH")) {
          MonsterHandler.changeMonsterSleepState(m, false);
        }
      }

      // Update cooldowns
      for (Ability a : m.getAbilities()) {
        a.checkReady();
      }
    }
  }

  public static int calculateAbilityDamage(Ability ABILITY, Creature TARGET) {

    Creature CASTER = ABILITY.getCaster();

    int baseClassId = 0;
    int modifLevel = 1;
    int weapon_damage = 0;
    // Get skill modifier
    if (CASTER != null) {
      // If caster is a player, check if the ability has any skill modifiers
      if (CASTER.getCreatureType() == CreatureType.Player && ABILITY.getClassId() > 0) {
        PlayerCharacter playerCaster = (PlayerCharacter) CASTER;

        baseClassId = GameInfo.classDef.get(ABILITY.getClassId()).baseClassId;
        modifLevel = playerCaster.getClassLevel(baseClassId);

        // If ability has a weapon damage factor, get the equipped weapon skill
        if (ABILITY.getWeaponDamageFactor() > 0) {
          int damage = DamageCalculator.calculateDamage(CASTER, TARGET);
          weapon_damage = Math.round(damage * ABILITY.getWeaponDamageFactor());
        }
      }
    }

    double aDmg = 0;

    if (ABILITY.getDamage() > 0) {
      if (CASTER != null) {
        if (CASTER.getCreatureType() == CreatureType.Monster) {
          aDmg = ABILITY.getDamage();
        } else {
          aDmg =
              ((CASTER.getStat("INTELLIGENCE") + 10.0f) / 10.0f)
                  * ((modifLevel + 12.0f) / 12.0f)
                  * ABILITY.getDamage()
                  / 10.0f;
        }

      } else {
        // TRAP DAMAGE
        aDmg = ABILITY.getDamage();
      }

      // Armor or resistance modifier
      double armorF = DamageCalculator.getArmorFactor(CASTER, TARGET);

      aDmg = aDmg * armorF;
    }

    int ability_dmg_max = (int) (weapon_damage + aDmg);
    int ability_dmg_min = ability_dmg_max;

    if (weapon_damage == 0) {
      ability_dmg_min = Math.round(0.75f * ability_dmg_min);
    }

    double ability_damage = 0;

    if (ability_dmg_max > 0) {
      ability_damage = RandomUtils.getInt(ability_dmg_min, ability_dmg_max);
    }

    // Half damage if PvP and not in lost archipelago
    if (CASTER != null) {
      if (TARGET.getCreatureType() == CreatureType.Player
          && CASTER.getCreatureType() == CreatureType.Player) {
        if (ABILITY.getWeaponDamageFactor() == 0 && TARGET.getZ() >= -200) {
          ability_damage = ability_damage / 2.0f;
        }
      }
    }

    return (int) ability_damage;
  }
}
