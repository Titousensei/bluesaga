package map;

import org.newdawn.slick.Color;

import utils.RandomUtils;
import graphics.ImageResource;

class FogCloud {

  int Id;
  float X;
  float Y;
  float Speed;
  int opacity;
  int gotoOpacity;

  int blue;
  int cameraStartX;
  int cameraStartY;
  Color baseColor;
  Color color;

  public FogCloud(int newId, int cameraX, int cameraY, Color aColor) {
    Id = newId;

    cameraStartX = cameraX;
    cameraStartY = cameraY;
    baseColor = aColor;

    X = RandomUtils.getInt(-400, 1024);
    Y = RandomUtils.getInt(-200, 640);
    Speed = RandomUtils.getFloat(0.5f, 2.5f);
    opacity = 0;
    gotoOpacity = getRandomOpacity();
  }

  private final int getRandomOpacity() {
    return RandomUtils.getInt(100, 200);
  }

  private final void setOpacity(int newOpacity) {
    opacity = newOpacity;
    color = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), opacity);
  }

  public void draw(int cameraX, int cameraY) {
    X -= (cameraStartX - cameraX) + Speed;
    Y -= (cameraStartY - cameraY);
    cameraStartX = cameraX;
    cameraStartY = cameraY;

    if (opacity < gotoOpacity) {
      setOpacity(opacity + 1);
    } else if (opacity > gotoOpacity) {
      setOpacity(opacity - 1);
    }

    if (X < -1024 || Y < -1000 || Y > 1000 || X > 2048) {
      if (gotoOpacity > 0) {
        X = RandomUtils.getInt(1024, 1824);
        Y = RandomUtils.getInt(-200, 640);
        Speed = RandomUtils.getFloat(0.5f, 2.5f);
        gotoOpacity = getRandomOpacity();
        setOpacity(gotoOpacity);
      }
    }

    ImageResource.getSprite("effects/fog" + Id)
        .draw(Math.round(X), Math.round(Y), color);
  }

  public void dissappear() {
    gotoOpacity = 0;
  }

  public void appear() {
    gotoOpacity = getRandomOpacity();
  }
}
