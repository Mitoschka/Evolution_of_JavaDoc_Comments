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

    public static ArrayList<JavaDocSegment> DocSegments = new ArrayList<>();

    public static int count = 0;


    static final Pattern JavaDocPattern = Pattern.compile("(?s)package\\s*(.*?);|(/\\*\\*(?s:(?!\\*/).)*\\*/)(.*?)[;\\{]");


    public static void PrintDocsReport(ArrayList<JavaDocSegment> comments, String args) {
        Gson json = new Gson();
        comments.forEach(segment -> {
            comments.add(segment);
            try (PrintWriter outJson = new PrintWriter(FolderCreate.folder + args + ".json")) {
                String response = json.toJson(comments);
                outJson.println(response);
            } catch (Exception e) {
                out.println(e.getMessage());
            }
        });
    }

    public static void mainOfAnalyze(String args) {
        try {

            //Парсим все java-исходники из указанной директории в список DocSegments - типа JavaDocSegment
            ParseDirectory(FolderCreate.file + args);
            count++;

            //PrintDocsReport распечатывает в PlainComments.txt извлеченные комментарии
            PrintDocsReport(DocSegments, args);

        } catch (ConcurrentModificationException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }


    public static void ParseJavadoc(String block, String range, String date, String signature, String nameOfCommits, String namespace, String path) throws IOException {
        if (block.contains("{@inheritDoc}")) return;
        if (signature.contains("package")) return;

        if (signature != null && block.length() > 0) {
            ArrayList<String> sents = Ngrams.sanitiseToWords(block);
            if (sents.size() >= MinCommentSize) {
                JavaDocSegment segment = new JavaDocSegment(block, sents, range, date, signature, nameOfCommits, namespace, path);

                theLock.lock();
                DocSegments.add(segment);
                theLock.unlock();
            }

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
                    else
                        ParseJavadoc(matcher.group(2).intern(), matcher.start() + "-" + matcher.end(), Connect.arraylistOfDate.get(count),  matcher.group(matcher.groupCount()), Connect.arraylistOfCommits.get(count), namespace, file.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static Lock theLock = new ReentrantReadWriteLock().writeLock();
}