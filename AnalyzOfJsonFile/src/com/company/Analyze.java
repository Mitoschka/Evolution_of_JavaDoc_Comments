package com.company;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Analyze {

    public static LinkedList<DocCommit> ArrayOfCommits = new LinkedList<>();
    public static LinkedList<LinkedList<DocCommit>> ArrayOfLog = new LinkedList<>();
    public static DocCommit[] docCommits;

    public static int count = 0;
    public static int countOfElementThatWillBeComparedWithTheRest = 0;
    public static String zipFileName;
    public static Boolean isUnZip = false;
    public static File file1;

    public static long firstSizeInBytes = -1;
    public static long secondSizeInBytes = -3;


    public static void AnalyzeDirectory(String Path) {
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
                        int count = 0;
                        while (!file1.exists() && count < 200) {
                            Thread.sleep(100);
                            isUnZip = false;
                            count++;
                        }
                        while (firstSizeInBytes != secondSizeInBytes) {
                            firstSizeInBytes = file1.length();
                            Thread.sleep(100);
                            secondSizeInBytes = file1.length();
                            isUnZip = false;
                        }
                        AnalyzeFile(file1);
                        isUnZip = true;
                    }
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            while (countOfElementThatWillBeComparedWithTheRest < ArrayOfCommits.size()) {
                LinkedList<DocCommit> ArrayOfDuplication = new LinkedList<>();
                ArrayOfDuplication.add(ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest));
                int i = 1;
                while (i < ArrayOfCommits.size()) {
                    if (ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DocSegments.Signature.equals(ArrayOfCommits.get(i).DocSegments.Signature)
                            && ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DocSegments.Namespace.equals(ArrayOfCommits.get(i).DocSegments.Namespace)) {
                        if (!ArrayOfDuplication.contains(ArrayOfCommits.get(i))) {
                            ArrayOfDuplication.add(ArrayOfCommits.get(i));
                            ArrayOfCommits.remove(i);
                        }
                    }
                    i++;
                }
                int j = 0;
                while (j + 1 < ArrayOfDuplication.size()) {
                    if (ArrayOfDuplication.get(j).DocSegments.Content.equals(ArrayOfDuplication.get(j + 1).DocSegments.Content)) {
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
        CheckUniqueElementInArrayOfLog();
    }

    public static void AnalyzeFile(File file) {
        try {
            String jsonToString = readFileAsString(file.toString());
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(jsonToString));
            reader.setLenient(true);
            docCommits = gson.fromJson(reader, DocCommit[].class);
            ArrayOfCommits.add(docCommits[count]);
            count++;
            file1.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    public static void CheckUniqueElementInArrayOfLog() {
        int i = 0;
        while (i < ArrayOfLog.size()) {
            int j = 0;
            while (j + 1 < ArrayOfLog.get(i).size()) {
                if (ArrayOfLog.get(i).get(j).DocSegments.Content.equals(ArrayOfLog.get(i).get(j + 1).DocSegments.Content) &&
                        (ArrayOfLog.get(i).get(j).DocSegments.Namespace.equals(ArrayOfLog.get(i).get(j + 1).DocSegments.Namespace) &&
                                (ArrayOfLog.get(i).get(j).DocSegments.Signature.equals(ArrayOfLog.get(i).get(j + 1).DocSegments.Signature) &&
                                        (ArrayOfLog.get(i).get(j).DateTime.equals(ArrayOfLog.get(i).get(j + 1).DateTime) &&
                                                (ArrayOfLog.get(i).get(j).Name.equals(ArrayOfLog.get(i).get(j + 1).Name)))))) {
                    ArrayOfLog.remove(j);
                } else {
                    j++;
                }
            }
            i++;
        }
    }
}
