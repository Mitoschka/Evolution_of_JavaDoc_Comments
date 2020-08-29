package com.company;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

public class Analyze {

    public static LinkedList<DocCommit> ArrayOfCommits = new LinkedList<>();
    public static DocCommit ArrayOfCommitsSegments;
    public static LinkedList<DocCommit> ArrayOfCommitsSegmentsElement = new LinkedList<>();
    public static LinkedList<LinkedList<DocCommit>> ArrayOfLog = new LinkedList<>();
    public static DocCommit[] docCommits;

    public static int count = 0;
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
            AnalyzeElements();
        }

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
        } catch (ArrayIndexOutOfBoundsException e) {
            file1.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    public static void AnalyzeElements() {
        int countOfElementThatWillBeComparedWithTheRest = 0;
        while (countOfElementThatWillBeComparedWithTheRest < ArrayOfCommits.size()) {
            int i = 0;
            int j = 0;
            while (j < ArrayOfCommits.size()) {
                int k = 0;
                while (k < ArrayOfCommits.get(i).DocSegments.size()) {
                    ArrayList<JavaDocSegment> javaDocSegments = new ArrayList<>();
                    javaDocSegments.add(ArrayOfCommits.get(i).DocSegments.get(k));
                    ArrayOfCommitsSegments = new DocCommit(javaDocSegments, ArrayOfCommits.get(i).Name, ArrayOfCommits.get(i).DateTime);
                    if (!ArrayOfCommitsSegmentsElement.contains(ArrayOfCommits.get(i).Name) &&
                            (!ArrayOfCommitsSegmentsElement.contains(ArrayOfCommits.get(i).DateTime) &&
                                    (!ArrayOfCommitsSegmentsElement.contains(ArrayOfCommits.get(i).DocSegments.get(k).Signature) &&
                                            (!ArrayOfCommitsSegmentsElement.contains(ArrayOfCommits.get(i).DocSegments.get(k).Namespace))))) {
                        ArrayOfCommitsSegmentsElement.add(ArrayOfCommitsSegments);
                        ArrayOfCommits.get(i).DocSegments.remove(k);
                    }
                    k++;
                }
                i++;
                j++;
            }
            ArrayOfCommits.remove(countOfElementThatWillBeComparedWithTheRest);
        }
        int countOfElementThatWillBeComparedWithTheRestOfElement = 0;
        while (countOfElementThatWillBeComparedWithTheRestOfElement < ArrayOfCommitsSegmentsElement.size()) {
            LinkedList<DocCommit> ArrayOfDuplication = new LinkedList<>();
            ArrayOfDuplication.add(ArrayOfCommitsSegmentsElement.get(countOfElementThatWillBeComparedWithTheRestOfElement));
            int i = 1;
            while (i < ArrayOfCommitsSegmentsElement.size()) {
                if (ArrayOfCommitsSegmentsElement.get(countOfElementThatWillBeComparedWithTheRestOfElement).DocSegments.get(0).Signature.equals(ArrayOfCommitsSegmentsElement.get(i).DocSegments.get(0).Signature)
                        && ArrayOfCommitsSegmentsElement.get(countOfElementThatWillBeComparedWithTheRestOfElement).DocSegments.get(0).Namespace.equals(ArrayOfCommitsSegmentsElement.get(i).DocSegments.get(0).Namespace)) {
                    if (!ArrayOfDuplication.contains(ArrayOfCommitsSegmentsElement.get(i))) {
                        ArrayOfDuplication.add(ArrayOfCommitsSegmentsElement.get(i));
                        ArrayOfCommitsSegmentsElement.remove(i);
                    }
                }
                i++;
            }
            CheckUniqueElementInArrayOfLog(ArrayOfDuplication);
            if (ArrayOfDuplication.size() > 1) {
                ArrayOfLog.add(ArrayOfDuplication);
            } else {
                ArrayOfDuplication.clear();
            }
            ArrayOfCommitsSegmentsElement.remove(countOfElementThatWillBeComparedWithTheRestOfElement);
            System.out.println("\nThere are still " + ArrayOfCommitsSegmentsElement.size() + " files left\n");
        }
    }

    public static LinkedList<DocCommit> CheckUniqueElementInArrayOfLog(LinkedList<DocCommit> arrayOfDuplication) {
        int i = 0;
            while (i < arrayOfDuplication.size()) {
                int countOfUniqueElement = 1 + i;
                while (arrayOfDuplication.size() > countOfUniqueElement) {
                    if (arrayOfDuplication.get(i).DocSegments.get(0).Content.equals(arrayOfDuplication.get(countOfUniqueElement).DocSegments.get(0).Content)) {
                        arrayOfDuplication.remove(countOfUniqueElement);
                    } else {
                        countOfUniqueElement++;
                    }
                }
                i++;
            }
        return arrayOfDuplication;
    }
}