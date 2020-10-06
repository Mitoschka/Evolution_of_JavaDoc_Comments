package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Connect {

    public static Thread current;

    public static List<String> arraylistOfCommits = new ArrayList();
    public static List<String> arraylistOfDownload = new ArrayList();
    public static List<String> arraylistOfDate = new ArrayList();
    public static File out;
    public static File fileToDelete;
    public static boolean isSafe = true;

    private boolean isFirst = false;

    protected void Connect(String newString, String link, String args) throws Exception, ConnectException {

        HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(newString)).openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

        String line;
        String inputLine;
        for (line = ""; (inputLine = bufferedReader.readLine()) != null; line = line + "\n" + inputLine) {
        }

        bufferedReader.close();
        Arrays.stream(line.split("\\[\\{\"sha\":\"")).skip(1L).map((L) -> {
            return L.split("\"")[0];
        }).forEach((L) -> {
            if (!isFirst) {
                arraylistOfCommits.add(L);
                isFirst = true;
            }
        });
        Arrays.stream(line.split("]},\\{\"sha\":\"")).skip(1L).map((L) -> {
            return L.split("\"")[0];
        }).forEach((L) -> {
            if (!arraylistOfCommits.contains(L)) {
                arraylistOfCommits.add(L);
            }
        });
        Arrays.stream(line.split("\"date\":\"")).skip(1L).map((G) -> {
            return G.split("\"")[0];
        }).forEach((G) -> {
            if (!arraylistOfDate.contains(G)) {
                String time = G.replace("T", " ").replace("Z", "");
                arraylistOfDate.add(time);
            }
        });

        for (String downloaded : CheckForDownloadedData.arrayOfDownloadedFiles) {
            for (String commits : arraylistOfCommits) {
                if (downloaded == commits) {
                    arraylistOfDate.remove(arraylistOfCommits.indexOf(commits));
                    arraylistOfCommits.remove(commits);
                    CheckForDownloadedData.arrayOfDownloadedFiles.remove(downloaded);
                }
            }
        }
        arraylistOfCommits.parallelStream().forEach(commit -> ParallelDownload(commit, link, args));
        UnZip.arraylist.parallelStream().forEachOrdered(commit -> ParallelParser(commit));
        UnZip.arraylist.clear();
    }

    protected void ParallelDownload(String commit, String link, String args) {
        String links = link + "/archive/" + commit + ".zip";
        out = new File(Main.pathToFile + Main.folderName + "\\" + commit + ".zip");
        out.deleteOnExit();
        if (!args.equals("\\")) {
            isSafe = false;
        } else {
            isSafe = true;
        }
        Download.DownloadZipFileOfCommit(links, out, args);
    }

    protected void ParallelParser(String commit) {
        MainOfAnalyze mainOfAnalyze = new MainOfAnalyze();
        mainOfAnalyze.mainOfAnalyze(commit);
        fileToDelete = new File(FolderCreate.folder + commit);
        try {
            ZipFile.Zip();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ZipFile.jsonToDelete.delete();
        while (fileToDelete.exists()) {
            DeleteDirectory.DeleteDirectory(commit);
        }
        if (ZipFile.zipToDelete.length() < 400) {
            ZipFile.zipToDelete.delete();
            while (ZipFile.zipToDelete.exists()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ZipFile.zipToDelete.delete();
            }
        }
        arraylistOfCommits.remove(0);
        arraylistOfDate.remove(0);
    }
}
