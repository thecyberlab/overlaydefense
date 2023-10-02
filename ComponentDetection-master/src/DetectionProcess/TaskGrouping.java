package DetectionProcess;


import java.util.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskGrouping {
    private static String F = "finish";
    private static String FA = "finishAffinity";

    private static Map<String, Boolean> trackToastTaskID = new LinkedHashMap<>();

    private static LinkedHashSet<String> backStackOrderList = new LinkedHashSet<>(); //O(1)

    private static Map<String, StringBuilder> focusedResult = new LinkedHashMap<>();

    private static Map< String, LinkedList<Component> > backStack = new LinkedHashMap<>();


    public static void main(String[] args) {

        List<Component> activities = new ArrayList<>();

        //ex1
       /* activities.add(new Component("M", "49", new String[]{}, true, false, null, null, "pkg" , false, true, "activity"));
        activities.add(new Component("T", "49", new String[]{F},false, false, "M" , null, "pkg", true, false, "activity" ));
        activities.add(new Component("A0", "49", new String[]{F}, false, false, "T", null, "pkg", false, false, "activity" ));
        activities.add(new Component("A1", "50", new String[]{}, true, false, "A0",  null, "com.a1", false, false, "activity" ));
       // activities.add(new Component("S", "10000", new String[]{}, true, true, "A1", null, null, false, false, "activity" ));
        activities.add(new Component("A2", "49", new String[]{F, FA}, false, false, "A1", null, "pkg", false, false , "activity" ));*/

        activities.add(new Component("M", "49", new String[]{}, true, false, null, null, "pkg", false, true, "typeActivity" ));
        activities.add(new Component("D", "49", new String[]{}, false, false, "M", null, "pkg", true, false,"typeActivity" ));
        activities.add(new Component("T", "50", new String[]{}, false, false, "M", null, "com.ta", false, false,"typeActivity" )); // singletask affinity different
        activities.add(new Component("A", "50", new String[]{}, false, false, "T", null, "com.ta", false, false,"typeActivity" ));
        activities.add(new Component("B", "50", new String[]{FA}, false, false, "T", null, "com.ta", false, false,"typeActivity"));

        //exmple 2
        /*activities.add(new Component("M", "1538", new String[]{}, true, false, null, null, "pkg", false  ));
        activities.add(new Component("T", "1539", new String[]{FA}, true, false, "M", "com.t", "com.t", true  )); //  singleInstance affinity different
        activities.add(new Component("S", "10000", new String[]{}, true, true, "T", null, null, false ));
        activities.add(new Component("B", "1538", new String[]{}, false, false, "T" , null, "pkg", false ));
        activities.add(new Component("D", "1540", new String[]{F}, true, false, "T", "com.d", "com.d", false)); // singleTask, affinity d
        activities.add(new Component("C", "1538", new String[]{}, false, false, "B", null, "pkg", true));*/


        /*activities.add(new Component("M", "1629", new String[]{}, true, false, null, null, "package" ));
        activities.add(new Component("T", "1630", new String[]{"finish"}, true, false, "M", "com.t", "com.t" ));
        activities.add(new Component("A0", "1630", new String[]{}, true, false, "T", "com.to", "com.t"));
        activities.add(new Component("A1", "1630", new String[]{"finishAffinity"}, false, false, "A0", "com.to", "com.t"));*/

        /*activities.add(new Component("M", "355", new String[]{}, true, false, null, null, null, false));
        activities.add(new Component("D", "355", new String[]{}, false, false, "M", null, null, false ));
        activities.add(new Component("T", "356", new String[]{}, true, false, "M", "com.ta", "com.ta", false )); //singleTask
        activities.add(new Component("A", "356", new String[]{}, false, false, "T", "com.ta", "com.ta", false ));
        activities.add(new Component("B", "356", new String[]{FA}, false, false, "A", "com.ta", "com.ta", false  ));
        activities.add(new Component("C", "357", new String[]{FA}, false, false, "B", "com.tb", "com.ta", true ));
*/
        /////////////////////////////////////////////////////

        //for (Activity activity : activities) {
        for (int i = activities.size() - 1 ; i>=0; i-- ) {
            Component activity = activities.get(i);
            /*if(activity.isToastOverlayPresence())
                trackToastTaskID.put(activity.getTaskID(), true);*/
            backStack.computeIfAbsent(activity.getTaskID(), k -> new LinkedList<>()).add(activity); //O(n)
            //backStackOrderList.add(activity.getTaskID()); //O(1)
        }
        //Traversing or Iterating
        focusedActivityCalculation(backStack);

    }
    private static void focusedActivityCalculation(Map<String, LinkedList<Component>> groupedObjects) {

        System.out.println("-------------------------------------------" + "processing" + "-----------------------------------------------");


        Map<String, String> analysedSummary = new HashMap<>();


        for (Map.Entry<String, LinkedList<Component>> entry: groupedObjects.entrySet() ) {
            //System.out.println("#################################" + toastStackID + "#################################");
            String backStackID = entry.getKey();
            LinkedList<Component> activitiesGroup = groupedObjects.get(backStackID);
            focusedResult.put(backStackID, new StringBuilder());
            focusedActivitybyFinishTypes(backStackID, activitiesGroup);

            analysedSummary = printResult(backStackID, analysedSummary);
            if(analysedSummary.get("empty_stack") == null)
                return;
            clearResults(analysedSummary, activitiesGroup);

        }

    }

    private static void clearResults(Map<String, String> analysedSummary, LinkedList<Component> activitiesGroup) {
        analysedSummary.clear();
        focusedResult.clear();
        activitiesGroup.clear();
    }

    private static Map<String, String> printResult(String toastStackID, Map<String, String> analysedSummary) {
        //Map<String, String> analysedSummary;
        Map<Integer, String> analysisBlocks = processAnalysedString(focusedResult.get(toastStackID).toString());
        analysedSummary = checkAnalysedResult(analysisBlocks, toastStackID);
        if (analysedSummary.get("non_empty_stack") != null) {
            printFocusedActivity(toastStackID, analysisBlocks);
        }
        return analysedSummary;
    }

    private static void printFocusedActivity(String taskID, Map<Integer, String> analysisBlocks) {

        //System.out.println("Focused Result: " + toastStackID + " " + analysisBlocks);

        System.out.println("---------------------------");

        System.out.println("Focused Result Task: " + taskID + "-->");
        for (Map.Entry<Integer, String> resultEntry : analysisBlocks.entrySet()) {

            //String key = resultEntry.getKey();
            String val = resultEntry.getValue();
            System.out.println(val);
            /*String[] branches = val.split("\\, ");
            for (int b=0; b <branches.length; b++) {

            }*/
        }

        System.out.println("---------------------------");

    }

    private static Map<String, String> checkAnalysedResult(Map<Integer, String> analysisBlocks, String analysedStackID) {

        Map<String, String>  resultAnalysed = new HashMap<>();
        resultAnalysed.put("non_empty_stack", null);
        resultAnalysed.put("empty_stack", null);

        for (Map.Entry<Integer, String> entry : analysisBlocks.entrySet()) {

            String[] bits = entry.getValue().split(";;");
            String lastOne = bits[bits.length-1];
            String[] lastOneSize = lastOne.split(":");
            int size = Integer.parseInt(lastOneSize[lastOneSize.length - 1].trim());
            if(size > 0) {
                resultAnalysed.put("non_empty_stack", analysedStackID);
            } else if (size == 0) {
                resultAnalysed.put("empty_stack", analysedStackID);
            }

        }

        return resultAnalysed;

    }


    private static Map<Integer, String> processAnalysedString(String result) {

        String resultTrimmed = result.trim();
        Map<Integer, String> analysisBlocks = new LinkedHashMap<>();

        if (resultTrimmed.contains("Iteration") && resultTrimmed.contains("Focused Activity")) {

            boolean first = true;
            List<Integer> storeIndex = new ArrayList<>();
            String[] linesArray = resultTrimmed.split("\n");

            List<String> blocks = new ArrayList<>();
            StringBuilder blockBuilder = new StringBuilder();

            int counter = 0;

            for (int l = 0; l < linesArray.length ; l++) {
                if (linesArray[l].contains("Iteration") ) {
                    storeIndex.add(l);
                }
                else if (linesArray[l].contains("Focused") && linesArray[l-1].contains("Iteration")) {
                    blockBuilder.append(joinStringsInRang(linesArray, storeIndex.get(0), l));
                    analysisBlocks.put(counter, blockBuilder.toString());
                    blockBuilder.setLength(0);
                    storeIndex.clear();
                    counter++;
                    continue;
                }
            }
        } else {
            analysisBlocks.put(0, resultTrimmed);
        }

        return analysisBlocks;

    }


    private static String joinStringsInRang(String[] strings, int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex >= strings.length || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid range!");
        }

        return String.join(";;", Arrays.copyOfRange(strings, startIndex, endIndex + 1));
    }

    private static List<String> getBlocks(String input) {
        List<String> blocks = new ArrayList<>();

        Pattern pattern = Pattern.compile("Iteration:.+?(?=Iteration:|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String block = matcher.group().trim();
            blocks.add(block);
        }

        return blocks;
    }

    public static List<String> extractLinesAsBlock(String input, String string1, String string2) {
        String[] linesArray = input.split("\\n");
        List<String> blockLines = new ArrayList<>();

        boolean isString1Found = false;
        for (String line : linesArray) {
            if (!isString1Found && line.contains(string1)) {
                isString1Found = true;
            }

            if (isString1Found) {
                blockLines.add(line);
                if (line.contains(string2)) {
                    break;
                }
            }
        }

        return blockLines;
    }

    public static void printLinesWithNumbers(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            System.out.println((i + 1) + " -> " + lines.get(i));
        }
    }
    private static void focusedActivitybyFinishTypes(String taskID, LinkedList<Component> activitiesGroup) {

        for (int j = 0 ; j < activitiesGroup.size() /*&& !focusedAcivityFound.get()*/ ;  j++) {
            Component processingActivity = activitiesGroup.get(j);
            String[] processingActivityFinishTypes = processingActivity.getFinishTypes();
            //System.out.println("CurrentActivity: " + processingActivity.getName() + "-->" + Arrays.toString(processingActivity.getFinishTypes()));
            if (processingActivity.isOtherApp() == true) { //system app/otherAapp
                System.out.println("Focused Activity: " + processingActivity.getName() + ":" + activitiesGroup);
                focusedResult.put(taskID, focusedResult.get(taskID).append("Focused Activity: " + processingActivity.getName()+ ":" + activitiesGroup + ";;Size: " + activitiesGroup.size()).append("\n"));
                //focused.add("Focused Activity: " + processingActivity.getName() + ":" + activitiesGroup);
                return;
            }
            else if (processingActivityFinishTypes.length == 0 && processingActivity.isOtherApp() == false) { // last node but no finish types
                System.out.println("Focused Activity: " + processingActivity.getName()+ ":" + activitiesGroup);
                focusedResult.put(taskID, focusedResult.get(taskID).append("Focused Activity: " + processingActivity.getName()+ ":" + activitiesGroup + ";;Size: " + activitiesGroup.size()).append("\n"));
                //focused.add("Focused Activity: " + processingActivity.getName() + ":" + activitiesGroup);
                return;
            } else if (processingActivityFinishTypes.length != 0 && processingActivity.isOtherApp() == false) { //calculate movement
                for (int k= 0; k < processingActivityFinishTypes.length; k++) {
                    String finishType = processingActivityFinishTypes[k];
                    System.out.println(processingActivity.getName() + "----Iteration:" + k + "---->" + finishType);
                    focusedResult.put(taskID, focusedResult.get(taskID).append(processingActivity.getName() + "----Iteration:" + k + "---->" + finishType).append("\n"));
                    if (finishType.equals("finish")  || finishType.equals("finishAndRemoveTask") ) {
                        LinkedList<Component> copied = new LinkedList<>(activitiesGroup);
                        copied.remove();
                        if (copied.size() ==0) {
                            System.out.println("Focused Activity: " + copied);
                            focusedResult.put(taskID, focusedResult.get(taskID).append("Focused Activity: "  + copied +  ";;Size: " + copied.size()).append("\n"));
                            //continue; //
                            return;
                        }
                        focusedActivitybyFinishTypes(taskID, copied);
                        continue;
                        //System.out.println(copied);
                    } else if (finishType == "finishAffinity")  {
                        LinkedList<Component> copied = new LinkedList<>(activitiesGroup);
                        copied = removeActivity(processingActivity, copied);
                        focusedActivitybyFinishTypes(taskID, copied);
                        System.out.println("Focused Activity: " + copied);
                        focusedResult.put(taskID, focusedResult.get(taskID).append("Focused Activity: " + copied +  ";;Size: " + copied.size()).append("\n"));
                        return;
                        //continue;
                    }
                }
            }
        }
    }
    private static LinkedList<Component> removeActivity(Component processingActivity, LinkedList<Component> activitiesGroup) {
        String processingAffinity = reCheckAffinity(processingActivity);
        activitiesGroup.remove(); // only deleting the first one
        if(activitiesGroup.size() == 0)
            return activitiesGroup;
        Component nextActivity = activitiesGroup.peek();
        String nextAffinityString = reCheckAffinity(nextActivity);
        if (processingAffinity.equals(nextAffinityString)) {
            removeActivity(nextActivity, activitiesGroup);
            //return activitiesGroup;
        }
        return activitiesGroup;
    }
    private static String reCheckAffinity(Component activity) {
        if(activity.getAssignedAffinity() == null) {
            activity.setAssignedAffinity("null");
        } else if (activity.equals("")) {
            activity.setAssignedAffinity("empty");
        }
        return activity.getAssignedAffinity();
    }
}