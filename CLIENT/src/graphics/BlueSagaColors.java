package graphics;

import java.util.*;
import java.security.MessageDigest;
import org.newdawn.slick.Color;

public class BlueSagaColors {

  public final static Color BRIGHT_RED = new Color(255, 0, 0);
  public final static Color RED = new Color(222, 67, 67);
  public final static Color WHITE = new Color(255, 255, 255);
  public final static Color BLACKER = new Color(0, 0, 0);
  public final static Color BLACK = new Color(21, 21, 21);
  public final static Color BROWN = new Color(142, 95, 18);
  public final static Color YELLOW = new Color(255, 234, 116);
  public final static Color YELLOW2 = new Color(255, 249, 75);
  public final static Color GREEN = new Color(0, 180, 0);
  public final static Color BLUE = new Color(0, 165, 255);
  public final static Color ORANGE = new Color(227, 144, 0);
  public final static Color ORANGE2 = new Color(255, 212, 109);
  public final static Color PURPLE = new Color(188, 104, 215);
  public final static Color LIME = new Color(200, 255, 111);
  public final static Color AQUA = new Color(136, 255, 203);
  public final static Color NIGHT = new Color(185, 150, 255);
  public final static Color BURGUNDY = new Color(164, 42, 42);
  public final static Color WINE = new Color(236, 86, 86);

  public final static Color STANCE_DEF_COLOR = new Color(169, 255, 122);
  public final static Color STANCE_ATK_COLOR = new Color(249, 144, 144);

  public final static Color BOUNTY_UP = new Color(128, 215, 97, 255);
  public final static Color BOUNTY_DOWN = new Color(236, 86, 86, 255);

  public final static Color YELLOW_PROJECTILE = new Color(255, 255, 100, 200);
  public final static Color YELLOW_ABILITY = new Color(255, 255, 135, 200);
  public final static Color WHITE_COOLDOWN = new Color(255, 255, 255, 150);
  public final static Color BLACK_TRANS = new Color(0, 0, 0, 150);
  public final static Color BLACK_TRANS200 = new Color(0, 0, 0, 200);
  public final static Color BLACK_TRANS100 = new Color(0, 0, 0, 100);
  public final static Color BLACK_TRANS50 = new Color(0, 0, 0, 50);
  public final static Color RED_TRANS = new Color(255, 0, 0, 150);
  public final static Color WHITE_TRANS = new Color(255, 255, 255, 100);
  public final static Color WHITE_TRANS50 = new Color(255, 255, 255, 50);
  public final static Color WHITE_TRANS20 = new Color(255, 255, 255, 20);
  public final static Color WHITE_TRANS200 = new Color(255, 255, 255, 200);
  public final static Color WHITE_TRANS0 = new Color(255, 255, 255, 0);
  public final static Color NOTHING = new Color(0, 0, 0, 0);

  public final static Color GRAY130 = new Color(130, 130, 130);
  public final static Color GRAY100 = new Color(100, 100, 100);
  public final static Color DARK = new Color(50, 50, 50);
  public final static Color GRAY27 = new Color(27, 27, 27);

  public final static Color CHAR_BLUE = new Color(71, 178, 218);
  public final static Color CHAR_RED = new Color(225, 91, 91);
  public final static Color CHAR_ORANGE = new Color(255, 176, 23);
  public final static Color CHAR_YELLOW = new Color(246, 255, 101);
  public final static Color CHAR_GREEN = new Color(127, 191, 146);
  public final static Color CHAR_PINK = new Color(255, 173, 238);
  public final static Color CHAR_DARK_GRAY = new Color(75, 80, 82);
  public final static Color CHAR_PURPLE = new Color(219, 128, 255);

  private final static Map<String, Color> COLOR_MAP = new HashMap<>(8);

  static {
    COLOR_MAP.put("@RED", RED);
    COLOR_MAP.put("@WHI", WHITE);
    COLOR_MAP.put("@BLA", BLACK);
    COLOR_MAP.put("@YEL", YELLOW);
    COLOR_MAP.put("@GRR", GREEN);
    COLOR_MAP.put("@BLU", BLUE);
    COLOR_MAP.put("@ORA", ORANGE);
    COLOR_MAP.put("@PUR", PURPLE);
  }

  public static Color getColorFromString(String colorInfo) {
    String color_info[] = colorInfo.split(",");
    Color newColor =
        new Color(
            Integer.parseInt(color_info[0]),
            Integer.parseInt(color_info[1]),
            Integer.parseInt(color_info[2]));
    return newColor;
  }

  public static Color getColorMap(String colorName) {
    Color ret = COLOR_MAP.get(colorName);
    return (ret != null) ? ret : RED;
  }

  public static Color getConsistentColor(String seed) {
    try {
      byte[] msg = seed.getBytes("UTF-8");
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] md5 = md.digest(msg);
      return new Color((int) md5[2] & 0xFF, (int) md5[1] & 0xFF, (int) md5[0] & 0xFF, 255);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return ORANGE;
  }
}
