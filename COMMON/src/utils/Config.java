package utils;

import java.util.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Config {
  static Map<String, String> readConfig(String path) throws FileNotFoundException, IOException {
    Map<String, String> ret = new TreeMap<>();

    FileInputStream fstream = new FileInputStream(path);
    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

    String strLine;

    //Read File Line By Line
    while ((strLine = br.readLine()) != null) {
      if ("".equals(strLine) || strLine.charAt(0) == '#') {
        continue;
      }
      String[] parts = strLine.split("=");
      if (parts.length == 2) {
        ret.put(parts[0].trim(), parts[1].trim());
      }
      else if (strLine.contains("=")) {
        ret.put(parts[0].trim(), null);
      }
      else {
        throw new IllegalArgumentException("No = in this line: \""+parts.length + strLine + '"');
      }
    }

    //Close the input stream
    br.close();

    return ret;
  }

  static <T> void assignConfig(Map<String, String> config, Class<T> configClass)
      throws IllegalAccessException {
    Map<String, Field> fields = new HashMap<>();
    Field[] declaredFields = configClass.getDeclaredFields();
    for (Field f : declaredFields) {
      if (Modifier.isStatic(f.getModifiers())) {
        fields.put(f.getName(), f);
      }
    }
    for (Map.Entry<String, String> ent : config.entrySet()) {
      Field f = fields.get(ent.getKey());
      if (f == null) {
        throw new IllegalArgumentException("Not a config field: \"" + ent.getKey() + '"');
      }

      String type = f.getType().getSimpleName();
      System.out.println("[Config] " + type + " " + f.getName() + " = " + ent.getValue());

      if ("String".equals(type)) {
        f.set(null, ent.getValue());
      } else if ("boolean".equals(type)) {
        boolean val = Boolean.parseBoolean(ent.getValue());
        f.setBoolean(null, val);
      } else if ("byte".equals(type)) {
        try {
          byte val = Byte.parseByte(ent.getValue());
          f.setByte(null, val);
        } catch (NumberFormatException ex) {
          throw new IllegalArgumentException(
              "Not a byte value: " + ent.getKey() + " = " + ent.getValue());
        }
      } else if ("char".equals(type)) {
        String val = ent.getValue();
        if (val.length() == 1) {
          f.setChar(null, val.charAt(0));
        } else {
          throw new IllegalArgumentException(
              "Not a char value: " + ent.getKey() + " = " + ent.getValue());
        }
      } else if ("double".equals(type)) {
        try {
          double val = Double.parseDouble(ent.getValue());
          f.setDouble(null, val);
        } catch (NumberFormatException ex) {
          throw new IllegalArgumentException(
              "Not a byte value: " + ent.getKey() + " = " + ent.getValue());
        }
      } else if ("float".equals(type)) {
        try {
          float val = Float.parseFloat(ent.getValue());
          f.setFloat(null, val);
        } catch (NumberFormatException ex) {
          throw new IllegalArgumentException(
              "Not a float value: " + ent.getKey() + " = " + ent.getValue());
        }
      } else if ("int".equals(type)) {
        try {
          int val = Integer.parseInt(ent.getValue());
          f.setInt(null, val);
        } catch (NumberFormatException ex) {
          throw new IllegalArgumentException(
              "Not a int value: " + ent.getKey() + " = " + ent.getValue());
        }
      } else if ("long".equals(type)) {
        try {
          long val = Long.parseLong(ent.getValue());
          f.setLong(null, val);
        } catch (NumberFormatException ex) {
          throw new IllegalArgumentException(
              "Not a long value: " + ent.getKey() + " = " + ent.getValue());
        }
      } else if ("short".equals(type)) {
        try {
          short val = Short.parseShort(ent.getValue());
          f.setShort(null, val);
        } catch (NumberFormatException ex) {
          throw new IllegalArgumentException(
              "Not a short value: " + ent.getKey() + " = " + ent.getValue());
        }
      } else {
        throw new IllegalArgumentException(
            "Unsupported type field type: " + type + " " + f.getName());
      }
    }
  }

  public static <T> void configure(Class<T> configClass, String path) {
    if (!path.endsWith("/")) {
      path += '/';
    }
    String configFile = path + configClass.getSimpleName() + ".cfg";
    try {
      Map<String, String> config = readConfig(configFile);
      config.put("PATH", path);
      assignConfig(config, configClass);
    } catch (FileNotFoundException ex) {
      throw new RuntimeException("File not found: \"" + configFile + '"');
    } catch (IllegalAccessException ex) {
      throw new RuntimeException("Invalid config.txt", ex);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
