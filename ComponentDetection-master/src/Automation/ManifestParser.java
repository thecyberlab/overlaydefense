package Automation;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ManifestParser {

    private static String sourceFolder = "/media/user01/HDD_4TB/AK/decompiled_apktool_10";
    private static String outputFolder = "/home/user01/Documents/ManifestParsing/apktool_10/";
    public static JSONObject parseAndroidManifest(String filePath, String apkName) {

        JSONObject apkDetails = new JSONObject();
        JSONArray activityArr = new JSONArray();
        // String filePath = "/home/ani0904071/Documents/thesis/overlay/sample/jadx_output/conn-release/resources/AndroidManifest.xml"; // Replace with the actual path to your AndroidManifest.xml file
        try {
            //File manifestFile = new File(filePath);
            FileInputStream manifestFile = new FileInputStream(new File(filePath));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //Document document = builder.parse(manifestFile);
            Document document = builder.parse(manifestFile, String.valueOf(StandardCharsets.UTF_8));





            NodeList activityNodes = document.getElementsByTagName("activity");
            for (int i = 0; i < activityNodes.getLength(); i++) { // for each activity
                Node activityNode = activityNodes.item(i);
                JSONObject eachActivity = new JSONObject();
                if (activityNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element activityElement = (Element) activityNode;
                    String activityName = activityElement.getAttribute("android:name");
                    //System.out.println("Activity Name: " + activityName);
                    eachActivity.put("actvityName", activityName);
                    NodeList intentFilterNodes = activityElement.getElementsByTagName("intent-filter");
                    for (int j = 0; j < intentFilterNodes.getLength(); j++) {
                        Node intentFilterNode = intentFilterNodes.item(j);
                        if (intentFilterNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element intentFilterElement = (Element) intentFilterNode;
                            NodeList actionNodes = intentFilterElement.getElementsByTagName("action");
                            eachActivity.put("actionNodes","");
                            for (int k = 0; k < actionNodes.getLength(); k++) {
                                Node actionNode = actionNodes.item(k);
                                if (actionNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element actionElement = (Element) actionNode;
                                    String action = actionElement.getAttribute("android:name");
                                    //System.out.println("Action: " + action);
                                    eachActivity.put("actionNodes", eachActivity.get("actionNodes") + ";;;" + action);
                                }
                            }

                            NodeList categoryNodes = intentFilterElement.getElementsByTagName("category");
                            eachActivity.put("categoryNodes","");
                            for (int k = 0; k < categoryNodes.getLength(); k++) {
                                Node categoryNode = categoryNodes.item(k);
                                if (categoryNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element categoryElement = (Element) categoryNode;
                                    String category = categoryElement.getAttribute("android:name");
                                    //System.out.println("Category: " + category);
                                    eachActivity.put("categoryNodes", eachActivity.get("categoryNodes") + ";;;" + category);
                                }
                            }

                            NodeList dataNodes = intentFilterElement.getElementsByTagName("data");
                            eachActivity.put("dataNodes","");
                            for (int k = 0; k < dataNodes.getLength(); k++) {
                                Node dataNode = dataNodes.item(k);
                                if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element dataElement = (Element) dataNode;
                                    String data = dataElement.getAttribute("android:scheme");
                                    //System.out.println("Data: " + data);
                                    eachActivity.put("dataNodes", eachActivity.get("dataNodes") + ";;;" + data);
                                }
                            }
                        }
                    }
                    String launchMode = activityElement.getAttribute("android:launchMode");
                    eachActivity.put("launchMode", launchMode);
                    String affinity = "null";
                    if(activityElement.getAttributeNode("android:taskAffinity") != null)
                        affinity = activityElement.getAttribute("android:taskAffinity");
                    eachActivity.put("taskAffinity", affinity);
                    String finishOnTaskLaunch  = activityElement.getAttribute("android:finishOnTaskLaunch");
                    eachActivity.put("finishOnTaskLaunch", finishOnTaskLaunch);
                    String noHistory  = activityElement.getAttribute("android:noHistory");
                    eachActivity.put("noHistory", noHistory);
                    String allowTaskReparenting  = activityElement.getAttribute("android:allowTaskReparenting");
                    eachActivity.put("allowTaskReparenting", allowTaskReparenting);
                    String documentLaunchMode  = activityElement.getAttribute("android:documentLaunchMode");
                    eachActivity.put("documentLaunchMode", documentLaunchMode);
                    String excludeFromRecents  = activityElement.getAttribute("android:excludeFromRecents");
                    eachActivity.put("excludeFromRecents", excludeFromRecents);

                }
                activityArr.put(eachActivity);
            } // for each activity
            apkDetails.put(apkName, activityArr);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
        } finally {

            apkDetails.put(apkName, activityArr);
            System.out.println();
        }

        return apkDetails;
    }
    private static File searchManifestFile(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File manifestFile = searchManifestFile(file);
                    if (manifestFile != null) {
                        return manifestFile;
                    }
                } else if (file.getName().equals("AndroidManifest.xml")) {
                    return file;
                }
            }
        }
        return null;
    }
    // Usage example
    public static void main(String[] args) {

        //String sourceFolder = "/home/ani0904071/Documents/thesis/overlay/sample/jadx_output";
        File folder = new File(sourceFolder);
        if (folder.exists() && folder.isDirectory()) {
            File[] subfolders = folder.listFiles(File::isDirectory);
            if (subfolders != null) {
                for (File subfolder : subfolders) {
                    String subfolderName = subfolder.getName();
                    //System.out.println(subfolderName);
                    File directory = new File(sourceFolder + "/" + subfolderName);
                    String targetFileName = "AndroidManifest.xml";
                    searchFile(directory, targetFileName, subfolderName);
                }
            }
        } else {
            System.out.println("Invalid source folder path.");
        }
    }
    public static void searchFile(File directory, String targetFileName, String apkName) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().equals(targetFileName)) {
                        // File found
                        // System.out.println("File found: " + file.getAbsolutePath());
                        JSONObject apkDetails = parseAndroidManifest(file.getAbsolutePath(), apkName);
                        writeJsonObject(apkDetails, apkName);
                    } else if (file.isDirectory()) {
                        // Recursive call for subdirectory
                        searchFile(file, targetFileName, apkName);
                    }
                }
            }
        }
    }

    private static void writeJsonObject(JSONObject apkDetails, String apkName) {

        FileWriter fileW = null;
        try {
            fileW = new FileWriter(outputFolder + apkName + ".json");
            fileW.write(apkDetails.toString(4));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fileW!=null) {
                try {
                    fileW.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
