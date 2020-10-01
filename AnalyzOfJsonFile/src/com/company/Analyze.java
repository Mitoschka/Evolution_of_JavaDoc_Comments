package com.company;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Analyze {

    public static LinkedList<DocCommit> ArrayOfCommits = new LinkedList<>();
    public static LinkedList<LinkedList<DocCommit>> ArrayOfLog = new LinkedList<>();
    public static DocCommit[] docCommits;

    public static Map<List<String>, ArrayList<DocCommit>> dictionary = new HashMap<>();
    public static Map<List<String>, ArrayList<DocCommit>> dictionaryToLog = new HashMap<>();

    public static String zipFileName;
    public static Boolean isUnZip = false;
    public static File file1;

    public static long firstSizeInBytes = -1;
    public static long secondSizeInBytes = -3;

    public static void AnalyzeDirectory(String Path) throws InterruptedException {
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
            ArrayOfCommits.add(docCommits[0]);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            file1.delete();
        }
    }

    public static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    public static void AnalyzeElements() throws InterruptedException {
        int countOfElementThatWillBeComparedWithTheRest = 0;
        while (countOfElementThatWillBeComparedWithTheRest < ArrayOfCommits.size()) {
            int k = 0;
            while (k < ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DocSegments.size()) {
                List<String> key = Arrays.asList(ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DocSegments.get(k).Signature, ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DocSegments.get(k).Namespace, ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DocSegments.get(k).Location);
                ArrayList<JavaDocSegment> javaDocSegments = new ArrayList<>();
                javaDocSegments.add(ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DocSegments.get(k));
                DocCommit ArrayOfCommitsSegments = new DocCommit(javaDocSegments, ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).Name, ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DateTime);
                ArrayList<DocCommit> docCommitArrayList = new ArrayList<>();
                docCommitArrayList.add(ArrayOfCommitsSegments);
                if (!dictionary.containsKey(key))
                {
                    dictionary.put(key, docCommitArrayList);
                } else {
                    if (!dictionary.get(key).get(0).DocSegments.get(0).Content.equals(ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DocSegments.get(k).Content))
                    {
                        dictionary.get(key).add(0, ArrayOfCommitsSegments);
                    }
                }
                ArrayOfCommits.get(countOfElementThatWillBeComparedWithTheRest).DocSegments.remove(k);
                k++;
            }
            ArrayOfCommits.remove(countOfElementThatWillBeComparedWithTheRest);
            System.out.println("\nThere are still " + ArrayOfCommits.size() + " files left\n");
        }
            for (Map.Entry<List<String>, ArrayList<DocCommit>> evolution : dictionary.entrySet()) {
                if (evolution.getValue().size() > 1)
                {
                    List<String> key = evolution.getKey();
                    ArrayList<DocCommit> value = evolution.getValue();
                    dictionaryToLog.put(key, value);
                }
            }
            dictionary.clear();
        }
    }