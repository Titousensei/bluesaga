package map;

import java.util.*;

import game.BlueSaga;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import screens.ScreenHandler;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import data_handlers.AreaEffectHandler;
import utils.LanguageUtils;

public class AreaEffect {
  private int Id = 0;

  // AREA NAME
  private boolean SHOW_AREA_NAME = false;
  private String AreaName = "";
  private int AreaNameAlpha = 0;
  private int AreaNameScrollWidth = 0;
  private int AreaNameTimeItr = 0;

  private int guardLevel = 0;

  private boolean Tint = false;
  private Color TintColorInit = BlueSagaColors.WHITE;
  private Color TintColor = BlueSagaColors.WHITE;
  private int TintAlpha = 0;

  private boolean Fog = false;
  private Fog FogEffect;

  private String ParticleType = "None";
  private List<EnvParticle> Particles = new ArrayList<EnvParticle>(100);

  private final int widthUnguarded;
  private final int widthSafe;


  public AreaEffect()
  {
    widthUnguarded = Font.size18.getWidth(LanguageUtils.getString("ui.area_effect.unguarded_zone"));
    widthSafe = Font.size18.getWidth(LanguageUtils.getString("ui.area_effect.safe_zone"));
  }

  public Color updatedTintColor(float alpha) {
    if (alpha > 1.0f) {
      return TintColorInit;
    } else if (alpha < 0.0f) {
      return BlueSagaColors.WHITE;
    }
    int r = Math.round(255 * (1.0f - alpha) + TintColorInit.getRed() * alpha);
    int g = Math.round(255 * (1.0f - alpha) + TintColorInit.getGreen() * alpha);
    int b = Math.round(255 * (1.0f - alpha) + TintColorInit.getBlue() * alpha);
    return new Color(r, g, b);
  }

  public void draw(Graphics g) {
    if (Tint) {
      if (TintAlpha < 255) {
        TintAlpha++;
        TintColor = updatedTintColor(TintAlpha / 255.0f);
      }
    } else {
      if (TintAlpha > 0) {
        TintAlpha--;
        TintColor = updatedTintColor(TintAlpha / 255.0f);
      }
    }

    if (FogEffect != null) {
      FogEffect.draw(ScreenHandler.myCamera.getX(), ScreenHandler.myCamera.getY());
    }

    if (SHOW_AREA_NAME) {
      AreaNameTimeItr++;
      if (AreaNameAlpha < 255) {
        AreaNameAlpha += 5;
      }
      if (AreaNameScrollWidth < 200) {
        AreaNameScrollWidth += 5;
      }
      if (AreaNameTimeItr > 200) {
        SHOW_AREA_NAME = false;
      }
    } else {
      if (AreaNameAlpha > 0) {
        AreaNameAlpha -= 2;
      }
    }

    if (AreaNameAlpha > 0) {

      ImageResource.getSprite("gui/world/map_name_scroll_middle")
          .getImage()
          .draw(
              490 - AreaNameScrollWidth + 20,
              90,
              AreaNameScrollWidth * 2,
              54,
              new Color(255, 255, 255, AreaNameAlpha));

      ImageResource.getSprite("gui/world/map_name_scroll_left")
          .draw(490 - AreaNameScrollWidth - 13, 75, new Color(255, 255, 255, AreaNameAlpha));
      ImageResource.getSprite("gui/world/map_name_scroll_right")
          .draw(490 + AreaNameScrollWidth + 20, 75, new Color(255, 255, 255, AreaNameAlpha));

      g.setFont(Font.size30);

      /*
      g.setColor(new Color(165,165,85,AreaNameOpacity));
      g.drawString(MapName, 512 - Font.size30.getWidth(MapName)/2, 75);
      */
      g.setColor(new Color(158, 116, 66, AreaNameAlpha));
      g.setWorldClip(490 - AreaNameScrollWidth + 20, 90, AreaNameScrollWidth * 2 - 20, 50);
      g.drawString(AreaName, 510 - Font.size30.getWidth(AreaName) / 2, 100);
      g.clearWorldClip();

      g.setFont(Font.size18);

      int zoneStatusY = 140;
      if (guardLevel == 0) {
        g.setColor(new Color(0, 0, 0, AreaNameAlpha - 170));
        g.fillRoundRect(
            512 - widthUnguarded / 2 - 13,
            zoneStatusY - 4,
            widthUnguarded + 26,
            38,
            5);
        g.setColor(new Color(238, 76, 76, AreaNameAlpha));
        g.fillRoundRect(
            512 - widthUnguarded / 2 - 9,
            zoneStatusY,
            widthUnguarded + 18,
            30,
            5);

        g.setColor(new Color(22, 22, 22, AreaNameAlpha));
        g.drawString(
            LanguageUtils.getString("ui.area_effect.unguarded_zone"),
            512 - widthUnguarded / 2,
            zoneStatusY + 5);
        ImageResource.getSprite("gui/skulls/silver")
            .getImage()
            .draw(
                512 - widthUnguarded / 2 - 40,
                zoneStatusY - 5,
                40,
                40,
                new Color(255, 255, 255, AreaNameAlpha));
        ImageResource.getSprite("gui/skulls/silver")
            .getImage()
            .draw(
                512 + widthUnguarded / 2,
                zoneStatusY - 5,
                40,
                40,
                new Color(255, 255, 255, AreaNameAlpha));
      } else {
        g.setColor(new Color(0, 0, 0, AreaNameAlpha - 170));
        g.fillRoundRect(
            512 - widthSafe / 2 - 13,
            zoneStatusY - 4,
            widthSafe + 26,
            38,
            5);
        g.setColor(new Color(196, 233, 125, AreaNameAlpha));
        g.fillRoundRect(
            512 - widthSafe / 2 - 9,
            zoneStatusY,
            widthSafe + 18,
            30,
            5);
        g.setColor(new Color(22, 22, 22, AreaNameAlpha));
        g.drawString(
            LanguageUtils.getString("ui.area_effect.safe_zone"),
            512 - widthSafe / 2,
            zoneStatusY + 5);
        ImageResource.getSprite("gui/skulls/silver")
            .getImage()
            .draw(
                512 - widthSafe / 2 - 40,
                zoneStatusY - 5,
                40,
                40,
                new Color(255, 255, 255, AreaNameAlpha));
        ImageResource.getSprite("gui/skulls/silver")
            .getImage()
            .draw(
                512 + widthSafe / 2,
                zoneStatusY - 5,
                40,
                40,
                new Color(255, 255, 255, AreaNameAlpha));
      }
    }

    if (!ParticleType.equals("None")) {

      Iterator<EnvParticle> it = Particles.iterator();

      while (it.hasNext()) {
        EnvParticle p = it.next();
        p.draw();
        if (p.getAlpha() == 0) {
          it.remove();
        }
      }
    }
  }

