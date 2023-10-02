package Automation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeleteSubfolders {
    public static void main(String[] args) {
        // Provide the source folder path and the list of subfolder names to keep


        //readLinesAndConvertToList();
        String sourceFolderPath = "/home/user01/Documents/aaa/malware_decompiled_jadx_backup";  //delete folders from herr
        //List<String> subfoldersToKeep = Arrays.asList("aa", "subfolder2", "cc");
        List<String> subfoldersToKeep = readLinesAndConvertToList();

        // Get a list of all subfolders in the source folder
        File sourceFolder = new File(sourceFolderPath);
        File[] subfolders = sourceFolder.listFiles(File::isDirectory);

        // Delete subfolders that are not in the list of subfolders to keep
        if (subfolders != null) {
            for (File subfolder : subfolders) {
                String subfolderName = subfolder.getName();
                if (!subfoldersToKeep.contains(subfolderName)) {
                    //deleteFolder(subfolder);
                }
            }
        } else {
            System.out.println("No subfolders found in the source folder.");
        }
    }

    // Recursive method to delete a folder and its contents
    private static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    //deleteFolder(file);
                }
            }
        }
        folder.delete();
        System.out.println("Deleted folder: " + folder.getAbsolutePath());
    }

    public static List<String> readLinesAndConvertToList() {
        List<String> result = new ArrayList<>();

        // Build the file path
        String filePath =  "/media/user01/HDD_4TB/AK_GOOGLE_APKS/apks_malware_265.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //List<String> lineList = Arrays.asList(line.split("\\s+"));
                result.add(line.substring(0, line.length() - 4));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}

