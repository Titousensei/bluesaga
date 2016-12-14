package utils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

public class RandomUtils {
  public static int getInt(int min, int max) {
    return ThreadLocalRandom.current().nextInt(max - min + 1) + min;
  }

  public static float getFloat(float minf, float maxf) {
    return ThreadLocalRandom.current().nextFloat() * (maxf - minf) + minf;
  }

  public static double getPercent() {
    return ThreadLocalRandom.current().nextDouble();
  }

  public static double getGaussian() {
    return ThreadLocalRandom.current().nextGaussian();
  }

  public static <T> T getAny(List<T> things) {
    int pick = ThreadLocalRandom.current().nextInt(things.size());
    return things.get(pick);
  }

  /**
   * Check attacker strength versus defender's resistance
   * based on percentage of difference (normalized delta).
   *
   * dn=-0.50: .10 <- low anchor
   * dn=-0.33: .156
   * dn=-0.20: .213
   * dn=-0.10: .266
   * dn=0.000: .326
   * dn=0.370: .586
   * dn=0.500: .674
   * dn=0.667: .770
   * dn=1.000: .90 <- high anchor
   *
   * dn = (v1-v2)/v1
   * SigmoidCheck(dn) = 1/(1+exp(-2.9*(dn-.25)))
   */
  public static boolean sigmoidCheck(double att, double def) {
    double dn = (att - def) / att;
    double percent = 1.0 / (1.0 + Math.exp(-2.9 * (dn - 0.25)));
    return (percent >= RandomUtils.getPercent());
  }
}
