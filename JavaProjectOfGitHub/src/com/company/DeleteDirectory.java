
package com.company;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DeleteDirectory {

    public static void DeleteDirectory() {
        String SRC__FOLDER = Main.pathToFile + FolderCreate.folder.getName() + UnZip.arraylist.get(0);
        File directory = new File(SRC__FOLDER);

        //make sure directory exists
        if (!directory.exists()) {

            System.out.println("Directory does not exist.");
            System.exit(0);

        } else {
            try {
                long start = System.currentTimeMillis();
                //delete(directory);
                FileUtils.deleteDirectory(new File(String.valueOf(directory)));
                long end = System.currentTimeMillis();
                System.out.println("\n" + "Deletion of " + directory.getName() + " completed " + (end - start) / 1000);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        System.out.println("Done");
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