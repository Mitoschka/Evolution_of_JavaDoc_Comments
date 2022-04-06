
package com.company;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DeleteDirectory {

    public static void DeleteDirectory() {
        String SRC__FOLDER = Connect.fileToDelete.getAbsolutePath();
        File directory = new File(SRC__FOLDER);

        long start = System.currentTimeMillis();
        //make sure directory exists
        if (!directory.exists()) {

            System.out.println("Directory does not exist.");
            System.exit(0);

        } else {
            try {
                while (directory.exists()) {
                    //delete(directory);
                    try {
                        FileUtils.deleteDirectory(directory);
                    } catch (Exception e) {
                        Thread.sleep(100);
                    }
                }
                System.out.println("\n" + "Deletion of " + directory.getName() + " completed ");
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Done " + (end - start) / 1000);
        return;
    }

    public static void delete(File file)
            throws IOException {

        if (file.isDirectory()) {

            //directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath() + "\n");

            } else {

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath() + "\n");
                }
            }

        } else {
            //if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath() + "\n");
        }
    }
}