import java.net.URLDecoder;
import java.io.File;

public class Run
{
  public static void main(String... args)
  throws Exception
  {
    File runningJar = new File(Run.class.getProtectionDomain().getCodeSource().get‌Location().toURI());
    File runningDir = runningJar.getParentFile();

    String[] cmd = new String[] {
        "java",
        "-Djava.library.path=JARS",
        "-jar",
        "JARS/editor.jar",
        URLDecoder.decode(runningDir.getPath(), "UTF-8")
    };
    ProcessBuilder builder = new ProcessBuilder(cmd);
    builder.directory(runningDir.getParentFile());
    builder.redirectErrorStream(true);
    builder.inheritIO();
    Process p = builder.start();
    int ret = p.waitFor();
    System.exit(ret);
  }
}
