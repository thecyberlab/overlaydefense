package CountingLogs;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.plaf.LabelUI;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountingWindowCounts {

    static String windowOutputpaths = "/media/user01/HDD_4TB/AK_GOOGLE_APKS/window_params_presence_vt";
    //static String toastOutPutPaths = "/home/ani0904071/Documents/thesis/overlay/output_folder/vt_window_all";
    //static String toastOutPutPaths = "/home/ani0904071/Documents/thesis/overlay/output_folder/gplay_window";

    public static void main(String[] args) {
        readJsonFiles(new File(windowOutputpaths));
    }
    static ArrayList<String> avoidFiles = new ArrayList<>();
    static ArrayList<String> overlayType = new ArrayList<>();

    static Set<String> overlayTypeApp =  new HashSet<>();

    static ArrayList<String> appOverlayTypeNo = new ArrayList<>();

    public static void readJsonFiles(File directory) {
        File[] files = directory.listFiles();

        avoidFiles.add("LockscreenManagerImpl.java");
        avoidFiles.add("BackupService.java");
        avoidFiles.add("BaseWebView.java");
        avoidFiles.add("AppCompatDelegateImplV7.java");
        avoidFiles.add("TooltipPopup.java");
        avoidFiles.add("AppCompatDelegateImplV9.java");
        avoidFiles.add("AppCompatDelegateImpl.java");
        avoidFiles.add("AndroidDocumentProvider.java");


        overlayType.add("2002");  // TYPE_PHONE
        overlayType.add("2003");  // TYPE_SYSTEM_ALERT
        overlayType.add("2005");  // TYPE_TOAST
        overlayType.add("2010");  // TYPE_SYSTEM_ERROR
        overlayType.add("2038");  // TYPE_APPLICATION_OVERLAY

        appOverlayTypeNo.add("56");
        appOverlayTypeNo.add("24");
        appOverlayTypeNo.add("65816");
        appOverlayTypeNo.add("1048");


        Map<Integer, LinkedList<String> > apksByView = new HashMap<>();
        Set<String> noOfApks = new HashSet<String>();
        ArrayList<String> fileNames = new ArrayList<>();



        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try {
                        String jsonContent = Files.readString(Paths.get(windowOutputpaths + File.separator + file.getName()));
                        JSONObject jsonObject = new JSONObject(jsonContent);
                        //System.out.println("fileName:" + file);
                        System.out.println(jsonObject.length());
                        for (String key : jsonObject.keySet()) {
                            String apkHash = key;
                            JSONArray windowInstances = jsonObject.getJSONArray(apkHash);
                            // Print the values in the array
                            for (int i = 0; i < windowInstances.length(); i++) {

                                JSONObject windowJSon = (JSONObject) windowInstances.get(i);

                                String fileName = windowJSon.get("fileName_").toString();
                                fileNames.add(fileName);
                                if(avoidFiles.contains(fileName.trim()))
                                    continue;

                                String wndowPropsStr = windowJSon.get("params_").toString();
                                String[] wndowProps = wndowPropsStr.split("####");
                                for (int w=0; w< wndowProps.length; w++) {
                                    int overlayTypeNo = processAndFindTouchParams(wndowProps[w]);
                                    apksByView.computeIfAbsent(overlayTypeNo, k -> new LinkedList<>()).add(apkHash); //O(n)
                                    if (overlayType.contains(overlayTypeNo +"")) {
                                        noOfApks.add(apkHash);
                                    }

                                }

                            }
                            //System.out.println("---------------------------------------------------------------------------------------------------");
                        }

                        //System.out.println("count" + count);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }



        // Create a HashMap to store the frequencies of elements
        //fileCounts(fileNames);

        // Using forEach method (Java 8 and later)
        summaryResults(apksByView, noOfApks);


        //apksByView.get(2002);

    }

    private static void summaryResults(Map<Integer, LinkedList<String>> apksByView, Set<String> noOfApks) {

        //apksByView.forEach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));
        System.out.println("No of unique apks:" + noOfApks.size());
        System.out.print("Total:");
        System.out.println( apksByView.get(2002).size() + apksByView.get(2003).size() + apksByView.get(2005).size() +
                apksByView.get(2010).size() + apksByView.get(2038).size() );

        System.out.println(apksByView.get(2002).size());
        System.out.println(apksByView.get(2003).size());
        System.out.println(apksByView.get(2005).size());
        System.out.println(apksByView.get(2010).size());
        System.out.println(apksByView.get(2038).size());


        //validating the counts thats'it it :)
        Set<String> uniqueValdiation = new HashSet<>();

        for (Integer key : apksByView.keySet()) {
            if (key  != -1 ) {
                System.out.println(key + ":" + apksByView.get(key));
                System.out.println("size: " + apksByView.get(key).size());
                System.out.println("apks: " + ":" + apksByView.get(key));
               // System.out.println();
                List<String> apks  = apksByView.get(key);
                StringBuilder apkName = new StringBuilder();
                for(String apkHash: apks) {
                    uniqueValdiation.add(apkHash);
                    apkName.append("'" + apkHash + "' ");
                }
                System.out.println("folders=(" + apkName.toString().trim() + ")"); //copy this... per file
                System.out.println();
                 //
            }

        }


        System.out.println(uniqueValdiation.size());



    }

    private static int processAndFindTouchParams(String wndowProps) {


        Pattern pattern = Pattern.compile("WindowManager.LayoutParams\\((.*?)\\);");
        Matcher matcher = pattern.matcher(wndowProps);
        String thirdParam = null;


        if (matcher.find()) {
            String paramsString = matcher.group(1);
            String[] params = paramsString.split(",\\s*");
            int paramI = 0;

            /*for (String param : params) {
               // System.out.println(param.trim());
                //int overlayParam = Integer.parseInt(param);
                if(paramI > 1) {
                    if (overlayType.contains(param.trim())) {
                         if(param.equals("2038")) {
                             thirdParam = param;
                             System.out.println("Found param: " + wndowProps);
                         }
                         return Integer.parseInt(param);
                    }
                }

                paramI++;

            }*/

            for (int s=0; s<params.length; s++) {

                if (s>1) {
                    if (overlayType.contains(params[s].trim())) {
                        if(params[s].equals("2038")) {
                            thirdParam = params[s];
                            if (appOverlayTypeNo.contains(params[s+1]))
                                return Integer.parseInt(params[s]);
                            else
                                return -1;
                            //overlayTypeApp.add(params[s+1]);
                            //System.out.println("Found param: " + wndowProps);
                        }
                        else
                            return Integer.parseInt(params[s]);

                    }

                }

            }
        } else {
            System.out.println("No WindowManager.LayoutParams found.");
        }

        return  -1 ;

    }

    private static void fileCounts(List<String> fileCounts) {
        Map<String, Integer> frequencyMap = new HashMap<>();

        // Iterate over the list and count the occurrences of each element
        for (String element : fileCounts) {
            frequencyMap.put(element, frequencyMap.getOrDefault(element, 0) + 1);
        }

        // Convert the map to a list of map entries
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(frequencyMap.entrySet());

        // Sort the list based on frequencies in descending order
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });

        // Print the frequencies in descending order
        for (Map.Entry<String, Integer> entry : entryList) {
            System.out.println("Element: " + entry.getKey() + ", Frequency: " + entry.getValue());
        }
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


}