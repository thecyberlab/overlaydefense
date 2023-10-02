package Automation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApkFolders {


    //static String toastOutPutPaths = "/media/user01/HDD_4TB/AK_GOOGLE_APKS/2023_toast_presence_vt";
    static String toastOutPutPaths = "/media/user01/HDD_4TB/AK_GOOGLE_APKS/2023_toast_presence"; //gplay


    public static void main(String[] args) {
        readJsonFiles(new File(toastOutPutPaths));
    }


    public static void readJsonFiles(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {

                    try {
                        String jsonContent = Files.readString(Paths.get(toastOutPutPaths + File.separator + file.getName()));
                        JSONObject jsonObject = new JSONObject(jsonContent);
                        //System.out.println("fileName:" + file);
                        System.out.println(jsonObject.length());
                        StringBuilder apkName = new StringBuilder();
                        for (String key : jsonObject.keySet()) {
                            String apkHash = key;
                            JSONArray toastInstances = jsonObject.getJSONArray(apkHash);
                            apkName.append("'" + apkHash + "' ");
                        }
                        System.out.println("folders=(" + apkName.toString().trim() + ")"); //copy this... per file
                        System.out.println("done with " + file.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }

}
