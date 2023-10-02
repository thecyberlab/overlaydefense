package CountingLogs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountingWndwManagerLogs {


    static String windowManagerToastPath =  "/media/user01/HDD_4TB/AK_GOOGLE_APKS/window_params_presence_test" ;

    public static void main(String[] args) {
        readJsonFiles(new File(windowManagerToastPath));
    }


    public static void readJsonFiles(File directory) {
    File[] files = directory.listFiles();

        ArrayList<String> apksWithBoth = new ArrayList<>();
        ArrayList<String> apksWithFNT = new ArrayList<>();
        ArrayList<String> apksWithFNF = new ArrayList<>();
        ArrayList<String> apksWithFullBlock = new ArrayList<>();

        if (files != null) {
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                try {
                    String jsonContent = Files.readString(Paths.get(windowManagerToastPath + File.separator + file.getName()));
                    JSONObject jsonObject = new JSONObject(jsonContent);
                    System.out.println("fileName:" + file);
                    System.out.println(jsonObject.length());

                    for (String key : jsonObject.keySet()) {
                        String apkHash = key;
                        JSONArray toastInstances = jsonObject.getJSONArray(apkHash);
                        // Print the values in the array
                        System.out.println("apkName:" + apkHash);
                        System.out.println("windowFound:" + toastInstances.length());
                        for (int i = 0; i < toastInstances.length(); i++) {
                            JSONObject windoPropJson = (JSONObject) toastInstances.get(i);
                            String windowViewNameProperties = null;

                            for (String keyT : windoPropJson.keySet()) {
                                if (keyT.contains("params")) {
                                    windowViewNameProperties = windoPropJson.get(keyT).toString().trim();

                                    if(windowViewNameProperties.contains("FLAG_NOT_TOUCHABLE") &&  windowViewNameProperties.contains("FLAG_NOT_FOCUSABLE")  ) {
                                        apksWithBoth.add(apkHash);
                                    } else if(windowViewNameProperties.contains("FLAG_NOT_TOUCHABLE") ) {
                                        apksWithFNT.add(apkHash);
                                    } else if(windowViewNameProperties.contains("FLAG_NOT_FOCUSABLE") ) {
                                        apksWithFNF.add(apkHash);
                                    }

                                    if (windowViewNameProperties.contains("FLAG_FULLSCREEN")) {
                                        apksWithFullBlock.add(apkHash);
                                    }


                                    /*if (toastViewNameProperties.contains(toastVarName + ".setView")) {
                                        apksSetView.add(apkHash);
                                        //gravityVariables.add(extractSetGravityFirstParam(toastViewNameProperties, "setView"));
                                    } else {
                                        apksWoSetView.add(apkHash);
                                        //gravityVariables.add(extractSetGravityFirstParam(toastViewNameProperties, null));

                                    }*/

                                }


                            }
                            //System.out.println(toastVarName + " <----------> " + toastViewNameProperties);
                        }
                        System.out.println("---------------------------------------------------------------------------------------------------");
                    }

                    //System.out.println("count" + count);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    System.out.println("Both FNF and FNT: " + apksWithBoth.size() );
    System.out.println("FNT: " + apksWithFNT.size() );
    System.out.println("FNF: " + apksWithFNF.size() );
    System.out.println("Full block: " + apksWithFullBlock.size() );

    //System.out.println(gravityVariables);

}

}
