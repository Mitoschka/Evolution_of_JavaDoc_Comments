package com.company;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FolderCreate {

    public static File file;
    protected static String folderName;
    protected static File folder;
    private static int count = 2;
    public static boolean isCreated = false;


    protected FolderCreate() {

        if (!isCreated) {
            folderName = "Project Folder(1)";
        }
        if (isCreated) {
            folderName = "JSON Folder(1)";
        }

        for (Path path = Paths.get("C:\\" + folderName); Files.exists(path, new LinkOption[0]); ++count) {
            if (isCreated) {
                folderName = "JSON Folder(" + count + ")";
            }
            if (!isCreated) {
                folderName = "Project Folder(" + count + ")";
            }
            path = Paths.get("C:\\" + folderName);
        }

        folder = new File("C:\\" + folderName);
        if (!isCreated) {
            file = folder;
        }
        folder.mkdir();
    }

    private FolderCreate(String folderName) {
        FolderCreate.folderName = folderName;
    }
}