package map;

import java.util.*;

import components.Builder;

public class AreaEffectBuilder
extends Builder<AreaEffect>
{
  protected AreaEffect ae = null;

  public void init(int id, String name, String origin) {
    ae = new AreaEffect(id, name, origin);
  }

  public void song(String val) {
    ae.setSong(val);
  }

  public void ambient(String val) {
    ae.setAmbient(val);
  }

  public void guardLevel(String val) {
    ae.setGuardedLevel(parseInt(val));
  }

  public void tintColor(String val) {
    ae.setTintColor(val);
  }

  public void fogColor(String val) {
    ae.setFogColor(val);
  }

  public void particles(String val) {
    ae.setParticles(val);
  }

  public void areaItems(String val) {
    ae.setAreaItems(val);
  }

  public void areaCopper(String val) {
    ae.setAreaCopper(parseInt(val));
  }

  public void impassableForMonsters() {
    ae.setImpassableForMonsters(true);
  }

  public AreaEffect build() {
    return ae;
  }

  public static void main(String... args) {
    Map<Integer, AreaEffect> m = new HashMap<>();
    Builder.load(args[0], AreaEffectBuilder.class, m);
    if (args.length>1) {
      if ("-".equals(args[1])) {
        for (AreaEffect q : m.values()) {
          System.out.println(q);
        }
      }
      else {
        System.out.println(m.get(parseInt(args[1])));
      }
    }
  }
}
