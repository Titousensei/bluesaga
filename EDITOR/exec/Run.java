import java.net.URLDecoder;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public class Run
{
  public static final String MIN_VERSION = "1.8";

  public static void main(String... args)
  throws Exception
  {
    String version = Runtime.class.getPackage().getImplementationVersion();
    System.out.println("Java version detected: " + version);

    if (MIN_VERSION.compareTo(version) > 0) {
      JOptionPane.showMessageDialog(null,
          "This program requires Java " + MIN_VERSION
              + " or above.\nPlease download the latest version at http://www.java.com/",
          "Info: Java Version mismatch",
          JOptionPane.WARNING_MESSAGE);
      System.exit(-1);
    }

    File runningJar = new File(Run.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    File runningDir = runningJar.getParentFile();

    String fullName = runningJar.getName();
    System.out.println("Jar name: "+ fullName);
    if (!fullName.startsWith("run-")) {
      JOptionPane.showMessageDialog(null,
          "Specify what to run by renaming this jar to either run-editor.jar or run-server.jar.",
          "Info: Jar name not recognized",
          JOptionPane.WARNING_MESSAGE);
      System.exit(-1);
    }
    int cut = fullName.length() - 4;
    String name = fullName.substring(0, cut).substring(4);
    System.out.println("Exec name: "+ name);

    String[] cmd = new String[] {
        "java",
        "-Djava.library.path=JARS",
        "-jar",
        "JARS/" + name + ".jar",
        URLDecoder.decode(runningDir.getPath(), "UTF-8")
    };

    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    File log = new File(name + "-" + date + ".log");
    ProcessBuilder builder = new ProcessBuilder(cmd);
    builder.directory(runningDir.getParentFile());
    builder.redirectErrorStream(true);
    builder.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
    //builder.inheritIO();
    Process p = builder.start();
    int ret = p.waitFor();
    System.exit(ret);
  }
}
