package map;

public class AreaEffect {

  public final int id;
  public final String name;
  private final String origin;

  private int guardedLevel = 1;

  private String particles = "None";

  private String tintColor = null;
  private String fogColor = null;

  private String song = "none";
  private String ambient = "none";

  private int[] areaItems = null;
  private int areaCopper = 0;

  private String infoDay = null;
  private String infoNight = null;

  private boolean impassableForMonsters = false;

  public AreaEffect(int id, String name, String origin) {
    this.id = id;
    this.name = name;
    this.origin = origin;
  }

  public String getInfoDay() {
    if (infoDay == null) {
      infoDay = getInfo(song, ambient, particles);
    }
    return infoDay;
  }

  public String getInfoNight() {
    if (infoNight == null) {
      infoNight = getInfo("night", "night", "firefly");
    }
    return infoNight;
  }

  private String getInfo(String s, String a, String p)
  {
    StringBuilder sb = new StringBuilder(1000);
    sb.append(id)
      .append(',')
      .append(name)
      .append(',');
    if (tintColor!=null) {
      sb.append("1,")
        .append(tintColor)
        .append(',');
    }
    else {
      sb.append("0,0,0,0,");
    }
    if (fogColor!=null) {
      sb.append("1,")
        .append(fogColor)
        .append(',');
    }
    else {
      sb.append("0,0,0,0,");
    }
    sb.append(song)
      .append(',')
      .append(ambient)
      .append(',')
      .append(particles)
      .append(',')
      .append(guardedLevel);

    return sb.toString();
  }

  /**
   * Getters and setters
   * @return
   */
  public int getTint() { return (tintColor!=null) ? 1 : 0 ; }
  public String getTintColor() { return tintColor; }
  void setTintColor(String tintColor) { this.tintColor = tintColor; }

  public int getFog() { return (fogColor!=null) ? 1 : 0; }
  public String getFogColor() { return fogColor; }
  void setFogColor(String fogColor) { this.fogColor = fogColor; }

  public int getGuardedLevel() { return guardedLevel; }
  void setGuardedLevel(int guardedLevel) { this.guardedLevel = guardedLevel; }

  public String getParticles() { return particles; }
  void setParticles(String particles) { this.particles = particles; }

  public int[] getAreaItems() { return areaItems; }
  void setAreaItems(int[] areaItems) { this.areaItems = areaItems; }

  public int getAreaCopper() { return areaCopper; }
  void setAreaCopper(int areaCopper) { this.areaCopper = areaCopper; }

  public String getSong() { return song; }
  void setSong(String song) { this.song = song; }

  public String getAmbient() { return ambient; }
  void setAmbient(String ambient) { this.ambient = ambient; }

  public boolean isImpassableForMonsters() { return impassableForMonsters; }
  void setImpassableForMonsters(boolean val) { impassableForMonsters = val; }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(1000);
    sb.append("AreaEffect{")
      .append(id)
      .append(" / ")
      .append(name)
      .append(", guardedLevel=")
      .append(guardedLevel)
      .append(", areaCopper=")
      .append(areaCopper);
    if (!"None".equals(song)) {
      sb.append(", song=")
        .append(song);
    }
    if (!"None".equals(ambient)) {
      sb.append(", ambient=")
        .append(ambient);
    }
    if (fogColor!=null) {
      sb.append(", fogColor=")
        .append(fogColor);
    }
    if (tintColor!=null) {
      sb.append(", tintColor=")
        .append(tintColor);
    }
    if (tintColor!=null) {
      sb.append(", tintColor=")
        .append(tintColor);
    }
    if (!"None".equals(particles)) {
      sb.append(", particles=")
        .append(particles);
    }
    if (!"None".equals(areaItems)) {
      sb.append(", areaItems=")
        .append(areaItems);
    }
    sb.append(", Origin=").append(origin)
      .append('}');
    return sb.toString();
  }
}
