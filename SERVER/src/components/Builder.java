package components;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public abstract class Builder<T>
{
  public abstract void init(int id, String name, String origin);

  public abstract T build();

  private final static Pattern R_ID_NAME = Pattern.compile("### *([a-zA-Z]?[0-9]+) ?[^a-zA-Z]? ?(.*) *");
  private final static Pattern R_INT = Pattern.compile("[a-zA-Z ]*(-?[0-9]+)[^0-9]*");
  private final static Pattern R_FLOAT = Pattern.compile("(-?[0-9]+[.][0-9]+)[^0-9]*");

  protected static int parseInt(String str)
  {
    try {
      Matcher m = R_INT.matcher(str);
      if (m.find()) {
        return Integer.parseInt(m.group(1));
      }
    }
    catch (NumberFormatException | IllegalStateException ex) {
      System.err.println("[Builder] ERROR - Can't parseInt in \"" + str + "\": " + ex);
    }
    return 0;
  }

  protected static float parseFloat(String str)
  {
    try {
      Matcher m = R_FLOAT.matcher(str);
      if (m.find()) {
        return Float.parseFloat(m.group(1));
      }
    }
    catch (NumberFormatException | IllegalStateException ex) {
      System.err.println("[Builder] ERROR - Can't parseFloat in \"" + str + "\": " + ex);
    }
    return 0;
  }

  protected boolean isDuplicateAllowed(String setter)
  { return false; }

  protected  boolean set(String name, String value)
  { return false; }

  private static <T> void set(Builder<T> current, String lastSetter, String lastValue)
  throws InvocationTargetException, IllegalAccessException
  {
    boolean isMissing = true;
    if (current.set(lastSetter, lastValue)) {
      isMissing = false;
    }
    else if (lastValue!=null) {
      for (Method m : current.getClass().getMethods()) {
        if (lastSetter.equalsIgnoreCase(m.getName())
        && m.getParameterTypes().length == 1
        && m.getParameterTypes()[0] == String.class
        ) {
          m.invoke(current, lastValue);
          isMissing = false;
          break;
        }
      }
    }
    else {
      for (Method m : current.getClass().getMethods()) {
        if (lastSetter.equalsIgnoreCase(m.getName())
        && m.getParameterTypes().length == 0
        ) {
          m.invoke(current);
          isMissing = false;
          break;
        }
      }
    }
    if (isMissing) {
      throw new InvocationTargetException(new RuntimeException("Missing builder method: " + lastSetter));
    }
  }

  public static <T> void load(String filename, Class<? extends Builder<T>> builder, Map<Integer, T> ret)
  {
    System.err.println("[Builder] INFO - " + builder.getSimpleName() + " loading " + filename);
    String line = null;
    Builder<T> current = null;
    Integer currentId = null;
    String lastSetter = null;
    String lastValue = null;
    String lastOrigin = null;
    int line_num = 0;
    Set<String> already = new HashSet<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      while ((line = br.readLine()) != null) {
        ++ line_num;
        if (line.isEmpty() || line.charAt(0)=='-') {
          continue;
        }
        Matcher m = R_ID_NAME.matcher(line);
        if (m.find()) {
          if (current != null) {
            if (lastSetter!=null) {
              try {
                set(current, lastSetter, lastValue);
              }
              catch (InvocationTargetException ex) {
                System.err.println("[Builder] ERROR - New entry exception in "
                    + lastOrigin
                    + " - " + ex.getCause());
                //ex.printStackTrace();
              }
              lastSetter = null;
            }
            if (ret.put(currentId, current.build()) != null) {
              System.err.println("[Builder] ERROR - duplicate Id " + currentId
                  + " found in " + lastOrigin);
            }
          }
          currentId = parseInt(m.group(1));
          current = builder.newInstance();
          lastOrigin = filename + ':' + line_num;
          current.init(currentId, m.group(2), lastOrigin);
          already.clear();
        }
        else if (line.charAt(0) != ' ') {
          if (lastSetter != null) {
            try {
              set(current, lastSetter, lastValue);
            }
            catch (InvocationTargetException ex) {
              System.err.println("[Builder] ERROR - Value exception in +"
                  + lastOrigin
                  + " -> " + lastSetter + ": " + lastValue
                  + " - " + ex.getCause());
              ex.printStackTrace();
            }
            lastSetter = null;
          }
          if (line.contains(":")) {
            String[] parts = line.trim().split(":", 2);
            lastSetter = parts[0].trim();
            lastValue = parts[1].trim();
          }
          else {
            lastSetter = line.trim();
            lastValue = null;
          }
          if (already.contains(lastSetter) && !current.isDuplicateAllowed(lastSetter)) {
            System.err.println("[Builder] ERROR - duplicate \"" + lastSetter
                + "\" found in " + lastOrigin);
          } else {
            already.add(lastSetter);
          }
        }
        else { // multi-line value
          lastValue = lastValue + "\n" + line.trim();
        }
      }

      if (current != null) {
        if (lastSetter!=null) {
          try {
            set(current, lastSetter, lastValue);
          }
          catch (InvocationTargetException ex) {
            System.err.println("[Builder] ERROR - New entry exception in "
                + lastOrigin
                + " - " + ex.getCause());
            //ex.printStackTrace();
          }
        }
        if (ret.put(currentId, current.build()) != null) {
          System.err.println("[Builder] ERROR - duplicate Id " + currentId
              + " found in " + lastOrigin);
        }
      }
    }
    catch (InstantiationException ex) {
      System.err.println("[Builder] ERROR - Can't instantiate: " + builder);
    }
    catch (IllegalAccessException ex) {
      System.err.println("[Builder] ERROR - Can't call method: " + builder.getSimpleName() + "." + lastSetter);
    }
    catch (FileNotFoundException ex) {
      System.err.println("[Builder] ERROR - File not found: " + filename);
    }
    catch (IOException ex) {
      System.err.println("[Builder] ERROR - Can't read file: " + filename);
      ex.printStackTrace();
    }
  }
}
