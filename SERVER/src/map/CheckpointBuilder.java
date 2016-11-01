package map;

import java.util.*;

import components.Builder;
import utils.Coords;

public class CheckpointBuilder
extends Builder<Coords>
{
  protected Coords pos = null;

  @Override
  public void init(int id, String name, String origin) {
  }

  public void position(String val) {
    String xyz[] = val.split(",");
    int x = parseInt(xyz[0]);
    int y = parseInt(xyz[1]);
    int z = parseInt(xyz[2]);
    pos = new Coords(x, y, z);
  }

  public Coords build() {
    return pos;
  }
}
