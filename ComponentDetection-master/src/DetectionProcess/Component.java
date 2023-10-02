package DetectionProcess;


public class Component {
    private String name;
    private String taskID;
    private boolean isBackTraversed = false;
    private String[] finishTypes;
    private boolean backStackCreated;
    private  boolean isOtherApp;
    private String parent;
    private String assignedAffinity;
    private String generatedAffinity;
    private boolean isToastOverlayPresence;

    private boolean isLauncher = false;

    private String componentType  = null;

    public Component(String name, String taskID, String[] finishTypes, Boolean backStackCreated, Boolean isOtherApp,
                     String parent, String assignedAffinity, String generatedAffinity, Boolean isToastOverlayPresence, Boolean isLauncher, String componentType) {
        this.name = name;
        this.taskID = taskID;
        this.isBackTraversed = false;
        this.backStackCreated = backStackCreated;
        this.finishTypes = finishTypes;
        this.isOtherApp = isOtherApp;
        this.parent = parent;
        this.assignedAffinity = assignedAffinity;
        this.generatedAffinity = generatedAffinity;
        this.isToastOverlayPresence = isToastOverlayPresence;
        this.isLauncher = isLauncher;
        this.componentType = componentType;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }
    public String getName() {
        return  name;
    }
    @Override
    public String toString() {
        return name;
    }
    public String[] getFinishTypes() {
        return finishTypes;
    }
    public boolean isBackStackCreated() {
        return backStackCreated;
    }
    public boolean isOtherApp() {
        return isOtherApp;
    }
    public String getParent() {
        return  parent;
    }
    public String getAssignedAffinity() {
        return  assignedAffinity;
    }
    public String getGeneratedAffinity() {
        return  generatedAffinity;
    }
    public void setGeneratedAffinity(String generatedAffinity) {
            this.generatedAffinity = generatedAffinity;
    }
    public void setAssignedAffinity(String assignedAffinity) {
        this.assignedAffinity = assignedAffinity;
    }
    public boolean isToastOverlayPresence() {
        return this.isToastOverlayPresence;
    }

    public void setIsToastOverlayPresence (Boolean isToastOverlayPresence) {
        this.isToastOverlayPresence = isToastOverlayPresence;
    }

    public void setFinishTypes(String[] finishTypes) {
        this.finishTypes = finishTypes;
    }

    public void setParent(String parentName) {
        this.parent = parentName;
    }

    public void setIsLauncher(Boolean isLauncher) {
        this.isLauncher = isLauncher;
    }

    public  Boolean getIsLauncher() {
        return this.isLauncher;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public  String getComponentType() {
        return this.componentType;
    }


}
