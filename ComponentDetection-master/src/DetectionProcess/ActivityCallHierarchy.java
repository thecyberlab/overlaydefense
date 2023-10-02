package DetectionProcess;


import com.github.javaparser.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;

public class ActivityCallHierarchy {


    private static String manifestFilesDir =  "/media/user01/HDD_4TB/AK_GOOGLE_APKS/output_vt_wndow_manifest/2003";

    private static String decompiledPath = "/media/user01/HDD_4TB/AK_GOOGLE_APKS/vt_filtered_window/2003";

    private static  String launcherActivity = null;
    private static  String processingApkName = null;
    

    private static String packageIdJadx = "BuildConfig.APPLICATION_ID";

    public  static  List<Component> componentGenerationStates = new ArrayList<>();
    private  static  Set<String> uniquePairsOfCalls = new HashSet<>();

    private static String fileExtension = ".java";

    // Regular expression pattern to match the desired finish patterns
    static Pattern patternFinish = Pattern.compile("\\b(finish\\(\\)|finishAndRemoveTask\\(\\)|finishAfterTransition\\(\\)|finishAffinity\\(\\))\\b");


    private static String packageName = null;
    public  static  List<Component> initialStorage = new ArrayList<>();

    private static Queue<String> queue = new LinkedList<>();

