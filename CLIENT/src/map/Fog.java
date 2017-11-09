package map;

import java.util.*;

import org.newdawn.slick.Color;

public class Fog {
  private List<FogCloud> FogClouds;

  public Fog(int cameraStartX, int cameraStartY, Color aColor) {
    FogClouds = new ArrayList<FogCloud>(10);
    for (int i = 1; i < 5; i++) {
      FogCloud f = new FogCloud(i, cameraStartX, cameraStartY, aColor);
      FogClouds.add(f);
    }
  }

  public void draw(int cameraX, int cameraY) {

    Iterator<FogCloud> cloudIterator = FogClouds.iterator();

    while (cloudIterator.hasNext()) {
      FogCloud cloud = cloudIterator.next();
      cloud.draw(cameraX, cameraY);
      if (cloud.opacity == 0) {
        cloudIterator.remove();
      }
    }
  }

  public void appear(int cameraStartX, int cameraStartY, Color aColor) {
    int nrClouds = 0;
    for (FogCloud f : FogClouds) {
      f.appear();
      nrClouds++;
    }

    for (int i = nrClouds + 1; i < 5; i++) {
      FogCloud f = new FogCloud(i, cameraStartX, cameraStartY, aColor);
      FogClouds.add(f);
    }
  }

  public void dissappear() {
    Iterator<FogCloud> cloudIterator = FogClouds.iterator();

    while (cloudIterator.hasNext()) {
      FogCloud cloud = cloudIterator.next();
      cloud.dissappear();
    }
  }
}
