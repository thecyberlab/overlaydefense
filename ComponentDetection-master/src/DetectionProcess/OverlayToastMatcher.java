package DetectionProcess;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OverlayToastMatcher {

    static ArrayList<String> overlayType = new ArrayList<>();
    static ArrayList<String> appOverlayTypeNo = new ArrayList<>();

    public  static Boolean overlayToastMatcher(String content) {
        Boolean presenceOfToastOverlay = false;

        // Read the Java file content
        // Find toast occurrences in the file
        Pattern pattern = Pattern.compile("(Toast\\s+\\w+\\s*=\\s*new\\s+Toast\\([^;]+\\);)");
        Matcher matcher = pattern.matcher(content);

        List<JSONObject> toastProperties = new ArrayList<>();

        while (matcher.find()) {
            String toastCreation = matcher.group(1);

            // Extract toast variable name
            Pattern varNamePattern = Pattern.compile("Toast\\s+(\\w+)\\s*=\\s*new\\s+Toast\\([^;]+\\);");
            Matcher varNameMatcher = varNamePattern.matcher(toastCreation);

            String toastVarName = "";
            if (varNameMatcher.find()) {
                toastVarName = varNameMatcher.group(1);
            }

            // Find setAttributes calls for the toast variable
            Pattern setAttributesPattern = Pattern.compile(toastVarName + "\\.set(View|Gravity|Duration)\\([^;]+\\);");
            Matcher setAttributesMatcher = setAttributesPattern.matcher(content);

            boolean toastFound = false;
            String toastAttributes = "";

            while (setAttributesMatcher.find()) {
                toastFound = true;

                // Extract method and parameter
                String method = setAttributesMatcher.group(1);
                String parameter = setAttributesMatcher.group(0);

                // Add toast attribute and value to the string
                if (!toastAttributes.isEmpty()) {
                    toastAttributes += ", ";
                }
                toastAttributes += method + ": " + parameter;
            }

            if (toastFound) {
                // Create JSON object for the toast properties
                JSONObject toastPropertiesObj = new JSONObject();
                toastPropertiesObj.put("toast_varName", toastVarName);
                toastPropertiesObj.put("toast_found", true);
                toastPropertiesObj.put("toast_attributes", toastAttributes);
                if(toastAttributes.toLowerCase().contains("setgravity") && toastAttributes.toLowerCase().contains("setview"))
                    presenceOfToastOverlay = true;
                toastProperties.add(toastPropertiesObj);
            }
        }

        return  presenceOfToastOverlay;
    }


    public static Boolean overlayWindowMatcher(String fileContents) {

        overlayType.add("2002");  // TYPE_PHONE
        overlayType.add("2003");  // TYPE_SYSTEM_ALERT
        overlayType.add("2005");  // TYPE_TOAST
        overlayType.add("2010");  // TYPE_SYSTEM_ERROR
        overlayType.add("2038");  // TYPE_APPLICATION_OVERLAY

        appOverlayTypeNo.add("56");
        appOverlayTypeNo.add("24");
        appOverlayTypeNo.add("65816");
        appOverlayTypeNo.add("1048");


        String[] lines = fileContents.split("\n");

        for (String line : lines) {

            Pattern pattern = Pattern.compile("WindowManager.LayoutParams\\((.*?)\\);");
            Matcher matcher = pattern.matcher(line);
            String thirdParam = null;


            if (matcher.find()) {
                String paramsString = matcher.group(1);
                String[] params = paramsString.split(",\\s*");
                int paramI = 0;

                for (int s=0; s<params.length; s++) {

                    if (s>1) {
                        if (overlayType.contains(params[s].trim())) {
                            if(params[s].equals("2038")) {
                                thirdParam = params[s];
                                if (appOverlayTypeNo.contains(params[s+1]))
                                    return true;
                                else
                                    return false;
                                //overlayTypeApp.add(params[s+1]);
                                //System.out.println("Found param: " + wndowProps);
                            }
                            else
                                return  true;

                        }

                    }

                }
            } else {
                //System.out.println("No WindowManager.LayoutParams found.");
            }
        }

        return  false;

    }
}