    public static void main(String[] args) {

        File directory = new File(manifestFilesDir);


        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {

                System.out.println("--------Reading: " + file + "--------");
                readManifest(file.toString());
                System.out.println(launcherActivity);

                System.out.println("--------component creation done--------");

                //String fileName = parseFileName(launcherActivity);
                String fileName = launcherActivity;
                queue.add(fileName);
                componentGenerationStates.add(CreateAndUpdateComponents.createListObject(fileName, initialStorage, packageName));

                while (!queue.isEmpty()) {
                    fileName = queue.poll();
                    readFileFromDirectory(fileName);
                }

                //2nd step: backStack generation
                System.out.println(componentGenerationStates);
                BackStackGeneration.generateBackStacks(componentGenerationStates);


                //3rd step


                System.out.println("FINISHED processing XXXXX--"+ processingApkName + "--XXXXX");
                System.out.println();
                System.out.println();
                //nullify all
                launcherActivity = null;
                processingApkName = null;
                componentGenerationStates.clear();
                initialStorage.clear();
                uniquePairsOfCalls.clear();


            }
        }

    }


    private static void readManifest(String manifestPathFile) {

        try {
            String jsonContent = Files.readString(Paths.get(manifestPathFile));
            JSONObject jsonObject = new JSONObject(jsonContent);
            JSONObject componentDetails  = (JSONObject) jsonObject.get("componentDetails");
            packageName = componentDetails.get("packageName").toString();
            processingApkName =  jsonObject.get("apkName").toString();

            System.out.println("packageName: " + packageName);
            System.out.println("processingApkName: " + processingApkName);

            createComponentObjects(componentDetails);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void createComponentObjects(JSONObject componentDetails) {

        JSONArray activityDetails = (JSONArray) componentDetails.get("activities");
        JSONArray serviceDetails = (JSONArray) componentDetails.get("services");

        for (int a = 0, size = activityDetails.length(); a < size; a++)
        {
            createObject(activityDetails.getJSONObject(a), "typeActivity");
        }

        for (int s = 0, size = serviceDetails.length(); s < size; s++)
        {
            createObject(serviceDetails.getJSONObject(s), "typeService");
        }

    }


    /** https://stackoverflow.com/a/15526849/2634103
     *
     * @param componentNode
     * @param componentType
     */
    private static void createObject(JSONObject componentNode, String componentType) {

        String componentName = String.valueOf(componentNode.get("name")).replaceAll("\"+$", "");

        String taskID = null;
        String generatedAffinity = null;
        Boolean backStackCreated = false;
        String assignedAffinity = null;
        JSONArray intentFilters = null;
        Boolean main = false;
        Boolean laucnher = false;
        Boolean defaultProp = false;
        Boolean toastPresence = false;
        String[] finishTypes = null;
        String parentActivity = null;
        Boolean isOtherApp = false;
        Boolean isLauncher = false;

        if(componentType.equals("typeActivity")) {
            assignedAffinity = String.valueOf(componentNode.get("taskAffinity"));
            if(assignedAffinity.equals("null"))
                assignedAffinity = null;
            intentFilters = (JSONArray) componentNode.get("intentFilters");

            for (int j = 0, size = intentFilters.length(); j < size; j++) {

                JSONObject intnFltrObject = intentFilters.getJSONObject(j);
                // System.out.println(intnFltrObject);
                JSONArray actions = (JSONArray) intnFltrObject.get("actions");
                for (Object action : actions) {
                    if(action.toString().contains("MAIN"))
                        main = true;
                }

                JSONArray categories = (JSONArray) intnFltrObject.get("categories");
                for (Object category : categories) {
                    if(category.toString().contains("LAUNCHER"))
                        laucnher = true;
                    if(category.toString().contains("DEFAULT"))
                        defaultProp = true;
                }
            }

            if(main && laucnher )
                isLauncher = true;
            else if (main && laucnher && defaultProp)
                isLauncher = true;

            if (isLauncher) {
                launcherActivity = componentName;
                if(assignedAffinity == null)
                    generatedAffinity = "packageAffinity";
                taskID = "49"; //random
            }
        } else if(componentType.equals("typeService")) {
            assignedAffinity = null;
        }


        initialStorage.add(new Component(componentName, taskID, finishTypes, backStackCreated, isOtherApp, parentActivity,
                assignedAffinity, generatedAffinity, toastPresence, isLauncher, componentType ));

    }

    private static String parseFileName(String packageName) {

        String parseFileName = null;

        int lastDotIndex = packageName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < packageName.length() - 1) {
            parseFileName= packageName.substring(lastDotIndex + 1);
            System.out.println("Last Segment: " + parseFileName);
        } else {
            System.out.println("Invalid class name.");
        }


        return parseFileName;
    }

    private static String replaceDotsWithFileSeparator(String input) {
        return input.replace(".", File.separator);
    }

    private static void readFileFromDirectory(String fileName) {

        Pair<String, String> fileInfo = separateFileName(fileName);
        String pathOfFile = replaceDotsWithFileSeparator(fileInfo.a);
        String nameOfFile = fileInfo.b;

        try {
            Path filePath = searchFile(Paths.get(decompiledPath)  +File.separator+ processingApkName +  File.separator+ "sources"+ File.separator + pathOfFile,  nameOfFile +  fileExtension);
            if (filePath != null) {
                System.out.println("File found: " + filePath);
                // Read the contents of the file
                String fileContents = Files.readString(filePath);
                // Create a matcher for the input string

                String[] finishTypes = parseFinishPatterns(fileContents);

                //finish
                if(finishTypes.length> 0)
                    CreateAndUpdateComponents.updateListActivityFinishTypes(fileName, finishTypes);
                else CreateAndUpdateComponents.updateListActivityFinishTypes(fileName, finishTypes);

                //toastOverlay
                Boolean toastOverlayPresence = OverlayToastMatcher.overlayToastMatcher(fileContents);
                Boolean windowOverlayPresence = OverlayToastMatcher.overlayWindowMatcher(fileContents);
                if (toastOverlayPresence || windowOverlayPresence)
                    updateListObject(fileName, "overlayPresence");

                //intent parsing
                parseIntents(fileName, fileContents);


                //System.out.println("File contents:\n");
            } else {
                System.out.println("File not found: " + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Pair<String, String> separateFileName(String inputString) {
        String fileDirectory, fileName;
        int lastDotIndex = inputString.lastIndexOf(".");
        if (lastDotIndex != -1) {
            fileDirectory = inputString.substring(0, lastDotIndex);
            fileName = inputString.substring(lastDotIndex + 1);
        } else {
            // No dot found, consider the whole input as the file name
            fileDirectory = "";
            fileName = inputString;
        }

        return new Pair<>(fileDirectory, fileName);
    }

    private static void parseIntents(String currentFileName, String fileContents) {


        String[] lines = fileContents.split("\n");



        Map<Integer, String> intentMap = new LinkedHashMap<>();
        System.out.println("Intent searching under start.....");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (Patterns.startActivityPattern.matcher(line).find() || Patterns.startActivityForResultPattern.matcher(line).find()
                    || Patterns.startServicePattern.matcher(line).find() || Patterns.startServiceForeGroundPattern.matcher(line).find()) {

                Matcher matcher = Patterns.patternStartAllComponent.matcher(line);

                if (matcher.find()) {
                    String functionName = matcher.group(1);
                    String parameter = matcher.group(2).trim();


                    if (!parameter.isEmpty()) {
                        System.out.println("Function Name: " + functionName);
                        System.out.println("Parameter: " + parameter);

                        if(parameter.contains("new Intent")) {
                            intentMap.put(i + 1, line);
                        } else {
                            boolean intenTypeFound = false;
                            int tempLine  = i;
                            while (!intenTypeFound) {
                                if (lines[tempLine].trim().contains(parameter) && lines[tempLine].trim().startsWith("Intent") ) {
                                    intenTypeFound = true;
                                    intentMap.put(i + 1, line);
                                }
                                tempLine--;
                            }
                        }

                    } else {
                        System.out.println("No parameter found for " + functionName + " function.");
                    }
                } else {
                    System.out.println("No matching intent function with Intent parameter found at line: "+ i);
                }


            }
        }

        // Print the line number and the corresponding startActivity() or startActivityForResult() line
        for (Map.Entry<Integer, String> entry : intentMap.entrySet()) {
            System.out.println("Line " + entry.getKey() + ": " + entry.getValue());
            int line = entry.getKey();
            String startString = entry.getValue();

            findIntentBlocks(currentFileName, line, startString , fileContents);
        }

    }

    private static void findIntentBlocks(String currentFileName, int line, String startString, String fileContents) {


        Matcher newIntentMatcher = Patterns.newIntentPattern.matcher(startString);

        String className = null;

        if (newIntentMatcher.find()) { // explicit class Name //startActivity(new Intent(this, ToastActivity.class));
            Matcher classNameMatcher = Patterns.classNamePattern.matcher(startString);

            if (classNameMatcher.find()) {
                className = classNameMatcher.group(1);
                //queue.add(className);
                if (!checkDuplicatePairPresence(currentFileName, className)) {
                    componentGenerationStates.add(CreateAndUpdateComponents.createListObject(className, initialStorage, packageName));
                    CreateAndUpdateComponents.updateListActivityParent(currentFileName, className);
                    addComponentToQ(currentFileName, componentGenerationStates);
                }
            } else {

                Matcher nonClassMatcher = Patterns.newIntentNoClass.matcher(startString);
                if (nonClassMatcher.find()) {
                    String appPackageName = nonClassMatcher.group(1);
                    className = nonClassMatcher.group(2);
                    if (!checkDuplicatePairPresence(currentFileName, className)) {
                        componentGenerationStates.add(CreateAndUpdateComponents.createListObject(className, initialStorage, packageName));
                        CreateAndUpdateComponents.updateListActivityParent(currentFileName, className);
                        if(appPackageName.contains(packageName) || appPackageName.equals(packageIdJadx)) {
                            addComponentToQ(currentFileName, componentGenerationStates);
                        }

                    }

                } else {
                    System.out.println("No new Intent found.");
                }

            }

        } else { // not new param..

            //Extract parameters from startActivity and startActivityForResult
            Matcher nonNewIntentMatcher = Patterns.nonNewPattern.matcher(startString);

            String  parameter = null;
            while (nonNewIntentMatcher.find()) {
                String methodName = nonNewIntentMatcher.group(1);
                parameter = nonNewIntentMatcher.group(2);
                System.out.println(methodName + " parameter: " + parameter);
            }

            String[] lines = fileContents.split("\n");
            StringBuilder sbIntentBlock = new StringBuilder();
            //sbIntentBlock.append(startString);

            for (int i = line; i >= 0; i--) {
                String linePrev = lines[i];
                // System.out.println("Reading line: " + i);

                newIntentMatcher = Patterns.newIntentPattern.matcher(linePrev);
                if (newIntentMatcher.find() && linePrev.contains(parameter)) {

                    Matcher classNameMatcher = Patterns.classNamePattern.matcher(linePrev); //classParsingExplicit
                    if (classNameMatcher.find()) {  // new Intent(this, ToastActivity.class)
                        System.out.println("look for explicit intents 2nd parameter parse className");
                        className = classNameMatcher.group(1);
                        //queue.add(className);
                        if (!checkDuplicatePairPresence(currentFileName, className)) {
                            componentGenerationStates.add(CreateAndUpdateComponents.createListObject(className, initialStorage, packageName));
                            CreateAndUpdateComponents.updateListActivityParent(currentFileName, className);
                            addComponentToQ(currentFileName, componentGenerationStates);
                        }


                        System.out.println(className);
                    } else { //check the StringBuilder Explicit intent Component and SetPackage
                        String blockString = sbIntentBlock.toString();
                        String[] intentLines = blockString.split("\n");

                        if (!blockString.contains("setClassName") && !blockString.contains("setPackage") && !blockString.contains("setComponent")) {
                            System.out.println("looking for Implicit intents");
                            if (!checkDuplicatePairPresence(currentFileName, className)) {
                                componentGenerationStates.add(CreateAndUpdateComponents.createListObject("implicitIntent", initialStorage, packageName));
                                CreateAndUpdateComponents.updateListActivityParent(currentFileName, "implicitIntent");
                            }// Your logic here
                        } else {
                            //explicit block
                            System.out.println("look for explicit intents");
                            if (blockString.contains("setClassName")) {

                                for (int k = intentLines.length -1 ; k >= 0; k--) {
                                    String intentLine  = intentLines[k];
                                    // Regex pattern to match setClassName and extract the second parameter's class name

                                    Matcher matcherClassName = Patterns.patternClassName.matcher(intentLine);
                                    if (matcherClassName.find()) {
                                        String appPackageName = matcherClassName.group(1);
                                        className = removeFirstAndLastQuotes(matcherClassName.group(2));
                                        if(appPackageName.contains(packageName) || appPackageName.equals(packageIdJadx)) { //sameApp
                                            //queue.add(className);
                                            if (!checkDuplicatePairPresence(currentFileName, className)) {
                                                componentGenerationStates.add(CreateAndUpdateComponents.createListObject(className, initialStorage, packageName));
                                                CreateAndUpdateComponents.updateListActivityParent(currentFileName, className);
                                                addComponentToQ(currentFileName, componentGenerationStates);
                                            }
                                        } else { //differentApp
                                            if (!checkDuplicatePairPresence(currentFileName, className)) {
                                                componentGenerationStates.add(CreateAndUpdateComponents.createListObject(appPackageName, initialStorage, packageName));
                                                CreateAndUpdateComponents.updateListActivityParent(currentFileName, appPackageName);
                                            }
                                        }
                                        //className = parseFileName(matcherClassName.group(1));

                                        System.out.println(className);
                                        break;
                                    }
                                }

                            } // setClassName
                            else if (blockString.contains("setPackage")) {
                                System.out.println("looking for setPackage");
                                for (int k = intentLines.length -1 ; k >= 0; k--) {
                                    String intentLine  = intentLines[k];

                                    Matcher matcherSetPackage = Patterns.patternSetPackage.matcher(intentLine);
                                    if (matcherSetPackage.find()) {
                                        String appPackageName = matcherSetPackage.group(1);
                                        //if(!appPackageName.contains(packageName)) { //otherApp
                                        //queue.add(className);
                                        if (!checkDuplicatePairPresence(currentFileName, className)) {
                                            componentGenerationStates.add(CreateAndUpdateComponents.createListObject(appPackageName, initialStorage, packageName));
                                            CreateAndUpdateComponents.updateListActivityParent(currentFileName, appPackageName);
                                            if (!appPackageName.contains(packageName))
                                                addComponentToQ(currentFileName, componentGenerationStates);
                                            //}
                                        }
                                        break;
                                    }

                                }

                            } // setPackage
                            else if (blockString.contains("setComponent") ) {
                                System.out.println("looking for setComponent");
                                for (int k = intentLines.length -1 ; k >= 0; k--) {
                                    String intentLine  = intentLines[k];
                                    // Regex pattern to match setClassName and extract the second parameter's class name

                                    Matcher matcherSetComponent = Patterns.patternSetComponent.matcher(intentLine);
                                    if (matcherSetComponent.find()) {
                                        String appPackageName = matcherSetComponent.group(1);
                                        className = removeFirstAndLastQuotes(matcherSetComponent.group(2));
                                        /*if(appPackageName.contains(packageName)) {
                                             queue.add(className);
                                        }*/
                                        componentGenerationStates.add(CreateAndUpdateComponents.createListObject(className, initialStorage, packageName));
                                        CreateAndUpdateComponents.updateListActivityParent(currentFileName, className);
                                        if(appPackageName.contains(packageName) || appPackageName.equals(packageIdJadx)) {
                                            addComponentToQ(currentFileName, componentGenerationStates);
                                        }

                                        break;
                                    }
                                }
                            } //setComponent
                        }




                    } // else prev

                    break;
                }
                else  {
                    sbIntentBlock.append(linePrev.trim()).append("\n");
                }

            }

        }





    }

    private static boolean checkDuplicatePairPresence(String currentFileName, String className) {

        if(uniquePairsOfCalls.contains(currentFileName + "-->" + className)) {
            return true;
        } else {
            uniquePairsOfCalls.add(currentFileName + "-->" + className);
        }
        
        return false;

    }

    private static String removeFirstAndLastQuotes(String input) {

        if (input == null || input.isEmpty()) {
            return input;
        }

        // Check if the first and last characters are quotes
        if (input.charAt(0) == '"' && input.charAt(input.length() - 1) == '"') {
            // If they are quotes, remove them and return the modified string
            return input.substring(1, input.length() - 1);
        } else {
            // If no quotes at the beginning and end, return the original string
            return input;
        }
    }

    private static void addComponentToQ(String currentFileName, List<Component> componentGenerationStates) {



        int lastIndex =  componentGenerationStates.size()-1 ;
        Component component = componentGenerationStates.get(lastIndex);
        String cName = component.getName();

        if (!cName.equals(currentFileName)) {
            queue.add(cName);
            System.out.println("Added to Processing Q:" +  cName);
            //update
            CreateAndUpdateComponents.updateTaskID(currentFileName, component, initialStorage, componentGenerationStates);
        }
        else  {
            componentGenerationStates.remove(lastIndex); // if same name from same class/ dont need to add
        }

    }

    private static void updateListActivityFinishTypes(String fileName, String type, String[] finishTypes) {

        switch (type) {
            case "finishTypes":
            for (int i = 0; i < initialStorage.size(); i++) {
                Component currentActivity = initialStorage.get(i);
                String activityName = currentActivity.getName();
                if (activityName.equals(packageName + "." + fileName)) {
                    currentActivity.setFinishTypes(finishTypes);
                    break;
                }
            }

            break;
        }
    }

    private static void updateListObject(String fileName, String type) {


        switch (type) {
            case "overlayPresence":
                for(int i = 0; i < ActivityCallHierarchy.componentGenerationStates.size(); i++) {
                    Component currentActivity = ActivityCallHierarchy.componentGenerationStates.get(i);
                    String activityName = currentActivity.getName();
                    if (activityName.equals(fileName)) {
                        currentActivity.setIsToastOverlayPresence(true);
                        break;
                    }
                }
                break;
        }

    }

    private static void parseToastOverlay(String fileContents) {

        String toastRegex = "([\\w\\d]+)\\s*=\\s*((new\\s+)?Toast\\(\\)|Toast\\.makeText\\(.*?\\));\\s*\\1\\.(setView|setDuration|setGravity)\\(.*?\\);\\s*\\1\\.show\\(\\);";

        Pattern pattern = Pattern.compile(toastRegex);
        Matcher matcher = pattern.matcher(fileContents);

        while (matcher.find()) {
            String toastVariable = matcher.group(1);
            String methodCall = matcher.group(4);
            System.out.println("Found toast overlay presence:");
            System.out.println("Toast Variable: " + toastVariable);
            System.out.println("Method Call: " + methodCall);
        }



    }

    private static String[] parseFinishPatterns(String input) {

        Set<String> finishPatterns = new HashSet<>();

        // Convert the input to lowercase for case-insensitive matching
        //input = input.toLowerCase();

        // Define the finish patterns to search for
        String[] patterns = {"finish()", "finishAndRemoveTask()", "finishAfterTransition()", "finishAffinity()"};

        // Iterate over the patterns and find occurrences in the input
        for (String pattern : patterns) {
            int index = input.indexOf(pattern);
            while (index != -1) {
                finishPatterns.add(pattern);
                index = input.indexOf(pattern, index + pattern.length());
            }
        }

//        System.out.println(finishPatterns);

        return finishPatterns.toArray(new String[finishPatterns.size()]);

    }

    private static Path searchFile(Path directoryPath, String fileName) throws IOException {
        if (Files.isDirectory(directoryPath)) {
            try (var stream = Files.walk(directoryPath)) {
                return stream
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .findFirst()
                        .orElse(null);
            }
        }
        return null;
    }

    private static Path searchFile(String directory, String fileName) {
        try {
            return Files.walk(Paths.get(directory))
                    .filter(path -> path.toFile().isFile() && path.getFileName().toString().equals(fileName))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            System.out.println("Error searching file: " + e.getMessage());
            return null;
        }
    }

}

