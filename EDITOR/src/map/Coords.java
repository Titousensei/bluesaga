package map;

public class Coords {
  public final int x;
  public final int y;
  public final int z;

  public Coords(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public String toString()
  { return x + "," + y + "," + z; }
}
