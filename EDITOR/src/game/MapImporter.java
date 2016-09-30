package game;

import java.util.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.imageio.ImageIO;

public class MapImporter
{
  public Map<RGB, String> tiles_ = new HashMap();
  public Map<RGB, String> objects_ = new HashMap();

  public Map<RGB, RGB> cache_ = new HashMap();

  public final String imgPath;
  public final Random randomGenerator;

  public MapImporter(String path)
  {
    imgPath = path;
    randomGenerator = new Random();
  }

  public void addPalette(int rgb, String type, String obj_type)
  {
    RGB pixel = new RGB(rgb);
    if (tiles_.put(pixel, type) == null) {
      if (obj_type != null) {
        objects_.put(pixel, obj_type);
      }
    }
    else {
      throw new RuntimeException("Palette duplicate for color: " + pixel);
    }
  }

  public RGB lookUp(int rgb)
  {
    RGB pixel = new RGB(rgb);
    RGB ret = cache_.get(pixel);
    if (ret == null) {
      int best = Integer.MAX_VALUE;
      for (Map.Entry<RGB, String> ent : tiles_.entrySet()) {
        RGB m = ent.getKey();
        int dist = pixel.distance2(m);
        if (dist < best) {
          ret = m;
          best = dist;
        }
      }
      cache_.put(pixel, ret);
    }
    return ret;
  }

  public Tile[] newRow(int sz)
  {
    Tile[] row = new Tile[sz];
    for (int x = 0 ; x<sz ; x ++) {
      row[x] = new Tile();
      row[x].x = x + EditorSettings.startX;
    }
    return row;
  }

  public void loadRow(int y, BufferedImage img, Tile[] row)
  {
    for (int x = 0 ; x<row.length ; x ++) {
      Tile t = row[x];
      t.y = y + EditorSettings.startY;

      RGB pixel = lookUp(img.getRGB(x, y));
      t.tileType = tiles_.get(pixel);
      if (t.tileType==null) {
        t.tileType = null;
        continue;
      }

      int randomTile = randomGenerator.nextInt(100) + 1;
      if (BP_EDITOR.GFX.getSprite("textures/" + t.tileType + "/" + randomTile) == null) {
        randomTile = 1;
      }
      t.tileName = Integer.toString(randomTile);

      t.objType = "None";
      t.passable = 1;
      if ((x+y)%2 == 0) {
        String obj = objects_.get(pixel);
        if (obj != null) {
          if ("rock" == obj) {
            if ("mountainpatch".equals(t.tileName)) {
              t.objType = "rock/stone4";
            }
            else {
              t.objType = "water/rock3";
            }
          }
          else { // tree
            if ("snow".equals(t.tileName)) {
              t.objType = "tree/snowtree3";
            }
            else {
              t.objType = "tree/tree3";
            }
          }
          t.passable = 0;
        }
      }
    }
  }

  public String changeTile(String type, String name, String ext)
  {
//System.out.println("changeTile - Type="+type+", name="+name+", ext="+ext);
    int lastChar = name.length() - 1;
    while (lastChar>0 &&  Character.isUpperCase(name.charAt(lastChar-1))) {
      -- lastChar;
    }
//System.out.println("<- " + name.substring(0, lastChar) + ext);
    return name.substring(0, lastChar) + ext;
  }

  public void fixRow(Tile[] row0, Tile[] row1, Tile[] row2)
  {
    if (row0[0].tileType == null) {
      return;
    }

    // fill trees and rocks passable alternate pattern
    for (int x = 1 ; x<row1.length-1 ; x ++) {
      String tileType = row1[x].tileType;
      if (tileType == null) {
        continue;
      }

      if (row1[x-1].passable == 0
      &&  row1[x+1].passable == 0
      &&  row0[x].passable == 0
      &&  row2[x].passable == 0
      ) {
        row1[x].passable = 0;
      }
/*
      if (row0[x].tileType != null && row0[x].tileType.equals(row2[x].tileType)) {  // U=D => L or R
        if (row1[x-1].tileType.equals(row1[x+1].tileType)) {  // L=R
          // ignore
        }
        else if (row0[x].tileType.equals(row1[x-1].tileType)) { // U=D & L!=R & L=U => LUD
          row1[x].tileName = changeTile(row0[x].tileType, row0[x].tileName, "R");
        }
        else {  // U=D L!=R & L!=U => RUD
          row1[x].tileName = changeTile(row0[x].tileType, row0[x].tileName, "L");
        }
      }
      else if (row1[x-1].tileType.equals(row1[x+1].tileType)) {  // U!=D & L=R
        if (row0[x].tileType.equals(row1[x-1].tileType)) {  // U!=D & L=R & L=U -= LUR
          row1[x].tileName = changeTile(row0[x].tileType, row0[x].tileName, "D");
        }
        else {  // U!=D & L=R & L!=U => LDR
          row1[x].tileName = changeTile(row2[x].tileType, row2[x].tileName, "U");
        }
      }
      else if (row0[x].tileType.equals(row1[x-1].tileType)) {  // U!=D & L!=R & U=L => UL or DR
        if (row0[x].tileType.equals(row1[x].tileType)) {  // U!=D & L!=R & U=L & O=U => UL
          row1[x].tileName = changeTile(row0[x].tileType, row0[x].tileName, "UL");
        }
        else {  // U!=D & L!=R & U=L & O!=U => DR
          row1[x].tileName = changeTile(row0[x].tileType, row0[x].tileName, "DR");
        }
      }
      else {  // U!=D & L!=R & U!=L => UR or DL
        if (row0[x].tileType.equals(row1[x].tileType)) {  // U!=D & L!=R & U!=L & O=U => UR
          row1[x].tileName = changeTile(row0[x].tileType, row0[x].tileName, "UR");
        }
        else {  // U!=D & L!=R & U!=L & O!=U => DL
          row1[x].tileName = changeTile(row0[x].tileType, row0[x].tileName, "DL");
        }
      }
*/
      /*
    addPalette(img.getRGB( 0, 0), "shallow", null);
    addPalette(img.getRGB( 1, 0), "water", null);
    addPalette(img.getRGB( 2, 0), "sand", null);
    addPalette(img.getRGB( 3, 0), "mountainpatch", null);
    addPalette(img.getRGB( 4, 0), "grass", null);
    addPalette(img.getRGB( 5, 0), "grasspatch", null);
    addPalette(img.getRGB( 6, 0), "grass", null);
    addPalette(img.getRGB( 7, 0), "grass", "tree");
    addPalette(img.getRGB( 8, 0), "mountain", null);
    addPalette(img.getRGB( 9, 0), "mountainpatch", null);
    addPalette(img.getRGB(10, 0), "mountainpatch", "rock");
    addPalette(img.getRGB(11, 0), "snow", null);
    addPalette(img.getRGB(12, 0), "watercave", null);
      */

    }
  }

