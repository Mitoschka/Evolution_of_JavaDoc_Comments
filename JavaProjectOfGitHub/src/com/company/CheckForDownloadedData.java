package com.company;

import java.io.File;
import java.util.ArrayList;

public class CheckForDownloadedData {

    public static int countOfDownloadFile;

    public static ArrayList<String> arrayOfDownloadedFiles = new ArrayList<>();

    public static void FindFiles(String ext) {
        String SRC__FOLDER = Main.pathToFile + FolderCreate.folder.getName();
        File file = new File(SRC__FOLDER);
        File[] listFiles = file.listFiles(new MyFileNameFilter(ext));
        if (listFiles.length == 0) {
            return;
        } else {
            for (File f : listFiles) {
                String result = f.getName().substring(f.getName().indexOf('-') + 1, f.getName().indexOf('.'));
                arrayOfDownloadedFiles.add(result);
            }
        }
        countOfDownloadFile = arrayOfDownloadedFiles.size();
    }
}

