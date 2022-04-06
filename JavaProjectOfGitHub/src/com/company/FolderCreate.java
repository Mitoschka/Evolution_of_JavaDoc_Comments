package com.company;

import java.io.File;

public class FolderCreate {

    protected static File folder;
    protected static File temporaryFolder;

    protected FolderCreate() {

        folder = new File(Main.pathToDisk + Main.folderName);
        temporaryFolder = new File(Main.pathToTemporaryDisk + Main.folderName);

        if (folder.exists() && temporaryFolder.exists()) {
            return;
        }
        folder.mkdir();
        temporaryFolder.mkdir();
    }
}