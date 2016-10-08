package graphics;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Sprite {

  private Image image;
  private Animation animation = null;
  private Image thumb;

  public Sprite(String filename, int nrFrames) {
    Image[] graphics;

    if (nrFrames <= 2 || filename.contains("largewindow")) {
      graphics = new Image[nrFrames];
    } else {
      graphics = new Image[nrFrames * 2 - 2];
    }

    try {
      int k = 0;
      for (int i = 0; i < nrFrames; i++) {
        graphics[k] = new Image(filename + "_" + i + ".png");
        graphics[k].setFilter(Image.FILTER_NEAREST);
        k++;
      }

      if (!filename.contains("largewindow")) {
        if (nrFrames > 2) {
          for (int i = nrFrames - 2; i > 0; i--) {
            graphics[k] = new Image(filename + "_" + i + ".png");
            graphics[k].setFilter(Image.FILTER_NEAREST);
            k++;
          }
        }
      }
    } catch (SlickException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    int duration = 1000 / nrFrames;

    if (nrFrames > 2 && !filename.contains("largewindow")) {
      duration = 1000 / ((nrFrames * 2) - 2);
    }
    animation = new Animation(graphics, duration, true);
    animation.restart();
    thumb = animation.getImage(0).getScaledCopy(50, 50);
  }

  public Sprite(String filename) {
    try {
      image = new Image(filename + ".png");
      image.setFilter(Image.FILTER_NEAREST);
      thumb = image.getScaledCopy(50, 50);
    } catch (SlickException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public boolean isAnimated() {
    return animation != null;
  }

  public Animation getAnimation() {
    return animation;
  }

  public Image getImage() {
    return image;
  }

  public void draw(int x, int y) {
    if (animation != null) {
      animation.draw(x, y);
    } else {
      image.draw(x, y);
    }
  }

  public void draw(int x, int y, Color color) {
    if (animation != null) {
      animation.draw(x, y, color);
    } else {
      image.draw(x, y, color);
    }
  }

  public void draw(int x, int y, int width, int height, Color color) {
    if (animation != null) {
      animation.draw(x, y, width, height, color);
    } else {
      image.draw(x, y, width, height, color);
    }
  }

  public void drawSmall(int x, int y) {
    thumb.draw(x, y);
  }
}
