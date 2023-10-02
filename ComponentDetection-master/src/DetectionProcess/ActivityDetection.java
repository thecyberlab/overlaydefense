package DetectionProcess;

import java.util.*;

public class ActivityDetection {

    private static Map<String, StringBuilder> focusedResult = new LinkedHashMap<>();

    public static void calculateActivity(LinkedHashSet<String> backStackOrderList, Map<String, LinkedList<Component>> backStack) {

        focusedActivityCalculation(backStackOrderList, backStack);
    }

    private static void focusedActivityCalculation(LinkedHashSet<String> orderedSet, Map<String, LinkedList<Component>> groupedObjects) {
        System.out.println("-------------------------------------------" + "processing" + "-----------------------------------------------");


        Map<String, String> analysedSummary = new HashMap<>();
        // Convert the LinkedHashSet to a List
        List<String> listTaskIDs = new ArrayList<>(orderedSet);


        for (Map.Entry<String, Boolean> entry: BackStackGeneration.trackToastTaskID.entrySet() ) {
            //System.out.println("#################################" + toastStackID + "#################################");
            String toastStackID = entry.getKey();
            LinkedList<Component> activitiesGroup = groupedObjects.get(toastStackID);

            int index =  listTaskIDs.indexOf(toastStackID);
            focusedResult.put(toastStackID, new StringBuilder()); //~~~
            focusedActivitybyFinishTypes(toastStackID, activitiesGroup);

            analysedSummary = printResult(toastStackID, analysedSummary);
            boolean afterToastScan = true;

            //if 0 is found in Toast stack
            if (index == 0 &&  analysedSummary.get("empty_stack") != null ) { // scan dowtowards

                while (afterToastScan || analysedSummary.get("non_empty_stack") == null) {
                    index++;
                    if (index == listTaskIDs.size() )
                        index = 0;
                    clearResults(analysedSummary, activitiesGroup);
                    afterToastScan = false;

                    String checkTaskID = listTaskIDs.get(index);
                    activitiesGroup = groupedObjects.get(checkTaskID);
                    focusedResult.put(checkTaskID, new StringBuilder()); //~~~~~
                    focusedActivitybyFinishTypes(checkTaskID, activitiesGroup);

                    analysedSummary = printResult(checkTaskID, analysedSummary);
                }
            } else if (index > 0   && analysedSummary.get("empty_stack") != null ) { //upper

                while (afterToastScan || analysedSummary.get("non_empty_stack") == null) {
                    index--;
                    if (index < 0 )
                        index = listTaskIDs.size()-1;
                    clearResults(analysedSummary, activitiesGroup);
                    afterToastScan = false;

                    String checkTaskId = listTaskIDs.get(index);
                    activitiesGroup = groupedObjects.get(checkTaskId);
                    focusedResult.put(checkTaskId, new StringBuilder()); //~~~~~
                    focusedActivitybyFinishTypes(checkTaskId, activitiesGroup);

                    analysedSummary = printResult(checkTaskId, analysedSummary);

                }

            }

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
                    if (finishType.equals("finish()")  || finishType.equals("finishAndRemoveTask()") ) {
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

    private static Map<String, String> printResult(String toastStackID, Map<String, String> analysedSummary) {
        //Map<String, String> analysedSummary;
        Map<Integer, String> analysisBlocks = processAnalysedString(focusedResult.get(toastStackID).toString());
        analysedSummary = checkAnalysedResult(analysisBlocks, toastStackID);
        if (analysedSummary.get("non_empty_stack") != null) {
            printFocusedActivity(toastStackID, analysisBlocks);
        }
        return analysedSummary;
    }

    private static void clearResults(Map<String, String> analysedSummary, LinkedList<Component> activitiesGroup) {
        analysedSummary.clear();
        focusedResult.clear();
        activitiesGroup.clear();
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

    private static String joinStringsInRang(String[] strings, int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex >= strings.length || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid range!");
        }

        return String.join(";;", Arrays.copyOfRange(strings, startIndex, endIndex + 1));
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