  public void showAreaName(String newAreaName) {
    if (!newAreaName.equals(AreaName)) {
      AreaName = newAreaName;
      SHOW_AREA_NAME = true;
      AreaNameAlpha = 0;
      AreaNameScrollWidth = 0;
      AreaNameTimeItr = 0;
    }
  }

  public boolean getFog() {
    return Fog;
  }

  public boolean getTint() {
    return Tint;
  }

  public String getAreaName() {
    return AreaName;
  }

  public void setAreaEffect(
      int newId,
      boolean tint,
      Color newTintColor,
      boolean fog,
      Color newFogColor,
      String particleType,
      int guardLevel) {
    if (newId != Id) {
      Id = newId;

      if (!fog) {
        if (Fog) {
          FogEffect.dissappear();
        }
      } else {
        if (FogEffect == null) {
          FogEffect = new Fog(ScreenHandler.myCamera.getX(), ScreenHandler.myCamera.getY(), newFogColor);
        } else {
          FogEffect.appear(ScreenHandler.myCamera.getX(), ScreenHandler.myCamera.getY(), newFogColor);
        }
      }

      Fog = fog;

      this.guardLevel = guardLevel;

      Tint = tint;
      if (Tint) {
        TintAlpha = 0;
        setTintColor(newTintColor);
      }

      if (AreaEffectHandler.nightTime && BlueSaga.playerCharacter.getZ() == 0) {
        Tint = true;
        setTintColor(BlueSagaColors.NIGHT);
      }

      setParticleType(particleType);
    }
  }

  public void setParticleType(String particleType) {
    // Only change particles if not the same
    if (!ParticleType.equals(particleType)) {

      // Remove old particles
      for (EnvParticle p : Particles) {
        p.dissappear();
      }

      ParticleType = particleType;

      if (!ParticleType.equals("None")) {

        Particles.clear();
        int nrParticles = 0;
        if (ParticleType.equals("spore")) {
          nrParticles = 60;
        } else if (ParticleType.equals("snow")) {
          nrParticles = 500;
        } else if (ParticleType.equals("firefly")) {
          nrParticles = 40;
        }
        for (int i = 0; i < nrParticles; i++) {
          Particles.add(new EnvParticle(ParticleType));
        }
      }
    }
  }

  public Color getTintColor() {
    return TintColor;
  }

  public void setTintColor(Color tintColor) {
    TintColorInit = tintColor;
    TintColor = tintColor;
    Tint = true;
  }

  public void setTintColorNow(Color tintColor) {
    TintColorInit = tintColor;
    TintColor = tintColor;
    TintAlpha = 255;
    Tint = true;
  }

  public void removeTintColor() {
    Tint = false;
    TintAlpha = 255;
  }

  public void removeTintColorNow() {
    Tint = false;
    TintAlpha = 0;
  }

  public void removeParticles() {
    for (EnvParticle p : Particles) {
      p.dissappear();
    }
  }

  public void removeParticlesNow() {
    Particles.clear();
  }

  public int getGuardLevel() {
    return guardLevel;
  }

  public void setGuardLevel(int guardLevel) {
    this.guardLevel = guardLevel;
  }
}
