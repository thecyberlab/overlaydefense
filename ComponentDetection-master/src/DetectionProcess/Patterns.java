package DetectionProcess;

import java.util.regex.Pattern;

public class Patterns {


    public static Pattern startActivityPattern = Pattern.compile("startActivity\\(.*?\\)");
    public static  Pattern startActivityForResultPattern = Pattern.compile("startActivityForResult\\(.*?\\)");
    public static Pattern startServicePattern = Pattern.compile("startService\\(.*?\\)");
    public static Pattern startServiceForeGroundPattern = Pattern.compile("startForegroundService\\(.*?\\)");
    public static Pattern patternStartAllComponent = Pattern.compile("\\b(startService|startActivityForResult|startActivity|startForegroundService)\\((.*?)\\)");

    public static Pattern newIntentPattern = Pattern.compile("\\bnew\\s+Intent\\b");

    public static Pattern classNamePattern = Pattern.compile("new\\s+Intent\\(.*?,\\s*(\\w+)\\.class\\)");

    public static Pattern nonNewPattern = Pattern.compile("(startActivity|startActivityForResult|startService|startForeGroundService)\\(([^\\)]+)\\)");


    public static Pattern patternClassName = Pattern.compile("\\bsetClassName\\s*\\(\\s*(.*?),\\s*\"(.*?)\"\\s*\\)");

    public static Pattern patternSetPackage = Pattern.compile("\\.setPackage\\(\"(.*?)\"\\)");

    public static Pattern patternSetComponent = Pattern.compile("new\\s+ComponentName\\s*\\(\\s*(.*?),\\s*(.*?)\\s*\\)");

   // public static  Pattern newIntentNoClass = Pattern.compile("new\\s+Intent\\((.*?)\\)");
   // public static  Pattern newIntentNoClass = Pattern.compile("new\\s+Intent\\(([^,]+),[^,]+\\)");
    public static  Pattern newIntentNoClass = Pattern.compile("new\\s+Intent\\(([^,]+),\\s*([^)]+)\\)");
}
