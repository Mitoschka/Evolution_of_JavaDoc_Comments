package com.company;

import java.io.File;
import java.io.IOException;

public class DeleteDirectory

{
    private static final String SRC__FOLDER ="C:\\" + FolderCreate.file.getName();

    public static void DeleteDirectory()
    {

        File directory = new File(SRC__FOLDER);

        //make sure directory exists
        if(!directory.exists()){

            System.out.println("Directory does not exist.");
            System.exit(0);

        }else{

            try{

                delete(directory);

            }catch(IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }

        System.out.println("Done");
    }

    public static void delete(File file)
            throws IOException{

        if(file.isDirectory()){

            //directory is empty, then delete it
            if(file.list().length==0){

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath() + "\n");

            }else{

                //list all the directory contents
                String files[]= file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if(file.list().length==0){
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath() + "\n");
                }
            }

        }else{
            //if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath() + "\n");
        }
    }
}