package com.company;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

public class MainOfAnalyze {

    public static int MinCommentSize = 10;

    public static ArrayList DocSegments = new ArrayList<>();
    public static ArrayList<String> ArrayOfNameOfCommits = new ArrayList<>();
    public static ArrayList<String> ArrayOfDateTime = new ArrayList<>();
    public static ArrayList<String> ArrayOfBlock = new ArrayList<>();
    public static ArrayList<String> ArrayOfSignature = new ArrayList<>();
    public static ArrayList<String> ArrayOfNamespace = new ArrayList<>();

    public static int count = 0;

    public static String nameOfOutJson;


    static final Pattern JavaDocPattern = Pattern.compile("(?s)package\\s*(.*?);|(/\\*\\*(?s:(?!\\*/).)*\\*/)(.*?)[;\\{]");


    public static void PrintDocsReport(ArrayList<DocCommit> comments, String args) {
        Gson json = new Gson();
        try (PrintWriter outJson = new PrintWriter(FolderCreate.folder + args + ".json")) {
            nameOfOutJson = FolderCreate.folder + args + ".json";
            String response = json.toJson(comments);
            outJson.println(response);
            while ((DocSegments.size() != 0) || (ArrayOfNameOfCommits.size() != 0)) {
                DocSegments.clear();
                ArrayOfNameOfCommits.clear();
                ArrayOfDateTime.clear();
                ArrayOfBlock.clear();
                ArrayOfNamespace.clear();
                ArrayOfSignature.clear();
            }
        } catch (Exception e) {
            out.println(e.getMessage());
        }
    }

    public static void mainOfAnalyze(String args) {
        try {

            //Парсим все java-исходники из указанной директории в список DocSegments - типа JavaDocSegment
            ParseDirectory(FolderCreate.file + args);

            Analyze();
            //PrintDocsReport распечатывает в PlainComments.txt извлеченные комментарии
            PrintDocsReport(DocSegments, args);

        } catch (ConcurrentModificationException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }


    public static void ParseJavadoc(String block, String date, String signature, String nameOfCommits, String namespace) throws IOException {
        if (block.contains("{@inheritDoc}")) return;
        if (signature.contains("package")) return;

        if (signature != null && block.length() > 0) {
            ArrayList<String> sents = Ngrams.sanitiseToWords(block);
            if (sents.size() >= MinCommentSize) {
                ArrayOfNameOfCommits.add(nameOfCommits);
                ArrayOfDateTime.add(date);
                ArrayOfBlock.add(block);
                ArrayOfNamespace.add(namespace);
                ArrayOfSignature.add(signature);
            }
        }
    }

    public static void Analyze() {
        int i = 0;
        while (i < ArrayOfNameOfCommits.size()) {
            ArrayList<JavaDocSegment> JavaDocSegments = new ArrayList<>();
            int k = 1;
            JavaDocSegments.add(new JavaDocSegment(ArrayOfBlock.get(i), ArrayOfSignature.get(i), ArrayOfNamespace.get(i)));
            while (k < ArrayOfDateTime.size()) {
                if (ArrayOfNameOfCommits.get(i).equals(ArrayOfNameOfCommits.get(k)) &&
                        (ArrayOfDateTime.get(i).equals(ArrayOfDateTime.get(k)))) {
                    JavaDocSegments.add(new JavaDocSegment(ArrayOfBlock.get(k), ArrayOfSignature.get(k), ArrayOfNamespace.get(k)));
                    k++;
                } else {
                    k++;
                }
            }
            DocCommit segment = new DocCommit(JavaDocSegments, ArrayOfNameOfCommits.get(i), ArrayOfDateTime.get(i));
            theLock.lock();
            DocSegments.add(segment);
            theLock.unlock();
            i++;
        }
    }


    public static void ParseDirectory(String Path) throws IOException {
        File path = new File(Path);
        if (path.isFile())
            ParseFile(new File(Path));
        else {
            ArrayList<File> files = new ArrayList<>();
            new com.company.DirExplorer((level, fpath, file) -> fpath.endsWith(".java"), (level, fpath, file) -> {
                files.add(file);
            }).explore(path);

            files.forEach(file -> {
                try {
                    ParseFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            count++;
        }

    }


    public static void ParseFile(File file) throws IOException {
        if (file.getAbsolutePath().contains(".java")) {
            byte[] encodedContent = Files.readAllBytes(file.toPath());
            try {
                String content = new String(encodedContent, StandardCharsets.UTF_8);
                String namespace = "";
                Matcher matcher = JavaDocPattern.matcher(content);
                while (matcher.find()) {
                    if (matcher.group(matcher.groupCount()) == null)
                        namespace = matcher.group(1);
                    else {
                        ParseJavadoc(matcher.group(2).intern(), Connect.arraylistOfDate.get(count), matcher.group(matcher.groupCount()), Connect.arraylistOfCommits.get(count), namespace);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static Lock theLock = new ReentrantReadWriteLock().writeLock();
}