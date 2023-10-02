package CountingLogs;


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

public class CountingToastLogs {


    //static String toastOutPutPaths = "/media/user01/HDD_4TB/AK_GOOGLE_APKS/2023_toast_presence_vt";
    static String toastOutPutPaths = "/media/user01/HDD_4TB/AK_GOOGLE_APKS/2023_toast_presence"; //gplay


    public static void main(String[] args) {
        readJsonFiles(new File(toastOutPutPaths));
    }


    public static void readJsonFiles(File directory) {
        File[] files = directory.listFiles();


        List<Integer> gravityVariables = new ArrayList<>();
        Set<String> apksSetView = new HashSet<>();
        Set<String> apksWoSetView = new HashSet<>();

        Map<Integer, LinkedList<String>> grpAPKsByView = new HashMap<>();


        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {

                    try {
                        String jsonContent = Files.readString(Paths.get(toastOutPutPaths + File.separator + file.getName()));
                        JSONObject jsonObject = new JSONObject(jsonContent);
                        System.out.println("fileName:" + file);
                        System.out.println(jsonObject.length());
                        for (String key : jsonObject.keySet()) {
                            String apkHash = key;
                            JSONArray toastInstances = jsonObject.getJSONArray(apkHash);
                            // Print the values in the array
                            System.out.println("apkName:" + apkHash);

                            System.out.println("toastFound:" + toastInstances.length());
                            for (int i = 0; i < toastInstances.length(); i++) {

                                JSONObject toastsJSON = (JSONObject) toastInstances.get(i);
                                String toastVarName = null;
                                String toastViewNameProperties = null;
                                String toastFilePath = null;

                                for (String keyT : toastsJSON.keySet()) {
                                    if (keyT.contains("variableName")) {
                                        toastVarName = toastsJSON.get(keyT).toString().split(";;;;;;")[0];
                                        toastViewNameProperties = toastsJSON.get(keyT).toString().split(";;;;;;")[1];
                                        if (toastViewNameProperties.contains(toastVarName + ".setView")) {
                                            apksSetView.add(apkHash);
                                        } else {
                                            apksWoSetView.add(apkHash);
                                        }

                                        int setViewNo = extractSetGravityFirstParam(toastViewNameProperties, "setView");
                                        gravityVariables.add(setViewNo);
                                        grpAPKsByView.computeIfAbsent(setViewNo, k -> new LinkedList<>()).add(apkHash); //O(n)

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
        //System.out.println(gravityVariables);
        countGravityProperties((ArrayList<Integer>) gravityVariables);
        System.out.println("setViewAPKs: " + apksSetView.size());
        System.out.println("setWOViewAPKs: " + apksWoSetView.size());
        //System.out.println("apks: " + grpAPKsByView);

        for (Map.Entry<Integer, LinkedList<String>> entry : grpAPKsByView.entrySet()) {
            Integer key = entry.getKey();
            List<String> values = entry.getValue();
            System.out.print(key + ": ");
            StringBuilder apkNameHash = new StringBuilder();
            for (String value : values) {
                System.out.print(value + ", ");
                apkNameHash.append("'" + value + "' ");
                //apkName.append( apkName.append("'" + value + "' "));

            }
            System.out.println("folders=(" + apkNameHash.toString().trim() + ")");
            System.out.println();
        }
    }

    private static void countGravityProperties(ArrayList<Integer> gravityVariables) {

        Map<Integer, Integer> countMap = new HashMap<>();
        int totalSumOfToasts = 0;

        // Count the occurrences of each integer in the ArrayList
        for (Integer num : gravityVariables) {
            totalSumOfToasts++;
            countMap.put(num, countMap.getOrDefault(num, 0) + 1);
        }

        // Sort the countMap in descending order of values (occurrences)
        List<Map.Entry<Integer, Integer>> sortedList = new ArrayList<>(countMap.entrySet());
        sortedList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // Print the counts in descending order
        for (Map.Entry<Integer, Integer> entry : sortedList) {
            System.out.println("Number: " + entry.getKey() + ", Count: " + entry.getValue());

        }

        System.out.println("Total no toasts: " + totalSumOfToasts);

    }

    public static int extractSetGravityFirstParam(String inputString, String setView) {

        if (setView == null)
            return -2; // no setView
        // Regular expression pattern to find setGravity and its first parameter
        String regex = "\\bsetGravity\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)";

        // Create a pattern object
        Pattern pattern = Pattern.compile(regex);

        // Create a matcher object
        Matcher matcher = pattern.matcher(inputString);

        // Find the first occurrence of the pattern
        if (matcher.find()) {
            // Get the first parameter of setGravity
            String firstParamString = matcher.group(1);

            // Parse the first parameter to an integer and return
            return Integer.parseInt(firstParamString);
        }

        // If setGravity is not found, return -1
        return -1;
    }

    public static void processJsonObject(JsonNode jsonObject) {
        if (jsonObject.isObject()) {
            System.out.println("Reading JSON object:");
            for (Iterator<String> it = jsonObject.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                JsonNode fieldValue = jsonObject.get(fieldName);
                System.out.println(fieldName + ": " + fieldValue);
            }
            System.out.println();
        } else {
            System.out.println("Not a JSON object: " + jsonObject);
        }
    }

    public static void processJsonArray(ArrayNode jsonArray) {
        // Process the JSON array elements
        for (JsonNode element : jsonArray) {
            // Process individual JSON objects within the array
            processJsonObject(element);
        }
    }


}
