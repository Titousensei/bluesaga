package graphics;

import java.util.*;
import java.security.MessageDigest;
import org.newdawn.slick.Color;

public class BlueSagaColors {

  public final static Color RED = new Color(222, 67, 67);
  public final static Color WHITE = new Color(255, 255, 255, 255);
  public final static Color BLACK = new Color(21, 21, 21, 255);
  public final static Color YELLOW = new Color(255, 234, 116, 255);
  public final static Color GREEN = new Color(0, 180, 0, 255);
  public final static Color BLUE = new Color(0, 165, 255, 255);
  public final static Color ORANGE = new Color(227, 144, 0, 255);
  public final static Color PURPLE = new Color(188, 104, 215, 255);

  public final static Color STANCE_DEF_COLOR = new Color(169, 255, 122);
  public final static Color STANCE_ATK_COLOR = new Color(249, 144, 144);

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
