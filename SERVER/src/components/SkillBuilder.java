package components;

import java.util.*;

public class SkillBuilder
extends Builder<JobSkill>
{
  protected JobSkill s = null;

  public void init(int id, String name, String origin) {
    s = new JobSkill(id, name, origin);
  }

  public void type(String val) {
    s.setType(val);
  }

  public void noXP() {
    s.setGainSP(false);
  }

  public JobSkill build() {
    return s;
  }

  public static Map<String, JobSkill> mapNames(Map<Integer, JobSkill> skillDef) {
    Map<String, JobSkill> ret = new HashMap<>();
    for (JobSkill q : skillDef.values()) {
      ret.put(q.getName(), q);
    }
    return ret;
  }

  public static void main(String... args) {
    Map<Integer, JobSkill> m = new HashMap<>();
    Builder.load(args[0], SkillBuilder.class, m);
    if (args.length>1) {
      if ("-".equals(args[1])) {
        for (JobSkill q : m.values()) {
          System.out.println(q);
        }
      }
      else {
        System.out.println(m.get(parseInt(args[1])));
      }
    }
  }
}
