package com.company;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

public class MainOfAnalyze {

    public static int MinCommentSize = 10;

    public static ArrayList DocSegments;

    public static String nameOfOutJson;

    static final Pattern JavaDocPattern = Pattern.compile("(?s)package\\s*(.*?);|(/\\*\\*(?s:(?!\\*/).)*\\*/)(.*?)[;\\{]");

    public static void PrintDocsReport(ArrayList<DocCommit> comments, String args) {
        Gson json = new GsonBuilder().setPrettyPrinting().create();
        try (PrintWriter outJson = new PrintWriter(FolderCreate.folder + args + ".json")) {
            nameOfOutJson = FolderCreate.folder + args + ".json";
            String response = json.toJson(comments);
            outJson.println(response);
        } catch (Exception e) {
            out.println(e.getMessage());
        }
    }

    public static void mainOfAnalyze(String args, String commit, String date) {
        try {
            DocSegments = new ArrayList<>();
            String commitName = args.substring(args.lastIndexOf("-") + 1);
            String repositoryName = args.substring(args.length() - args.length(), args.lastIndexOf("-") + 1);
            String dateName = Main.dateToParse.peek().split(" ")[0];
            String sourceFile = repositoryName + dateName + "_" + commitName;

            //Парсим все java-исходники из указанной директории в список DocSegments - типа JavaDocSegment
            ParseDirectory(FolderCreate.temporaryFolder + args, commit, date);

            //PrintDocsReport распечатывает в PlainComments.txt извлеченные комментарии
            PrintDocsReport(DocSegments, sourceFile);

        } catch (ConcurrentModificationException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void ParseJavadoc(String block, String range, String signature, String namespace, String path, Queue<JavaDocSegment> JavaDocSegments) throws IOException {
        if (block.contains("{@inheritDoc}")) return;
        if (signature.contains("package")) return;

        if (signature != null && block.length() > 0) {
            ArrayList<String> sents = Ngrams.sanitiseToWords(block);
            if (sents.size() >= MinCommentSize) {
                JavaDocSegments.add(new JavaDocSegment(block, sents, range, signature, namespace, path, JavaDocSegments.size()));
            }
        }
    }

    public static void ParseDirectory(String Path, String commit, String date) throws IOException {
        Queue<JavaDocSegment> JavaDocSegments = new ConcurrentLinkedQueue<>();
        File path = new File(Path);
        if (path.isFile())
            ParseFile(new File(Path), JavaDocSegments);
        else {
            ArrayList<File> files = new ArrayList<>();
            new com.company.DirExplorer((level, fpath, file) -> fpath.endsWith(".java"), (level, fpath, file) -> {
                files.add(file);
            }).explore(path);

            files.parallelStream().forEach(file -> {
                try {
                    ParseFile(file, JavaDocSegments);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            DocCommit segment = new DocCommit(JavaDocSegments, commit, date);
            theLock.lock();
            DocSegments.add(segment);
            theLock.unlock();
        }

    }

    public static void ParseFile(File file, Queue<JavaDocSegment> JavaDocSegments) throws IOException {
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
                        ParseJavadoc(matcher.group(2).intern(), matcher.start() + "-" + matcher.end(), matcher.group(matcher.groupCount()), namespace, file.getAbsolutePath(), JavaDocSegments);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static Lock theLock = new ReentrantReadWriteLock().writeLock();
}