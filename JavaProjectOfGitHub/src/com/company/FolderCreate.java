package com.company;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FolderCreate {

    protected static String folderName;
    protected static File folder;
    private static int count = 2;

    protected FolderCreate() {
        folderName = "Project Folder(1)";

        for (Path path = Paths.get("C:\\" + folderName); Files.exists(path, new LinkOption[0]); ++count) {
            folderName = "Project Folder(" + count + ")";
            path = Paths.get("C:\\" + folderName);
        }

        folder = new File("C:\\" + folderName);
        folder.mkdir();
    }

    private FolderCreate(String folderName) {
        FolderCreate.folderName = folderName;
    }
}