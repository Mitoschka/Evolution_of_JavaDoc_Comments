package com.company;

import java.io.File;
import java.util.ArrayList;

public class CheckForDownloadedData {

    public static int countOfDownloadFile;

    public static ArrayList<String> arrayOfDownloadedFiles = new ArrayList<>();

    public static void FindFiles(String ext) {
        if (!FolderCreate.temporaryFolder.exists()) {
            System.out.println("Don`t find temporary folder");
            System.exit(1);
        }
        if (!FolderCreate.folder.exists()) {
            System.out.println("Don`t find folder");
            System.exit(1);
        }
        String SRC__FOLDER = Main.pathToDisk + FolderCreate.folder.getName();
        File file = new File(SRC__FOLDER);
        File[] listFiles = file.listFiles(new MyFileNameFilter(ext));
        if (listFiles.length == 0) {
            return;
        } else {
            for (File f : listFiles) {
                String result = f.getName().substring(f.getName().lastIndexOf("_") + 1, f.getName().indexOf('.'));
                arrayOfDownloadedFiles.add(result);
            }
        }
        countOfDownloadFile = arrayOfDownloadedFiles.size();
    }
}

