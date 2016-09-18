package game;

import java.util.*;

import map.Tile;

public final class EdgeHelper
{
  private final static int DOWN_LEFT  = 0b00000010;
  private final static int DOWN_RIGHT = 0b00000011;
  private final static int UP_LEFT    = 0b00001000;
  private final static int UP_RIGHT   = 0b00001100;
  private final static int LEFT_UP    = 0b00100000;
  private final static int LEFT_DOWN  = 0b00110000;
  private final static int RIGHT_UP   = 0b10000000;
  private final static int RIGHT_DOWN = 0b11000000;

  private final static int DOWN_ANY  = 0b00000001;
  private final static int UP_ANY    = 0b00000100;
  private final static int LEFT_ANY  = 0b00010000;
  private final static int RIGHT_ANY = 0b01000000;

  public static int suffixPosition(String tileName) {
    int lastChar = tileName.length() - 1;

    while (lastChar>0) {
      -- lastChar;
      if (!Character.isUpperCase(tileName.charAt(lastChar))) {
        return lastChar + 1;
      }
    }

    return 0;
  }

  public static String complete(Tile tileLeft, Tile tileRight, Tile tileUp, Tile tileDown)
  {

    String onLeft  = tileLeft.getName();
    String onRight = tileRight.getName();
    String onUp    = tileUp.getName();
    String onDown  = tileDown.getName();

    onLeft = onLeft.substring(suffixPosition(onLeft));
    onRight = onRight.substring(suffixPosition(onRight));
    onUp = onUp.substring(suffixPosition(onUp));
    onDown = onDown.substring(suffixPosition(onDown));

    int pattern = 0;

    if ("L".equals(onDown)  || "DL".equals(onDown)  || "IDR".equals(onDown)) {
      pattern |= DOWN_LEFT;
    }
    else if ("R".equals(onDown)  || "DR".equals(onDown)  || "IDL".equals(onDown)) {
      pattern |= DOWN_RIGHT;
    }
    if ("L".equals(onUp)    || "UL".equals(onUp)    || "IUR".equals(onUp)) {
      pattern |= UP_LEFT;
    }
    else if ("R".equals(onUp)    || "UR".equals(onUp)    || "IUL".equals(onUp)) {
      pattern |= UP_RIGHT;
    }
    if ("U".equals(onLeft)  || "UL".equals(onLeft)  || "IDL".equals(onLeft)) {
      pattern |= LEFT_UP;
    }
    else if ("D".equals(onLeft)  || "DL".equals(onLeft)  || "IUL".equals(onLeft)) {
      pattern |= LEFT_DOWN;
    }
    if ("U".equals(onRight) || "UR".equals(onRight) || "IDR".equals(onRight)) {
      pattern |= RIGHT_UP;
    }
    else if ("D".equals(onRight) || "DR".equals(onRight) || "IUR".equals(onRight)) {
      pattern |= RIGHT_DOWN;
    }

    switch (pattern) {

    // 1 or 2 Matching
    case DOWN_LEFT:
      return "L,IUR,UL";
    case UP_LEFT:
      return "L,IDR,DL";
    case DOWN_LEFT | UP_LEFT:
      return "L";
    case DOWN_RIGHT:
      return "R,IUL,UR";
    case UP_RIGHT:
      return "R,IDL,DR";
    case DOWN_RIGHT | UP_RIGHT:
      return "R";
    case LEFT_UP:
      return "U,IDR,UR";
    case RIGHT_UP:
      return "U,IDL,UL";
    case LEFT_UP | RIGHT_UP:
      return "U";
    case LEFT_DOWN:
      return "D,IUR,DR";
    case RIGHT_DOWN:
      return "D,IUL,DL";
    case LEFT_DOWN | RIGHT_DOWN:
      return "D";

    case DOWN_LEFT | LEFT_DOWN:
      return "IUR";
    case DOWN_LEFT | RIGHT_UP:
      return "UL";
    case LEFT_UP | UP_LEFT:
      return "IDR";
    case LEFT_DOWN | UP_RIGHT:
      return "DR";
    case DOWN_RIGHT | LEFT_UP:
      return "UR";
    case DOWN_RIGHT | RIGHT_DOWN:
      return "IUL";
    case RIGHT_DOWN | UP_LEFT:
      return "DL";
    case RIGHT_UP | UP_RIGHT:
      return "IDL";

    // 2 non matching
    case DOWN_LEFT | LEFT_UP:
      return "L,U";
    case DOWN_LEFT | RIGHT_DOWN:
      return "D,L";
    case DOWN_RIGHT | LEFT_DOWN:
      return "D,R";
    case DOWN_RIGHT | RIGHT_UP:
      return "R,U";
    case DOWN_LEFT | UP_RIGHT:
    case DOWN_RIGHT | UP_LEFT:
      return "L,R";

    case LEFT_DOWN | RIGHT_UP:
    case LEFT_UP | RIGHT_DOWN:
      return "D,U";
    case LEFT_DOWN | UP_LEFT:
      return "D,L";
    case LEFT_UP | UP_RIGHT:
      return "R,U";
    case RIGHT_UP | UP_LEFT:
      return "L,U";
    case RIGHT_DOWN | UP_RIGHT:
      return "D,R";

    // 3 Matching
    case DOWN_LEFT | LEFT_DOWN | RIGHT_DOWN:
      return "D,IUR,L";
    case DOWN_LEFT | LEFT_DOWN | RIGHT_UP:
      return "UL,IUR,D,L,U";
    case DOWN_LEFT | LEFT_DOWN | UP_LEFT:
      return "L,IUR,D";
    case DOWN_LEFT | LEFT_DOWN | UP_RIGHT:
      return "DR,IUR,D,L,R";
    case DOWN_LEFT | LEFT_UP | RIGHT_DOWN:
      return "D,L,U";
    case DOWN_LEFT | LEFT_UP | RIGHT_UP:
      return "U,UL,L";
    case DOWN_LEFT | LEFT_UP | UP_LEFT:
      return "L,IDR,U";
    case DOWN_LEFT | LEFT_UP | UP_RIGHT:
      return "L,R,U";
    case DOWN_LEFT | RIGHT_DOWN | UP_LEFT:
      return "L,DL,D";
    case DOWN_LEFT | RIGHT_DOWN | UP_RIGHT:
      return "D,L,R";
    case DOWN_LEFT | RIGHT_UP | UP_LEFT:
      return "L,UL,U";
    case DOWN_LEFT | RIGHT_UP | UP_RIGHT:
      return "UL,IDL,L,R,U";
    case DOWN_RIGHT | LEFT_DOWN | RIGHT_DOWN:
      return "D,IUL,R";
    case DOWN_RIGHT | LEFT_DOWN | RIGHT_UP:
      return "D,R,U";
    case DOWN_RIGHT | LEFT_DOWN | UP_LEFT:
      return "D,L,R";
    case DOWN_RIGHT | LEFT_DOWN | UP_RIGHT:
      return "R,DR,D";
    case DOWN_RIGHT | LEFT_UP | RIGHT_DOWN:
      return "UR,IUL,D,R,U";
    case DOWN_RIGHT | LEFT_UP | RIGHT_UP:
      return "U,UR,R";
    case DOWN_RIGHT | LEFT_UP | UP_LEFT:
      return "UR,IDR,L,R,U";
    case DOWN_RIGHT | LEFT_UP | UP_RIGHT:
      return "R,UR,U";
    case DOWN_RIGHT | RIGHT_DOWN | UP_LEFT:
      return "DL,IUL,D,L,R";
    case DOWN_RIGHT | RIGHT_DOWN | UP_RIGHT:
      return "R,IUL,D";
    case DOWN_RIGHT | RIGHT_UP | UP_LEFT:
      return "L,R,U";
    case DOWN_RIGHT | RIGHT_UP | UP_RIGHT:
      return "R,IDL,U";
    case LEFT_DOWN | RIGHT_DOWN | UP_LEFT:
      return "D,DL,L";
    case LEFT_DOWN | RIGHT_DOWN | UP_RIGHT:
      return "D,DR,R";
    case LEFT_DOWN | RIGHT_UP | UP_LEFT:
      return "D,L,U";
    case LEFT_DOWN | RIGHT_UP | UP_RIGHT:
      return "DR,IDL,D,R,U";
    case LEFT_UP | RIGHT_DOWN | UP_LEFT:
      return "DL,IDR,D,L,U";
    case LEFT_UP | RIGHT_DOWN | UP_RIGHT:
      return "D,R,U";
    case LEFT_UP | RIGHT_UP | UP_LEFT:
      return "U,IDR,L";
    case LEFT_UP | RIGHT_UP | UP_RIGHT:
      return "U,IDL,R";

    // 4 Matching
    case LEFT_DOWN | RIGHT_DOWN | UP_LEFT | DOWN_LEFT:
      return "D,L,DL,IUR";
    case LEFT_DOWN | RIGHT_DOWN | UP_LEFT | DOWN_RIGHT:
      return "D,DL,IUL,L,R";
    case LEFT_DOWN | RIGHT_DOWN | UP_RIGHT | DOWN_LEFT:
      return "D,DR,IUR,L,R";
    case LEFT_DOWN | RIGHT_DOWN | UP_RIGHT | DOWN_RIGHT:
      return "D,R,DR,IUL";
    case LEFT_DOWN | RIGHT_UP | UP_LEFT | DOWN_LEFT:
      return "L,UL,IUR,D,U";
    case LEFT_DOWN | RIGHT_UP | UP_LEFT | DOWN_RIGHT:
      return "D,L,R,U";
    case LEFT_DOWN | RIGHT_UP | UP_RIGHT | DOWN_LEFT:
      return "DR,UL,IDL,IUR,D,L,R,U";
    case LEFT_DOWN | RIGHT_UP | UP_RIGHT | DOWN_RIGHT:
      return "R,DR,IDL,D,R,U";
    case LEFT_UP | RIGHT_DOWN | UP_LEFT | DOWN_LEFT:
      return "L,DL,IDR,D,U";
    case LEFT_UP | RIGHT_DOWN | UP_LEFT | DOWN_RIGHT:
      return "DL,UR,IDR,IUL,D,L,R,U";
    case LEFT_UP | RIGHT_DOWN | UP_RIGHT | DOWN_LEFT:
      return "D,L,R,U";
    case LEFT_UP | RIGHT_DOWN | UP_RIGHT | DOWN_RIGHT:
      return "R,UR,IUL,D,U";
    case LEFT_UP | RIGHT_UP | UP_LEFT | DOWN_LEFT:
      return "L,U,UL,IDR";
    case LEFT_UP | RIGHT_UP | UP_LEFT | DOWN_RIGHT:
      return "U,UR,IDR,L,R";
    case LEFT_UP | RIGHT_UP | UP_RIGHT | DOWN_LEFT:
      return "U,UL,IDL,L,R";
    case LEFT_UP | RIGHT_UP | UP_RIGHT | DOWN_RIGHT:
      return "R,U,UR,IDL";
    }

    // Full tiles all around
    onLeft  = tileLeft.getType();
    onRight = tileRight.getType();
    onUp    = tileUp.getType();
    onDown  = tileDown.getType();

    pattern = UP_ANY;

    if (onUp.equals(onDown))  { pattern |= DOWN_ANY; }
    if (onUp.equals(onLeft))  { pattern |= LEFT_ANY; }
    if (onUp.equals(onRight)) { pattern |= RIGHT_ANY; }

    switch (pattern) {

    case DOWN_ANY:
    case UP_ANY:
      return "D,U,L,R";
    case LEFT_ANY:
    case RIGHT_ANY:
      return "L,R,D,U";
    case UP_ANY | DOWN_ANY:
    case LEFT_ANY | RIGHT_ANY:
      return "D,U,L,R";

    case DOWN_ANY | RIGHT_ANY:
    case UP_ANY | LEFT_ANY:
      return "UL,IUL,DR,IDR";
    case DOWN_ANY | LEFT_ANY:
    case UP_ANY | RIGHT_ANY:
      return "UR,IUR,DL,IDL";
    }

    return "D,U,L,R";
  }

  public static String nextPossible(String possible, String suffix)
  {
    String[] option = possible.split(",");

    for (int i=0 ; i<option.length-1 ; ++i) {
      if (suffix.equals(option[i])) {
        return option[i+1];
      }
    }

    return option[0];
  }
}
