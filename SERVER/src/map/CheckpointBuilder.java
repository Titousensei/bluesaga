package map;

import java.util.*;

import components.Builder;
import utils.Coords;

public class CheckpointBuilder
extends Builder<Coords>
{
  protected Coords pos = null;

  public void init(int id, String name, String origin) {
  }

  public void position(String val) {
    String xyz[] = val.split(",");
    int x = Integer.parseInt(xyz[0]);
    int y = Integer.parseInt(xyz[1]);
    int z = Integer.parseInt(xyz[2]);
    pos = new Coords(x, y, z);
  }

  public Coords build() {
    return pos;
  }
}
