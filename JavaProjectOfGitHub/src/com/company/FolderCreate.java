package com.company;

import java.io.File;

public class FolderCreate {

    protected static File folder;

    protected FolderCreate() {

        folder = new File(Main.pathToFile + Main.folderName);

        String SRC__FOLDER = Main.pathToFile + folder.getName();
        File file = new File(SRC__FOLDER);
        if (file.exists()) {
            return;
        }
        folder.mkdir();
    }
}