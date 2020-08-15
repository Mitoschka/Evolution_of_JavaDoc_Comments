package com.company;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Analyze {

    public static LinkedList<JavaDocSegment> ArrayOfCommits = new LinkedList<>();
    public static LinkedList<LinkedList<JavaDocSegment>> ArrayOfLog = new LinkedList<LinkedList<JavaDocSegment>>();
    public static JavaDocSegment[] javaDocSegment;

    public static int count = 0;
    public static int countOfElementThatWillBeComparedWithTheRest = 0;
    public static String zipFileName;
    public static Boolean isUnZip = false;
    public static File file1;


    public static void AnalyzeDirectory(String Path) throws Exception {
        File path = new File(Path);
        if (path.isFile())
            AnalyzeFile(new File(Path));
        else {
            LinkedList<File> files = new LinkedList<>();
            new com.company.DirExplorer((level, fpath, file) -> fpath.endsWith(".zip"), (level, fpath, file) -> {
                files.add(file);
            }).explore(path);

            files.forEach(file -> {
                try {
                    isUnZip = false;
                    UnZip.UnZip(file);
                    file1 = new File(Path + "\\" + file.getName().replace(".zip", ""));
                    while (!isUnZip) {
                        if (file1.exists()) {
                            AnalyzeFile(file1);
                            isUnZip = true;
                        } else {
                            isUnZip = false;
                        }
                    }
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            while (countOfElementThatWillBeComparedWithTheRest < ArrayOfCommits.size()) {
                int i = 1;
                LinkedList<JavaDocSegment> ArrayOfDuplication = new LinkedList<>();
                ArrayOfDuplication.add(ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest));
                while (i < ArrayOfCommits.size()) {
                    if (ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).Signature.equals(ArrayOfCommits.get(i).Signature)
                            && ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).Namespace.equals(ArrayOfCommits.get(i).Namespace)) {
                        if (!ArrayOfDuplication.contains(ArrayOfCommits.get(i))) {
                            ArrayOfDuplication.add(ArrayOfCommits.get(i));
                            ArrayOfCommits.remove(i);
                        }
                    }
                    i++;
                }
                int j = 0;
                while (j + 1 < ArrayOfDuplication.size()) {
                    if (ArrayOfDuplication.get(j).Content.equals(ArrayOfDuplication.get(j + 1).Content)) {
                        ArrayOfDuplication.remove(j);
                    } else {
                        j++;
                    }
                }
                if (ArrayOfDuplication.size() > 1) {
                    ArrayOfLog.add(ArrayOfDuplication);
                } else {
                    ArrayOfDuplication.remove(0);
                }
                ArrayOfCommits.remove(countOfElementThatWillBeComparedWithTheRest);
            }
        }
    }

    public static void AnalyzeFile(File file) {
        try {
            String jsonToString = readFileAsString(file.toString());
            javaDocSegment = new Gson().fromJson(jsonToString, JavaDocSegment[].class);
            ArrayOfCommits.add(javaDocSegment[count]);
            count++;
            file1.delete();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }
}

