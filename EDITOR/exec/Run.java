import java.net.URLDecoder;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Run
{
  public static void main(String... args)
  throws Exception
  {
    File runningJar = new File(Run.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    File runningDir = runningJar.getParentFile();

    String[] cmd = new String[] {
        "java",
        "-Djava.library.path=JARS",
        "-jar",
        "JARS/editor.jar",
        URLDecoder.decode(runningDir.getPath(), "UTF-8")
    };

    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    File log = new File("editor-" + date + ".log");
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
