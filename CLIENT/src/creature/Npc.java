package creature;

import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import abilitysystem.Ability;
import components.Item;

public class Npc extends Creature {

  private Timer respawnTimer;

  private boolean showHealth;

  private int OtherPlayerId;
  private String OtherPlayerName;
  private int OtherPlayerBounty;
  private int OtherPlayerLightIndex;
  private boolean OtherPlayerKiller;

  private int
      AggroType; // 0 = not moving and no aggro, 1 = moving and no aggro, 2 = moving and aggro, 3 = npc quest/shop
  private boolean Aggro;
  private int aggroBubbleItr = 0; // used to show aggro bubble for a time

  private Color specialColor;

  private boolean logout;

  private ArrayList<Item> Loot;

  private boolean Boss;
  private boolean epic = false;

  public Npc(int creatureId, int newX, int newY, int newZ) {
    super(newX, newY, newZ);

    setType(creatureId);

    showHealth = false;
    OtherPlayerId = 0; // 0 = no player
    OtherPlayerBounty = 0;
    OtherPlayerLightIndex = 0;
    OtherPlayerKiller = false;
    logout = false;

    respawnTimer = new Timer();
  }

  public void draw(Graphics g, int centerX, int centerY) {
    sizeWidthF = SizeWidth;
    sizeHeightF = SizeHeight;

    float cXf = centerX - (sizeWidthF) * 25.0f;
    float cYf = centerY - (sizeHeightF) * 25.0f;

    int cornerX = Math.round(cXf);
    int cornerY = Math.round(cYf);

    // Gloomy outline
    if (specialColor!=null && !isEpic() && !Dead) {
      ImageResource.getSprite("effects/fx_special_glow")
          .draw(
              cornerX - 12 - ((SizeWidth - 1) * 20),
              cornerY - 12 - ((SizeHeight - 1) * 35),
              Math.round(sizeWidthF * 75),
              Math.round(sizeHeightF * 75),
              specialColor);
    }

    super.draw(g, centerX, centerY, specialColor);

    // Draw name & crewname
    if (!Dead && !hasStatusEffect(13) && isAggro()) {

      if (aggroBubbleItr > 0) {
        aggroBubbleItr--;
        ImageResource.getSprite("gui/battle/turn_aggro").drawCentered(centerX, cornerY - 26);
      } else {
        g.setFont(Font.size10);

        int nameWidth = Font.size10.getWidth(Name);
        int nameX = centerX - nameWidth / 2;

        g.setColor(BlueSagaColors.BLACK_TRANS);

        g.drawString(Name, nameX, cornerY - 25);

        if (getHealthStatus() == 4) {
          g.setColor(BlueSagaColors.WHITE);
        } else if (getHealthStatus() == 3) {
          g.setColor(BlueSagaColors.YELLOW2);
        } else if (getHealthStatus() == 2) {
          g.setColor(BlueSagaColors.ORANGE);
        } else {
          g.setColor(BlueSagaColors.RED);
        }
        g.drawString(Name, nameX, cornerY - 26);
      }
    }

    if (!Dead && !isAggro()) {
      switch (getAggroType()) {
      case 3:
        talkBubble.drawCentered(centerX - 20, cornerY - 26);
        break;
      case 13:
        newquestBubble.drawCentered(centerX - 20, cornerY - 26);
        break;
      case 14:
        completequestBubble.drawCentered(centerX - 20, cornerY - 26);
        break;
      }
    }
  }

  public void generateStats() {
    Health = getTotalStat("MAX_HEALTH");
    Mana = getTotalStat("MAX_MANA");
  }

  public Ability useRandomAbility() {
    /*
    ArrayList<AbilityOrb> ActiveAbilities = new ArrayList<AbilityOrb>();

    for(int i = 0; i < nrAbilities; i++){
      if(!Abilities.get(i).isPassive()){
        ActiveAbilities.add(Abilities.get(i));
      }
    }

    if(ActiveAbilities.size() > 0){
      int random = randomGenerator.nextInt() % ActiveAbilities.size();
      return ActiveAbilities.get(random);
    }
     */
    return null;
  }

  public ArrayList<Item> getLoot() {
    return Loot;
  }

  public void respawn(int SpawnX, int SpawnY) {
    X = SpawnX;
    Y = SpawnY;
    hidden = false;
    ATTACK_MODE = false;
    ATTACKED = false;
    Dead = false;

    hidden = false;
    MyEquipHandler.setHideEquipment(false);

    useAbilityAnimate = false;
    animationDamage = false;
    IsCriticalHit = false;

    Health = Stats.getValue("MAX_HEALTH");
    Mana = Stats.getValue("MAX_MANA");

    STATUS = "IDLE";
    animation = deathAnimation;
    respawnTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            animation = frontAnimation;
          }
        },
        1000);
  }

  public void login() {
    MyEquipHandler.setHideEquipment(true);
    animation = deathAnimation;
    logout = false;
    Dead = false;

    respawnTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            MyEquipHandler.setHideEquipment(false);
            animation = frontAnimation;
          }
        },
        1000);
  }

  public void logout() {
    MyEquipHandler.setHideEquipment(true);
    animation = deathAnimation;

    respawnTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            logout = true;
            Dead = true;
          }
        },
        1000);
  }

  public boolean getLogout() {
    return logout;
  }

  public void setOtherPlayerId(int newId) {
    OtherPlayerId = newId;
  }

  public int getOtherPlayerId() {
    return OtherPlayerId;
  }

  public void setOtherPlayerName(String newName) {
    OtherPlayerName = newName;
  }

  public String getOtherPlayerName() {
    return OtherPlayerName;
  }

  public void setOtherPlayerBounty(int bounty) {
    OtherPlayerBounty = bounty;
  }

  public void setOtherPlayerKiller(boolean killer) {
    OtherPlayerKiller = killer;
  }

  public boolean getOtherPlayerKiller() {
    return OtherPlayerKiller;
  }

  public int getOtherPlayerBounty() {
    return OtherPlayerBounty;
  }

  public void setOtherPlayerLightIndex(int newIndex) {
    OtherPlayerLightIndex = newIndex;
  }

  public int getOtherPlayerLightIndex() {
    return OtherPlayerLightIndex;
  }

  public void setShowHealth(boolean newShowHealthStatus) {
    showHealth = newShowHealthStatus;
  }

  public boolean getShowHealth() {
    return showHealth;
  }

  public int getAggroType() {
    return AggroType;
  }

  public void setAggroType(int newType) {
    AggroType = newType;
  }

  public boolean isAggro() {
    return Aggro;
  }

  public void setAggro(boolean aggroState) {
    Aggro = aggroState;

    // If Aggro then show aggro bubble
    if (Aggro) {
      aggroBubbleItr = 60;
    }
  }

  public void setSpecialColor(int type, int r, int g, int b) {
    specialColor = (type>0) ? new Color(r, g, b, 150) : null;
  }

  public void die() {
    super.die();
    Aggro = false;
  }

  public boolean isBoss() {
    return Boss;
  }

  public boolean isEpic() {
    return epic;
  }

  public void setEpic(boolean epic) {
    this.epic = epic;
  }
}
