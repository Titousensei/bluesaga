package map;

import java.util.*;

import components.Builder;

public class AreaEffectBuilder
extends Builder<AreaEffect>
{
  protected AreaEffect ae = null;
  protected List<Integer> items = null;

  @Override
  public void init(int id, String name, String origin) {
    ae = new AreaEffect(id, name, origin);
  }

  @Override
  protected boolean isDuplicateAllowed(String setter)
  { return "AreaItem".equals(setter); }

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

  public void areaItem(String val) {
    int id = parseInt(val);
    if (id!=0) {
      if (items==null) {
        items = new ArrayList<>(4);
      }
      items.add(id);
    }

  }

  public void areaCopper(String val) {
    ae.setAreaCopper(parseInt(val));
  }

  public void impassableForMonsters() {
    ae.setImpassableForMonsters(true);
  }

  public AreaEffect build() {
    if (items!=null) {
      int[] it = new int[items.size()];
      int i = 0;
      for (Integer val : items) {
        it[i] = val;
        ++ i;
      }
      ae.setAreaItems(it);
    }
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
