package DetectionProcess;

import java.util.*;

public class BackStackGeneration {

    public static LinkedHashSet<String> backStackOrderList = new LinkedHashSet<>(); //O(1)
    public static Map< String, LinkedList<Component> > backStack = new HashMap<>();

    public static Map<String, Boolean> trackToastTaskID = new LinkedHashMap<>();

    public static void generateBackStacks(List<Component> componentGenerationStates) {

        int componentsSize = componentGenerationStates.size();
        String serviceTaskId = null;

        for (int i = componentsSize - 1 ; i>=0; i-- ) {
            Component component = componentGenerationStates.get(i);
            if (component.getComponentType().equals("typeService") && component.isToastOverlayPresence() ==true)
                serviceTaskId = component.getTaskID();
            if (component.getComponentType().equals("typeActivity") || component.getComponentType().equals(""))
                backStack.computeIfAbsent(component.getTaskID(), k -> new LinkedList<>()).add(component); //O(n)

            backStackOrderList.add(component.getTaskID()); //O(1)

            if(component.isToastOverlayPresence() || component.getTaskID().equals(serviceTaskId))
                trackToastTaskID.put(component.getTaskID(), true);

        }

        System.out.println("BackStack Creation done....");
        System.out.println();
        System.out.println();

        System.out.println("backStack" + backStack);
        ActivityDetection.calculateActivity(backStackOrderList, backStack);

    }
}
