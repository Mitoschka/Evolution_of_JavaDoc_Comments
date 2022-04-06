package com.company;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class Analyze {

    public static LinkedList<DocCommit> ArrayOfCommits = new LinkedList<>();
    public static LinkedList<DocCommit> Test = new LinkedList<>();
    public static LinkedList<DocCommit> SortedArrayOfCommits = new LinkedList<>();

    public static Map<List<String>, ArrayList<DocCommit>> listOfTypos = new LinkedHashMap<>();
    public static Map<List<String>, ArrayList<DocCommit>> listOfTyposToLog = new LinkedHashMap<>();

    public static LinkedList<String> UniqueContent = new LinkedList<>();

    public static DocCommit[] docCommits;

    public static Map<List<String>, ArrayList<DocCommit>> dictionary = new LinkedHashMap<>();
    public static Map<List<String>, ArrayList<DocCommit>> dictionaryToLog = new LinkedHashMap<>();

    public static String zipFileName;
    public static Boolean isUnZip = false;
    public static File file1;

    private static Boolean isLastCommit = false;

    public static long firstSizeInBytes = -1;
    public static long secondSizeInBytes = -3;

    private static DocCommit ArrayOfCommitsSegmentsToDictionary;

    public static void AnalyzeDirectory(String Path) {
        File path = new File(Path);
        if (path.isFile())
            AnalyzeFile(new File(Path));
        else {
            LinkedList<File> files = new LinkedList<>();
            new com.company.DirExplorer((level, fpath, file) -> fpath.endsWith(".zip"), (level, fpath, file) -> files.add(file)).explore(path);

            File lastCommit = files.getLast();

            files.parallelStream().forEachOrdered(file -> {
                try {
                    if (file.equals(lastCommit)) {
                        isLastCommit = true;
                    }
                    isUnZip = false;
                    file1 = new File("D:\\JSON Folder\\" + UnZip.UnZip(file));
                    while (!isUnZip) {
                        while (firstSizeInBytes != secondSizeInBytes) {
                            firstSizeInBytes = file1.length();
                            Thread.sleep(50);
                            secondSizeInBytes = file1.length();
                            isUnZip = false;
                        }
                        AnalyzeFile(file1);
                        isUnZip = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //DateSort();
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
            if (!isLastCommit) {
                file1.delete();
            }
        }
    }

    public static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

   /* public static void DateSort() {
        ArrayList<String> dateString = new ArrayList<>();
        for (DocCommit element : ArrayOfCommits) {
            dateString.add(element.DateTime);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateString.sort(Comparator.comparing(s -> LocalDateTime.parse(s, formatter)));
        while (dateString.size() != 0) {
            int i = 0;
            while (!ArrayOfCommits.get(i).DateTime.equals(dateString.get(0))) {
                i++;
            }
            if (ArrayOfCommits.get(i).DateTime.equals(dateString.get(0))) {
                SortedArrayOfCommits.add(ArrayOfCommits.get(i));
                dateString.remove(0);
            }
        }
    }*/

    public static void AnalyzeElements() {
        Iterator<DocCommit> docCommitIterator = ArrayOfCommits.iterator();
        while (docCommitIterator.hasNext()) {
            DocCommit docCommit = docCommitIterator.next();
            Iterator<JavaDocSegment> commitIterator = docCommit.DocSegments.iterator();
            while (commitIterator.hasNext()) {
                try {
                    JavaDocSegment commit = commitIterator.next();
                    if (commit.Location.contains("JSON Folder")) {
                        String find = "\\";
                        for (int i = 0; i < 2; i++) {
                            commit.Location = commit.Location.substring(commit.Location.indexOf(find) + 1);
                        }
                    }
                    List<String> key = Arrays.asList(commit.Signature, commit.Namespace, commit.Location);
                    ArrayList<JavaDocSegment> javaDocSegments = new ArrayList<>();
                    javaDocSegments.add(commit);
                    DocCommit ArrayOfCommitsSegments = new DocCommit(javaDocSegments, docCommit.Name, docCommit.DateTime);
                    ArrayList<DocCommit> docCommitArrayList = new ArrayList<>();
                    ArrayList<DocCommit> docCommitArrayListTypos = new ArrayList<>();
                    docCommitArrayList.add(ArrayOfCommitsSegments);
                    Test.add(ArrayOfCommitsSegments);

                    if (!listOfTypos.containsKey(key)) {
                        listOfTypos.put(key, docCommitArrayList);
                    }
                    if (!dictionary.containsKey(key)) {
                        dictionary.put(key, new ArrayList<DocCommit>(docCommitArrayList));
                    } else {
                        if (!dictionary.get(key).get(dictionary.get(key).size() - 1).DocSegments.get(dictionary.get(key).get(dictionary.get(key).size() - 1).DocSegments.size() - 1).Content.equals(ArrayOfCommitsSegments.DocSegments.get(0).Content)
                                & (!dictionary.get(key).get(dictionary.get(key).size() - 1).Name.equals(ArrayOfCommitsSegments.Name)
                                & (!dictionary.get(key).get(dictionary.get(key).size() - 1).DateTime.equals(ArrayOfCommitsSegments.DateTime)))) {
                            String dictionaryContent = dictionary.get(key).get(dictionary.get(key).size() - 1).DocSegments.get(dictionary.get(key).get(dictionary.get(key).size() - 1).DocSegments.size() - 1).Content;
                            UniqueContent = new LinkedList<>();
                            for (DocCommit item : dictionary.get(key)) {
                                UniqueContent.add(item.DocSegments.get(0).Content);
                            }
                            if (UniqueContent.size() != 0) {
                                dictionaryContent = UniqueContent.getLast().replace("{{", "").replace("}}", "");
                            }
                            if (dictionaryContent == commit.Content) {
                                continue;
                            }
                            String[] spacesForSecondString = commit.Content.toLowerCase().replaceAll("  ", " ").split("");
                            ArrayList<String> listOfSpacesForSecondString = new ArrayList<>();
                            Collections.addAll(listOfSpacesForSecondString, spacesForSecondString);
                            String[] firstString = dictionaryContent.toLowerCase().replaceAll(" ", "").split("");
                            String[] secondString = commit.Content.toLowerCase().replaceAll(" ", "").split("");
                            String firstStringWithoutSpace = dictionaryContent.toLowerCase().replaceAll(" ", "");
                            String secondStringWithoutSpace = commit.Content.toLowerCase().replaceAll(" ", "");
                            if (firstStringWithoutSpace.equals(secondStringWithoutSpace)) {
                                continue;
                            }
                            ArrayList<String> firstStringTemp = new ArrayList<>();
                            for (String item : firstString) {
                                if (!item.equals("")) {
                                    firstStringTemp.add(item);
                                }
                            }
                            ArrayList<String> secondStringTemp = new ArrayList<>();
                            for (String item : secondString) {
                                if (!item.equals("")) {
                                    secondStringTemp.add(item);
                                }
                            }
                            if (firstStringTemp.equals(secondStringTemp)) {
                                continue;
                            }
                            firstString = firstStringTemp.toArray(new String[0]);
                            secondString = secondStringTemp.toArray(new String[0]);
                            int FirstOfDifference = 0;
                            while (firstString.length - 1 > FirstOfDifference && secondString.length - 1 > FirstOfDifference) {
                                if (firstString[FirstOfDifference].equals(secondString[FirstOfDifference])) {
                                    FirstOfDifference++;
                                } else {
                                    firstString[FirstOfDifference] = "{{" + firstString[FirstOfDifference];
                                    secondString[FirstOfDifference] = "{{" + secondString[FirstOfDifference];
                                    break;
                                }
                            }
                            if (firstString.length - 1 == FirstOfDifference || secondString.length - 1 == FirstOfDifference) {
                                firstString[FirstOfDifference] = firstString[FirstOfDifference] + "{{";
                                secondString[FirstOfDifference] = secondString[FirstOfDifference] + "{{";
                            }
                            ArrayUtils.reverse(firstString);
                            ArrayUtils.reverse(secondString);
                            int LastOfDifference = 0;
                            while (firstString.length - 1 > LastOfDifference && secondString.length - 1 > LastOfDifference) {
                                if (firstString[LastOfDifference].equals(secondString[LastOfDifference])) {
                                    LastOfDifference++;
                                } else {
                                    firstString[LastOfDifference] = firstString[LastOfDifference] + "}}";
                                    secondString[LastOfDifference] = secondString[LastOfDifference] + "}}";
                                    break;
                                }
                            }
                            if (firstString.length - 1 == LastOfDifference || secondString.length - 1 == LastOfDifference) {
                                firstString[LastOfDifference] = "}}" + firstString[LastOfDifference];
                                secondString[LastOfDifference] = "}}" + secondString[LastOfDifference];
                            }
                            ArrayUtils.reverse(firstString);
                            ArrayUtils.reverse(secondString);
                            LastOfDifference = (secondString.length - 1) - LastOfDifference;
                            ArrayList<String> resultOfStrings = new ArrayList<>();
                            for (int i = 0; i < secondString.length; i++) {
                                resultOfStrings.add(secondString[i]);
                            }
                            UniqueContent.add(String.join("", resultOfStrings));
                            JavaDocSegment commitToDictionary = new JavaDocSegment(commit.Content, commit.NGrams, commit.Range, commit.Signature, commit.Namespace, commit.Location);
                            ArrayList<JavaDocSegment> javaDocSegmentsToDictionary = new ArrayList<>();
                            javaDocSegmentsToDictionary.add(commitToDictionary);
                            ArrayOfCommitsSegmentsToDictionary = new DocCommit(javaDocSegmentsToDictionary, docCommit.Name, docCommit.DateTime);
                            int spaceCount = 0;
                            int firstOfDifferenceSpaceCount = 0;
                            int secondOfDifferenceSpaceCount = 0;
                            for (int i = 0; i < listOfSpacesForSecondString.size(); i++) {
                                if (listOfSpacesForSecondString.get(i).equals(" ")) {
                                    resultOfStrings.add(i, " ");
                                    spaceCount++;
                                }
                                if (i == FirstOfDifference + spaceCount) {
                                    firstOfDifferenceSpaceCount = spaceCount;
                                }
                                if (i == LastOfDifference + spaceCount) {
                                    secondOfDifferenceSpaceCount = spaceCount;
                                }
                            }

                            if (LastOfDifference == 0 && secondOfDifferenceSpaceCount == 0) {
                                secondOfDifferenceSpaceCount = firstOfDifferenceSpaceCount;
                            }

                            //Собираю строку, которая не совпала, для последующей её классификации "Typos"
                            ArrayList<String> mismatch = new ArrayList<>();
                            for (int i = FirstOfDifference; i < LastOfDifference; i++) {
                                mismatch.add(secondString[i]);
                            }

                            for (String item : resultOfStrings) {
                                if (item.matches("^.*\\{\\{.\\}\\}.*$")) {
                                    mismatch.add(item);
                                }
                            }

                            ArrayOfCommitsSegmentsToDictionary.DocSegments.get(0).Content = String.join("", resultOfStrings);
                            dictionary.get(key).add((dictionary.get(key).size()), ArrayOfCommitsSegmentsToDictionary);

                            //Классификация по отбору опечаток
                            boolean isContain = false;
                            docCommitArrayListTypos.add(ArrayOfCommitsSegmentsToDictionary);
                            if ((mismatch.size() == 1 && !mismatch.get(0).matches("[\\p{Punct}\\p{Space}\\p{Blank}]"))) {
                                for (DocCommit current : listOfTypos.get(key)) {
                                    if (current.equals(ArrayOfCommitsSegmentsToDictionary)) {
                                        isContain = true;
                                        break;
                                    }
                                }
                                if (!isContain) {
                                    listOfTypos.get(key).add(ArrayOfCommitsSegmentsToDictionary);
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                    commitIterator.remove();
                } catch (NullPointerException exception) {
                    exception.fillInStackTrace();
                }
            }
            docCommitIterator.remove();
            System.out.println("\nThere are still " + ArrayOfCommits.size() + " files left\n");
        }
        /*
        for (Map.Entry<List<String>, ArrayList<DocCommit>> evolution : dictionary.entrySet()) {
            if (evolution.getValue().size() > 1) {
                List<String> key = evolution.getKey();
                ArrayList<DocCommit> value = evolution.getValue();
                dictionaryToLog.put(key, value);
            }
        }
*/
        for (Map.Entry<List<String>, ArrayList<DocCommit>> evolution : listOfTypos.entrySet()) {
            if (evolution.getValue().size() > 1) {
                List<String> key = evolution.getKey();
                ArrayList<DocCommit> value = evolution.getValue();
                listOfTyposToLog.put(key, value);
                //Удаляю ключи, связанные с Typos из общего словаря
                if (dictionary.containsKey(key)) {
                    dictionary.remove(key);
                }
            }
        }
        //dictionary.clear();
    }
}