  public static void insertRow(Tile[] row, int z)
  throws SQLException
  {
    for (int x = 0 ; x<row.length && row[x]!=null ; x ++) {
      if (row[x].tileType != null) {
        BP_EDITOR.mapDB.update(row[x].toSqlInsert(z));
      }
    }
  }

  public void load()
  {
    System.out.println("Importing PNG: " + imgPath);

    int z = 0;
    try (ResultSet mapInfo = BP_EDITOR.mapDB.askDB("select max(Z) from area_tile")
    ) {
      if (mapInfo.next()) {
        z = mapInfo.getInt("max(Z)") + 1;
      }
      mapInfo.getStatement().close();
    }
    catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
    System.out.println("Z = " + z);

    BufferedImage img;
    try {
      img = ImageIO.read(new File(imgPath));
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    int max_x = img.getWidth();
    int max_y = img.getHeight();

    addPalette(img.getRGB( 0, 0), "shallow", null);
    addPalette(img.getRGB( 1, 0), "water", null);
    addPalette(img.getRGB( 2, 0), "sand", null);
    addPalette(img.getRGB( 3, 0), "mountainpatch", null);
    addPalette(img.getRGB( 4, 0), "grass", null);
    addPalette(img.getRGB( 5, 0), "grasspatch", null);
    addPalette(img.getRGB( 6, 0), "grass", null);
    addPalette(img.getRGB( 7, 0), "grass", "tree");
    addPalette(img.getRGB( 8, 0), "mountain", null);
    addPalette(img.getRGB( 9, 0), "mountainpatch", null);
    addPalette(img.getRGB(10, 0), "mountainpatch", "rock");
    addPalette(img.getRGB(11, 0), "snow", null);
    addPalette(img.getRGB(12, 0), "watercave", null);

    Tile[] row0 = newRow(max_x);
    Tile[] row1 = newRow(max_x);
    Tile[] row2 = newRow(max_x);

    BP_EDITOR.mapDB.updateDB("BEGIN TRANSACTION");
    try {
      for (int y = 1 ; y<max_y ; y ++) {
        if (y % 100 == 0) {
          System.out.println("... " + y + " / " + max_y);
          BP_EDITOR.mapDB.updateDB("END TRANSACTION");
          BP_EDITOR.mapDB.updateDB("BEGIN TRANSACTION");
        }

        Tile[] row_tmp = row0;
        row0 = row1;
        row1 = row2;
        row2 = row_tmp;

        loadRow(y, img, row2);
        fixRow(row0, row1, row2);
        insertRow(row1, z);
      }
      // last row
      row0 = row1;
      row1 = row2;
      fixRow(row0, row1, row2);
      insertRow(row1, z);

      BP_EDITOR.mapDB.updateDB("END TRANSACTION");
    }
    catch (SQLException ex) {
      BP_EDITOR.mapDB.updateDB("ROLLBACK TRANSACTION");
      ex.printStackTrace();
    }
  }

  public static class RGB
  {
    public final int rgb;
    public final int r;
    public final int g;
    public final int b;

    public RGB(int rgb)
    {
      this.rgb = rgb;
      r = (rgb & 0x00FF0000) >> 16;
      g = (rgb & 0x0000FF00) >> 8;
      b = (rgb & 0x000000FF);
    }

    @Override
    public int hashCode()
    { return rgb; }

    @Override
    public boolean equals(Object obj)
    {
      if (obj instanceof RGB) {
        return ((RGB) obj).rgb == rgb;
      }
      return false;
    }

    public int distance2(RGB other) {
      int dr = r - other.r;
      int dg = g - other.g;
      int db = b - other.b;

      return dr*dr + dg*dg + db*db;
    }

    @Override
    public String toString()
    { return Integer.toHexString(rgb); }
  }

  public static class Tile
  {
    public String tileType = null;
    public String tileName = null;
    public String objType = null;
    public int x = 0;
    public int y = 0;
    public int passable = 1;

    public String toSqlInsert(int z)
    {
      return "insert into area_tile (Type, Name, X, Y, Z, ObjectId, Passable) values ('"
          + tileType + "', '" + tileName + "',"
          + x + "," + y + "," + z + ", '"
          + ((objType!=null) ? objType : "None")
          + "'," + passable + ")";
    }
  }
}
