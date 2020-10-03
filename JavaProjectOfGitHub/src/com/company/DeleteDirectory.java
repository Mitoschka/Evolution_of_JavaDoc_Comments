package com.company;

import java.io.File;
import java.io.IOException;

public class DeleteDirectory {

    public static boolean isSafe;
    private static boolean conditionForSafe;

    public static void DeleteDirectory(String SRC__FOLDER, String safe) {
        File directory = new File(SRC__FOLDER);

        //make sure directory exists
        if (!directory.exists()) {

            System.out.println("Directory does not exist.");
            System.exit(0);

        } else {
            try {
                delete(directory, safe);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }

        System.out.println("Done");
    }

    public static void delete(File file, String safe)
            throws IOException {


            if (!isSafe) {
                if (safe.equals("\\")) {
                    return;
                } else {
                    conditionForSafe = !file.getAbsolutePath().contains(safe) && !isSafe;
                }
            } else {
                    conditionForSafe = file.getAbsolutePath().contains(safe) && isSafe;

            }


        if (file.isDirectory()) {

            //directory is empty, then delete it
            if (file.list().length == 0) {
                if (conditionForSafe) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath() + "\n");
                }

            } else {

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete, safe);
                }

                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    if (conditionForSafe) {
                        file.delete();
                        System.out.println("Directory is deleted : "
                                + file.getAbsolutePath() + "\n");
                    }
                }
            }

        } else {
            //if file, then delete it
            if (conditionForSafe) {
                file.delete();
                System.out.println("File is deleted : " + file.getAbsolutePath() + "\n");
            }
        }
    }
}