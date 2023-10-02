package DetectionProcess;

import java.util.List;

public class CreateAndUpdateComponents {

    private static int otherAppTaskID = 10000;

    public static Component createListObject(String fileName, List<Component> initialStorage, String packageName) {

        Boolean samePakcageObjectCreated = false;

        Component createdActivity = null;
        for (int i = 0; i < initialStorage.size(); i++) {
            Component component = initialStorage.get(i);
            if (component.getName().contains(fileName)) {

                String activityName = component.getName();
                //String activityName = activity.getName();
                String taskID = component.getTaskID();
                String generatedAffinity = component.getGeneratedAffinity();
                Boolean backStackCreated = component.isBackStackCreated();
                String assignedAffinity = component.getAssignedAffinity();
                Boolean toastPresence = component.isToastOverlayPresence();
                String[] finishTypes = component.getFinishTypes();
                String parentActivity = component.getParent();
                Boolean isOtherApp = component.isOtherApp();
                Boolean isLauncher = component.getIsLauncher();
                String componentType = component.getComponentType();

                createdActivity = new Component(activityName, taskID, finishTypes, backStackCreated, isOtherApp, parentActivity,
                        assignedAffinity, generatedAffinity, toastPresence, isLauncher, componentType);
                samePakcageObjectCreated = true;
                break;

            }


        }

        if (!samePakcageObjectCreated) {
            return createdOtherAppActivity(fileName);
        }

        return createdActivity;
    }

    private static Component createdOtherAppActivity(String fileName) {

        String activityName = fileName;
        String taskID = String.valueOf(++ otherAppTaskID);
        String generatedAffinity = null;
        Boolean backStackCreated = true;
        String assignedAffinity = null;
        Boolean toastPresence = false;
        String[] finishTypes = null;
        String parentActivity = null;
        Boolean isOtherApp = true;
        Boolean isLauncher = false;
        String componentType = "";


        return new Component(activityName, taskID, finishTypes, backStackCreated, isOtherApp, parentActivity,
                assignedAffinity, generatedAffinity, toastPresence, isLauncher, componentType ) ;
    }

    public static void updateListActivityFinishTypes(String fileName, String[] finishTypes) {


        for (int i = 0; i < ActivityCallHierarchy.componentGenerationStates.size(); i++) {
            Component currentActivity = ActivityCallHierarchy.componentGenerationStates.get(i);
            String activityName = currentActivity.getName();
            if (activityName.equals(fileName)) {
                currentActivity.setFinishTypes(finishTypes);
                break;
            }
        }

    }

    public static void updateToastPresence(String fileName) {

        for(int i = 0; i < ActivityCallHierarchy.componentGenerationStates.size(); i++) {
            Component currentActivity = ActivityCallHierarchy.componentGenerationStates.get(i);
            String activityName = currentActivity.getName();
            if (activityName.equals(fileName)) {
                currentActivity.setIsToastOverlayPresence(true);
                break;
            }
        }
    }


    public static void updateListActivityParent(String currentFileName, String className) {

        for(int i = 0; i <  ActivityCallHierarchy.componentGenerationStates.size(); i++) {
            Component activity =  ActivityCallHierarchy.componentGenerationStates.get(i);
            String activityName = activity.getName();
            if (activityName.contains(className)) {
                //currentActivity.setIsToastOverlayPresence(true);
                if (!activityName.equals(currentFileName)) {
                    activity.setParent(currentFileName);
                    break;
                }
            }
        }

    }

    public static void updateTaskID(String currentFileName, Component addedComponent, List<Component> initialStorage,
                                    List<Component> componentGenerationStates) {


        String assignedAffinity = addedComponent.getAssignedAffinity();
        String addedComponentType = addedComponent.getComponentType();
        String addedComponentParent = addedComponent.getParent();
        String addedComponentName = addedComponent.getName();


        for (int cg = componentGenerationStates.size() - 2; cg >= 0; cg--) {

            Component parentComponent = componentGenerationStates.get(cg);
            String parentComponentName = parentComponent.getName();
            String parentComponentType = parentComponent.getComponentType();
            String parentTaskID = parentComponent.getTaskID();

            Boolean setTaskID = false;

            if(addedComponentType.equals("typeActivity") &&
                    parentComponentName.equals(addedComponentParent)) {

                addedComponent.setTaskID(parentComponent.getTaskID());
                addedComponent.setGeneratedAffinity(parentComponent.getGeneratedAffinity());
                // activity state rules basic
                //addedComponent.setGeneratedAffinity(parentComponents.getGeneratedAffinity());
                setTaskID = true; // make sure it is set otherwise continue to set
            }
            else if(addedComponentType.equals("typeService")) {
                addedComponent.setTaskID(parentComponent.getTaskID());
            }

            if (setTaskID)
                break;

        }




    }
}